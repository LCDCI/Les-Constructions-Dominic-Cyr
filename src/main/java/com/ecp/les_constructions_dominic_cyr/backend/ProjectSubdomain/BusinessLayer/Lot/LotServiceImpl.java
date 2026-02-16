package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Lot;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.NotificationService;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.MailerServiceClient;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.NotificationCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LotServiceImpl implements LotService {
    private final LotRepository lotRepository;
    private final UsersRepository usersRepository;
    private final ProjectRepository projectRepository;
    private final NotificationService notificationService;
    private final MailerServiceClient mailerServiceClient;

    @Override
    public List<LotResponseModel> getAllLots() {
        List<Lot> lots = lotRepository.findAll();
        return mapLotsToResponses(lots);
    }

    @Override
    public List<LotResponseModel> getAllLotsByProject(String projectIdentifier) {
        if (projectIdentifier == null || projectIdentifier.isBlank()) {
            throw new InvalidInputException("Project identifier must not be blank");
        }
        List<Lot> lots = lotRepository.findByProject_ProjectIdentifier(projectIdentifier);
        return mapLotsToResponses(lots);
    }

    @Override
    public List<LotResponseModel> getLotsByProjectAndBothUsersAssigned(String projectIdentifier, String salespersonId,
            String customerId) {
        if (projectIdentifier == null || projectIdentifier.isBlank()) {
            throw new InvalidInputException("Project identifier must not be blank");
        }
        if (salespersonId == null || salespersonId.isBlank()) {
            throw new InvalidInputException("Salesperson ID must not be blank");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new InvalidInputException("Customer ID must not be blank");
        }

        UUID salespersonUuid = UUID.fromString(salespersonId);
        UUID customerUuid = UUID.fromString(customerId);

        List<Lot> lots = lotRepository.findByProjectAndBothUsersAssigned(projectIdentifier, salespersonUuid,
                customerUuid);
        return mapLotsToResponses(lots);
    }

    @Override
    public LotResponseModel getLotById(String lotId) {
        UUID lotUuid = UUID.fromString(lotId);
        Lot lot = lotRepository.findByLotIdentifier_LotId(lotUuid);

        if (lot == null)
            throw new NotFoundException("Unknown Lot Id: " + lotId);

        return mapToResponse(lot);
    }

    @Override
    @Transactional
    public LotResponseModel addLotToProject(String projectIdentifier, LotRequestModel lotRequestModel) {
        log.info("Creating lot for project identifier: {}", projectIdentifier);

        // Find the project
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new NotFoundException("Project not found with identifier: " + projectIdentifier));

        log.info("Found project: {} with projectId: {}", project.getProjectName(), project.getProjectIdentifier());

        validateLotRequest(lotRequestModel);

        Lot lot = new Lot(
                new LotIdentifier(),
                lotRequestModel.getLotNumber(),
                lotRequestModel.getCivicAddress(),
                lotRequestModel.getPrice(),
                lotRequestModel.getDimensionsSquareFeet(),
                lotRequestModel.getDimensionsSquareMeters(),
                lotRequestModel.getLotStatus());

        // Set the project entity reference (like Schedule does)
        lot.setProject(project);

        log.info("Set project on lot: {}", project.getProjectIdentifier());

        // Handle multiple user assignments (any role)
        if (lotRequestModel.getAssignedUserIds() != null && !lotRequestModel.getAssignedUserIds().isEmpty()) {
            List<Users> assignedUsers = getUsersByIds(lotRequestModel.getAssignedUserIds());
            lot.setAssignedUsers(assignedUsers);

            // Automatically set status to RESERVED when users are assigned (if not SOLD)
            if (lot.getLotStatus() != LotStatus.SOLD) {
                lot.setLotStatus(LotStatus.RESERVED);
                log.info("Lot status automatically set to RESERVED due to user assignment");
            }

            // Notify assigned users
            notifyAssignedUsers(assignedUsers, lot, project);
        }

        Lot savedLot = lotRepository.save(lot);
        return mapToResponse(savedLot);
    }

    @Override
    @Transactional
    public LotResponseModel updateLot(LotRequestModel lotRequestModel, String lotId) {
        UUID lotUuid = UUID.fromString(lotId);
        Lot foundLot = lotRepository.findByLotIdentifier_LotId(lotUuid);
        if (foundLot == null) {
            throw new NotFoundException("Unknown Lot Id: " + lotId);
        }

        validateLotRequest(lotRequestModel);
        foundLot.setLotNumber(lotRequestModel.getLotNumber());
        foundLot.setCivicAddress(lotRequestModel.getCivicAddress());
        foundLot.setPrice(lotRequestModel.getPrice());
        foundLot.setDimensionsSquareFeet(lotRequestModel.getDimensionsSquareFeet());
        foundLot.setDimensionsSquareMeters(lotRequestModel.getDimensionsSquareMeters());
        foundLot.setLotStatus(lotRequestModel.getLotStatus());

        // Handle multiple user assignments update
        if (lotRequestModel.getAssignedUserIds() != null && !lotRequestModel.getAssignedUserIds().isEmpty()) {
            List<Users> currentAssignedUsers = foundLot.getAssignedUsers() != null ? foundLot.getAssignedUsers()
                    : new ArrayList<>();
            List<Users> assignedUsers = getUsersByIds(lotRequestModel.getAssignedUserIds());
            foundLot.setAssignedUsers(assignedUsers);

            // Automatically set status to RESERVED when users are assigned (if not SOLD)
            if (foundLot.getLotStatus() != LotStatus.SOLD) {
                foundLot.setLotStatus(LotStatus.RESERVED);
                log.info("Lot status automatically set to RESERVED due to user assignment");
            }

            // Calculate new assignments and notify
            List<String> currentIds = currentAssignedUsers.stream()
                    .map(u -> u.getUserIdentifier().getUserId().toString())
                    .collect(Collectors.toList());

            List<Users> usersToNotify = assignedUsers.stream()
                    .filter(u -> !currentIds.contains(u.getUserIdentifier().getUserId().toString()))
                    .collect(Collectors.toList());

            if (!usersToNotify.isEmpty()) {
                // We need the project for the link
                Project project = foundLot.getProject();
                notifyAssignedUsers(usersToNotify, foundLot, project);
            }
        } else {
            // When unassigning all users, revert to AVAILABLE unless it's SOLD
            foundLot.setAssignedUsers(new ArrayList<>());
            if (foundLot.getLotStatus() == LotStatus.RESERVED) {
                foundLot.setLotStatus(LotStatus.AVAILABLE);
                log.info("Lot status automatically set to AVAILABLE due to user unassignment");
            }
        }

        Lot updatedLot = lotRepository.save(foundLot);
        return mapToResponse(updatedLot);
    }

    @Override
    @Transactional
    public void deleteLot(String lotId) {
        UUID lotUuid = UUID.fromString(lotId);
        Lot foundLot = lotRepository.findByLotIdentifier_LotId(lotUuid);
        if (foundLot == null) {
            throw new NotFoundException("Unknown Lot Id: " + lotId);
        }
        lotRepository.delete(foundLot);
    }

    private List<Users> getUsersByIds(List<String> userIds) {
        List<Users> users = new ArrayList<>();
        for (String userId : userIds) {
            try {
                UUID userUuid = UUID.fromString(userId);
                Users user = usersRepository.findByUserIdentifier_UserId(userUuid)
                        .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
                users.add(user);
            } catch (IllegalArgumentException e) {
                throw new InvalidInputException("Invalid user ID format: " + userId);
            }
        }
        return users;
    }

    private LotResponseModel mapToResponse(Lot lot) {
        LotResponseModel dto = new LotResponseModel();
        dto.setId(lot.getId());
        dto.setLotId(lot.getLotIdentifier() != null ? lot.getLotIdentifier().getLotId().toString()
                : UUID.randomUUID().toString());
        dto.setLotNumber(lot.getLotNumber());
        dto.setCivicAddress(lot.getCivicAddress());
        dto.setPrice(lot.getPrice());
        dto.setDimensionsSquareFeet(lot.getDimensionsSquareFeet());
        dto.setDimensionsSquareMeters(lot.getDimensionsSquareMeters());
        dto.setLotStatus(lot.getLotStatus());

        // Map project information (like Schedule does)
        if (lot.getProject() != null) {
            dto.setProjectId(lot.getProject().getProjectId());
            dto.setProjectIdentifier(lot.getProject().getProjectIdentifier());
            dto.setProjectName(lot.getProject().getProjectName());
        }

        // Map assigned users
        if (lot.getAssignedUsers() != null && !lot.getAssignedUsers().isEmpty()) {
            List<LotResponseModel.AssignedUserInfo> assignedUserInfoList = lot.getAssignedUsers().stream()
                    .map(user -> LotResponseModel.AssignedUserInfo.builder()
                            .userId(user.getUserIdentifier().getUserId().toString())
                            .fullName(user.getFirstName() + " " + user.getLastName())
                            .email(user.getPrimaryEmail())
                            .role(user.getUserRole() != null ? user.getUserRole().name() : "UNKNOWN")
                            .build())
                    .collect(Collectors.toList());
            dto.setAssignedUsers(assignedUserInfoList);
        } else {
            dto.setAssignedUsers(new ArrayList<>());
        }

        // Calculate progress percentage
        int totalUpcomingWork = 59;
        int remaining = lot.getRemainingUpcomingWork() != null ? lot.getRemainingUpcomingWork() : totalUpcomingWork;
        int completed = totalUpcomingWork - remaining;
        dto.setProgressPercentage((int) Math.round((double) completed / totalUpcomingWork * 100));

        return dto;
    }

    public List<LotResponseModel> mapLotsToResponses(List<Lot> lots) {
        return lots.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void validateLotRequest(LotRequestModel requestModel) {
        if (requestModel.getLotNumber() == null || requestModel.getLotNumber().isBlank()) {
            throw new InvalidInputException("Lot number is required");
        }
        if (requestModel.getCivicAddress() == null || requestModel.getCivicAddress().isBlank()) {
            throw new InvalidInputException("Civic address is required");
        }
        if (requestModel.getLotStatus() == null) {
            throw new InvalidInputException("Lot status is required");
        }
    }

    private void notifyAssignedUsers(List<Users> users, Lot lot, Project project) {
        for (Users user : users) {
            try {
                // Create portal notification - link to projects page
                String link = "/projects";

                String lotId = lot.getLotIdentifier() != null
                        ? lot.getLotIdentifier().getLotId().toString()
                        : lot.getId().toString();

                notificationService.createNotification(
                        user.getUserIdentifier().getUserId(),
                        "Assigned to Lot " + lot.getLotNumber(),
                        "You have been assigned to lot " + lot.getLotNumber() + " at " + lot.getCivicAddress(),
                        NotificationCategory.LOT_ASSIGNED,
                        link);

                // Send email with improved template
                String emailBody = buildLotAssignmentEmailTemplate(
                        user.getFirstName() + " " + user.getLastName(),
                        lot.getLotNumber(),
                        lot.getCivicAddress(),
                        project.getProjectName(),
                        lotId,
                        project.getProjectIdentifier());

                mailerServiceClient.sendEmail(
                        user.getPrimaryEmail(),
                        "You've Been Assigned to Lot " + lot.getLotNumber(),
                        emailBody,
                        null).subscribe();
            } catch (Exception e) {
                log.error("Failed to notify user {}: {}", user.getUserIdentifier().getUserId(), e.getMessage());
            }
        }
    }

    private String buildLotAssignmentEmailTemplate(String userName, String lotNumber, String address,
            String projectName, String lotId, String projectIdentifier) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "</head>" +
                "<body style=\"margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4;\">"
                +
                "<div style=\"max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);\">"
                +

                "<!-- Header -->" +
                "<div style=\"background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 40px 30px; text-align: center;\">"
                +
                "<h1 style=\"color: #ffffff; margin: 0; font-size: 28px; font-weight: 600;\">New Lot Assignment</h1>" +
                "</div>" +

                "<!-- Content -->" +
                "<div style=\"padding: 40px 30px;\">" +
                "<p style=\"color: #333333; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;\">Hello " + userName
                + ",</p>" +

                "<p style=\"color: #333333; font-size: 16px; line-height: 1.6; margin: 0 0 30px 0;\">" +
                "You have been assigned to a new lot. Here are the details:" +
                "</p>" +

                "<!-- Lot Details Card -->" +
                "<div style=\"background-color: #f8f9fa; border-left: 4px solid #667eea; padding: 20px; margin: 0 0 30px 0; border-radius: 4px;\">"
                +
                "<div style=\"margin-bottom: 15px;\">" +
                "<span style=\"color: #666666; font-size: 14px; display: block; margin-bottom: 5px;\">Lot Number</span>"
                +
                "<span style=\"color: #333333; font-size: 18px; font-weight: 600;\">" + lotNumber + "</span>" +
                "</div>" +
                "<div style=\"margin-bottom: 15px;\">" +
                "<span style=\"color: #666666; font-size: 14px; display: block; margin-bottom: 5px;\">Address</span>" +
                "<span style=\"color: #333333; font-size: 16px;\">" + address + "</span>" +
                "</div>" +
                "<div>" +
                "<span style=\"color: #666666; font-size: 14px; display: block; margin-bottom: 5px;\">Project</span>" +
                "<span style=\"color: #333333; font-size: 16px;\">" + projectName + "</span>" +
                "</div>" +
                "</div>" +

                "<!-- CTA Button -->" +
                "<div style=\"text-align: center; margin: 30px 0;\">" +
                "<a href=\"" + getPortalBaseUrl() + "/projects/" + projectIdentifier + "/lots/" + lotId
                + "/documents\" " +
                "style=\"display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #ffffff; "
                +
                "text-decoration: none; padding: 14px 40px; border-radius: 6px; font-size: 16px; font-weight: 600; " +
                "box-shadow: 0 4px 6px rgba(102, 126, 234, 0.3);\">" +
                "View Lot Documents" +
                "</a>" +
                "</div>" +

                "<p style=\"color: #666666; font-size: 14px; line-height: 1.6; margin: 30px 0 0 0; text-align: center;\">"
                +
                "If you have any questions, please contact your project manager." +
                "</p>" +
                "</div>" +

                "<!-- Footer -->" +
                "<div style=\"background-color: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #e9ecef;\">"
                +
                "<p style=\"color: #999999; font-size: 12px; margin: 0;\">© " + java.time.Year.now().getValue()
                + " Les Constructions Dominic Cyr. All rights reserved.</p>" +
                "</div>" +

                "</div>" +
                "</body>" +
                "</html>";
    }

    private String getPortalBaseUrl() {
        // You can make this configurable via environment variable or
        // application.properties
        String baseUrl = System.getenv("PORTAL_BASE_URL");
        return baseUrl != null && !baseUrl.isEmpty() ? baseUrl : "https://portal.lesconstructionsdominiccyr.com";
    }
}

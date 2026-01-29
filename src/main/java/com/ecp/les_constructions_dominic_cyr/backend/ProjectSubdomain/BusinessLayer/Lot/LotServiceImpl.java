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
public class LotServiceImpl implements LotService{
    private final LotRepository lotRepository;
    private final UsersRepository usersRepository;
    private final ProjectRepository projectRepository;

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
    public LotResponseModel getLotById(String lotId) {
        Lot lot = lotRepository.findByLotIdentifier_LotId(lotId);

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
                lotRequestModel.getLotStatus()
        );

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
        }

        Lot savedLot = lotRepository.save(lot);
        return mapToResponse(savedLot);
    }

    @Override
    @Transactional
    public LotResponseModel updateLot(LotRequestModel lotRequestModel, String lotId) {
        Lot foundLot = lotRepository.findByLotIdentifier_LotId(lotId);
        if(foundLot == null){
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
            List<Users> assignedUsers = getUsersByIds(lotRequestModel.getAssignedUserIds());
            foundLot.setAssignedUsers(assignedUsers);

            // Automatically set status to RESERVED when users are assigned (if not SOLD)
            if (foundLot.getLotStatus() != LotStatus.SOLD) {
                foundLot.setLotStatus(LotStatus.RESERVED);
                log.info("Lot status automatically set to RESERVED due to user assignment");
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
        Lot foundLot = lotRepository.findByLotIdentifier_LotId(lotId);
        if(foundLot == null){
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
        dto.setLotId(lot.getLotIdentifier().getLotId());
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

        return dto;
    }

    private List<LotResponseModel> mapLotsToResponses(List<Lot> lots) {
        List<LotResponseModel> responseList = new ArrayList<>();
        for (Lot lot : lots) {
            responseList.add(mapToResponse(lot));
        }
        return responseList;
    }

    private void validateLotRequest(LotRequestModel lotRequestModel) {
        if (lotRequestModel.getLotNumber() == null || lotRequestModel.getLotNumber().isBlank()) {
            throw new InvalidInputException("Lot number must not be blank");
        }
        if (lotRequestModel.getCivicAddress() == null || lotRequestModel.getCivicAddress().isBlank()) {
            throw new InvalidInputException("Civic address must not be blank");
        }
        // Price is optional - only validate if provided
        if (lotRequestModel.getPrice() != null && lotRequestModel.getPrice() < 0) {
            throw new InvalidInputException("Price cannot be negative");
        }
        if (lotRequestModel.getDimensionsSquareFeet() == null || lotRequestModel.getDimensionsSquareFeet().isBlank()) {
            throw new InvalidInputException("Dimensions in square feet must not be blank");
        }
        if (lotRequestModel.getDimensionsSquareMeters() == null || lotRequestModel.getDimensionsSquareMeters().isBlank()) {
            throw new InvalidInputException("Dimensions in square meters must not be blank");
        }
        if (lotRequestModel.getLotStatus() == null) {
            throw new InvalidInputException("Lot status must not be null");
        }
    }
}

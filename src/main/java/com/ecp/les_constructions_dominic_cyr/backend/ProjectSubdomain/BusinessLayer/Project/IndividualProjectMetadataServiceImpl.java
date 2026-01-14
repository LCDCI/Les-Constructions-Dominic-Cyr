package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.AssignedUsersDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.IndividualProjectResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.UserSummaryDTO;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.ForbiddenAccessException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndividualProjectMetadataServiceImpl implements IndividualProjectMetadataService{
    private static final Logger log = LoggerFactory.getLogger(IndividualProjectMetadataServiceImpl.class);
    private final ProjectRepository projectRepository;
    private final UsersRepository usersRepository;
    private final LotRepository lotRepository;

    @Override
    public IndividualProjectResponseModel getProjectMetadata(String projectIdentifier, String auth0UserId) {
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with identifier: " + projectIdentifier));

        Users requestingUser = usersRepository.findByAuth0UserId(auth0UserId)
                .orElseThrow(() -> new ForbiddenAccessException("You are not authorized to access this project"));

        validateAccess(project, requestingUser);

        String location = determineProjectLocation(project);
        AssignedUsersDTO assignedUsers = buildAssignedUsers(project);

        return IndividualProjectResponseModel.builder()
                .projectIdentifier(project.getProjectIdentifier())
                .projectName(project.getProjectName())
                .projectDescription(project.getProjectDescription())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .completionDate(project.getCompletionDate())
                .primaryColor(project.getPrimaryColor())
                .tertiaryColor(project.getTertiaryColor())
                .buyerColor(project.getBuyerColor())
                .buyerName(project.getBuyerName())
                .imageIdentifier(project.getImageIdentifier())
                .location(location)
                .progressPercentage(project.getProgressPercentage())
                .assignedUsers(assignedUsers)
                .build();
    }

    private void validateAccess(Project project, Users requestingUser) {
        UserRole role = requestingUser.getUserRole();
        String userId = requestingUser.getUserIdentifier().getUserId().toString();

        if (role == UserRole.OWNER) {
            return;
        }

        boolean hasAccess = false;

        if (role == UserRole.CONTRACTOR && userId.equals(project.getContractorId())) {
            hasAccess = true;
        } else if (role == UserRole.SALESPERSON && userId.equals(project.getSalespersonId())) {
            hasAccess = true;
        } else if (role == UserRole.CUSTOMER && userId.equals(project.getCustomerId())) {
            hasAccess = true;
        }

        if (!hasAccess) {
            throw new ForbiddenAccessException("You do not have access to this project");
        }
    }

    private String determineProjectLocation(Project project) {
        if (project.getLocation() != null && !project.getLocation().isEmpty()) {
            return project.getLocation();
        }

        List<String> lotIdentifiers = project.getLotIdentifiers();
        if (lotIdentifiers != null && !lotIdentifiers.isEmpty()) {
            Lot firstLot = lotRepository.findByLotIdentifier_LotId(lotIdentifiers.get(0));
            if (firstLot != null && firstLot.getLocation() != null) {
                return firstLot.getLocation();
            }
        }

        return "Location not specified";
    }

    private AssignedUsersDTO buildAssignedUsers(Project project) {
        UserSummaryDTO contractor = project.getContractorId() != null
                ? buildUserSummary(project.getContractorId())
                : null;

        UserSummaryDTO salesperson = project.getSalespersonId() != null
                ? buildUserSummary(project.getSalespersonId())
                : null;

        UserSummaryDTO customer = project.getCustomerId() != null
                ? buildUserSummary(project.getCustomerId())
                : null;

        return AssignedUsersDTO.builder()
                .contractor(contractor)
                .salesperson(salesperson)
                .customer(customer)
                .build();
    }

    private UserSummaryDTO buildUserSummary(String userId) {
       UserIdentifier userIdentifier = parseUserIdentifier(userId);

        if (userIdentifier == null) {
              return null;
        }

        try {
            return usersRepository.findById(userIdentifier)
                    .map(user -> UserSummaryDTO.builder()
                            .userIdentifier(user.getUserIdentifier().getUserId().toString())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .primaryEmail(user.getPrimaryEmail())
                            .phone(user.getPhone())
                            .role(user.getUserRole().name())
                            .build())
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error fetching user with ID {}", userId, e);
            return null;
        }
    }

    private com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier parseUserIdentifier(String userId) {
        try {
            return UserIdentifier.fromString(userId);
        } catch (IllegalArgumentException e) {
            log.warn("Could not parse UUID from userId: {}", userId, e);
            return null;
        }
    }
}


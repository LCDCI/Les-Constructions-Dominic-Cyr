package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Component
public class ProjectMapper {

    public Project requestModelToEntity(ProjectRequestModel requestModel) {
        Project project = new Project();
        project.setProjectIdentifier(UUID.randomUUID().toString());
        project.setProjectName(requestModel.getProjectName());
        project.setProjectDescription(requestModel.getProjectDescription());
        project.setStatus(requestModel.getStatus());
        project.setStartDate(requestModel.getStartDate());
        project.setEndDate(requestModel.getEndDate());
        project.setCompletionDate(requestModel.getCompletionDate());
        project.setPrimaryColor(requestModel.getPrimaryColor());
        project.setTertiaryColor(requestModel.getTertiaryColor());
        project.setBuyerColor(requestModel.getBuyerColor());
        project.setBuyerName(requestModel.getBuyerName());
        // Set default empty string if imageIdentifier is null to satisfy NOT NULL constraint
        project.setImageIdentifier(requestModel.getImageIdentifier() != null ? requestModel.getImageIdentifier() : "");
        project.setCustomerId(requestModel.getCustomerId());
        project.setContractorIds(requestModel.getContractorIds() != null ? new ArrayList<>(requestModel.getContractorIds()) : new ArrayList<>());
        project.setSalespersonIds(requestModel.getSalespersonIds() != null ? new ArrayList<>(requestModel.getSalespersonIds()) : new ArrayList<>());
        if (requestModel.getLotIdentifiers() != null) {
            project.setLotIdentifiers(new ArrayList<>(requestModel.getLotIdentifiers()));
        } else {
            project.setLotIdentifiers(new ArrayList<>());
        }
        // Deprecated single lot_identifier column: DB may have NOT NULL; set empty string so insert succeeds
        project.setLotIdentifier("");
        project.setProgressPercentage(requestModel.getProgressPercentage());
        project.setLocation(requestModel.getLocation());
        return project;
    }

    public ProjectResponseModel entityToResponseModel(Project project) {
        return ProjectResponseModel.builder()
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
                .customerId(project.getCustomerId())
                .contractorIds(project.getContractorIds() != null ? new ArrayList<>(project.getContractorIds()) : new ArrayList<>())
                .salespersonIds(project.getSalespersonIds() != null ? new ArrayList<>(project.getSalespersonIds()) : new ArrayList<>())
                .lotIdentifiers(project.getLotIdentifiers() != null ? new ArrayList<>(project.getLotIdentifiers()) : new ArrayList<>())
                .progressPercentage(project.getProgressPercentage())
                .location(project.getLocation())
                .build();
    }

    public void updateEntityFromRequestModel(ProjectRequestModel requestModel, Project project) {
        if (requestModel.getProjectName() != null) {
            project.setProjectName(requestModel.getProjectName());
        }
        if (requestModel.getProjectDescription() != null) {
            project.setProjectDescription(requestModel.getProjectDescription());
        }
        if (requestModel.getStatus() != null) {
            project.setStatus(requestModel.getStatus());
        }
        if (requestModel.getStartDate() != null) {
            project.setStartDate(requestModel.getStartDate());
        }
        if (requestModel.getEndDate() != null) {
            project.setEndDate(requestModel.getEndDate());
        }
        if (requestModel.getCompletionDate() != null) {
            project.setCompletionDate(requestModel.getCompletionDate());
        }
        if (requestModel.getPrimaryColor() != null) {
            project.setPrimaryColor(requestModel.getPrimaryColor());
        }
        if (requestModel.getTertiaryColor() != null) {
            project.setTertiaryColor(requestModel.getTertiaryColor());
        }
        if (requestModel.getBuyerColor() != null) {
            project.setBuyerColor(requestModel.getBuyerColor());
        }
        if (requestModel.getBuyerName() != null) {
            project.setBuyerName(requestModel.getBuyerName());
        }
        if (requestModel.getImageIdentifier() != null) {
            project.setImageIdentifier(requestModel.getImageIdentifier());
        }
        if (requestModel.getCustomerId() != null) {
            project.setCustomerId(requestModel.getCustomerId());
        }
        if (requestModel.getContractorIds() != null) {
            project.setContractorIds(new ArrayList<>(requestModel.getContractorIds()));
        }
        if (requestModel.getSalespersonIds() != null) {
            project.setSalespersonIds(new ArrayList<>(requestModel.getSalespersonIds()));
        }
        if (requestModel.getLotIdentifiers() != null) {
            project.setLotIdentifiers(new ArrayList<>(requestModel.getLotIdentifiers()));
        }
        if (requestModel.getProgressPercentage() != null) {
            project.setProgressPercentage(requestModel.getProgressPercentage());
        }
        if (requestModel.getLocation() != null) {
            project.setLocation(requestModel.getLocation());
        }
    }
}
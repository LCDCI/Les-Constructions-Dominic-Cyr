package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequestModel {
    private String projectName;
    private String projectDescription;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate completionDate;
    private String primaryColor;
    private String tertiaryColor;
    private String buyerColor;
    private String buyerName;
    private String imageIdentifier;
    private String customerId;
    private String contractorId;
    private String salespersonId;
    private List<String> lotIdentifiers;
    private Integer progressPercentage;
    private String location;
}
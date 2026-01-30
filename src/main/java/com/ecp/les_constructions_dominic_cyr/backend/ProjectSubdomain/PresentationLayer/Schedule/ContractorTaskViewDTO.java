package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * DTO for contractor task view with project and lot grouping information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractorTaskViewDTO {
    private String taskId;
    private String taskStatus;
    private String taskTitle;
    private Date periodStart;
    private Date periodEnd;
    private String taskDescription;
    private String taskPriority;
    private Number estimatedHours;
    private Number hoursSpent;
    private Number taskProgress;
    private String assignedToUserId;
    private String assignedToUserName;
    private String scheduleId;

    // Project information
    private String projectIdentifier;
    private String projectName;

    // Lot information
    private String lotId;
    private String lotNumber;
}


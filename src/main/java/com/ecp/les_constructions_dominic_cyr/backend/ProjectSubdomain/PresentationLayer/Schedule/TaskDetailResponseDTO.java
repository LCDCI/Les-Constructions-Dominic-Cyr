package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule;

import java.util.Date;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskPriority;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDetailResponseDTO {
    private String taskId;
    private TaskStatus taskStatus;
    private String taskTitle;
    private Date periodStart;
    private Date periodEnd;
    private String taskDescription;
    private TaskPriority taskPriority;
    private Number estimatedHours;
    private Number hoursSpent;
    private Number taskProgress;
    private String assignedToUserId;
    private String assignedToUserName;
    private String scheduleId;
    private String projectIdentifier;
    private String projectName;
    private String lotId;
    private String lotNumber;
}

package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskPriority;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRequestDTO {
    private TaskStatus taskStatus;
    private String taskTitle;
    private Date periodStart;
    private Date periodEnd;
    private String taskDescription;
    private TaskPriority taskPriority;
    private Number estimatedHours;
    private Number hoursSpent;
    private Number taskProgress;
    private String assignedToUserId; // User UUID
}

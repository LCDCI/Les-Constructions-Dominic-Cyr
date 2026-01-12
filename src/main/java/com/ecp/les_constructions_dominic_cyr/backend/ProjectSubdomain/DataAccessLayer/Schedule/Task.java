package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private TaskIdentifier taskIdentifier;

    private TaskStatus taskStatus;

    private String taskTitle;

    private Date period_start;

    private Date period_end;

    private String taskDescription;

    private TaskPriority taskPriority;

    private Number estimatedHours;

    private Number hoursSpent;

    private Number taskProgress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private Users assignedTo;

    public Task(@NotNull TaskIdentifier taskIdentifier,@NotNull TaskStatus taskStatus,@NotNull String taskTitle,@NotNull Date period_start,@NotNull Date period_end,@NotNull String taskDescription,@NotNull TaskPriority taskPriority,@NotNull Number estimatedHours,@NotNull Number hoursSpent,@NotNull Number taskProgress,@NotNull Users assignedTo) {
        this.taskIdentifier = taskIdentifier;
        this.taskStatus = taskStatus;
        this.taskTitle = taskTitle;
        this.period_start = period_start;
        this.period_end = period_end;
        this.taskDescription = taskDescription;
        this.taskPriority = taskPriority;
        this.estimatedHours = estimatedHours;
        this.hoursSpent = hoursSpent;
        this.taskProgress = taskProgress;
        this.assignedTo = assignedTo;
    }
}

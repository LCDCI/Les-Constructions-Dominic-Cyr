package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @Embedded
    private TaskIdentifier taskIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_status")
    private TaskStatus taskStatus;

    private String taskTitle;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    private String taskDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_priority")
    private TaskPriority taskPriority;

    @Column(name = "estimated_hours")
    private Double estimatedHours;

    @Column(name = "hours_spent")
    private Double hoursSpent;

    @Column(name = "task_progress")
    private Double taskProgress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private Users assignedTo;

    @Column(name = "schedule_id", nullable = false)
    private String scheduleId;

    public Task(@NotNull TaskIdentifier taskIdentifier, @NotNull TaskStatus taskStatus, @NotNull String taskTitle, @NotNull LocalDate periodStart, @NotNull LocalDate periodEnd, @NotNull String taskDescription, @NotNull TaskPriority taskPriority, @NotNull Double estimatedHours, @NotNull Double hoursSpent, @NotNull Double taskProgress, @NotNull Users assignedTo) {
        this.taskIdentifier = taskIdentifier;
        this.taskStatus = taskStatus;
        this.taskTitle = taskTitle;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.taskDescription = taskDescription;
        this.taskPriority = taskPriority;
        this.estimatedHours = estimatedHours;
        this.hoursSpent = hoursSpent;
        this.taskProgress = taskProgress;
        this.assignedTo = assignedTo;
    }
}

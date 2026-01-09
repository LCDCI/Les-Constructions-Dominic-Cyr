package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Task is a value object that provides a summary view of tasks for contractors in a schedule period.
 * This is NOT a database entity but a computed/aggregated object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    private String contractorId;
    private String scheduleId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer totalTasks;
    private Integer openTasksCount;
    private Integer completedTasksCount;
    private Integer overdueTasksCount;
    private Double totalEstimatedHours;
    private Double totalRemainingHours;
    private TaskPreview nextDueTaskPreview;
    private List<TopPriorityTask> topPriorityTasks;
    private Integer blockedTasksCount;
    private Boolean milestonesPresent;
    private List<String> taskIds;
    private LocalDateTime generatedAt;

    /**
     * Preview of the next due task
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskPreview {
        private String id;
        private String title;
        private LocalDate dueDate;
        private Double estimateHours;
        private String status;
        private String assignedTo;
    }

    /**
     * Represents a high-priority task
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPriorityTask {
        private String id;
        private String title;
        private String priority;
        private LocalDate dueDate;
    }
}

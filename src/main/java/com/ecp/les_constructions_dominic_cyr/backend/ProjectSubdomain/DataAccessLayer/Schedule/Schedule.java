package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "schedule_identifier", unique = true, nullable = false)
    private String scheduleIdentifier;

    @Column(name = "task_date", nullable = false)
    private LocalDate taskDate;

    @Column(name = "task_description", nullable = false, length = 500)
    private String taskDescription;

    @Column(name = "lot_number", nullable = false, length = 50)
    private String lotNumber;

    @Column(name = "day_of_week", nullable = false, length = 20)
    private String dayOfWeek;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private List<Task> tasks;

    public Schedule(@NotNull String scheduleIdentifier, @NotNull LocalDate taskDate, @NotNull String taskDescription, @NotNull String lotNumber, @NotNull String dayOfWeek) {
        this.scheduleIdentifier = scheduleIdentifier;
        this.taskDate = taskDate;
        this.taskDescription = taskDescription;
        this.lotNumber = lotNumber;
        this.dayOfWeek = dayOfWeek;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.tasks = new ArrayList<>();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
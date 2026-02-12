package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
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
import java.util.UUID;

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

    @Column(name = "schedule_start_date", nullable = false)
    private LocalDate scheduleStartDate;

    @Column(name = "schedule_end_date", nullable = false)
    private LocalDate scheduleEndDate;

    @Column(name = "schedule_description", nullable = false, length = 500)
    private String scheduleDescription;

    @Column(name = "lot_id", nullable = false)
    private UUID lotId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", referencedColumnName = "schedule_identifier")
    private List<Task> tasks;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "project_id",                          // The FK column in 'schedules' table
            referencedColumnName = "project_identifier",   // The column in 'projects' table
            nullable = false
    )
    private Project project;

    public Schedule(@NotNull String scheduleIdentifier, @NotNull LocalDate scheduleStartDate, @NotNull LocalDate scheduleEndDate, @NotNull String scheduleDescription, @NotNull UUID lotId) {
        this.scheduleIdentifier = scheduleIdentifier;
        this.scheduleStartDate = scheduleStartDate;
        this.scheduleEndDate = scheduleEndDate;
        this.scheduleDescription = scheduleDescription;
        this.lotId = lotId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.tasks = new ArrayList<>();
    }

    // Backward compatibility: keep legacy accessor names until callers migrate to lotId
    public String getLotNumber() {
        return lotId != null ? lotId.toString() : null;
    }

    public void setLotNumber(String lotNumber) {
        this.lotId = lotNumber != null ? UUID.fromString(lotNumber) : null;
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
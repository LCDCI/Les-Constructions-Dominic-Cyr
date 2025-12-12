package com.ecp. les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Data
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectId;

    @Column(nullable = false, unique = true)
    private String projectIdentifier;

    @Column(nullable = false)
    private String projectName;

    @Column(length = 1000)
    private String projectDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate completionDate;

    @Column(nullable = false)
    private String primaryColor;

    @Column(nullable = false)
    private String tertiaryColor;

    @Column(nullable = false)
    private String buyerColor;

    @Column(nullable = false)
    private String buyerName;

    @Column(nullable = false)
    private String imageIdentifier;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String lotIdentifier;

    private Integer progressPercentage;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime. now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
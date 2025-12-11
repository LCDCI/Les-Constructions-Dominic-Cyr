package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_features")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_identifier", nullable = false)
    private String projectIdentifier;

    @Column(name = "feature_title", nullable = false, length = 200)
    private String featureTitle;

    @Column(name = "feature_description", columnDefinition = "TEXT")
    private String featureDescription;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
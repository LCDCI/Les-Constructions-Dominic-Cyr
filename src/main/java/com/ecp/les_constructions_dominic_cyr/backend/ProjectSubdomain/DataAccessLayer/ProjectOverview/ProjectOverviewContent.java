package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_overview_content")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectOverviewContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_identifier", nullable = false, unique = true)
    private String projectIdentifier;

    @Column(name = "hero_title", nullable = false, length = 500)
    private String heroTitle;

    @Column(name = "hero_subtitle", length = 1000)
    private String heroSubtitle;

    @Column(name = "hero_description", columnDefinition = "TEXT")
    private String heroDescription;

    @Column(name = "overview_section_title", length = 300)
    private String overviewSectionTitle;

    @Column(name = "overview_section_content", columnDefinition = "TEXT")
    private String overviewSectionContent;

    @Column(name = "features_section_title", length = 300)
    private String featuresSectionTitle;

    @Column(name = "location_section_title", length = 300)
    private String locationSectionTitle;

    @Column(name = "location_description", columnDefinition = "TEXT")
    private String locationDescription;

    @Column(name = "location_address", length = 500)
    private String locationAddress;

    @Column(name = "location_map_embed_url", columnDefinition = "TEXT")
    private String locationMapEmbedUrl;

    @Column(name = "gallery_section_title", length = 300)
    private String gallerySectionTitle;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "location_latitude")
    private Double locationLatitude;

    @Column(name = "location_longitude")
    private Double locationLongitude;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
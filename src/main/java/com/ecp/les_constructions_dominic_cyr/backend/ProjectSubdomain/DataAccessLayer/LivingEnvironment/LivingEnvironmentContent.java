package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LivingEnvironment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "living_environment_content")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LivingEnvironmentContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_identifier", nullable = false)
    private String projectIdentifier;

    @Column(name = "language", nullable = false, length = 10)
    private String language;

    @Column(name = "header_title", nullable = false)
    private String headerTitle;

    @Column(name = "header_subtitle")
    private String headerSubtitle;

    @Column(name = "header_subtitle_last")
    private String headerSubtitleLast;

    @Column(name = "header_tagline", length = 500)
    private String headerTagline;

    @Column(name = "description_text", columnDefinition = "TEXT")
    private String descriptionText;

    @Column(name = "proximity_title")
    private String proximityTitle;

    @Column(name = "footer_text", length = 500)
    private String footerText;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "livingEnvironmentContent", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<LivingEnvironmentAmenity> amenities = new ArrayList<>();

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

package com.ecp.les_constructions_dominic_cyr.backend.SiteContentSubdomain.DataAccessLayer;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "site_content", uniqueConstraints = @UniqueConstraint(
        name = "ux_site_content_page_language",
        columnNames = {"page_group", "page_key", "language"}
))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_group", nullable = false, length = 100)
    private String pageGroup;

    @Column(name = "page_key", nullable = false, length = 150)
    private String pageKey;

    @Column(name = "language", nullable = false, length = 10)
    private String language;

    @Column(name = "content_text", columnDefinition = "TEXT")
    private String contentText;

    @Column(name = "image_identifier", length = 500)
    private String imageIdentifier;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

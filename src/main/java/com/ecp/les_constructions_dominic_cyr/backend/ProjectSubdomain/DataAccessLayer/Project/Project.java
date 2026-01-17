package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(nullable = true)
    private String buyerName;

    @Column(nullable = false)
    private String imageIdentifier;

    @Column(nullable = true)
    private String customerId;

    // Legacy column - kept temporarily for database migration compatibility
    // This field is not used by the application but prevents constraint errors
    // Hibernate will attempt to make this nullable when ddl-auto: update runs
    @Column(name = "lot_identifier", nullable = true, insertable = true, updatable = false)
    @Deprecated
    private String lotIdentifier;

    @ElementCollection
    @CollectionTable(name = "project_lots", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "lot_identifier", nullable = false)
    private List<String> lotIdentifiers = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lot> lots = new ArrayList<>();

    private Integer progressPercentage;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ElementCollection
    @CollectionTable(name = "project_contractors", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "contractor_id", nullable = false)
    private List<String> contractorIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "project_salespersons", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "salesperson_id", nullable = false)
    private List<String> salespersonIds = new ArrayList<>();

    @Column(nullable = true)
    private String location;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Set legacy lotIdentifier to empty string to satisfy NOT NULL constraint during migration
        // This will be removed once the column is dropped
        if (lotIdentifier == null) {
            lotIdentifier = "";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
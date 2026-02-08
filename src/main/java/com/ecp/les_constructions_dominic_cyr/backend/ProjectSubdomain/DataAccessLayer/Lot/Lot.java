package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot;


import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "lots")
@Data
@NoArgsConstructor
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private LotIdentifier lotIdentifier;

    private String lotNumber;

    private String civicAddress;

    private Float price;

    private String dimensionsSquareFeet;

    private String dimensionsSquareMeters;

    @Enumerated(EnumType.STRING)
    private LotStatus lotStatus;

    // ManyToOne relationship with Project entity (like Schedule does)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "project_id",                        
            referencedColumnName = "project_identifier",
            nullable = false
    )
    private Project project;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "lot_assigned_users",
            joinColumns = @JoinColumn(name = "lot_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    )
    @lombok.Getter(lombok.AccessLevel.NONE)
    private List<Users> assignedUsers = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, columnDefinition = "int default 59")
    private Integer remainingUpcomingWork = 59;

    public Lot(@NonNull LotIdentifier lotIdentifier, @NonNull String lotNumber, @NonNull String civicAddress, Float price, @NonNull String dimensionsSquareFeet, @NonNull String dimensionsSquareMeters, @NonNull LotStatus lotStatus) {
        this.lotIdentifier = lotIdentifier;
        this.lotNumber = lotNumber;
        this.civicAddress = civicAddress;
        this.price = price;
        this.dimensionsSquareFeet = dimensionsSquareFeet;
        this.dimensionsSquareMeters = dimensionsSquareMeters;
        this.lotStatus = lotStatus;
        this.assignedUsers = new ArrayList<>();
    }

    public List<Users> getAssignedUsers() {
        return Collections.unmodifiableList(this.assignedUsers);
    }
}

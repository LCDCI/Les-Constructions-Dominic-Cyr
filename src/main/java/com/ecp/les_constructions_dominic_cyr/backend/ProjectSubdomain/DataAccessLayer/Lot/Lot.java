package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot;


import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

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

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "project_id",                          // The FK column in 'lots' table
            referencedColumnName = "project_identifier",   // The column in 'projects' table
            nullable = false
    )
    private Project project;

    public Lot(@NonNull LotIdentifier lotIdentifier, @NonNull String lotNumber, @NonNull String civicAddress, @NonNull Float price, @NonNull String dimensionsSquareFeet, @NonNull String dimensionsSquareMeters, @NonNull LotStatus lotStatus) {
        this.lotIdentifier = lotIdentifier;
        this.lotNumber = lotNumber;
        this.civicAddress = civicAddress;
        this.price = price;
        this.dimensionsSquareFeet = dimensionsSquareFeet;
        this.dimensionsSquareMeters = dimensionsSquareMeters;
        this.lotStatus = lotStatus;
    }
}

package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.DataAccessLayer;


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

    private String location;

    private Float price;

    private String dimensions;

    @Enumerated(EnumType.STRING)
    private LotStatus lotStatus;

    public Lot(@NonNull LotIdentifier lotIdentifier, @NonNull String location, @NonNull Float price, @NonNull String dimensions, @NonNull LotStatus lotStatus) {
        this.lotIdentifier = lotIdentifier;
        this.location = location;
        this.price = price;
        this.dimensions = dimensions;
        this.lotStatus = lotStatus;
    }
}

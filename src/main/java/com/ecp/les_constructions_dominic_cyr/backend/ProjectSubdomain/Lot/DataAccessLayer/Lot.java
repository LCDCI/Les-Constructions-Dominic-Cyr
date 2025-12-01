package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.DataAccessLayer;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lots")
@Data
@NoArgsConstructor
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lot_identifier")
    private LotIdentifier lotIdentifier;

    @Column(name = "location")
    private String location;

    @Column(name = "price")
    private Float price;

    @Column(name = "dimensions")
    private String dimensions;

    @Enumerated(EnumType.STRING)
    @Column(name = "lot_status")
    private com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.DataAccessLayer.LotStatus LotStatus;
}

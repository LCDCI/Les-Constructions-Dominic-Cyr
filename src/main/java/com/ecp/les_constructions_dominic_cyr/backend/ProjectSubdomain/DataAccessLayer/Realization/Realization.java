package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "realizations")
@Data
@NoArgsConstructor
public class Realization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private RealizationIdentifier realizationIdentifier;

    private String realizationName;

    private String location;

    private String description;

    private String imageIdentifier;

    private Integer numberOfRooms;

    private Integer numberOfBedrooms;

    private Integer numberOfBathrooms;

    private Integer constructionYear;
}

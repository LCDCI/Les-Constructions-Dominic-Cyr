package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "houses")
@Data
@NoArgsConstructor
public class House {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private HouseIdentifier houseIdentifier;

    private String houseName;

    private String location;

    private String description;

    private Integer numberOfRooms;

    private Integer numberOfBedrooms;

    private Integer numberOfBathrooms;

    private Integer constructionYear;
}

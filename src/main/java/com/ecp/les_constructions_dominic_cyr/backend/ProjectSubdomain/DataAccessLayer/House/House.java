package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "houses", uniqueConstraints = @UniqueConstraint(
        name = "ux_houses_project_identifier",
        columnNames = {"project_identifier", "house_identifier"}
))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class House {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "house_identifier", nullable = false, unique = true)
    private UUID houseIdentifier;

    @Column(name = "project_identifier", nullable = false, length = 255)
    private String projectIdentifier;

    @Column(name = "house_name", nullable = false, length = 255)
    private String houseName;

    @Column(length = 255)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_fr", columnDefinition = "TEXT")
    private String descriptionFr;

    @Column(name = "image_identifier", length = 500)
    private String imageIdentifier;

    @Column(name = "number_of_rooms", precision = 5, scale = 2)
    private BigDecimal numberOfRooms;

    @Column(name = "number_of_bedrooms", precision = 5, scale = 2)
    private BigDecimal numberOfBedrooms;

    @Column(name = "number_of_bathrooms", precision = 5, scale = 2)
    private BigDecimal numberOfBathrooms;

    @Column(name = "construction_year")
    private Integer constructionYear;
}

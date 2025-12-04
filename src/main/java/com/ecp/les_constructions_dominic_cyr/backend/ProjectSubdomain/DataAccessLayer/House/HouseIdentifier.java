package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class HouseIdentifier {

    @Column(name = "house_identifier", unique = true)
    private String houseId;

    public HouseIdentifier() {
        this.houseId = java.util.UUID.randomUUID().toString();
    }
    public HouseIdentifier(String houseId) {
        this.houseId = houseId;
    }
}

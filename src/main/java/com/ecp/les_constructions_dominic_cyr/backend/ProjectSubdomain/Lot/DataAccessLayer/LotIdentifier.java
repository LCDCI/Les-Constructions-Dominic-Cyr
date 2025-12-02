package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.DataAccessLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.UUID;

@Embeddable
@Getter
public class LotIdentifier {

    @Column(name = "lot_identifier", unique = true)
    private String lotId;

    public LotIdentifier() {
        this.lotId = UUID.randomUUID().toString();
    }

    public LotIdentifier(String lotId) {
        this.lotId = lotId;
    }
}

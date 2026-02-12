package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import lombok.Getter;

import java.util.UUID;

@Embeddable
@Access(AccessType.FIELD)
@Getter
public class LotIdentifier {

    @Column(name = "lot_identifier", unique = true, columnDefinition = "uuid")
    private UUID lotId;

    public LotIdentifier() {
        this.lotId = UUID.randomUUID();
    }

    public LotIdentifier(String lotId) {
        this.lotId = UUID.fromString(lotId);
    }

    @Override
    public String toString() {
        return "LotIdentifier{" +
                "lotId='" + (lotId == null ? "" : lotId.toString()) + '\'' +
                '}';
    }
}

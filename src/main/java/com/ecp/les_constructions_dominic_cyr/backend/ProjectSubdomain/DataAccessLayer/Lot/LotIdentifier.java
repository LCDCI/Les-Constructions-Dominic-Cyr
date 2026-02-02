package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Embeddable
@Getter
public class LotIdentifier {

    @Column(name = "lot_identifier", unique = true)
    @JdbcTypeCode(SqlTypes.UUID)
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

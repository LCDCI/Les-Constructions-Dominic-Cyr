package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class RealizationIdentifier {

    @Column(name = "realization_identifier", unique = true)
    private String realizationId;

    public RealizationIdentifier() {
        this.realizationId = java.util.UUID.randomUUID().toString();
    }
    public RealizationIdentifier(String realizationId) {
        this.realizationId = realizationId;
    }

    @Override
    public String toString() {
        return "RealizationIdentifier{" +
                "realizationId='" + (realizationId == null ? "" : realizationId) + '\'' +
                '}';
    }
}

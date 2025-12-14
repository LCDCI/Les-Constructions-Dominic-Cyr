package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class RenovationIdentifier {

    @Column(name = "renovation_identifier", unique = true)
    private String renovationId;

    public RenovationIdentifier() {
        this.renovationId = java.util.UUID.randomUUID().toString();
    }
    public RenovationIdentifier(String renovationId) {
        this.renovationId = renovationId;
    }

    @Override
    public String toString() {
        return "RenovationIdentifier{" +
                "renovationId='" + (renovationId == null ? "" : renovationId) + '\'' +
                '}';
    }
}

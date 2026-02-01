package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LotDocument;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.UUID;

@Embeddable
@Getter
public class LotDocumentIdentifier {

    @Column(name = "id")
    private UUID documentId;

    public LotDocumentIdentifier() {
        this.documentId = UUID.randomUUID();
    }

    public LotDocumentIdentifier(UUID documentId) {
        this.documentId = documentId;
    }

    @Override
    public String toString() {
        return "LotDocumentIdentifier{" +
                "documentId='" + (documentId == null ? "" : documentId.toString()) + '\'' +
                '}';
    }
}

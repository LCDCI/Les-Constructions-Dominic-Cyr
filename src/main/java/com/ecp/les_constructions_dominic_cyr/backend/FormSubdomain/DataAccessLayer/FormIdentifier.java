package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.UUID;

/**
 * Embeddable identifier for Form entities.
 */
@Embeddable
@Getter
public class FormIdentifier {

    @Column(name = "form_identifier", unique = true, nullable = false)
    private String formId;

    public FormIdentifier() {
        this.formId = UUID.randomUUID().toString();
    }

    public FormIdentifier(String formId) {
        this.formId = formId;
    }

    @Override
    public String toString() {
        return "FormIdentifier{" +
                "formId='" + (formId == null ? "" : formId) + '\'' +
                '}';
    }
}

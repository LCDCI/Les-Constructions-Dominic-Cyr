package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Request model for updating form data (used by customers when filling out forms).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormDataUpdateRequestModel {

    @NotNull(message = "Form data is required")
    @Builder.Default
    private Map<String, Object> formData = new HashMap<>();

    /**
     * Optional notes from the customer
     */
    private String submissionNotes;

    /**
     * Set to true when customer is submitting the form (not just saving progress)
     */
    @JsonProperty("isSubmitting")
    @Builder.Default
    private boolean isSubmitting = false;
}

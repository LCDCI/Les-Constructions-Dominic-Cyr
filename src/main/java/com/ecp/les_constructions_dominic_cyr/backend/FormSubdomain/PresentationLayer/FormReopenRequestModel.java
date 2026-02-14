package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request model for reopening a form.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormReopenRequestModel {

    @NotBlank(message = "Reopen reason is required")
    private String reopenReason;

    /**
     * Optional new instructions for the customer
     */
    private String newInstructions;
}

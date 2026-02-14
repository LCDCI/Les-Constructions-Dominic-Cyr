package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Request model for creating a new form and assigning it to a customer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormRequestModel {

    @NotNull(message = "Form type is required")
    private FormType formType;

    @NotBlank(message = "Project identifier is required")
    private String projectIdentifier;

    @NotBlank(message = "Lot identifier is required")
    private String lotIdentifier;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    /**
     * Optional custom title for the form
     */
    private String formTitle;

    /**
     * Optional instructions or notes for the customer
     */
    private String instructions;

    /**
     * Initial form data (optional, can be empty when first created)
     */
    @Builder.Default
    private Map<String, Object> formData = new HashMap<>();
}

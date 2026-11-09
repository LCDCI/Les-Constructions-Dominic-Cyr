package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class FormRevisionRequestModel {
    @NotNull
    private Map<String, Object> formData;

    @NotBlank
    private String reason;
}

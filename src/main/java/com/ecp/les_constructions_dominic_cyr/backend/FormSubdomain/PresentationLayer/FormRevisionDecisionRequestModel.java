package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FormRevisionDecisionRequestModel {
    @NotBlank
    private String reason;
}

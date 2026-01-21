package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InquiryResponseRequestModel {
    @NotBlank(message = "Response is required")
    @Size(max = 2000, message = "Response must not exceed 2000 characters")
    private String response;

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
}

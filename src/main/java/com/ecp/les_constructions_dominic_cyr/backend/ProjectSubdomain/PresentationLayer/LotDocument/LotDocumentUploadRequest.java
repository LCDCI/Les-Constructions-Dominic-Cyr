package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LotDocument;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LotDocumentUploadRequest {
    @NotBlank(message = "Uploader user ID is required")
    private String uploaderUserId;
    
    @NotNull(message = "Uploader name is required")
    private String uploaderName;
}

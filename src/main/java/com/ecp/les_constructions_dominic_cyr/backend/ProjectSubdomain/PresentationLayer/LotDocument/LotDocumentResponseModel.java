package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LotDocument;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class LotDocumentResponseModel {
    private UUID id;
    private String lotId;
    private UUID uploaderUserId;
    private String uploaderName;
    private String fileName;
    private String mimeType;
    private Long sizeBytes;
    private Boolean isImage;
    private LocalDateTime uploadedAt;
    
    // Optional: include download URL
    private String downloadUrl;
}

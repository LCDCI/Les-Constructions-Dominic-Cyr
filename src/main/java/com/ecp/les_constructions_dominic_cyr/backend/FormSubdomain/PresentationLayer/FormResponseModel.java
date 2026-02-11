package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormStatus;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Response model for Form entities.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormResponseModel {

    private String formId;
    private FormType formType;
    private FormStatus formStatus;
    private String projectIdentifier;
    
    // Customer information
    private String customerId;
    private String customerName;
    private String customerEmail;
    
    // Assigned by information
    private String assignedByUserId;
    private String assignedByName;
    
    // Form details
    private String formTitle;
    private String instructions;
    
    @Builder.Default
    private Map<String, Object> formData = new HashMap<>();
    
    // Dates
    private LocalDateTime assignedDate;
    private LocalDateTime firstSubmittedDate;
    private LocalDateTime lastSubmittedDate;
    private LocalDateTime completedDate;
    private LocalDateTime reopenedDate;
    
    // Reopen information
    private String reopenedByUserId;
    private String reopenReason;
    
    @Builder.Default
    private Integer reopenCount = 0;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Response model for FormSubmissionHistory entities.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormSubmissionHistoryResponseModel {

    private Long id;
    private String formIdentifier;
    private Integer submissionNumber;
    private FormStatus statusAtSubmission;
    
    @Builder.Default
    private Map<String, Object> formDataSnapshot = new HashMap<>();
    
    private String submittedByCustomerId;
    private String submittedByCustomerName;
    private String submissionNotes;
    private LocalDateTime submittedAt;
}

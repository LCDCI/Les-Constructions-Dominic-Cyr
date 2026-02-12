package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.MapperLayer;

import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormSubmissionHistory;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer.FormSubmissionHistoryResponseModel;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between FormSubmissionHistory entities and presentation models.
 */
@Component
public class FormSubmissionHistoryMapper {

    /**
     * Convert FormSubmissionHistory entity to response model
     */
    public FormSubmissionHistoryResponseModel entityToResponseModel(FormSubmissionHistory history) {
        return FormSubmissionHistoryResponseModel.builder()
                .id(history.getId())
                .formIdentifier(history.getFormIdentifier())
                .submissionNumber(history.getSubmissionNumber())
                .statusAtSubmission(history.getStatusAtSubmission())
                .formDataSnapshot(history.getFormDataSnapshot())
                .submittedByCustomerId(history.getSubmittedByCustomerId())
                .submittedByCustomerName(history.getSubmittedByCustomerName())
                .submissionNotes(history.getSubmissionNotes())
                .submittedAt(history.getSubmittedAt())
                .build();
    }
}

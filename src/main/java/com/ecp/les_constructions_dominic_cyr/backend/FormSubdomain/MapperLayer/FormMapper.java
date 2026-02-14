package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.MapperLayer;

import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.Form;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormStatus;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer.FormRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer.FormResponseModel;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper for converting between Form entities and presentation models.
 */
@Component
public class FormMapper {

    /**
     * Convert FormRequestModel to Form entity
     */
    public Form requestModelToEntity(FormRequestModel requestModel) {
        Form form = new Form();
        form.setFormIdentifier(new FormIdentifier());
        form.setFormType(requestModel.getFormType());
        form.setFormStatus(FormStatus.DRAFT); // New forms start as DRAFT
        form.setProjectIdentifier(requestModel.getProjectIdentifier());
        form.setLotIdentifier(UUID.fromString(requestModel.getLotIdentifier()));
        form.setCustomerId(UUID.fromString(requestModel.getCustomerId()));
        form.setFormTitle(requestModel.getFormTitle());
        form.setInstructions(requestModel.getInstructions());
        
        if (requestModel.getFormData() != null) {
            form.setFormData(requestModel.getFormData());
        }
        
        return form;
    }

    /**
     * Convert Form entity to FormResponseModel
     */
    public FormResponseModel entityToResponseModel(Form form) {
        return FormResponseModel.builder()
                .formId(form.getFormIdentifier().getFormId())
                .formType(form.getFormType())
                .formStatus(form.getFormStatus())
                .projectIdentifier(form.getProjectIdentifier())
                .lotIdentifier(form.getLotIdentifier().toString())
                .customerId(form.getCustomerId().toString())
                .customerName(form.getCustomerName())
                .customerEmail(form.getCustomerEmail())
                .assignedByUserId(form.getAssignedByUserId() != null ? form.getAssignedByUserId().toString() : null)
                .assignedByName(form.getAssignedByName())
                .formTitle(form.getFormTitle())
                .instructions(form.getInstructions())
                .formData(form.getFormData())
                .assignedDate(form.getAssignedDate())
                .firstSubmittedDate(form.getFirstSubmittedDate())
                .lastSubmittedDate(form.getLastSubmittedDate())
                .completedDate(form.getCompletedDate())
                .reopenedDate(form.getReopenedDate())
                .reopenedByUserId(form.getReopenedByUserId() != null ? form.getReopenedByUserId().toString() : null)
                .reopenReason(form.getReopenReason())
                .reopenCount(form.getReopenCount())
                .createdAt(form.getCreatedAt())
                .updatedAt(form.getUpdatedAt())
                .build();
    }

    /**
     * Update existing Form entity from FormRequestModel
     * (Used for updating form details, not for customer data updates)
     */
    public void updateEntityFromRequestModel(FormRequestModel requestModel, Form form) {
        if (requestModel.getFormTitle() != null) {
            form.setFormTitle(requestModel.getFormTitle());
        }
        if (requestModel.getInstructions() != null) {
            form.setInstructions(requestModel.getInstructions());
        }
        // Note: formType, projectId, customerId should not be changed after creation
    }
}

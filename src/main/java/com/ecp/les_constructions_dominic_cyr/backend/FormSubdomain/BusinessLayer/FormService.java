package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormStatus;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormType;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer.*;

import java.util.List;

/**
 * Service interface for managing customer forms.
 */
public interface FormService {

    /**
     * Create and assign a new form to a customer
     * Only salespersons or owners can create forms
     * 
     * @param requestModel Form details
     * @param assignedByUserId User ID of the salesperson/owner creating the form
     * @return Created form
     */
    FormResponseModel createAndAssignForm(FormRequestModel requestModel, String assignedByUserId);

    /**
     * Get a form by its identifier
     * 
     * @param formId Form identifier
     * @return Form details
     */
    FormResponseModel getFormById(String formId);

    /**
     * Get all forms (for privileged roles: owner, salesperson, contractor)
     * 
     * @return List of all forms
     */
    List<FormResponseModel> getAllForms();

    /**
     * Get all forms for a specific project
     * 
     * @param projectIdentifier Project identifier
     * @return List of forms
     */
    List<FormResponseModel> getFormsByProject(String projectIdentifier);

    /**
     * Get all forms assigned to a specific customer
     * 
     * @param customerId Customer user ID
     * @return List of forms
     */
    List<FormResponseModel> getFormsByCustomer(String customerId);

    /**
     * Get all forms created by a specific salesperson
     * 
     * @param salespersonId Salesperson user ID
     * @return List of forms
     */
    List<FormResponseModel> getFormsCreatedBy(String salespersonId);

    /**
     * Get all forms with a specific status
     * 
     * @param status Form status
     * @return List of forms
     */
    List<FormResponseModel> getFormsByStatus(FormStatus status);

    /**
     * Update form data (used by customers filling out the form)
     * 
     * @param formId Form identifier
     * @param updateRequest Updated form data
     * @param customerId Customer user ID making the update
     * @return Updated form
     */
    FormResponseModel updateFormData(String formId, FormDataUpdateRequestModel updateRequest, String customerId);

    /**
     * Mark form as submitted by customer
     * Creates a snapshot in submission history
     * 
     * @param formId Form identifier
     * @param updateRequest Form data with submission notes
     * @param customerId Customer user ID
     * @return Updated form
     */
    FormResponseModel submitForm(String formId, FormDataUpdateRequestModel updateRequest, String customerId);

    /**
     * Reopen a submitted form for customer to re-edit
     * Only salespersons or owners can reopen forms
     * Sends notification to customer
     * 
     * @param formId Form identifier
     * @param reopenRequest Reason for reopening and optional new instructions
     * @param reopenedByUserId User ID of person reopening the form
     * @return Updated form
     */
    FormResponseModel reopenForm(String formId, FormReopenRequestModel reopenRequest, String reopenedByUserId);

    /**
     * Mark form as completed (final approval)
     * Only salespersons or owners can complete forms
     * 
     * @param formId Form identifier
     * @param completedByUserId User ID of person completing the form
     * @return Updated form
     */
    FormResponseModel completeForm(String formId, String completedByUserId);

    /**
     * Update form details (title, instructions) - only by salesperson/owner
     * 
     * @param formId Form identifier
     * @param requestModel Updated form details
     * @param updatedByUserId User ID making the update
     * @return Updated form
     */
    FormResponseModel updateFormDetails(String formId, FormRequestModel requestModel, String updatedByUserId);

    /**
     * Delete a form
     * Only salespersons or owners can delete forms
     * 
     * @param formId Form identifier
     * @param deletedByUserId User ID of person deleting the form
     */
    void deleteForm(String formId, String deletedByUserId);

    /**
     * Get submission history for a form
     * 
     * @param formId Form identifier
     * @return List of submission history entries
     */
    List<FormSubmissionHistoryResponseModel> getFormSubmissionHistory(String formId);

    /**
     * Check if a customer already has a form of a specific type for a project
     * 
     * @param projectIdentifier Project identifier
     * @param customerId Customer user ID
     * @param formType Type of form
     * @return true if form exists, false otherwise
     */
    boolean hasFormOfType(String projectIdentifier, String customerId, FormType formType);
}

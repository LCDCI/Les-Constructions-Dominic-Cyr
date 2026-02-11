package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.MailerServiceClient;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.NotificationService;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.NotificationCategory;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.*;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.MapperLayer.FormMapper;
import com. ecp.les_constructions_dominic_cyr.backend.FormSubdomain.MapperLayer.FormSubmissionHistoryMapper;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer.*;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of FormService for managing customer forms.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FormServiceImpl implements FormService {

    private final FormRepository formRepository;
    private final FormSubmissionHistoryRepository historyRepository;
    private final FormMapper formMapper;
    private final FormSubmissionHistoryMapper historyMapper;
    private final UsersRepository usersRepository;
    private final LotRepository lotRepository;
    private final ProjectRepository projectRepository;
    private final NotificationService notificationService;
    private final MailerServiceClient mailerServiceClient;

    @Override
    @Transactional
    public FormResponseModel createAndAssignForm(FormRequestModel requestModel, String assignedByUserId) {
        log.info("Creating and assigning form of type {} to customer {} for project {} lot {}",
                requestModel.getFormType(), requestModel.getCustomerId(), 
                requestModel.getProjectIdentifier(), requestModel.getLotIdentifier());

        // Validate that customer exists
        Users customer = usersRepository.findByUserIdentifier(requestModel.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Customer not found with ID: " + requestModel.getCustomerId()));

        // Validate that assigner exists
        Users assignedBy = usersRepository.findByAuth0UserId(assignedByUserId)
                .orElseThrow(() -> new NotFoundException("User not found with Auth0 ID: " + assignedByUserId));

        // Validate that project exists
        Project project = projectRepository.findByProjectIdentifier(requestModel.getProjectIdentifier())
                .orElseThrow(() -> new NotFoundException("Project not found with ID: " + requestModel.getProjectIdentifier()));

        // Validate that lot exists and belongs to the project (with assigned users eagerly loaded)
        Lot lot = lotRepository.findByLotIdentifier_LotIdWithUsers(
                java.util.UUID.fromString(requestModel.getLotIdentifier()));
        
        if (lot == null) {
            throw new NotFoundException("Lot not found with ID: " + requestModel.getLotIdentifier());
        }

        if (!lot.getProject().getProjectIdentifier().equals(requestModel.getProjectIdentifier())) {
            throw new InvalidInputException("Lot " + requestModel.getLotIdentifier() + 
                    " does not belong to project " + requestModel.getProjectIdentifier());
        }

        // Validate that both salesperson and customer are assigned to the lot
        List<Users> assignedUsers = lot.getAssignedUsers();
        if (assignedUsers == null || assignedUsers.isEmpty()) {
            throw new InvalidInputException("No users are assigned to lot " + requestModel.getLotIdentifier());
        }
        
        boolean isSalespersonAssigned = assignedUsers.stream()
                .anyMatch(user -> user.getUserIdentifier() != null 
                        && user.getUserIdentifier().getUserId() != null
                        && user.getUserIdentifier().getUserId().equals(assignedBy.getUserIdentifier().getUserId()));
        
        boolean isCustomerAssigned = assignedUsers.stream()
                .anyMatch(user -> user.getUserIdentifier() != null 
                        && user.getUserIdentifier().getUserId() != null
                        && user.getUserIdentifier().getUserId().equals(customer.getUserIdentifier().getUserId()));
        
        if (!isSalespersonAssigned) {
            throw new InvalidInputException("Salesperson is not assigned to lot " + requestModel.getLotIdentifier());
        }
        
        if (!isCustomerAssigned) {
            throw new InvalidInputException("Customer is not assigned to lot " + requestModel.getLotIdentifier());
        }

        // Check if customer already has this form type for this project and lot
        if (formRepository.existsByProjectIdentifierAndLotIdentifierAndCustomerIdAndFormType(
                requestModel.getProjectIdentifier(), requestModel.getLotIdentifier(),
                requestModel.getCustomerId(), requestModel.getFormType())) {
            throw new InvalidInputException("Customer already has a " + requestModel.getFormType() +
                    " form for this project and lot");
        }

        // Create form entity
        Form form = formMapper.requestModelToEntity(requestModel);
        form.setAssignedByUserId(assignedBy.getUserIdentifier().getUserId().toString());
        form.setAssignedByName(getFullName(assignedBy));
        form.setCustomerName(getFullName(customer));
        form.setCustomerEmail(customer.getPrimaryEmail());
        form.setFormStatus(FormStatus.ASSIGNED);
        form.setAssignedDate(LocalDateTime.now());

        // Save form
        Form savedForm = formRepository.save(form);
        log.info("Form created with ID: {}", savedForm.getFormIdentifier().getFormId());

        // Send notification to customer
        sendFormAssignedNotification(savedForm, customer);

        return formMapper.entityToResponseModel(savedForm);
    }

    @Override
    public FormResponseModel getFormById(String formId) {
        log.info("Fetching form with ID: {}", formId);
        Form form = formRepository.findByFormIdentifier_FormId(formId)
                .orElseThrow(() -> new NotFoundException("Form not found with ID: " + formId));
        return formMapper.entityToResponseModel(form);
    }

    @Override
    public List<FormResponseModel> getFormsByProject(String projectIdentifier) {
        log.info("Fetching forms for project: {}", projectIdentifier);
        return formRepository.findByProjectIdentifier(projectIdentifier).stream()
                .map(formMapper::entityToResponseModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<FormResponseModel> getFormsByCustomer(String customerId) {
        log.info("Fetching forms for customer: {}", customerId);
        return formRepository.findByCustomerId(customerId).stream()
                .map(formMapper::entityToResponseModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<FormResponseModel> getFormsCreatedBy(String salespersonId) {
        log.info("Fetching forms created by: {}", salespersonId);
        return formRepository.findByAssignedByUserId(salespersonId).stream()
                .map(formMapper::entityToResponseModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<FormResponseModel> getFormsByStatus(FormStatus status) {
        log.info("Fetching forms with status: {}", status);
        return formRepository.findByFormStatus(status).stream()
                .map(formMapper::entityToResponseModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FormResponseModel updateFormData(String formId, FormDataUpdateRequestModel updateRequest, String customerId) {
        log.info("Updating form data for form: {} by customer: {}", formId, customerId);

        Form form = formRepository.findByFormIdentifier_FormId(formId)
                .orElseThrow(() -> new NotFoundException("Form not found with ID: " + formId));

        // Verify customer owns this form
        if (!form.getCustomerId().equals(customerId)) {
            throw new InvalidInputException("Customer is not authorized to update this form");
        }

        // Verify form is in a state that allows editing
        if (form.getFormStatus() == FormStatus.COMPLETED) {
            throw new InvalidInputException("Cannot update a completed form");
        }

        // Update form data
        form.setFormData(updateRequest.getFormData());

        // Update status to IN_PROGRESS if it was ASSIGNED or REOPENED
        if (form.getFormStatus() == FormStatus.ASSIGNED || form.getFormStatus() == FormStatus.REOPENED) {
            form.setFormStatus(FormStatus.IN_PROGRESS);
        }

        Form savedForm = formRepository.save(form);
        log.info("Form data updated successfully");

        return formMapper.entityToResponseModel(savedForm);
    }

    @Override
    @Transactional
    public FormResponseModel submitForm(String formId, FormDataUpdateRequestModel updateRequest, String customerId) {
        log.info("Submitting form: {} by customer: {}", formId, customerId);

        Form form = formRepository.findByFormIdentifier_FormId(formId)
                .orElseThrow(() -> new NotFoundException("Form not found with ID: " + formId));

        // Verify customer owns this form
        if (!form.getCustomerId().equals(customerId)) {
            throw new InvalidInputException("Customer is not authorized to submit this form");
        }

        // Verify form is in a state that allows submission
        if (form.getFormStatus() == FormStatus.COMPLETED || form.getFormStatus() == FormStatus.SUBMITTED) {
            throw new InvalidInputException("Form has already been submitted");
        }

        // Update form data
        form.setFormData(updateRequest.getFormData());
        form.setFormStatus(FormStatus.SUBMITTED);
        form.setLastSubmittedDate(LocalDateTime.now());

        // Set first submission date if this is the first submission
        if (form.getFirstSubmittedDate() == null) {
            form.setFirstSubmittedDate(LocalDateTime.now());
        }

        Form savedForm = formRepository.save(form);

        // Create submission history entry
        createSubmissionHistoryEntry(savedForm, updateRequest.getSubmissionNotes(), customerId);

        log.info("Form submitted successfully");

        // Notify salesperson/owner
        Users customer = usersRepository.findByUserIdentifier(customerId).orElse(null);
        sendFormSubmittedNotification(savedForm, customer);

        return formMapper.entityToResponseModel(savedForm);
    }

    @Override
    @Transactional
    public FormResponseModel reopenForm(String formId, FormReopenRequestModel reopenRequest, String reopenedByUserId) {
        log.info("Reopening form: {} by user: {}", formId, reopenedByUserId);

        Form form = formRepository.findByFormIdentifier_FormId(formId)
                .orElseThrow(() -> new NotFoundException("Form not found with ID: " + formId));

        // Verify user exists
        Users reopenedBy = usersRepository.findByAuth0UserId(reopenedByUserId)
                .orElseThrow(() -> new NotFoundException("User not found with Auth0 ID: " + reopenedByUserId));

        // Can only reopen submitted forms
        if (form.getFormStatus() != FormStatus.SUBMITTED) {
            throw new InvalidInputException("Can only reopen submitted forms");
        }

        // Update form status
        form.setFormStatus(FormStatus.REOPENED);
        form.setReopenedDate(LocalDateTime.now());
        form.setReopenedByUserId(reopenedBy.getUserIdentifier().getUserId().toString());
        form.setReopenReason(reopenRequest.getReopenReason());
        form.setReopenCount(form.getReopenCount() + 1);

        // Update instructions if provided
        if (reopenRequest.getNewInstructions() != null && !reopenRequest.getNewInstructions().isEmpty()) {
            form.setInstructions(reopenRequest.getNewInstructions());
        }

        Form savedForm = formRepository.save(form);
        log.info("Form reopened successfully. Reopen count: {}", savedForm.getReopenCount());

        // Send notification to customer
        Users customer = usersRepository.findByUserIdentifier(form.getCustomerId()).orElse(null);
        sendFormReopenedNotification(savedForm, customer, reopenRequest.getReopenReason());

        return formMapper.entityToResponseModel(savedForm);
    }

    @Override
    @Transactional
    public FormResponseModel completeForm(String formId, String completedByUserId) {
        log.info("Completing form: {} by user: {}", formId, completedByUserId);

        Form form = formRepository.findByFormIdentifier_FormId(formId)
                .orElseThrow(() -> new NotFoundException("Form not found with ID: " + formId));

        // Verify user exists
        usersRepository.findByAuth0UserId(completedByUserId)
                .orElseThrow(() -> new NotFoundException("User not found with Auth0 ID: " + completedByUserId));

        // Can only complete submitted forms
        if (form.getFormStatus() != FormStatus.SUBMITTED) {
            throw new InvalidInputException("Can only complete submitted forms");
        }

        // Update form status
        form.setFormStatus(FormStatus.COMPLETED);
        form.setCompletedDate(LocalDateTime.now());

        Form savedForm = formRepository.save(form);
        log.info("Form completed successfully");

        return formMapper.entityToResponseModel(savedForm);
    }

    @Override
    @Transactional
    public FormResponseModel updateFormDetails(String formId, FormRequestModel requestModel, String updatedByUserId) {
        log.info("Updating form details for form: {} by user: {}", formId, updatedByUserId);

        Form form = formRepository.findByFormIdentifier_FormId(formId)
                .orElseThrow(() -> new NotFoundException("Form not found with ID: " + formId));

        // Verify user exists
        usersRepository.findByAuth0UserId(updatedByUserId)
                .orElseThrow(() -> new NotFoundException("User not found with Auth0 ID: " + updatedByUserId));

        // Update form details
        formMapper.updateEntityFromRequestModel(requestModel, form);

        Form savedForm = formRepository.save(form);
        log.info("Form details updated successfully");

        return formMapper.entityToResponseModel(savedForm);
    }

    @Override
    @Transactional
    public void deleteForm(String formId, String deletedByUserId) {
        log.info("Deleting form: {} by user: {}", formId, deletedByUserId);

        Form form = formRepository.findByFormIdentifier_FormId(formId)
                .orElseThrow(() -> new NotFoundException("Form not found with ID: " + formId));

        // Verify user exists
        usersRepository.findByAuth0UserId(deletedByUserId)
                .orElseThrow(() -> new NotFoundException("User not found with Auth0 ID: " + deletedByUserId));

        // Delete associated history entries first
        List<FormSubmissionHistory> history = historyRepository.findByFormIdentifierOrderBySubmittedAtDesc(formId);
        historyRepository.deleteAll(history);

        // Delete form
        formRepository.delete(form);
        log.info("Form deleted successfully");
    }

    @Override
    public List<FormSubmissionHistoryResponseModel> getFormSubmissionHistory(String formId) {
        log.info("Fetching submission history for form: {}", formId);

        // Verify form exists
        formRepository.findByFormIdentifier_FormId(formId)
                .orElseThrow(() -> new NotFoundException("Form not found with ID: " + formId));

        return historyRepository.findByFormIdentifierOrderBySubmissionNumberAsc(formId).stream()
                .map(historyMapper::entityToResponseModel)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasFormOfType(String projectIdentifier, String customerId, FormType formType) {
        return formRepository.existsByProjectIdentifierAndCustomerIdAndFormType(
                projectIdentifier, customerId, formType);
    }

    // ========== Private Helper Methods ==========

    private void createSubmissionHistoryEntry(Form form, String submissionNotes, String customerId) {
        // Determine submission number
        Long existingCount = historyRepository.countByFormIdentifier(form.getFormIdentifier().getFormId());
        int submissionNumber = existingCount.intValue() + 1;

        Users customer = usersRepository.findByUserIdentifier(customerId).orElse(null);
        String customerName = customer != null ? getFullName(customer) : "Unknown Customer";

        // Create snapshot with deep copy of form data
        Map<String, Object> dataSnapshot = new HashMap<>(form.getFormData());

        FormSubmissionHistory history = new FormSubmissionHistory(
                form.getFormIdentifier().getFormId(),
                submissionNumber,
                form.getFormStatus(),
                dataSnapshot,
                customerId,
                customerName
        );
        history.setSubmissionNotes(submissionNotes);

        historyRepository.save(history);
        log.info("Created submission history entry #{} for form: {}", submissionNumber, form.getFormIdentifier().getFormId());
    }

    private void sendFormAssignedNotification(Form form, Users customer) {
        try {
            String formTypeName = getFormTypeDisplayName(form.getFormType());
            String notificationTitle = "New Form Assigned: " + formTypeName;
            String notificationMessage = String.format(
                    "A %s form has been assigned to you for project %s. Please complete it at your earliest convenience.",
                    formTypeName, form.getProjectIdentifier()
            );

            // Create system notification
            notificationService.createNotification(
                    customer.getUserIdentifier().getUserId(),
                    notificationTitle,
                    notificationMessage,
                    NotificationCategory.FORM_ASSIGNED,
                    "/forms/" + form.getFormIdentifier().getFormId()
            );

            // Send email
            String emailSubject = "New Form to Complete: " + formTypeName;
            String emailBody = buildFormAssignedEmailBody(form, customer);

            mailerServiceClient.sendEmail(
                    customer.getPrimaryEmail(),
                    emailSubject,
                    emailBody,
                    "Les Constructions Dominic Cyr"
            ).subscribe(
                    null,
                    error -> log.error("Failed to send form assigned email to {}: {}", customer.getPrimaryEmail(), error.getMessage()),
                    () -> log.info("Form assigned email sent to: {}", customer.getPrimaryEmail())
            );

        } catch (Exception e) {
            log.error("Error sending form assigned notification: {}", e.getMessage(), e);
        }
    }

    private void sendFormSubmittedNotification(Form form, Users customer) {
        try {
            // Notify salesperson/owner who assigned the form
            Users assignedBy = usersRepository.findByUserIdentifier(form.getAssignedByUserId()).orElse(null);
            if (assignedBy == null) {
                log.warn("Cannot send form submitted notification: assigner not found");
                return;
            }

            String formTypeName = getFormTypeDisplayName(form.getFormType());
            String customerName = customer != null ? getFullName(customer) : form.getCustomerName();

            String notificationTitle = "Form Submitted: " + formTypeName;
            String notificationMessage = String.format(
                    "%s has submitted their %s form for project %s.",
                    customerName, formTypeName, form.getProjectIdentifier()
            );

            // Create system notification
            notificationService.createNotification(
                    assignedBy.getUserIdentifier().getUserId(),
                    notificationTitle,
                    notificationMessage,
                    NotificationCategory.FORM_SUBMITTED,
                    "/forms/" + form.getFormIdentifier().getFormId()
            );

            // Send email
            String emailSubject = "Form Submitted by " + customerName;
            String emailBody = buildFormSubmittedEmailBody(form, customerName);

            mailerServiceClient.sendEmail(
                    assignedBy.getPrimaryEmail(),
                    emailSubject,
                    emailBody,
                    "Les Constructions Dominic Cyr"
            ).subscribe(
                    null,
                    error -> log.error("Failed to send form submitted email to {}: {}", assignedBy.getPrimaryEmail(), error.getMessage()),
                    () -> log.info("Form submitted email sent to: {}", assignedBy.getPrimaryEmail())
            );

        } catch (Exception e) {
            log.error("Error sending form submitted notification: {}", e.getMessage(), e);
        }
    }

    private void sendFormReopenedNotification(Form form, Users customer, String reopenReason) {
        try {
            if (customer == null) {
                log.warn("Cannot send form reopened notification: customer not found");
                return;
            }

            String formTypeName = getFormTypeDisplayName(form.getFormType());
            String notificationTitle = "Form Reopened: " + formTypeName;
            String notificationMessage = String.format(
                    "Your %s form for project %s has been reopened. Reason: %s. Please review and resubmit.",
                    formTypeName, form.getProjectIdentifier(), reopenReason
            );

            // Create system notification
            notificationService.createNotification(
                    customer.getUserIdentifier().getUserId(),
                    notificationTitle,
                    notificationMessage,
                    NotificationCategory.FORM_REOPENED,
                    "/forms/" + form.getFormIdentifier().getFormId()
            );

            // Send email
            String emailSubject = "Form Reopened: " + formTypeName;
            String emailBody = buildFormReopenedEmailBody(form, customer, reopenReason);

            mailerServiceClient.sendEmail(
                    customer.getPrimaryEmail(),
                    emailSubject,
                    emailBody,
                    "Les Constructions Dominic Cyr"
            ).subscribe(
                    null,
                    error -> log.error("Failed to send form reopened email to {}: {}", customer.getPrimaryEmail(), error.getMessage()),
                    () -> log.info("Form reopened email sent to: {}", customer.getPrimaryEmail())
            );

        } catch (Exception e) {
            log.error("Error sending form reopened notification: {}", e.getMessage(), e);
        }
    }

    private String getFullName(Users user) {
        if (user == null) return "Unknown";
        String first = user.getFirstName() == null ? "" : user.getFirstName();
        String last = user.getLastName() == null ? "" : user.getLastName();
        return (first + " " + last).trim();
    }

    private String getFormTypeDisplayName(FormType formType) {
        switch (formType) {
            case EXTERIOR_DOORS:
                return "Exterior Doors";
            case GARAGE_DOORS:
                return "Garage Doors";
            case WINDOWS:
                return "Windows";
            case ASPHALT_SHINGLES:
                return "Asphalt Shingles";
            case WOODWORK:
                return "Woodwork";
            case PAINT:
                return "Paint";
            default:
                return formType.name();
        }
    }

    private String buildFormAssignedEmailBody(Form form, Users customer) {
        String formTypeName = getFormTypeDisplayName(form.getFormType());
        StringBuilder body = new StringBuilder();
        body.append("<html><body>");
        body.append("<h2>New Form Assignment</h2>");
        body.append("<p>Hello ").append(getFullName(customer)).append(",</p>");
        body.append("<p>A new <strong>").append(formTypeName).append("</strong> form has been assigned to you.</p>");
        body.append("<p><strong>Project:</strong> ").append(form.getProjectIdentifier()).append("</p>");

        if (form.getInstructions() != null && !form.getInstructions().isEmpty()) {
            body.append("<p><strong>Instructions:</strong></p>");
            body.append("<p>").append(escapeHtml(form.getInstructions())).append("</p>");
        }

        body.append("<p>Please log in to the customer portal to complete this form.</p>");
        body.append("<p>Thank you,<br/>Les Constructions Dominic Cyr Team</p>");
        body.append("</body></html>");

        return body.toString();
    }

    private String buildFormSubmittedEmailBody(Form form, String customerName) {
        String formTypeName = getFormTypeDisplayName(form.getFormType());
        StringBuilder body = new StringBuilder();
        body.append("<html><body>");
        body.append("<h2>Form Submitted</h2>");
        body.append("<p>").append(customerName).append(" has submitted their <strong>").append(formTypeName).append("</strong> form.</p>");
        body.append("<p><strong>Project:</strong> ").append(form.getProjectIdentifier()).append("</p>");
        body.append("<p><strong>Form ID:</strong> ").append(form.getFormIdentifier().getFormId()).append("</p>");
        body.append("<p><strong>Submitted:</strong> ").append(form.getLastSubmittedDate()).append("</p>");
        body.append("<p>Please review the submission in the admin portal.</p>");
        body.append("<p>Best regards,<br/>System Notification</p>");
        body.append("</body></html>");

        return body.toString();
    }

    private String buildFormReopenedEmailBody(Form form, Users customer, String reopenReason) {
        String formTypeName = getFormTypeDisplayName(form.getFormType());
        StringBuilder body = new StringBuilder();
        body.append("<html><body>");
        body.append("<h2>Form Reopened for Review</h2>");
        body.append("<p>Hello ").append(getFullName(customer)).append(",</p>");
        body.append("<p>Your <strong>").append(formTypeName).append("</strong> form has been reopened for revision.</p>");
        body.append("<p><strong>Project:</strong> ").append(form.getProjectIdentifier()).append("</p>");
        body.append("<p><strong>Reason for reopening:</strong></p>");
        body.append("<p>").append(escapeHtml(reopenReason)).append("</p>");

        if (form.getInstructions() != null && !form.getInstructions().isEmpty()) {
            body.append("<p><strong>Updated Instructions:</strong></p>");
            body.append("<p>").append(escapeHtml(form.getInstructions())).append("</p>");
        }

        body.append("<p>Please log in to the customer portal to review and resubmit your form.</p>");
        body.append("<p>Thank you,<br/>Les Constructions Dominic Cyr Team</p>");
        body.append("</body></html>");

        return body.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("\n", "<br/>");
    }
}

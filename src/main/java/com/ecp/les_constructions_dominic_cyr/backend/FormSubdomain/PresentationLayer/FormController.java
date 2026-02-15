package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.BusinessLayer.FormService;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormStatus;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormType;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for managing customer forms.
 */
@RestController
@RequestMapping("/api/v1/forms")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FormController {

    private final FormService formService;
    private final UserService userService;
    private final LotRepository lotRepository;

    private static final String ROLE_OWNER = "ROLE_OWNER";
    private static final String ROLE_SALESPERSON = "ROLE_SALESPERSON";
    private static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";

    /**
     * Create and assign a new form to a customer
     * Only salespersons and owners can create forms
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_SALESPERSON')")
    public ResponseEntity<FormResponseModel> createForm(
            @Valid @RequestBody FormRequestModel requestModel,
            @AuthenticationPrincipal Jwt jwt
    ) {
        log.info("POST /api/v1/forms - Creating new form of type: {}", requestModel.getFormType());

        String auth0UserId = jwt.getSubject();
        FormResponseModel response = formService.createAndAssignForm(requestModel, auth0UserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a specific form by ID
     * Accessible by owner, salesperson, or the customer who owns the form
     */
    @GetMapping("/{formId}")
    public ResponseEntity<FormResponseModel> getFormById(
            @PathVariable String formId,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        log.info("GET /api/v1/forms/{}", formId);

        FormResponseModel form = formService.getFormById(formId);

        // Check authorization
        if (!isAuthorizedToViewForm(form, jwt, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(form);
    }

    /**
     * Get all forms (with optional filters)
     * Owners/Salespersons: can see all forms
     * Customers: only see their own forms
     */
    @GetMapping
    public ResponseEntity<List<FormResponseModel>> getAllForms(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) FormStatus status,
            @RequestParam(required = false) FormType formType,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        log.info("GET /api/v1/forms - projectId: {}, customerId: {}, status: {}, formType: {}",
                projectId, customerId, status, formType);

        List<FormResponseModel> forms;

        // Determine which forms to retrieve based on role and filters
        if (isOwnerOrSalesperson(authentication)) {
            // Owners and salespersons can see all forms, apply filters
            if (projectId != null) {
                forms = formService.getFormsByProject(projectId);
            } else if (customerId != null) {
                forms = formService.getFormsByCustomer(customerId);
            } else if (status != null) {
                forms = formService.getFormsByStatus(status);
            } else {
                // Get all forms (could be expensive in production, consider pagination)
                UserResponseModel currentUser = getUserByAuth0Id(jwt.getSubject());
                String userId = currentUser.getUserIdentifier();
                forms = formService.getFormsCreatedBy(userId);
            }
        } else {
            // Customers only see their own forms
            UserResponseModel currentUser = getUserByAuth0Id(jwt.getSubject());
            String userId = currentUser.getUserIdentifier();
            forms = formService.getFormsByCustomer(userId);
        }

        // Apply additional filters
        if (status != null) {
            forms = forms.stream()
                    .filter(f -> f.getFormStatus() == status)
                    .collect(Collectors.toList());
        }
        if (formType != null) {
            forms = forms.stream()
                    .filter(f -> f.getFormType() == formType)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(forms);
    }

    /**
     * Get all forms for a specific lot
     * Accessible by owner, salesperson assigned to the lot, or the customer who owns the forms
     */
    @GetMapping("/lot/{lotId}")
    public ResponseEntity<List<FormResponseModel>> getFormsByLot(
            @PathVariable String lotId,
            @RequestParam(required = false) FormStatus status,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        log.info("GET /api/v1/forms/lot/{}", lotId);

        if (jwt == null || authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserResponseModel currentUser = getUserByAuth0Id(jwt.getSubject());
        String userId = currentUser.getUserIdentifier();

        if (!isAuthorizedForLotForms(lotId, userId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FormResponseModel> forms = formService.getFormsByLot(lotId);

        if (hasAuthority(authentication, ROLE_CUSTOMER)) {
            forms = forms.stream()
                    .filter(form -> userId.equals(form.getCustomerId()))
                    .collect(Collectors.toList());
        }

        if (status != null) {
            FormStatus requestedStatus = status;
            forms = forms.stream()
                    .filter(form -> form.getFormStatus() == requestedStatus)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(forms);
    }

    /**
     * Get all forms for a specific project
     */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_SALESPERSON')")
    public ResponseEntity<List<FormResponseModel>> getFormsByProject(@PathVariable String projectId) {
        log.info("GET /api/v1/forms/project/{}", projectId);
        List<FormResponseModel> forms = formService.getFormsByProject(projectId);
        return ResponseEntity.ok(forms);
    }

    /**
     * Get all forms assigned to a specific customer
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<FormResponseModel>> getFormsByCustomer(
            @PathVariable String customerId,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        log.info("GET /api/v1/forms/customer/{}", customerId);

        // Customers can only view their own forms
        if (!isOwnerOrSalesperson(authentication)) {
            UserResponseModel currentUser = getUserByAuth0Id(jwt.getSubject());
            if (!currentUser.getUserIdentifier().equals(customerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        List<FormResponseModel> forms = formService.getFormsByCustomer(customerId);
        return ResponseEntity.ok(forms);
    }

    /**
     * Get all forms assigned to the current authenticated customer
     * This endpoint is specifically for customers to access their own forms
     */
    @GetMapping("/my-forms")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<List<FormResponseModel>> getMyForms(
            @AuthenticationPrincipal Jwt jwt
    ) {
        log.info("GET /api/v1/forms/my-forms - Customer accessing their forms");

        UserResponseModel currentUser = getUserByAuth0Id(jwt.getSubject());
        String userId = currentUser.getUserIdentifier();

        List<FormResponseModel> forms = formService.getFormsByCustomer(userId);
        return ResponseEntity.ok(forms);
    }

    /**
     * Update form data (used by customers filling out forms)
     */
    @PutMapping("/{formId}/data")
    public ResponseEntity<FormResponseModel> updateFormData(
            @PathVariable String formId,
            @Valid @RequestBody FormDataUpdateRequestModel updateRequest,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        log.info("PUT /api/v1/forms/{}/data, isSubmitting: {}", formId, updateRequest.isSubmitting());

        UserResponseModel currentUser = getUserByAuth0Id(jwt.getSubject());
        String userId = currentUser.getUserIdentifier();

        // Verify authorization
        FormResponseModel form = formService.getFormById(formId);
        if (!form.getCustomerId().equals(userId) && !isOwnerOrSalesperson(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // If the request indicates submission, use submitForm instead
        if (updateRequest.isSubmitting()) {
            log.info("Routing to submitForm for form: {}", formId);
            FormResponseModel updatedForm = formService.submitForm(formId, updateRequest, userId);
            log.info("Form submitted, new status: {}", updatedForm.getFormStatus());
            return ResponseEntity.ok(updatedForm);
        }

        log.info("Routing to updateFormData (save only) for form: {}", formId);
        FormResponseModel updatedForm = formService.updateFormData(formId, updateRequest, userId);
        return ResponseEntity.ok(updatedForm);
    }

    /**
     * Submit a form
     */
    @PostMapping("/{formId}/submit")
    public ResponseEntity<FormResponseModel> submitForm(
            @PathVariable String formId,
            @Valid @RequestBody FormDataUpdateRequestModel updateRequest,
            @AuthenticationPrincipal Jwt jwt
    ) {
        log.info("POST /api/v1/forms/{}/submit", formId);

        UserResponseModel currentUser = getUserByAuth0Id(jwt.getSubject());
        String userId = currentUser.getUserIdentifier();

        FormResponseModel updatedForm = formService.submitForm(formId, updateRequest, userId);
        return ResponseEntity.ok(updatedForm);
    }

    /**
     * Reopen a form for customer to re-edit
     * Only salespersons and owners can reopen forms
     */
    @PostMapping("/{formId}/reopen")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_SALESPERSON')")
    public ResponseEntity<FormResponseModel> reopenForm(
            @PathVariable String formId,
            @Valid @RequestBody FormReopenRequestModel reopenRequest,
            @AuthenticationPrincipal Jwt jwt
    ) {
        log.info("POST /api/v1/forms/{}/reopen", formId);

        String auth0UserId = jwt.getSubject();
        FormResponseModel updatedForm = formService.reopenForm(formId, reopenRequest, auth0UserId);

        return ResponseEntity.ok(updatedForm);
    }

    /**
     * Mark form as completed
     * Only salespersons and owners can complete forms
     */
    @PostMapping("/{formId}/complete")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_SALESPERSON')")
    public ResponseEntity<FormResponseModel> completeForm(
            @PathVariable String formId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        log.info("POST /api/v1/forms/{}/complete", formId);

        String auth0UserId = jwt.getSubject();
        FormResponseModel updatedForm = formService.completeForm(formId, auth0UserId);

        return ResponseEntity.ok(updatedForm);
    }

    /**
     * Download a finalized form as PDF
     * Accessible by owner, salesperson assigned to the lot, or the customer who owns the form
     */
    @GetMapping("/{formId}/download")
    public ResponseEntity<byte[]> downloadForm(
            @PathVariable String formId,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        log.info("GET /api/v1/forms/{}/download", formId);

        if (jwt == null || authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        FormResponseModel form = formService.getFormById(formId);
        if (form.getFormStatus() != FormStatus.COMPLETED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        UserResponseModel currentUser = getUserByAuth0Id(jwt.getSubject());
        String userId = currentUser.getUserIdentifier();

        if (!isAuthorizedForFormDownload(form, userId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        byte[] pdfBytes = buildFormPdf(form);
        String fileName = buildFormFileName(form);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(pdfBytes);
    }

    /**
     * Update form details (title, instructions)
     * Only salespersons and owners can update form details
     */
    @PutMapping("/{formId}")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_SALESPERSON')")
    public ResponseEntity<FormResponseModel> updateFormDetails(
            @PathVariable String formId,
            @Valid @RequestBody FormRequestModel requestModel,
            @AuthenticationPrincipal Jwt jwt
    ) {
        log.info("PUT /api/v1/forms/{}", formId);

        String auth0UserId = jwt.getSubject();
        FormResponseModel updatedForm = formService.updateFormDetails(formId, requestModel, auth0UserId);

        return ResponseEntity.ok(updatedForm);
    }

    /**
     * Delete a form
     * Only salespersons and owners can delete forms
     */
    @DeleteMapping("/{formId}")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_SALESPERSON')")
    public ResponseEntity<Void> deleteForm(
            @PathVariable String formId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        log.info("DELETE /api/v1/forms/{}", formId);

        String auth0UserId = jwt.getSubject();
        formService.deleteForm(formId, auth0UserId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Get submission history for a form
     */
    @GetMapping("/{formId}/history")
    public ResponseEntity<List<FormSubmissionHistoryResponseModel>> getFormHistory(
            @PathVariable String formId,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        log.info("GET /api/v1/forms/{}/history", formId);

        FormResponseModel form = formService.getFormById(formId);

        // Check authorization
        if (!isAuthorizedToViewForm(form, jwt, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FormSubmissionHistoryResponseModel> history = formService.getFormSubmissionHistory(formId);
        return ResponseEntity.ok(history);
    }

    // ========== Helper Methods ==========

    private boolean isOwnerOrSalesperson(Authentication authentication) {
        if (authentication == null) return false;
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.contains(new SimpleGrantedAuthority(ROLE_OWNER)) ||
                authorities.contains(new SimpleGrantedAuthority(ROLE_SALESPERSON));
    }

    private boolean hasAuthority(Authentication authentication, String role) {
        if (authentication == null) return false;
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.contains(new SimpleGrantedAuthority(role));
    }

    private boolean isAuthorizedForLotForms(String lotId, String userId, Authentication authentication) {
        if (hasAuthority(authentication, ROLE_OWNER)) {
            return true;
        }

        if (!hasAuthority(authentication, ROLE_SALESPERSON) && !hasAuthority(authentication, ROLE_CUSTOMER)) {
            return false;
        }

        return isAssignedToLot(lotId, userId);
    }

    private boolean isAuthorizedForFormDownload(FormResponseModel form, String userId, Authentication authentication) {
        if (hasAuthority(authentication, ROLE_OWNER)) {
            return true;
        }

        if (hasAuthority(authentication, ROLE_CUSTOMER)) {
            return userId.equals(form.getCustomerId()) && isAssignedToLot(form.getLotIdentifier(), userId);
        }

        if (hasAuthority(authentication, ROLE_SALESPERSON)) {
            return isAssignedToLot(form.getLotIdentifier(), userId);
        }

        return false;
    }

    private boolean isAssignedToLot(String lotId, String userId) {
        if (lotId == null || userId == null) {
            return false;
        }

        try {
            UUID userUuid = UUID.fromString(userId);
            UUID lotUuid = UUID.fromString(lotId);
            List<Lot> assignedLots = lotRepository.findByAssignedUserId(userUuid);
            return assignedLots.stream()
                    .anyMatch(lot -> lot.getLotIdentifier().getLotId().equals(lotUuid));
        } catch (IllegalArgumentException ex) {
            // Invalid UUID format for userId or lotId; treat as not assigned
            return false;
        }
    }

    private boolean isAuthorizedToViewForm(FormResponseModel form, Jwt jwt, Authentication authentication) {
        if (isOwnerOrSalesperson(authentication)) {
            return true;
        }

        // Check if customer owns the form
        UserResponseModel currentUser = getUserByAuth0Id(jwt.getSubject());
        return form.getCustomerId().equals(currentUser.getUserIdentifier());
    }

    private byte[] buildFormPdf(FormResponseModel form) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            document.add(new Paragraph("Finalized Form").setBold().setFontSize(18));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Form Summary").setBold().setFontSize(14));
            document.add(new Paragraph("Form Type: " + formatFormType(form.getFormType())));
            document.add(new Paragraph("Form ID: " + form.getFormId()));
            document.add(new Paragraph("Status: " + String.valueOf(form.getFormStatus())));
            document.add(new Paragraph("Project: " + form.getProjectIdentifier()));
            document.add(new Paragraph("Lot: " + form.getLotIdentifier()));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Customer Details").setBold().setFontSize(14));
            document.add(new Paragraph("Customer Name: " + safeValue(form.getCustomerName())));
            document.add(new Paragraph("Customer Email: " + safeValue(form.getCustomerEmail())));
            document.add(new Paragraph("Customer ID: " + safeValue(form.getCustomerId())));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Assignment Details").setBold().setFontSize(14));
            document.add(new Paragraph("Assigned By: " + safeValue(form.getAssignedByName())));
            document.add(new Paragraph("Assigned By ID: " + safeValue(form.getAssignedByUserId())));
            document.add(new Paragraph("Assigned Date: " + safeValue(form.getAssignedDate())));
            if (form.getInstructions() != null && !form.getInstructions().isEmpty()) {
                document.add(new Paragraph("Instructions: " + form.getInstructions()));
            }
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Timeline").setBold().setFontSize(14));
            document.add(new Paragraph("First Submitted: " + safeValue(form.getFirstSubmittedDate())));
            document.add(new Paragraph("Last Submitted: " + safeValue(form.getLastSubmittedDate())));
            document.add(new Paragraph("Completed: " + safeValue(form.getCompletedDate())));
            if (form.getReopenedDate() != null) {
                document.add(new Paragraph("Reopened Date: " + safeValue(form.getReopenedDate())));
                document.add(new Paragraph("Reopened By: " + safeValue(form.getReopenedByUserId())));
                document.add(new Paragraph("Reopen Reason: " + safeValue(form.getReopenReason())));
            }
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Form Data").setBold().setFontSize(14));
            Table table = new Table(2).useAllAvailableWidth();
            table.addHeaderCell(new Cell().add(new Paragraph("Field").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Value").setBold()));

            if (form.getFormData() != null && !form.getFormData().isEmpty()) {
                for (Map.Entry<String, Object> entry : form.getFormData().entrySet()) {
                    String value = stringifyValue(entry.getValue(), objectMapper);
                    table.addCell(new Cell().add(new Paragraph(entry.getKey())));
                    table.addCell(new Cell().add(new Paragraph(value)));
                }
            } else {
                table.addCell(new Cell().add(new Paragraph("(No data)")));
                table.addCell(new Cell().add(new Paragraph("")));
            }

            document.add(table);
            return outputStream.toByteArray();
        } finally {
            document.close();
        }
    }

    private String safeValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String stringifyValue(Object value, ObjectMapper objectMapper) {
        if (value == null) {
            return "";
        }

        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    private String buildFormFileName(FormResponseModel form) {
        String formType = form.getFormType() != null ? form.getFormType().name().toLowerCase() : "form";
        return "form_" + formType + "_" + form.getFormId() + ".pdf";
    }

    private String formatFormType(FormType formType) {
        if (formType == null) {
            return "Form";
        }
        return formType.name().replace('_', ' ').toLowerCase();
    }

    private UserResponseModel getUserByAuth0Id(String auth0UserId) {
        return userService.getUserByAuth0Id(auth0UserId);
    }
}

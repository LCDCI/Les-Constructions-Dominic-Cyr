package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.BusinessLayer.FormService;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormStatus;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormType;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
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

    private static final String ROLE_OWNER = "ROLE_OWNER";
    private static final String ROLE_SALESPERSON = "ROLE_SALESPERSON";
    private static final String ROLE_CONTRACTOR = "ROLE_CONTRACTOR";
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
        if (isPrivilegedRole(authentication)) {
            // Owners, salespersons, and contractors can see all forms, apply filters
            if (projectId != null) {
                forms = formService.getFormsByProject(projectId);
            } else if (customerId != null) {
                forms = formService.getFormsByCustomer(customerId);
            } else if (status != null) {
                forms = formService.getFormsByStatus(status);
            } else {
                // Get all forms for privileged roles
                forms = formService.getAllForms();
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
        if (!isPrivilegedRole(authentication)) {
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
        log.info("PUT /api/v1/forms/{}/data", formId);

        UserResponseModel currentUser = getUserByAuth0Id(jwt.getSubject());
        String userId = currentUser.getUserIdentifier();

        // Verify authorization
        FormResponseModel form = formService.getFormById(formId);
        if (!form.getCustomerId().equals(userId) && !isPrivilegedRole(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // If the request indicates submission, use submitForm instead
        if (updateRequest.isSubmitting()) {
            FormResponseModel updatedForm = formService.submitForm(formId, updateRequest, userId);
            return ResponseEntity.ok(updatedForm);
        }

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

    private boolean isPrivilegedRole(Authentication authentication) {
        if (authentication == null) return false;
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.contains(new SimpleGrantedAuthority(ROLE_OWNER)) ||
                authorities.contains(new SimpleGrantedAuthority(ROLE_SALESPERSON)) ||
                authorities.contains(new SimpleGrantedAuthority(ROLE_CONTRACTOR));
    }

    private boolean isAuthorizedToViewForm(FormResponseModel form, Jwt jwt, Authentication authentication) {
        if (isPrivilegedRole(authentication)) {
            return true;
        }

        // Check if customer owns the form
        UserResponseModel currentUser = getUserByAuth0Id(jwt.getSubject());
        return form.getCustomerId().equals(currentUser.getUserIdentifier());
    }

    private UserResponseModel getUserByAuth0Id(String auth0UserId) {
        return userService.getUserByAuth0Id(auth0UserId);
    }
}

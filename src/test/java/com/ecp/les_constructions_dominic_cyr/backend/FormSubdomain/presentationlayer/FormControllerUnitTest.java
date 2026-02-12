package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.BusinessLayer.FormService;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormStatus;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormType;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer.*;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for FormController using WebMvcTest.
 */
@WebMvcTest(FormController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(com.ecp.les_constructions_dominic_cyr.backend.utils.GlobalControllerExceptionHandler.class)
class FormControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FormService formService;

    @MockitoBean
    private UserService userService;

    private ObjectMapper objectMapper;
    private FormResponseModel testFormResponse;
    private FormRequestModel testFormRequest;
    private UserResponseModel testCustomer;
    private UserResponseModel testSalesperson;
    
    // UUID constants for testing - must match FormServiceImplUnitTest
    private static final String CUSTOMER_UUID = "123e4567-e89b-12d3-a456-426614174000";
    private static final String SALESPERSON_UUID = "223e4567-e89b-12d3-a456-426614174001";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Setup test customer
        testCustomer = new UserResponseModel();
        testCustomer.setUserIdentifier(CUSTOMER_UUID);
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Customer");
        testCustomer.setPrimaryEmail("john.customer@example.com");
        testCustomer.setUserRole(UserRole.CUSTOMER);

        // Setup test salesperson
        testSalesperson = new UserResponseModel();
        testSalesperson.setUserIdentifier(SALESPERSON_UUID);
        testSalesperson.setFirstName("Jane");
        testSalesperson.setLastName("Salesperson");
        testSalesperson.setPrimaryEmail("jane.salesperson@example.com");
        testSalesperson.setUserRole(UserRole.SALESPERSON);

        // Setup test form request
        testFormRequest = FormRequestModel.builder()
                .formType(FormType.WINDOWS)
                .projectIdentifier("project-123")
                .lotIdentifier("lot-uuid-456")
                .customerId(CUSTOMER_UUID)
                .formTitle("Window Selection")
                .instructions("Please select windows")
                .formData(new HashMap<>())
                .build();

        // Setup test form response
        testFormResponse = FormResponseModel.builder()
                .formId("form-id-789")
                .formType(FormType.WINDOWS)
                .formStatus(FormStatus.ASSIGNED)
                .projectIdentifier("project-123")
                .lotIdentifier("lot-uuid-456")
                .customerId(CUSTOMER_UUID)
                .customerName("John Customer")
                .assignedByUserId(SALESPERSON_UUID)
                .assignedByName("Jane Salesperson")
                .formTitle("Window Selection")
                .instructions("Please select windows")
                .formData(new HashMap<>())
                .reopenCount(0)
                .build();
    }

    // ========== Create Form Tests ==========

    @Test
    void createForm_WithValidRequest_ReturnsCreatedForm() throws Exception {
        when(formService.createAndAssignForm(any(FormRequestModel.class), anyString()))
                .thenReturn(testFormResponse);

        mockMvc.perform(post("/api/v1/forms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testFormRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.formId").value("form-id-789"))
                .andExpect(jsonPath("$.formType").value("WINDOWS"))
                .andExpect(jsonPath("$.formStatus").value("ASSIGNED"));

        verify(formService).createAndAssignForm(any(FormRequestModel.class), anyString());
    }

    // ========== Get Form By ID Tests ==========

    @Test
    void getFormById_WithValidId_ReturnsForm() throws Exception {
        when(formService.getFormById("form-id-789")).thenReturn(testFormResponse);
        when(userService.getUserByAuth0Id(anyString())).thenReturn(testCustomer);

        mockMvc.perform(get("/api/v1/forms/{formId}", "form-id-789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formId").value("form-id-789"))
                .andExpect(jsonPath("$.formType").value("WINDOWS"));

        verify(formService).getFormById("form-id-789");
    }

    // ========== Get All Forms Tests ==========

    @Test
    void getAllForms_ReturnsFormsList() throws Exception {
        List<FormResponseModel> forms = Arrays.asList(testFormResponse);
        when(userService.getUserByAuth0Id(anyString())).thenReturn(testSalesperson);
        when(formService.getFormsCreatedBy(anyString())).thenReturn(forms);

        mockMvc.perform(get("/api/v1/forms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].formId").value("form-id-789"));

        verify(formService).getFormsCreatedBy(anyString());
    }

    @Test
    void getAllForms_WithProjectFilter_ReturnsFilteredForms() throws Exception {
        List<FormResponseModel> forms = Arrays.asList(testFormResponse);
        when(formService.getFormsByProject("project-123")).thenReturn(forms);

        mockMvc.perform(get("/api/v1/forms")
                        .param("projectId", "project-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectIdentifier").value("project-123"));

        verify(formService).getFormsByProject("project-123");
    }

    @Test
    void getAllForms_WithCustomerFilter_ReturnsFilteredForms() throws Exception {
        List<FormResponseModel> forms = Arrays.asList(testFormResponse);
        when(formService.getFormsByCustomer(CUSTOMER_UUID)).thenReturn(forms);

        mockMvc.perform(get("/api/v1/forms")
                        .param("customerId", CUSTOMER_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(CUSTOMER_UUID));

        verify(formService).getFormsByCustomer(CUSTOMER_UUID);
    }

    @Test
    void getAllForms_WithStatusFilter_ReturnsFilteredForms() throws Exception {
        List<FormResponseModel> forms = Arrays.asList(testFormResponse);
        when(formService.getFormsByStatus(FormStatus.ASSIGNED)).thenReturn(forms);

        mockMvc.perform(get("/api/v1/forms")
                        .param("status", "ASSIGNED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].formStatus").value("ASSIGNED"));

        verify(formService).getFormsByStatus(FormStatus.ASSIGNED);
    }

    // ========== Get Forms By Project Tests ==========

    @Test
    void getFormsByProject_ReturnsProjectForms() throws Exception {
        List<FormResponseModel> forms = Arrays.asList(testFormResponse);
        when(formService.getFormsByProject("project-123")).thenReturn(forms);

        mockMvc.perform(get("/api/v1/forms/project/{projectId}", "project-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectIdentifier").value("project-123"));

        verify(formService).getFormsByProject("project-123");
    }

    // ========== Get Forms By Customer Tests ==========

    @Test
    void getFormsByCustomer_ReturnsCustomerForms() throws Exception {
        List<FormResponseModel> forms = Arrays.asList(testFormResponse);
        when(formService.getFormsByCustomer(CUSTOMER_UUID)).thenReturn(forms);
        when(userService.getUserByAuth0Id(anyString())).thenReturn(testSalesperson);

        mockMvc.perform(get("/api/v1/forms/customer/{customerId}", CUSTOMER_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(CUSTOMER_UUID));

        verify(formService).getFormsByCustomer(CUSTOMER_UUID);
    }

    // ========== Get My Forms Tests ==========

    @Test
    void getMyForms_ReturnsCurrentUserForms() throws Exception {
        List<FormResponseModel> forms = Arrays.asList(testFormResponse);
        when(userService.getUserByAuth0Id(anyString())).thenReturn(testCustomer);
        when(formService.getFormsByCustomer(CUSTOMER_UUID)).thenReturn(forms);

        mockMvc.perform(get("/api/v1/forms/my-forms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(CUSTOMER_UUID));

        verify(formService).getFormsByCustomer(CUSTOMER_UUID);
    }

    // ========== Update Form Data Tests ==========

    @Test
    void updateFormData_WithValidData_UpdatesForm() throws Exception {
        Map<String, Object> formData = new HashMap<>();
        formData.put("color", "Blue");

        FormDataUpdateRequestModel updateRequest = FormDataUpdateRequestModel.builder()
                .formData(formData)
                .isSubmitting(false)
                .build();

        when(userService.getUserByAuth0Id(anyString())).thenReturn(testCustomer);
        when(formService.getFormById("form-id-789")).thenReturn(testFormResponse);
        when(formService.updateFormData(eq("form-id-789"), any(FormDataUpdateRequestModel.class), anyString()))
                .thenReturn(testFormResponse);

        mockMvc.perform(put("/api/v1/forms/{formId}/data", "form-id-789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formId").value("form-id-789"));

        verify(formService).updateFormData(eq("form-id-789"), any(FormDataUpdateRequestModel.class), anyString());
    }

    // ========== Submit Form Tests ==========

    @Test
    void submitForm_WithValidData_SubmitsSuccessfully() throws Exception {
        FormDataUpdateRequestModel submitRequest = FormDataUpdateRequestModel.builder()
                .formData(new HashMap<>())
                .submissionNotes("Completed")
                .isSubmitting(true)
                .build();

        when(userService.getUserByAuth0Id(anyString())).thenReturn(testCustomer);
        when(formService.submitForm(eq("form-id-789"), any(FormDataUpdateRequestModel.class), anyString()))
                .thenReturn(testFormResponse);

        mockMvc.perform(post("/api/v1/forms/{formId}/submit", "form-id-789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formId").value("form-id-789"));

        verify(formService).submitForm(eq("form-id-789"), any(FormDataUpdateRequestModel.class), anyString());
    }

    // ========== Reopen Form Tests ==========

    @Test
    void reopenForm_WithValidRequest_ReopensForm() throws Exception {
        FormReopenRequestModel reopenRequest = FormReopenRequestModel.builder()
                .reopenReason("Need changes")
                .newInstructions("Updated instructions")
                .build();

        when(formService.reopenForm(eq("form-id-789"), any(FormReopenRequestModel.class), anyString()))
                .thenReturn(testFormResponse);

        mockMvc.perform(post("/api/v1/forms/{formId}/reopen", "form-id-789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reopenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formId").value("form-id-789"));

        verify(formService).reopenForm(eq("form-id-789"), any(FormReopenRequestModel.class), anyString());
    }

    // ========== Complete Form Tests ==========

    @Test
    void completeForm_WithValidRequest_CompletesForm() throws Exception {
        when(formService.completeForm(eq("form-id-789"), anyString()))
                .thenReturn(testFormResponse);

        mockMvc.perform(post("/api/v1/forms/{formId}/complete", "form-id-789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formId").value("form-id-789"));

        verify(formService).completeForm(eq("form-id-789"), anyString());
    }

    // ========== Update Form Details Tests ==========

    @Test
    void updateFormDetails_WithValidRequest_UpdatesForm() throws Exception {
        when(formService.updateFormDetails(eq("form-id-789"), any(FormRequestModel.class), anyString()))
                .thenReturn(testFormResponse);

        mockMvc.perform(put("/api/v1/forms/{formId}", "form-id-789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testFormRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formId").value("form-id-789"));

        verify(formService).updateFormDetails(eq("form-id-789"), any(FormRequestModel.class), anyString());
    }

    // ========== Delete Form Tests ==========

    @Test
    void deleteForm_WithValidId_DeletesSuccessfully() throws Exception {
        doNothing().when(formService).deleteForm(eq("form-id-789"), anyString());

        mockMvc.perform(delete("/api/v1/forms/{formId}", "form-id-789"))
                .andExpect(status().isNoContent());

        verify(formService).deleteForm(eq("form-id-789"), anyString());
    }

    // ========== Get Form History Tests ==========

    @Test
    void getFormHistory_WithValidId_ReturnsHistory() throws Exception {
        FormSubmissionHistoryResponseModel historyItem = FormSubmissionHistoryResponseModel.builder()
                .id(1L)
                .formIdentifier("form-id-789")
                .submissionNumber(1)
                .build();
        List<FormSubmissionHistoryResponseModel> history = Arrays.asList(historyItem);

        when(formService.getFormById("form-id-789")).thenReturn(testFormResponse);
        when(userService.getUserByAuth0Id(anyString())).thenReturn(testSalesperson);
        when(formService.getFormSubmissionHistory("form-id-789")).thenReturn(history);

        mockMvc.perform(get("/api/v1/forms/{formId}/history", "form-id-789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].submissionNumber").value(1));

        verify(formService).getFormSubmissionHistory("form-id-789");
    }

    // ========== Authorization Tests ==========

    @Test
    void getFormById_WhenCustomerAccessesOtherCustomerForm_ReturnsForbidden() throws Exception {
        // Customer trying to access another customer's form
        UserResponseModel otherCustomer = new UserResponseModel();
        otherCustomer.setUserIdentifier("different-customer-id");
        otherCustomer.setUserRole(UserRole.CUSTOMER);

        when(formService.getFormById("form-id-789")).thenReturn(testFormResponse);
        when(userService.getUserByAuth0Id(anyString())).thenReturn(otherCustomer);

        mockMvc.perform(get("/api/v1/forms/{formId}", "form-id-789"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getFormsByCustomer_WhenCustomerAccessesOtherCustomerId_ReturnsForbidden() throws Exception {
        UserResponseModel currentCustomer = new UserResponseModel();
        currentCustomer.setUserIdentifier(CUSTOMER_UUID);
        currentCustomer.setUserRole(UserRole.CUSTOMER);

        when(userService.getUserByAuth0Id(anyString())).thenReturn(currentCustomer);

        // Customer trying to access different customer ID
        mockMvc.perform(get("/api/v1/forms/customer/{customerId}", "different-customer-id"))
                .andExpect(status().isForbidden());

        verify(formService, never()).getFormsByCustomer(anyString());
    }

    @Test
    void getFormsByCustomer_WhenSalespersonAccessesAnyCustomer_ReturnsOk() throws Exception {
        List<FormResponseModel> forms = Arrays.asList(testFormResponse);
        
        when(userService.getUserByAuth0Id(anyString())).thenReturn(testSalesperson);
        when(formService.getFormsByCustomer("any-customer-id")).thenReturn(forms);

        mockMvc.perform(get("/api/v1/forms/customer/{customerId}", "any-customer-id"))
                .andExpect(status().isOk());

        verify(formService).getFormsByCustomer("any-customer-id");
    }

    @Test
    void updateFormData_WhenCustomerUpdatesOwnForm_ReturnsOk() throws Exception {
        Map<String, Object> formData = new HashMap<>();
        formData.put("color", "Blue");

        FormDataUpdateRequestModel updateRequest = FormDataUpdateRequestModel.builder()
                .formData(formData)
                .isSubmitting(false)
                .build();

        when(userService.getUserByAuth0Id(anyString())).thenReturn(testCustomer);
        when(formService.getFormById("form-id-789")).thenReturn(testFormResponse);
        when(formService.updateFormData(eq("form-id-789"), any(FormDataUpdateRequestModel.class), anyString()))
                .thenReturn(testFormResponse);

        mockMvc.perform(put("/api/v1/forms/{formId}/data", "form-id-789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(formService).updateFormData(eq("form-id-789"), any(FormDataUpdateRequestModel.class), anyString());
    }

    @Test
    void updateFormData_WhenCustomerUpdatesOtherCustomerForm_ReturnsForbidden() throws Exception {
        Map<String, Object> formData = new HashMap<>();
        formData.put("color", "Blue");

        FormDataUpdateRequestModel updateRequest = FormDataUpdateRequestModel.builder()
                .formData(formData)
                .isSubmitting(false)
                .build();

        UserResponseModel differentCustomer = new UserResponseModel();
        differentCustomer.setUserIdentifier("different-customer-id");
        differentCustomer.setUserRole(UserRole.CUSTOMER);

        when(userService.getUserByAuth0Id(anyString())).thenReturn(differentCustomer);
        when(formService.getFormById("form-id-789")).thenReturn(testFormResponse);

        mockMvc.perform(put("/api/v1/forms/{formId}/data", "form-id-789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        verify(formService, never()).updateFormData(any(), any(), any());
    }

    @Test
    void updateFormData_WhenSalespersonUpdatesForm_ReturnsOk() throws Exception {
        Map<String, Object> formData = new HashMap<>();
        formData.put("color", "Red");

        FormDataUpdateRequestModel updateRequest = FormDataUpdateRequestModel.builder()
                .formData(formData)
                .isSubmitting(false)
                .build();

        when(userService.getUserByAuth0Id(anyString())).thenReturn(testSalesperson);
        when(formService.getFormById("form-id-789")).thenReturn(testFormResponse);
        when(formService.updateFormData(eq("form-id-789"), any(FormDataUpdateRequestModel.class), anyString()))
                .thenReturn(testFormResponse);

        mockMvc.perform(put("/api/v1/forms/{formId}/data", "form-id-789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(formService).updateFormData(eq("form-id-789"), any(FormDataUpdateRequestModel.class), anyString());
    }

    @Test
    void getFormHistory_WhenCustomerAccessesOwnForm_ReturnsOk() throws Exception {
        FormSubmissionHistoryResponseModel historyItem = FormSubmissionHistoryResponseModel.builder()
                .id(1L)
                .formIdentifier("form-id-789")
                .submissionNumber(1)
                .build();
        List<FormSubmissionHistoryResponseModel> history = Arrays.asList(historyItem);

        when(formService.getFormById("form-id-789")).thenReturn(testFormResponse);
        when(userService.getUserByAuth0Id(anyString())).thenReturn(testCustomer);
        when(formService.getFormSubmissionHistory("form-id-789")).thenReturn(history);

        mockMvc.perform(get("/api/v1/forms/{formId}/history", "form-id-789"))
                .andExpect(status().isOk());

        verify(formService).getFormSubmissionHistory("form-id-789");
    }

    @Test
    void getFormHistory_WhenCustomerAccessesOtherCustomerForm_ReturnsForbidden() throws Exception {
        UserResponseModel differentCustomer = new UserResponseModel();
        differentCustomer.setUserIdentifier("different-customer-id");
        differentCustomer.setUserRole(UserRole.CUSTOMER);

        when(formService.getFormById("form-id-789")).thenReturn(testFormResponse);
        when(userService.getUserByAuth0Id(anyString())).thenReturn(differentCustomer);

        mockMvc.perform(get("/api/v1/forms/{formId}/history", "form-id-789"))
                .andExpect(status().isForbidden());

        verify(formService, never()).getFormSubmissionHistory(anyString());
    }

    // ========== Filter Combination Tests ==========

    @Test
    void getAllForms_WithStatusAndFormTypeFilters_ReturnsBothFiltered() throws Exception {
        FormResponseModel form1 = FormResponseModel.builder()
                .formId("form-1")
                .formType(FormType.WINDOWS)
                .formStatus(FormStatus.ASSIGNED)
                .build();
        
        FormResponseModel form2 = FormResponseModel.builder()
                .formId("form-2")
                .formType(FormType.WINDOWS)
                .formStatus(FormStatus.SUBMITTED)
                .build();

        List<FormResponseModel> allForms = Arrays.asList(form1, form2);
        
        when(userService.getUserByAuth0Id(anyString())).thenReturn(testSalesperson);
        when(formService.getFormsCreatedBy(anyString())).thenReturn(allForms);

        mockMvc.perform(get("/api/v1/forms")
                        .param("status", "ASSIGNED")
                        .param("formType", "WINDOWS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].formId").value("form-1"));
    }

    @Test
    void getAllForms_AsCustomer_ReturnsOnlyOwnForms() throws Exception {
        List<FormResponseModel> customerForms = Arrays.asList(testFormResponse);
        
        when(userService.getUserByAuth0Id(anyString())).thenReturn(testCustomer);
        when(formService.getFormsByCustomer(CUSTOMER_UUID)).thenReturn(customerForms);

        mockMvc.perform(get("/api/v1/forms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(CUSTOMER_UUID));

        verify(formService).getFormsByCustomer(CUSTOMER_UUID);
        verify(formService, never()).getFormsCreatedBy(anyString());
    }

    @Test
    void getAllForms_WithProjectIdFilter_AsSalesperson_ReturnsProjectForms() throws Exception {
        List<FormResponseModel> projectForms = Arrays.asList(testFormResponse);
        
        when(formService.getFormsByProject("project-123")).thenReturn(projectForms);

        mockMvc.perform(get("/api/v1/forms")
                        .param("projectId", "project-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectIdentifier").value("project-123"));

        verify(formService).getFormsByProject("project-123");
    }

    @Test
    void getAllForms_WithCustomerIdFilter_ReturnsCustomerForms() throws Exception {
        List<FormResponseModel> customerForms = Arrays.asList(testFormResponse);
        
        when(formService.getFormsByCustomer(CUSTOMER_UUID)).thenReturn(customerForms);

        mockMvc.perform(get("/api/v1/forms")
                        .param("customerId", CUSTOMER_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(CUSTOMER_UUID));

        verify(formService).getFormsByCustomer(CUSTOMER_UUID);
    }

    @Test
    void getAllForms_WithStatusFilter_ReturnsStatusFilteredForms() throws Exception {
        List<FormResponseModel> statusForms = Arrays.asList(testFormResponse);
        
        when(formService.getFormsByStatus(FormStatus.SUBMITTED)).thenReturn(statusForms);

        mockMvc.perform(get("/api/v1/forms")
                        .param("status", "SUBMITTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].formStatus").value("ASSIGNED")); // Note: the forms need secondary filtering

        verify(formService).getFormsByStatus(FormStatus.SUBMITTED);
    }

    @Test
    void getAllForms_AsOwner_ReturnsFormsByCreator() throws Exception {
        UserResponseModel owner = new UserResponseModel();
        owner.setUserIdentifier("owner-id-123");
        owner.setUserRole(UserRole.OWNER);

        List<FormResponseModel> ownerForms = Arrays.asList(testFormResponse);
        
        when(userService.getUserByAuth0Id(anyString())).thenReturn(owner);
        when(formService.getFormsCreatedBy("owner-id-123")).thenReturn(ownerForms);

        mockMvc.perform(get("/api/v1/forms"))
                .andExpect(status().isOk());

        verify(formService).getFormsCreatedBy("owner-id-123");
    }

    @Test
    void getAllForms_WithFormTypeFilter_ReturnsOnlyMatchingType() throws Exception {
        FormResponseModel windowsForm = FormResponseModel.builder()
                .formId("form-1")
                .formType(FormType.WINDOWS)
                .formStatus(FormStatus.ASSIGNED)
                .build();
        
        FormResponseModel paintForm = FormResponseModel.builder()
                .formId("form-2")
                .formType(FormType.PAINT)
                .formStatus(FormStatus.ASSIGNED)
                .build();

        List<FormResponseModel> allForms = Arrays.asList(windowsForm, paintForm);
        
        when(userService.getUserByAuth0Id(anyString())).thenReturn(testSalesperson);
        when(formService.getFormsCreatedBy(anyString())).thenReturn(allForms);

        mockMvc.perform(get("/api/v1/forms")
                        .param("formType", "WINDOWS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].formId").value("form-1"));
    }

    // ========== Update Form Data with Submission Tests ==========

    @Test
    void updateFormData_WhenIsSubmittingTrue_CallsSubmitForm() throws Exception {
        Map<String, Object> formData = new HashMap<>();
        formData.put("color", "Green");

        FormDataUpdateRequestModel updateRequest = FormDataUpdateRequestModel.builder()
                .formData(formData)
                .isSubmitting(true)
                .submissionNotes("Final submission")
                .build();

        when(userService.getUserByAuth0Id(anyString())).thenReturn(testCustomer);
        when(formService.getFormById("form-id-789")).thenReturn(testFormResponse);
        when(formService.submitForm(eq("form-id-789"), any(FormDataUpdateRequestModel.class), anyString()))
                .thenReturn(testFormResponse);

        mockMvc.perform(put("/api/v1/forms/{formId}/data", "form-id-789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(formService).submitForm(eq("form-id-789"), any(FormDataUpdateRequestModel.class), anyString());
        verify(formService, never()).updateFormData(any(), any(), any());
    }

    // ========== Additional Edge Case Tests ==========

    @Test
    void getFormsByCustomer_WhenCustomerAccessesOwnId_ReturnsOk() throws Exception {
        List<FormResponseModel> forms = Arrays.asList(testFormResponse);
        
        when(userService.getUserByAuth0Id(anyString())).thenReturn(testCustomer);
        when(formService.getFormsByCustomer(CUSTOMER_UUID)).thenReturn(forms);

        mockMvc.perform(get("/api/v1/forms/customer/{customerId}", CUSTOMER_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(CUSTOMER_UUID));

        verify(formService).getFormsByCustomer(CUSTOMER_UUID);
    }

    @Test
    void createForm_WithAllRequiredFields_ReturnsCreated() throws Exception {
        when(formService.createAndAssignForm(any(FormRequestModel.class), anyString()))
                .thenReturn(testFormResponse);

        mockMvc.perform(post("/api/v1/forms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testFormRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.formId").exists())
                .andExpect(jsonPath("$.formType").value("WINDOWS"));

        verify(formService).createAndAssignForm(any(FormRequestModel.class), anyString());
    }

    @Test
    void getMyForms_AsCustomer_ReturnsOwnForms() throws Exception {
        List<FormResponseModel> forms = Arrays.asList(testFormResponse);
        
        when(userService.getUserByAuth0Id(anyString())).thenReturn(testCustomer);
        when(formService.getFormsByCustomer(CUSTOMER_UUID)).thenReturn(forms);

        mockMvc.perform(get("/api/v1/forms/my-forms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(CUSTOMER_UUID));

        verify(formService).getFormsByCustomer(CUSTOMER_UUID);
        verify(userService).getUserByAuth0Id(anyString());
    }

    @Test
    void submitForm_AsCustomer_SubmitsSuccessfully() throws Exception {
        FormDataUpdateRequestModel submitRequest = FormDataUpdateRequestModel.builder()
                .formData(new HashMap<>())
                .submissionNotes("All done")
                .isSubmitting(true)
                .build();

        testFormResponse.setFormStatus(FormStatus.SUBMITTED);
        
        when(userService.getUserByAuth0Id(anyString())).thenReturn(testCustomer);
        when(formService.submitForm(eq("form-id-789"), any(FormDataUpdateRequestModel.class), anyString()))
                .thenReturn(testFormResponse);

        mockMvc.perform(post("/api/v1/forms/{formId}/submit", "form-id-789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formId").value("form-id-789"));

        verify(formService).submitForm(eq("form-id-789"), any(FormDataUpdateRequestModel.class), eq(CUSTOMER_UUID));
    }

    @Test
    void reopenForm_AsSalesperson_ReopensSuccessfully() throws Exception {
        FormReopenRequestModel reopenRequest = FormReopenRequestModel.builder()
                .reopenReason("Customer needs to change selections")
                .newInstructions("Please review section 3")
                .build();

        testFormResponse.setFormStatus(FormStatus.REOPENED);
        
        when(formService.reopenForm(eq("form-id-789"), any(FormReopenRequestModel.class), anyString()))
                .thenReturn(testFormResponse);

        mockMvc.perform(post("/api/v1/forms/{formId}/reopen", "form-id-789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reopenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formId").value("form-id-789"));

        verify(formService).reopenForm(eq("form-id-789"), any(FormReopenRequestModel.class), anyString());
    }

    @Test
    void completeForm_AsSalesperson_CompletesSuccessfully() throws Exception {
        testFormResponse.setFormStatus(FormStatus.COMPLETED);
        
        when(formService.completeForm(eq("form-id-789"), anyString()))
                .thenReturn(testFormResponse);

        mockMvc.perform(post("/api/v1/forms/{formId}/complete", "form-id-789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formId").value("form-id-789"));

        verify(formService).completeForm(eq("form-id-789"), anyString());
    }

    @Test
    void updateFormDetails_AsSalesperson_UpdatesSuccessfully() throws Exception {
        when(formService.updateFormDetails(eq("form-id-789"), any(FormRequestModel.class), anyString()))
                .thenReturn(testFormResponse);

        mockMvc.perform(put("/api/v1/forms/{formId}", "form-id-789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testFormRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formId").value("form-id-789"));

        verify(formService).updateFormDetails(eq("form-id-789"), any(FormRequestModel.class), anyString());
    }

    @Test
    void deleteForm_AsSalesperson_DeletesSuccessfully() throws Exception {
        doNothing().when(formService).deleteForm(eq("form-id-789"), anyString());

        mockMvc.perform(delete("/api/v1/forms/{formId}", "form-id-789"))
                .andExpect(status().isNoContent());

        verify(formService).deleteForm(eq("form-id-789"), anyString());
    }

    // ========== Lambda and Filter Tests ==========

    @Test
    void getAllForms_WithStatusFilter_FiltersCorrectly_ExecutesLambda() throws Exception {
        FormResponseModel assignedForm = FormResponseModel.builder()
                .formId("form-1")
                .formType(FormType.WINDOWS)
                .formStatus(FormStatus.ASSIGNED)
                .build();
        
        FormResponseModel submittedForm = FormResponseModel.builder()
                .formId("form-2")
                .formType(FormType.WINDOWS)
                .formStatus(FormStatus.SUBMITTED)
                .build();

        FormResponseModel inProgressForm = FormResponseModel.builder()
                .formId("form-3")
                .formType(FormType.PAINT)
                .formStatus(FormStatus.IN_PROGRESS)
                .build();

        List<FormResponseModel> allForms = Arrays.asList(assignedForm, submittedForm, inProgressForm);
        
        when(userService.getUserByAuth0Id(anyString())).thenReturn(testSalesperson);
        when(formService.getFormsCreatedBy(anyString())).thenReturn(allForms);

        mockMvc.perform(get("/api/v1/forms")
                        .param("status", "ASSIGNED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].formId").value("form-1"))
                .andExpect(jsonPath("$[0].formStatus").value("ASSIGNED"));
    }

    @Test
    void getAllForms_WithFormTypeFilter_FiltersCorrectly_ExecutesLambda() throws Exception {
        FormResponseModel windowsForm = FormResponseModel.builder()
                .formId("form-1")
                .formType(FormType.WINDOWS)
                .formStatus(FormStatus.ASSIGNED)
                .build();
        
        FormResponseModel paintForm = FormResponseModel.builder()
                .formId("form-2")
                .formType(FormType.PAINT)
                .formStatus(FormStatus.ASSIGNED)
                .build();

        FormResponseModel doorsForm = FormResponseModel.builder()
                .formId("form-3")
                .formType(FormType.EXTERIOR_DOORS)
                .formStatus(FormStatus.ASSIGNED)
                .build();

        List<FormResponseModel> allForms = Arrays.asList(windowsForm, paintForm, doorsForm);
        
        when(userService.getUserByAuth0Id(anyString())).thenReturn(testSalesperson);
        when(formService.getFormsCreatedBy(anyString())).thenReturn(allForms);

        mockMvc.perform(get("/api/v1/forms")
                        .param("formType", "PAINT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].formId").value("form-2"))
                .andExpect(jsonPath("$[0].formType").value("PAINT"));
    }

    @Test
    void getAllForms_WithBothStatusAndFormTypeFilters_FiltersBoth_ExecutesLambdas() throws Exception {
        FormResponseModel match = FormResponseModel.builder()
                .formId("form-match")
                .formType(FormType.WINDOWS)
                .formStatus(FormStatus.ASSIGNED)
                .build();
        
        FormResponseModel wrongType = FormResponseModel.builder()
                .formId("form-wrongtype")
                .formType(FormType.PAINT)
                .formStatus(FormStatus.ASSIGNED)
                .build();

        FormResponseModel wrongStatus = FormResponseModel.builder()
                .formId("form-wrongstatus")
                .formType(FormType.WINDOWS)
                .formStatus(FormStatus.SUBMITTED)
                .build();

        FormResponseModel wrongBoth = FormResponseModel.builder()
                .formId("form-wrongboth")
                .formType(FormType.PAINT)
                .formStatus(FormStatus.COMPLETED)
                .build();

        List<FormResponseModel> allForms = Arrays.asList(match, wrongType, wrongStatus, wrongBoth);
        
        when(userService.getUserByAuth0Id(anyString())).thenReturn(testSalesperson);
        when(formService.getFormsCreatedBy(anyString())).thenReturn(allForms);

        mockMvc.perform(get("/api/v1/forms")
                        .param("status", "ASSIGNED")
                        .param("formType", "WINDOWS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].formId").value("form-match"));
    }

    @Test
    void getAllForms_WithStatusFilter_OnProjectForms_FiltersProjectResults() throws Exception {
        FormResponseModel assignedForm = FormResponseModel.builder()
                .formId("form-1")
                .formType(FormType.WINDOWS)
                .formStatus(FormStatus.ASSIGNED)
                .projectIdentifier("project-123")
                .build();
        
        FormResponseModel submittedForm = FormResponseModel.builder()
                .formId("form-2")
                .formType(FormType.WINDOWS)
                .formStatus(FormStatus.SUBMITTED)
                .projectIdentifier("project-123")
                .build();

        List<FormResponseModel> projectForms = Arrays.asList(assignedForm, submittedForm);
        
        when(formService.getFormsByProject("project-123")).thenReturn(projectForms);

        mockMvc.perform(get("/api/v1/forms")
                        .param("projectId", "project-123")
                        .param("status", "ASSIGNED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].formId").value("form-1"));
    }

    @Test
    void getAllForms_WithFormTypeFilter_OnCustomerForms_FiltersCustomerResults() throws Exception {
        FormResponseModel windowsForm = FormResponseModel.builder()
                .formId("form-1")
                .formType(FormType.WINDOWS)
                .formStatus(FormStatus.ASSIGNED)
                .customerId(CUSTOMER_UUID)
                .build();
        
        FormResponseModel paintForm = FormResponseModel.builder()
                .formId("form-2")
                .formType(FormType.PAINT)
                .formStatus(FormStatus.ASSIGNED)
                .customerId(CUSTOMER_UUID)
                .build();

        List<FormResponseModel> customerForms = Arrays.asList(windowsForm, paintForm);
        
        when(formService.getFormsByCustomer(CUSTOMER_UUID)).thenReturn(customerForms);

        mockMvc.perform(get("/api/v1/forms")
                        .param("customerId", CUSTOMER_UUID)
                        .param("formType", "WINDOWS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].formId").value("form-1"));
    }

    @Test
    void getAllForms_WithStatusFilter_OnStatusForms_FiltersStatusResults() throws Exception {
        FormResponseModel assignedWindows = FormResponseModel.builder()
                .formId("form-1")
                .formType(FormType.WINDOWS)
                .formStatus(FormStatus.ASSIGNED)
                .build();
        
        FormResponseModel assignedPaint = FormResponseModel.builder()
                .formId("form-2")
                .formType(FormType.PAINT)
                .formStatus(FormStatus.ASSIGNED)
                .build();

        List<FormResponseModel> statusForms = Arrays.asList(assignedWindows, assignedPaint);
        
        when(formService.getFormsByStatus(FormStatus.ASSIGNED)).thenReturn(statusForms);

        mockMvc.perform(get("/api/v1/forms")
                        .param("status", "ASSIGNED")
                        .param("formType", "PAINT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].formId").value("form-2"));
    }

    @Test
    void getAllForms_AsCustomerWithStatusFilter_FiltersOwnForms() throws Exception {
        FormResponseModel assignedForm = FormResponseModel.builder()
                .formId("form-1")
                .formType(FormType.WINDOWS)
                .formStatus(FormStatus.ASSIGNED)
                .customerId(CUSTOMER_UUID)
                .build();
        
        FormResponseModel submittedForm = FormResponseModel.builder()
                .formId("form-2")
                .formType(FormType.PAINT)
                .formStatus(FormStatus.SUBMITTED)
                .customerId(CUSTOMER_UUID)
                .build();

        List<FormResponseModel> customerForms = Arrays.asList(assignedForm, submittedForm);
        
        when(userService.getUserByAuth0Id(anyString())).thenReturn(testCustomer);
        when(formService.getFormsByCustomer(CUSTOMER_UUID)).thenReturn(customerForms);

        mockMvc.perform(get("/api/v1/forms")
                        .param("status", "SUBMITTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].formId").value("form-2"))
                .andExpect(jsonPath("$[0].formStatus").value("SUBMITTED"));
    }

    @Test
    void getAllForms_AsCustomerWithFormTypeFilter_FiltersOwnForms() throws Exception {
        FormResponseModel windowsForm = FormResponseModel.builder()
                .formId("form-1")
                .formType(FormType.WINDOWS)
                .formStatus(FormStatus.ASSIGNED)
                .customerId(CUSTOMER_UUID)
                .build();
        
        FormResponseModel paintForm = FormResponseModel.builder()
                .formId("form-2")
                .formType(FormType.PAINT)
                .formStatus(FormStatus.ASSIGNED)
                .customerId(CUSTOMER_UUID)
                .build();

        List<FormResponseModel> customerForms = Arrays.asList(windowsForm, paintForm);
        
        when(userService.getUserByAuth0Id(anyString())).thenReturn(testCustomer);
        when(formService.getFormsByCustomer(CUSTOMER_UUID)).thenReturn(customerForms);

        mockMvc.perform(get("/api/v1/forms")
                        .param("formType", "PAINT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].formId").value("form-2"))
                .andExpect(jsonPath("$[0].formType").value("PAINT"));
    }

    @Test
    void getAllForms_MultipleFilters_NoMatches_ReturnsEmptyList() throws Exception {
        FormResponseModel form1 = FormResponseModel.builder()
                .formId("form-1")
                .formType(FormType.WINDOWS)
                .formStatus(FormStatus.ASSIGNED)
                .build();
        
        FormResponseModel form2 = FormResponseModel.builder()
                .formId("form-2")
                .formType(FormType.PAINT)
                .formStatus(FormStatus.SUBMITTED)
                .build();

        List<FormResponseModel> allForms = Arrays.asList(form1, form2);
        
        when(userService.getUserByAuth0Id(anyString())).thenReturn(testSalesperson);
        when(formService.getFormsCreatedBy(anyString())).thenReturn(allForms);

        // Filter for COMPLETED status but none exist
        mockMvc.perform(get("/api/v1/forms")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}


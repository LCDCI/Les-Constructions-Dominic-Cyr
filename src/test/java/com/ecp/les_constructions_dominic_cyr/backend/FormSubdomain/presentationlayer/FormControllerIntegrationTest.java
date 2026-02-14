package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.*;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer.FormDataUpdateRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer.FormReopenRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer.FormRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectStatus;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.*;
import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Auth0ManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for FormController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersPostgresConfig.class)
class FormControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private FormSubmissionHistoryRepository historyRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private Auth0ManagementService auth0ManagementService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final String BASE_URI = "/api/v1/forms";
    private final SimpleGrantedAuthority ROLE_OWNER = new SimpleGrantedAuthority("ROLE_OWNER");
    private final SimpleGrantedAuthority ROLE_SALESPERSON = new SimpleGrantedAuthority("ROLE_SALESPERSON");
    private final SimpleGrantedAuthority ROLE_CUSTOMER = new SimpleGrantedAuthority("ROLE_CUSTOMER");

    private Users testCustomer;
    private Users testSalesperson;
    private Project testProject;
    private Lot testLot;

    @BeforeEach
    void setUp() {
        // Clean up test data
        historyRepository.deleteAll();
        formRepository.deleteAll();
        lotRepository.deleteAll();
        projectRepository.deleteAll();
        // Note: We don't delete users as they might be needed for auth

        // Create test salesperson
        testSalesperson = new Users();
        testSalesperson.setUserIdentifier(UserIdentifier.newId());
        testSalesperson.setAuth0UserId("auth0|salesperson-test");
        testSalesperson.setFirstName("Test");
        testSalesperson.setLastName("Salesperson");
        testSalesperson.setPrimaryEmail("salesperson.test@example.com");
        testSalesperson.setUserRole(UserRole.SALESPERSON);
        testSalesperson.setUserStatus(UserStatus.ACTIVE);
        testSalesperson = usersRepository.save(testSalesperson);

        // Create test customer
        testCustomer = new Users();
        testCustomer.setUserIdentifier(UserIdentifier.newId());
        testCustomer.setAuth0UserId("auth0|customer-test");
        testCustomer.setFirstName("Test");
        testCustomer.setLastName("Customer");
        testCustomer.setPrimaryEmail("customer.test@example.com");
        testCustomer.setUserRole(UserRole.CUSTOMER);
        testCustomer.setUserStatus(UserStatus.ACTIVE);
        testCustomer = usersRepository.save(testCustomer);

        // Create test project
        testProject = new Project();
        testProject.setProjectIdentifier("test-project-" + UUID.randomUUID().toString().substring(0, 8));
        testProject.setProjectName("Test Project");
        testProject.setProjectDescription("Test Description");
        testProject.setStatus(ProjectStatus.IN_PROGRESS);
        testProject.setStartDate(LocalDate.now());
        testProject.setPrimaryColor("#000000");
        testProject.setTertiaryColor("#CCCCCC");
        testProject.setBuyerColor("#FFFFFF");
        testProject.setImageIdentifier("test-image");
        testProject = projectRepository.save(testProject);

        // Create test lot
        testLot = new Lot();
        testLot.setLotIdentifier(new LotIdentifier(UUID.randomUUID().toString()));
        testLot.setLotNumber("LOT-001");
        testLot.setCivicAddress("123 Test Street");
        testLot.setPrice(250000f);
        testLot.setDimensionsSquareFeet("2000");
        testLot.setDimensionsSquareMeters("185.8");
        testLot.setLotStatus(LotStatus.AVAILABLE);
        testLot.setProject(testProject);
        testLot.setAssignedUsers(Arrays.asList(testCustomer, testSalesperson));
        testLot = lotRepository.save(testLot);
    }

    // ========== Create Form Tests ==========

    @Test
    void createForm_AsOwner_CreatesForm() throws Exception {
        // Arrange
        FormRequestModel requestModel = FormRequestModel.builder()
                .formType(FormType.WINDOWS)
                .projectIdentifier(testProject.getProjectIdentifier())
                .lotIdentifier(testLot.getLotIdentifier().getLotId().toString())
                .customerId(testCustomer.getUserIdentifier().getUserId().toString())
                .formTitle("Window Selection")
                .instructions("Please select all windows")
                .formData(new HashMap<>())
                .build();

        // Act & Assert
        mockMvc.perform(post(BASE_URI)
                        .with(jwt().authorities(ROLE_OWNER)
                                .jwt(builder -> builder.subject(testSalesperson.getAuth0UserId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestModel)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.formId").exists())
                .andExpect(jsonPath("$.formType").value("WINDOWS"))
                .andExpect(jsonPath("$.formStatus").value("ASSIGNED"))
                .andExpect(jsonPath("$.projectIdentifier").value(testProject.getProjectIdentifier()))
                .andExpect(jsonPath("$.customerId").value(testCustomer.getUserIdentifier().getUserId().toString()));

        assertEquals(1, formRepository.count());
    }

    @Test
    void createForm_AsSalesperson_CreatesForm() throws Exception {
        // Arrange
        FormRequestModel requestModel = FormRequestModel.builder()
                .formType(FormType.EXTERIOR_DOORS)
                .projectIdentifier(testProject.getProjectIdentifier())
                .lotIdentifier(testLot.getLotIdentifier().getLotId().toString())
                .customerId(testCustomer.getUserIdentifier().getUserId().toString())
                .formTitle("Door Selection")
                .build();

        // Act & Assert
        mockMvc.perform(post(BASE_URI)
                        .with(jwt().authorities(ROLE_SALESPERSON)
                                .jwt(builder -> builder.subject(testSalesperson.getAuth0UserId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestModel)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.formType").value("EXTERIOR_DOORS"));

        assertEquals(1, formRepository.count());
    }

    @Test
    void createForm_AsCustomer_ReturnsForbidden() throws Exception {
        // Arrange
        FormRequestModel requestModel = FormRequestModel.builder()
                .formType(FormType.PAINT)
                .projectIdentifier(testProject.getProjectIdentifier())
                .lotIdentifier(testLot.getLotIdentifier().getLotId().toString())
                .customerId(testCustomer.getUserIdentifier().getUserId().toString())
                .build();

        // Act & Assert
        mockMvc.perform(post(BASE_URI)
                        .with(jwt().authorities(ROLE_CUSTOMER)
                                .jwt(builder -> builder.subject(testCustomer.getAuth0UserId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestModel)))
                .andExpect(status().isForbidden());

        assertEquals(0, formRepository.count());
    }

    // ========== Get Form By ID Tests ==========

    @Test
    void getFormById_AsFormOwner_ReturnsForm() throws Exception {
        // Arrange
        Form form = createTestForm();

        // Act & Assert
        mockMvc.perform(get(BASE_URI + "/{formId}", form.getFormIdentifier().getFormId())
                        .with(jwt().authorities(ROLE_CUSTOMER)
                                .jwt(builder -> builder.subject(testCustomer.getAuth0UserId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formId").value(form.getFormIdentifier().getFormId()))
                .andExpect(jsonPath("$.formType").value("WINDOWS"));
    }

    @Test
    void getFormById_AsOwner_ReturnsForm() throws Exception {
        // Arrange
        Form form = createTestForm();

        // Act & Assert
        mockMvc.perform(get(BASE_URI + "/{formId}", form.getFormIdentifier().getFormId())
                        .with(jwt().authorities(ROLE_OWNER)
                                .jwt(builder -> builder.subject(testSalesperson.getAuth0UserId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formId").value(form.getFormIdentifier().getFormId()));
    }

    @Test
    void getFormById_AsUnauthorizedCustomer_ReturnsForbidden() throws Exception {
        // Arrange
        Form form = createTestForm();
        Users differentCustomer = createDifferentCustomer();

        // Act & Assert
        mockMvc.perform(get(BASE_URI + "/{formId}", form.getFormIdentifier().getFormId())
                        .with(jwt().authorities(ROLE_CUSTOMER)
                                .jwt(builder -> builder.subject(differentCustomer.getAuth0UserId()))))
                .andExpect(status().isForbidden());
    }

    // ========== Get All Forms Tests ==========

    @Test
    void getAllForms_AsCustomer_ReturnsOwnForms() throws Exception {
        // Arrange
        createTestForm();
        Form differentCustomerForm = createFormForDifferentCustomer();

        // Act & Assert
        mockMvc.perform(get(BASE_URI)
                        .with(jwt().authorities(ROLE_CUSTOMER)
                                .jwt(builder -> builder.subject(testCustomer.getAuth0UserId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerId").value(testCustomer.getUserIdentifier().getUserId().toString()));
    }

    @Test
    void getAllForms_AsOwner_CanFilterByProject() throws Exception {
        // Arrange
        createTestForm();

        // Act & Assert
        mockMvc.perform(get(BASE_URI)
                        .param("projectId", testProject.getProjectIdentifier())
                        .with(jwt().authorities(ROLE_OWNER)
                                .jwt(builder -> builder.subject(testSalesperson.getAuth0UserId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ========== Update Form Data Tests ==========

    @Test
    void updateFormData_AsFormOwner_UpdatesSuccessfully() throws Exception {
        // Arrange
        Form form = createTestForm();
        Map<String, Object> newData = new HashMap<>();
        newData.put("color", "Blue");

        FormDataUpdateRequestModel updateRequest = FormDataUpdateRequestModel.builder()
                .formData(newData)
                .isSubmitting(false)
                .build();

        // Act & Assert
        mockMvc.perform(put(BASE_URI + "/{formId}/data", form.getFormIdentifier().getFormId())
                        .with(jwt().authorities(ROLE_CUSTOMER)
                                .jwt(builder -> builder.subject(testCustomer.getAuth0UserId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formStatus").value("IN_PROGRESS"));

        Form updatedForm = formRepository.findByFormIdentifier_FormId(form.getFormIdentifier().getFormId()).orElseThrow();
        assertEquals(FormStatus.IN_PROGRESS, updatedForm.getFormStatus());
        assertEquals("Blue", updatedForm.getFormData().get("color"));
    }

    // ========== Submit Form Tests ==========

    @Test
    void submitForm_WithValidData_SubmitsSuccessfully() throws Exception {
        // Arrange
        Form form = createTestForm();
        FormDataUpdateRequestModel submitRequest = FormDataUpdateRequestModel.builder()
                .formData(new HashMap<>())
                .submissionNotes("All done")
                .isSubmitting(true)
                .build();

        // Act & Assert
        mockMvc.perform(post(BASE_URI + "/{formId}/submit", form.getFormIdentifier().getFormId())
                        .with(jwt().authorities(ROLE_CUSTOMER)
                                .jwt(builder -> builder.subject(testCustomer.getAuth0UserId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formStatus").value("SUBMITTED"));

        Form submittedForm = formRepository.findByFormIdentifier_FormId(form.getFormIdentifier().getFormId()).orElseThrow();
        assertEquals(FormStatus.SUBMITTED, submittedForm.getFormStatus());
        assertNotNull(submittedForm.getFirstSubmittedDate());
        assertEquals(1, historyRepository.countByFormIdentifier(form.getFormIdentifier().getFormId()));
    }

    // ========== Reopen Form Tests ==========

    @Test
    void reopenForm_AsOwner_ReopensSuccessfully() throws Exception {
        // Arrange
        Form form = createTestForm();
        form.setFormStatus(FormStatus.SUBMITTED);
        form = formRepository.save(form);

        FormReopenRequestModel reopenRequest = FormReopenRequestModel.builder()
                .reopenReason("Need changes")
                .newInstructions("Updated instructions")
                .build();

        // Act & Assert
        mockMvc.perform(post(BASE_URI + "/{formId}/reopen", form.getFormIdentifier().getFormId())
                        .with(jwt().authorities(ROLE_OWNER)
                                .jwt(builder -> builder.subject(testSalesperson.getAuth0UserId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reopenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formStatus").value("REOPENED"))
                .andExpect(jsonPath("$.reopenCount").value(1));

        Form reopenedForm = formRepository.findByFormIdentifier_FormId(form.getFormIdentifier().getFormId()).orElseThrow();
        assertEquals(FormStatus.REOPENED, reopenedForm.getFormStatus());
        assertEquals("Need changes", reopenedForm.getReopenReason());
        assertEquals(1, reopenedForm.getReopenCount());
    }

    // ========== Complete Form Tests ==========

    @Test
    void completeForm_AsOwner_CompletesSuccessfully() throws Exception {
        // Arrange
        Form form = createTestForm();
        form.setFormStatus(FormStatus.SUBMITTED);
        form = formRepository.save(form);

        // Act & Assert
        mockMvc.perform(post(BASE_URI + "/{formId}/complete", form.getFormIdentifier().getFormId())
                        .with(jwt().authorities(ROLE_OWNER)
                                .jwt(builder -> builder.subject(testSalesperson.getAuth0UserId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formStatus").value("COMPLETED"));

        Form completedForm = formRepository.findByFormIdentifier_FormId(form.getFormIdentifier().getFormId()).orElseThrow();
        assertEquals(FormStatus.COMPLETED, completedForm.getFormStatus());
        assertNotNull(completedForm.getCompletedDate());
    }

    // ========== Delete Form Tests ==========

    @Test
    void deleteForm_AsOwner_DeletesSuccessfully() throws Exception {
        // Arrange
        Form form = createTestForm();
        String formId = form.getFormIdentifier().getFormId();

        // Act & Assert
        mockMvc.perform(delete(BASE_URI + "/{formId}", formId)
                        .with(jwt().authorities(ROLE_OWNER)
                                .jwt(builder -> builder.subject(testSalesperson.getAuth0UserId()))))
                .andExpect(status().isNoContent());

        assertFalse(formRepository.findByFormIdentifier_FormId(formId).isPresent());
    }

    // ========== Get Form History Tests ==========

    @Test
    void getFormHistory_AsOwner_ReturnsHistory() throws Exception {
        // Arrange
        Form form = createTestForm();
        form.setFormStatus(FormStatus.SUBMITTED);
        form.setFirstSubmittedDate(LocalDateTime.now());
        form = formRepository.save(form);

        FormSubmissionHistory history = new FormSubmissionHistory();
        history.setFormIdentifier(form.getFormIdentifier().getFormId());
        history.setSubmissionNumber(1);
        history.setStatusAtSubmission(FormStatus.SUBMITTED);
        history.setFormDataSnapshot(new HashMap<>());
        history.setSubmittedByCustomerId(testCustomer.getUserIdentifier().getUserId());
        history.setSubmittedByCustomerName("Test Customer");
        history.setSubmittedAt(LocalDateTime.now());
        historyRepository.save(history);

        // Act & Assert
        mockMvc.perform(get(BASE_URI + "/{formId}/history", form.getFormIdentifier().getFormId())
                        .with(jwt().authorities(ROLE_OWNER)
                                .jwt(builder -> builder.subject(testSalesperson.getAuth0UserId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].submissionNumber").value(1));
    }

    // ========== Helper Methods ==========

    private Form createTestForm() {
        Form form = new Form();
        form.setFormIdentifier(new FormIdentifier(UUID.randomUUID().toString()));
        form.setFormType(FormType.WINDOWS);
        form.setFormStatus(FormStatus.ASSIGNED);
        form.setProjectIdentifier(testProject.getProjectIdentifier());
        form.setLotIdentifier(testLot.getLotIdentifier().getLotId());
        form.setCustomerId(testCustomer.getUserIdentifier().getUserId());
        form.setCustomerName("Test Customer");
        form.setCustomerEmail(testCustomer.getPrimaryEmail());
        form.setAssignedByUserId(testSalesperson.getUserIdentifier().getUserId());
        form.setAssignedByName("Test Salesperson");
        form.setFormTitle("Window Selection");
        form.setInstructions("Please select windows");
        form.setFormData(new HashMap<>());
        form.setAssignedDate(LocalDateTime.now());
        form.setReopenCount(0);
        return formRepository.save(form);
    }

    private Users createDifferentCustomer() {
        Users differentCustomer = new Users();
        differentCustomer.setUserIdentifier(UserIdentifier.newId());
        differentCustomer.setAuth0UserId("auth0|different-customer");
        differentCustomer.setFirstName("Different");
        differentCustomer.setLastName("Customer");
        differentCustomer.setPrimaryEmail("different.customer@example.com");
        differentCustomer.setUserRole(UserRole.CUSTOMER);
        differentCustomer.setUserStatus(UserStatus.ACTIVE);
        return usersRepository.save(differentCustomer);
    }

    private Form createFormForDifferentCustomer() {
        Users differentCustomer = createDifferentCustomer();

        Form form = new Form();
        form.setFormIdentifier(new FormIdentifier(UUID.randomUUID().toString()));
        form.setFormType(FormType.PAINT);
        form.setFormStatus(FormStatus.ASSIGNED);
        form.setProjectIdentifier(testProject.getProjectIdentifier());
        form.setLotIdentifier(testLot.getLotIdentifier().getLotId());
        form.setCustomerId(differentCustomer.getUserIdentifier().getUserId());
        form.setCustomerName("Different Customer");
        form.setCustomerEmail(differentCustomer.getPrimaryEmail());
        form.setAssignedByUserId(testSalesperson.getUserIdentifier().getUserId());
        form.setAssignedByName("Test Salesperson");
        form.setFormTitle("Paint Selection");
        form.setFormData(new HashMap<>());
        form.setAssignedDate(LocalDateTime.now());
        form.setReopenCount(0);
        return formRepository.save(form);
    }
}


package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.MailerServiceClient;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.NotificationService;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.BusinessLayer.FormServiceImpl;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.*;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.MapperLayer.FormMapper;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.MapperLayer.FormSubmissionHistoryMapper;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer.*;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FormServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class FormServiceImplUnitTest {

    @Mock
    private FormRepository formRepository;

    @Mock
    private FormSubmissionHistoryRepository historyRepository;

    @Mock
    private FormMapper formMapper;

    @Mock
    private FormSubmissionHistoryMapper historyMapper;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private LotRepository lotRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private MailerServiceClient mailerServiceClient;

    @InjectMocks
    private FormServiceImpl formService;

    private Form testForm;
    private FormRequestModel testRequestModel;
    private FormResponseModel testResponseModel;
    private Users testCustomer;
    private Users testSalesperson;
    private FormIdentifier testFormIdentifier;
    private Project testProject;
    private Lot testLot;
    
    // UUID constants for testing
    private static final String CUSTOMER_UUID = "123e4567-e89b-12d3-a456-426614174000";
    private static final String SALESPERSON_UUID = "223e4567-e89b-12d3-a456-426614174001";
    private static final String EXTRA_USER_1_UUID = "323e4567-e89b-12d3-a456-426614174002";
    private static final String EXTRA_USER_2_UUID = "423e4567-e89b-12d3-a456-426614174003";
    private static final String OTHER_USER_1_UUID = "523e4567-e89b-12d3-a456-426614174004";
    private static final String OTHER_USER_2_UUID = "623e4567-e89b-12d3-a456-426614174005";

    @BeforeEach
    void setUp() {
        // Setup test form identifier
        testFormIdentifier = new FormIdentifier("test-form-id-123");

        // Setup test users
        testCustomer = new Users();
        testCustomer.setUserIdentifier(UserIdentifier.fromString(CUSTOMER_UUID));
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Customer");
        testCustomer.setPrimaryEmail("john.customer@example.com");

        testSalesperson = new Users();
        testSalesperson.setUserIdentifier(UserIdentifier.fromString(SALESPERSON_UUID));
        testSalesperson.setAuth0UserId("auth0|salesperson-456");
        testSalesperson.setFirstName("Jane");
        testSalesperson.setLastName("Salesperson");
        testSalesperson.setPrimaryEmail("jane.salesperson@example.com");

        // Setup test project
        testProject = new Project();
        testProject.setProjectIdentifier("project-123");
        testProject.setProjectName("Test Project");

        // Setup test lot
        testLot = new Lot();
        testLot.setLotIdentifier(new LotIdentifier(UUID.randomUUID().toString()));
        testLot.setProject(testProject);
        testLot.setAssignedUsers(Arrays.asList(testCustomer, testSalesperson));

        // Setup test request model
        testRequestModel = FormRequestModel.builder()
                .formType(FormType.WINDOWS)
                .projectIdentifier("project-123")
                .lotIdentifier(testLot.getLotIdentifier().getLotId().toString())
                .customerId(CUSTOMER_UUID)
                .formTitle("Window Color Selection")
                .instructions("Please select colors for all windows")
                .formData(new HashMap<>())
                .build();

        // Setup test form entity
        testForm = new Form();
        testForm.setId(1L);
        testForm.setFormIdentifier(testFormIdentifier);
        testForm.setFormType(FormType.WINDOWS);
        testForm.setFormStatus(FormStatus.ASSIGNED);
        testForm.setProjectIdentifier("project-123");
        testForm.setLotIdentifier(testLot.getLotIdentifier().getLotId().toString());
        testForm.setCustomerId(CUSTOMER_UUID);
        testForm.setCustomerName("John Customer");
        testForm.setCustomerEmail("john.customer@example.com");
        testForm.setAssignedByUserId(SALESPERSON_UUID);
        testForm.setAssignedByName("Jane Salesperson");
        testForm.setFormTitle("Window Color Selection");
        testForm.setInstructions("Please select colors for all windows");
        testForm.setFormData(new HashMap<>());
        testForm.setAssignedDate(LocalDateTime.now());
        testForm.setReopenCount(0);

        // Setup test response model
        testResponseModel = FormResponseModel.builder()
                .formId("test-form-id-123")
                .formType(FormType.WINDOWS)
                .formStatus(FormStatus.ASSIGNED)
                .projectIdentifier("project-123")
                .lotIdentifier(testLot.getLotIdentifier().getLotId().toString())
                .customerId(CUSTOMER_UUID)
                .customerName("John Customer")
                .customerEmail("john.customer@example.com")
                .assignedByUserId(SALESPERSON_UUID)
                .assignedByName("Jane Salesperson")
                .formTitle("Window Color Selection")
                .instructions("Please select colors for all windows")
                .formData(new HashMap<>())
                .reopenCount(0)
                .build();
    }

    // ========== Create and Assign Form Tests ==========

    @Test
    void createAndAssignForm_WithValidData_ReturnsCreatedForm() {
        // Arrange
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);
        when(formRepository.existsByProjectIdentifierAndLotIdentifierAndCustomerIdAndFormType(
                "project-123", testLot.getLotIdentifier().getLotId().toString(), CUSTOMER_UUID, FormType.WINDOWS)).thenReturn(false);
        when(formMapper.requestModelToEntity(any())).thenReturn(testForm);
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        FormResponseModel result = formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456");

        // Assert
        assertNotNull(result);
        assertEquals("test-form-id-123", result.getFormId());
        assertEquals(FormType.WINDOWS, result.getFormType());
        assertEquals(FormStatus.ASSIGNED, result.getFormStatus());

        verify(formRepository).save(any(Form.class));
        verify(notificationService).createNotification(any(UUID.class), anyString(), anyString(), any(), anyString());
        verify(mailerServiceClient).sendEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void createAndAssignForm_WithNonExistentCustomer_ThrowsNotFoundException() {
        // Arrange
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456")
        );

        verify(formRepository, never()).save(any());
    }

    @Test
    void createAndAssignForm_WithDuplicateFormType_ThrowsInvalidInputException() {
        // Arrange
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);
        when(formRepository.existsByProjectIdentifierAndLotIdentifierAndCustomerIdAndFormType(
                "project-123", testLot.getLotIdentifier().getLotId().toString(), CUSTOMER_UUID, FormType.WINDOWS)).thenReturn(true);

        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456")
        );

        verify(formRepository, never()).save(any());
    }

    // ========== Get Form Tests ==========

    @Test
    void getFormById_WithValidId_ReturnsForm() {
        // Arrange
        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);

        // Act
        FormResponseModel result = formService.getFormById("test-form-id-123");

        // Assert
        assertNotNull(result);
        assertEquals("test-form-id-123", result.getFormId());
        verify(formRepository).findByFormIdentifier_FormId("test-form-id-123");
    }

    @Test
    void getFormById_WithInvalidId_ThrowsNotFoundException() {
        // Arrange
        when(formRepository.findByFormIdentifier_FormId("invalid-id"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                formService.getFormById("invalid-id")
        );
    }

    @Test
    void getFormsByProject_ReturnsAllFormsForProject() {
        // Arrange
        List<Form> forms = Arrays.asList(testForm);
        when(formRepository.findByProjectIdentifier("project-123")).thenReturn(forms);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);

        // Act
        List<FormResponseModel> result = formService.getFormsByProject("project-123");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(formRepository).findByProjectIdentifier("project-123");
    }

    // ========== Update Form Data Tests ==========

    @Test
    void updateFormData_WithValidData_UpdatesForm() {
        // Arrange
        Map<String, Object> newData = new HashMap<>();
        newData.put("exteriorColorFacade", "White");
        newData.put("interiorColorFacade", "Oak");

        FormDataUpdateRequestModel updateRequest = FormDataUpdateRequestModel.builder()
                .formData(newData)
                .isSubmitting(false)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);

        // Act
        FormResponseModel result = formService.updateFormData("test-form-id-123", updateRequest, CUSTOMER_UUID);

        // Assert
        assertNotNull(result);
        verify(formRepository).save(any(Form.class));
    }

    @Test
    void updateFormData_ByNonOwner_ThrowsInvalidInputException() {
        // Arrange
        FormDataUpdateRequestModel updateRequest = FormDataUpdateRequestModel.builder()
                .formData(new HashMap<>())
                .isSubmitting(false)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));

        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                formService.updateFormData("test-form-id-123", updateRequest, "different-customer-id")
        );

        verify(formRepository, never()).save(any());
    }

    @Test
    void updateFormData_OnCompletedForm_ThrowsInvalidInputException() {
        // Arrange
        testForm.setFormStatus(FormStatus.COMPLETED);
        FormDataUpdateRequestModel updateRequest = FormDataUpdateRequestModel.builder()
                .formData(new HashMap<>())
                .isSubmitting(false)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));

        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                formService.updateFormData("test-form-id-123", updateRequest, CUSTOMER_UUID)
        );

        verify(formRepository, never()).save(any());
    }

    // ========== Submit Form Tests ==========

    @Test
    void submitForm_WithValidData_CreatesHistoryAndNotifies() {
        // Arrange
        FormDataUpdateRequestModel submitRequest = FormDataUpdateRequestModel.builder()
                .formData(new HashMap<>())
                .submissionNotes("All selections completed")
                .isSubmitting(true)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByUserIdentifier(SALESPERSON_UUID)).thenReturn(Optional.of(testSalesperson));
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(historyRepository.countByFormIdentifier(any())).thenReturn(0L);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        FormResponseModel result = formService.submitForm("test-form-id-123", submitRequest, CUSTOMER_UUID);

        // Assert
        assertNotNull(result);
        verify(formRepository).save(any(Form.class));
        verify(historyRepository).save(any(FormSubmissionHistory.class));
        verify(notificationService).createNotification(any(UUID.class), anyString(), anyString(), any(), anyString());
        verify(mailerServiceClient).sendEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void submitForm_WhenAlreadySubmitted_ThrowsInvalidInputException() {
        // Arrange
        testForm.setFormStatus(FormStatus.SUBMITTED);
        FormDataUpdateRequestModel submitRequest = FormDataUpdateRequestModel.builder()
                .formData(new HashMap<>())
                .isSubmitting(true)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));

        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                formService.submitForm("test-form-id-123", submitRequest, CUSTOMER_UUID)
        );

        verify(historyRepository, never()).save(any());
    }

    // ========== Reopen Form Tests ==========

    @Test
    void reopenForm_WithValidData_ReopensAndNotifiesCustomer() {
        // Arrange
        testForm.setFormStatus(FormStatus.SUBMITTED);
        FormReopenRequestModel reopenRequest = FormReopenRequestModel.builder()
                .reopenReason("Need to adjust window colors for north side")
                .newInstructions("Please review and update northern window colors")
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456"))
                .thenReturn(Optional.of(testSalesperson));
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID))
                .thenReturn(Optional.of(testCustomer));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        FormResponseModel result = formService.reopenForm("test-form-id-123", reopenRequest, "auth0|salesperson-456");

        // Assert
        assertNotNull(result);
        verify(formRepository).save(argThat(form ->
                form.getFormStatus() == FormStatus.REOPENED &&
                form.getReopenCount() == 1 &&
                form.getReopenReason().equals("Need to adjust window colors for north side")
        ));
        verify(notificationService).createNotification(any(UUID.class), anyString(), anyString(), any(), anyString());
        verify(mailerServiceClient).sendEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void reopenForm_OnNonSubmittedForm_ThrowsInvalidInputException() {
        // Arrange
        testForm.setFormStatus(FormStatus.IN_PROGRESS);
        FormReopenRequestModel reopenRequest = FormReopenRequestModel.builder()
                .reopenReason("Test reason")
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456"))
                .thenReturn(Optional.of(testSalesperson));

        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                formService.reopenForm("test-form-id-123", reopenRequest, "auth0|salesperson-456")
        );

        verify(formRepository, never()).save(any());
    }

    // ========== Complete Form Tests ==========

    @Test
    void completeForm_WithSubmittedForm_MarksAsCompleted() {
        // Arrange
        testForm.setFormStatus(FormStatus.SUBMITTED);

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456"))
                .thenReturn(Optional.of(testSalesperson));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);

        // Act
        FormResponseModel result = formService.completeForm("test-form-id-123", "auth0|salesperson-456");

        // Assert
        assertNotNull(result);
        verify(formRepository).save(argThat(form ->
                form.getFormStatus() == FormStatus.COMPLETED &&
                form.getCompletedDate() != null
        ));
    }

    @Test
    void completeForm_WithNonSubmittedForm_ThrowsInvalidInputException() {
        // Arrange
        testForm.setFormStatus(FormStatus.IN_PROGRESS);

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456"))
                .thenReturn(Optional.of(testSalesperson));

        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                formService.completeForm("test-form-id-123", "auth0|salesperson-456")
        );

        verify(formRepository, never()).save(any());
    }

    // ========== Delete Form Tests ==========

    @Test
    void deleteForm_WithValidId_DeletesFormAndHistory() {
        // Arrange
        List<FormSubmissionHistory> historyList = new ArrayList<>();
        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456"))
                .thenReturn(Optional.of(testSalesperson));
        when(historyRepository.findByFormIdentifierOrderBySubmittedAtDesc("test-form-id-123"))
                .thenReturn(historyList);

        // Act
        formService.deleteForm("test-form-id-123", "auth0|salesperson-456");

        // Assert
        verify(historyRepository).deleteAll(historyList);
        verify(formRepository).delete(testForm);
    }

    // ========== Get Submission History Tests ==========

    @Test
    void getFormSubmissionHistory_WithValidFormId_ReturnsHistory() {
        // Arrange
        FormSubmissionHistory history1 = new FormSubmissionHistory();
        history1.setId(1L);
        history1.setFormIdentifier("test-form-id-123");
        history1.setSubmissionNumber(1);

        List<FormSubmissionHistory> historyList = Arrays.asList(history1);

        FormSubmissionHistoryResponseModel historyResponse = FormSubmissionHistoryResponseModel.builder()
                .id(1L)
                .formIdentifier("test-form-id-123")
                .submissionNumber(1)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(historyRepository.findByFormIdentifierOrderBySubmissionNumberAsc("test-form-id-123"))
                .thenReturn(historyList);
        when(historyMapper.entityToResponseModel(any())).thenReturn(historyResponse);

        // Act
        List<FormSubmissionHistoryResponseModel> result = formService.getFormSubmissionHistory("test-form-id-123");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(historyRepository).findByFormIdentifierOrderBySubmissionNumberAsc("test-form-id-123");
    }

    // ========== Has Form Of Type Tests ==========

    @Test
    void hasFormOfType_WhenFormExists_ReturnsTrue() {
        // Arrange
        when(formRepository.existsByProjectIdentifierAndCustomerIdAndFormType(
                "project-123", CUSTOMER_UUID, FormType.WINDOWS)).thenReturn(true);

        // Act
        boolean result = formService.hasFormOfType("project-123", CUSTOMER_UUID, FormType.WINDOWS);

        // Assert
        assertTrue(result);
    }

    @Test
    void hasFormOfType_WhenFormDoesNotExist_ReturnsFalse() {
        // Arrange
        when(formRepository.existsByProjectIdentifierAndCustomerIdAndFormType(
                "project-123", CUSTOMER_UUID, FormType.PAINT)).thenReturn(false);

        // Act
        boolean result = formService.hasFormOfType("project-123", CUSTOMER_UUID, FormType.PAINT);

        // Assert
        assertFalse(result);
    }

    // ========== Additional Coverage Tests ==========

    @Test
    void createAndAssignForm_WithNonExistentProject_ThrowsNotFoundException() {
        // Arrange
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456")
        );

        verify(formRepository, never()).save(any());
    }

    @Test
    void createAndAssignForm_WithNonExistentLot_ThrowsNotFoundException() {
        // Arrange
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(null);

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456")
        );

        verify(formRepository, never()).save(any());
    }

    @Test
    void createAndAssignForm_WithLotNotBelongingToProject_ThrowsInvalidInputException() {
        // Arrange
        Project differentProject = new Project();
        differentProject.setProjectIdentifier("different-project");
        testLot.setProject(differentProject);

        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);

        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456")
        );

        verify(formRepository, never()).save(any());
    }

    @Test
    void createAndAssignForm_WithNoAssignedUsers_ThrowsInvalidInputException() {
        // Arrange
        testLot.setAssignedUsers(new ArrayList<>());

        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);

        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456")
        );

        verify(formRepository, never()).save(any());
    }

    @Test
    void createAndAssignForm_WithSalespersonNotAssignedToLot_ThrowsInvalidInputException() {
        // Arrange
        Users differentSalesperson = new Users();
        differentSalesperson.setUserIdentifier(UserIdentifier.fromString("different-salesperson"));
        testLot.setAssignedUsers(Arrays.asList(testCustomer)); // Only customer, no salesperson

        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);

        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456")
        );

        verify(formRepository, never()).save(any());
    }

    @Test
    void createAndAssignForm_WithCustomerNotAssignedToLot_ThrowsInvalidInputException() {
        // Arrange
        testLot.setAssignedUsers(Arrays.asList(testSalesperson)); // Only salesperson, no customer

        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);

        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456")
        );

        verify(formRepository, never()).save(any());
    }

    @Test
    void createAndAssignForm_WithNonExistentSalesperson_ThrowsNotFoundException() {
        // Arrange
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456")
        );

        verify(formRepository, never()).save(any());
    }

    @Test
    void getFormsByCustomer_ReturnsAllFormsForCustomer() {
        // Arrange
        List<Form> forms = Arrays.asList(testForm);
        when(formRepository.findByCustomerId(CUSTOMER_UUID)).thenReturn(forms);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);

        // Act
        List<FormResponseModel> result = formService.getFormsByCustomer(CUSTOMER_UUID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(formRepository).findByCustomerId(CUSTOMER_UUID);
    }

    @Test
    void getFormsCreatedBy_ReturnsAllFormsCreatedBySalesperson() {
        // Arrange
        List<Form> forms = Arrays.asList(testForm);
        when(formRepository.findByAssignedByUserId(SALESPERSON_UUID)).thenReturn(forms);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);

        // Act
        List<FormResponseModel> result = formService.getFormsCreatedBy(SALESPERSON_UUID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(formRepository).findByAssignedByUserId(SALESPERSON_UUID);
    }

    @Test
    void getFormsByStatus_ReturnsAllFormsWithStatus() {
        // Arrange
        List<Form> forms = Arrays.asList(testForm);
        when(formRepository.findByFormStatus(FormStatus.ASSIGNED)).thenReturn(forms);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);

        // Act
        List<FormResponseModel> result = formService.getFormsByStatus(FormStatus.ASSIGNED);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(formRepository).findByFormStatus(FormStatus.ASSIGNED);
    }

    @Test
    void updateFormData_ChangesStatusToInProgress_WhenStatusIsAssigned() {
        // Arrange
        testForm.setFormStatus(FormStatus.ASSIGNED);
        Map<String, Object> newData = new HashMap<>();
        newData.put("color", "Blue");

        FormDataUpdateRequestModel updateRequest = FormDataUpdateRequestModel.builder()
                .formData(newData)
                .isSubmitting(false)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);

        // Act
        FormResponseModel result = formService.updateFormData("test-form-id-123", updateRequest, CUSTOMER_UUID);

        // Assert
        assertNotNull(result);
        verify(formRepository).save(argThat(form -> form.getFormStatus() == FormStatus.IN_PROGRESS));
    }

    @Test
    void updateFormData_ChangesStatusToInProgress_WhenStatusIsReopened() {
        // Arrange
        testForm.setFormStatus(FormStatus.REOPENED);
        Map<String, Object> newData = new HashMap<>();

        FormDataUpdateRequestModel updateRequest = FormDataUpdateRequestModel.builder()
                .formData(newData)
                .isSubmitting(false)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);

        // Act
        FormResponseModel result = formService.updateFormData("test-form-id-123", updateRequest, CUSTOMER_UUID);

        // Assert
        assertNotNull(result);
        verify(formRepository).save(argThat(form -> form.getFormStatus() == FormStatus.IN_PROGRESS));
    }

    @Test
    void submitForm_ByNonOwner_ThrowsInvalidInputException() {
        // Arrange
        FormDataUpdateRequestModel submitRequest = FormDataUpdateRequestModel.builder()
                .formData(new HashMap<>())
                .isSubmitting(true)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));

        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                formService.submitForm("test-form-id-123", submitRequest, "different-customer-id")
        );

        verify(historyRepository, never()).save(any());
    }

    @Test
    void submitForm_WhenAlreadyCompleted_ThrowsInvalidInputException() {
        // Arrange
        testForm.setFormStatus(FormStatus.COMPLETED);
        FormDataUpdateRequestModel submitRequest = FormDataUpdateRequestModel.builder()
                .formData(new HashMap<>())
                .isSubmitting(true)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));

        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                formService.submitForm("test-form-id-123", submitRequest, CUSTOMER_UUID)
        );

        verify(historyRepository, never()).save(any());
    }

    @Test
    void submitForm_SetsFirstSubmittedDate_WhenFirstTimeSubmission() {
        // Arrange
        testForm.setFirstSubmittedDate(null);
        FormDataUpdateRequestModel submitRequest = FormDataUpdateRequestModel.builder()
                .formData(new HashMap<>())
                .submissionNotes("First submission")
                .isSubmitting(true)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByUserIdentifier(SALESPERSON_UUID)).thenReturn(Optional.of(testSalesperson));
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(historyRepository.countByFormIdentifier(any())).thenReturn(0L);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        FormResponseModel result = formService.submitForm("test-form-id-123", submitRequest, CUSTOMER_UUID);

        // Assert
        assertNotNull(result);
        verify(formRepository).save(argThat(form ->
                form.getFormStatus() == FormStatus.SUBMITTED &&
                form.getFirstSubmittedDate() != null &&
                form.getLastSubmittedDate() != null
        ));
    }

    @Test
    void reopenForm_WithNewInstructions_UpdatesInstructions() {
        // Arrange
        testForm.setFormStatus(FormStatus.SUBMITTED);
        FormReopenRequestModel reopenRequest = FormReopenRequestModel.builder()
                .reopenReason("Need changes")
                .newInstructions("New instructions here")
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456"))
                .thenReturn(Optional.of(testSalesperson));
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID))
                .thenReturn(Optional.of(testCustomer));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        FormResponseModel result = formService.reopenForm("test-form-id-123", reopenRequest, "auth0|salesperson-456");

        // Assert
        assertNotNull(result);
        verify(formRepository).save(argThat(form ->
                "New instructions here".equals(form.getInstructions())
        ));
    }

    @Test
    void reopenForm_WithNonExistentUser_ThrowsNotFoundException() {
        // Arrange
        testForm.setFormStatus(FormStatus.SUBMITTED);
        FormReopenRequestModel reopenRequest = FormReopenRequestModel.builder()
                .reopenReason("Test reason")
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                formService.reopenForm("test-form-id-123", reopenRequest, "auth0|salesperson-456")
        );

        verify(formRepository, never()).save(any());
    }

    @Test
    void completeForm_WithNonExistentUser_ThrowsNotFoundException() {
        // Arrange
        testForm.setFormStatus(FormStatus.SUBMITTED);

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                formService.completeForm("test-form-id-123", "auth0|salesperson-456")
        );

        verify(formRepository, never()).save(any());
    }

    @Test
    void updateFormDetails_UpdatesFormSuccessfully() {
        // Arrange
        FormRequestModel updateModel = FormRequestModel.builder()
                .formType(FormType.WINDOWS)
                .projectIdentifier("project-123")
                .lotIdentifier(testLot.getLotIdentifier().getLotId().toString())
                .customerId(CUSTOMER_UUID)
                .formTitle("Updated Title")
                .instructions("Updated instructions")
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456"))
                .thenReturn(Optional.of(testSalesperson));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);

        // Act
        FormResponseModel result = formService.updateFormDetails("test-form-id-123", updateModel, "auth0|salesperson-456");

        // Assert
        assertNotNull(result);
        verify(formMapper).updateEntityFromRequestModel(updateModel, testForm);
        verify(formRepository).save(testForm);
    }

    @Test
    void updateFormDetails_WithNonExistentForm_ThrowsNotFoundException() {
        // Arrange
        FormRequestModel updateModel = FormRequestModel.builder().build();

        when(formRepository.findByFormIdentifier_FormId("invalid-id"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                formService.updateFormDetails("invalid-id", updateModel, "auth0|salesperson-456")
        );

        verify(formRepository, never()).save(any());
    }

    @Test
    void updateFormDetails_WithNonExistentUser_ThrowsNotFoundException() {
        // Arrange
        FormRequestModel updateModel = FormRequestModel.builder().build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                formService.updateFormDetails("test-form-id-123", updateModel, "auth0|salesperson-456")
        );

        verify(formRepository, never()).save(any());
    }

    @Test
    void deleteForm_WithNonExistentUser_ThrowsNotFoundException() {
        // Arrange
        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                formService.deleteForm("test-form-id-123", "auth0|salesperson-456")
        );

        verify(formRepository, never()).delete(any());
    }

    @Test
    void getFormSubmissionHistory_WithNonExistentForm_ThrowsNotFoundException() {
        // Arrange
        when(formRepository.findByFormIdentifier_FormId("invalid-id"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                formService.getFormSubmissionHistory("invalid-id")
        );

        verify(historyRepository, never()).findByFormIdentifierOrderBySubmissionNumberAsc(any());
    }

    @Test
    void hasFormOfType_WithProjectAndLotAndType_ChecksCorrectly() {
        // Arrange
        when(formRepository.existsByProjectIdentifierAndCustomerIdAndFormType(
                "project-123", CUSTOMER_UUID, FormType.EXTERIOR_DOORS))
                .thenReturn(true);

        // Act
        boolean result = formService.hasFormOfType("project-123", CUSTOMER_UUID, FormType.EXTERIOR_DOORS);

        // Assert
        assertTrue(result);
        verify(formRepository).existsByProjectIdentifierAndCustomerIdAndFormType(
                "project-123", CUSTOMER_UUID, FormType.EXTERIOR_DOORS);
    }

    // ========== Tests that exercise helper methods through public methods ==========
    
    @Test
    void createAndAssignForm_WithSpecialCharactersInInstructions_EscapesHtmlInEmail() {
        // Arrange - This will test escapeHtml, getFullName, getFormTypeDisplayName, buildFormAssignedEmailBody
        testRequestModel.setInstructions("<script>alert('test')</script>\nLine 2");
        
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);
        when(formRepository.existsByProjectIdentifierAndLotIdentifierAndCustomerIdAndFormType(
                any(), any(), any(), any())).thenReturn(false);
        when(formMapper.requestModelToEntity(any())).thenReturn(testForm);
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        FormResponseModel result = formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456");

        // Assert
        assertNotNull(result);
        verify(mailerServiceClient).sendEmail(
                eq(testCustomer.getPrimaryEmail()),
                anyString(),
                argThat(body -> body.contains("&lt;script&gt;") && body.contains("<br/>")),
                anyString()
        );
    }

    @Test
    void createAndAssignForm_WithDifferentFormTypes_UsesCorrectDisplayNames() {
        // Test EXTERIOR_DOORS
        testRequestModel.setFormType(FormType.EXTERIOR_DOORS);
        testForm.setFormType(FormType.EXTERIOR_DOORS);
        
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);
        when(formRepository.existsByProjectIdentifierAndLotIdentifierAndCustomerIdAndFormType(
                any(), any(), any(), any())).thenReturn(false);
        when(formMapper.requestModelToEntity(any())).thenReturn(testForm);
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456");

        // Assert - verify email contains form type display name
        verify(mailerServiceClient).sendEmail(
                anyString(),
                argThat(subject -> subject.contains("Exterior Doors")),
                argThat(body -> body.contains("Exterior Doors")),
                anyString()
        );
    }

    @Test
    void createAndAssignForm_WithGarageDoorFormType_UsesCorrectDisplayName() {
        testRequestModel.setFormType(FormType.GARAGE_DOORS);
        testForm.setFormType(FormType.GARAGE_DOORS);
        
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);
        when(formRepository.existsByProjectIdentifierAndLotIdentifierAndCustomerIdAndFormType(
                any(), any(), any(), any())).thenReturn(false);
        when(formMapper.requestModelToEntity(any())).thenReturn(testForm);
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456");

        verify(mailerServiceClient).sendEmail(
                anyString(),
                argThat(subject -> subject.contains("Garage Doors")),
                anyString(),
                anyString()
        );
    }

    @Test
    void createAndAssignForm_WithAsphaltShinglesFormType_UsesCorrectDisplayName() {
        testRequestModel.setFormType(FormType.ASPHALT_SHINGLES);
        testForm.setFormType(FormType.ASPHALT_SHINGLES);
        
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);
        when(formRepository.existsByProjectIdentifierAndLotIdentifierAndCustomerIdAndFormType(
                any(), any(), any(), any())).thenReturn(false);
        when(formMapper.requestModelToEntity(any())).thenReturn(testForm);
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456");

        verify(mailerServiceClient).sendEmail(
                anyString(),
                argThat(subject -> subject.contains("Asphalt Shingles")),
                anyString(),
                anyString()
        );
    }

    @Test
    void createAndAssignForm_WithWoodworkFormType_UsesCorrectDisplayName() {
        testRequestModel.setFormType(FormType.WOODWORK);
        testForm.setFormType(FormType.WOODWORK);
        
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);
        when(formRepository.existsByProjectIdentifierAndLotIdentifierAndCustomerIdAndFormType(
                any(), any(), any(), any())).thenReturn(false);
        when(formMapper.requestModelToEntity(any())).thenReturn(testForm);
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456");

        verify(mailerServiceClient).sendEmail(
                anyString(),
                argThat(subject -> subject.contains("Woodwork")),
                anyString(),
                anyString()
        );
    }

    @Test
    void createAndAssignForm_WithPaintFormType_UsesCorrectDisplayName() {
        testRequestModel.setFormType(FormType.PAINT);
        testForm.setFormType(FormType.PAINT);
        
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);
        when(formRepository.existsByProjectIdentifierAndLotIdentifierAndCustomerIdAndFormType(
                any(), any(), any(), any())).thenReturn(false);
        when(formMapper.requestModelToEntity(any())).thenReturn(testForm);
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456");

        verify(mailerServiceClient).sendEmail(
                anyString(),
                argThat(subject -> subject.contains("Paint")),
                anyString(),
                anyString()
        );
    }

    @Test
    void submitForm_WithValidData_CallsAllNotificationHelpers() {
        // This tests: sendFormSubmittedNotification, buildFormSubmittedEmailBody, getFullName, getFormTypeDisplayName
        FormDataUpdateRequestModel submitRequest = FormDataUpdateRequestModel.builder()
                .formData(new HashMap<>())
                .submissionNotes("All done")
                .isSubmitting(true)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByUserIdentifier(SALESPERSON_UUID)).thenReturn(Optional.of(testSalesperson));
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(historyRepository.countByFormIdentifier(any())).thenReturn(0L);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        formService.submitForm("test-form-id-123", submitRequest, CUSTOMER_UUID);

        // Verify email was sent with proper content
        verify(mailerServiceClient).sendEmail(
                eq(testSalesperson.getPrimaryEmail()),
                argThat(subject -> subject.contains("John Customer")),
                argThat(body -> body.contains("John Customer") && 
                               body.contains("Windows") && 
                               body.contains("project-123") &&
                               body.contains("test-form-id-123")),
                anyString()
        );
    }

    @Test
    void reopenForm_WithSpecialCharactersInReason_EscapesHtmlInEmail() {
        // Tests: sendFormReopenedNotification, buildFormReopenedEmailBody, escapeHtml
        testForm.setFormStatus(FormStatus.SUBMITTED);
        FormReopenRequestModel reopenRequest = FormReopenRequestModel.builder()
                .reopenReason("<script>test</script> & \"quotes\"")
                .newInstructions("New <strong>instructions</strong>")
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456"))
                .thenReturn(Optional.of(testSalesperson));
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID))
                .thenReturn(Optional.of(testCustomer));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        formService.reopenForm("test-form-id-123", reopenRequest, "auth0|salesperson-456");

        // Verify HTML is escaped in email
        verify(mailerServiceClient).sendEmail(
                eq(testCustomer.getPrimaryEmail()),
                anyString(),
                argThat(body -> body.contains("&lt;script&gt;") && 
                               body.contains("&amp;") && 
                               body.contains("&quot;") &&
                               body.contains("&lt;strong&gt;")),
                anyString()
        );
    }

    @Test
    void createAndAssignForm_WithCustomerWithOnlyFirstName_HandlesGracefully() {
        // Tests getFullName with missing last name
        Users customerWithOnlyFirstName = new Users();
        customerWithOnlyFirstName.setUserIdentifier(UserIdentifier.fromString(CUSTOMER_UUID));
        customerWithOnlyFirstName.setFirstName("John");
        customerWithOnlyFirstName.setLastName(null);
        customerWithOnlyFirstName.setPrimaryEmail("john@example.com");

        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(customerWithOnlyFirstName));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);
        when(formRepository.existsByProjectIdentifierAndLotIdentifierAndCustomerIdAndFormType(
                any(), any(), any(), any())).thenReturn(false);
        when(formMapper.requestModelToEntity(any())).thenReturn(testForm);
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456");

        verify(mailerServiceClient).sendEmail(
                anyString(),
                anyString(),
                argThat(body -> body.contains("John") && !body.contains("null")),
                anyString()
        );
    }

    @Test
    void createAndAssignForm_WithCustomerWithOnlyLastName_HandlesGracefully() {
        // Tests getFullName with missing first name
        Users customerWithOnlyLastName = new Users();
        customerWithOnlyLastName.setUserIdentifier(UserIdentifier.fromString(CUSTOMER_UUID));
        customerWithOnlyLastName.setFirstName(null);
        customerWithOnlyLastName.setLastName("Doe");
        customerWithOnlyLastName.setPrimaryEmail("doe@example.com");

        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(customerWithOnlyLastName));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);
        when(formRepository.existsByProjectIdentifierAndLotIdentifierAndCustomerIdAndFormType(
                any(), any(), any(), any())).thenReturn(false);
        when(formMapper.requestModelToEntity(any())).thenReturn(testForm);
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456");

        verify(mailerServiceClient).sendEmail(
                anyString(),
                anyString(),
                argThat(body -> body.contains("Doe") && !body.contains("null")),
                anyString()
        );
    }

    @Test
    void reopenForm_WithNullInstructions_DoesNotIncludeInstructionsInEmail() {
        testForm.setFormStatus(FormStatus.SUBMITTED);
        testForm.setInstructions(null);
        FormReopenRequestModel reopenRequest = FormReopenRequestModel.builder()
                .reopenReason("Please review")
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456"))
                .thenReturn(Optional.of(testSalesperson));
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID))
                .thenReturn(Optional.of(testCustomer));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        formService.reopenForm("test-form-id-123", reopenRequest, "auth0|salesperson-456");

        verify(mailerServiceClient).sendEmail(
                eq(testCustomer.getPrimaryEmail()),
                anyString(),
                argThat(body -> !body.contains("Updated Instructions:")),
                anyString()
        );
    }

    @Test
    void hasFormOfType_ChecksRepositoryCorrectly() {
        when(formRepository.existsByProjectIdentifierAndCustomerIdAndFormType(
                "project-123", "customer-123", FormType.WINDOWS))
                .thenReturn(true);

        boolean result = formService.hasFormOfType("project-123", "customer-123", FormType.WINDOWS);

        assertTrue(result);
        verify(formRepository).existsByProjectIdentifierAndCustomerIdAndFormType(
                "project-123", "customer-123", FormType.WINDOWS);
    }

    // ========== Tests for notification subscription lambdas ==========

    @Test
    void sendFormAssignedNotification_WhenEmailSucceeds_LogsSuccess() {
        // Arrange - mock Mono that completes successfully
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);
        when(formRepository.existsByProjectIdentifierAndLotIdentifierAndCustomerIdAndFormType(
                any(), any(), any(), any())).thenReturn(false);
        when(formMapper.requestModelToEntity(any())).thenReturn(testForm);
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        
        // Create a Mono that completes successfully to trigger the onComplete lambda
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456");

        // Assert - verify the Mono was subscribed to
        verify(mailerServiceClient).sendEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sendFormAssignedNotification_WhenEmailFails_LogsError() {
        // Arrange - mock Mono that fails
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);
        when(formRepository.existsByProjectIdentifierAndLotIdentifierAndCustomerIdAndFormType(
                any(), any(), any(), any())).thenReturn(false);
        when(formMapper.requestModelToEntity(any())).thenReturn(testForm);
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        
        // Create a Mono that fails to trigger the onError lambda
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Email service down")));

        // Act - should not throw exception
        assertDoesNotThrow(() -> formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456"));

        // Assert
        verify(mailerServiceClient).sendEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sendFormSubmittedNotification_WhenEmailSucceeds_LogsSuccess() {
        FormDataUpdateRequestModel submitRequest = FormDataUpdateRequestModel.builder()
                .formData(new HashMap<>())
                .submissionNotes("Done")
                .isSubmitting(true)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByUserIdentifier(SALESPERSON_UUID)).thenReturn(Optional.of(testSalesperson));
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(historyRepository.countByFormIdentifier(any())).thenReturn(0L);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        formService.submitForm("test-form-id-123", submitRequest, CUSTOMER_UUID);

        verify(mailerServiceClient).sendEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sendFormSubmittedNotification_WhenEmailFails_LogsError() {
        FormDataUpdateRequestModel submitRequest = FormDataUpdateRequestModel.builder()
                .formData(new HashMap<>())
                .submissionNotes("Done")
                .isSubmitting(true)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByUserIdentifier(SALESPERSON_UUID)).thenReturn(Optional.of(testSalesperson));
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(historyRepository.countByFormIdentifier(any())).thenReturn(0L);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Email service down")));

        assertDoesNotThrow(() -> formService.submitForm("test-form-id-123", submitRequest, CUSTOMER_UUID));

        verify(mailerServiceClient).sendEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sendFormReopenedNotification_WhenEmailSucceeds_LogsSuccess() {
        testForm.setFormStatus(FormStatus.SUBMITTED);
        FormReopenRequestModel reopenRequest = FormReopenRequestModel.builder()
                .reopenReason("Need changes")
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456"))
                .thenReturn(Optional.of(testSalesperson));
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID))
                .thenReturn(Optional.of(testCustomer));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        formService.reopenForm("test-form-id-123", reopenRequest, "auth0|salesperson-456");

        verify(mailerServiceClient).sendEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sendFormReopenedNotification_WhenEmailFails_LogsError() {
        testForm.setFormStatus(FormStatus.SUBMITTED);
        FormReopenRequestModel reopenRequest = FormReopenRequestModel.builder()
                .reopenReason("Need changes")
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456"))
                .thenReturn(Optional.of(testSalesperson));
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID))
                .thenReturn(Optional.of(testCustomer));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Email service down")));

        assertDoesNotThrow(() -> formService.reopenForm("test-form-id-123", reopenRequest, "auth0|salesperson-456"));

        verify(mailerServiceClient).sendEmail(anyString(), anyString(), anyString(), anyString());
    }

    // ========== Tests for stream lambdas in createAndAssignForm ==========

    @Test
    void createAndAssignForm_WithMixedUsersIncludingBothSalespersonAndCustomer_Succeeds() {
        // Add extra users to the lot to test the stream lambdas properly
        Users extraUser1 = new Users();
        extraUser1.setUserIdentifier(UserIdentifier.fromString(EXTRA_USER_1_UUID));
        
        Users extraUser2 = new Users();
        extraUser2.setUserIdentifier(UserIdentifier.fromString(EXTRA_USER_2_UUID));
        
        // Set the lot with multiple users including the salesperson and customer
        testLot.setAssignedUsers(Arrays.asList(extraUser1, testCustomer, extraUser2, testSalesperson));

        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);
        when(formRepository.existsByProjectIdentifierAndLotIdentifierAndCustomerIdAndFormType(
                any(), any(), any(), any())).thenReturn(false);
        when(formMapper.requestModelToEntity(any())).thenReturn(testForm);
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act - should succeed because both salesperson and customer are in the list
        FormResponseModel result = formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456");

        // Assert
        assertNotNull(result);
        verify(formRepository).save(any(Form.class));
    }

    @Test
    void createAndAssignForm_WithOnlyCustomerInLot_ThrowsInvalidInputException() {
        // Test when only customer is assigned but not salesperson
        testLot.setAssignedUsers(Arrays.asList(testCustomer));

        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);

        assertThrows(InvalidInputException.class, () ->
                formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456")
        );

        verify(formRepository, never()).save(any());
    }

    @Test
    void createAndAssignForm_WithMultipleUsersButNoMatches_ThrowsInvalidInputException() {
        // Create users with different IDs
        Users otherUser1 = new Users();
        otherUser1.setUserIdentifier(UserIdentifier.fromString(OTHER_USER_1_UUID));
        
        Users otherUser2 = new Users();
        otherUser2.setUserIdentifier(UserIdentifier.fromString(OTHER_USER_2_UUID));
        
        // Lot has users but neither is the salesperson nor customer
        testLot.setAssignedUsers(Arrays.asList(otherUser1, otherUser2));

        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);

        assertThrows(InvalidInputException.class, () ->
                formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456")
        );

        verify(formRepository, never()).save(any());
    }

    @Test
    void updateFormData_WithInProgressStatus_RemainsInProgress() {
        testForm.setFormStatus(FormStatus.IN_PROGRESS);
        Map<String, Object> newData = new HashMap<>();
        newData.put("field", "value");

        FormDataUpdateRequestModel updateRequest = FormDataUpdateRequestModel.builder()
                .formData(newData)
                .isSubmitting(false)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);

        formService.updateFormData("test-form-id-123", updateRequest, CUSTOMER_UUID);

        verify(formRepository).save(argThat(form -> form.getFormStatus() == FormStatus.IN_PROGRESS));
    }

    @Test
    void updateFormData_WithSubmittedStatus_RemainsSubmitted() {
        testForm.setFormStatus(FormStatus.SUBMITTED);
        Map<String, Object> newData = new HashMap<>();
        newData.put("field", "value");

        FormDataUpdateRequestModel updateRequest = FormDataUpdateRequestModel.builder()
                .formData(newData)
                .isSubmitting(false)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);

        formService.updateFormData("test-form-id-123", updateRequest, CUSTOMER_UUID);

        verify(formRepository).save(argThat(form -> form.getFormStatus() == FormStatus.SUBMITTED));
    }

    @Test
    void reopenForm_WithoutNewInstructions_DoesNotUpdateInstructions() {
        testForm.setFormStatus(FormStatus.SUBMITTED);
        String originalInstructions = "Original instructions";
        testForm.setInstructions(originalInstructions);
        
        FormReopenRequestModel reopenRequest = FormReopenRequestModel.builder()
                .reopenReason("Need changes")
                .newInstructions(null)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456"))
                .thenReturn(Optional.of(testSalesperson));
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID))
                .thenReturn(Optional.of(testCustomer));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        formService.reopenForm("test-form-id-123", reopenRequest, "auth0|salesperson-456");

        verify(formRepository).save(argThat(form ->
                originalInstructions.equals(form.getInstructions())
        ));
    }

    @Test
    void createSubmissionHistoryEntry_WithNullCustomer_UsesUnknownCustomer() {
        FormDataUpdateRequestModel submitRequest = FormDataUpdateRequestModel.builder()
                .formData(new HashMap<>())
                .submissionNotes("Done")
                .isSubmitting(true)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.empty());
        when(usersRepository.findByUserIdentifier(SALESPERSON_UUID)).thenReturn(Optional.of(testSalesperson));
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(historyRepository.countByFormIdentifier(any())).thenReturn(0L);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        formService.submitForm("test-form-id-123", submitRequest, CUSTOMER_UUID);

        verify(historyRepository).save(argThat(history ->
                "Unknown Customer".equals(history.getSubmittedByCustomerName())
        ));
    }

    @Test
    void submitForm_WhereCustomerIsNull_UsesFormCustomerName() {
        FormDataUpdateRequestModel submitRequest = FormDataUpdateRequestModel.builder()
                .formData(new HashMap<>())
                .submissionNotes("Done")
                .isSubmitting(true)
                .build();

        testForm.setCustomerName("Form Customer Name");

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.empty());
        when(usersRepository.findByUserIdentifier(SALESPERSON_UUID)).thenReturn(Optional.of(testSalesperson));
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(historyRepository.countByFormIdentifier(any())).thenReturn(0L);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        formService.submitForm("test-form-id-123", submitRequest, CUSTOMER_UUID);

        verify(mailerServiceClient).sendEmail(
                anyString(),
                argThat(subject -> subject.contains("Form Customer Name")),
                argThat(body -> body.contains("Form Customer Name")),
                anyString()
        );
    }

    @Test
    void buildFormAssignedEmailBody_WithEmptyInstructions_DoesNotShowInstructionsSection() {
        testForm.setInstructions("");

        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);
        when(formRepository.existsByProjectIdentifierAndLotIdentifierAndCustomerIdAndFormType(
                any(), any(), any(), any())).thenReturn(false);
        when(formMapper.requestModelToEntity(any())).thenReturn(testForm);
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456");

        verify(mailerServiceClient).sendEmail(
                anyString(),
                anyString(),
                argThat(body -> !body.contains("<p><strong>Instructions:</strong></p>")),
                anyString()
        );
    }

    @Test
    void buildFormReopenedEmailBody_WithEmptyInstructions_DoesNotShowInstructionsSection() {
        testForm.setFormStatus(FormStatus.SUBMITTED);
        testForm.setInstructions("");
        FormReopenRequestModel reopenRequest = FormReopenRequestModel.builder()
                .reopenReason("Please review")
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456"))
                .thenReturn(Optional.of(testSalesperson));
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID))
                .thenReturn(Optional.of(testCustomer));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        formService.reopenForm("test-form-id-123", reopenRequest, "auth0|salesperson-456");

        verify(mailerServiceClient).sendEmail(
                anyString(),
                anyString(),
                argThat(body -> !body.contains("<p><strong>Updated Instructions:</strong></p>")),
                anyString()
        );
    }

    // ========== Notification Error Handling Tests ==========

    @Test
    void sendFormAssignedNotification_WithEmailFailure_LogsErrorButContinues() {
        // Arrange
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);
        when(formRepository.existsByProjectIdentifierAndLotIdentifierAndCustomerIdAndFormType(
                any(), any(), any(), any())).thenReturn(false);
        when(formMapper.requestModelToEntity(any())).thenReturn(testForm);
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        
        // Mock email failure
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Email service unavailable")));

        // Act - should not throw exception despite email failure
        FormResponseModel result = formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456");

        // Assert
        assertNotNull(result);
        verify(mailerServiceClient).sendEmail(anyString(), anyString(), anyString(), anyString());
        verify(notificationService).createNotification(any(UUID.class), anyString(), anyString(), any(), anyString());
    }

    @Test
    void sendFormSubmittedNotification_WithMissingAssigner_LogsWarning() {
        // Arrange
        testForm.setAssignedByUserId("non-existent-user");
        FormDataUpdateRequestModel submitRequest = FormDataUpdateRequestModel.builder()
                .formData(new HashMap<>())
                .submissionNotes("Test submission")
                .isSubmitting(true)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByUserIdentifier("non-existent-user")).thenReturn(Optional.empty());
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(historyRepository.countByFormIdentifier(any())).thenReturn(0L);

        // Act - should not throw exception despite missing assigner
        FormResponseModel result = formService.submitForm("test-form-id-123", submitRequest, CUSTOMER_UUID);

        // Assert
        assertNotNull(result);
        verify(historyRepository).save(any(FormSubmissionHistory.class));
        // Notification should not be sent if assigner is not found
        verify(notificationService, never()).createNotification(any(UUID.class), anyString(), anyString(), any(), anyString());
    }

    @Test
    void sendFormReopenedNotification_WithNullCustomer_LogsWarning() {
        // Arrange
        testForm.setFormStatus(FormStatus.SUBMITTED);
        FormReopenRequestModel reopenRequest = FormReopenRequestModel.builder()
                .reopenReason("Need changes")
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456"))
                .thenReturn(Optional.of(testSalesperson));
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID))
                .thenReturn(Optional.empty());
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);

        // Act - should not throw exception despite missing customer
        FormResponseModel result = formService.reopenForm("test-form-id-123", reopenRequest, "auth0|salesperson-456");

        // Assert
        assertNotNull(result);
        verify(formRepository).save(any(Form.class));
        // Notification should not be sent if customer is not found
        verify(notificationService, never()).createNotification(any(UUID.class), anyString(), anyString(), any(), anyString());
    }

    @Test
    void submitForm_CreatesCorrectSubmissionHistoryEntry() {
        // Arrange
        Map<String, Object> formData = new HashMap<>();
        formData.put("color", "Blue");
        formData.put("size", "Large");
        
        FormDataUpdateRequestModel submitRequest = FormDataUpdateRequestModel.builder()
                .formData(formData)
                .submissionNotes("All selections are final")
                .isSubmitting(true)
                .build();

        testForm.setFormData(formData);
        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByUserIdentifier(SALESPERSON_UUID)).thenReturn(Optional.of(testSalesperson));
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(historyRepository.countByFormIdentifier(any())).thenReturn(2L); // Third submission
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        FormResponseModel result = formService.submitForm("test-form-id-123", submitRequest, CUSTOMER_UUID);

        // Assert
        assertNotNull(result);
        verify(historyRepository).save(argThat(history ->
                history.getSubmissionNumber() == 3 &&
                history.getFormIdentifier().equals("test-form-id-123") &&
                "All selections are final".equals(history.getSubmissionNotes()) &&
                history.getStatusAtSubmission() == FormStatus.SUBMITTED &&
                history.getSubmittedByCustomerId().equals(CUSTOMER_UUID) &&
                history.getSubmittedByCustomerName().equals("John Customer") &&
                history.getFormDataSnapshot().containsKey("color") &&
                history.getFormDataSnapshot().get("color").equals("Blue")
        ));
    }

    @Test
    void createAndAssignForm_WithEmptyInstructions_AssignsFormSuccessfully() {
        // Arrange
        testRequestModel = FormRequestModel.builder()
                .formType(FormType.WINDOWS)
                .projectIdentifier("project-123")
                .lotIdentifier(testLot.getLotIdentifier().getLotId().toString())
                .customerId(CUSTOMER_UUID)
                .formTitle("Window Selection")
                .instructions("")
                .formData(new HashMap<>())
                .build();

        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(projectRepository.findByProjectIdentifier("project-123")).thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotIdWithUsers(any(UUID.class))).thenReturn(testLot);
        when(formRepository.existsByProjectIdentifierAndLotIdentifierAndCustomerIdAndFormType(
                any(), any(), any(), any())).thenReturn(false);
        when(formMapper.requestModelToEntity(any())).thenReturn(testForm);
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        FormResponseModel result = formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456");

        // Assert
        assertNotNull(result);
        verify(formRepository).save(any(Form.class));
    }

    @Test
    void reopenForm_WithEmptyNewInstructions_DoesNotUpdateInstructions() {
        // Arrange
        testForm.setFormStatus(FormStatus.SUBMITTED);
        String originalInstructions = "Original instructions";
        testForm.setInstructions(originalInstructions);
        
        FormReopenRequestModel reopenRequest = FormReopenRequestModel.builder()
                .reopenReason("Need changes")
                .newInstructions("")
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456"))
                .thenReturn(Optional.of(testSalesperson));
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID))
                .thenReturn(Optional.of(testCustomer));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        FormResponseModel result = formService.reopenForm("test-form-id-123", reopenRequest, "auth0|salesperson-456");

        // Assert
        assertNotNull(result);
        verify(formRepository).save(argThat(form ->
                form.getFormStatus() == FormStatus.REOPENED &&
                form.getReopenCount() == 1
        ));
    }

    // ========== Tests for Viewing Submitted Forms (Lot Documents Page Functionality) ==========

    @Test
    void getFormById_SubmittedForm_ReturnsFormWithCustomerData() {
        // Arrange
        Map<String, Object> customerFormData = new HashMap<>();
        customerFormData.put("pdfFile", Map.of("fileId", "file-123", "fileName", "selection.pdf"));
        customerFormData.put("additionalNotes", "Customer notes here");
        
        testForm.setFormStatus(FormStatus.SUBMITTED);
        testForm.setFormData(customerFormData);
        testForm.setSubmittedAt(LocalDateTime.now());
        
        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        
        testResponseModel = FormResponseModel.builder()
                .formId("test-form-id-123")
                .formType(FormType.GARAGE_DOORS)
                .formStatus(FormStatus.SUBMITTED)
                .formData(customerFormData)
                .build();
        
        when(formMapper.entityToResponseModel(testForm)).thenReturn(testResponseModel);

        // Act
        FormResponseModel result = formService.getFormById("test-form-id-123");

        // Assert
        assertNotNull(result);
        assertEquals(FormStatus.SUBMITTED, result.getFormStatus());
        assertNotNull(result.getFormData());
        assertEquals("Customer notes here", result.getFormData().get("additionalNotes"));
        verify(formRepository).findByFormIdentifier_FormId("test-form-id-123");
        verify(formMapper).entityToResponseModel(testForm);
    }

    @Test
    void getFormById_SubmittedForm_PreservesAllFormData() {
        // Arrange
        Map<String, Object> complexFormData = new HashMap<>();
        complexFormData.put("exteriorColor", "Navy Blue");
        complexFormData.put("interiorColor", "Warm White");
        complexFormData.put("trim", "Maple");
        complexFormData.put("specialNotes", "Custom finish requested");
        
        testForm.setFormStatus(FormStatus.SUBMITTED);
        testForm.setFormData(complexFormData);
        
        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        
        testResponseModel = FormResponseModel.builder()
                .formId("test-form-id-123")
                .formType(FormType.PAINT)
                .formStatus(FormStatus.SUBMITTED)
                .formData(complexFormData)
                .build();
        
        when(formMapper.entityToResponseModel(testForm)).thenReturn(testResponseModel);

        // Act
        FormResponseModel result = formService.getFormById("test-form-id-123");

        // Assert
        assertNotNull(result);
        assertEquals(4, result.getFormData().size());
        assertEquals("Navy Blue", result.getFormData().get("exteriorColor"));
        assertEquals("Warm White", result.getFormData().get("interiorColor"));
        assertEquals("Maple", result.getFormData().get("trim"));
        assertEquals("Custom finish requested", result.getFormData().get("specialNotes"));
    }

    @Test
    void updateFormData_SubmittedForm_ThrowsInvalidInputException() {
        // Arrange
        testForm.setFormStatus(FormStatus.SUBMITTED);
        Map<String, Object> newFormData = Map.of("newField", "newValue");
        
        FormUpdateRequestModel updateRequest = FormUpdateRequestModel.builder()
                .formData(newFormData)
                .build();
        
        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));

        // Act & Assert
        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> formService.updateFormData("test-form-id-123", updateRequest)
        );
        
        assertTrue(exception.getMessage().contains("Cannot update form data for a submitted form"));
        verify(formRepository, never()).save(any());
    }

    @Test
    void updateFormData_CompletedForm_ThrowsInvalidInputException() {
        // Arrange
        testForm.setFormStatus(FormStatus.COMPLETED);
        Map<String, Object> newFormData = Map.of("field", "value");
        
        FormUpdateRequestModel updateRequest = FormUpdateRequestModel.builder()
                .formData(newFormData)
                .build();
        
        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));

        // Act & Assert
        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> formService.updateFormData("test-form-id-123", updateRequest)
        );
        
        assertTrue(exception.getMessage().contains("Cannot update form data for a completed form") ||
                   exception.getMessage().contains("Cannot update form data for a submitted form"));
        verify(formRepository, never()).save(any());
    }

    @Test
    void updateFormData_AssignedForm_AllowsUpdate() {
        // Arrange
        testForm.setFormStatus(FormStatus.ASSIGNED);
        Map<String, Object> newFormData = Map.of("doorModel", "Model X");
        Map<String, Object> originalData = new HashMap<>();
        testForm.setFormData(originalData);
        
        FormUpdateRequestModel updateRequest = FormUpdateRequestModel.builder()
                .formData(newFormData)
                .build();
        
        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        
        testResponseModel = FormResponseModel.builder()
                .formId("test-form-id-123")
                .formStatus(FormStatus.IN_PROGRESS)
                .formData(newFormData)
                .build();
        
        when(formMapper.entityToResponseModel(any(Form.class))).thenReturn(testResponseModel);

        // Act
        FormResponseModel result = formService.updateFormData("test-form-id-123", updateRequest);

        // Assert
        assertNotNull(result);
        verify(formRepository).save(argThat(form ->
                form.getFormStatus() == FormStatus.IN_PROGRESS &&
                form.getFormData().get("doorModel").equals("Model X")
        ));
    }

    @Test
    void updateFormData_InProgressForm_AllowsUpdate() {
        // Arrange
        testForm.setFormStatus(FormStatus.IN_PROGRESS);
        Map<String, Object> existingData = new HashMap<>(Map.of("field1", "value1"));
        testForm.setFormData(existingData);
        
        Map<String, Object> newFormData = Map.of("field1", "updated", "field2", "new");
        FormUpdateRequestModel updateRequest = FormUpdateRequestModel.builder()
                .formData(newFormData)
                .build();
        
        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        
        testResponseModel = FormResponseModel.builder()
                .formId("test-form-id-123")
                .formStatus(FormStatus.IN_PROGRESS)
                .formData(newFormData)
                .build();
        
        when(formMapper.entityToResponseModel(any(Form.class))).thenReturn(testResponseModel);

        // Act
        FormResponseModel result = formService.updateFormData("test-form-id-123", updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("updated", result.getFormData().get("field1"));
        assertEquals("new", result.getFormData().get("field2"));
        verify(formRepository).save(any(Form.class));
    }

    @Test
    void updateFormData_ReopenedForm_AllowsUpdate() {
        // Arrange
        testForm.setFormStatus(FormStatus.REOPENED);
        Map<String, Object> formData = new HashMap<>();
        testForm.setFormData(formData);
        
        Map<String, Object> newFormData = Map.of("correctedField", "new value");
        FormUpdateRequestModel updateRequest = FormUpdateRequestModel.builder()
                .formData(newFormData)
                .build();
        
        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        
        testResponseModel = FormResponseModel.builder()
                .formId("test-form-id-123")
                .formStatus(FormStatus.REOPENED)
                .formData(newFormData)
                .build();
        
        when(formMapper.entityToResponseModel(any(Form.class))).thenReturn(testResponseModel);

        // Act
        FormResponseModel result = formService.updateFormData("test-form-id-123", updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("new value", result.getFormData().get("correctedField"));
        verify(formRepository).save(any(Form.class));
    }

    // ========== getAllForms Tests ==========

    @Test
    void getAllForms_ReturnsAllFormsInRepository() {
        // Arrange
        Form form2 = new Form();
        form2.setFormIdentifier(new FormIdentifier("form-2"));
        form2.setFormType(FormType.PAINT);

        FormResponseModel response2 = FormResponseModel.builder()
                .formId("form-2")
                .formType(FormType.PAINT)
                .build();

        when(formRepository.findAll()).thenReturn(Arrays.asList(testForm, form2));
        when(formMapper.entityToResponseModel(testForm)).thenReturn(testResponseModel);
        when(formMapper.entityToResponseModel(form2)).thenReturn(response2);

        // Act
        List<FormResponseModel> result = formService.getAllForms();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(formRepository).findAll();
    }

    @Test
    void getAllForms_WhenEmpty_ReturnsEmptyList() {
        // Arrange
        when(formRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<FormResponseModel> result = formService.getAllForms();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(formRepository).findAll();
    }

    // ========== submitForm formData Preservation Tests ==========

    @Test
    void submitForm_WithEmptyFormData_PreservesExistingFormData() {
        // Arrange - form already has saved data
        Map<String, Object> existingData = new HashMap<>();
        existingData.put("windowColor", "Blue");
        existingData.put("doorStyle", "Modern");
        testForm.setFormData(existingData);
        testForm.setFormStatus(FormStatus.IN_PROGRESS);

        // Submit request has empty formData (simulating frontend submitForm call)
        FormDataUpdateRequestModel submitRequest = FormDataUpdateRequestModel.builder()
                .formData(new HashMap<>())
                .isSubmitting(true)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(formMapper.entityToResponseModel(any(Form.class))).thenReturn(testResponseModel);

        // Act
        formService.submitForm("test-form-id-123", submitRequest, CUSTOMER_UUID);

        // Assert - existing data should be preserved
        assertEquals(existingData, testForm.getFormData());
        assertEquals("Blue", testForm.getFormData().get("windowColor"));
        assertEquals("Modern", testForm.getFormData().get("doorStyle"));
    }

    @Test
    void submitForm_WithNullFormData_PreservesExistingFormData() {
        // Arrange
        Map<String, Object> existingData = new HashMap<>();
        existingData.put("color", "Red");
        testForm.setFormData(existingData);
        testForm.setFormStatus(FormStatus.IN_PROGRESS);

        FormDataUpdateRequestModel submitRequest = FormDataUpdateRequestModel.builder()
                .formData(null)
                .isSubmitting(true)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(formMapper.entityToResponseModel(any(Form.class))).thenReturn(testResponseModel);

        // Act
        formService.submitForm("test-form-id-123", submitRequest, CUSTOMER_UUID);

        // Assert - existing data should be preserved
        assertEquals("Red", testForm.getFormData().get("color"));
    }

    @Test
    void submitForm_WithNonEmptyFormData_UpdatesFormData() {
        // Arrange
        Map<String, Object> existingData = new HashMap<>();
        existingData.put("color", "Red");
        testForm.setFormData(existingData);
        testForm.setFormStatus(FormStatus.IN_PROGRESS);

        Map<String, Object> newData = new HashMap<>();
        newData.put("color", "Blue");
        newData.put("size", "Large");

        FormDataUpdateRequestModel submitRequest = FormDataUpdateRequestModel.builder()
                .formData(newData)
                .isSubmitting(true)
                .build();

        when(formRepository.findByFormIdentifier_FormId("test-form-id-123"))
                .thenReturn(Optional.of(testForm));
        when(formRepository.save(any(Form.class))).thenReturn(testForm);
        when(usersRepository.findByUserIdentifier(CUSTOMER_UUID)).thenReturn(Optional.of(testCustomer));
        when(formMapper.entityToResponseModel(any(Form.class))).thenReturn(testResponseModel);

        // Act
        formService.submitForm("test-form-id-123", submitRequest, CUSTOMER_UUID);

        // Assert - data should be updated
        assertEquals(newData, testForm.getFormData());
        assertEquals("Blue", testForm.getFormData().get("color"));
        assertEquals("Large", testForm.getFormData().get("size"));
    }
}


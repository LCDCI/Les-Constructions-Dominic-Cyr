package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.MailerServiceClient;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.NotificationService;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.BusinessLayer.FormServiceImpl;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.*;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.MapperLayer.FormMapper;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.MapperLayer.FormSubmissionHistoryMapper;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer.*;
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

    @BeforeEach
    void setUp() {
        // Setup test form identifier
        testFormIdentifier = new FormIdentifier("test-form-id-123");

        // Setup test users
        testCustomer = new Users();
        testCustomer.setUserIdentifier(new UserIdentifier("customer-id-123"));
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Customer");
        testCustomer.setPrimaryEmail("john.customer@example.com");

        testSalesperson = new Users();
        testSalesperson.setUserIdentifier(new UserIdentifier("salesperson-id-456"));
        testSalesperson.setAuth0UserId("auth0|salesperson-456");
        testSalesperson.setFirstName("Jane");
        testSalesperson.setLastName("Salesperson");
        testSalesperson.setPrimaryEmail("jane.salesperson@example.com");

        // Setup test request model
        testRequestModel = FormRequestModel.builder()
                .formType(FormType.WINDOWS)
                .projectIdentifier("project-123")
                .customerId("customer-id-123")
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
        testForm.setCustomerId("customer-id-123");
        testForm.setCustomerName("John Customer");
        testForm.setCustomerEmail("john.customer@example.com");
        testForm.setAssignedByUserId("salesperson-id-456");
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
                .customerId("customer-id-123")
                .customerName("John Customer")
                .customerEmail("john.customer@example.com")
                .assignedByUserId("salesperson-id-456")
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
        when(usersRepository.findByUserIdentifier("customer-id-123")).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(formRepository.existsByProjectIdentifierAndCustomerIdAndFormType(
                "project-123", "customer-id-123", FormType.WINDOWS)).thenReturn(false);
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
        verify(notificationService).createNotification(anyString(), anyString(), anyString(), any(), anyString());
        verify(mailerServiceClient).sendEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void createAndAssignForm_WithNonExistentCustomer_ThrowsNotFoundException() {
        // Arrange
        when(usersRepository.findByUserIdentifier("customer-id-123")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                formService.createAndAssignForm(testRequestModel, "auth0|salesperson-456")
        );

        verify(formRepository, never()).save(any());
    }

    @Test
    void createAndAssignForm_WithDuplicateFormType_ThrowsInvalidInputException() {
        // Arrange
        when(usersRepository.findByUserIdentifier("customer-id-123")).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByAuth0UserId("auth0|salesperson-456")).thenReturn(Optional.of(testSalesperson));
        when(formRepository.existsByProjectIdentifierAndCustomerIdAndFormType(
                "project-123", "customer-id-123", FormType.WINDOWS)).thenReturn(true);

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
        FormResponseModel result = formService.updateFormData("test-form-id-123", updateRequest, "customer-id-123");

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
                formService.updateFormData("test-form-id-123", updateRequest, "customer-id-123")
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
        when(usersRepository.findByUserIdentifier("customer-id-123")).thenReturn(Optional.of(testCustomer));
        when(usersRepository.findByUserIdentifier("salesperson-id-456")).thenReturn(Optional.of(testSalesperson));
        when(formMapper.entityToResponseModel(any())).thenReturn(testResponseModel);
        when(historyRepository.countByFormIdentifier(any())).thenReturn(0L);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        FormResponseModel result = formService.submitForm("test-form-id-123", submitRequest, "customer-id-123");

        // Assert
        assertNotNull(result);
        verify(formRepository).save(any(Form.class));
        verify(historyRepository).save(any(FormSubmissionHistory.class));
        verify(notificationService).createNotification(anyString(), anyString(), anyString(), any(), anyString());
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
                formService.submitForm("test-form-id-123", submitRequest, "customer-id-123")
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
        when(usersRepository.findByUserIdentifier("customer-id-123"))
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
        verify(notificationService).createNotification(anyString(), anyString(), anyString(), any(), anyString());
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
                "project-123", "customer-id-123", FormType.WINDOWS)).thenReturn(true);

        // Act
        boolean result = formService.hasFormOfType("project-123", "customer-id-123", FormType.WINDOWS);

        // Assert
        assertTrue(result);
    }

    @Test
    void hasFormOfType_WhenFormDoesNotExist_ReturnsFalse() {
        // Arrange
        when(formRepository.existsByProjectIdentifierAndCustomerIdAndFormType(
                "project-123", "customer-id-123", FormType.PAINT)).thenReturn(false);

        // Act
        boolean result = formService.hasFormOfType("project-123", "customer-id-123", FormType.PAINT);

        // Assert
        assertFalse(result);
    }
}

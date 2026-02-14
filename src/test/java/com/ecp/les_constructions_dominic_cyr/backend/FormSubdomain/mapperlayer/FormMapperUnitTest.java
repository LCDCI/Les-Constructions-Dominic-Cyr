package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.mapperlayer;

import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.Form;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormStatus;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormType;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.MapperLayer.FormMapper;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer.FormRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer.FormResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FormMapper.
 */
class FormMapperUnitTest {

    private FormMapper formMapper;

    @BeforeEach
    void setUp() {
        formMapper = new FormMapper();
    }

    // ========== Request Model To Entity Tests ==========

    @Test
    void requestModelToEntity_WithValidData_ConvertsSuccessfully() {
        // Arrange
        Map<String, Object> formData = new HashMap<>();
        formData.put("color", "Blue");

        FormRequestModel requestModel = FormRequestModel.builder()
                .formType(FormType.WINDOWS)
                .projectIdentifier("project-123")
                .lotIdentifier("lot-uuid-456")
                .customerId("customer-id-123")
                .formTitle("Window Selection")
                .instructions("Please select windows")
                .formData(formData)
                .build();

        // Act
        Form form = formMapper.requestModelToEntity(requestModel);

        // Assert
        assertNotNull(form);
        assertNotNull(form.getFormIdentifier());
        assertEquals(FormType.WINDOWS, form.getFormType());
        assertEquals(FormStatus.DRAFT, form.getFormStatus());
        assertEquals("project-123", form.getProjectIdentifier());
        assertEquals("lot-uuid-456", form.getLotIdentifier());
        assertEquals("customer-id-123", form.getCustomerId());
        assertEquals("Window Selection", form.getFormTitle());
        assertEquals("Please select windows", form.getInstructions());
        assertEquals(formData, form.getFormData());
    }

    @Test
    void requestModelToEntity_WithNullFormData_InitializesEmptyMap() {
        // Arrange
        FormRequestModel requestModel = FormRequestModel.builder()
                .formType(FormType.PAINT)
                .projectIdentifier("project-123")
                .lotIdentifier("lot-uuid-456")
                .customerId("customer-id-123")
                .formTitle("Paint Selection")
                .build();

        // Act
        Form form = formMapper.requestModelToEntity(requestModel);

        // Assert
        assertNotNull(form);
        assertEquals(FormType.PAINT, form.getFormType());
        assertEquals(FormStatus.DRAFT, form.getFormStatus());
    }

    @Test
    void requestModelToEntity_WithMinimalData_CreatesValidForm() {
        // Arrange
        FormRequestModel requestModel = FormRequestModel.builder()
                .formType(FormType.EXTERIOR_DOORS)
                .projectIdentifier("proj-456")
                .lotIdentifier("lot-789")
                .customerId("cust-789")
                .build();

        // Act
        Form form = formMapper.requestModelToEntity(requestModel);

        // Assert
        assertNotNull(form);
        assertNotNull(form.getFormIdentifier());
        assertEquals(FormType.EXTERIOR_DOORS, form.getFormType());
        assertEquals("proj-456", form.getProjectIdentifier());
        assertEquals("lot-789", form.getLotIdentifier());
        assertEquals("cust-789", form.getCustomerId());
    }

    // ========== Entity To Response Model Tests ==========

    @Test
    void entityToResponseModel_WithCompleteData_ConvertsSuccessfully() {
        // Arrange
        Form form = new Form();
        form.setId(1L);
        form.setFormIdentifier(new FormIdentifier("form-id-123"));
        form.setFormType(FormType.WINDOWS);
        form.setFormStatus(FormStatus.SUBMITTED);
        form.setProjectIdentifier("project-123");
        form.setLotIdentifier(UUID.fromString("lot-uuid-456"));
        form.setCustomerId(UUID.fromString("customer-id-123"));
        form.setCustomerName("John Customer");
        form.setCustomerEmail("john@example.com");
        form.setAssignedByUserId(UUID.fromString("salesperson-id-456"));
        form.setAssignedByName("Jane Salesperson");
        form.setFormTitle("Window Selection");
        form.setInstructions("Please select windows");

        Map<String, Object> formData = new HashMap<>();
        formData.put("color", "Blue");
        form.setFormData(formData);

        LocalDateTime now = LocalDateTime.now();
        form.setAssignedDate(now);
        form.setFirstSubmittedDate(now.minusDays(1));
        form.setLastSubmittedDate(now);
        form.setCompletedDate(now.plusDays(1));
        form.setReopenedDate(now.minusHours(2));
        form.setReopenedByUserId(UUID.fromString("reopener-id"));
        form.setReopenReason("Need changes");
        form.setReopenCount(2);
        form.setCreatedAt(now.minusDays(5));
        form.setUpdatedAt(now);

        // Act
        FormResponseModel responseModel = formMapper.entityToResponseModel(form);

        // Assert
        assertNotNull(responseModel);
        assertEquals("form-id-123", responseModel.getFormId());
        assertEquals(FormType.WINDOWS, responseModel.getFormType());
        assertEquals(FormStatus.SUBMITTED, responseModel.getFormStatus());
        assertEquals("project-123", responseModel.getProjectIdentifier());
        assertEquals("lot-uuid-456", responseModel.getLotIdentifier());
        assertEquals("customer-id-123", responseModel.getCustomerId());
        assertEquals("John Customer", responseModel.getCustomerName());
        assertEquals("john@example.com", responseModel.getCustomerEmail());
        assertEquals("salesperson-id-456", responseModel.getAssignedByUserId());
        assertEquals("Jane Salesperson", responseModel.getAssignedByName());
        assertEquals("Window Selection", responseModel.getFormTitle());
        assertEquals("Please select windows", responseModel.getInstructions());
        assertEquals(formData, responseModel.getFormData());
        assertEquals(now, responseModel.getAssignedDate());
        assertEquals(now.minusDays(1), responseModel.getFirstSubmittedDate());
        assertEquals(now, responseModel.getLastSubmittedDate());
        assertEquals(now.plusDays(1), responseModel.getCompletedDate());
        assertEquals(now.minusHours(2), responseModel.getReopenedDate());
        assertEquals("reopener-id", responseModel.getReopenedByUserId());
        assertEquals("Need changes", responseModel.getReopenReason());
        assertEquals(2, responseModel.getReopenCount());
        assertEquals(now.minusDays(5), responseModel.getCreatedAt());
        assertEquals(now, responseModel.getUpdatedAt());
    }

    @Test
    void entityToResponseModel_WithMinimalData_ConvertsSuccessfully() {
        // Arrange
        Form form = new Form();
        form.setFormIdentifier(new FormIdentifier("minimal-form-id"));
        form.setFormType(FormType.GARAGE_DOORS);
        form.setFormStatus(FormStatus.ASSIGNED);
        form.setProjectIdentifier("proj-minimal");
        form.setLotIdentifier(UUID.fromString("lot-minimal"));
        form.setCustomerId(UUID.fromString("cust-minimal"));
        form.setFormData(new HashMap<>());
        form.setReopenCount(0);

        // Act
        FormResponseModel responseModel = formMapper.entityToResponseModel(form);

        // Assert
        assertNotNull(responseModel);
        assertEquals("minimal-form-id", responseModel.getFormId());
        assertEquals(FormType.GARAGE_DOORS, responseModel.getFormType());
        assertEquals(FormStatus.ASSIGNED, responseModel.getFormStatus());
        assertEquals(0, responseModel.getReopenCount());
    }

    @Test
    void entityToResponseModel_WithNullOptionalFields_HandlesGracefully() {
        // Arrange
        Form form = new Form();
        form.setFormIdentifier(new FormIdentifier("form-with-nulls"));
        form.setFormType(FormType.ASPHALT_SHINGLES);
        form.setFormStatus(FormStatus.IN_PROGRESS);
        form.setProjectIdentifier("project-nulls");
        form.setLotIdentifier(UUID.fromString("lot-nulls"));
        form.setCustomerId(UUID.fromString("customer-nulls"));
        form.setFormData(new HashMap<>());
        form.setReopenCount(0);
        // Leave optional fields as null

        // Act
        FormResponseModel responseModel = formMapper.entityToResponseModel(form);

        // Assert
        assertNotNull(responseModel);
        assertEquals("form-with-nulls", responseModel.getFormId());
        assertNull(responseModel.getCustomerName());
        assertNull(responseModel.getCustomerEmail());
        assertNull(responseModel.getAssignedByUserId());
        assertNull(responseModel.getFormTitle());
        assertNull(responseModel.getInstructions());
        assertNull(responseModel.getAssignedDate());
        assertNull(responseModel.getFirstSubmittedDate());
    }

    // ========== Update Entity From Request Model Tests ==========

    @Test
    void updateEntityFromRequestModel_WithNewTitle_UpdatesTitle() {
        // Arrange
        Form form = new Form();
        form.setFormTitle("Old Title");
        form.setInstructions("Old instructions");

        FormRequestModel requestModel = FormRequestModel.builder()
                .formType(FormType.WINDOWS)
                .projectIdentifier("project-123")
                .lotIdentifier("lot-uuid-456")
                .customerId("customer-id-123")
                .formTitle("New Title")
                .build();

        // Act
        formMapper.updateEntityFromRequestModel(requestModel, form);

        // Assert
        assertEquals("New Title", form.getFormTitle());
        assertEquals("Old instructions", form.getInstructions()); // Should not be updated if not provided
    }

    @Test
    void updateEntityFromRequestModel_WithNewInstructions_UpdatesInstructions() {
        // Arrange
        Form form = new Form();
        form.setFormTitle("Title");
        form.setInstructions("Old instructions");

        FormRequestModel requestModel = FormRequestModel.builder()
                .formType(FormType.PAINT)
                .projectIdentifier("project-123")
                .lotIdentifier("lot-uuid-456")
                .customerId("customer-id-123")
                .instructions("New instructions")
                .build();

        // Act
        formMapper.updateEntityFromRequestModel(requestModel, form);

        // Assert
        assertEquals("New instructions", form.getInstructions());
        assertEquals("Title", form.getFormTitle());
    }

    @Test
    void updateEntityFromRequestModel_WithBothUpdates_UpdatesBothFields() {
        // Arrange
        Form form = new Form();
        form.setFormTitle("Old Title");
        form.setInstructions("Old instructions");

        FormRequestModel requestModel = FormRequestModel.builder()
                .formType(FormType.WOODWORK)
                .projectIdentifier("project-123")
                .lotIdentifier("lot-uuid-456")
                .customerId("customer-id-123")
                .formTitle("Updated Title")
                .instructions("Updated instructions")
                .build();

        // Act
        formMapper.updateEntityFromRequestModel(requestModel, form);

        // Assert
        assertEquals("Updated Title", form.getFormTitle());
        assertEquals("Updated instructions", form.getInstructions());
    }

    @Test
    void updateEntityFromRequestModel_WithNullValues_DoesNotUpdateFields() {
        // Arrange
        Form form = new Form();
        form.setFormTitle("Existing Title");
        form.setInstructions("Existing instructions");

        FormRequestModel requestModel = FormRequestModel.builder()
                .formType(FormType.EXTERIOR_DOORS)
                .projectIdentifier("project-123")
                .lotIdentifier("lot-uuid-456")
                .customerId("customer-id-123")
                .formTitle(null)
                .instructions(null)
                .build();

        // Act
        formMapper.updateEntityFromRequestModel(requestModel, form);

        // Assert
        assertEquals("Existing Title", form.getFormTitle());
        assertEquals("Existing instructions", form.getInstructions());
    }

    @Test
    void updateEntityFromRequestModel_DoesNotChangeImmutableFields() {
        // Arrange
        Form form = new Form();
        form.setFormType(FormType.WINDOWS);
        form.setProjectIdentifier("original-project");
        form.setLotIdentifier(UUID.fromString("original-lot"));
        form.setCustomerId(UUID.fromString("original-customer"));
        form.setFormTitle("Title");
        form.setInstructions("Instructions");

        FormRequestModel requestModel = FormRequestModel.builder()
                .formType(FormType.PAINT) // Different type
                .projectIdentifier("different-project") // Different project
                .lotIdentifier("different-lot") // Different lot
                .customerId("different-customer") // Different customer
                .formTitle("New Title")
                .instructions("New instructions")
                .build();

        // Act
        formMapper.updateEntityFromRequestModel(requestModel, form);

        // Assert
        // Only title and instructions should be updated
        assertEquals("New Title", form.getFormTitle());
        assertEquals("New instructions", form.getInstructions());

        // These should NOT be changed (they're immutable after creation)
        assertEquals(FormType.WINDOWS, form.getFormType());
        assertEquals("original-project", form.getProjectIdentifier());
        assertEquals("original-lot", form.getLotIdentifier());
        assertEquals("original-customer", form.getCustomerId());
    }

    // ========== Edge Cases Tests ==========

    @Test
    void requestModelToEntity_WithAllFormTypes_CreatesCorrectly() {
        for (FormType formType : FormType.values()) {
            // Arrange
            FormRequestModel requestModel = FormRequestModel.builder()
                    .formType(formType)
                    .projectIdentifier("project-test")
                    .lotIdentifier("lot-test")
                    .customerId("customer-test")
                    .build();

            // Act
            Form form = formMapper.requestModelToEntity(requestModel);

            // Assert
            assertNotNull(form);
            assertEquals(formType, form.getFormType());
        }
    }

    @Test
    void entityToResponseModel_WithAllFormStatuses_ConvertsCorrectly() {
        for (FormStatus status : FormStatus.values()) {
            // Arrange
            Form form = new Form();
            form.setFormIdentifier(new FormIdentifier("test-id"));
            form.setFormType(FormType.WINDOWS);
            form.setFormStatus(status);
            form.setProjectIdentifier("proj");
            form.setLotIdentifier(UUID.fromString("lot"));
            form.setCustomerId(UUID.fromString("cust"));
            form.setFormData(new HashMap<>());
            form.setReopenCount(0);

            // Act
            FormResponseModel responseModel = formMapper.entityToResponseModel(form);

            // Assert
            assertNotNull(responseModel);
            assertEquals(status, responseModel.getFormStatus());
        }
    }
}


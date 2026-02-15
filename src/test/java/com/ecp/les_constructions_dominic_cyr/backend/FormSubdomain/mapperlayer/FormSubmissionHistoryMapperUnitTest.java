package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.mapperlayer;

import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormStatus;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer.FormSubmissionHistory;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.MapperLayer.FormSubmissionHistoryMapper;
import com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.PresentationLayer.FormSubmissionHistoryResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FormSubmissionHistoryMapper.
 */
class FormSubmissionHistoryMapperUnitTest {

    private static final UUID CUSTOMER_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");

    private FormSubmissionHistoryMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FormSubmissionHistoryMapper();
    }

    // ========== Entity To Response Model Tests ==========

    @Test
    void entityToResponseModel_WithCompleteData_ConvertsSuccessfully() {
        // Arrange
        Map<String, Object> formDataSnapshot = new HashMap<>();
        formDataSnapshot.put("color", "Blue");
        formDataSnapshot.put("style", "Modern");

        FormSubmissionHistory history = new FormSubmissionHistory();
        history.setId(1L);
        history.setFormIdentifier("form-id-123");
        history.setSubmissionNumber(1);
        history.setStatusAtSubmission(FormStatus.SUBMITTED);
        history.setFormDataSnapshot(formDataSnapshot);
        history.setSubmittedByCustomerId(CUSTOMER_ID);
        history.setSubmittedByCustomerName("John Customer");
        history.setSubmissionNotes("All selections completed");
        history.setSubmittedAt(LocalDateTime.now());

        // Act
        FormSubmissionHistoryResponseModel responseModel = mapper.entityToResponseModel(history);

        // Assert
        assertNotNull(responseModel);
        assertEquals(1L, responseModel.getId());
        assertEquals("form-id-123", responseModel.getFormIdentifier());
        assertEquals(1, responseModel.getSubmissionNumber());
        assertEquals(FormStatus.SUBMITTED, responseModel.getStatusAtSubmission());
        assertEquals(formDataSnapshot, responseModel.getFormDataSnapshot());
        assertEquals(CUSTOMER_ID.toString(), responseModel.getSubmittedByCustomerId());
        assertEquals("John Customer", responseModel.getSubmittedByCustomerName());
        assertEquals("All selections completed", responseModel.getSubmissionNotes());
        assertNotNull(responseModel.getSubmittedAt());
    }

    @Test
    void entityToResponseModel_WithMinimalData_ConvertsSuccessfully() {
        // Arrange
        FormSubmissionHistory history = new FormSubmissionHistory();
        history.setId(2L);
        history.setFormIdentifier("form-id-456");
        history.setSubmissionNumber(1);
        history.setStatusAtSubmission(FormStatus.SUBMITTED);
        history.setFormDataSnapshot(new HashMap<>());
        history.setSubmittedByCustomerId(CUSTOMER_ID);
        history.setSubmittedByCustomerName("Jane Customer");
        history.setSubmittedAt(LocalDateTime.now());

        // Act
        FormSubmissionHistoryResponseModel responseModel = mapper.entityToResponseModel(history);

        // Assert
        assertNotNull(responseModel);
        assertEquals(2L, responseModel.getId());
        assertEquals("form-id-456", responseModel.getFormIdentifier());
        assertEquals(1, responseModel.getSubmissionNumber());
        assertEquals(FormStatus.SUBMITTED, responseModel.getStatusAtSubmission());
        assertNotNull(responseModel.getFormDataSnapshot());
        assertTrue(responseModel.getFormDataSnapshot().isEmpty());
        assertEquals(CUSTOMER_ID.toString(), responseModel.getSubmittedByCustomerId());
        assertEquals("Jane Customer", responseModel.getSubmittedByCustomerName());
        assertNull(responseModel.getSubmissionNotes());
    }

    @Test
    void entityToResponseModel_WithNullSubmissionNotes_HandlesGracefully() {
        // Arrange
        FormSubmissionHistory history = new FormSubmissionHistory();
        history.setId(3L);
        history.setFormIdentifier("form-id-789");
        history.setSubmissionNumber(2);
        history.setStatusAtSubmission(FormStatus.SUBMITTED);
        history.setFormDataSnapshot(new HashMap<>());
        history.setSubmittedByCustomerId(CUSTOMER_ID);
        history.setSubmittedByCustomerName("Bob Customer");
        history.setSubmissionNotes(null);
        history.setSubmittedAt(LocalDateTime.now());

        // Act
        FormSubmissionHistoryResponseModel responseModel = mapper.entityToResponseModel(history);

        // Assert
        assertNotNull(responseModel);
        assertNull(responseModel.getSubmissionNotes());
    }

    @Test
    void entityToResponseModel_WithComplexFormData_PreservesData() {
        // Arrange
        Map<String, Object> complexData = new HashMap<>();
        complexData.put("exteriorColor", "White");
        complexData.put("interiorColor", "Oak");

        Map<String, Object> nestedData = new HashMap<>();
        nestedData.put("front", "Style A");
        nestedData.put("back", "Style B");
        complexData.put("doorStyles", nestedData);

        complexData.put("quantity", 5);
        complexData.put("additionalNotes", "Please include hardware");

        FormSubmissionHistory history = new FormSubmissionHistory();
        history.setId(4L);
        history.setFormIdentifier("form-id-complex");
        history.setSubmissionNumber(1);
        history.setStatusAtSubmission(FormStatus.SUBMITTED);
        history.setFormDataSnapshot(complexData);
        history.setSubmittedByCustomerId(CUSTOMER_ID);
        history.setSubmittedByCustomerName("Complex Customer");
        history.setSubmissionNotes("Complex submission");
        history.setSubmittedAt(LocalDateTime.now());

        // Act
        FormSubmissionHistoryResponseModel responseModel = mapper.entityToResponseModel(history);

        // Assert
        assertNotNull(responseModel);
        assertEquals(complexData, responseModel.getFormDataSnapshot());
        assertEquals("White", responseModel.getFormDataSnapshot().get("exteriorColor"));
        assertEquals("Oak", responseModel.getFormDataSnapshot().get("interiorColor"));
        assertEquals(5, responseModel.getFormDataSnapshot().get("quantity"));

        @SuppressWarnings("unchecked")
        Map<String, Object> nestedResponse = (Map<String, Object>) responseModel.getFormDataSnapshot().get("doorStyles");
        assertNotNull(nestedResponse);
        assertEquals("Style A", nestedResponse.get("front"));
        assertEquals("Style B", nestedResponse.get("back"));
    }

    @Test
    void entityToResponseModel_WithMultipleSubmissions_HandlesSequentially() {
        // Test that multiple submission numbers are preserved correctly
        LocalDateTime baseTime = LocalDateTime.now();

        for (int i = 1; i <= 5; i++) {
            // Arrange
            FormSubmissionHistory history = new FormSubmissionHistory();
            history.setId((long) i);
            history.setFormIdentifier("form-id-multi");
            history.setSubmissionNumber(i);
            history.setStatusAtSubmission(FormStatus.SUBMITTED);
            history.setFormDataSnapshot(new HashMap<>());
            history.setSubmittedByCustomerId(CUSTOMER_ID);
            history.setSubmittedByCustomerName("Multi Customer");
            history.setSubmissionNotes("Submission #" + i);
            history.setSubmittedAt(baseTime.plusHours(i));

            // Act
            FormSubmissionHistoryResponseModel responseModel = mapper.entityToResponseModel(history);

            // Assert
            assertNotNull(responseModel);
            assertEquals(i, responseModel.getSubmissionNumber());
            assertEquals("Submission #" + i, responseModel.getSubmissionNotes());
            assertEquals(baseTime.plusHours(i), responseModel.getSubmittedAt());
        }
    }

    @Test
    void entityToResponseModel_WithEmptyFormData_CreatesValidResponse() {
        // Arrange
        FormSubmissionHistory history = new FormSubmissionHistory();
        history.setId(5L);
        history.setFormIdentifier("form-id-empty");
        history.setSubmissionNumber(1);
        history.setStatusAtSubmission(FormStatus.SUBMITTED);
        history.setFormDataSnapshot(new HashMap<>());
        history.setSubmittedByCustomerId(CUSTOMER_ID);
        history.setSubmittedByCustomerName("Empty Customer");
        history.setSubmittedAt(LocalDateTime.now());

        // Act
        FormSubmissionHistoryResponseModel responseModel = mapper.entityToResponseModel(history);

        // Assert
        assertNotNull(responseModel);
        assertNotNull(responseModel.getFormDataSnapshot());
        assertTrue(responseModel.getFormDataSnapshot().isEmpty());
    }

    @Test
    void entityToResponseModel_PreservesAllTimestampPrecision() {
        // Arrange
        LocalDateTime preciseTime = LocalDateTime.of(2026, 2, 11, 14, 30, 45, 123456789);

        FormSubmissionHistory history = new FormSubmissionHistory();
        history.setId(6L);
        history.setFormIdentifier("form-id-timestamp");
        history.setSubmissionNumber(1);
        history.setStatusAtSubmission(FormStatus.SUBMITTED);
        history.setFormDataSnapshot(new HashMap<>());
        history.setSubmittedByCustomerId(CUSTOMER_ID);
        history.setSubmittedByCustomerName("Timestamp Customer");
        history.setSubmittedAt(preciseTime);

        // Act
        FormSubmissionHistoryResponseModel responseModel = mapper.entityToResponseModel(history);

        // Assert
        assertNotNull(responseModel);
        assertEquals(preciseTime, responseModel.getSubmittedAt());
        assertEquals(preciseTime.getNano(), responseModel.getSubmittedAt().getNano());
    }

    @Test
    void entityToResponseModel_WithLongSubmissionNotes_PreservesFullText() {
        // Arrange
        StringBuilder longNotes = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longNotes.append("This is submission note line ").append(i).append(". ");
        }
        String notesText = longNotes.toString();

        FormSubmissionHistory history = new FormSubmissionHistory();
        history.setId(7L);
        history.setFormIdentifier("form-id-long-notes");
        history.setSubmissionNumber(1);
        history.setStatusAtSubmission(FormStatus.SUBMITTED);
        history.setFormDataSnapshot(new HashMap<>());
        history.setSubmittedByCustomerId(CUSTOMER_ID);
        history.setSubmittedByCustomerName("Long Notes Customer");
        history.setSubmissionNotes(notesText);
        history.setSubmittedAt(LocalDateTime.now());

        // Act
        FormSubmissionHistoryResponseModel responseModel = mapper.entityToResponseModel(history);

        // Assert
        assertNotNull(responseModel);
        assertEquals(notesText, responseModel.getSubmissionNotes());
        assertTrue(responseModel.getSubmissionNotes().length() > 1000);
    }

    @Test
    void entityToResponseModel_WithSpecialCharactersInNotes_PreservesCharacters() {
        // Arrange
        String specialNotes = "Notes with special chars: àéîöü ñ ç €£¥ 你好 こんにちは <>&\"'";

        FormSubmissionHistory history = new FormSubmissionHistory();
        history.setId(8L);
        history.setFormIdentifier("form-id-special");
        history.setSubmissionNumber(1);
        history.setStatusAtSubmission(FormStatus.SUBMITTED);
        history.setFormDataSnapshot(new HashMap<>());
        history.setSubmittedByCustomerId(CUSTOMER_ID);
        history.setSubmittedByCustomerName("Special Customer");
        history.setSubmissionNotes(specialNotes);
        history.setSubmittedAt(LocalDateTime.now());

        // Act
        FormSubmissionHistoryResponseModel responseModel = mapper.entityToResponseModel(history);

        // Assert
        assertNotNull(responseModel);
        assertEquals(specialNotes, responseModel.getSubmissionNotes());
    }
}


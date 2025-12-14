package com.ecp.les_constructions_dominic_cyr.CommunicationSubdomain.mapperlayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.Inquiry;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.MapperLayer.InquiryMapper;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class InquiryMapperTest {

    private InquiryMapper inquiryMapper;

    @BeforeEach
    void setUp() {
        inquiryMapper = new InquiryMapper();
    }

    @Test
    void requestModelToEntity_WithAllFields_MapsCorrectly() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("John Doe");
        requestModel.setEmail("john.doe@example.com");
        requestModel.setPhone("555-1234");
        requestModel.setMessage("I am interested in your construction services.");
        requestModel.setRecaptchaToken("test-token");

        // Act
        Inquiry result = inquiryMapper.requestModelToEntity(requestModel);

        // Assert
        assertNotNull(result);
        assertNull(result.getId()); // ID should not be set by mapper
        assertNull(result.getCreatedAt()); // createdAt should be set by @PrePersist
        assertEquals("John Doe", result.getName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("555-1234", result.getPhone());
        assertEquals("I am interested in your construction services.", result.getMessage());
    }

    @Test
    void requestModelToEntity_WithNullPhone_MapsCorrectly() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("Jane Smith");
        requestModel.setEmail("jane.smith@example.com");
        requestModel.setPhone(null);
        requestModel.setMessage("Question about renovation services.");

        // Act
        Inquiry result = inquiryMapper.requestModelToEntity(requestModel);

        // Assert
        assertNotNull(result);
        assertEquals("Jane Smith", result.getName());
        assertEquals("jane.smith@example.com", result.getEmail());
        assertNull(result.getPhone());
        assertEquals("Question about renovation services.", result.getMessage());
    }

    @Test
    void requestModelToEntity_WithLongMessage_MapsCorrectly() {
        // Arrange
        String longMessage = "A".repeat(2000);
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("Bob Johnson");
        requestModel.setEmail("bob@example.com");
        requestModel.setMessage(longMessage);

        // Act
        Inquiry result = inquiryMapper.requestModelToEntity(requestModel);

        // Assert
        assertNotNull(result);
        assertEquals(2000, result.getMessage().length());
        assertEquals(longMessage, result.getMessage());
    }

    @Test
    void requestModelToEntity_WithMaxLengthFields_MapsCorrectly() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("A".repeat(150));
        requestModel.setEmail("a".repeat(190) + "@email.com");
        requestModel.setPhone("1".repeat(30));
        requestModel.setMessage("Test message");

        // Act
        Inquiry result = inquiryMapper.requestModelToEntity(requestModel);

        // Assert
        assertNotNull(result);
        assertEquals(150, result.getName().length());
        assertEquals(30, result.getPhone().length());
        assertTrue(result.getEmail().length() <= 200);
    }

    @Test
    void entityToResponseModel_WithAllFields_MapsCorrectly() {
        // Arrange
        Inquiry inquiry = new Inquiry();
        inquiry.setId(1L);
        inquiry.setName("John Doe");
        inquiry.setEmail("john.doe@example.com");
        inquiry.setPhone("555-1234");
        inquiry.setMessage("I am interested in your construction services.");
        inquiry.setCreatedAt(OffsetDateTime.now());

        // Act
        InquiryResponseModel result = inquiryMapper.entityToResponseModel(inquiry);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("555-1234", result.getPhone());
        assertEquals("I am interested in your construction services.", result.getMessage());
        assertNotNull(result.getCreatedAt());
        assertEquals(inquiry.getCreatedAt(), result.getCreatedAt());
    }

    @Test
    void entityToResponseModel_WithNullPhone_MapsCorrectly() {
        // Arrange
        Inquiry inquiry = new Inquiry();
        inquiry.setId(2L);
        inquiry.setName("Jane Smith");
        inquiry.setEmail("jane.smith@example.com");
        inquiry.setPhone(null);
        inquiry.setMessage("Question about services.");
        inquiry.setCreatedAt(OffsetDateTime.now());

        // Act
        InquiryResponseModel result = inquiryMapper.entityToResponseModel(inquiry);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Jane Smith", result.getName());
        assertNull(result.getPhone());
        assertEquals("Question about services.", result.getMessage());
    }

    @Test
    void entityToResponseModel_WithNullId_MapsCorrectly() {
        // Arrange
        Inquiry inquiry = new Inquiry();
        inquiry.setId(null);
        inquiry.setName("Test User");
        inquiry.setEmail("test@example.com");
        inquiry.setMessage("Test message");
        inquiry.setCreatedAt(OffsetDateTime.now());

        // Act
        InquiryResponseModel result = inquiryMapper.entityToResponseModel(inquiry);

        // Assert
        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("Test User", result.getName());
    }

    @Test
    void entityToResponseModel_PreservesCreatedAtTimestamp() {
        // Arrange
        OffsetDateTime specificTime = OffsetDateTime.parse("2025-12-07T10:30:00Z");
        Inquiry inquiry = new Inquiry();
        inquiry.setId(3L);
        inquiry.setName("Alice Brown");
        inquiry.setEmail("alice@example.com");
        inquiry.setMessage("Message");
        inquiry.setCreatedAt(specificTime);

        // Act
        InquiryResponseModel result = inquiryMapper.entityToResponseModel(inquiry);

        // Assert
        assertNotNull(result);
        assertEquals(specificTime, result.getCreatedAt());
    }

    @Test
    void requestModelToEntity_DoesNotCopyRecaptchaToken() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("Test User");
        requestModel.setEmail("test@example.com");
        requestModel.setMessage("Test message");
        requestModel.setRecaptchaToken("some-captcha-token");

        // Act
        Inquiry result = inquiryMapper.requestModelToEntity(requestModel);

        // Assert
        assertNotNull(result);
        // Verify the recaptcha token is not mapped (entity doesn't have this field)
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void roundTrip_RequestToEntityToResponse_PreservesData() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("Round Trip Test");
        requestModel.setEmail("roundtrip@example.com");
        requestModel.setPhone("555-9999");
        requestModel.setMessage("Testing round trip mapping");

        // Act
        Inquiry entity = inquiryMapper.requestModelToEntity(requestModel);
        entity.setId(10L); // Simulate DB assignment
        entity.setCreatedAt(OffsetDateTime.now()); // Simulate @PrePersist

        InquiryResponseModel responseModel = inquiryMapper.entityToResponseModel(entity);

        // Assert
        assertEquals(requestModel.getName(), responseModel.getName());
        assertEquals(requestModel.getEmail(), responseModel.getEmail());
        assertEquals(requestModel.getPhone(), responseModel.getPhone());
        assertEquals(requestModel.getMessage(), responseModel.getMessage());
        assertNotNull(responseModel.getId());
        assertNotNull(responseModel.getCreatedAt());
    }

    @Test
    void entityToResponseModel_WithLongMessage_MapsCorrectly() {
        // Arrange
        String longMessage = "B".repeat(2000);
        Inquiry inquiry = new Inquiry();
        inquiry.setId(5L);
        inquiry.setName("Long Message User");
        inquiry.setEmail("long@example.com");
        inquiry.setMessage(longMessage);
        inquiry.setCreatedAt(OffsetDateTime.now());

        // Act
        InquiryResponseModel result = inquiryMapper.entityToResponseModel(inquiry);

        // Assert
        assertNotNull(result);
        assertEquals(2000, result.getMessage().length());
        assertEquals(longMessage, result.getMessage());
    }

    @Test
    void requestModelToEntity_WithEmptyPhone_MapsCorrectly() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("Empty Phone User");
        requestModel.setEmail("emptyphone@example.com");
        requestModel.setPhone("");
        requestModel.setMessage("Test with empty phone");

        // Act
        Inquiry result = inquiryMapper.requestModelToEntity(requestModel);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getPhone());
    }

    @Test
    void entityToResponseModel_WithEmptyPhone_MapsCorrectly() {
        // Arrange
        Inquiry inquiry = new Inquiry();
        inquiry.setId(6L);
        inquiry.setName("Empty Phone Response");
        inquiry.setEmail("response@example.com");
        inquiry.setPhone("");
        inquiry.setMessage("Response with empty phone");
        inquiry.setCreatedAt(OffsetDateTime.now());

        // Act
        InquiryResponseModel result = inquiryMapper.entityToResponseModel(inquiry);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getPhone());
    }
}

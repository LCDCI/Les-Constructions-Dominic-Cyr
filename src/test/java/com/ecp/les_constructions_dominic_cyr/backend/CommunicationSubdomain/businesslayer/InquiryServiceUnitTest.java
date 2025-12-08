package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.InquiryServiceImpl;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.Inquiry;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.InquiryRepository;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.MapperLayer.InquiryMapper;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InquiryServiceUnitTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private InquiryMapper inquiryMapper;

    @InjectMocks
    private InquiryServiceImpl inquiryService;

    private InquiryRequestModel testRequestModel;
    private Inquiry testInquiry;
    private Inquiry savedInquiry;
    private InquiryResponseModel testResponseModel;

    @BeforeEach
    void setUp() {
        // Arrange test data
        testRequestModel = new InquiryRequestModel();
        testRequestModel.setName("John Doe");
        testRequestModel.setEmail("john.doe@example.com");
        testRequestModel.setPhone("555-1234");
        testRequestModel.setMessage("I am interested in your construction services.");
        testRequestModel.setRecaptchaToken("test-token");

        testInquiry = new Inquiry();
        testInquiry.setName("John Doe");
        testInquiry.setEmail("john.doe@example.com");
        testInquiry.setPhone("555-1234");
        testInquiry.setMessage("I am interested in your construction services.");

        savedInquiry = new Inquiry();
        savedInquiry.setId(1L);
        savedInquiry.setName("John Doe");
        savedInquiry.setEmail("john.doe@example.com");
        savedInquiry.setPhone("555-1234");
        savedInquiry.setMessage("I am interested in your construction services.");
        savedInquiry.setCreatedAt(OffsetDateTime.now());

        testResponseModel = new InquiryResponseModel();
        testResponseModel.setId(1L);
        testResponseModel.setName("John Doe");
        testResponseModel.setEmail("john.doe@example.com");
        testResponseModel.setPhone("555-1234");
        testResponseModel.setMessage("I am interested in your construction services.");
        testResponseModel.setCreatedAt(OffsetDateTime.now());
    }

    @Test
    void submitInquiry_WithValidRequest_ReturnsResponseModel() {
        // Arrange
        when(inquiryMapper.requestModelToEntity(testRequestModel)).thenReturn(testInquiry);
        when(inquiryRepository.save(testInquiry)).thenReturn(savedInquiry);
        when(inquiryMapper.entityToResponseModel(savedInquiry)).thenReturn(testResponseModel);

        // Act
        InquiryResponseModel result = inquiryService.submitInquiry(testRequestModel);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("555-1234", result.getPhone());
        assertEquals("I am interested in your construction services.", result.getMessage());
        assertNotNull(result.getCreatedAt());

        verify(inquiryMapper, times(1)).requestModelToEntity(testRequestModel);
        verify(inquiryRepository, times(1)).save(testInquiry);
        verify(inquiryMapper, times(1)).entityToResponseModel(savedInquiry);
    }

    @Test
    void submitInquiry_WithNullPhone_ReturnsResponseModel() {
        // Arrange
        testRequestModel.setPhone(null);
        testInquiry.setPhone(null);
        savedInquiry.setPhone(null);
        testResponseModel.setPhone(null);

        when(inquiryMapper.requestModelToEntity(testRequestModel)).thenReturn(testInquiry);
        when(inquiryRepository.save(testInquiry)).thenReturn(savedInquiry);
        when(inquiryMapper.entityToResponseModel(savedInquiry)).thenReturn(testResponseModel);

        // Act
        InquiryResponseModel result = inquiryService.submitInquiry(testRequestModel);

        // Assert
        assertNotNull(result);
        assertNull(result.getPhone());
        assertEquals("John Doe", result.getName());

        verify(inquiryMapper, times(1)).requestModelToEntity(testRequestModel);
        verify(inquiryRepository, times(1)).save(testInquiry);
        verify(inquiryMapper, times(1)).entityToResponseModel(savedInquiry);
    }

    @Test
    void submitInquiry_WithLongMessage_ReturnsResponseModel() {
        // Arrange
        String longMessage = "A".repeat(2000);
        testRequestModel.setMessage(longMessage);
        testInquiry.setMessage(longMessage);
        savedInquiry.setMessage(longMessage);
        testResponseModel.setMessage(longMessage);

        when(inquiryMapper.requestModelToEntity(testRequestModel)).thenReturn(testInquiry);
        when(inquiryRepository.save(testInquiry)).thenReturn(savedInquiry);
        when(inquiryMapper.entityToResponseModel(savedInquiry)).thenReturn(testResponseModel);

        // Act
        InquiryResponseModel result = inquiryService.submitInquiry(testRequestModel);

        // Assert
        assertNotNull(result);
        assertEquals(2000, result.getMessage().length());

        verify(inquiryMapper, times(1)).requestModelToEntity(testRequestModel);
        verify(inquiryRepository, times(1)).save(testInquiry);
        verify(inquiryMapper, times(1)).entityToResponseModel(savedInquiry);
    }

    @Test
    void submitInquiry_VerifiesMapperCalledWithCorrectParameter() {
        // Arrange
        when(inquiryMapper.requestModelToEntity(any(InquiryRequestModel.class))).thenReturn(testInquiry);
        when(inquiryRepository.save(any(Inquiry.class))).thenReturn(savedInquiry);
        when(inquiryMapper.entityToResponseModel(any(Inquiry.class))).thenReturn(testResponseModel);

        // Act
        inquiryService.submitInquiry(testRequestModel);

        // Assert
        verify(inquiryMapper).requestModelToEntity(argThat(request ->
                request.getName().equals("John Doe") &&
                request.getEmail().equals("john.doe@example.com") &&
                request.getMessage().equals("I am interested in your construction services.")
        ));
    }

    @Test
    void submitInquiry_VerifiesRepositorySaveCalledWithCorrectParameter() {
        // Arrange
        when(inquiryMapper.requestModelToEntity(any(InquiryRequestModel.class))).thenReturn(testInquiry);
        when(inquiryRepository.save(any(Inquiry.class))).thenReturn(savedInquiry);
        when(inquiryMapper.entityToResponseModel(any(Inquiry.class))).thenReturn(testResponseModel);

        // Act
        inquiryService.submitInquiry(testRequestModel);

        // Assert
        verify(inquiryRepository).save(argThat(inquiry ->
                inquiry.getName().equals("John Doe") &&
                inquiry.getEmail().equals("john.doe@example.com")
        ));
    }

    @Test
    void submitInquiry_VerifiesResponseMapperCalledWithSavedEntity() {
        // Arrange
        when(inquiryMapper.requestModelToEntity(any(InquiryRequestModel.class))).thenReturn(testInquiry);
        when(inquiryRepository.save(any(Inquiry.class))).thenReturn(savedInquiry);
        when(inquiryMapper.entityToResponseModel(any(Inquiry.class))).thenReturn(testResponseModel);

        // Act
        inquiryService.submitInquiry(testRequestModel);

        // Assert
        verify(inquiryMapper).entityToResponseModel(argThat(inquiry ->
                inquiry.getId() != null &&
                inquiry.getId().equals(1L) &&
                inquiry.getCreatedAt() != null
        ));
    }

    @Test
    void submitInquiry_WithDifferentEmail_ReturnsCorrectResponseModel() {
        // Arrange
        testRequestModel.setEmail("different@example.com");
        testInquiry.setEmail("different@example.com");
        savedInquiry.setEmail("different@example.com");
        testResponseModel.setEmail("different@example.com");

        when(inquiryMapper.requestModelToEntity(testRequestModel)).thenReturn(testInquiry);
        when(inquiryRepository.save(testInquiry)).thenReturn(savedInquiry);
        when(inquiryMapper.entityToResponseModel(savedInquiry)).thenReturn(testResponseModel);

        // Act
        InquiryResponseModel result = inquiryService.submitInquiry(testRequestModel);

        // Assert
        assertNotNull(result);
        assertEquals("different@example.com", result.getEmail());

        verify(inquiryMapper, times(1)).requestModelToEntity(testRequestModel);
        verify(inquiryRepository, times(1)).save(testInquiry);
        verify(inquiryMapper, times(1)).entityToResponseModel(savedInquiry);
    }

    @Test
    void submitInquiry_EnsuresCorrectFlowOfExecution() {
        // Arrange
        when(inquiryMapper.requestModelToEntity(any(InquiryRequestModel.class))).thenReturn(testInquiry);
        when(inquiryRepository.save(any(Inquiry.class))).thenReturn(savedInquiry);
        when(inquiryMapper.entityToResponseModel(any(Inquiry.class))).thenReturn(testResponseModel);

        // Act
        inquiryService.submitInquiry(testRequestModel);

        // Assert - verify the order of method calls
        var inOrder = inOrder(inquiryMapper, inquiryRepository);
        inOrder.verify(inquiryMapper).requestModelToEntity(any(InquiryRequestModel.class));
        inOrder.verify(inquiryRepository).save(any(Inquiry.class));
        inOrder.verify(inquiryMapper).entityToResponseModel(any(Inquiry.class));
    }
}

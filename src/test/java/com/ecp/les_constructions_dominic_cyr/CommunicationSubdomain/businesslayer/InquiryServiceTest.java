package com.ecp.les_constructions_dominic_cyr.CommunicationSubdomain.businesslayer;

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

/**
 * Service Unit Test for InquiryServiceImpl
 * Following AAA Pattern (Arrange, Act, Assert)
 * Uses @ExtendWith(MockitoExtension.class) to avoid bringing up full application context
 */
@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private InquiryMapper inquiryMapper;

    @InjectMocks
    private InquiryServiceImpl inquiryService;

    private InquiryRequestModel validRequest;
    private Inquiry savedInquiry;
    private InquiryResponseModel responseModel;

    @BeforeEach
    void setUp() {
        // Arrange - setup test data
        validRequest = new InquiryRequestModel();
        validRequest.setName("Alice Johnson");
        validRequest.setEmail("alice@example.com");
        validRequest.setPhone("555-9876");
        validRequest.setMessage("Looking for custom home design.");

        savedInquiry = new Inquiry();
        savedInquiry.setId(1L);
        savedInquiry.setName(validRequest.getName());
        savedInquiry.setEmail(validRequest.getEmail());
        savedInquiry.setPhone(validRequest.getPhone());
        savedInquiry.setMessage(validRequest.getMessage());
        savedInquiry.setCreatedAt(OffsetDateTime.now());

        responseModel = new InquiryResponseModel();
        responseModel.setId(1L);
        responseModel.setName(validRequest.getName());
        responseModel.setEmail(validRequest.getEmail());
        responseModel.setPhone(validRequest.getPhone());
        responseModel.setMessage(validRequest.getMessage());
        responseModel.setCreatedAt(savedInquiry.getCreatedAt());
    }

    @Test
    void whenSubmitInquiry_thenSavesInquirySuccessfully() {
        // Arrange
        when(inquiryMapper.requestModelToEntity(any(InquiryRequestModel.class))).thenReturn(new Inquiry());
        when(inquiryRepository.save(any(Inquiry.class))).thenReturn(savedInquiry);
        when(inquiryMapper.entityToResponseModel(any(Inquiry.class))).thenReturn(responseModel);

        // Act
        InquiryResponseModel result = inquiryService.submitInquiry(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Alice Johnson", result.getName());
        assertEquals("alice@example.com", result.getEmail());
        assertEquals("555-9876", result.getPhone());
        assertEquals("Looking for custom home design.", result.getMessage());
        verify(inquiryMapper, times(1)).requestModelToEntity(any(InquiryRequestModel.class));
        verify(inquiryRepository, times(1)).save(any(Inquiry.class));
        verify(inquiryMapper, times(1)).entityToResponseModel(any(Inquiry.class));
    }

    @Test
    void whenSubmitInquiry_withoutPhone_thenSavesSuccessfully() {
        // Arrange
        validRequest.setPhone(null);
        
        Inquiry savedInquiryNoPhone = new Inquiry();
        savedInquiryNoPhone.setId(2L);
        savedInquiryNoPhone.setName(validRequest.getName());
        savedInquiryNoPhone.setEmail(validRequest.getEmail());
        savedInquiryNoPhone.setMessage(validRequest.getMessage());
        savedInquiryNoPhone.setCreatedAt(OffsetDateTime.now());

        InquiryResponseModel responseNoPhone = new InquiryResponseModel();
        responseNoPhone.setId(2L);
        responseNoPhone.setName(validRequest.getName());
        responseNoPhone.setEmail(validRequest.getEmail());
        responseNoPhone.setMessage(validRequest.getMessage());
        responseNoPhone.setCreatedAt(savedInquiryNoPhone.getCreatedAt());

        when(inquiryMapper.requestModelToEntity(any(InquiryRequestModel.class))).thenReturn(new Inquiry());
        when(inquiryRepository.save(any(Inquiry.class))).thenReturn(savedInquiryNoPhone);
        when(inquiryMapper.entityToResponseModel(any(Inquiry.class))).thenReturn(responseNoPhone);

        // Act
        InquiryResponseModel result = inquiryService.submitInquiry(validRequest);

        // Assert
        assertNotNull(result);
        assertNull(result.getPhone());
        assertEquals("Alice Johnson", result.getName());
        assertEquals("alice@example.com", result.getEmail());
        verify(inquiryRepository, times(1)).save(any(Inquiry.class));
    }

    @Test
    void whenSubmitInquiry_thenMapsAllFieldsCorrectly() {
        // Arrange
        when(inquiryMapper.requestModelToEntity(any(InquiryRequestModel.class))).thenReturn(new Inquiry());
        when(inquiryRepository.save(any(Inquiry.class))).thenReturn(savedInquiry);
        when(inquiryMapper.entityToResponseModel(any(Inquiry.class))).thenReturn(responseModel);

        // Act
        InquiryResponseModel result = inquiryService.submitInquiry(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals(validRequest.getName(), result.getName());
        assertEquals(validRequest.getEmail(), result.getEmail());
        assertEquals(validRequest.getPhone(), result.getPhone());
        assertEquals(validRequest.getMessage(), result.getMessage());
        verify(inquiryMapper, times(1)).requestModelToEntity(validRequest);
        verify(inquiryRepository, times(1)).save(any(Inquiry.class));
        verify(inquiryMapper, times(1)).entityToResponseModel(savedInquiry);
    }
}

package com.ecp.les_constructions_dominic_cyr.CommunicationSubdomain.Services;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DTOs.InquiryRequest;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.Entities.Inquiry;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.Repositories.InquiryRepository;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.Services.InquiryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Service Unit Test for InquiryService
 * Following AAA Pattern (Arrange, Act, Assert)
 * Uses @ExtendWith(MockitoExtension.class) to avoid bringing up full application context
 */
@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @InjectMocks
    private InquiryService inquiryService;

    private InquiryRequest validRequest;
    private Inquiry savedInquiry;

    @BeforeEach
    void setUp() {
        // Arrange - setup test data
        validRequest = new InquiryRequest();
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
    }

    @Test
    void whenSubmitInquiry_thenSavesInquirySuccessfully() {
        // Arrange
        when(inquiryRepository.save(any(Inquiry.class))).thenReturn(savedInquiry);

        // Act
        Inquiry result = inquiryService.submitInquiry(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Alice Johnson", result.getName());
        assertEquals("alice@example.com", result.getEmail());
        assertEquals("555-9876", result.getPhone());
        assertEquals("Looking for custom home design.", result.getMessage());
        verify(inquiryRepository, times(1)).save(any(Inquiry.class));
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

        when(inquiryRepository.save(any(Inquiry.class))).thenReturn(savedInquiryNoPhone);

        // Act
        Inquiry result = inquiryService.submitInquiry(validRequest);

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
        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(invocation -> {
            Inquiry inquiry = invocation.getArgument(0);
            inquiry.setId(3L);
            return inquiry;
        });

        // Act
        Inquiry result = inquiryService.submitInquiry(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals(validRequest.getName(), result.getName());
        assertEquals(validRequest.getEmail(), result.getEmail());
        assertEquals(validRequest.getPhone(), result.getPhone());
        assertEquals(validRequest.getMessage(), result.getMessage());
        verify(inquiryRepository, times(1)).save(any(Inquiry.class));
    }
}

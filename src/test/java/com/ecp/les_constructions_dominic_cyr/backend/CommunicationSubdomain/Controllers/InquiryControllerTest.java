package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.Controllers;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DTOs.InquiryRequest;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.Entities.Inquiry;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.Services.InquiryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller Unit Test for InquiryController
 * Following AAA Pattern (Arrange, Act, Assert)
 * Uses @WebMvcTest to bring up only web layer components
 * Uses @MockBean to mock the service layer
 */
@WebMvcTest(InquiryController.class)
class InquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InquiryService inquiryService;

    private InquiryRequest validRequest;
    private Inquiry savedInquiry;

    @BeforeEach
    void setUp() {
        // Arrange - setup test data
        validRequest = new InquiryRequest();
        validRequest.setName("John Doe");
        validRequest.setEmail("john@example.com");
        validRequest.setPhone("555-1234");
        validRequest.setMessage("I need a quote for a new home.");

        savedInquiry = new Inquiry();
        savedInquiry.setId(1L);
        savedInquiry.setName(validRequest.getName());
        savedInquiry.setEmail(validRequest.getEmail());
        savedInquiry.setPhone(validRequest.getPhone());
        savedInquiry.setMessage(validRequest.getMessage());
    }

    @Test
    void whenSubmitInquiry_withValidData_thenReturnsSuccess() throws Exception {
        // Arrange
        when(inquiryService.submitInquiry(any(InquiryRequest.class))).thenReturn(savedInquiry);

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Thank you! Your inquiry has been received."));
    }

    @Test
    void whenSubmitInquiry_withMissingEmail_thenReturnsBadRequest() throws Exception {
        // Arrange
        InquiryRequest request = new InquiryRequest();
        request.setName("John Doe");
        request.setMessage("I need a quote.");

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenSubmitInquiry_withInvalidEmail_thenReturnsBadRequest() throws Exception {
        // Arrange
        InquiryRequest request = new InquiryRequest();
        request.setName("John Doe");
        request.setEmail("invalid-email");
        request.setMessage("I need a quote.");

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenSubmitInquiry_withMissingMessage_thenReturnsBadRequest() throws Exception {
        // Arrange
        InquiryRequest request = new InquiryRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenSubmitInquiry_withoutPhone_thenReturnsSuccess() throws Exception {
        // Arrange - phone is optional
        InquiryRequest request = new InquiryRequest();
        request.setName("Jane Smith");
        request.setEmail("jane@example.com");
        request.setMessage("Interested in renovations.");

        Inquiry savedInquiryNoPhone = new Inquiry();
        savedInquiryNoPhone.setId(2L);
        savedInquiryNoPhone.setName(request.getName());
        savedInquiryNoPhone.setEmail(request.getEmail());
        savedInquiryNoPhone.setMessage(request.getMessage());

        when(inquiryService.submitInquiry(any(InquiryRequest.class))).thenReturn(savedInquiryNoPhone);

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Thank you! Your inquiry has been received."));
    }
}
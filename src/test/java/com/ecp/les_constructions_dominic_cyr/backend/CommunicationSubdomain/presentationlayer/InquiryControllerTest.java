package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.InquiryService;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryController;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryResponseModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

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

    private InquiryRequestModel validRequest;
    private InquiryResponseModel savedInquiry;

    @BeforeEach
    void setUp() {
        // Arrange - setup test data
        validRequest = new InquiryRequestModel();
        validRequest.setName("John Doe");
        validRequest.setEmail("john@example.com");
        validRequest.setPhone("555-1234");
        validRequest.setMessage("I need a quote for a new home.");

        savedInquiry = new InquiryResponseModel();
        savedInquiry.setId(1L);
        savedInquiry.setName(validRequest.getName());
        savedInquiry.setEmail(validRequest.getEmail());
        savedInquiry.setPhone(validRequest.getPhone());
        savedInquiry.setMessage(validRequest.getMessage());
        savedInquiry.setCreatedAt(OffsetDateTime.now());
    }

    @Test
    void whenSubmitInquiry_withValidData_thenReturnsSuccess() throws Exception {
        // Arrange
        when(inquiryService.submitInquiry(any(InquiryRequestModel.class))).thenReturn(savedInquiry);

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
        InquiryRequestModel request = new InquiryRequestModel();
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
        InquiryRequestModel request = new InquiryRequestModel();
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
        InquiryRequestModel request = new InquiryRequestModel();
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
        InquiryRequestModel request = new InquiryRequestModel();
        request.setName("Jane Smith");
        request.setEmail("jane@example.com");
        request.setMessage("Interested in renovations.");

        InquiryResponseModel savedInquiryNoPhone = new InquiryResponseModel();
        savedInquiryNoPhone.setId(2L);
        savedInquiryNoPhone.setName(request.getName());
        savedInquiryNoPhone.setEmail(request.getEmail());
        savedInquiryNoPhone.setMessage(request.getMessage());
        savedInquiryNoPhone.setCreatedAt(OffsetDateTime.now());

        when(inquiryService.submitInquiry(any(InquiryRequestModel.class))).thenReturn(savedInquiryNoPhone);

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Thank you! Your inquiry has been received."));
    }
}

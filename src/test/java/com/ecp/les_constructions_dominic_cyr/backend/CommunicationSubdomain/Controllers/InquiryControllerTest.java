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

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    void whenOwnerRequestsInquiries_thenReturnsOrderedList() throws Exception {
        // Arrange
        Inquiry older = new Inquiry();
        older.setId(10L);
        older.setName("Old");
        older.setEmail("old@example.com");
        older.setMessage("Old msg");
        older.setCreatedAt(OffsetDateTime.parse("2024-01-01T10:00:00Z"));

        Inquiry newer = new Inquiry();
        newer.setId(11L);
        newer.setName("New");
        newer.setEmail("new@example.com");
        newer.setMessage("New msg");
        newer.setCreatedAt(OffsetDateTime.parse("2024-02-01T10:00:00Z"));

        List<Inquiry> inquiries = Arrays.asList(newer, older);
        when(inquiryService.getInquiries()).thenReturn(inquiries);

        // Act & Assert
        mockMvc.perform(get("/api/inquiries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(11)))
                .andExpect(jsonPath("$[0].name", is("New")))
                .andExpect(jsonPath("$[0].email", is("new@example.com")))
                .andExpect(jsonPath("$[0].message", is("New msg")))
                .andExpect(jsonPath("$[0].createdAt", notNullValue()))
                .andExpect(jsonPath("$[1].id", is(10)))
                .andExpect(jsonPath("$[1].createdAt", notNullValue()));
    }

    @Test
    void whenVisitorAttemptsDelete_thenForbidden() throws Exception {
        mockMvc.perform(delete("/api/inquiries/1"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Inquiries cannot be deleted."));
    }
}
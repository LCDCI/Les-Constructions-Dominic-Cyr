package com.ecp.les_constructions_dominic_cyr.CommunicationSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.InquiryService;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryController;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryResponseModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InquiryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class InquiryControllerCaptchaTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InquiryService inquiryService;

    private ObjectMapper objectMapper;
    private InquiryRequestModel testRequestModel;
    private InquiryResponseModel testResponseModel;

    @BeforeEach
    void setUp() throws Exception {
        // Clear rate limiting buckets before each test
        Field bucketsField = InquiryController.class.getDeclaredField("buckets");
        bucketsField.setAccessible(true);
        Map<?, ?> buckets = (Map<?, ?>) bucketsField.get(null);
        buckets.clear();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testRequestModel = new InquiryRequestModel();
        testRequestModel.setName("John Doe");
        testRequestModel.setEmail("john.doe@example.com");
        testRequestModel.setPhone("555-1234");
        testRequestModel.setMessage("I am interested in your construction services.");

        testResponseModel = new InquiryResponseModel();
        testResponseModel.setId(1L);
        testResponseModel.setName("John Doe");
        testResponseModel.setEmail("john.doe@example.com");
        testResponseModel.setPhone("555-1234");
        testResponseModel.setMessage("I am interested in your construction services.");
        testResponseModel.setCreatedAt(OffsetDateTime.now());
    }

    @Test
    void submitInquiry_WithValidCaptchaToken_ReturnsOk() throws Exception {
        // Arrange
        testRequestModel.setRecaptchaToken("valid-captcha-token");
        
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        
        when(mockResponse.body()).thenReturn("{\"success\": true, \"score\": 0.9}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        
        when(inquiryService.submitInquiry(any(InquiryRequestModel.class))).thenReturn(testResponseModel);

        // Act & Assert
        try (MockedStatic<HttpClient> mockedStatic = mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            mockMvc.perform(post("/api/inquiries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequestModel)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("text/plain;charset=UTF-8"))
                    .andExpect(content().string("Thank you! Your inquiry has been received."));

            verify(inquiryService, times(1)).submitInquiry(any(InquiryRequestModel.class));
        }
    }

    @Test
    void submitInquiry_WithFailedCaptchaVerification_ReturnsBadRequest() throws Exception {
        // Arrange
        testRequestModel.setRecaptchaToken("invalid-captcha-token");
        
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        
        when(mockResponse.body()).thenReturn("{\"success\": false, \"score\": 0.1}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        try (MockedStatic<HttpClient> mockedStatic = mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            mockMvc.perform(post("/api/inquiries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequestModel)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType("text/plain;charset=UTF-8"))
                    .andExpect(content().string("CAPTCHA validation failed."));

            verify(inquiryService, never()).submitInquiry(any(InquiryRequestModel.class));
        }
    }

    @Test
    void submitInquiry_WithCaptchaIOException_ReturnsBadRequest() throws Exception {
        // Arrange
        testRequestModel.setRecaptchaToken("test-token");
        
        HttpClient mockHttpClient = mock(HttpClient.class);
        
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new java.io.IOException("Network error"));

        // Act & Assert
        try (MockedStatic<HttpClient> mockedStatic = mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            mockMvc.perform(post("/api/inquiries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequestModel)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType("text/plain;charset=UTF-8"))
                    .andExpect(content().string("CAPTCHA validation failed."));

            verify(inquiryService, never()).submitInquiry(any(InquiryRequestModel.class));
        }
    }

    @Test
    void submitInquiry_WithCaptchaInterruptedException_ReturnsBadRequest() throws Exception {
        // Arrange
        testRequestModel.setRecaptchaToken("test-token");
        
        HttpClient mockHttpClient = mock(HttpClient.class);
        
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new InterruptedException("Thread interrupted"));

        // Act & Assert
        try (MockedStatic<HttpClient> mockedStatic = mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            mockMvc.perform(post("/api/inquiries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequestModel)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType("text/plain;charset=UTF-8"))
                    .andExpect(content().string("CAPTCHA validation failed."));

            verify(inquiryService, never()).submitInquiry(any(InquiryRequestModel.class));
        }
    }

    @Test
    void submitInquiry_WithMalformedCaptchaResponse_ReturnsBadRequest() throws Exception {
        // Arrange
        testRequestModel.setRecaptchaToken("test-token");
        
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        
        // Malformed JSON response
        when(mockResponse.body()).thenReturn("{invalid json");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        try (MockedStatic<HttpClient> mockedStatic = mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            mockMvc.perform(post("/api/inquiries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequestModel)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType("text/plain;charset=UTF-8"))
                    .andExpect(content().string("CAPTCHA validation failed."));

            verify(inquiryService, never()).submitInquiry(any(InquiryRequestModel.class));
        }
    }

    @Test
    void submitInquiry_WithCaptchaResponseMissingSuccessField_ReturnsBadRequest() throws Exception {
        // Arrange
        testRequestModel.setRecaptchaToken("test-token");
        
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        
        // Response without "success" field
        when(mockResponse.body()).thenReturn("{\"score\": 0.9}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        try (MockedStatic<HttpClient> mockedStatic = mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            mockMvc.perform(post("/api/inquiries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequestModel)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType("text/plain;charset=UTF-8"))
                    .andExpect(content().string("CAPTCHA validation failed."));

            verify(inquiryService, never()).submitInquiry(any(InquiryRequestModel.class));
        }
    }

    @Test
    void submitInquiry_WithEmptyCaptchaToken_ReturnsBadRequest() throws Exception {
        // Arrange - empty token will fail CAPTCHA verification
        testRequestModel.setRecaptchaToken("");
        
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        
        when(mockResponse.body()).thenReturn("{\"success\": false}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        try (MockedStatic<HttpClient> mockedStatic = mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            mockMvc.perform(post("/api/inquiries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequestModel)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType("text/plain;charset=UTF-8"))
                    .andExpect(content().string("CAPTCHA validation failed."));

            verify(inquiryService, never()).submitInquiry(any(InquiryRequestModel.class));
        }
    }

    @Test
    void submitInquiry_VerifiesCaptchaRequestFormat() throws Exception {
        // Arrange
        testRequestModel.setRecaptchaToken("test-captcha-token-123");
        
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        
        when(mockResponse.body()).thenReturn("{\"success\": true}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        
        when(inquiryService.submitInquiry(any(InquiryRequestModel.class))).thenReturn(testResponseModel);

        // Act & Assert
        try (MockedStatic<HttpClient> mockedStatic = mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            mockMvc.perform(post("/api/inquiries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequestModel)))
                    .andExpect(status().isOk());

            // Verify HTTP client was called exactly once
            verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        }
    }

    @Test
    void submitInquiry_WithCaptchaSuccessFalse_ReturnsBadRequest() throws Exception {
        // Arrange
        testRequestModel.setRecaptchaToken("failing-token");
        
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        
        // Explicit success: false
        when(mockResponse.body()).thenReturn("{\"success\": false, \"error-codes\": [\"invalid-input-response\"]}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        try (MockedStatic<HttpClient> mockedStatic = mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            mockMvc.perform(post("/api/inquiries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequestModel)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("CAPTCHA validation failed."));

            verify(inquiryService, never()).submitInquiry(any(InquiryRequestModel.class));
        }
    }

    @Test
    void submitInquiry_WithHighScoreCaptcha_ReturnsOk() throws Exception {
        // Arrange
        testRequestModel.setRecaptchaToken("high-score-token");
        
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        
        // High score indicates likely human
        when(mockResponse.body()).thenReturn("{\"success\": true, \"score\": 0.95, \"action\": \"submit\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        
        when(inquiryService.submitInquiry(any(InquiryRequestModel.class))).thenReturn(testResponseModel);

        // Act & Assert
        try (MockedStatic<HttpClient> mockedStatic = mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            mockMvc.perform(post("/api/inquiries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequestModel)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Thank you! Your inquiry has been received."));

            verify(inquiryService, times(1)).submitInquiry(any(InquiryRequestModel.class));
        }
    }
}

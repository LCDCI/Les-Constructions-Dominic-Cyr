package com.ecp.les_constructions_dominic_cyr.CommunicationSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.InquiryService;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryController;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryResponseModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InquiryController.class)
public class InquiryControllerUnitTest {

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
        testRequestModel.setRecaptchaToken(null); // Set to null for testing

        testResponseModel = new InquiryResponseModel();
        testResponseModel.setId(1L);
        testResponseModel.setName("John Doe");
        testResponseModel.setEmail("john.doe@example.com");
        testResponseModel.setPhone("555-1234");
        testResponseModel.setMessage("I am interested in your construction services.");
        testResponseModel.setCreatedAt(OffsetDateTime.now());
    }

    @Test
    void submitInquiry_WithValidRequest_ReturnsOkWithSuccessMessage() throws Exception {
        // Arrange
        when(inquiryService.submitInquiry(any(InquiryRequestModel.class))).thenReturn(testResponseModel);

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestModel)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("Thank you! Your inquiry has been received."));

        verify(inquiryService, times(1)).submitInquiry(any(InquiryRequestModel.class));
    }

    @Test
    void submitInquiry_WithNullPhone_ReturnsOk() throws Exception {
        // Arrange
        testRequestModel.setPhone(null);
        when(inquiryService.submitInquiry(any(InquiryRequestModel.class))).thenReturn(testResponseModel);

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestModel)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("Thank you! Your inquiry has been received."));

        verify(inquiryService, times(1)).submitInquiry(any(InquiryRequestModel.class));
    }

    @Test
    void submitInquiry_WithBlankName_ReturnsBadRequest() throws Exception {
        // Arrange
        testRequestModel.setName("");

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestModel)))
                .andExpect(status().isBadRequest());

        verify(inquiryService, never()).submitInquiry(any(InquiryRequestModel.class));
    }

    @Test
    void submitInquiry_WithNullName_ReturnsBadRequest() throws Exception {
        // Arrange
        testRequestModel.setName(null);

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestModel)))
                .andExpect(status().isBadRequest());

        verify(inquiryService, never()).submitInquiry(any(InquiryRequestModel.class));
    }

    @Test
    void submitInquiry_WithBlankEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        testRequestModel.setEmail("");

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestModel)))
                .andExpect(status().isBadRequest());

        verify(inquiryService, never()).submitInquiry(any(InquiryRequestModel.class));
    }

    @Test
    void submitInquiry_WithNullEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        testRequestModel.setEmail(null);

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestModel)))
                .andExpect(status().isBadRequest());

        verify(inquiryService, never()).submitInquiry(any(InquiryRequestModel.class));
    }

    @Test
    void submitInquiry_WithInvalidEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        testRequestModel.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestModel)))
                .andExpect(status().isBadRequest());

        verify(inquiryService, never()).submitInquiry(any(InquiryRequestModel.class));
    }

    @Test
    void submitInquiry_WithBlankMessage_ReturnsBadRequest() throws Exception {
        // Arrange
        testRequestModel.setMessage("");

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestModel)))
                .andExpect(status().isBadRequest());

        verify(inquiryService, never()).submitInquiry(any(InquiryRequestModel.class));
    }

    @Test
    void submitInquiry_WithNullMessage_ReturnsBadRequest() throws Exception {
        // Arrange
        testRequestModel.setMessage(null);

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestModel)))
                .andExpect(status().isBadRequest());

        verify(inquiryService, never()).submitInquiry(any(InquiryRequestModel.class));
    }

    @Test
    void submitInquiry_WithNameTooLong_ReturnsBadRequest() throws Exception {
        // Arrange
        testRequestModel.setName("A".repeat(151)); // Exceeds 150 character limit

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestModel)))
                .andExpect(status().isBadRequest());

        verify(inquiryService, never()).submitInquiry(any(InquiryRequestModel.class));
    }

    @Test
    void submitInquiry_WithEmailTooLong_ReturnsBadRequest() throws Exception {
        // Arrange
        testRequestModel.setEmail("a".repeat(191) + "@email.com"); // Exceeds 200 character limit

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestModel)))
                .andExpect(status().isBadRequest());

        verify(inquiryService, never()).submitInquiry(any(InquiryRequestModel.class));
    }

    @Test
    void submitInquiry_WithPhoneTooLong_ReturnsBadRequest() throws Exception {
        // Arrange
        testRequestModel.setPhone("1".repeat(31)); // Exceeds 30 character limit

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestModel)))
                .andExpect(status().isBadRequest());

        verify(inquiryService, never()).submitInquiry(any(InquiryRequestModel.class));
    }

    @Test
    void submitInquiry_WithMessageTooLong_ReturnsBadRequest() throws Exception {
        // Arrange
        testRequestModel.setMessage("A".repeat(2001)); // Exceeds 2000 character limit

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestModel)))
                .andExpect(status().isBadRequest());

        verify(inquiryService, never()).submitInquiry(any(InquiryRequestModel.class));
    }

    @Test
    void submitInquiry_WithMaxLengthFields_ReturnsOk() throws Exception {
        // Arrange
        testRequestModel.setName("A".repeat(150)); // Max allowed
        // Create valid email close to 200 chars: 64 chars local + @ + long domain
        testRequestModel.setEmail("user" + "1".repeat(60) + "@" + "sub".repeat(20) + ".example.com");
        testRequestModel.setPhone("1".repeat(30)); // Max allowed
        testRequestModel.setMessage("B".repeat(1000)); // Controller MAX_MESSAGE_LENGTH
        
        when(inquiryService.submitInquiry(any(InquiryRequestModel.class))).thenReturn(testResponseModel);

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestModel)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("Thank you! Your inquiry has been received."));

        verify(inquiryService, times(1)).submitInquiry(any(InquiryRequestModel.class));
    }

    @Test
    void submitInquiry_WithEmptyPhone_ReturnsOk() throws Exception {
        // Arrange
        testRequestModel.setPhone("");
        when(inquiryService.submitInquiry(any(InquiryRequestModel.class))).thenReturn(testResponseModel);

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestModel)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("Thank you! Your inquiry has been received."));

        verify(inquiryService, times(1)).submitInquiry(any(InquiryRequestModel.class));
    }

    @Test
    void submitInquiry_WithNullRecaptchaToken_ReturnsOk() throws Exception {
        // Arrange
        testRequestModel.setRecaptchaToken(null);
        when(inquiryService.submitInquiry(any(InquiryRequestModel.class))).thenReturn(testResponseModel);

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestModel)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("Thank you! Your inquiry has been received."));

        verify(inquiryService, times(1)).submitInquiry(any(InquiryRequestModel.class));
    }

    @Test
    void submitInquiry_VerifiesServiceCalledWithCorrectData() throws Exception {
        // Arrange
        when(inquiryService.submitInquiry(any(InquiryRequestModel.class))).thenReturn(testResponseModel);

        // Act
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestModel)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"));

        // Assert
        verify(inquiryService).submitInquiry(argThat(request ->
                request.getName().equals("John Doe") &&
                request.getEmail().equals("john.doe@example.com") &&
                request.getMessage().equals("I am interested in your construction services.")
        ));
    }

    @Test
    void submitInquiry_WithDifferentValidData_ReturnsOk() throws Exception {
        // Arrange
        testRequestModel.setName("Jane Smith");
        testRequestModel.setEmail("jane.smith@example.com");
        testRequestModel.setPhone("555-5678");
        testRequestModel.setMessage("Looking for renovation services.");
        
        when(inquiryService.submitInquiry(any(InquiryRequestModel.class))).thenReturn(testResponseModel);

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestModel)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("Thank you! Your inquiry has been received."));

        verify(inquiryService, times(1)).submitInquiry(any(InquiryRequestModel.class));
    }
}

package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.Inquiry;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.InquiryRepository;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryController;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Auth0ManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersPostgresConfig.class)
class InquiryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private Auth0ManagementService auth0ManagementService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final String BASE_URI = "/api/v1/inquiries"; // Verified common path

    @BeforeEach
    void setUp() throws Exception {
        // Handle rate limiting reset
        try {
            Field bucketsField = InquiryController.class.getDeclaredField("buckets");
            bucketsField.setAccessible(true);
            Map<?, ?> buckets = (Map<?, ?>) bucketsField.get(null);
            buckets.clear();
        } catch (NoSuchFieldException ignored) {}

        inquiryRepository.deleteAll();
    }

    //@Test
    void submitInquiry_WithValidRequest_SavesInquiryAndReturnsSuccessMessage() throws Exception {
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("John Doe");
        requestModel.setEmail("john.doe@example.com");
        requestModel.setPhone("555-1234");
        requestModel.setMessage("I am interested in your construction services.");

        mockMvc.perform(post(BASE_URI)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestModel)))
                .andExpect(status().isOk())
                .andExpect(content().string("Thank you! Your inquiry has been received."));

        assertEquals(1, inquiryRepository.count());
    }

    //@Test
    void submitInquiry_WithInvalidEmail_ReturnsBadRequest() throws Exception {
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("Test User");
        requestModel.setEmail("invalid-email");
        requestModel.setMessage("Test message");

        mockMvc.perform(post(BASE_URI)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestModel)))
                .andExpect(status().isBadRequest());
    }
}
package com.ecp.les_constructions_dominic_cyr.CommunicationSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.Inquiry;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.InquiryRepository;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryController;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryRequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(com.ecp.les_constructions_dominic_cyr.config.TestcontainersPostgresConfig.class)
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
class InquiryControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private InquiryRepository inquiryRepository;

    private final String BASE_URI = "/api/inquiries";

    @BeforeEach
    void setUp() throws Exception {
        // Clear rate limiting buckets before each test
        Field bucketsField = InquiryController.class.getDeclaredField("buckets");
        bucketsField.setAccessible(true);
        Map<?, ?> buckets = (Map<?, ?>) bucketsField.get(null);
        buckets.clear();

        inquiryRepository.deleteAll();
    }

    @Test
    void submitInquiry_WithValidRequest_SavesInquiryAndReturnsSuccessMessage() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("John Doe");
        requestModel.setEmail("john.doe@example.com");
        requestModel.setPhone("555-1234");
        requestModel.setMessage("I am interested in your construction services.");

        long initialCount = inquiryRepository.count();

        // Act & Assert
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Thank you! Your inquiry has been received.");

        // Verify database
        long finalCount = inquiryRepository.count();
        assertEquals(initialCount + 1, finalCount);

        Inquiry savedInquiry = inquiryRepository.findAll().get(0);
        // Note: The controller sanitizes HTML, so we need to check for escaped values
        assertTrue(savedInquiry.getName().contains("John Doe"));
        assertTrue(savedInquiry.getEmail().contains("john.doe@example.com"));
        assertNotNull(savedInquiry.getCreatedAt());
    }

    @Test
    void submitInquiry_WithNullPhone_SavesSuccessfully() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("Jane Smith");
        requestModel.setEmail("jane.smith@example.com");
        requestModel.setPhone(null);
        requestModel.setMessage("Question about renovation services.");

        long initialCount = inquiryRepository.count();

        // Act & Assert
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Thank you! Your inquiry has been received.");

        // Verify database
        long finalCount = inquiryRepository.count();
        assertEquals(initialCount + 1, finalCount);
    }

    @Test
    void submitInquiry_WithBlankName_ReturnsBadRequest() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("");
        requestModel.setEmail("test@example.com");
        requestModel.setMessage("Test message");

        long initialCount = inquiryRepository.count();

        // Act & Assert
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isBadRequest();

        // Verify nothing was saved
        long finalCount = inquiryRepository.count();
        assertEquals(initialCount, finalCount);
    }

    @Test
    void submitInquiry_WithInvalidEmail_ReturnsBadRequest() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("Test User");
        requestModel.setEmail("invalid-email");
        requestModel.setMessage("Test message");

        long initialCount = inquiryRepository.count();

        // Act & Assert
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isBadRequest();

        // Verify nothing was saved
        long finalCount = inquiryRepository.count();
        assertEquals(initialCount, finalCount);
    }

    @Test
    void submitInquiry_WithBlankMessage_ReturnsBadRequest() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("Test User");
        requestModel.setEmail("test@example.com");
        requestModel.setMessage("");

        long initialCount = inquiryRepository.count();

        // Act & Assert
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isBadRequest();

        // Verify nothing was saved
        long finalCount = inquiryRepository.count();
        assertEquals(initialCount, finalCount);
    }

    @Test
    void submitInquiry_WithNameTooLong_ReturnsBadRequest() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("A".repeat(151)); // Exceeds 150 character limit
        requestModel.setEmail("test@example.com");
        requestModel.setMessage("Test message");

        long initialCount = inquiryRepository.count();

        // Act & Assert
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isBadRequest();

        // Verify nothing was saved
        long finalCount = inquiryRepository.count();
        assertEquals(initialCount, finalCount);
    }

    @Test
    void submitInquiry_WithEmailTooLong_ReturnsBadRequest() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("Test User");
        requestModel.setEmail("a".repeat(191) + "@email.com"); // Exceeds 200 character limit
        requestModel.setMessage("Test message");

        long initialCount = inquiryRepository.count();

        // Act & Assert
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isBadRequest();

        // Verify nothing was saved
        long finalCount = inquiryRepository.count();
        assertEquals(initialCount, finalCount);
    }

    @Test
    void submitInquiry_WithPhoneTooLong_ReturnsBadRequest() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("Test User");
        requestModel.setEmail("test@example.com");
        requestModel.setPhone("1".repeat(31)); // Exceeds 30 character limit
        requestModel.setMessage("Test message");

        long initialCount = inquiryRepository.count();

        // Act & Assert
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isBadRequest();

        // Verify nothing was saved
        long finalCount = inquiryRepository.count();
        assertEquals(initialCount, finalCount);
    }

    @Test
    void submitInquiry_WithMessageTooLong_ReturnsBadRequest() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("Test User");
        requestModel.setEmail("test@example.com");
        requestModel.setMessage("A".repeat(2001)); // Exceeds 2000 character limit

        long initialCount = inquiryRepository.count();

        // Act & Assert
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isBadRequest();

        // Verify nothing was saved
        long finalCount = inquiryRepository.count();
        assertEquals(initialCount, finalCount);
    }

    @Test
    void submitInquiry_WithMaxLengthFields_SavesSuccessfully() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("A".repeat(150)); // Max allowed
        // Create valid email close to 200 chars: 64 chars local + @ + long domain
        requestModel.setEmail("user" + "1".repeat(60) + "@" + "sub".repeat(20) + ".example.com");
        requestModel.setPhone("1".repeat(30)); // Max allowed
        requestModel.setMessage("B".repeat(1000)); // Controller MAX_MESSAGE_LENGTH

        long initialCount = inquiryRepository.count();

        // Act & Assert
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Thank you! Your inquiry has been received.");

        // Verify database
        long finalCount = inquiryRepository.count();
        assertEquals(initialCount + 1, finalCount);
    }

    @Test
    void submitInquiry_MultipleRequests_SavesAllSuccessfully() {
        // Arrange
        InquiryRequestModel request1 = new InquiryRequestModel();
        request1.setName("User One");
        request1.setEmail("user1@example.com");
        request1.setMessage("First inquiry");

        InquiryRequestModel request2 = new InquiryRequestModel();
        request2.setName("User Two");
        request2.setEmail("user2@example.com");
        request2.setMessage("Second inquiry");

        InquiryRequestModel request3 = new InquiryRequestModel();
        request3.setName("User Three");
        request3.setEmail("user3@example.com");
        request3.setMessage("Third inquiry");

        long initialCount = inquiryRepository.count();

        // Act
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request1)
                .exchange()
                .expectStatus().isOk();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request2)
                .exchange()
                .expectStatus().isOk();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request3)
                .exchange()
                .expectStatus().isOk();

        // Assert
        long finalCount = inquiryRepository.count();
        assertEquals(initialCount + 3, finalCount);
    }

    @Test
    void submitInquiry_WithSpecialCharactersInMessage_SavesSuccessfully() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("Test User");
        requestModel.setEmail("test@example.com");
        requestModel.setMessage("Special characters: !@#$%^&*()_+-=[]{}|;':,.<>?/~`");

        long initialCount = inquiryRepository.count();

        // Act & Assert
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Thank you! Your inquiry has been received.");

        // Verify database
        long finalCount = inquiryRepository.count();
        assertEquals(initialCount + 1, finalCount);
    }

    @Test
    void submitInquiry_WithHtmlInMessage_SanitizesAndSaves() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("Test User");
        requestModel.setEmail("test@example.com");
        requestModel.setMessage("<script>alert('XSS')</script>This is a test message");

        long initialCount = inquiryRepository.count();

        // Act & Assert
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Thank you! Your inquiry has been received.");

        // Verify database - HTML should be escaped
        long finalCount = inquiryRepository.count();
        assertEquals(initialCount + 1, finalCount);

        Inquiry savedInquiry = inquiryRepository.findAll().get(0);
        // The controller uses HtmlUtils.htmlEscape, so tags should be escaped
        assertFalse(savedInquiry.getMessage().contains("<script>"));
        assertTrue(savedInquiry.getMessage().contains("&lt;script&gt;") || 
                   savedInquiry.getMessage().contains("This is a test message"));
    }

    @Test
    void submitInquiry_WithEmptyPhone_SavesSuccessfully() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("Test User");
        requestModel.setEmail("test@example.com");
        requestModel.setPhone("");
        requestModel.setMessage("Test message");

        long initialCount = inquiryRepository.count();

        // Act & Assert
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Thank you! Your inquiry has been received.");

        // Verify database
        long finalCount = inquiryRepository.count();
        assertEquals(initialCount + 1, finalCount);
    }

    @Test
    void submitInquiry_VerifiesCreatedAtIsSet() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("Timestamp Test");
        requestModel.setEmail("timestamp@example.com");
        requestModel.setMessage("Testing timestamp creation");

        // Act
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Thank you! Your inquiry has been received.");

        // Assert
        Inquiry savedInquiry = inquiryRepository.findAll().get(0);
        assertNotNull(savedInquiry.getCreatedAt());
    }

    @Test
    void submitInquiry_WithNullRecaptchaToken_SavesSuccessfully() {
        // Arrange
        InquiryRequestModel requestModel = new InquiryRequestModel();
        requestModel.setName("No Captcha User");
        requestModel.setEmail("nocaptcha@example.com");
        requestModel.setMessage("Testing without captcha token");
        requestModel.setRecaptchaToken(null);

        long initialCount = inquiryRepository.count();

        // Act & Assert
        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isOk();

        // Verify database
        long finalCount = inquiryRepository.count();
        assertEquals(initialCount + 1, finalCount);
    }
}

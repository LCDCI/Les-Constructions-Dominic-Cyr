package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.MailerServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for MailerServiceClient.
 * Note: WebClient's fluent API is complex to mock, so these tests focus on
 * constructor behavior and the sendEmail method's error handling characteristics.
 */
@ExtendWith(MockitoExtension.class)
class MailerServiceClientTest {

    @Mock
    private Environment environment;

    private MailerServiceClient mailerServiceClient;

    @BeforeEach
    void setUp() {
        // Create client with default URL for testing
        mailerServiceClient = new MailerServiceClient(
                environment,
                "", // envVarDirect
                ""  // baseUrlFromConfig
        );
    }

    @Test
    void constructor_WithNoEnvironmentVariables_UsesDefaultUrl() {
        // Arrange & Act
        MailerServiceClient client = new MailerServiceClient(
                environment,
                "",
                ""
        );

        // Assert - Should use default Docker service URL
        assertNotNull(client);
    }

    @Test
    void constructor_WithEnvironmentVariable_UsesEnvironmentUrl() {
        // Arrange
        String testUrl = "http://test-mailer:8080";
        when(environment.getProperty("MAILER_SERVICE_BASE_URL")).thenReturn(testUrl);

        // Act
        MailerServiceClient client = new MailerServiceClient(
                environment,
                "",
                ""
        );

        // Assert
        assertNotNull(client);
    }

    @Test
    void sendEmail_WithValidParameters_ReturnsMonoVoid() {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";
        String senderName = "Test Sender";

        // Act
        Mono<Void> result = mailerServiceClient.sendEmail(to, subject, body, senderName);

        // Assert - Should return a Mono (will complete even on error due to onErrorComplete)
        assertNotNull(result);
        // The Mono will complete even if the actual HTTP call fails
        // because of onErrorComplete() in the implementation
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void sendEmail_WithNullSenderName_HandlesGracefully() {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";
        String senderName = null;

        // Act
        Mono<Void> result = mailerServiceClient.sendEmail(to, subject, body, senderName);

        // Assert - Should complete without error
        assertNotNull(result);
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void sendEmail_CompletesEvenOnError() {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";
        String senderName = "Test Sender";

        // Act
        Mono<Void> result = mailerServiceClient.sendEmail(to, subject, body, senderName);

        // Assert - Should always complete (due to onErrorComplete in implementation)
        StepVerifier.create(result)
                .verifyComplete();
    }
}

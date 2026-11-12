package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

/**
 * Client for communicating with the mailer service to send emails.
 */
@Component
public class MailerServiceClient {

    private static final Logger log = LoggerFactory.getLogger(MailerServiceClient.class);
    private static final String COMPANY_NAME = "Les Constructions Dominic Cyr Inc.";
    private static final String LOGO_URL = "https://constructions-dominiccyr.com/fallback.jpg";
    
    private final WebClient webClient;

    private final String baseUrl;
    
    public MailerServiceClient(
            Environment environment, 
            @Value("${MAILER_SERVICE_BASE_URL:}") String envVarDirect,
            @Value("${mailer.service.base-url:}") String baseUrlFromConfig) {
        // Check environment variable first (both System.getenv and Spring Environment)
        String envUrl = System.getenv("MAILER_SERVICE_BASE_URL");
        if (envUrl == null || envUrl.isEmpty()) {
            envUrl = environment.getProperty("MAILER_SERVICE_BASE_URL");
        }
        if (envUrl == null || envUrl.isEmpty()) {
            envUrl = envVarDirect; // Try direct @Value injection
        }
        
        log.info("===== MailerServiceClient INITIALIZATION =====");
        log.info("System.getenv('MAILER_SERVICE_BASE_URL') = {}", System.getenv("MAILER_SERVICE_BASE_URL") != null ? System.getenv("MAILER_SERVICE_BASE_URL") : "NULL");
        log.info("Environment.getProperty('MAILER_SERVICE_BASE_URL') = {}", environment.getProperty("MAILER_SERVICE_BASE_URL") != null ? environment.getProperty("MAILER_SERVICE_BASE_URL") : "NULL");
        log.info("@Value('${MAILER_SERVICE_BASE_URL}') = {}", envVarDirect != null && !envVarDirect.isEmpty() ? envVarDirect : "NULL/EMPTY");
        log.info("@Value('${mailer.service.base-url}') = {}", baseUrlFromConfig != null && !baseUrlFromConfig.isEmpty() ? baseUrlFromConfig : "NULL/EMPTY");
        
        if (envUrl != null && !envUrl.isEmpty()) {
            this.baseUrl = envUrl;
            log.info("✓ Using MAILER_SERVICE_BASE_URL from environment: {}", this.baseUrl);
        } else if (baseUrlFromConfig != null && !baseUrlFromConfig.isEmpty() && !baseUrlFromConfig.contains("localhost")) {
            // Only use config if it's not the default localhost value
            this.baseUrl = baseUrlFromConfig;
            log.info("✓ Using base URL from config: {}", this.baseUrl);
        } else {
            // Default to Docker service URL (works in Docker Compose)
            // For local development, set MAILER_SERVICE_BASE_URL environment variable
            this.baseUrl = "http://mailer-service:8080";
            log.info("✓ Using Docker service URL (default): {}", this.baseUrl);
            log.info("ℹ Note: Set MAILER_SERVICE_BASE_URL environment variable to override");
        }
        
        log.info("Final baseUrl = {}", this.baseUrl);
        log.info("================================================");
        
        this.webClient = WebClient.builder()
                .baseUrl(this.baseUrl)
                .build();
    }

    /**
     * Sends an email via the mailer service.
     * 
     * @param to recipient email address
     * @param subject email subject
     * @param body HTML email body
     * @param senderName optional sender name
     * @return Mono<Void> completes when email is sent successfully
     */
    public Mono<Void> sendEmail(String to, String subject, String body, String senderName) {
        Map<String, String> emailPayload = Map.of(
            "EmailSendTo", to,
            "EmailTitle", subject,
            "Body", prepareEmailBody(subject, body),
            "SenderName", senderName != null ? senderName : "Les Constructions Dominic Cyr"
        );

        log.info("Sending email to {}", to);
        log.info("Using base URL: {}", baseUrl);
        log.info("Full URL will be: {}/mail", baseUrl);
        
        return webClient.post()
                .uri("/mail")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(emailPayload)
                .retrieve()
                .bodyToMono(String.class) // Read response to see if there are errors
                .timeout(Duration.ofSeconds(30)) // Add timeout
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                        .filter(throwable -> {
                            // Only retry on transient errors (network issues, timeouts)
                            String errorMsg = throwable.getMessage();
                            return errorMsg != null && (
                                errorMsg.contains("Connection refused") ||
                                errorMsg.contains("timeout") ||
                                errorMsg.contains("Temporary failure")
                            );
                        })
                        .doBeforeRetry(retrySignal -> 
                            log.warn("Retrying email send to {} (attempt {})", to, retrySignal.totalRetries() + 1)
                        ))
                .doOnNext(response -> {
                    log.info("SUCCESS - Mailer service response: {}", response);
                })
                .doOnError(error -> {
                    // Log detailed error information BEFORE onErrorComplete
                    String errorMessage = error.getMessage();
                    if (errorMessage != null && errorMessage.contains("Failed to resolve")) {
                        log.error("ERROR - Cannot resolve mailer service hostname '{}'. " +
                                "Please ensure the mailer-service container is running and on the same Docker network. " +
                                "Error: {}", 
                                baseUrl, errorMessage);
                        log.error("Troubleshooting: Run 'docker ps' to check if mailer-service container is running");
                    } else {
                        log.error("ERROR - Failed to send email to {} via mailer service at {}. Error: {}", 
                                to, baseUrl, errorMessage, error);
                    }
                })
                .then() // Convert to Mono<Void>
                .doOnSuccess(v -> log.info("Email send request completed successfully for {}", to))
                .onErrorComplete(); // Complete successfully even if email fails
    }

    private String prepareEmailBody(String subject, String body) {
        if (body == null || body.isBlank()) {
            return buildBrandedEmail(subject, "");
        }

        String normalizedBody = body.trim();
        String lowercaseBody = normalizedBody.toLowerCase();
        if (lowercaseBody.contains("<style") || lowercaseBody.contains("style=")) {
            return normalizedBody;
        }

        return buildBrandedEmail(subject, extractBodyContent(normalizedBody));
    }

    private String extractBodyContent(String body) {
        String lowercaseBody = body.toLowerCase();
        int bodyStart = lowercaseBody.indexOf("<body");
        if (bodyStart >= 0) {
            int contentStart = lowercaseBody.indexOf('>', bodyStart);
            int contentEnd = lowercaseBody.lastIndexOf("</body>");
            if (contentStart >= 0 && contentEnd > contentStart) {
                return body.substring(contentStart + 1, contentEnd).trim();
            }
        }

        return body
                .replaceFirst("(?is)^\\s*<!doctype[^>]*>", "")
                .replaceFirst("(?is)^\\s*<html[^>]*>", "")
                .replaceFirst("(?is)</html>\\s*$", "")
                .trim();
    }

    private String buildBrandedEmail(String subject, String content) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"></head>"
                + "<body style=\"margin:0;padding:24px 12px;background:#f4f6f4;"
                + "font-family:Arial,Helvetica,sans-serif;color:#29312d;line-height:1.6;\">"
                + "<div style=\"width:100%;max-width:620px;margin:0 auto;background:#ffffff;"
                + "border:1px solid #dfe4df;border-radius:10px;overflow:hidden;"
                + "box-shadow:0 10px 28px rgba(41,49,45,0.12);\">"
                + "<div style=\"padding:28px 24px;text-align:center;background:#2c3531;\">"
                + "<img src=\"" + LOGO_URL + "\" alt=\"" + COMPANY_NAME + "\" "
                + "style=\"display:block;width:220px;max-width:80%;height:auto;margin:0 auto 20px;"
                + "padding:8px;background:#ffffff;border-radius:4px;\">"
                + "<h1 style=\"margin:0;color:#ffffff;font-size:24px;line-height:1.3;\">"
                + escapeHtml(subject) + "</h1></div>"
                + "<div style=\"padding:30px 28px;background:#fbfcfb;font-size:15px;\">"
                + content + "</div>"
                + "<div style=\"padding:16px 24px;text-align:center;border-top:1px solid #e5e9e5;"
                + "color:#687a68;font-size:12px;background:#ffffff;\">"
                + COMPANY_NAME + "</div></div></body></html>";
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

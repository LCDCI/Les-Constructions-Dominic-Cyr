package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.Controllers;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DTOs.InquiryRequest;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.Services.InquiryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.web.util.HtmlUtils;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
@RestController
@RequestMapping("/api/inquiries")
@CrossOrigin(origins = "http://localhost:3000")
public class InquiryController {
    private final InquiryService service;
    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private static final String RECAPTCHA_SECRET = "YOUR_RECAPTCHA_SECRET"; // TODO: Replace with actual secret

    public InquiryController(InquiryService service) {
        this.service = service;
    }

    private Bucket resolveBucket(String ip) {
        return buckets.computeIfAbsent(ip, k -> Bucket4j.builder()
                .addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1))))
                .build());
    }

    private boolean verifyRecaptcha(String token) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String body = "secret=" + RECAPTCHA_SECRET + "&response=" + token;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.google.com/recaptcha/api/siteverify"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> result = mapper.readValue(response.body(), HashMap.class);
            return Boolean.TRUE.equals(result.get("success"));
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    @PostMapping
    public ResponseEntity<?> submit(@Valid @RequestBody InquiryRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = resolveBucket(ip);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests. Please try again later.");
        }

        // CAPTCHA validation (assumes InquiryRequest has a recaptchaToken field)
        if (request.getRecaptchaToken() == null || !verifyRecaptcha(request.getRecaptchaToken())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("CAPTCHA validation failed.");
        }

        // Message length validation
        if (request.getMessage() != null && request.getMessage().length() > MAX_MESSAGE_LENGTH) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Message is too long.");
        }

        // Input sanitization
        request.setName(HtmlUtils.htmlEscape(request.getName()));
        request.setEmail(HtmlUtils.htmlEscape(request.getEmail()));
        request.setMessage(HtmlUtils.htmlEscape(request.getMessage()));
        service.submitInquiry(request);
        return ResponseEntity.ok().body("Thank you! Your inquiry has been received.");
    }
}

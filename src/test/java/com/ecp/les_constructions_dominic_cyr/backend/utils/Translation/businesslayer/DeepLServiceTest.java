package com.ecp.les_constructions_dominic_cyr.backend.utils.Translation.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.DeepLService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeepLServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    private DeepLService deepLService;

    @BeforeEach
    void setUp() {
        deepLService = new DeepLService("test-api-key", "", true);
        ReflectionTestUtils.setField(deepLService, "webClient", webClient);
    }

    @Test
    void toDeepLLanguageCode_WithLowercase_ConvertsToUppercase() {
        assertEquals("EN", deepLService.toDeepLLanguageCode("en"));
        assertEquals("FR", deepLService.toDeepLLanguageCode("fr"));
    }

    @Test
    void toDeepLLanguageCode_WithUppercase_ReturnsUppercase() {
        assertEquals("EN", deepLService.toDeepLLanguageCode("EN"));
        assertEquals("FR", deepLService.toDeepLLanguageCode("FR"));
    }

    @Test
    void toDeepLLanguageCode_WithNull_ReturnsNull() {
        assertNull(deepLService.toDeepLLanguageCode(null));
    }

    @Test
    void getOppositeLanguage_WithEnglish_ReturnsFrench() {
        assertEquals("FR", deepLService.getOppositeLanguage("en"));
        assertEquals("FR", deepLService.getOppositeLanguage("EN"));
    }

    @Test
    void getOppositeLanguage_WithFrench_ReturnsEnglish() {
        assertEquals("EN", deepLService.getOppositeLanguage("fr"));
        assertEquals("EN", deepLService.getOppositeLanguage("FR"));
    }

    @Test
    void getOppositeLanguage_WithNull_ReturnsEN() {
        assertEquals("EN", deepLService.getOppositeLanguage(null));
    }

    @Test
    void getOppositeLanguage_WithOtherLanguage_ReturnsUppercase() {
        assertEquals("ES", deepLService.getOppositeLanguage("es"));
    }

    @Test
    void isApiKeyConfigured_WithValidKey_ReturnsTrue() {
        DeepLService service = new DeepLService("valid-key", "", true);
        assertTrue(service.isApiKeyConfigured());
    }

    @Test
    void isApiKeyConfigured_WithEmptyKey_ReturnsFalse() {
        DeepLService service = new DeepLService("", "", true);
        assertFalse(service.isApiKeyConfigured());
    }

    @Test
    void isApiKeyConfigured_WithNullKey_ReturnsFalse() {
        DeepLService service = new DeepLService(null, "", true);
        assertFalse(service.isApiKeyConfigured());
    }

    @Test
    void isApiKeyConfigured_WithWhitespaceKey_ReturnsFalse() {
        DeepLService service = new DeepLService("   ", "", true);
        assertFalse(service.isApiKeyConfigured());
    }

    @Test
    void getApiUrl_WithFreeApi_ReturnsFreeUrl() {
        DeepLService service = new DeepLService("key", "", true);
        assertEquals("https://api-free.deepl.com/v2", service.getApiUrl());
    }

    @Test
    void getApiUrl_WithPaidApi_ReturnsPaidUrl() {
        DeepLService service = new DeepLService("key", "", false);
        assertEquals("https://api.deepl.com/v2", service.getApiUrl());
    }

    @Test
    void getApiUrl_WithCustomUrl_ReturnsCustomUrl() {
        DeepLService service = new DeepLService("key", "https://custom.deepl.com/v2", true);
        assertEquals("https://custom.deepl.com/v2", service.getApiUrl());
    }

    @Test
    void translatePdf_WithNoApiKey_ReturnsError() {
        DeepLService service = new DeepLService("", "", true);
        
        StepVerifier.create(service.translatePdf(new byte[]{1, 2, 3}, "EN", "FR"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void translatePdf_WithNullApiKey_ReturnsError() {
        DeepLService service = new DeepLService(null, "", true);
        
        StepVerifier.create(service.translatePdf(new byte[]{1, 2, 3}, "EN", "FR"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void translatePdf_WithValidApiKey_ProcessesTranslation() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", "doc-123");
        uploadResponse.put("document_key", "key-456");
        
        Map<String, Object> statusResponse = new HashMap<>();
        statusResponse.put("status", "done");
        
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap("translated pdf".getBytes());
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(uploadResponse));
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(statusResponse));
        when(responseSpec.bodyToFlux(DataBuffer.class))
                .thenReturn(reactor.core.publisher.Flux.just(dataBuffer));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertTrue(result.length > 0);
                })
                .verifyComplete();
    }

    @Test
    void translatePdf_WithUploadFailure_ReturnsError() {
        byte[] pdfData = new byte[]{1, 2, 3};
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.error(new RuntimeException("Upload failed")));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .expectError()
                .verify();
    }

    @Test
    void translatePdf_WithMissingDocumentId_ReturnsError() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", null);
        uploadResponse.put("document_key", "key-456");
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(uploadResponse));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void translatePdf_WithErrorStatus_ReturnsError() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", "doc-123");
        uploadResponse.put("document_key", "key-456");
        
        Map<String, Object> statusResponse = new HashMap<>();
        statusResponse.put("status", "error");
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(uploadResponse));
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(statusResponse));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void translatePdf_WithNullSourceLanguage_StillUploads() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", "doc-123");
        uploadResponse.put("document_key", "key-456");
        
        Map<String, Object> statusResponse = new HashMap<>();
        statusResponse.put("status", "done");
        
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap("translated pdf".getBytes());
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // First call is for upload, subsequent calls are for status checks
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(uploadResponse))
                .thenReturn(Mono.just(statusResponse));
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(DataBuffer.class))
                .thenReturn(reactor.core.publisher.Flux.just(dataBuffer));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, null, "FR"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertTrue(result.length > 0);
                })
                .verifyComplete();
    }

    @Test
    void translatePdf_WithEmptySourceLanguage_StillUploads() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", "doc-123");
        uploadResponse.put("document_key", "key-456");
        
        Map<String, Object> statusResponse = new HashMap<>();
        statusResponse.put("status", "done");
        
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap("translated pdf".getBytes());
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // First call is for upload, subsequent calls are for status checks
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(uploadResponse))
                .thenReturn(Mono.just(statusResponse));
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(DataBuffer.class))
                .thenReturn(reactor.core.publisher.Flux.just(dataBuffer));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "", "FR"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertTrue(result.length > 0);
                })
                .verifyComplete();
    }

    @Test
    void translatePdf_WithMissingDocumentKey_ReturnsError() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", "doc-123");
        uploadResponse.put("document_key", null);
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(uploadResponse));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void translatePdf_WithTranslatingStatus_EventuallyCompletes() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", "doc-123");
        uploadResponse.put("document_key", "key-456");
        
        Map<String, Object> translatingStatus = new HashMap<>();
        translatingStatus.put("status", "translating");
        
        Map<String, Object> doneStatus = new HashMap<>();
        doneStatus.put("status", "done");
        
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap("translated pdf".getBytes());
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(uploadResponse));
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(translatingStatus))
                .thenReturn(Mono.just(doneStatus));
        when(responseSpec.bodyToFlux(DataBuffer.class))
                .thenReturn(reactor.core.publisher.Flux.just(dataBuffer));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertTrue(result.length > 0);
                })
                .verifyComplete();
    }

    @Test
    void translatePdf_WithUnknownStatus_ReturnsError() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", "doc-123");
        uploadResponse.put("document_key", "key-456");
        
        Map<String, Object> statusResponse = new HashMap<>();
        statusResponse.put("status", "unknown");
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(uploadResponse));
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(statusResponse));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void translatePdf_WithNullStatus_ReturnsError() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", "doc-123");
        uploadResponse.put("document_key", "key-456");
        
        Map<String, Object> statusResponse = new HashMap<>();
        statusResponse.put("status", null);
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(uploadResponse));
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(statusResponse));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void translatePdf_WithDownloadError_ReturnsError() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", "doc-123");
        uploadResponse.put("document_key", "key-456");
        
        Map<String, Object> statusResponse = new HashMap<>();
        statusResponse.put("status", "done");
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(uploadResponse));
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(statusResponse));
        when(responseSpec.bodyToFlux(DataBuffer.class))
                .thenReturn(reactor.core.publisher.Flux.error(new RuntimeException("Download failed")));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .expectError()
                .verify();
    }

    @Test
    void translatePdf_WithLowercaseLanguageCodes_ConvertsToUppercase() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", "doc-123");
        uploadResponse.put("document_key", "key-456");
        
        Map<String, Object> statusResponse = new HashMap<>();
        statusResponse.put("status", "done");
        
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap("translated pdf".getBytes());
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // First call is for upload, subsequent calls are for status checks
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(uploadResponse))
                .thenReturn(Mono.just(statusResponse));
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(DataBuffer.class))
                .thenReturn(reactor.core.publisher.Flux.just(dataBuffer));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "en", "fr"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertTrue(result.length > 0);
                })
                .verifyComplete();
    }

    @Test
    void translatePdf_WithMultiplePollingChecks_EventuallyCompletes() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", "doc-123");
        uploadResponse.put("document_key", "key-456");
        
        Map<String, Object> queuedStatus = new HashMap<>();
        queuedStatus.put("status", "queued");
        
        Map<String, Object> translatingStatus = new HashMap<>();
        translatingStatus.put("status", "translating");
        
        Map<String, Object> doneStatus = new HashMap<>();
        doneStatus.put("status", "done");
        
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap("translated pdf".getBytes());
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // First call is for upload, subsequent calls are for status checks
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(uploadResponse))
                .thenReturn(Mono.just(queuedStatus))
                .thenReturn(Mono.just(translatingStatus))
                .thenReturn(Mono.just(doneStatus));
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(DataBuffer.class))
                .thenReturn(reactor.core.publisher.Flux.just(dataBuffer));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertTrue(result.length > 0);
                })
                .verifyComplete();
    }

    @Test
    void translatePdf_WithQueuedStatus_EventuallyCompletes() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", "doc-123");
        uploadResponse.put("document_key", "key-456");
        
        Map<String, Object> queuedStatus = new HashMap<>();
        queuedStatus.put("status", "queued");
        
        Map<String, Object> doneStatus = new HashMap<>();
        doneStatus.put("status", "done");
        
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap("translated pdf".getBytes());
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // First call is for upload, subsequent calls are for status checks
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(uploadResponse))
                .thenReturn(Mono.just(queuedStatus))
                .thenReturn(Mono.just(doneStatus));
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(DataBuffer.class))
                .thenReturn(reactor.core.publisher.Flux.just(dataBuffer));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertTrue(result.length > 0);
                })
                .verifyComplete();
    }

    @Test
    void translatePdf_WithMultipleDataBuffers_CombinesCorrectly() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", "doc-123");
        uploadResponse.put("document_key", "key-456");
        
        Map<String, Object> statusResponse = new HashMap<>();
        statusResponse.put("status", "done");
        
        DataBuffer buffer1 = DefaultDataBufferFactory.sharedInstance.wrap("part1".getBytes());
        DataBuffer buffer2 = DefaultDataBufferFactory.sharedInstance.wrap("part2".getBytes());
        DataBuffer buffer3 = DefaultDataBufferFactory.sharedInstance.wrap("part3".getBytes());
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // First call is for upload, subsequent calls are for status checks
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(uploadResponse))
                .thenReturn(Mono.just(statusResponse));
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(DataBuffer.class))
                .thenReturn(reactor.core.publisher.Flux.just(buffer1, buffer2, buffer3));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertTrue(result.length > 0);
                    // Should combine all buffers
                    String combined = new String(result);
                    assertTrue(combined.contains("part1") || combined.contains("part2") || combined.contains("part3"));
                })
                .verifyComplete();
    }

    @Test
    void translatePdf_WithEmptyStatusResponse_ReturnsError() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", "doc-123");
        uploadResponse.put("document_key", "key-456");
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(uploadResponse));
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(new HashMap<>()));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void translatePdf_WithStatusCheckError_ReturnsError() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", "doc-123");
        uploadResponse.put("document_key", "key-456");
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(uploadResponse));
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.error(new RuntimeException("Status check failed")));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .expectError()
                .verify();
    }

    @Test
    void translatePdf_WithEmptyDataBuffers_HandlesCorrectly() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", "doc-123");
        uploadResponse.put("document_key", "key-456");
        
        Map<String, Object> statusResponse = new HashMap<>();
        statusResponse.put("status", "done");
        
        DataBuffer emptyBuffer = DefaultDataBufferFactory.sharedInstance.wrap(new byte[0]);
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // First call is for upload, subsequent calls are for status checks
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(uploadResponse))
                .thenReturn(Mono.just(statusResponse));
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(DataBuffer.class))
                .thenReturn(reactor.core.publisher.Flux.just(emptyBuffer));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(0, result.length);
                })
                .verifyComplete();
    }

    @Test
    void translatePdf_WithUploadResponseMissingFields_ReturnsError() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        // Missing document_id and document_key
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(uploadResponse));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void translatePdf_WithUploadResponseEmptyMap_ReturnsError() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(uploadResponse));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void translatePdf_WithStatusQueuedThenDone_Completes() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", "doc-123");
        uploadResponse.put("document_key", "key-456");
        
        Map<String, Object> queuedStatus = new HashMap<>();
        queuedStatus.put("status", "queued");
        
        Map<String, Object> doneStatus = new HashMap<>();
        doneStatus.put("status", "done");
        
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap("translated".getBytes());
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // First call is for upload, subsequent calls are for status checks
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(uploadResponse))
                .thenReturn(Mono.just(queuedStatus))
                .thenReturn(Mono.just(doneStatus));
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(DataBuffer.class))
                .thenReturn(reactor.core.publisher.Flux.just(dataBuffer));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertTrue(result.length > 0);
                })
                .verifyComplete();
    }

    @Test
    void translatePdf_WithStatusResponseEmptyString_ReturnsUnknown() {
        byte[] pdfData = new byte[]{1, 2, 3};
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", "doc-123");
        uploadResponse.put("document_key", "key-456");
        
        Map<String, Object> statusResponse = new HashMap<>();
        statusResponse.put("status", "");
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // First call is for upload, subsequent calls are for status checks
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(uploadResponse))
                .thenReturn(Mono.just(statusResponse));
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void translatePdf_WithLargePdfData_HandlesCorrectly() {
        byte[] pdfData = new byte[50000];
        for (int i = 0; i < pdfData.length; i++) {
            pdfData[i] = (byte) (i % 256);
        }
        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", "doc-123");
        uploadResponse.put("document_key", "key-456");
        
        Map<String, Object> statusResponse = new HashMap<>();
        statusResponse.put("status", "done");
        
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap("large translated pdf".getBytes());
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/document")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // First call is for upload, subsequent calls are for status checks
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(uploadResponse))
                .thenReturn(Mono.just(statusResponse));
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(DataBuffer.class))
                .thenReturn(reactor.core.publisher.Flux.just(dataBuffer));
        
        StepVerifier.create(deepLService.translatePdf(pdfData, "EN", "FR"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertTrue(result.length > 0);
                })
                .verifyComplete();
    }
}


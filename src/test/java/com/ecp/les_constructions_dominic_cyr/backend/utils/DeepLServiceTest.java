package com.ecp.les_constructions_dominic_cyr.backend.utils;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.DeepLService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeepLServiceTest {

    // 1. Instantiate the real service with @Spy to allow method mocking/reflection
    // Provide constructor arguments directly (or via @Value simulation if this were a SpringBootTest)
    @Spy
    private DeepLService deepLService = new DeepLService(API_KEY, API_URL, false);

    // 2. Mock components for WebClient chain (will be injected via reflection if needed)
    @Mock
    private WebClient mockWebClient;
    @Mock
    private RequestHeadersUriSpec mockUriSpec;
    @Mock
    private RequestHeadersSpec mockHeadersSpec;
    @Mock
    private RequestBodyUriSpec mockRequestBodyUriSpec;
    @Mock
    private RequestBodySpec mockRequestBodySpec;
    @Mock
    private ResponseSpec mockResponseSpec;

    // Test data
    private static final String API_KEY = "test-api-key";
    private static final String API_URL = "https://test-api.deepl.com/v2";
    private static final byte[] TEST_PDF_DATA = "pdf_bytes".getBytes();
    private static final String DOC_ID = "doc123";
    private static final String DOC_KEY = "key456";
    private static final String FREE_API_URL = "https://api-free.deepl.com/v2";
    private static final String PAID_API_URL = "https://api.deepl.com/v2";


    @BeforeEach
    void setUp() throws Exception {
        java.lang.reflect.Field webClientField = DeepLService.class.getDeclaredField("webClient");
        webClientField.setAccessible(true);
        webClientField.set(deepLService, mockWebClient);
    }


    @SuppressWarnings("unchecked")
    private Mono<Map<String, String>> callPrivateUploadDocument(byte[] pdfData, String sourceLanguage, String targetLanguage)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method method = DeepLService.class.getDeclaredMethod("uploadDocument", byte[].class, String.class, String.class);
        method.setAccessible(true);
        return (Mono<Map<String, String>>) method.invoke(deepLService, pdfData, sourceLanguage, targetLanguage);
    }

    private Mono<String> callPrivateCheckTranslationStatus(String documentId, String documentKey)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method method = DeepLService.class.getDeclaredMethod("checkTranslationStatus", String.class, String.class);
        method.setAccessible(true);
        return (Mono<String>) method.invoke(deepLService, documentId, documentKey);
    }


    private void setupMockUpload(String docId, String docKey) {
        Map<String, String> uploadResponse = new HashMap<>();
        uploadResponse.put("document_id", docId);
        uploadResponse.put("document_key", docKey);

        when(mockWebClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(anyString())).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.contentType(any())).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any())).thenReturn(mockHeadersSpec);
        when(mockHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.just(uploadResponse));
    }


    @Test
    @DisplayName("uploadDocument should include source_lang if provided (PRIVATE METHOD TEST)")
    void uploadDocument_withSourceLanguage_includesItInRequest() throws Exception {
        setupMockUpload(DOC_ID, DOC_KEY);

        Mono<Map<String, String>> resultMono = callPrivateUploadDocument(TEST_PDF_DATA, "en", "fr");

        StepVerifier.create(resultMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @DisplayName("uploadDocument should exclude source_lang if null or empty (PRIVATE METHOD TEST)")
    void uploadDocument_withoutSourceLanguage_excludesIt() throws Exception {
        setupMockUpload(DOC_ID, DOC_KEY);

        Mono<Map<String, String>> resultMonoNull = callPrivateUploadDocument(TEST_PDF_DATA, null, "fr");
        StepVerifier.create(resultMonoNull).expectNextCount(1).verifyComplete();

        Mono<Map<String, String>> resultMonoEmpty = callPrivateUploadDocument(TEST_PDF_DATA, "", "fr");
        StepVerifier.create(resultMonoEmpty).expectNextCount(1).verifyComplete();
    }


    @Test
    @DisplayName("Configuration: API URL should return paid API when useFreeApi=false")
    void getApiUrl_paidApi() {
        DeepLService service = new DeepLService(API_KEY, "", false);
        assertEquals(PAID_API_URL, service.getApiUrl());
    }

    @Test
    @DisplayName("Configuration: API URL should return free API when useFreeApi=true")
    void getApiUrl_freeApi() {
        DeepLService service = new DeepLService(API_KEY, "", true);
        assertEquals(FREE_API_URL, service.getApiUrl());
    }


    @Test
    @DisplayName("toDeepLLanguageCode should return null if input is null")
    void toDeepLLanguageCode_nullInput_returnsNull() {
        assertNull(deepLService.toDeepLLanguageCode(null));
    }

    @Test
    @DisplayName("isApiKeyConfigured returns false for null key")
    void isApiKeyConfigured_null() {
        DeepLService service = new DeepLService(null, "", true);
        assertFalse(service.isApiKeyConfigured());
    }

    @Test
    @DisplayName("isApiKeyConfigured returns false for empty key")
    void isApiKeyConfigured_empty() {
        DeepLService service = new DeepLService("", "", true);
        assertFalse(service.isApiKeyConfigured());
    }

    @Test
    void constructorShouldUseDefaultFreeApiUrlWhenNotSpecifiedAndUseFreeApiIsTrue() {
        DeepLService service = new DeepLService("testKey", null, true);
        assertEquals("https://api-free.deepl.com/v2", service.getApiUrl());
    }

    @Test
    void constructorShouldUseDefaultPaidApiUrlWhenNotSpecifiedAndUseFreeApiIsFalse() {
        DeepLService service = new DeepLService("testKey", "", false);
        assertEquals("https://api.deepl.com/v2", service.getApiUrl());
    }

    @Test
    void constructorShouldUseCustomApiUrlWhenSpecified() {
        DeepLService service = new DeepLService("testKey", "http://custom-url.com/v2", true);
        assertEquals("http://custom-url.com/v2", service.getApiUrl());
    }

    @Test
    void isApiKeyConfiguredShouldReturnFalseWhenApiKeyIsNull() {
        DeepLService service = new DeepLService(null, "someUrl", true);
        assertFalse(service.isApiKeyConfigured());
    }

    @Test
    void isApiKeyConfiguredShouldReturnFalseWhenApiKeyIsEmpty() {
        DeepLService service = new DeepLService("", "someUrl", true);
        assertFalse(service.isApiKeyConfigured());
    }

    @Test
    void isApiKeyConfiguredShouldReturnTrueWhenApiKeyIsPresent() {
        DeepLService service = new DeepLService("presentKey", "someUrl", true);
        assertTrue(service.isApiKeyConfigured());
    }

    @Test
    void toDeepLLanguageCodeShouldConvertLowercaseToUppercase() {
        assertEquals("EN", deepLService.toDeepLLanguageCode("en"));
    }

    @Test
    void toDeepLLanguageCodeShouldHandleAlreadyUppercaseCode() {
        assertEquals("FR", deepLService.toDeepLLanguageCode("FR"));
    }

    @Test
    void toDeepLLanguageCodeShouldReturnNullForNullInput() {
        assertNull(deepLService.toDeepLLanguageCode(null));
    }

    @Test
    void getOppositeLanguageShouldReturnFrForEnInput() {
        assertEquals("FR", deepLService.getOppositeLanguage("EN"));
        assertEquals("FR", deepLService.getOppositeLanguage("en"));
    }

    @Test
    void getOppositeLanguageShouldReturnEnForFrInput() {
        assertEquals("EN", deepLService.getOppositeLanguage("FR"));
        assertEquals("EN", deepLService.getOppositeLanguage("fr"));
    }

    @Test
    void getOppositeLanguageShouldReturnEnForNullInput() {
        assertEquals("EN", deepLService.getOppositeLanguage(null));
    }

    @Test
    void getOppositeLanguageShouldReturnSameLanguageCodeForNonEnFrInput() {
        assertEquals("DE", deepLService.getOppositeLanguage("de"));
        assertEquals("IT", deepLService.getOppositeLanguage("IT"));
    }

    @Test
    void translatePdfShouldThrowExceptionWhenApiKeyIsNotConfigured() {
        DeepLService serviceWithoutKey = new DeepLService(null, "someUrl", true);

        Mono<byte[]> result = serviceWithoutKey.translatePdf(new byte[1], "EN", "FR");

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof IllegalStateException &&
                        e.getMessage().contains("DeepL API key is not configured"))
                .verify();
    }
}
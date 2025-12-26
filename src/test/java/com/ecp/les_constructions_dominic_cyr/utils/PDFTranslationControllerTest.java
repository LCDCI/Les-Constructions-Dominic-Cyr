package com.ecp.les_constructions_dominic_cyr.utils;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.DeepLService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.TranslationCacheService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.presentationlayer.PDFTranslationController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PDFTranslationControllerTest {

    private WebTestClient webTestClient;

    @Mock
    private DeepLService deepLService;

    @Mock
    private TranslationCacheService cacheService;

    @InjectMocks
    private PDFTranslationController pdfTranslationController;

    private static final byte[] MOCK_PDF_BYTES = "PDF_CONTENT".getBytes();
    private static final byte[] MOCK_TRANSLATED_BYTES = "TRANSLATED_CONTENT".getBytes();
    private static final String ORIGINAL_FILENAME = "document.pdf";
    private static final String CACHE_FILENAME = "document_fr.pdf";

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToController(pdfTranslationController).build();
    }

    // Helper method to create a Mono<FilePart>
    private Mono<DataBuffer> createDataBuffer(byte[] bytes) {
        return Mono.just(org.springframework.core.io.buffer.DefaultDataBufferFactory.sharedInstance.wrap(bytes));
    }

    @Test
    void translatePdf_CacheHit_ReturnsCachedFile() {
        // Mock cache hit
        when(cacheService.getCachedTranslation(eq(ORIGINAL_FILENAME), eq("fr"))).thenReturn(MOCK_TRANSLATED_BYTES);
        when(cacheService.generateCacheFilename(eq(ORIGINAL_FILENAME), eq("fr"))).thenReturn(CACHE_FILENAME);

        // Build the multipart request body
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", MOCK_PDF_BYTES)
                .filename(ORIGINAL_FILENAME)
                .contentType(MediaType.APPLICATION_PDF);

        webTestClient.post().uri("/api/v1/pdf-translation/translate")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueEquals("Content-Disposition", "attachment; filename=\"" + CACHE_FILENAME + "\"")
                .expectBody(byte[].class).isEqualTo(MOCK_TRANSLATED_BYTES);

        verify(cacheService, times(1)).getCachedTranslation(anyString(), anyString());
        verify(deepLService, never()).translatePdf(any(), any(), any());
    }

    @Test
    void translatePdf_CacheMiss_DeepLSuccess_SavesAndReturns() {
        // Mock cache miss
        when(cacheService.getCachedTranslation(anyString(), anyString())).thenReturn(null);
        // Mock DeepL service success
        when(deepLService.translatePdf(eq(MOCK_PDF_BYTES), eq("EN"), eq("FR"))).thenReturn(Mono.just(MOCK_TRANSLATED_BYTES));
        // Mock utility methods
        when(deepLService.toDeepLLanguageCode(eq("en"))).thenReturn("EN");
        when(deepLService.toDeepLLanguageCode(eq("fr"))).thenReturn("FR");
        when(cacheService.generateCacheFilename(eq(ORIGINAL_FILENAME), eq("fr"))).thenReturn(CACHE_FILENAME);
        when(cacheService.saveTranslation(anyString(), anyString(), any())).thenReturn(true);

        // Build the multipart request body with source/target specified
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", MOCK_PDF_BYTES)
                .filename(ORIGINAL_FILENAME)
                .contentType(MediaType.APPLICATION_PDF);

        webTestClient.post().uri("/api/v1/pdf-translation/translate?sourceLanguage=en&targetLanguage=fr")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectBody(byte[].class).isEqualTo(MOCK_TRANSLATED_BYTES);

        verify(deepLService, times(1)).translatePdf(any(), any(), any());
        verify(cacheService, times(1)).saveTranslation(any(), any(), any());
    }

    @Test
    void translatePdf_AutoDetectLogic_SourceEnTargetNotSpecified_TranslatesToFr() {
        // Mock setup for auto-detection logic (Source: 'en', Target: 'fr')
        when(cacheService.getCachedTranslation(anyString(), eq("fr"))).thenReturn(null);
        when(deepLService.translatePdf(eq(MOCK_PDF_BYTES), eq("EN"), eq("FR"))).thenReturn(Mono.just(MOCK_TRANSLATED_BYTES));
        when(deepLService.toDeepLLanguageCode(eq("en"))).thenReturn("EN");
        when(deepLService.toDeepLLanguageCode(eq("fr"))).thenReturn("FR");
        when(cacheService.generateCacheFilename(anyString(), anyString())).thenReturn(CACHE_FILENAME);
        when(cacheService.saveTranslation(anyString(), anyString(), any())).thenReturn(true);

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", MOCK_PDF_BYTES)
                .filename(ORIGINAL_FILENAME)
                .contentType(MediaType.APPLICATION_PDF);

        webTestClient.post().uri("/api/v1/pdf-translation/translate?sourceLanguage=en")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk();

        verify(deepLService, times(1)).translatePdf(eq(MOCK_PDF_BYTES), eq("EN"), eq("FR"));
    }

    @Test
    void translatePdf_AutoDetectLogic_SourceFrTargetNotSpecified_TranslatesToEn() {
        // Mock setup for auto-detection logic (Source: 'fr', Target: 'en')
        when(cacheService.getCachedTranslation(anyString(), eq("en"))).thenReturn(null);
        when(deepLService.translatePdf(eq(MOCK_PDF_BYTES), eq("FR"), eq("EN"))).thenReturn(Mono.just(MOCK_TRANSLATED_BYTES));
        when(deepLService.toDeepLLanguageCode(eq("fr"))).thenReturn("FR");
        when(deepLService.toDeepLLanguageCode(eq("en"))).thenReturn("EN");
        when(cacheService.generateCacheFilename(anyString(), anyString())).thenReturn(CACHE_FILENAME);
        when(cacheService.saveTranslation(anyString(), anyString(), any())).thenReturn(true);

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", MOCK_PDF_BYTES)
                .filename(ORIGINAL_FILENAME)
                .contentType(MediaType.APPLICATION_PDF);

        webTestClient.post().uri("/api/v1/pdf-translation/translate?sourceLanguage=fr")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk();

        verify(deepLService, times(1)).translatePdf(eq(MOCK_PDF_BYTES), eq("FR"), eq("EN"));
    }

    @Test
    void translatePdf_AutoDetectLogic_SourceNotSpecified_TargetDefaultsToFr() {
        // Mock setup for auto-detection logic (Source: null, Target: 'fr')
        when(cacheService.getCachedTranslation(anyString(), eq("fr"))).thenReturn(null);
        when(deepLService.translatePdf(eq(MOCK_PDF_BYTES), eq(null), eq("FR"))).thenReturn(Mono.just(MOCK_TRANSLATED_BYTES));
        when(deepLService.toDeepLLanguageCode(eq("fr"))).thenReturn("FR");
        when(cacheService.generateCacheFilename(anyString(), anyString())).thenReturn(CACHE_FILENAME);
        when(cacheService.saveTranslation(anyString(), anyString(), any())).thenReturn(true);

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", MOCK_PDF_BYTES)
                .filename(ORIGINAL_FILENAME)
                .contentType(MediaType.APPLICATION_PDF);

        webTestClient.post().uri("/api/v1/pdf-translation/translate")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk();

        verify(deepLService, times(1)).translatePdf(eq(MOCK_PDF_BYTES), eq(null), eq("FR"));
    }

    @Test
    void translatePdf_DeepLError_ReturnsInternalServerError() {
        // Mock cache miss
        when(cacheService.getCachedTranslation(anyString(), anyString())).thenReturn(null);
        // Mock DeepL service failure
        when(deepLService.translatePdf(any(), any(), any())).thenReturn(Mono.error(new RuntimeException("DeepL Down")));
        when(deepLService.toDeepLLanguageCode(anyString())).thenReturn("FR");

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", MOCK_PDF_BYTES)
                .filename(ORIGINAL_FILENAME)
                .contentType(MediaType.APPLICATION_PDF);

        webTestClient.post().uri("/api/v1/pdf-translation/translate")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().is5xxServerError();


        verify(deepLService, times(1)).translatePdf(any(), any(), any());
        verify(cacheService, never()).saveTranslation(any(), any(), any());
    }

    @Test
    void translatePdf_InvalidFileType_ReturnsBadRequest() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        // File with text content type and wrong extension
        builder.part("file", "TEXT_CONTENT".getBytes())
                .filename("document.txt")
                .contentType(MediaType.TEXT_PLAIN);

        webTestClient.post().uri("/api/v1/pdf-translation/translate")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isBadRequest();


        verify(cacheService, never()).getCachedTranslation(any(), any());
    }

    @Test
    void healthCheck_Configured_ReturnsReadyMessage() {
        when(deepLService.isApiKeyConfigured()).thenReturn(true);
        when(deepLService.getApiUrl()).thenReturn("https://test.api.com");

        webTestClient.get().uri("/api/v1/pdf-translation/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(response -> {
                    assert(response.contains("API Key: Configured ✓"));
                    assert(response.contains("API URL: https://test.api.com"));
                });
    }

    @Test
    void healthCheck_NotConfigured_ReturnsWarningMessage() {
        when(deepLService.isApiKeyConfigured()).thenReturn(false);

        webTestClient.get().uri("/api/v1/pdf-translation/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(response -> {
                    assert(response.contains("API Key: Not configured ✗"));
                    assert(response.contains("Please set deepl.api.key"));
                });
    }
}
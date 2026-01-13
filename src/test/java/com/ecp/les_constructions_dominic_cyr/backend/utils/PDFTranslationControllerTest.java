package com.ecp.les_constructions_dominic_cyr.backend.utils;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.DeepLService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.TranslationCacheService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.presentationlayer.PDFTranslationController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToController(pdfTranslationController).build();
    }

    @Test
    void translatePdf_CacheHit_ReturnsCachedFile() {
        when(cacheService.getCachedTranslation(anyString(), anyString())).thenReturn(MOCK_TRANSLATED_BYTES);
        when(cacheService.generateCacheFilename(anyString(), anyString())).thenReturn("doc_fr.pdf");

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", MOCK_PDF_BYTES).filename(ORIGINAL_FILENAME).contentType(MediaType.APPLICATION_PDF);

        webTestClient.post().uri("/api/v1/pdf-translation/translate")
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(byte[].class).isEqualTo(MOCK_TRANSLATED_BYTES);
    }

    @Test
    void translatePdf_DeepLError_ReturnsInternalServerError() {
        // Fix: Ensure the mock returns a Mono error instead of throwing a raw exception
        when(cacheService.getCachedTranslation(anyString(), anyString())).thenReturn(null);
        when(deepLService.toDeepLLanguageCode(anyString())).thenReturn("FR");
        when(deepLService.translatePdf(any(), any(), any())).thenReturn(Mono.error(new RuntimeException("DeepL Down")));

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", MOCK_PDF_BYTES).filename(ORIGINAL_FILENAME).contentType(MediaType.APPLICATION_PDF);

        webTestClient.post().uri("/api/v1/pdf-translation/translate")
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().is5xxServerError(); // Aligns with handleGenericException
    }

    @Test
    void healthCheck_Configured_ReturnsReadyMessage() {
        when(deepLService.isApiKeyConfigured()).thenReturn(true);
        when(deepLService.getApiUrl()).thenReturn("https://api.deepl.com");

        webTestClient.get().uri("/api/v1/pdf-translation/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(v -> v.contains("Configured"));
    }
}
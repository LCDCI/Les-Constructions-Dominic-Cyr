package com.ecp.les_constructions_dominic_cyr.backend.utils.Translation.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.DeepLService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.FileServiceUploader;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.TranslationCacheService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.presentationlayer.PDFTranslationController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PDFTranslationControllerUnitTest {

    @Mock
    private DeepLService deepLService;

    @Mock
    private TranslationCacheService cacheService;

    @Mock
    private FileServiceUploader fileServiceUploader;

    @Mock
    private FilePart filePart;

    @InjectMocks
    private PDFTranslationController controller;

    private byte[] testPdfData;

    @BeforeEach
    void setUp() {
        testPdfData = "PDF content".getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void translatePdf_WithCachedTranslation_ReturnsCached() {
        byte[] cachedPdf = "cached pdf".getBytes();
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap(testPdfData);
        
        when(filePart.filename()).thenReturn("test.pdf");
        when(filePart.headers()).thenReturn(new HttpHeaders());
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));
        when(cacheService.getCachedTranslation("test.pdf", "fr")).thenReturn(cachedPdf);
        when(cacheService.generateCacheFilename("test.pdf", "fr")).thenReturn("test_fr.pdf");
        
        StepVerifier.create(controller.translatePdf(Mono.just(filePart), null, null))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
                })
                .verifyComplete();
        
        verify(deepLService, never()).translatePdf(any(), any(), any());
        verify(cacheService, never()).saveTranslation(any(), any(), any());
    }

    @Test
    void translatePdf_WithInvalidFileType_ReturnsBadRequest() {
        when(filePart.filename()).thenReturn("test.txt");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        when(filePart.headers()).thenReturn(headers);
        
        StepVerifier.create(controller.translatePdf(Mono.just(filePart), null, null))
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                })
                .verifyComplete();
        
        verify(deepLService, never()).translatePdf(any(), any(), any());
    }

    @Test
    void healthCheck_WithConfiguredApiKey_ReturnsReady() {
        when(deepLService.isApiKeyConfigured()).thenReturn(true);
        when(deepLService.getApiUrl()).thenReturn("https://api-free.deepl.com/v2");
        
        StepVerifier.create(controller.healthCheck())
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertTrue(response.getBody().contains("Configured"));
                    assertTrue(response.getBody().contains("Ready to translate PDFs"));
                })
                .verifyComplete();
    }

    @Test
    void healthCheck_WithUnconfiguredApiKey_ReturnsNotConfigured() {
        when(deepLService.isApiKeyConfigured()).thenReturn(false);
        when(deepLService.getApiUrl()).thenReturn("https://api-free.deepl.com/v2");
        
        StepVerifier.create(controller.healthCheck())
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertTrue(response.getBody().contains("Not configured"));
                    assertTrue(response.getBody().contains("deepl.api.key"));
                })
                .verifyComplete();
    }

    @Test
    void translatePdf_WithNonCachedTranslation_TranslatesAndUploads() {
        byte[] translatedPdf = "translated pdf".getBytes();
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap(testPdfData);
        
        when(filePart.filename()).thenReturn("test.pdf");
        when(filePart.headers()).thenReturn(new HttpHeaders());
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));
        when(cacheService.getCachedTranslation("test.pdf", "fr")).thenReturn(null);
        when(deepLService.toDeepLLanguageCode("fr")).thenReturn("FR");
        when(deepLService.translatePdf(any(), any(), eq("FR"))).thenReturn(Mono.just(translatedPdf));
        when(cacheService.generateCacheFilename("test.pdf", "fr")).thenReturn("test_fr.pdf");
        when(fileServiceUploader.uploadTranslatedPdf(any(), any(), any())).thenReturn(Mono.just("file-id-123"));
        
        StepVerifier.create(controller.translatePdf(Mono.just(filePart), null, null))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
                })
                .verifyComplete();
        
        verify(deepLService, times(1)).translatePdf(any(), any(), eq("FR"));
        verify(fileServiceUploader, times(1)).uploadTranslatedPdf(any(), any(), any());
    }

    @Test
    void translatePdf_WithSourceLanguageSpecified_TranslatesToOpposite() {
        byte[] translatedPdf = "translated pdf".getBytes();
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap(testPdfData);
        
        when(filePart.filename()).thenReturn("test.pdf");
        when(filePart.headers()).thenReturn(new HttpHeaders());
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));
        when(cacheService.getCachedTranslation("test.pdf", "fr")).thenReturn(null);
        when(deepLService.toDeepLLanguageCode("en")).thenReturn("EN");
        when(deepLService.toDeepLLanguageCode("fr")).thenReturn("FR");
        when(deepLService.translatePdf(any(), eq("EN"), eq("FR"))).thenReturn(Mono.just(translatedPdf));
        when(cacheService.generateCacheFilename("test.pdf", "fr")).thenReturn("test_fr.pdf");
        when(fileServiceUploader.uploadTranslatedPdf(any(), any(), any())).thenReturn(Mono.just("file-id-123"));
        
        StepVerifier.create(controller.translatePdf(Mono.just(filePart), "en", null))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                })
                .verifyComplete();
        
        verify(deepLService, times(1)).translatePdf(any(), eq("EN"), eq("FR"));
    }

    @Test
    void translatePdf_WithBothLanguagesSpecified_UsesProvidedLanguages() {
        byte[] translatedPdf = "translated pdf".getBytes();
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap(testPdfData);
        
        when(filePart.filename()).thenReturn("test.pdf");
        when(filePart.headers()).thenReturn(new HttpHeaders());
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));
        when(cacheService.getCachedTranslation("test.pdf", "en")).thenReturn(null);
        when(deepLService.toDeepLLanguageCode("fr")).thenReturn("FR");
        when(deepLService.toDeepLLanguageCode("en")).thenReturn("EN");
        when(deepLService.translatePdf(any(), eq("FR"), eq("EN"))).thenReturn(Mono.just(translatedPdf));
        when(cacheService.generateCacheFilename("test.pdf", "en")).thenReturn("test_en.pdf");
        when(fileServiceUploader.uploadTranslatedPdf(any(), any(), any())).thenReturn(Mono.just("file-id-123"));
        
        StepVerifier.create(controller.translatePdf(Mono.just(filePart), "fr", "en"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                })
                .verifyComplete();
        
        verify(deepLService, times(1)).translatePdf(any(), eq("FR"), eq("EN"));
    }

    @Test
    void translatePdf_WithTranslationError_ReturnsInternalServerError() {
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap(testPdfData);
        
        when(filePart.filename()).thenReturn("test.pdf");
        when(filePart.headers()).thenReturn(new HttpHeaders());
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));
        when(cacheService.getCachedTranslation("test.pdf", "fr")).thenReturn(null);
        when(deepLService.toDeepLLanguageCode("fr")).thenReturn("FR");
        when(deepLService.translatePdf(any(), any(), eq("FR"))).thenReturn(Mono.error(new RuntimeException("Translation failed")));
        
        StepVerifier.create(controller.translatePdf(Mono.just(filePart), null, null))
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void translatePdf_WithPdfContentType_ValidatesCorrectly() {
        byte[] translatedPdf = "translated".getBytes();
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap(testPdfData);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        
        when(filePart.filename()).thenReturn("document");
        when(filePart.headers()).thenReturn(headers);
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));
        when(cacheService.getCachedTranslation("document", "fr")).thenReturn(null);
        when(deepLService.toDeepLLanguageCode("fr")).thenReturn("FR");
        when(deepLService.translatePdf(any(), any(), any())).thenReturn(Mono.just(translatedPdf));
        when(cacheService.generateCacheFilename("document", "fr")).thenReturn("document_fr.pdf");
        when(fileServiceUploader.uploadTranslatedPdf(any(), any(), any())).thenReturn(Mono.just("file-id"));
        
        StepVerifier.create(controller.translatePdf(Mono.just(filePart), null, null))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void translatePdf_WithNullFilename_UsesDefaultFilename() {
        byte[] translatedPdf = "translated pdf".getBytes();
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap(testPdfData);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        
        when(filePart.filename()).thenReturn(null);
        when(filePart.headers()).thenReturn(headers);
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));
        when(cacheService.getCachedTranslation("document.pdf", "fr")).thenReturn(null);
        when(deepLService.toDeepLLanguageCode("fr")).thenReturn("FR");
        when(deepLService.translatePdf(any(), any(), any())).thenReturn(Mono.just(translatedPdf));
        when(cacheService.generateCacheFilename("document.pdf", "fr")).thenReturn("document_fr.pdf");
        when(fileServiceUploader.uploadTranslatedPdf(any(), any(), any())).thenReturn(Mono.just("file-id-123"));
        
        StepVerifier.create(controller.translatePdf(Mono.just(filePart), null, null))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                })
                .verifyComplete();
    }
}


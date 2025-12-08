package com.ecp.les_constructions_dominic_cyr.utils.Translation.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.FileServiceUploader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceUploaderTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private FileServiceUploader fileServiceUploader;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        fileServiceUploader = new FileServiceUploader("http://localhost:8082", objectMapper);
        ReflectionTestUtils.setField(fileServiceUploader, "webClient", webClient);
    }

    @Test
    void uploadTranslatedPdf_WithValidData_ReturnsFileId() {
        byte[] pdfData = "test pdf content".getBytes();
        String responseJson = "{\"fileId\":\"file-123\"}";

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/files")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseJson));

        StepVerifier.create(fileServiceUploader.uploadTranslatedPdf(pdfData, "test.pdf", "system"))
                .assertNext(fileId -> {
                    assertEquals("file-123", fileId);
                })
                .verifyComplete();

        verify(webClient, times(1)).post();
    }

    @Test
    void uploadTranslatedPdf_WithNullUploadedBy_UsesSystem() {
        byte[] pdfData = "test pdf content".getBytes();
        String responseJson = "{\"fileId\":\"file-456\"}";

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/files")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseJson));

        StepVerifier.create(fileServiceUploader.uploadTranslatedPdf(pdfData, "test.pdf", null))
                .assertNext(fileId -> {
                    assertEquals("file-456", fileId);
                })
                .verifyComplete();
    }

    @Test
    void uploadTranslatedPdf_WithInvalidJson_ThrowsException() {
        byte[] pdfData = "test pdf content".getBytes();
        String invalidJson = "invalid json";

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/files")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(invalidJson));

        StepVerifier.create(fileServiceUploader.uploadTranslatedPdf(pdfData, "test.pdf", "user-1"))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void uploadTranslatedPdf_WithNetworkError_ReturnsError() {
        byte[] pdfData = "test pdf content".getBytes();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/files")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("Network error")));

        StepVerifier.create(fileServiceUploader.uploadTranslatedPdf(pdfData, "test.pdf", "user-1"))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void uploadTranslatedPdf_WithMissingFileId_ThrowsException() {
        byte[] pdfData = "test pdf content".getBytes();
        String responseJson = "{\"status\":\"success\"}"; // Missing fileId

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/files")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseJson));

        StepVerifier.create(fileServiceUploader.uploadTranslatedPdf(pdfData, "test.pdf", "user-1"))
                .expectError(RuntimeException.class)
                .verify();
    }
}


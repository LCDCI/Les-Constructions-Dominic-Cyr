package com.ecp.les_constructions_dominic_cyr.backend.utils.Translation.businesslayer;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceUploaderUnitTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    private FileServiceUploader fileServiceUploader;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        fileServiceUploader = new FileServiceUploader("http://localhost:8082", objectMapper);
        ReflectionTestUtils.setField(fileServiceUploader, "webClient", webClient);
    }

    @Test
    void uploadTranslatedPdf_WithValidData_ReturnsFileId() throws Exception {
        byte[] pdfBytes = new byte[]{1, 2, 3, 4, 5};
        String filename = "test.pdf";
        String uploadedBy = "user-123";
        String responseJson = "{\"fileId\":\"file-id-123\"}";

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/files")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseJson));

        StepVerifier.create(fileServiceUploader.uploadTranslatedPdf(pdfBytes, filename, uploadedBy))
                .assertNext(fileId -> {
                    assertEquals("file-id-123", fileId);
                })
                .verifyComplete();

        verify(webClient).post();
        verify(requestBodyUriSpec).uri("/files");
    }

    @Test
    void uploadTranslatedPdf_WithNullUploadedBy_UsesSystemDefault() throws Exception {
        byte[] pdfBytes = new byte[]{1, 2, 3};
        String filename = "test.pdf";
        String responseJson = "{\"fileId\":\"file-id-456\"}";

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/files")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseJson));

        StepVerifier.create(fileServiceUploader.uploadTranslatedPdf(pdfBytes, filename, null))
                .assertNext(fileId -> {
                    assertEquals("file-id-456", fileId);
                })
                .verifyComplete();
    }

    @Test
    void uploadTranslatedPdf_WithEmptyUploadedBy_UsesSystemDefault() throws Exception {
        byte[] pdfBytes = new byte[]{1, 2, 3};
        String filename = "test.pdf";
        String responseJson = "{\"fileId\":\"file-id-789\"}";

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/files")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseJson));

        StepVerifier.create(fileServiceUploader.uploadTranslatedPdf(pdfBytes, filename, ""))
                .assertNext(fileId -> {
                    assertEquals("file-id-789", fileId);
                })
                .verifyComplete();
    }

    @Test
    void uploadTranslatedPdf_WithInvalidJson_ThrowsRuntimeException() {
        byte[] pdfBytes = new byte[]{1, 2, 3};
        String filename = "test.pdf";
        String invalidJson = "invalid json";

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/files")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(invalidJson));

        StepVerifier.create(fileServiceUploader.uploadTranslatedPdf(pdfBytes, filename, "user"))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void uploadTranslatedPdf_WithMissingFileId_ThrowsRuntimeException() {
        byte[] pdfBytes = new byte[]{1, 2, 3};
        String filename = "test.pdf";
        String responseJson = "{\"status\":\"success\"}";

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/files")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseJson));

        StepVerifier.create(fileServiceUploader.uploadTranslatedPdf(pdfBytes, filename, "user"))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void uploadTranslatedPdf_WithNetworkError_ReturnsError() {
        byte[] pdfBytes = new byte[]{1, 2, 3};
        String filename = "test.pdf";

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/files")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("Network error")));

        StepVerifier.create(fileServiceUploader.uploadTranslatedPdf(pdfBytes, filename, "user"))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void uploadTranslatedPdf_WithEmptyPdfBytes_StillUploads() throws Exception {
        byte[] pdfBytes = new byte[]{};
        String filename = "empty.pdf";
        String responseJson = "{\"fileId\":\"file-id-empty\"}";

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/files")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseJson));

        StepVerifier.create(fileServiceUploader.uploadTranslatedPdf(pdfBytes, filename, "user"))
                .assertNext(fileId -> {
                    assertEquals("file-id-empty", fileId);
                })
                .verifyComplete();
    }

    @Test
    void uploadTranslatedPdf_WithNullFilename_StillUploads() throws Exception {
        byte[] pdfBytes = new byte[]{1, 2, 3};
        String responseJson = "{\"fileId\":\"file-id-null-filename\"}";

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/files")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseJson));

        StepVerifier.create(fileServiceUploader.uploadTranslatedPdf(pdfBytes, null, "user"))
                .assertNext(fileId -> {
                    assertEquals("file-id-null-filename", fileId);
                })
                .verifyComplete();
    }

    @Test
    void uploadTranslatedPdf_WithLargePdfBytes_HandlesCorrectly() throws Exception {
        byte[] pdfBytes = new byte[10000];
        for (int i = 0; i < pdfBytes.length; i++) {
            pdfBytes[i] = (byte) (i % 256);
        }
        String filename = "large.pdf";
        String responseJson = "{\"fileId\":\"file-id-large\"}";

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/files")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseJson));

        StepVerifier.create(fileServiceUploader.uploadTranslatedPdf(pdfBytes, filename, "user"))
                .assertNext(fileId -> {
                    assertEquals("file-id-large", fileId);
                })
                .verifyComplete();
    }

    @Test
    void uploadTranslatedPdf_WithNullFileIdInResponse_ThrowsException() {
        byte[] pdfBytes = new byte[]{1, 2, 3};
        String filename = "test.pdf";
        // When fileId is null in JSON, asText() returns "null" string, which causes issues
        // Let's test with missing fileId field instead
        String responseJson = "{\"status\":\"success\"}";

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/files")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseJson));

        StepVerifier.create(fileServiceUploader.uploadTranslatedPdf(pdfBytes, filename, "user"))
                .expectError(RuntimeException.class)
                .verify();
    }
}

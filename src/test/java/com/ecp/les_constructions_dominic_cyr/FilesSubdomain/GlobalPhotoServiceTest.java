package com.ecp.les_constructions_dominic_cyr.FilesSubdomain;

import com.ecp. les_constructions_dominic_cyr.backend.FilesSubdomain.GlobalPhotoService;
import org.junit.jupiter. api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GlobalPhotoServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private GlobalPhotoService globalPhotoService;

    @BeforeEach
    void setUp() {
        globalPhotoService = new GlobalPhotoService("http://localhost:8082");
        ReflectionTestUtils.setField(globalPhotoService, "webClient", webClient);
    }

    private void setupWebClientMock(String response) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(eq("/files"))).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(eq(MediaType.MULTIPART_FORM_DATA))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(BodyInserter.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(response));
    }

    private void setupWebClientMockError(Throwable error) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(eq("/files"))).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(eq(MediaType.MULTIPART_FORM_DATA))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(BodyInserter.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(error));
    }

    @Test
    void uploadGlobalPhoto_WithValidJpegFile_ReturnsSuccessResponse() {
        String expectedResponse = "{\"id\":\"123\",\"status\":\"uploaded\"}";
        setupWebClientMock(expectedResponse);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test. jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        Mono<String> result = globalPhotoService.uploadGlobalPhoto(file, "test-user");

        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .verifyComplete();

        verify(webClient, times(1)).post();
        verify(requestBodyUriSpec, times(1)).uri("/files");
        verify(requestBodySpec, times(1)). contentType(MediaType.MULTIPART_FORM_DATA);
    }

    @Test
    void uploadGlobalPhoto_WithPngFile_ReturnsSuccessResponse() {
        String expectedResponse = "{\"id\":\"456\",\"fileName\":\"image.png\"}";
        setupWebClientMock(expectedResponse);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                MediaType. IMAGE_PNG_VALUE,
                "png image content".getBytes()
        );

        Mono<String> result = globalPhotoService.uploadGlobalPhoto(file, "user-123");

        StepVerifier.create(result)
                .expectNext(expectedResponse)
                . verifyComplete();
    }

    @Test
    void uploadGlobalPhoto_WithNullContentType_DefaultsToJpeg() {
        String expectedResponse = "{\"id\":\"789\",\"status\":\"uploaded\"}";
        setupWebClientMock(expectedResponse);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                null,
                "image bytes".getBytes()
        );

        Mono<String> result = globalPhotoService.uploadGlobalPhoto(file, "user-456");

        StepVerifier. create(result)
                .expectNext(expectedResponse)
                .verifyComplete();

        verify(webClient, times(1)).post();
    }

    @Test
    void uploadGlobalPhoto_WithGifFile_ReturnsSuccessResponse() {
        String expectedResponse = "{\"id\":\"gif-123\",\"fileName\":\"animation.gif\"}";
        setupWebClientMock(expectedResponse);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "animation.gif",
                MediaType.IMAGE_GIF_VALUE,
                "gif content".getBytes()
        );

        Mono<String> result = globalPhotoService.uploadGlobalPhoto(file, "gif-user");

        StepVerifier. create(result)
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void uploadGlobalPhoto_WithLargeFile_ReturnsSuccessResponse() {
        String expectedResponse = "{\"id\":\"large-file-123\",\"status\":\"uploaded\"}";
        setupWebClientMock(expectedResponse);

        byte[] largeContent = new byte[1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                largeContent
        );

        Mono<String> result = globalPhotoService.uploadGlobalPhoto(file, "large-file-user");

        StepVerifier. create(result)
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void uploadGlobalPhoto_WithSpecialCharactersInFilename_HandlesCorrectly() {
        String expectedResponse = "{\"id\":\"special-123\",\"status\":\"uploaded\"}";
        setupWebClientMock(expectedResponse);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo with spaces & special. jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        Mono<String> result = globalPhotoService.uploadGlobalPhoto(file, "special-user");

        StepVerifier. create(result)
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void uploadGlobalPhoto_WithDifferentUserIds_SendsCorrectUserId() {
        String expectedResponse = "{\"uploadedBy\":\"unique-user-id\"}";
        setupWebClientMock(expectedResponse);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType. IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        String userId = "unique-user-id-12345";
        Mono<String> result = globalPhotoService.uploadGlobalPhoto(file, userId);

        StepVerifier. create(result)
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void uploadGlobalPhoto_WhenServerError_PropagatesError() {
        setupWebClientMockError(new RuntimeException("Server error"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        Mono<String> result = globalPhotoService.uploadGlobalPhoto(file, "user");

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void uploadGlobalPhoto_WithEmptyUserId_SendsEmptyUserId() {
        String expectedResponse = "{\"status\":\"ok\"}";
        setupWebClientMock(expectedResponse);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test. jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        Mono<String> result = globalPhotoService.uploadGlobalPhoto(file, "");

        StepVerifier.create(result)
                . expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void uploadGlobalPhoto_VerifyWebClientPostCalled() {
        setupWebClientMock("{}");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        globalPhotoService.uploadGlobalPhoto(file, "user"). block();

        verify(webClient, times(1)).post();
    }

    @Test
    void uploadGlobalPhoto_VerifyUriIsFiles() {
        setupWebClientMock("{}");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType. IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        globalPhotoService. uploadGlobalPhoto(file, "user"). block();

        verify(requestBodyUriSpec, times(1)).uri("/files");
    }

    @Test
    void uploadGlobalPhoto_VerifyContentTypeIsMultipartFormData() {
        setupWebClientMock("{}");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        globalPhotoService.uploadGlobalPhoto(file, "user").block();

        verify(requestBodySpec, times(1)). contentType(MediaType.MULTIPART_FORM_DATA);
    }

    @Test
    void uploadGlobalPhoto_VerifyBodyInserterUsed() {
        setupWebClientMock("{}");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        globalPhotoService.uploadGlobalPhoto(file, "user").block();

        verify(requestBodySpec, times(1)). body(any(BodyInserter. class));
    }

    @Test
    void uploadGlobalPhoto_VerifyRetrieveCalled() {
        setupWebClientMock("{}");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        globalPhotoService.uploadGlobalPhoto(file, "user").block();

        verify(requestHeadersSpec, times(1)).retrieve();
    }

    @Test
    void uploadGlobalPhoto_VerifyBodyToMonoCalled() {
        setupWebClientMock("{}");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        globalPhotoService.uploadGlobalPhoto(file, "user").block();

        verify(responseSpec, times(1)).bodyToMono(String.class);
    }

    @Test
    void uploadGlobalPhoto_WithWebpFile_ReturnsSuccessResponse() {
        String expectedResponse = "{\"id\":\"webp-123\"}";
        setupWebClientMock(expectedResponse);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.webp",
                "image/webp",
                "webp content".getBytes()
        );

        Mono<String> result = globalPhotoService.uploadGlobalPhoto(file, "webp-user");

        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void uploadGlobalPhoto_ReturnsJsonResponse() {
        String jsonResponse = "{\"id\":\"abc-123\",\"fileName\":\"test.jpg\",\"size\":1024}";
        setupWebClientMock(jsonResponse);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test. jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        Mono<String> result = globalPhotoService.uploadGlobalPhoto(file, "user");

        StepVerifier.create(result)
                .expectNext(jsonResponse)
                .verifyComplete();
    }

    @Test
    void uploadGlobalPhoto_WithEmptyFile_StillCallsWebClient() {
        setupWebClientMock("{\"status\":\"ok\"}");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        Mono<String> result = globalPhotoService.uploadGlobalPhoto(file, "user");

        StepVerifier.create(result)
                . expectNextCount(1)
                .verifyComplete();

        verify(webClient, times(1)).post();
    }

    @Test
    void constructor_CreatesServiceWithBaseUrl() {
        GlobalPhotoService service = new GlobalPhotoService("http://test-url:8080");
        assertNotNull(service);
    }

    @Test
    void constructor_WithDifferentBaseUrls_CreatesService() {
        GlobalPhotoService service1 = new GlobalPhotoService("http://localhost:8082");
        GlobalPhotoService service2 = new GlobalPhotoService("http://files-service:8080");

        assertNotNull(service1);
        assertNotNull(service2);
    }

    @Test
    void uploadGlobalPhoto_MultipleCalls_EachCallsWebClient() {
        setupWebClientMock("{\"call\":\"1\"}");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        globalPhotoService.uploadGlobalPhoto(file, "user1").block();
        globalPhotoService. uploadGlobalPhoto(file, "user2").block();

        verify(webClient, times(2)).post();
    }
}
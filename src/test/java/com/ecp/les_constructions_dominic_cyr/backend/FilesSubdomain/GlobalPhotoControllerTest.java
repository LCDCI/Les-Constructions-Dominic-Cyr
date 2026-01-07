package com.ecp.les_constructions_dominic_cyr.backend.FilesSubdomain;

import org.junit.jupiter. api.BeforeEach;
import org.junit.jupiter. api.Test;
import org.junit.jupiter.api. extension.ExtendWith;
import org. mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito. junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org. springframework.mock.web.MockMultipartFile;
import org.springframework. web.multipart.MultipartFile;
import reactor.core. publisher.Mono;

import static org.junit.jupiter.api. Assertions.*;
import static org.mockito.ArgumentMatchers. any;
import static org.mockito. ArgumentMatchers.eq;
import static org. mockito.Mockito.*;

@ExtendWith(MockitoExtension. class)
public class GlobalPhotoControllerTest {

    @Mock
    private GlobalPhotoService globalPhotoService;

    @InjectMocks
    private GlobalPhotoController globalPhotoController;

    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() {
        testFile = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test content".getBytes()
        );
    }

    @Test
    void uploadGlobal_WithValidFile_ReturnsSuccessResponse() {
        String expectedResponse = "{\"id\":\"123\",\"status\":\"uploaded\"}";
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile.class), eq("test-user")))
                . thenReturn(Mono.just(expectedResponse));

        Mono<String> result = globalPhotoController.uploadGlobal(testFile, "test-user");

        String response = result.block();
        assertEquals(expectedResponse, response);
        verify(globalPhotoService, times(1)).uploadGlobalPhoto(any(MultipartFile.class), eq("test-user"));
    }

    @Test
    void uploadGlobal_WithDefaultUser_UsesDefaultValue() {
        String expectedResponse = "{\"id\":\"456\",\"status\":\"uploaded\"}";
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile. class), eq("demo-user")))
                .thenReturn(Mono.just(expectedResponse));

        Mono<String> result = globalPhotoController.uploadGlobal(testFile, "demo-user");

        assertEquals(expectedResponse, result.block());
        verify(globalPhotoService, times(1)).uploadGlobalPhoto(any(MultipartFile.class), eq("demo-user"));
    }

    @Test
    void uploadGlobal_WithCustomUser_PassesCorrectUser() {
        String customUser = "custom-user-123";
        String expectedResponse = "{\"id\":\"789\",\"status\":\"uploaded\"}";
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile. class), eq(customUser)))
                .thenReturn(Mono.just(expectedResponse));

        Mono<String> result = globalPhotoController.uploadGlobal(testFile, customUser);

        assertEquals(expectedResponse, result.block());
        verify(globalPhotoService, times(1)).uploadGlobalPhoto(any(MultipartFile.class), eq(customUser));
    }

    @Test
    void uploadGlobal_ReturnsMonoFromService() {
        String expectedResponse = "{\"message\":\"success\"}";
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile.class), any(String.class)))
                .thenReturn(Mono. just(expectedResponse));

        Mono<String> result = globalPhotoController.uploadGlobal(testFile, "user");

        assertNotNull(result);
        assertEquals(expectedResponse, result.block());
    }

    @Test
    void uploadGlobal_CallsServiceWithCorrectFile() {
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile.class), any(String.class)))
                .thenReturn(Mono. just("{}"));

        MockMultipartFile specificFile = new MockMultipartFile(
                "file",
                "specific-file.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "specific content".getBytes()
        );

        globalPhotoController.uploadGlobal(specificFile, "user"). block();

        verify(globalPhotoService, times(1)).uploadGlobalPhoto(argThat(multipartFile ->
                "specific-file.jpg".equals(multipartFile.getOriginalFilename())
        ), eq("user"));
    }

    @Test
    void uploadGlobal_WithEmptyUserId_PassesEmptyString() {
        String expectedResponse = "{\"status\":\"ok\"}";
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile.class), eq("")))
                .thenReturn(Mono.just(expectedResponse));

        Mono<String> result = globalPhotoController. uploadGlobal(testFile, "");

        assertEquals(expectedResponse, result.block());
        verify(globalPhotoService, times(1)).uploadGlobalPhoto(any(MultipartFile. class), eq(""));
    }

    @Test
    void uploadGlobal_WhenServiceReturnsError_PropagatesError() {
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile.class), any(String.class)))
                .thenReturn(Mono. error(new RuntimeException("Upload failed")));

        Mono<String> result = globalPhotoController.uploadGlobal(testFile, "user");

        assertThrows(RuntimeException. class, result::block);
    }

    @Test
    void uploadGlobal_WithPngFile_CallsService() {
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile.class), any(String.class)))
                .thenReturn(Mono. just("{\"type\":\"png\"}"));

        MockMultipartFile pngFile = new MockMultipartFile(
                "file",
                "image.png",
                MediaType.IMAGE_PNG_VALUE,
                "png bytes".getBytes()
        );

        String result = globalPhotoController.uploadGlobal(pngFile, "png-user"). block();

        assertNotNull(result);
        assertTrue(result.contains("png"));
    }

    @Test
    void uploadGlobal_WithGifFile_CallsService() {
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile.class), any(String.class)))
                .thenReturn(Mono.just("{\"type\":\"gif\"}"));

        MockMultipartFile gifFile = new MockMultipartFile(
                "file",
                "animation.gif",
                MediaType.IMAGE_GIF_VALUE,
                "gif bytes".getBytes()
        );

        String result = globalPhotoController.uploadGlobal(gifFile, "gif-user"). block();

        assertNotNull(result);
    }

    @Test
    void uploadGlobal_ServiceCalledExactlyOnce() {
        when(globalPhotoService. uploadGlobalPhoto(any(MultipartFile.class), any(String.class)))
                .thenReturn(Mono.just("{}"));

        globalPhotoController.uploadGlobal(testFile, "user"). block();

        verify(globalPhotoService, times(1)). uploadGlobalPhoto(any(MultipartFile. class), any(String.class));
    }

    @Test
    void uploadGlobal_WithNullUserId_PassesNull() {
        String expectedResponse = "{\"status\":\"ok\"}";
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile.class), eq(null)))
                .thenReturn(Mono.just(expectedResponse));

        Mono<String> result = globalPhotoController.uploadGlobal(testFile, null);

        assertEquals(expectedResponse, result.block());
    }

    @Test
    void uploadGlobal_VerifyFilePassedToService() {
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile.class), any(String. class)))
                . thenReturn(Mono.just("{}"));

        globalPhotoController.uploadGlobal(testFile, "user").block();

        verify(globalPhotoService). uploadGlobalPhoto(argThat(file ->
                file.getOriginalFilename().equals("test.jpg") &&
                        file.getContentType().equals(MediaType.IMAGE_JPEG_VALUE)
        ), any(String.class));
    }

    @Test
    void uploadGlobal_WithLargeFile_CallsService() {
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile.class), any(String.class)))
                .thenReturn(Mono.just("{\"size\":\"large\"}"));

        byte[] largeContent = new byte[1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                largeContent
        );

        String result = globalPhotoController.uploadGlobal(largeFile, "user").block();

        assertNotNull(result);
        verify(globalPhotoService, times(1)).uploadGlobalPhoto(any(MultipartFile. class), any(String.class));
    }

    @Test
    void uploadGlobal_WithSpecialCharactersInFilename_CallsService() {
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile.class), any(String. class)))
                . thenReturn(Mono.just("{\"status\":\"ok\"}"));

        MockMultipartFile specialFile = new MockMultipartFile(
                "file",
                "photo with spaces & special. jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "content". getBytes()
        );

        String result = globalPhotoController.uploadGlobal(specialFile, "user").block();

        assertNotNull(result);
    }

    @Test
    void uploadGlobal_WhenServiceReturnsEmpty_ReturnsEmpty() {
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile.class), any(String.class)))
                .thenReturn(Mono. empty());

        Mono<String> result = globalPhotoController. uploadGlobal(testFile, "user");

        assertNull(result.block());
    }
}
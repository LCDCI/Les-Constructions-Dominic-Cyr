package com.ecp.les_constructions_dominic_cyr.FilesSubdomain;

import com.ecp.les_constructions_dominic_cyr.backend.FilesSubdomain.GlobalPhotoController;
import com.ecp.les_constructions_dominic_cyr.backend.FilesSubdomain.GlobalPhotoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web. servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GlobalPhotoController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(com.ecp.les_constructions_dominic_cyr.config.TestcontainersPostgresConfig.class)
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
public class GlobalPhotoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GlobalPhotoService globalPhotoService;


    @Test
    void uploadGlobal_WithPngFile_ReturnsOk() throws Exception {
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile.class), any(String.class)))
                .thenReturn(Mono. just("{\"type\":\"png\"}"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                MediaType.IMAGE_PNG_VALUE,
                "png content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/photos/upload")
                        .file(file)
                        .header("X-User", "png-user"))
                .andExpect(status().isOk());
    }

    @Test
    void uploadGlobal_WithGifFile_ReturnsOk() throws Exception {
        when(globalPhotoService. uploadGlobalPhoto(any(MultipartFile.class), any(String.class)))
                .thenReturn(Mono.just("{\"type\":\"gif\"}"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "animation.gif",
                MediaType.IMAGE_GIF_VALUE,
                "gif content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/photos/upload")
                        .file(file)
                        .header("X-User", "gif-user"))
                .andExpect(status().isOk());
    }



    @Test
    void uploadGlobal_WithLargeFile_ReturnsOk() throws Exception {
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile.class), any(String.class)))
                .thenReturn(Mono.just("{\"status\":\"uploaded\"}"));

        byte[] largeContent = new byte[1024 * 100];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                largeContent
        );

        mockMvc.perform(multipart("/api/v1/photos/upload")
                        .file(file)
                        . header("X-User", "user"))
                .andExpect(status(). isOk());
    }

    @Test
    void uploadGlobal_WithSpecialFilename_ReturnsOk() throws Exception {
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile.class), any(String.class)))
                .thenReturn(Mono.just("{\"status\":\"ok\"}"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo with spaces.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/photos/upload")
                        .file(file)
                        .header("X-User", "user"))
                .andExpect(status().isOk());
    }

    @Test
    void uploadGlobal_WithEmptyFileName_ReturnsOk() throws Exception {
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile.class), any(String.class)))
                .thenReturn(Mono. just("{\"status\":\"ok\"}"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/photos/upload")
                        .file(file)
                        .header("X-User", "user"))
                .andExpect(status().isOk());
    }

    @Test
    void uploadGlobal_WithDifferentContentTypes_HandlesCorrectly() throws Exception {
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile.class), any(String.class)))
                .thenReturn(Mono. just("{\"status\":\"ok\"}"));

        MockMultipartFile jpegFile = new MockMultipartFile(
                "file",
                "image.jpeg",
                "image/jpeg",
                "jpeg content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/photos/upload")
                        .file(jpegFile)
                        .header("X-User", "user"))
                .andExpect(status().isOk());
    }

    @Test
    void uploadGlobal_VerifyContentTypeIsMultipartFormData() throws Exception {
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile.class), any(String.class)))
                .thenReturn(Mono.just("{}"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType. IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/photos/upload")
                        .file(file)
                        . contentType(MediaType. MULTIPART_FORM_DATA)
                        .header("X-User", "user"))
                . andExpect(status().isOk());
    }
}
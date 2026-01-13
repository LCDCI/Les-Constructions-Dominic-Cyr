package com.ecp.les_constructions_dominic_cyr.backend.FilesSubdomain;

import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Auth0ManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersPostgresConfig.class)
public class GlobalPhotoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GlobalPhotoService globalPhotoService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private Auth0ManagementService auth0ManagementService;

    @Test
    void uploadGlobal_WithPngFile_ReturnsOk() throws Exception {
        when(globalPhotoService.uploadGlobalPhoto(any(MultipartFile.class), any(String.class)))
                .thenReturn(Mono.just("{\"type\":\"png\"}"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "image.png", MediaType.IMAGE_PNG_VALUE, "png content".getBytes());

        // Handling Mono in MockMvc requires performing the initial request and then an async dispatch
        MvcResult mvcResult = mockMvc.perform(multipart("/api/v1/photos/upload")
                        .file(file)
                        .header("X-User", "png-user")
                        .with(jwt()))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());
    }
}
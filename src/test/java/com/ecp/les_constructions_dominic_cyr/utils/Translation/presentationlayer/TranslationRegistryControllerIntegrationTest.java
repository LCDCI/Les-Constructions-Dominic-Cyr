package com.ecp.les_constructions_dominic_cyr.utils.Translation.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.dataaccesslayer.TranslationRegistry;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.presentationlayer.TranslationRegistryController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TranslationRegistryController.class)
class TranslationRegistryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TranslationRegistry registry;

    @BeforeEach
    void setUp() {
        reset(registry);
    }
    @Test
    void getFileId_WhenExists_ReturnsOk() throws Exception {
        when(registry.getFileId("en", "home")).thenReturn("file-id-123");
        
        mockMvc.perform(get("/api/v1/translations/registry/en/home"))
                .andExpect(status().isOk())
                .andExpect(content().string("file-id-123"));
        
        verify(registry, times(1)).getFileId("en", "home");
    }

    @Test
    void getFileId_WhenNotExists_ReturnsNotFound() throws Exception {
        when(registry.getFileId("en", "home")).thenReturn(null);
        
        mockMvc.perform(get("/api/v1/translations/registry/en/home"))
                .andExpect(status().isNotFound());
        
        verify(registry, times(1)).getFileId("en", "home");
    }

    @Test
    void getFileId_WithUpperCaseLanguage_HandlesCorrectly() throws Exception {
        when(registry.getFileId("EN", "home")).thenReturn("file-id-123");
        
        mockMvc.perform(get("/api/v1/translations/registry/EN/home"))
                .andExpect(status().isOk())
                .andExpect(content().string("file-id-123"));
    }

    @Test
    void getFileId_WithUpperCasePage_HandlesCorrectly() throws Exception {
        when(registry.getFileId("en", "HOME")).thenReturn("file-id-123");
        
        mockMvc.perform(get("/api/v1/translations/registry/en/HOME"))
                .andExpect(status().isOk())
                .andExpect(content().string("file-id-123"));
    }

    @Test
    void getFileId_WithFrenchLanguage_ReturnsFileId() throws Exception {
        when(registry.getFileId("fr", "home")).thenReturn("file-id-fr-123");
        
        mockMvc.perform(get("/api/v1/translations/registry/fr/home"))
                .andExpect(status().isOk())
                .andExpect(content().string("file-id-fr-123"));
    }
}


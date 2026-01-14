package com.ecp.les_constructions_dominic_cyr.backend.utils.Translation.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.dataaccesslayer.TranslationRegistry;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.presentationlayer.TranslationRegistryController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranslationRegistryControllerUnitTest {

    @Mock
    private TranslationRegistry registry;

    @InjectMocks
    private TranslationRegistryController controller;

    @BeforeEach
    void setUp() {
        // Reset mocks if needed
    }

    @Test
    void registerFileId_WithValidData_ReturnsOk() {
        doNothing().when(registry).registerFileId(anyString(), anyString(), anyString());
        
        ResponseEntity<String> response = controller.registerFileId("en", "home", "file-id-123");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("File ID registered successfully"));
        assertTrue(response.getBody().contains("en.home"));
        
        verify(registry, times(1)).registerFileId("en", "home", "file-id-123");
    }

    @Test
    void registerFileId_TrimsWhitespace() {
        doNothing().when(registry).registerFileId(anyString(), anyString(), anyString());
        
        ResponseEntity<String> response = controller.registerFileId("en", "home", "  file-id-123  ");
        
        verify(registry, times(1)).registerFileId("en", "home", "file-id-123");
    }

    @Test
    void registerFileId_WithUpperCaseLanguage_HandlesCorrectly() {
        doNothing().when(registry).registerFileId(anyString(), anyString(), anyString());
        
        ResponseEntity<String> response = controller.registerFileId("EN", "home", "file-id-123");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(registry, times(1)).registerFileId("EN", "home", "file-id-123");
    }

    @Test
    void registerFileId_WithUpperCasePage_HandlesCorrectly() {
        doNothing().when(registry).registerFileId(anyString(), anyString(), anyString());
        
        ResponseEntity<String> response = controller.registerFileId("en", "HOME", "file-id-123");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(registry, times(1)).registerFileId("en", "HOME", "file-id-123");
    }

    @Test
    void getFileId_WhenExists_ReturnsOk() {
        when(registry.getFileId("en", "home")).thenReturn("file-id-123");
        
        ResponseEntity<String> response = controller.getFileId("en", "home");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("file-id-123", response.getBody());
        verify(registry, times(1)).getFileId("en", "home");
    }

    @Test
    void getFileId_WhenNotExists_ReturnsNotFound() {
        when(registry.getFileId("en", "home")).thenReturn(null);
        
        ResponseEntity<String> response = controller.getFileId("en", "home");
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(registry, times(1)).getFileId("en", "home");
    }

    @Test
    void getFileId_WithUpperCaseLanguage_HandlesCorrectly() {
        when(registry.getFileId("EN", "home")).thenReturn("file-id-123");
        
        ResponseEntity<String> response = controller.getFileId("EN", "home");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("file-id-123", response.getBody());
    }

    @Test
    void getFileId_WithUpperCasePage_HandlesCorrectly() {
        when(registry.getFileId("en", "HOME")).thenReturn("file-id-123");
        
        ResponseEntity<String> response = controller.getFileId("en", "HOME");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("file-id-123", response.getBody());
    }

    @Test
    void registerFileId_WithEmptyFileId_HandlesGracefully() {
        doNothing().when(registry).registerFileId(anyString(), anyString(), anyString());
        
        ResponseEntity<String> response = controller.registerFileId("en", "home", "");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(registry, times(1)).registerFileId("en", "home", "");
    }
    @Test
    void registerFileId_WithNullFileId_HandlesGracefully() {
        doNothing().when(registry).registerFileId(anyString(), anyString(), anyString());
        
        assertDoesNotThrow(() -> {
            ResponseEntity<String> response = controller.registerFileId("en", "home", null);
            assertEquals(HttpStatus.OK, response.getStatusCode());
        });
    }
}


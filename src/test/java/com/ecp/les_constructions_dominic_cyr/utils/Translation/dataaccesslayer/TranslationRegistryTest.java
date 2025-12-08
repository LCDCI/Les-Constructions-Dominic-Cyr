package com.ecp.les_constructions_dominic_cyr.utils.Translation.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.dataaccesslayer.TranslationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TranslationRegistryTest {

    private TranslationRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new TranslationRegistry();
    }

    @Test
    void getFileId_WhenNotRegistered_ReturnsNull() {
        String result = registry.getFileId("en", "home");
        assertNull(result);
    }

    @Test
    void registerFileId_WithValidData_StoresFileId() {
        registry.registerFileId("en", "home", "file-id-123");
        
        String result = registry.getFileId("en", "home");
        assertEquals("file-id-123", result);
    }

    @Test
    void registerFileId_WithUpperCaseLanguage_StoresCorrectly() {
        registry.registerFileId("EN", "home", "file-id-123");
        
        String result = registry.getFileId("en", "home");
        assertEquals("file-id-123", result);
    }

    @Test
    void registerFileId_WithUpperCasePage_StoresCorrectly() {
        registry.registerFileId("en", "HOME", "file-id-123");
        
        String result = registry.getFileId("en", "home");
        assertEquals("file-id-123", result);
    }

    @Test
    void registerFileId_WithMixedCase_StoresCorrectly() {
        registry.registerFileId("En", "HoMe", "file-id-123");
        
        String result = registry.getFileId("EN", "HOME");
        assertEquals("file-id-123", result);
    }

    @Test
    void registerFileId_OverwritesExistingEntry() {
        registry.registerFileId("en", "home", "file-id-123");
        registry.registerFileId("en", "home", "file-id-456");
        
        String result = registry.getFileId("en", "home");
        assertEquals("file-id-456", result);
    }

    @Test
    void registerFileId_WithMultipleLanguages_StoresSeparately() {
        registry.registerFileId("en", "home", "file-id-en");
        registry.registerFileId("fr", "home", "file-id-fr");
        
        assertEquals("file-id-en", registry.getFileId("en", "home"));
        assertEquals("file-id-fr", registry.getFileId("fr", "home"));
    }

    @Test
    void registerFileId_WithMultiplePages_StoresSeparately() {
        registry.registerFileId("en", "home", "file-id-home");
        registry.registerFileId("en", "projects", "file-id-projects");
        
        assertEquals("file-id-home", registry.getFileId("en", "home"));
        assertEquals("file-id-projects", registry.getFileId("en", "projects"));
    }

    @Test
    void hasTranslation_WhenNotRegistered_ReturnsFalse() {
        assertFalse(registry.hasTranslation("en", "home"));
    }

    @Test
    void hasTranslation_WhenRegistered_ReturnsTrue() {
        registry.registerFileId("en", "home", "file-id-123");
        
        assertTrue(registry.hasTranslation("en", "home"));
    }

    @Test
    void hasTranslation_WithCaseInsensitive_ReturnsTrue() {
        registry.registerFileId("en", "home", "file-id-123");
        
        assertTrue(registry.hasTranslation("EN", "HOME"));
    }

    @Test
    void registerFileId_WithEmptyString_StoresCorrectly() {
        registry.registerFileId("", "", "file-id-123");
        
        String result = registry.getFileId("", "");
        assertEquals("file-id-123", result);
    }

    @Test
    void getFileId_WithSpecialCharacters_WorksCorrectly() {
        registry.registerFileId("en", "page-name", "file-id-123");
        
        String result = registry.getFileId("en", "page-name");
        assertEquals("file-id-123", result);
    }
}


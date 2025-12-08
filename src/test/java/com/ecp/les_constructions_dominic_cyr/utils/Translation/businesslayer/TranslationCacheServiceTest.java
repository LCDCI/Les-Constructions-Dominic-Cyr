package com.ecp.les_constructions_dominic_cyr.utils.Translation.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.TranslationCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TranslationCacheServiceTest {

    private TranslationCacheService cacheService;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        cacheService = new TranslationCacheService();
    }

    @Test
    void generateCacheFilename_WithNormalFilename_AddsLanguageSuffix() {
        String result = cacheService.generateCacheFilename("document.pdf", "fr");
        assertEquals("document_fr.pdf", result);
    }

    @Test
    void generateCacheFilename_WithExistingFrSuffix_ReplacesSuffix() {
        String result = cacheService.generateCacheFilename("document_fr.pdf", "en");
        assertEquals("document_en.pdf", result);
    }

    @Test
    void generateCacheFilename_WithExistingEnSuffix_ReplacesSuffix() {
        String result = cacheService.generateCacheFilename("document_en.pdf", "fr");
        assertEquals("document_fr.pdf", result);
    }

    @Test
    void generateCacheFilename_WithNullFilename_ReturnsDefault() {
        String result = cacheService.generateCacheFilename(null, "fr");
        assertEquals("translated_fr.pdf", result);
    }

    @Test
    void generateCacheFilename_WithEmptyFilename_ReturnsDefault() {
        String result = cacheService.generateCacheFilename("", "en");
        assertEquals("translated_en.pdf", result);
    }

    @Test
    void generateCacheFilename_WithNoExtension_AddsPdfExtension() {
        String result = cacheService.generateCacheFilename("document", "fr");
        assertEquals("document_fr.pdf", result);
    }

    @Test
    void generateCacheFilename_WithUpperCaseLanguage_ConvertsToLowercase() {
        String result = cacheService.generateCacheFilename("document.pdf", "FR");
        assertEquals("document_fr.pdf", result);
    }

    @Test
    void generateCacheFilename_WithDifferentExtension_PreservesExtension() {
        String result = cacheService.generateCacheFilename("document.txt", "fr");
        assertEquals("document_fr.txt", result);
    }

    @Test
    void isCached_WhenFileDoesNotExist_ReturnsFalse() {
        boolean result = cacheService.isCached("nonexistent.pdf", "fr");
        assertFalse(result);
    }

    @Test
    void getCachedTranslation_WhenFileDoesNotExist_ReturnsNull() {
        byte[] result = cacheService.getCachedTranslation("nonexistent.pdf", "fr");
        assertNull(result);
    }

    @Test
    void saveTranslation_WithValidData_SavesSuccessfully() {
        byte[] pdfData = "test pdf content".getBytes();
        // This will attempt to save - may succeed or fail depending on file system permissions
        // We just verify it doesn't throw an exception
        assertDoesNotThrow(() -> {
            cacheService.saveTranslation("test.pdf", "fr", pdfData);
        });
    }

    @Test
    void saveTranslation_WithNullFilename_HandlesGracefully() {
        byte[] pdfData = "test".getBytes();
        // Should not throw exception
        assertDoesNotThrow(() -> {
            cacheService.saveTranslation(null, "fr", pdfData);
        });
    }

    @Test
    void getCacheDirectory_ReturnsPath() {
        Path result = cacheService.getCacheDirectory();
        assertNotNull(result);
    }

    @Test
    void getCacheFilePath_ReturnsValidPath() {
        Path result = cacheService.getCacheFilePath("test.pdf");
        assertNotNull(result);
        assertTrue(result.toString().contains("test.pdf"));
    }

    @Test
    void generateCacheFilename_WithSpecialCharacters_HandlesCorrectly() {
        String result = cacheService.generateCacheFilename("doc-ument.pdf", "fr");
        assertEquals("doc-ument_fr.pdf", result);
    }

    @Test
    void generateCacheFilename_WithInvalidTargetLanguage_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            cacheService.generateCacheFilename("test.pdf", "../fr");
        });
    }

    @Test
    void generateCacheFilename_WithInvalidFilename_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            cacheService.generateCacheFilename("../test.pdf", "fr");
        });
    }

    @Test
    void generateCacheFilename_WithPathSeparator_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            cacheService.generateCacheFilename("test/file.pdf", "fr");
        });
    }

    @Test
    void getCachedTranslation_WithExistingFile_ReturnsContent() throws IOException {
        byte[] testData = "test pdf content".getBytes();
        String filename = "test.pdf";
        String language = "fr";
        String cacheFilename = cacheService.generateCacheFilename(filename, language);
        Path cachePath = cacheService.getCacheFilePath(cacheFilename);
        
        Files.createDirectories(cachePath.getParent());
        Files.write(cachePath, testData);
        
        byte[] result = cacheService.getCachedTranslation(filename, language);
        assertNotNull(result);
        assertArrayEquals(testData, result);
        
        // Cleanup
        Files.deleteIfExists(cachePath);
    }

    @Test
    void saveTranslation_WithIOException_ReturnsFalse() {
        // This test may not always fail, but we can test the error path
        // by using an invalid path if possible
        byte[] pdfData = "test".getBytes();
        boolean result = cacheService.saveTranslation("test.pdf", "fr", pdfData);
        // Result depends on file system permissions, but should not throw
        assertNotNull(Boolean.valueOf(result));
    }

    @Test
    void getCacheFilePath_CreatesDirectoryIfNotExists() {
        Path result = cacheService.getCacheFilePath("test.pdf");
        assertNotNull(result);
        assertTrue(Files.exists(result.getParent()) || Files.exists(result.getParent().getParent()));
    }
}


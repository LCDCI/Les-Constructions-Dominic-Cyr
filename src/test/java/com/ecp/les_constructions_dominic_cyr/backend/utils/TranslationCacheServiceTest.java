package com.ecp.les_constructions_dominic_cyr.backend.utils;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.TranslationCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class TranslationCacheServiceTest {

    private TranslationCacheService translationCacheService;

    @TempDir
    Path tempDir;

    private static final String ORIGINAL_FILENAME = "My_Car_Specs_fr.pdf";
    private static final String TARGET_LANGUAGE_EN = "en";
    private static final byte[] TEST_PDF_CONTENT = "Mock PDF Content".getBytes();

    @BeforeEach
    void setup() {
        translationCacheService = new TranslationCacheService();
        // Redirect the service to use the temp directory instead of real local storage
        ReflectionTestUtils.setField(translationCacheService, "basePath", tempDir);
    }

    @Test
    @DisplayName("saveTranslation should create the directory and save the file")
    void saveTranslation_writesFileAndCreatesDirectory() {
        boolean success = translationCacheService.saveTranslation(ORIGINAL_FILENAME, TARGET_LANGUAGE_EN, TEST_PDF_CONTENT);
        Path expectedFile = tempDir.resolve("My_Car_Specs_en.pdf");

        assertTrue(success);
        assertTrue(Files.exists(expectedFile));
    }

    @Test
    @DisplayName("getCachedTranslation should return content if cached")
    void getCachedTranslation_exists_returnsContent() throws IOException {
        String cacheFilename = "My_Car_Specs_en.pdf";
        Path cachePath = tempDir.resolve(cacheFilename);
        Files.write(cachePath, TEST_PDF_CONTENT);

        byte[] result = translationCacheService.getCachedTranslation(ORIGINAL_FILENAME, TARGET_LANGUAGE_EN);
        assertArrayEquals(TEST_PDF_CONTENT, result);
    }

    @Test
    @DisplayName("generateCacheFilename should handle various naming scenarios")
    void testGenerateCacheFilename() {
        assertEquals("car_en.pdf", translationCacheService.generateCacheFilename("car.pdf", "en"));
        assertEquals("doc_fr.pdf", translationCacheService.generateCacheFilename("doc_en.pdf", "FR"));
    }

    @Test
    @DisplayName("validatePathComponent should throw for malicious paths")
    void validatePathComponent_filenameWithSeparator_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> translationCacheService.generateCacheFilename("file/../name.pdf", "en"));
    }
}
package com.ecp.les_constructions_dominic_cyr.backend.utils;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.TranslationCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.DisplayName;
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
    void setup() throws IOException {
        translationCacheService = new TranslationCacheService();
        // Ensure the directory exists so the service doesn't fail on permissions
        Path cacheDir = Path.of("translations");
        if (!Files.exists(cacheDir)) {
            Files.createDirectories(cacheDir);
        }
    }

    @Test
    @DisplayName("saveTranslation should successfully save the file")
    void saveTranslation_writesFile() {
        boolean success = translationCacheService.saveTranslation(ORIGINAL_FILENAME, TARGET_LANGUAGE_EN, TEST_PDF_CONTENT);
        assertTrue(success);
    }

    @Test
    @DisplayName("isCached should return true if file exists")
    void isCached_fileExists_returnsTrue() throws IOException {
        String cacheFilename = translationCacheService.generateCacheFilename(ORIGINAL_FILENAME, TARGET_LANGUAGE_EN);
        Path cachePath = Path.of("translations", cacheFilename);

        Files.createDirectories(cachePath.getParent());
        Files.write(cachePath, TEST_PDF_CONTENT);

        assertTrue(translationCacheService.isCached(ORIGINAL_FILENAME, TARGET_LANGUAGE_EN));
    }

    @Test
    @DisplayName("generateCacheFilename handles various naming scenarios")
    void generateCacheFilename_logicTest() {
        assertEquals("car_en.pdf", translationCacheService.generateCacheFilename("car.pdf", "en"));
        assertEquals("translated_fr.pdf", translationCacheService.generateCacheFilename("", "fr"));
    }

    @Test
    @DisplayName("validatePathComponent should throw for malicious paths")
    void validatePathComponent_throwsOnIllegalChars() {
        assertThrows(IllegalArgumentException.class,
                () -> translationCacheService.generateCacheFilename("file/../name.pdf", "en"));
    }
}
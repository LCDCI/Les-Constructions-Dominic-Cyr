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
    private static final String TARGET_LANGUAGE_FR = "fr";
    private static final byte[] TEST_PDF_CONTENT = "Mock PDF Content".getBytes();


    @BeforeEach
    void setup() {
        translationCacheService = new TranslationCacheService();
    }



    @Test
    @DisplayName("saveTranslation should create the directory and save the file")
    void saveTranslation_writesFileAndCreatesDirectory() {
       boolean success = translationCacheService.saveTranslation(ORIGINAL_FILENAME, TARGET_LANGUAGE_EN, TEST_PDF_CONTENT);
        Path cachePath = translationCacheService.getCacheDirectory().resolve("My_Car_Specs_en.pdf");

        assertTrue(success);
        assertTrue(Files.exists(cachePath));
        assertDoesNotThrow(() -> {
            assertArrayEquals(TEST_PDF_CONTENT, Files.readAllBytes(cachePath));
        });

        try {
            Files.deleteIfExists(cachePath);
        } catch (IOException e) {

        }
    }

    @Test
    @DisplayName("isCached should return true if file exists")
    void isCached_fileExists_returnsTrue() throws IOException {
        String cacheFilename = translationCacheService.generateCacheFilename(ORIGINAL_FILENAME, TARGET_LANGUAGE_EN);
        Path cachePath = translationCacheService.getCacheFilePath(cacheFilename);

        Files.write(cachePath, TEST_PDF_CONTENT);

        assertTrue(translationCacheService.isCached(ORIGINAL_FILENAME, TARGET_LANGUAGE_EN));

        Files.deleteIfExists(cachePath);
    }

    @Test
    @DisplayName("isCached should return false if file does not exist")
    void isCached_fileDoesNotExist_returnsFalse() {
        assertFalse(translationCacheService.isCached("non_existent_file.pdf", TARGET_LANGUAGE_EN));
    }

    @Test
    @DisplayName("getCachedTranslation should return content if cached")
    void getCachedTranslation_exists_returnsContent() throws IOException {
        String cacheFilename = translationCacheService.generateCacheFilename(ORIGINAL_FILENAME, TARGET_LANGUAGE_EN);
        Path cachePath = translationCacheService.getCacheFilePath(cacheFilename);

        Files.write(cachePath, TEST_PDF_CONTENT);

        byte[] result = translationCacheService.getCachedTranslation(ORIGINAL_FILENAME, TARGET_LANGUAGE_EN);
        assertArrayEquals(TEST_PDF_CONTENT, result);

        Files.deleteIfExists(cachePath);
    }

    @Test
    @DisplayName("getCachedTranslation should return null if not cached")
    void getCachedTranslation_notExists_returnsNull() {
        assertNull(translationCacheService.getCachedTranslation("non_existent.pdf", TARGET_LANGUAGE_FR));
    }



    @Test
    @DisplayName("generateCacheFilename should handle original filename with existing _fr suffix")
    void generateCacheFilename_removesExistingFrSuffix() {
        String result = translationCacheService.generateCacheFilename(ORIGINAL_FILENAME, TARGET_LANGUAGE_EN); // My_Car_Specs_fr.pdf -> My_Car_Specs_en.pdf
        assertEquals("My_Car_Specs_en.pdf", result);
    }

    @Test
    @DisplayName("generateCacheFilename should handle original filename with existing _en suffix")
    void generateCacheFilename_removesExistingEnSuffix() {
        String result = translationCacheService.generateCacheFilename("My_Car_Specs_en.pdf", TARGET_LANGUAGE_FR);
        assertEquals("My_Car_Specs_fr.pdf", result);
    }

    @Test
    @DisplayName("generateCacheFilename should handle filename without existing language suffix")
    void generateCacheFilename_noExistingSuffix() {
        String result = translationCacheService.generateCacheFilename("car.pdf", TARGET_LANGUAGE_EN);
        assertEquals("car_en.pdf", result);
    }


    @Test
    @DisplayName("generateCacheFilename should handle null or empty original filename")
    void generateCacheFilename_nullOrEmptyOriginalFilename() {
        assertEquals("translated_fr.pdf", translationCacheService.generateCacheFilename(null, TARGET_LANGUAGE_FR));
        assertEquals("translated_en.pdf", translationCacheService.generateCacheFilename("", TARGET_LANGUAGE_EN));
    }

    @Test
    @DisplayName("generateCacheFilename should normalize target language to lowercase")
    void generateCacheFilename_uppercaseTargetLanguage_isLowercased() {
        String result = translationCacheService.generateCacheFilename("doc.pdf", "FR");
        assertEquals("doc_fr.pdf", result);
    }



    @Test
    @DisplayName("validatePathComponent should pass for safe inputs")
    void validatePathComponent_safeInput_noException() {

        assertDoesNotThrow(() -> translationCacheService.generateCacheFilename("safe_file-name.pdf", "en"));
        assertDoesNotThrow(() -> translationCacheService.generateCacheFilename("safe_file_name", "fr"));
    }

    @Test
    @DisplayName("validatePathComponent should throw for path separators in filename")
    void validatePathComponent_filenameWithSeparator_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> translationCacheService.generateCacheFilename("file/name.pdf", "en"));
        assertThrows(IllegalArgumentException.class,
                () -> translationCacheService.generateCacheFilename("file\\name.pdf", "en"));
    }

    @Test
    @DisplayName("validatePathComponent should throw for path separators in language")
    void validatePathComponent_languageWithSeparator_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> translationCacheService.generateCacheFilename("file.pdf", "e/n"));
    }

    @Test
    @DisplayName("validatePathComponent should throw for parent reference in filename")
    void validatePathComponent_filenameWithParentReference_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> translationCacheService.generateCacheFilename("..file.pdf", "en"));
    }
}
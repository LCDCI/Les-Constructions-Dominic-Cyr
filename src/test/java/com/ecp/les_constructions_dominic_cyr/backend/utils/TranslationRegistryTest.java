package com.ecp.les_constructions_dominic_cyr.backend.utils;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.dataaccesslayer.TranslationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TranslationRegistryTest {

    private TranslationRegistry registry;

    private static final String LANG_EN = "en";
    private static final String LANG_FR = "FR"; // Use mixed case for testing
    private static final String PAGE_HOME = "home";
    private static final String PAGE_PROJECTS = "PROJECTS"; // Use mixed case for testing
    private static final String FILE_ID_1 = "file-id-1";
    private static final String FILE_ID_2 = "file-id-2";

    @BeforeEach
    void setUp() {
        registry = new TranslationRegistry();
        registry.registerFileId(LANG_EN, PAGE_HOME, FILE_ID_1);
    }

    @Test
    void registerFileId_NewEntry_SavesCorrectly() {

        registry.registerFileId(LANG_FR, PAGE_PROJECTS, FILE_ID_2);

        assertEquals(FILE_ID_2, registry.getFileId("fr", "projects"), "New registration should be retrievable.");
    }

    @Test
    void registerFileId_CaseInsensitive_SavesLowercaseKey() {

        registry.registerFileId(LANG_FR, PAGE_PROJECTS, FILE_ID_2);

        assertEquals(FILE_ID_2, registry.getFileId("fr", "projects"), "Registration with uppercase should be stored and retrievable via lowercase.");
    }

    @Test
    void registerFileId_OverwritesExistingKey() {
        final String NEW_FILE_ID = "new-file-id";

        registry.registerFileId(LANG_EN, PAGE_HOME, NEW_FILE_ID);

        assertEquals(NEW_FILE_ID, registry.getFileId(LANG_EN, PAGE_HOME), "Registration should overwrite the existing file ID.");
    }

    @Test
    void getFileId_ExistingKey_ReturnsFileId() {
        String retrievedId = registry.getFileId(LANG_EN, PAGE_HOME);

        assertEquals(FILE_ID_1, retrievedId, "Should return the correct file ID for an existing key.");
    }

    @Test
    void getFileId_CaseInsensitive_ReturnsCorrectFileId() {

        assertEquals(FILE_ID_1, registry.getFileId("En", "HoMe"), "Retrieval should be case-insensitive.");
    }

    @Test
    void getFileId_NonExistentKey_ReturnsNull() {

        String retrievedId = registry.getFileId("de", "about");

        assertNull(retrievedId, "Should return null for a non-existent key.");
    }

    @Test
    void hasTranslation_ExistingKey_ReturnsTrue() {

        assertTrue(registry.hasTranslation(LANG_EN, PAGE_HOME), "Should return true for an existing translation.");
    }

    @Test
    void hasTranslation_CaseInsensitive_ReturnsTrue() {

        assertTrue(registry.hasTranslation("EN", "home"), "Should be case-insensitive and return true.");
    }

    @Test
    void hasTranslation_NonExistentKey_ReturnsFalse() {

        assertFalse(registry.hasTranslation("es", "about"), "Should return false for a non-existent translation.");
    }
}

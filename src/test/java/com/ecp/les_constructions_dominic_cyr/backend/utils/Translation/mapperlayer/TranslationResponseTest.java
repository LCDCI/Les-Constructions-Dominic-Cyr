package com.ecp.les_constructions_dominic_cyr.backend.utils.Translation.mapperlayer;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.mapperlayer.TranslationResponse;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TranslationResponseTest {

    @Test
    void builder_CreatesResponseWithAllFields() {
        Map<String, Object> translations = new HashMap<>();
        translations.put("key1", "value1");
        
        TranslationResponse response = TranslationResponse.builder()
                .language("en")
                .translations(translations)
                .build();
        
        assertEquals("en", response.getLanguage());
        assertEquals(translations, response.getTranslations());
    }

    @Test
    void noArgsConstructor_CreatesEmptyResponse() {
        TranslationResponse response = new TranslationResponse();
        
        assertNull(response.getLanguage());
        assertNull(response.getTranslations());
    }

    @Test
    void allArgsConstructor_CreatesResponseWithAllFields() {
        Map<String, Object> translations = new HashMap<>();
        translations.put("key1", "value1");
        
        TranslationResponse response = new TranslationResponse("fr", translations);
        
        assertEquals("fr", response.getLanguage());
        assertEquals(translations, response.getTranslations());
    }

    @Test
    void setters_UpdateFieldsCorrectly() {
        TranslationResponse response = new TranslationResponse();
        Map<String, Object> translations = new HashMap<>();
        translations.put("key1", "value1");
        
        response.setLanguage("en");
        response.setTranslations(translations);
        
        assertEquals("en", response.getLanguage());
        assertEquals(translations, response.getTranslations());
    }

    @Test
    void equals_WithSameValues_ReturnsTrue() {
        Map<String, Object> translations1 = new HashMap<>();
        translations1.put("key1", "value1");
        
        Map<String, Object> translations2 = new HashMap<>();
        translations2.put("key1", "value1");
        
        TranslationResponse response1 = TranslationResponse.builder()
                .language("en")
                .translations(translations1)
                .build();
        
        TranslationResponse response2 = TranslationResponse.builder()
                .language("en")
                .translations(translations2)
                .build();
        
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void equals_WithDifferentValues_ReturnsFalse() {
        TranslationResponse response1 = TranslationResponse.builder()
                .language("en")
                .translations(new HashMap<>())
                .build();
        
        TranslationResponse response2 = TranslationResponse.builder()
                .language("fr")
                .translations(new HashMap<>())
                .build();
        
        assertNotEquals(response1, response2);
    }

    @Test
    void toString_ContainsAllFields() {
        Map<String, Object> translations = new HashMap<>();
        translations.put("key1", "value1");
        
        TranslationResponse response = TranslationResponse.builder()
                .language("en")
                .translations(translations)
                .build();
        
        String toString = response.toString();
        assertTrue(toString.contains("en"));
        assertTrue(toString.contains("TranslationResponse"));
    }
}


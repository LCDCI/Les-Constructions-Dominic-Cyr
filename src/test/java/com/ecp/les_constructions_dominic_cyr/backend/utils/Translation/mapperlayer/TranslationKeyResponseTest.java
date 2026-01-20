package com.ecp.les_constructions_dominic_cyr.backend.utils.Translation.mapperlayer;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.mapperlayer.TranslationKeyResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TranslationKeyResponseTest {

    @Test
    void builder_CreatesResponseWithAllFields() {
        TranslationKeyResponse response = TranslationKeyResponse.builder()
                .key("home.title")
                .value("Welcome")
                .language("en")
                .build();
        
        assertEquals("home.title", response.getKey());
        assertEquals("Welcome", response.getValue());
        assertEquals("en", response.getLanguage());
    }

    @Test
    void noArgsConstructor_CreatesEmptyResponse() {
        TranslationKeyResponse response = new TranslationKeyResponse();
        
        assertNull(response.getKey());
        assertNull(response.getValue());
        assertNull(response.getLanguage());
    }

    @Test
    void allArgsConstructor_CreatesResponseWithAllFields() {
        TranslationKeyResponse response = new TranslationKeyResponse("home.title", "Bienvenue", "fr");
        
        assertEquals("home.title", response.getKey());
        assertEquals("Bienvenue", response.getValue());
        assertEquals("fr", response.getLanguage());
    }

    @Test
    void setters_UpdateFieldsCorrectly() {
        TranslationKeyResponse response = new TranslationKeyResponse();
        
        response.setKey("home.title");
        response.setValue("Welcome");
        response.setLanguage("en");
        
        assertEquals("home.title", response.getKey());
        assertEquals("Welcome", response.getValue());
        assertEquals("en", response.getLanguage());
    }

    @Test
    void equals_WithSameValues_ReturnsTrue() {
        TranslationKeyResponse response1 = TranslationKeyResponse.builder()
                .key("home.title")
                .value("Welcome")
                .language("en")
                .build();
        
        TranslationKeyResponse response2 = TranslationKeyResponse.builder()
                .key("home.title")
                .value("Welcome")
                .language("en")
                .build();
        
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void equals_WithDifferentValues_ReturnsFalse() {
        TranslationKeyResponse response1 = TranslationKeyResponse.builder()
                .key("home.title")
                .value("Welcome")
                .language("en")
                .build();
        
        TranslationKeyResponse response2 = TranslationKeyResponse.builder()
                .key("home.title")
                .value("Bienvenue")
                .language("fr")
                .build();
        
        assertNotEquals(response1, response2);
    }

    @Test
    void toString_ContainsAllFields() {
        TranslationKeyResponse response = TranslationKeyResponse.builder()
                .key("home.title")
                .value("Welcome")
                .language("en")
                .build();
        
        String toString = response.toString();
        assertTrue(toString.contains("home.title"));
        assertTrue(toString.contains("Welcome"));
        assertTrue(toString.contains("en"));
        assertTrue(toString.contains("TranslationKeyResponse"));
    }
}


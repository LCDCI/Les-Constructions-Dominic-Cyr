package com.ecp.les_constructions_dominic_cyr.backend.utils.Translation.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.TranslationService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.mapperlayer.TranslationKeyResponse;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.mapperlayer.TranslationResponse;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.presentationlayer.TranslationController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranslationControllerUnitTest {

    @Mock
    private TranslationService translationService;

    @InjectMocks
    private TranslationController translationController;

    @BeforeEach
    void setUp() {
        // Setup default mocks
        when(translationService.getDefaultLanguage()).thenReturn("en");
        when(translationService.isLanguageSupported(anyString())).thenReturn(true);
    }
    @Test
    void getAllTranslations_WithValidLanguage_ReturnsOk() {
        Map<String, Object> translations = new HashMap<>();
        translations.put("home", Map.of("title", "Welcome"));
        
        when(translationService.getAllTranslations("en")).thenReturn(Mono.just(translations));
        
        StepVerifier.create(translationController.getAllTranslations("en"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals("en", response.getBody().getLanguage());
                    assertEquals(translations, response.getBody().getTranslations());
                })
                .verifyComplete();
    }

    @Test
    void getAllTranslations_WithInvalidLanguage_DefaultsToEnglish() {
        Map<String, Object> translations = new HashMap<>();
        when(translationService.isLanguageSupported("invalid")).thenReturn(false);
        when(translationService.getAllTranslations("en")).thenReturn(Mono.just(translations));
        
        StepVerifier.create(translationController.getAllTranslations("invalid"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("en", response.getBody().getLanguage());
                })
                .verifyComplete();
    }
    @Test
    void getAllTranslations_WithNullLanguage_DefaultsToEnglish() {
        Map<String, Object> translations = new HashMap<>();
        when(translationService.isLanguageSupported(null)).thenReturn(false);
        when(translationService.getAllTranslations("en")).thenReturn(Mono.just(translations));
        
        StepVerifier.create(translationController.getAllTranslations(null))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("en", response.getBody().getLanguage());
                })
                .verifyComplete();
    }

    @Test
    void getAllTranslations_WithError_ReturnsInternalServerError() {
        when(translationService.getAllTranslations("en"))
                .thenReturn(Mono.error(new RuntimeException("Service error")));
        
        StepVerifier.create(translationController.getAllTranslations("en"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals("en", response.getBody().getLanguage());
                    assertTrue(response.getBody().getTranslations().isEmpty());
                })
                .verifyComplete();
    }
    @Test
    void getDefaultTranslations_CallsGetAllTranslationsWithDefault() {
        Map<String, Object> translations = new HashMap<>();
        when(translationService.getAllTranslations("en")).thenReturn(Mono.just(translations));
        
        StepVerifier.create(translationController.getDefaultTranslations())
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("en", response.getBody().getLanguage());
                })
                .verifyComplete();
        
        verify(translationService, times(1)).getDefaultLanguage();
    }

    @Test
    void getTranslation_WithValidKey_ReturnsOk() {
        when(translationService.getTranslation("home.title", "en"))
                .thenReturn(Mono.just("Welcome"));
        
        StepVerifier.create(translationController.getTranslation("home.title", "en"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals("home.title", response.getBody().getKey());
                    assertEquals("Welcome", response.getBody().getValue());
                    assertEquals("en", response.getBody().getLanguage());
                })
                .verifyComplete();
    }

    @Test
    void getTranslation_WithError_ReturnsNotFound() {
        when(translationService.getTranslation(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Error")));

        StepVerifier.create(translationController.getTranslation("key", "en"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void getTranslationsBatch_WithValidKeys_ReturnsOk() {
        Map<String, String> translations = new HashMap<>();
        translations.put("key1", "value1");
        translations.put("key2", "value2");

        when(translationService.getTranslations(anyList(), anyString()))
                .thenReturn(Mono.just(translations));

        List<String> keys = Arrays.asList("key1", "key2");

        StepVerifier.create(translationController.getTranslationsBatch("en", keys))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals("value1", response.getBody().get("key1"));
                    assertEquals("value2", response.getBody().get("key2"));
                })
                .verifyComplete();
    }

    @Test
    void getTranslationsBatch_WithError_ReturnsInternalServerError() {
        when(translationService.getTranslations(anyList(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Error")));

        StepVerifier.create(translationController.getTranslationsBatch("en", Arrays.asList("key1")))
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void getPageTranslations_WithValidPage_ReturnsOk() {
        Map<String, Object> translations = new HashMap<>();
        translations.put("title", "Home Page");

        when(translationService.getPageTranslations("home", "en"))
                .thenReturn(Mono.just(translations));

        StepVerifier.create(translationController.getPageTranslations("home", "en"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals("en", response.getBody().getLanguage());
                    assertEquals(translations, response.getBody().getTranslations());
                })
                .verifyComplete();
    }

    @Test
    void getPageTranslations_WithError_ReturnsInternalServerError() {
        when(translationService.getPageTranslations(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Error")));
        
        StepVerifier.create(translationController.getPageTranslations("home", "en"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals("en", response.getBody().getLanguage());
                    assertTrue(response.getBody().getTranslations().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void getTranslationsByNamespace_WithValidNamespace_ReturnsOk() {
        Map<String, Object> translations = new HashMap<>();
        translations.put("key", "value");

        when(translationService.getTranslationsByNamespace("home", "en"))
                .thenReturn(Mono.just(translations));

        StepVerifier.create(translationController.getTranslationsByNamespace("home", "en"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals("en", response.getBody().getLanguage());
                    assertEquals(translations, response.getBody().getTranslations());
                })
                .verifyComplete();
    }

    @Test
    void getTranslationsByNamespace_WithError_ReturnsInternalServerError() {
        when(translationService.getTranslationsByNamespace(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Error")));

        StepVerifier.create(translationController.getTranslationsByNamespace("home", "en"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals("en", response.getBody().getLanguage());
                    assertTrue(response.getBody().getTranslations().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void getSupportedLanguages_ReturnsList() {
        List<String> languages = Arrays.asList("en", "fr");
        when(translationService.getSupportedLanguages()).thenReturn(languages);

        StepVerifier.create(translationController.getSupportedLanguages())
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(2, response.getBody().size());
                    assertTrue(response.getBody().contains("en"));
                    assertTrue(response.getBody().contains("fr"));
                })
                .verifyComplete();
    }

    @Test
    void getAllTranslations_WithUppercaseLanguage_NormalizesToLowercase() {
        Map<String, Object> translations = new HashMap<>();
        when(translationService.isLanguageSupported("FR")).thenReturn(true);
        when(translationService.getAllTranslations("fr")).thenReturn(Mono.just(translations));
        
        StepVerifier.create(translationController.getAllTranslations("FR"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("fr", response.getBody().getLanguage());
                })
                .verifyComplete();
    }
}


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
        // Don't set default mocks here - set them per test to avoid unnecessary stubbing
    }

    @Test
    void getAllTranslations_WithValidLanguage_ReturnsTranslations() {
        Map<String, Object> translations = Map.of("key1", "value1", "key2", "value2");
        when(translationService.isLanguageSupported("fr")).thenReturn(true);
        when(translationService.getAllTranslations("fr")).thenReturn(Mono.just(translations));
        
        StepVerifier.create(translationController.getAllTranslations("fr"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("fr", response.getBody().getLanguage());
                    assertEquals(translations, response.getBody().getTranslations());
                })
                .verifyComplete();
    }

    @Test
    void getAllTranslations_WithInvalidLanguage_DefaultsToEnglish() {
        Map<String, Object> translations = new HashMap<>();
        when(translationService.isLanguageSupported("invalid")).thenReturn(false);
        when(translationService.getDefaultLanguage()).thenReturn("en");
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
        // When language is null, the controller doesn't call isLanguageSupported, it directly uses default
        when(translationService.getDefaultLanguage()).thenReturn("en");
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
        when(translationService.isLanguageSupported("fr")).thenReturn(true);
        when(translationService.getAllTranslations("fr")).thenReturn(Mono.error(new RuntimeException("Service error")));
        
        StepVerifier.create(translationController.getAllTranslations("fr"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertEquals("fr", response.getBody().getLanguage());
                    assertTrue(response.getBody().getTranslations().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void getDefaultTranslations_CallsGetAllTranslationsWithDefault() {
        Map<String, Object> translations = new HashMap<>();
        when(translationService.getDefaultLanguage()).thenReturn("en");
        when(translationService.isLanguageSupported("en")).thenReturn(true);
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
    void getTranslation_WithValidKeyAndLanguage_ReturnsTranslation() {
        when(translationService.isLanguageSupported("fr")).thenReturn(true);
        when(translationService.getTranslation("test.key", "fr")).thenReturn(Mono.just("Test Value"));
        
        StepVerifier.create(translationController.getTranslation("test.key", "fr"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("test.key", response.getBody().getKey());
                    assertEquals("Test Value", response.getBody().getValue());
                    assertEquals("fr", response.getBody().getLanguage());
                })
                .verifyComplete();
    }

    @Test
    void getTranslation_WithInvalidLanguage_DefaultsToEnglish() {
        when(translationService.isLanguageSupported("invalid")).thenReturn(false);
        when(translationService.getDefaultLanguage()).thenReturn("en");
        when(translationService.getTranslation("test.key", "en")).thenReturn(Mono.just("Test Value"));
        
        StepVerifier.create(translationController.getTranslation("test.key", "invalid"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("en", response.getBody().getLanguage());
                })
                .verifyComplete();
    }

    @Test
    void getTranslation_WithError_ReturnsNotFound() {
        when(translationService.isLanguageSupported("fr")).thenReturn(true);
        when(translationService.getTranslation("test.key", "fr")).thenReturn(Mono.error(new RuntimeException("Not found")));
        
        StepVerifier.create(translationController.getTranslation("test.key", "fr"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void getTranslationsBatch_WithValidKeys_ReturnsTranslations() {
        List<String> keys = List.of("key1", "key2");
        Map<String, String> translations = Map.of("key1", "value1", "key2", "value2");
        when(translationService.isLanguageSupported("fr")).thenReturn(true);
        when(translationService.getTranslations(keys, "fr")).thenReturn(Mono.just(translations));
        
        StepVerifier.create(translationController.getTranslationsBatch("fr", keys))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(translations, response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void getTranslationsBatch_WithError_ReturnsInternalServerError() {
        List<String> keys = List.of("key1");
        when(translationService.isLanguageSupported("fr")).thenReturn(true);
        when(translationService.getTranslations(keys, "fr")).thenReturn(Mono.error(new RuntimeException("Error")));
        
        StepVerifier.create(translationController.getTranslationsBatch("fr", keys))
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void getPageTranslations_WithValidPage_ReturnsPageTranslations() {
        Map<String, Object> translations = Map.of("page.key", "value");
        when(translationService.isLanguageSupported("fr")).thenReturn(true);
        when(translationService.getPageTranslations("home", "fr")).thenReturn(Mono.just(translations));
        
        StepVerifier.create(translationController.getPageTranslations("home", "fr"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("fr", response.getBody().getLanguage());
                    assertEquals(translations, response.getBody().getTranslations());
                })
                .verifyComplete();
    }

    @Test
    void getPageTranslations_WithError_ReturnsInternalServerError() {
        when(translationService.isLanguageSupported("fr")).thenReturn(true);
        when(translationService.getPageTranslations("home", "fr")).thenReturn(Mono.error(new RuntimeException("Error")));
        
        StepVerifier.create(translationController.getPageTranslations("home", "fr"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertEquals("fr", response.getBody().getLanguage());
                    assertTrue(response.getBody().getTranslations().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void getTranslationsByNamespace_WithValidNamespace_ReturnsTranslations() {
        Map<String, Object> translations = Map.of("namespace.key", "value");
        when(translationService.isLanguageSupported("fr")).thenReturn(true);
        when(translationService.getTranslationsByNamespace("messages", "fr")).thenReturn(Mono.just(translations));
        
        StepVerifier.create(translationController.getTranslationsByNamespace("messages", "fr"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("fr", response.getBody().getLanguage());
                    assertEquals(translations, response.getBody().getTranslations());
                })
                .verifyComplete();
    }

    @Test
    void getTranslationsByNamespace_WithError_ReturnsInternalServerError() {
        when(translationService.isLanguageSupported("fr")).thenReturn(true);
        when(translationService.getTranslationsByNamespace("messages", "fr")).thenReturn(Mono.error(new RuntimeException("Error")));
        
        StepVerifier.create(translationController.getTranslationsByNamespace("messages", "fr"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertEquals("fr", response.getBody().getLanguage());
                    assertTrue(response.getBody().getTranslations().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void getSupportedLanguages_ReturnsListOfLanguages() {
        List<String> languages = List.of("en", "fr");
        when(translationService.getSupportedLanguages()).thenReturn(languages);
        
        StepVerifier.create(translationController.getSupportedLanguages())
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(languages, response.getBody());
                })
                .verifyComplete();
    }

}


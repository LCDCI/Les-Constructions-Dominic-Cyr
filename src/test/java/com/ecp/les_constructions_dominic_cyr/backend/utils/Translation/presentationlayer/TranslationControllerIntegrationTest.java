package com.ecp.les_constructions_dominic_cyr.backend.utils.Translation.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.TranslationService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.mapperlayer.TranslationResponse;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.presentationlayer.TranslationController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebFluxTest(TranslationController.class)
class TranslationControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TranslationService translationService;

    @BeforeEach
    void setUp() {
        when(translationService.getDefaultLanguage()).thenReturn("en");
        when(translationService.isLanguageSupported(anyString())).thenReturn(true);
    }

    @Test
    void getAllTranslations_WithValidLanguage_ReturnsOk() {
        Map<String, Object> translations = new HashMap<>();
        translations.put("home", Map.of("title", "Welcome"));
        
        when(translationService.getAllTranslations("en")).thenReturn(Mono.just(translations));
        
        webTestClient.get()
                .uri("/api/v1/translations/en")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(TranslationResponse.class)
                .value(response -> {
                    assertEquals("en", response.getLanguage());
                    assertNotNull(response.getTranslations());
                    assertTrue(response.getTranslations().containsKey("home"));
                });
    }

    @Test
    void getAllTranslations_WithDefaultEndpoint_ReturnsOk() {
        Map<String, Object> translations = new HashMap<>();
        when(translationService.getAllTranslations("en")).thenReturn(Mono.just(translations));
        
        webTestClient.get()
                .uri("/api/v1/translations")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TranslationResponse.class);
    }

    @Test
    void getAllTranslations_WithInvalidLanguage_DefaultsToEnglish() {
        Map<String, Object> translations = new HashMap<>();
        when(translationService.isLanguageSupported("invalid")).thenReturn(false);
        when(translationService.getAllTranslations("en")).thenReturn(Mono.just(translations));
        
        webTestClient.get()
                .uri("/api/v1/translations/invalid")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TranslationResponse.class)
                .value(response -> assertEquals("en", response.getLanguage()));
    }

    @Test
    void getAllTranslations_WithServiceError_ReturnsInternalServerError() {
        when(translationService.getAllTranslations("en"))
                .thenReturn(Mono.error(new RuntimeException("Service error")));
        
        webTestClient.get()
                .uri("/api/v1/translations/en")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.language").isEqualTo("en")
                .jsonPath("$.translations").isEmpty();
    }

    @Test
    void getTranslation_WithValidKey_ReturnsOk() {
        when(translationService.getTranslation("home.title", "en"))
                .thenReturn(Mono.just("Welcome"));
        
        webTestClient.get()
                .uri("/api/v1/translations/en/home.title")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.key").isEqualTo("home.title")
                .jsonPath("$.value").isEqualTo("Welcome")
                .jsonPath("$.language").isEqualTo("en");
    }

    @Test
    void getTranslation_WithError_ReturnsNotFound() {
        when(translationService.getTranslation(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Error")));
        
        webTestClient.get()
                .uri("/api/v1/translations/en/invalid.key")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getTranslationsBatch_WithValidKeys_ReturnsOk() {
        Map<String, String> translations = new HashMap<>();
        translations.put("key1", "value1");
        translations.put("key2", "value2");
        
        when(translationService.getTranslations(anyList(), anyString()))
                .thenReturn(Mono.just(translations));
        
        webTestClient.post()
                .uri("/api/v1/translations/en/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Arrays.asList("key1", "key2"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.key1").isEqualTo("value1")
                .jsonPath("$.key2").isEqualTo("value2");
    }

    @Test
    void getTranslationsBatch_WithError_ReturnsInternalServerError() {
        when(translationService.getTranslations(anyList(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Error")));
        
        webTestClient.post()
                .uri("/api/v1/translations/en/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Arrays.asList("key1"))
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void getPageTranslations_WithValidPage_ReturnsOk() {
        Map<String, Object> translations = new HashMap<>();
        translations.put("title", "Home Page");
        
        when(translationService.getPageTranslations("home", "en"))
                .thenReturn(Mono.just(translations));
        
        webTestClient.get()
                .uri("/api/v1/translations/en/page/home")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TranslationResponse.class)
                .value(response -> {
                    assertEquals("en", response.getLanguage());
                    assertEquals("Home Page", response.getTranslations().get("title"));
                });
    }

    @Test
    void getPageTranslations_WithError_ReturnsInternalServerError() {
        when(translationService.getPageTranslations(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Error")));
        
        webTestClient.get()
                .uri("/api/v1/translations/en/page/home")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.language").isEqualTo("en")
                .jsonPath("$.translations").isEmpty();
    }

    @Test
    void getTranslationsByNamespace_WithValidNamespace_ReturnsOk() {
        Map<String, Object> translations = new HashMap<>();
        translations.put("key", "value");
        
        when(translationService.getTranslationsByNamespace("home", "en"))
                .thenReturn(Mono.just(translations));
        
        webTestClient.get()
                .uri("/api/v1/translations/en/namespace/home")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TranslationResponse.class)
                .value(response -> {
                    assertEquals("en", response.getLanguage());
                    assertEquals("value", response.getTranslations().get("key"));
                });
    }
    @Test
    void getSupportedLanguages_ReturnsList() {
        List<String> languages = Arrays.asList("en", "fr");
        when(translationService.getSupportedLanguages()).thenReturn(languages);
        
        webTestClient.get()
                .uri("/api/v1/translations/languages")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(String.class)
                .hasSize(2)
                .contains("en", "fr");
    }
    @Test
    void getAllTranslations_WithFrenchLanguage_ReturnsFrench() {
        Map<String, Object> translations = new HashMap<>();
        when(translationService.isLanguageSupported("fr")).thenReturn(true);
        when(translationService.getAllTranslations("fr")).thenReturn(Mono.just(translations));
        
        webTestClient.get()
                .uri("/api/v1/translations/fr")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TranslationResponse.class)
                .value(response -> assertEquals("fr", response.getLanguage()));
    }
}


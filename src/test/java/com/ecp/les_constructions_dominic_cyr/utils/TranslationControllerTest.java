package com.ecp.les_constructions_dominic_cyr.utils;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranslationControllerTest {

    private WebTestClient webTestClient;

    @Mock
    private TranslationService translationService;

    @InjectMocks
    private TranslationController translationController;

    private static final String DEFAULT_LANG = "en";
    private static final String FR_LANG = "fr";
    private static final Map<String, Object> ALL_TRANSLATIONS = Map.of(
            "home", Map.of("title", "Accueil"),
            "nav", Map.of("about", "À propos")
    );
    private static final Map<String, String> BATCH_TRANSLATIONS = Map.of(
            "key1", "value1",
            "key2", "value2"
    );

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToController(translationController).build();
    }



    @Test
    void getDefaultTranslations_Success() {
        when(translationService.getDefaultLanguage()).thenReturn(DEFAULT_LANG);
        when(translationService.isLanguageSupported(DEFAULT_LANG)).thenReturn(true);
        when(translationService.getAllTranslations(eq(DEFAULT_LANG))).thenReturn(Mono.just(ALL_TRANSLATIONS));

        webTestClient.get().uri("/api/v1/translations")
                .exchange()
                .expectStatus().isOk()
                .expectBody(TranslationResponse.class)
                .isEqualTo(TranslationResponse.builder().language(DEFAULT_LANG).translations(ALL_TRANSLATIONS).build());
    }

    @Test
    void getAllTranslations_UnsupportedLanguage_DefaultsToEn() {
        when(translationService.getDefaultLanguage()).thenReturn(DEFAULT_LANG);
        when(translationService.isLanguageSupported("es")).thenReturn(false);
        when(translationService.getAllTranslations(eq(DEFAULT_LANG))).thenReturn(Mono.just(ALL_TRANSLATIONS));

        webTestClient.get().uri("/api/v1/translations/{language}", "es")
                .exchange()
                .expectStatus().isOk()
                .expectBody(TranslationResponse.class)
                .value(response -> {
                    assert(response.getLanguage().equals(DEFAULT_LANG));
                });
    }



    @Test
    void getTranslation_UnsupportedLanguage_DefaultsToEn() {
        String key = "home.title";
        String value = "Home";
        when(translationService.getDefaultLanguage()).thenReturn(DEFAULT_LANG);
        when(translationService.isLanguageSupported("es")).thenReturn(false);
        when(translationService.getTranslation(eq(key), eq(DEFAULT_LANG))).thenReturn(Mono.just(value));

        webTestClient.get().uri("/api/v1/translations/{language}/{key}", "es", key)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TranslationKeyResponse.class)
                .value(response -> {
                    assert(response.getLanguage().equals(DEFAULT_LANG));
                });
    }

    @Test
    void getTranslationsBatch_UnsupportedLanguage_DefaultsToEn() {
        List<String> keys = List.of("key1");
        when(translationService.getDefaultLanguage()).thenReturn(DEFAULT_LANG);
        when(translationService.isLanguageSupported("es")).thenReturn(false);
        when(translationService.getTranslations(eq(keys), eq(DEFAULT_LANG))).thenReturn(Mono.just(BATCH_TRANSLATIONS));

        webTestClient.post().uri("/api/v1/translations/{language}/batch", "es")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(keys)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .isEqualTo(BATCH_TRANSLATIONS);
    }


    @Test
    void getTranslationsByNamespace_UnsupportedLanguage_DefaultsToEn() {
        String namespace = "messages";
        Map<String, Object> namespaceTranslations = Map.of("error", "An error occurred");
        when(translationService.getDefaultLanguage()).thenReturn(DEFAULT_LANG);
        when(translationService.isLanguageSupported("es")).thenReturn(false);
        when(translationService.getTranslationsByNamespace(eq(namespace), eq(DEFAULT_LANG))).thenReturn(Mono.just(namespaceTranslations));

        webTestClient.get().uri("/api/v1/translations/{language}/namespace/{namespace}", "es", namespace)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TranslationResponse.class)
                .value(response -> {
                    assert(response.getLanguage().equals(DEFAULT_LANG));
                });
    }
}
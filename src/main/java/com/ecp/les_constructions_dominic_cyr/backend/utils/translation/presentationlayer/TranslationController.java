package com.ecp.les_constructions_dominic_cyr.backend.utils.translation.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.TranslationService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.mapperlayer.TranslationKeyResponse;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.mapperlayer.TranslationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Reactive REST controller for translation endpoints.
 * Provides endpoints to retrieve translations for i18next frontend integration.
 */
@RestController
@RequestMapping("/api/v1/translations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class TranslationController {

    private final TranslationService translationService;

    /**
     * Retrieves all translations for a specific language.
     * Returns translations in a nested structure compatible with i18next.
     *
     * @param language the language code (e.g., "en", "fr"). Defaults to "en" if not provided.
     * @return Mono containing TranslationResponse with all translations
     */
    @GetMapping(value = "/{language}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<TranslationResponse>> getAllTranslations(
            @PathVariable String language) {
        
        String lang = (language != null && translationService.isLanguageSupported(language)) 
                ? language.toLowerCase() 
                : translationService.getDefaultLanguage();
        
        return translationService.getAllTranslations(lang)
                .map(translations -> TranslationResponse.builder()
                        .language(lang)
                        .translations(translations)
                        .build())
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    System.err.println("Error in TranslationController.getAllTranslations: " + error.getMessage());
                    error.printStackTrace();
                    return Mono.just(
                            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(TranslationResponse.builder()
                                            .language(lang)
                                            .translations(Map.of())
                                            .build()));
                });
    }

    /**
     * Retrieves all translations for the default language (English).
     *
     * @return Mono containing TranslationResponse with all translations
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<TranslationResponse>> getDefaultTranslations() {
        return getAllTranslations(translationService.getDefaultLanguage());
    }

    /**
     * Retrieves a specific translation by key for a given language.
     *
     * @param key the translation key
     * @param language the language code (e.g., "en", "fr")
     * @return Mono containing TranslationKeyResponse with the translated value
     */
    @GetMapping(value = "/{language}/{key:.+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<TranslationKeyResponse>> getTranslation(
            @PathVariable String key,
            @PathVariable String language) {
        
        String lang = (language != null && translationService.isLanguageSupported(language)) 
                ? language.toLowerCase() 
                : translationService.getDefaultLanguage();
        
        return translationService.getTranslation(key, lang)
                .map(value -> TranslationKeyResponse.builder()
                        .key(key)
                        .value(value)
                        .language(lang)
                        .build())
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(
                        ResponseEntity.status(HttpStatus.NOT_FOUND).build()));
    }

    /**
     * Retrieves multiple translations by keys for a given language.
     * Accepts a list of keys in the request body.
     *
     * @param language the language code
     * @param keys list of translation keys
     * @return Mono containing a map of keys and their translated values
     */
    @PostMapping(value = "/{language}/batch", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, String>>> getTranslationsBatch(
            @PathVariable String language,
            @RequestBody List<String> keys) {
        
        String lang = (language != null && translationService.isLanguageSupported(language)) 
                ? language.toLowerCase() 
                : translationService.getDefaultLanguage();
        
        return translationService.getTranslations(keys, lang)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    /**
     * Retrieves translations for a specific page/namespace.
     * Returns translations for a single page (e.g., "home", "projects").
     *
     * @param pageName the page name (e.g., "home", "projects")
     * @param language the language code (e.g., "en", "fr"). Defaults to "en" if not provided.
     * @return Mono containing TranslationResponse with page translations
     */
    @GetMapping(value = "/{language}/page/{pageName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<TranslationResponse>> getPageTranslations(
            @PathVariable String pageName,
            @PathVariable String language) {
        
        String lang = (language != null && translationService.isLanguageSupported(language))
                ? language.toLowerCase()
                : translationService.getDefaultLanguage();
        
        return translationService.getPageTranslations(pageName, lang)
                .map(translations -> TranslationResponse.builder()
                        .language(lang)
                        .translations(translations)
                        .build())
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    System.err.println("Error in TranslationController.getPageTranslations: " + error.getMessage());
                    error.printStackTrace();
                    return Mono.just(
                            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(TranslationResponse.builder()
                                            .language(lang)
                                            .translations(Map.of())
                                            .build()));
                });
    }

    /**
     * Retrieves translations for a specific namespace.
     * Alias for getPageTranslations for consistency.
     *
     * @param namespace the namespace (e.g., "home", "messages")
     * @param language the language code (e.g., "en", "fr"). Defaults to "en" if not provided.
     * @return Mono containing TranslationResponse with namespace translations
     */
    @GetMapping(value = "/{language}/namespace/{namespace}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<TranslationResponse>> getTranslationsByNamespace(
            @PathVariable String namespace,
            @PathVariable String language) {
        
        String lang = (language != null && translationService.isLanguageSupported(language))
                ? language.toLowerCase()
                : translationService.getDefaultLanguage();
        
        return translationService.getTranslationsByNamespace(namespace, lang)
                .map(translations -> TranslationResponse.builder()
                        .language(lang)
                        .translations(translations)
                        .build())
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    System.err.println("Error in TranslationController.getTranslationsByNamespace: " + error.getMessage());
                    error.printStackTrace();
                    return Mono.just(
                            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(TranslationResponse.builder()
                                            .language(lang)
                                            .translations(Map.of())
                                            .build()));
                });
    }

    /**
     * Retrieves list of supported languages.
     *
     * @return Mono containing list of supported language codes
     */
    @GetMapping(value = "/languages", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<String>>> getSupportedLanguages() {
        return Mono.just(ResponseEntity.ok(translationService.getSupportedLanguages()));
    }
}


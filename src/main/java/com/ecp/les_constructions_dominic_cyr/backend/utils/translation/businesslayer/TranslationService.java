package com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.dataaccesslayer.TranslationRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.AbstractMap;

/**
 * Service for handling translation operations.
 * Fetches translation JSON files from the file service and provides them to the frontend.
 */
@Service
public class TranslationService {

    private final WebClient webClient;
    private final TranslationRegistry registry;
    private final ObjectMapper objectMapper;
    private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList("en", "fr");
    private static final String DEFAULT_LANGUAGE = "en";

    public TranslationService(
            @Value("${files.service.base-url}") String fileServiceBaseUrl,
            TranslationRegistry registry) {
        this.registry = registry;
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
                .baseUrl(fileServiceBaseUrl) // e.g. http://localhost:8082 (dev) or http://files-service:8080 (docker)
                .build();
    }

    /**
     * Retrieves all translations for a given language.
     * Loads all available translation files for the language.
     *
     * @param language the language code (e.g., "en", "fr")
     * @return Mono containing a map of all translation keys and values organized by namespace
     */
    public Mono<Map<String, Object>> getAllTranslations(String language) {
        String lang = normalizeLanguage(language);
        
        // Get all available pages for this language from registry
        List<String> pages = Arrays.asList("home", "projects", "lots", "nav", "footer", "messages");
        
        // Fetch all translation files in parallel
        List<Mono<Map.Entry<String, Map<String, Object>>>> translationMonos = new ArrayList<>();
        
        for (String page : pages) {
            String fileId = registry.getFileId(lang, page);
            if (fileId != null) {
                Mono<Map.Entry<String, Map<String, Object>>> pageMono = fetchTranslationFile(fileId)
                        .map(translations -> {
                            AbstractMap.SimpleEntry<String, Map<String, Object>> entry = 
                                new AbstractMap.SimpleEntry<>(page, translations);
                            return (Map.Entry<String, Map<String, Object>>) entry;
                        })
                        .doOnError(error -> {
                            System.err.println("Error loading translations for page " + page + " (" + lang + "): " + error.getMessage());
                        });
                translationMonos.add(pageMono);
            }
        }
        
        if (translationMonos.isEmpty()) {
            System.out.println("No translation files found for language: " + lang);
            return Mono.just(new HashMap<String, Object>());
        }
        
        // Combine all translations using Flux.merge for better type safety
        // Handle errors at Flux level to avoid type issues
        return Flux.fromIterable(translationMonos)
                .flatMap(mono -> mono.onErrorResume(e -> {
                    // Log error and return empty - will be filtered out
                    return Mono.empty();
                }))
                .collectList()
                .map(entries -> {
                    Map<String, Object> allTranslations = new HashMap<String, Object>();
                    for (Map.Entry<String, Map<String, Object>> entry : entries) {
                        if (entry != null && entry.getValue() != null && !entry.getValue().isEmpty()) {
                            allTranslations.put(entry.getKey(), entry.getValue());
                        }
                    }
                    return allTranslations;
                })
        .doOnSuccess(translations -> {
            if (translations.isEmpty()) {
                System.out.println("No translations loaded for language: " + lang);
            } else {
                System.out.println("Loaded translations for " + lang + " with namespaces: " + translations.keySet());
            }
        })
        .onErrorResume(error -> {
            System.err.println("Error loading translations for language " + lang + ": " + error.getMessage());
            return Mono.just(new HashMap<String, Object>());
        });
    }

    /**
     * Retrieves translations for a specific page/namespace.
     *
     * @param pageName the page name (e.g., "home", "projects")
     * @param language the language code (e.g., "en", "fr")
     * @return Mono containing a map of translation keys and values for the page
     */
    public Mono<Map<String, Object>> getPageTranslations(String pageName, String language) {
        String lang = normalizeLanguage(language);
        String fileId = registry.getFileId(lang, pageName);
        
        if (fileId == null) {
            System.out.println("No translation file found for page: " + pageName + " in language: " + lang);
            return Mono.just(new HashMap<String, Object>());
        }
        
        return fetchTranslationFile(fileId)
                .onErrorResume(error -> {
                    System.err.println("Error loading page translations for " + pageName + " (" + language + "): " + error.getMessage());
                    return Mono.just(new HashMap<String, Object>());
                });
    }

    /**
     * Retrieves translations for a specific namespace.
     * Alias for getPageTranslations for consistency.
     *
     * @param namespace the namespace (e.g., "home", "messages")
     * @param language the language code (e.g., "en", "fr")
     * @return Mono containing a map of translation keys and values for the namespace
     */
    public Mono<Map<String, Object>> getTranslationsByNamespace(String namespace, String language) {
        return getPageTranslations(namespace, language);
    }

    /**
     * Retrieves a specific translation by key for a given language.
     * This searches across all loaded namespaces.
     *
     * @param key the translation key (e.g., "app.title" or "home.welcome")
     * @param language the language code
     * @return Mono containing the translated value
     */
    public Mono<String> getTranslation(String key, String language) {
        return getAllTranslations(language)
                .map(translations -> {
                    // Search for key in nested structure
                    String[] keyParts = key.split("\\.");
                    Object current = translations;
                    
                    for (String part : keyParts) {
                        if (current instanceof Map) {
                            current = ((Map<String, Object>) current).get(part);
                        } else {
                            return key; // Key not found, return key as fallback
                        }
                    }
                    
                    return current != null ? current.toString() : key;
                })
                .defaultIfEmpty(key);
    }

    /**
     * Retrieves multiple translations by keys for a given language.
     *
     * @param keys list of translation keys
     * @param language the language code
     * @return Mono containing a map of keys and their translated values
     */
    public Mono<Map<String, String>> getTranslations(List<String> keys, String language) {
        return getAllTranslations(language)
                .map(translations -> {
                    Map<String, String> result = new HashMap<>();
                    for (String key : keys) {
                        String[] keyParts = key.split("\\.");
                        Object current = translations;
                        
                        for (String part : keyParts) {
                            if (current instanceof Map) {
                                current = ((Map<String, Object>) current).get(part);
                            } else {
                                result.put(key, key);
                                break;
                            }
                        }
                        
                        if (current != null && !(current instanceof Map)) {
                            result.put(key, current.toString());
                        } else {
                            result.put(key, key);
                        }
                    }
                    return result;
                });
    }

    /**
     * Fetches a translation JSON file from the file service.
     *
     * @param fileId the file ID from the file service
     * @return Mono containing the parsed JSON as a Map
     */
    private Mono<Map<String, Object>> fetchTranslationFile(String fileId) {
        return webClient.get()
                .uri("/files/{id}", fileId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .map(json -> {
                    try {
                        TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
                        Map<String, Object> result = objectMapper.readValue(json, typeRef);
                        return result != null ? result : new HashMap<String, Object>();
                    } catch (Exception e) {
                        System.err.println("Error parsing translation JSON for file " + fileId + ": " + e.getMessage());
                        return new HashMap<String, Object>();
                    }
                })
                .onErrorResume(error -> {
                    System.err.println("Error fetching translation file " + fileId + ": " + error.getMessage());
                    return Mono.just((Map<String, Object>) new HashMap<String, Object>());
                });
    }

    /**
     * Validates if a language is supported.
     *
     * @param language the language code to validate
     * @return true if supported, false otherwise
     */
    public boolean isLanguageSupported(String language) {
        return language != null && SUPPORTED_LANGUAGES.contains(language.toLowerCase());
    }

    /**
     * Gets the default language.
     *
     * @return the default language code
     */
    public String getDefaultLanguage() {
        return DEFAULT_LANGUAGE;
    }

    /**
     * Gets all supported languages.
     *
     * @return list of supported language codes
     */
    public List<String> getSupportedLanguages() {
        return new ArrayList<>(SUPPORTED_LANGUAGES);
    }

    /**
     * Normalizes and validates a language code.
     *
     * @param language the language code
     * @return normalized language code, or default if invalid
     */
    private String normalizeLanguage(String language) {
        if (language == null || !isLanguageSupported(language)) {
            return DEFAULT_LANGUAGE;
        }
        return language.toLowerCase();
    }

}


package com.ecp.les_constructions_dominic_cyr.utils.Translation.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.TranslationService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.dataaccesslayer.TranslationRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranslationServiceUnitTest {

    @Mock
    private TranslationRegistry registry;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private TranslationService translationService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        translationService = new TranslationService("http://localhost:8082", registry);
        ReflectionTestUtils.setField(translationService, "webClient", webClient);
        ReflectionTestUtils.setField(translationService, "objectMapper", objectMapper);
    }

    @Test
    void isLanguageSupported_WithEnglish_ReturnsTrue() {
        assertTrue(translationService.isLanguageSupported("en"));
        assertTrue(translationService.isLanguageSupported("EN"));
        assertTrue(translationService.isLanguageSupported("En"));
    }

    @Test
    void isLanguageSupported_WithFrench_ReturnsTrue() {
        assertTrue(translationService.isLanguageSupported("fr"));
        assertTrue(translationService.isLanguageSupported("FR"));
        assertTrue(translationService.isLanguageSupported("Fr"));
    }

    @Test
    void isLanguageSupported_WithUnsupportedLanguage_ReturnsFalse() {
        assertFalse(translationService.isLanguageSupported("es"));
        assertFalse(translationService.isLanguageSupported("de"));
        assertFalse(translationService.isLanguageSupported("zh"));
    }

    @Test
    void isLanguageSupported_WithNull_ReturnsFalse() {
        assertFalse(translationService.isLanguageSupported(null));
    }

    @Test
    void getDefaultLanguage_ReturnsEnglish() {
        assertEquals("en", translationService.getDefaultLanguage());
    }

    @Test
    void getSupportedLanguages_ReturnsListOfLanguages() {
        List<String> languages = translationService.getSupportedLanguages();
        
        assertNotNull(languages);
        assertEquals(2, languages.size());
        assertTrue(languages.contains("en"));
        assertTrue(languages.contains("fr"));
    }

    @Test
    void getAllTranslations_WithNoRegisteredFiles_ReturnsEmptyMap() {
        when(registry.getFileId(anyString(), anyString())).thenReturn(null);
        
        StepVerifier.create(translationService.getAllTranslations("en"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertTrue(translations.isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void getAllTranslations_WithRegisteredFiles_ReturnsTranslations() throws Exception {
        String fileId = "file-id-123";
        Map<String, Object> translationData = new HashMap<>();
        translationData.put("title", "Welcome");
        translationData.put("description", "Description");
        
        String json = objectMapper.writeValueAsString(translationData);
        
        when(registry.getFileId("en", "home")).thenReturn(fileId);
        when(registry.getFileId("en", "projects")).thenReturn(null);
        when(registry.getFileId("en", "lots")).thenReturn(null);
        when(registry.getFileId("en", "nav")).thenReturn(null);
        when(registry.getFileId("en", "footer")).thenReturn(null);
        when(registry.getFileId("en", "messages")).thenReturn(null);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));
        
        StepVerifier.create(translationService.getAllTranslations("en"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertTrue(translations.containsKey("home"));
                    Map<String, Object> homeTranslations = (Map<String, Object>) translations.get("home");
                    assertEquals("Welcome", homeTranslations.get("title"));
                })
                .verifyComplete();
    }

    @Test
    void getAllTranslations_WithWebClientError_ReturnsEmptyMap() {
        when(registry.getFileId("en", "home")).thenReturn("file-id-123");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", "file-id-123")).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("Network error")));

        StepVerifier.create(translationService.getAllTranslations("en"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertTrue(translations.isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void getPageTranslations_WithValidPage_ReturnsTranslations() throws Exception {
        String fileId = "file-id-123";
        Map<String, Object> translationData = new HashMap<>();
        translationData.put("title", "Home Page");

        String json = objectMapper.writeValueAsString(translationData);

        when(registry.getFileId("en", "home")).thenReturn(fileId);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));

        StepVerifier.create(translationService.getPageTranslations("home", "en"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertEquals("Home Page", translations.get("title"));
                })
                .verifyComplete();
    }
    @Test
    void getPageTranslations_WithNoFileId_ReturnsEmptyMap() {
        when(registry.getFileId("en", "home")).thenReturn(null);
        
        StepVerifier.create(translationService.getPageTranslations("home", "en"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertTrue(translations.isEmpty());
                })
                .verifyComplete();
    }
    @Test
    void getPageTranslations_WithError_ReturnsEmptyMap() {
        when(registry.getFileId("en", "home")).thenReturn("file-id-123");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", "file-id-123")).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("Error")));
        
        StepVerifier.create(translationService.getPageTranslations("home", "en"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertTrue(translations.isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void getTranslationsByNamespace_DelegatesToGetPageTranslations() throws Exception {
        String fileId = "file-id-123";
        Map<String, Object> translationData = new HashMap<>();
        translationData.put("key", "value");
        
        String json = objectMapper.writeValueAsString(translationData);
        
        when(registry.getFileId("en", "home")).thenReturn(fileId);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));
        
        StepVerifier.create(translationService.getTranslationsByNamespace("home", "en"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertEquals("value", translations.get("key"));
                })
                .verifyComplete();
    }

    @Test
    void getTranslation_WithInvalidKey_ReturnsKey() {
        when(registry.getFileId(anyString(), anyString())).thenReturn(null);
        
        StepVerifier.create(translationService.getTranslation("invalid.key", "en"))
                .assertNext(value -> assertEquals("invalid.key", value))
                .verifyComplete();
    }


    @Test
    void getTranslations_WithEmptyKeys_ReturnsEmptyMap() {
        when(registry.getFileId(anyString(), anyString())).thenReturn(null);
        
        StepVerifier.create(translationService.getTranslations(Collections.emptyList(), "en"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertTrue(result.isEmpty());
                })
                .verifyComplete();
    }
    @Test
    void getAllTranslations_WithInvalidJson_ReturnsEmptyMap() {
        when(registry.getFileId("en", "home")).thenReturn("file-id-123");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", "file-id-123")).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("invalid json"));
        
        StepVerifier.create(translationService.getAllTranslations("en"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertTrue(translations.isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void getAllTranslations_WithNullJson_ReturnsEmptyMap() {
        when(registry.getFileId("en", "home")).thenReturn("file-id-123");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", "file-id-123")).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("null"));
        
        StepVerifier.create(translationService.getAllTranslations("en"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertTrue(translations.isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void getTranslation_WithNonMapValue_ReturnsKey() throws Exception {
        String fileId = "file-id-123";
        Map<String, Object> translationData = new HashMap<>();
        translationData.put("home", "not a map");
        
        String json = objectMapper.writeValueAsString(translationData);
        
        when(registry.getFileId("en", "home")).thenReturn(fileId);
        when(registry.getFileId("en", "projects")).thenReturn(null);
        when(registry.getFileId("en", "lots")).thenReturn(null);
        when(registry.getFileId("en", "nav")).thenReturn(null);
        when(registry.getFileId("en", "footer")).thenReturn(null);
        when(registry.getFileId("en", "messages")).thenReturn(null);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));
        
        StepVerifier.create(translationService.getTranslation("home.title", "en"))
                .assertNext(value -> assertEquals("home.title", value))
                .verifyComplete();
    }

    @Test
    void getTranslation_WithNullValue_ReturnsKey() throws Exception {
        String fileId = "file-id-123";
        Map<String, Object> translationData = new HashMap<>();
        Map<String, Object> homeData = new HashMap<>();
        homeData.put("title", null);
        translationData.put("home", homeData);
        
        String json = objectMapper.writeValueAsString(translationData);
        
        when(registry.getFileId("en", "home")).thenReturn(fileId);
        when(registry.getFileId("en", "projects")).thenReturn(null);
        when(registry.getFileId("en", "lots")).thenReturn(null);
        when(registry.getFileId("en", "nav")).thenReturn(null);
        when(registry.getFileId("en", "footer")).thenReturn(null);
        when(registry.getFileId("en", "messages")).thenReturn(null);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));
        
        StepVerifier.create(translationService.getTranslation("home.title", "en"))
                .assertNext(value -> assertEquals("home.title", value))
                .verifyComplete();
    }

    @Test
    void getTranslations_WithMapValue_ReturnsKey() throws Exception {
        String fileId = "file-id-123";
        Map<String, Object> translationData = new HashMap<>();
        Map<String, Object> homeData = new HashMap<>();
        Map<String, Object> nestedData = new HashMap<>();
        nestedData.put("nested", "value");
        homeData.put("title", nestedData);
        translationData.put("home", homeData);
        
        String json = objectMapper.writeValueAsString(translationData);
        
        when(registry.getFileId("en", "home")).thenReturn(fileId);
        when(registry.getFileId("en", "projects")).thenReturn(null);
        when(registry.getFileId("en", "lots")).thenReturn(null);
        when(registry.getFileId("en", "nav")).thenReturn(null);
        when(registry.getFileId("en", "footer")).thenReturn(null);
        when(registry.getFileId("en", "messages")).thenReturn(null);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));
        
        List<String> keys = Arrays.asList("home.title");
        
        StepVerifier.create(translationService.getTranslations(keys, "en"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals("home.title", result.get("home.title"));
                })
                .verifyComplete();
    }

    @Test
    void getAllTranslations_WithMultiplePages_ReturnsAllTranslations() throws Exception {
        String fileId1 = "file-id-123";
        String fileId2 = "file-id-456";
        Map<String, Object> homeData = new HashMap<>();
        homeData.put("title", "Home");
        Map<String, Object> projectsData = new HashMap<>();
        projectsData.put("title", "Projects");
        
        String json1 = objectMapper.writeValueAsString(homeData);
        String json2 = objectMapper.writeValueAsString(projectsData);
        
        when(registry.getFileId("en", "home")).thenReturn(fileId1);
        when(registry.getFileId("en", "projects")).thenReturn(fileId2);
        when(registry.getFileId("en", "lots")).thenReturn(null);
        when(registry.getFileId("en", "nav")).thenReturn(null);
        when(registry.getFileId("en", "footer")).thenReturn(null);
        when(registry.getFileId("en", "messages")).thenReturn(null);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", fileId1)).thenReturn(requestBodySpec);
        when(requestHeadersUriSpec.uri("/files/{id}", fileId2)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(json1))
                .thenReturn(Mono.just(json2));
        
        StepVerifier.create(translationService.getAllTranslations("en"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertTrue(translations.containsKey("home"));
                    assertTrue(translations.containsKey("projects"));
                })
                .verifyComplete();
    }

    @Test
    void getAllTranslations_WithEmptyTranslationMap_ExcludesFromResult() throws Exception {
        String fileId = "file-id-123";
        Map<String, Object> emptyData = new HashMap<>();
        String json = objectMapper.writeValueAsString(emptyData);
        
        when(registry.getFileId("en", "home")).thenReturn(fileId);
        when(registry.getFileId("en", "projects")).thenReturn(null);
        when(registry.getFileId("en", "lots")).thenReturn(null);
        when(registry.getFileId("en", "nav")).thenReturn(null);
        when(registry.getFileId("en", "footer")).thenReturn(null);
        when(registry.getFileId("en", "messages")).thenReturn(null);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));
        
        StepVerifier.create(translationService.getAllTranslations("en"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertTrue(translations.isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void fetchTranslationFile_WithParseError_ReturnsEmptyMap() {
        when(registry.getFileId("en", "home")).thenReturn("file-id-123");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", "file-id-123")).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("invalid json"));
        
        StepVerifier.create(translationService.getPageTranslations("home", "en"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertTrue(translations.isEmpty());
                })
                .verifyComplete();
    }
}


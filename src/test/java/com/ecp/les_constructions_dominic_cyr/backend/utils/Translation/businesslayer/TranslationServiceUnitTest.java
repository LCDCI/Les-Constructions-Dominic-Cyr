package com.ecp.les_constructions_dominic_cyr.backend.utils.Translation.businesslayer;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranslationServiceUnitTest {

    @Mock
    private TranslationRegistry registry;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

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
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
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
    void getAllTranslations_WithInvalidLanguage_DefaultsToEnglish() {
        when(registry.getFileId(eq("en"), anyString())).thenReturn(null);
        
        StepVerifier.create(translationService.getAllTranslations("invalid"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertTrue(translations.isEmpty());
                })
                .verifyComplete();
        
        verify(registry, atLeastOnce()).getFileId(eq("en"), anyString());
    }

    @Test
    void getAllTranslations_WithNullLanguage_DefaultsToEnglish() {
        when(registry.getFileId(eq("en"), anyString())).thenReturn(null);

        StepVerifier.create(translationService.getAllTranslations(null))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertTrue(translations.isEmpty());
                })
                .verifyComplete();

        verify(registry, atLeastOnce()).getFileId(eq("en"), anyString());
    }

    @Test
    void getAllTranslations_WithWebClientError_ReturnsEmptyMap() {
        when(registry.getFileId("en", "home")).thenReturn("file-id-123");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", "file-id-123")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
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
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
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
        when(requestHeadersUriSpec.uri("/files/{id}", "file-id-123")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
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
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));
        
        StepVerifier.create(translationService.getTranslationsByNamespace("home", "en"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertEquals("value", translations.get("key"));
                })
                .verifyComplete();
    }

    @Test
    void getTranslation_WithValidKey_ReturnsValue() throws Exception {
        String fileId = "file-id-123";
        Map<String, Object> translationData = new HashMap<>();
        translationData.put("title", "Welcome");
        
        String json = objectMapper.writeValueAsString(translationData);
        
        when(registry.getFileId("en", "home")).thenReturn(fileId);
        when(registry.getFileId("en", "projects")).thenReturn(null);
        when(registry.getFileId("en", "lots")).thenReturn(null);
        when(registry.getFileId("en", "nav")).thenReturn(null);
        when(registry.getFileId("en", "footer")).thenReturn(null);
        when(registry.getFileId("en", "messages")).thenReturn(null);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));
        
        StepVerifier.create(translationService.getTranslation("home.title", "en"))
                .assertNext(value -> assertEquals("Welcome", value))
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
    void getTranslation_WithNullKey_ThrowsNullPointerException() {
        when(registry.getFileId(eq("en"), anyString())).thenReturn(null);
        
        StepVerifier.create(translationService.getTranslation(null, "en"))
                .expectError(NullPointerException.class)
                .verify();
    }

    @Test
    void getTranslations_WithMultipleKeys_ReturnsMap() throws Exception {
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
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));
        
        List<String> keys = Arrays.asList("home.title", "home.description", "invalid.key");
        
        StepVerifier.create(translationService.getTranslations(keys, "en"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals("Welcome", result.get("home.title"));
                    assertEquals("Description", result.get("home.description"));
                    assertEquals("invalid.key", result.get("invalid.key"));
                })
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
        when(requestHeadersUriSpec.uri("/files/{id}", "file-id-123")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
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
        when(requestHeadersUriSpec.uri("/files/{id}", "file-id-123")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("null"));
        
        StepVerifier.create(translationService.getAllTranslations("en"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertTrue(translations.isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void getAllTranslations_WithMultiplePages_SomeSucceedSomeFail() throws Exception {
        String homeFileId = "file-id-home";
        String projectsFileId = "file-id-projects";
        
        Map<String, Object> homeData = new HashMap<>();
        homeData.put("title", "Home");
        String homeJson = objectMapper.writeValueAsString(homeData);
        
        when(registry.getFileId("en", "home")).thenReturn(homeFileId);
        when(registry.getFileId("en", "projects")).thenReturn(projectsFileId);
        when(registry.getFileId("en", "lots")).thenReturn(null);
        when(registry.getFileId("en", "nav")).thenReturn(null);
        when(registry.getFileId("en", "footer")).thenReturn(null);
        when(registry.getFileId("en", "messages")).thenReturn(null);
        
        WebClient.RequestHeadersSpec homeRequestSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.RequestHeadersSpec projectsRequestSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec homeResponseSpec = mock(WebClient.ResponseSpec.class);
        WebClient.ResponseSpec projectsResponseSpec = mock(WebClient.ResponseSpec.class);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", homeFileId)).thenReturn(homeRequestSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", projectsFileId)).thenReturn(projectsRequestSpec);
        when(homeRequestSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(homeRequestSpec);
        when(projectsRequestSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(projectsRequestSpec);
        when(homeRequestSpec.retrieve()).thenReturn(homeResponseSpec);
        when(projectsRequestSpec.retrieve()).thenReturn(projectsResponseSpec);
        when(homeResponseSpec.bodyToMono(String.class)).thenReturn(Mono.just(homeJson));
        when(projectsResponseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("Error")));
        
        StepVerifier.create(translationService.getAllTranslations("en"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertTrue(translations.containsKey("home"));
                    assertFalse(translations.containsKey("projects"));
                })
                .verifyComplete();
    }

    @Test
    void getAllTranslations_WithFrenchLanguage_ReturnsTranslations() throws Exception {
        String fileId = "file-id-fr";
        Map<String, Object> translationData = new HashMap<>();
        translationData.put("title", "Bienvenue");
        
        String json = objectMapper.writeValueAsString(translationData);
        
        when(registry.getFileId("fr", "home")).thenReturn(fileId);
        when(registry.getFileId("fr", "projects")).thenReturn(null);
        when(registry.getFileId("fr", "lots")).thenReturn(null);
        when(registry.getFileId("fr", "nav")).thenReturn(null);
        when(registry.getFileId("fr", "footer")).thenReturn(null);
        when(registry.getFileId("fr", "messages")).thenReturn(null);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));
        
        StepVerifier.create(translationService.getAllTranslations("fr"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertTrue(translations.containsKey("home"));
                    Map<String, Object> homeTranslations = (Map<String, Object>) translations.get("home");
                    assertEquals("Bienvenue", homeTranslations.get("title"));
                })
                .verifyComplete();
    }

    @Test
    void getAllTranslations_WithEmptyMapEntries_FiltersThemOut() throws Exception {
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
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));
        
        StepVerifier.create(translationService.getAllTranslations("en"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertTrue(translations.isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void getPageTranslations_WithInvalidLanguage_DefaultsToEnglish() throws Exception {
        String fileId = "file-id-123";
        Map<String, Object> translationData = new HashMap<>();
        translationData.put("title", "Home Page");
        String json = objectMapper.writeValueAsString(translationData);
        
        when(registry.getFileId("en", "home")).thenReturn(fileId);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));
        
        StepVerifier.create(translationService.getPageTranslations("home", "invalid"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertEquals("Home Page", translations.get("title"));
                })
                .verifyComplete();
        
        verify(registry).getFileId("en", "home");
    }

    @Test
    void getPageTranslations_WithNullLanguage_DefaultsToEnglish() throws Exception {
        String fileId = "file-id-123";
        Map<String, Object> translationData = new HashMap<>();
        translationData.put("title", "Home Page");
        String json = objectMapper.writeValueAsString(translationData);
        
        when(registry.getFileId("en", "home")).thenReturn(fileId);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));
        
        StepVerifier.create(translationService.getPageTranslations("home", null))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertEquals("Home Page", translations.get("title"));
                })
                .verifyComplete();
        
        verify(registry).getFileId("en", "home");
    }

    @Test
    void getTranslation_WithDeeplyNestedKey_ReturnsValue() throws Exception {
        String fileId = "file-id-123";
        Map<String, Object> translationData = new HashMap<>();
        Map<String, Object> nestedData = new HashMap<>();
        nestedData.put("deep", "Nested Value");
        translationData.put("nested", nestedData);
        
        String json = objectMapper.writeValueAsString(translationData);
        
        when(registry.getFileId("en", "home")).thenReturn(fileId);
        when(registry.getFileId("en", "projects")).thenReturn(null);
        when(registry.getFileId("en", "lots")).thenReturn(null);
        when(registry.getFileId("en", "nav")).thenReturn(null);
        when(registry.getFileId("en", "footer")).thenReturn(null);
        when(registry.getFileId("en", "messages")).thenReturn(null);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));
        
        StepVerifier.create(translationService.getTranslation("home.nested.deep", "en"))
                .assertNext(value -> assertEquals("Nested Value", value))
                .verifyComplete();
    }

    @Test
    void getTranslation_WithKeyPointingToMap_ReturnsMapToString() throws Exception {
        String fileId = "file-id-123";
        Map<String, Object> translationData = new HashMap<>();
        Map<String, Object> nestedData = new HashMap<>();
        nestedData.put("deep", "Nested Value");
        translationData.put("nested", nestedData);
        
        String json = objectMapper.writeValueAsString(translationData);
        
        when(registry.getFileId("en", "home")).thenReturn(fileId);
        when(registry.getFileId("en", "projects")).thenReturn(null);
        when(registry.getFileId("en", "lots")).thenReturn(null);
        when(registry.getFileId("en", "nav")).thenReturn(null);
        when(registry.getFileId("en", "footer")).thenReturn(null);
        when(registry.getFileId("en", "messages")).thenReturn(null);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));
        
        StepVerifier.create(translationService.getTranslation("home.nested", "en"))
                .assertNext(value -> {
                    // When key points to a Map, it returns the Map's toString() representation
                    assertTrue(value.contains("deep") && value.contains("Nested Value"));
                })
                .verifyComplete();
    }

    @Test
    void getTranslation_WithNullValue_ReturnsKey() throws Exception {
        String fileId = "file-id-123";
        Map<String, Object> translationData = new HashMap<>();
        translationData.put("title", null);
        
        String json = objectMapper.writeValueAsString(translationData);
        
        when(registry.getFileId("en", "home")).thenReturn(fileId);
        when(registry.getFileId("en", "projects")).thenReturn(null);
        when(registry.getFileId("en", "lots")).thenReturn(null);
        when(registry.getFileId("en", "nav")).thenReturn(null);
        when(registry.getFileId("en", "footer")).thenReturn(null);
        when(registry.getFileId("en", "messages")).thenReturn(null);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));
        
        StepVerifier.create(translationService.getTranslation("home.title", "en"))
                .assertNext(value -> assertEquals("home.title", value))
                .verifyComplete();
    }

    @Test
    void getTranslations_WithNullKeysList_HandlesGracefully() {
        when(registry.getFileId(eq("en"), anyString())).thenReturn(null);
        
        StepVerifier.create(translationService.getTranslations(null, "en"))
                .expectError(NullPointerException.class)
                .verify();
    }

    @Test
    void getTranslations_WithNestedMapValue_ReturnsKey() throws Exception {
        String fileId = "file-id-123";
        Map<String, Object> translationData = new HashMap<>();
        Map<String, Object> nestedData = new HashMap<>();
        nestedData.put("deep", "Value");
        translationData.put("nested", nestedData);
        
        String json = objectMapper.writeValueAsString(translationData);
        
        when(registry.getFileId("en", "home")).thenReturn(fileId);
        when(registry.getFileId("en", "projects")).thenReturn(null);
        when(registry.getFileId("en", "lots")).thenReturn(null);
        when(registry.getFileId("en", "nav")).thenReturn(null);
        when(registry.getFileId("en", "footer")).thenReturn(null);
        when(registry.getFileId("en", "messages")).thenReturn(null);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));
        
        List<String> keys = Arrays.asList("home.nested");
        
        StepVerifier.create(translationService.getTranslations(keys, "en"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals("home.nested", result.get("home.nested"));
                })
                .verifyComplete();
    }

    @Test
    void getTranslations_WithFrenchLanguage_ReturnsTranslations() throws Exception {
        String fileId = "file-id-fr";
        Map<String, Object> translationData = new HashMap<>();
        translationData.put("title", "Titre");
        
        String json = objectMapper.writeValueAsString(translationData);
        
        when(registry.getFileId("fr", "home")).thenReturn(fileId);
        when(registry.getFileId("fr", "projects")).thenReturn(null);
        when(registry.getFileId("fr", "lots")).thenReturn(null);
        when(registry.getFileId("fr", "nav")).thenReturn(null);
        when(registry.getFileId("fr", "footer")).thenReturn(null);
        when(registry.getFileId("fr", "messages")).thenReturn(null);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));
        
        List<String> keys = Arrays.asList("home.title");
        
        StepVerifier.create(translationService.getTranslations(keys, "fr"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals("Titre", result.get("home.title"));
                })
                .verifyComplete();
    }

    @Test
    void getTranslations_WithKeyNotFoundInMiddle_ReturnsKey() throws Exception {
        String fileId = "file-id-123";
        Map<String, Object> translationData = new HashMap<>();
        translationData.put("title", "Welcome");
        
        String json = objectMapper.writeValueAsString(translationData);
        
        when(registry.getFileId("en", "home")).thenReturn(fileId);
        when(registry.getFileId("en", "projects")).thenReturn(null);
        when(registry.getFileId("en", "lots")).thenReturn(null);
        when(registry.getFileId("en", "nav")).thenReturn(null);
        when(registry.getFileId("en", "footer")).thenReturn(null);
        when(registry.getFileId("en", "messages")).thenReturn(null);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", fileId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(json));
        
        List<String> keys = Arrays.asList("home.nonexistent.key");
        
        StepVerifier.create(translationService.getTranslations(keys, "en"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals("home.nonexistent.key", result.get("home.nonexistent.key"));
                })
                .verifyComplete();
    }

    @Test
    void getTranslation_WithEmptyStringKey_ReturnsKey() {
        when(registry.getFileId(eq("en"), anyString())).thenReturn(null);
        
        StepVerifier.create(translationService.getTranslation("", "en"))
                .assertNext(value -> assertEquals("", value))
                .verifyComplete();
    }

    @Test
    void getAllTranslations_WithAllPagesRegistered_ReturnsAllTranslations() throws Exception {
        String homeFileId = "file-id-home";
        String projectsFileId = "file-id-projects";
        
        Map<String, Object> homeData = new HashMap<>();
        homeData.put("title", "Home");
        String homeJson = objectMapper.writeValueAsString(homeData);
        
        Map<String, Object> projectsData = new HashMap<>();
        projectsData.put("title", "Projects");
        String projectsJson = objectMapper.writeValueAsString(projectsData);
        
        when(registry.getFileId("en", "home")).thenReturn(homeFileId);
        when(registry.getFileId("en", "projects")).thenReturn(projectsFileId);
        when(registry.getFileId("en", "lots")).thenReturn(null);
        when(registry.getFileId("en", "nav")).thenReturn(null);
        when(registry.getFileId("en", "footer")).thenReturn(null);
        when(registry.getFileId("en", "messages")).thenReturn(null);
        
        WebClient.RequestHeadersSpec homeRequestSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.RequestHeadersSpec projectsRequestSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec homeResponseSpec = mock(WebClient.ResponseSpec.class);
        WebClient.ResponseSpec projectsResponseSpec = mock(WebClient.ResponseSpec.class);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", homeFileId)).thenReturn(homeRequestSpec);
        when(requestHeadersUriSpec.uri("/files/{id}", projectsFileId)).thenReturn(projectsRequestSpec);
        when(homeRequestSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(homeRequestSpec);
        when(projectsRequestSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(projectsRequestSpec);
        when(homeRequestSpec.retrieve()).thenReturn(homeResponseSpec);
        when(projectsRequestSpec.retrieve()).thenReturn(projectsResponseSpec);
        when(homeResponseSpec.bodyToMono(String.class)).thenReturn(Mono.just(homeJson));
        when(projectsResponseSpec.bodyToMono(String.class)).thenReturn(Mono.just(projectsJson));
        
        StepVerifier.create(translationService.getAllTranslations("en"))
                .assertNext(translations -> {
                    assertNotNull(translations);
                    assertTrue(translations.containsKey("home"));
                    assertTrue(translations.containsKey("projects"));
                    Map<String, Object> homeTranslations = (Map<String, Object>) translations.get("home");
                    Map<String, Object> projectsTranslations = (Map<String, Object>) translations.get("projects");
                    assertEquals("Home", homeTranslations.get("title"));
                    assertEquals("Projects", projectsTranslations.get("title"));
                })
                .verifyComplete();
    }
}


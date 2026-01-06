package com.ecp.les_constructions_dominic_cyr.backend.utils;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.TranslationService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.dataaccesslayer.TranslationRegistry;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.*;

import static org.mockito.Mockito.*;

public class TranslationServiceTest {

    @Mock
    private TranslationRegistry registry;

    @Mock(answer = Answers.RETURNS_SELF)
    private WebClient.Builder builder;

    @Mock
    private WebClient webClient;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersUriSpec uriSpec;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersSpec headersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Setup the WebClient chain
        when(builder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
    }

    private TranslationService createService() {
        return new TranslationService("http://localhost:8080", registry);
    }

    @Test
    void testGetAllTranslations_noFiles() {
        TranslationService service = createService();
        when(registry.getFileId(anyString(), anyString())).thenReturn(null);

        StepVerifier.create(service.getAllTranslations("fr"))
                .expectNextMatches(Map::isEmpty)
                .verifyComplete();
    }

    @Test
    void testGetPageTranslations_noFile() {
        TranslationService service = createService();
        when(registry.getFileId(anyString(), anyString())).thenReturn(null);

        StepVerifier.create(service.getPageTranslations("unknown", "en"))
                .expectNext(Collections.emptyMap())
                .verifyComplete();
    }

    @Test
    void testGetTranslation_notFound() {
        TranslationService service = createService();
        when(registry.getFileId(anyString(), anyString())).thenReturn("id100");

        // Mock a 404 or empty response from the translation server
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.getTranslation("home.xyz", "en"))
                .expectNext("home.xyz") // Verifies fallback logic
                .verifyComplete();
    }

    @Test
    void testLanguageSupport() {
        TranslationService service = createService();
        Assertions.assertTrue(service.isLanguageSupported("en"));
        Assertions.assertFalse(service.isLanguageSupported("es"));
        Assertions.assertEquals("en", service.getDefaultLanguage());
    }
}
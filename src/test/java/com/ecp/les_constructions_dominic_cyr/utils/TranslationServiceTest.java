package com.ecp.les_constructions_dominic_cyr.utils;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.TranslationService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.dataaccesslayer.TranslationRegistry;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.*;

import static org.mockito.Mockito.*;

public class TranslationServiceTest {

    @Mock
    private TranslationRegistry registry;

    @Mock
    private WebClient.Builder builder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec<?> uriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> headersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(webClient);
    }

    private TranslationService createService() {
        return new TranslationService("http://mock-service", registry);
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
        when(registry.getFileId("en", "unknown")).thenReturn(null);

        StepVerifier.create(service.getPageTranslations("unknown", "en"))
                .expectNext(Collections.emptyMap())
                .verifyComplete();
    }



    @Test
    void testGetTranslation_notFound() {
        TranslationService service = createService();

        when(registry.getFileId(anyString(), anyString())).thenReturn("id100");

        StepVerifier.create(service.getTranslation("home.xyz", "en"))
                .expectNext("home.xyz") // fallback
                .verifyComplete();
    }



    @Test
    void testLanguageSupport() {
        TranslationService service = createService();
        Assertions.assertTrue(service.isLanguageSupported("en"));
        Assertions.assertFalse(service.isLanguageSupported("es"));
        Assertions.assertEquals("en", service.getDefaultLanguage());
        Assertions.assertEquals(2, service.getSupportedLanguages().size());
    }


}


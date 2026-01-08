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

    @Mock
    private WebClient.RequestHeadersUriSpec uriSpec;

    @Mock
    private WebClient.RequestHeadersSpec headersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(builder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        // Default mock response to prevent "Connection Refused"
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.empty());
    }

    private TranslationService createService() {
        return new TranslationService("http://localhost:8080", registry);
    }

    @Test
    void testGetTranslation_notFound_returnsFallback() {
        TranslationService service = createService();
        when(registry.getFileId(anyString(), anyString())).thenReturn("id100");
        // Ensure Mono.empty() is returned so fallback logic is used
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.getTranslation("home.xyz", "en"))
                .expectNext("home.xyz")
                .verifyComplete();
    }

    @Test
    void testLanguageSupport() {
        TranslationService service = createService();
        Assertions.assertTrue(service.isLanguageSupported("en"));
        Assertions.assertFalse(service.isLanguageSupported("es"));
    }
}
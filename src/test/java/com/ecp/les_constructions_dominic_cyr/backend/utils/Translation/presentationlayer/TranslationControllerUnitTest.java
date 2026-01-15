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
        // Setup default mocks
        when(translationService.getDefaultLanguage()).thenReturn("en");
        when(translationService.isLanguageSupported(anyString())).thenReturn(true);
    }

    @Test
    void getAllTranslations_WithInvalidLanguage_DefaultsToEnglish() {
        Map<String, Object> translations = new HashMap<>();
        when(translationService.isLanguageSupported("invalid")).thenReturn(false);
        when(translationService.getAllTranslations("en")).thenReturn(Mono.just(translations));
        
        StepVerifier.create(translationController.getAllTranslations("invalid"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("en", response.getBody().getLanguage());
                })
                .verifyComplete();
    }

    @Test
    void getDefaultTranslations_CallsGetAllTranslationsWithDefault() {
        Map<String, Object> translations = new HashMap<>();
        when(translationService.getAllTranslations("en")).thenReturn(Mono.just(translations));
        
        StepVerifier.create(translationController.getDefaultTranslations())
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("en", response.getBody().getLanguage());
                })
                .verifyComplete();
        
        verify(translationService, times(1)).getDefaultLanguage();
    }

}


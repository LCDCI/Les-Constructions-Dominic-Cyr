package com.ecp.les_constructions_dominic_cyr.utils;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.dataaccesslayer.TranslationRegistry;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.presentationlayer.TranslationRegistryController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranslationRegistryControllerTest {

    private WebTestClient webTestClient;

    @Mock
    private TranslationRegistry registry;

    @InjectMocks
    private TranslationRegistryController registryController;

    private static final String LANG = "en";
    private static final String PAGE = "home";
    private static final String FILE_ID = "1a2b3c4d5e";
    private static final String BASE_URI = "/api/v1/translations/registry";

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToController(registryController).build();
    }

    @Test
    void registerFileId_Success() {
        webTestClient.post().uri(BASE_URI + "/{language}/{page}", LANG, PAGE)
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue(FILE_ID + " ") // Simulate body with extra space
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("File ID registered successfully for " + LANG + "." + PAGE);

        // Verify the fileId was trimmed before registering
        verify(registry).registerFileId(eq(LANG), eq(PAGE), eq(FILE_ID));
    }

    @Test
    void getFileId_Found_ReturnsFileId() {
        when(registry.getFileId(eq(LANG), eq(PAGE))).thenReturn(FILE_ID);

        webTestClient.get().uri(BASE_URI + "/{language}/{page}", LANG, PAGE)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(FILE_ID);
    }

    @Test
    void getFileId_NotFound_Returns404() {
        when(registry.getFileId(eq(LANG), eq(PAGE))).thenReturn(null);

        webTestClient.get().uri(BASE_URI + "/{language}/{page}", LANG, PAGE)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();
    }
}
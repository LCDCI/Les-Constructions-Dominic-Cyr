package com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;


@Service
public class DeepLService {

    private final WebClient webClient;
    private final String apiKey;
    private final String apiUrl;
    private static final String DEEPL_FREE_API = "https://api-free.deepl.com/v2";
    private static final String DEEPL_PAID_API = "https://api.deepl.com/v2";
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(2);
    private static final Duration MAX_WAIT_TIME = Duration.ofMinutes(5);

    public DeepLService(
            @Value("${deepl.api.key:}") String apiKey,
            @Value("${deepl.api.url:}") String apiUrl,
            @Value("${deepl.api.free:true}") boolean useFreeApi) {
        this.apiKey = apiKey;
        this.apiUrl = (apiUrl != null && !apiUrl.isEmpty()) 
            ? apiUrl 
            : (useFreeApi ? DEEPL_FREE_API : DEEPL_PAID_API);
        
        this.webClient = WebClient.builder()
                .baseUrl(this.apiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "DeepL-Auth-Key " + this.apiKey)
                .build();
    }

    /**
     * Translates a PDF document from one language to another.
     * 
     * @param pdfData the PDF file data as a byte array
     * @param sourceLanguage the source language code (e.g., "EN", "FR")
     * @param targetLanguage the target language code (e.g., "EN", "FR")
     * @return Mono containing the translated PDF as a byte array
     */
    public Mono<byte[]> translatePdf(byte[] pdfData, String sourceLanguage, String targetLanguage) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.error(new IllegalStateException("DeepL API key is not configured. Please set deepl.api.key in application.properties"));
        }

        // Step 1: Upload document and get document ID and key
        return uploadDocument(pdfData, sourceLanguage, targetLanguage)
                .flatMap(response -> {
                    String documentId = response.get("document_id");
                    String documentKey = response.get("document_key");
                    
                    if (documentId == null || documentKey == null) {
                        return Mono.error(new IllegalStateException("Failed to upload document to DeepL"));
                    }
                    
                    // Step 2: Poll for translation status
                    return pollTranslationStatus(documentId, documentKey)
                            .flatMap(status -> {
                                if ("done".equals(status)) {
                                    // Step 3: Download translated document
                                    return downloadTranslatedDocument(documentId, documentKey);
                                } else if ("error".equals(status)) {
                                    return Mono.error(new IllegalStateException("Translation failed on DeepL side"));
                                } else {
                                    return Mono.error(new IllegalStateException("Translation timed out or failed"));
                                }
                            });
                });
    }


    private Mono<Map<String, String>> uploadDocument(byte[] pdfData, String sourceLanguage, String targetLanguage) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", pdfData)
                .contentType(MediaType.APPLICATION_PDF)
                .filename("document.pdf");
        builder.part("target_lang", targetLanguage.toUpperCase());
        if (sourceLanguage != null && !sourceLanguage.isEmpty()) {
            builder.part("source_lang", sourceLanguage.toUpperCase());
        }

        return webClient.post()
                .uri("/document")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(response -> {
                    // DeepL returns document_id and document_key
                    Map<String, String> result = new java.util.HashMap<>();
                    result.put("document_id", (String) response.get("document_id"));
                    result.put("document_key", (String) response.get("document_key"));
                    return result;
                });
    }

    /**
     * Polls the translation status until it's done or an error occurs.
     */
    private Mono<String> pollTranslationStatus(String documentId, String documentKey) {
        return checkTranslationStatus(documentId, documentKey)
                .expand(status -> {
                    if ("done".equals(status) || "error".equals(status)) {
                        return Mono.empty();
                    }
                    return checkTranslationStatus(documentId, documentKey);
                })
                .take(Duration.ofSeconds(MAX_WAIT_TIME.getSeconds()))
                .last()
                .timeout(MAX_WAIT_TIME)
                .onErrorReturn("error");
    }

    /**
     * Checks the current translation status.
     */
    private Mono<String> checkTranslationStatus(String documentId, String documentKey) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/document/{documentId}")
                        .queryParam("document_key", documentKey)
                        .build(documentId))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    String status = (String) response.get("status");
                    return status != null ? status : "unknown";
                })
                .defaultIfEmpty("unknown")
                .delayElement(POLL_INTERVAL);
    }

    /**
     * Downloads the translated document.
     */
    private Mono<byte[]> downloadTranslatedDocument(String documentId, String documentKey) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/document/{documentId}/result")
                        .queryParam("document_key", documentKey)
                        .build(documentId))
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .collectList()
                .map(dataBuffers -> {
                    int totalSize = dataBuffers.stream()
                            .mapToInt(DataBuffer::readableByteCount)
                            .sum();
                    byte[] result = new byte[totalSize];
                    int offset = 0;
                    for (DataBuffer buffer : dataBuffers) {
                        int length = buffer.readableByteCount();
                        buffer.read(result, offset, length);
                        DataBufferUtils.release(buffer);
                        offset += length;
                    }
                    return result;
                });
    }

    /**
     * Converts language code from ISO 639-1 (en, fr) to DeepL format (EN, FR).
     */
    public String toDeepLLanguageCode(String languageCode) {
        if (languageCode == null) {
            return null;
        }
        return languageCode.toUpperCase();
    }

    /**
     * Determines the opposite language for translation.
     */
    public String getOppositeLanguage(String languageCode) {
        if (languageCode == null) {
            return "EN";
        }
        String upper = languageCode.toUpperCase();
        return "EN".equals(upper) || "FR".equals(upper) 
            ? ("EN".equals(upper) ? "FR" : "EN")
            : upper;
    }


    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isEmpty() && !apiKey.trim().isEmpty();
    }


    public String getApiUrl() {
        return apiUrl;
    }
}


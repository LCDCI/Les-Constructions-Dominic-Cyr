package com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Service for uploading translated PDF files to the file service.
 */
@Service
public class FileServiceUploader {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final DataBufferFactory dataBufferFactory;
    private static final String PDF_TRANSLATIONS_PROJECT_ID = "pdf-translations";

    public FileServiceUploader(
            @Value("${files.service.base-url}") String baseUrl,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.dataBufferFactory = new DefaultDataBufferFactory();
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Uploads a translated PDF to the file service.
     * 
     * @param pdfBytes the PDF file as byte array
     * @param filename the filename for the PDF
     * @param uploadedBy who uploaded the file (e.g., "system", "user-id")
     * @return Mono containing the file ID from the file service
     */
    public Mono<String> uploadTranslatedPdf(byte[] pdfBytes, String filename, String uploadedBy) {
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        
        // Create DataBuffer from byte array
        DataBuffer dataBuffer = dataBufferFactory.wrap(pdfBytes);
        
        body.part("file", dataBuffer)
                .filename(filename)
                .contentType(MediaType.APPLICATION_PDF);
        
        body.part("category", "DOCUMENT");
        body.part("projectId", PDF_TRANSLATIONS_PROJECT_ID);
        body.part("uploadedBy", uploadedBy != null ? uploadedBy : "system");

        return webClient.post()
                .uri("/files")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body.build()))
                .retrieve()
                .bodyToMono(String.class)
                .map(responseJson -> {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(responseJson);
                        String fileId = jsonNode.get("fileId").asText();
                        System.out.println("Uploaded translated PDF to file service. File ID: " + fileId);
                        return fileId;
                    } catch (Exception e) {
                        System.err.println("Error parsing file service response: " + e.getMessage());
                        throw new RuntimeException("Failed to parse file service response", e);
                    }
                })
                .onErrorResume(error -> {
                    System.err.println("Error uploading PDF to file service: " + error.getMessage());
                    error.printStackTrace();
                    return Mono.error(new RuntimeException("Failed to upload PDF to file service", error));
                });
    }
}


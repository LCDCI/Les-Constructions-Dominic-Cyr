package com.ecp.les_constructions_dominic_cyr.backend.Files;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@Service
public class GlobalPhotoService {

    private final WebClient webClient;

    public GlobalPhotoService(@Value("${files.service.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl) // e.g. http://localhost:8082 (dev) or http://files-service:8080 (docker)
                .build();
    }

    /**
     * Uploads a file to the files-service. Returns the files-service response body as String (JSON).
     */
    public Mono<String> uploadGlobalPhoto(MultipartFile file, String uploadedBy) {
        MultipartBodyBuilder body = new MultipartBodyBuilder();

        MediaType contentType = MediaType.IMAGE_JPEG;
        if (file.getContentType() != null) {
            try {
                contentType = MediaType.parseMediaType(file.getContentType());
            } catch (Exception ignored) {}
        }

        body.part("file", file.getResource())
                .filename(file.getOriginalFilename())
                .contentType(contentType);

        body.part("category", "PHOTO");
        body.part("projectId", ""); // MUST be empty for global photos
        body.part("uploadedBy", uploadedBy);

        return webClient.post()
                .uri("/files") // files-service endpoint that receives multipart uploads
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body.build()))
                .retrieve()
                .bodyToMono(String.class);
    }
}

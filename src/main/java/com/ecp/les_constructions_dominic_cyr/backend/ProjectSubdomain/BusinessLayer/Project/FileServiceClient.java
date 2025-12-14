package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Client for communicating with the files service to validate file existence.
 */
@Component
public class FileServiceClient {

    private final WebClient webClient;

    public FileServiceClient(@Value("${files.service.base-url:http://localhost:8082}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Validates that a file exists in the files service by attempting to retrieve it.
     * 
     * @param fileId the file identifier to validate
     * @return Mono<Boolean> true if file exists, false otherwise
     */
    public Mono<Boolean> validateFileExists(String fileId) {
        if (fileId == null || fileId.trim().isEmpty()) {
            return Mono.just(false);
        }

        return webClient.get()
                .uri("/files/{fileId}", fileId)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorReturn(false);
    }
}


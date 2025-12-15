package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.ProjectManagement;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectManagement.ProjectManagementPageContent;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectManagement.ProjectManagementPageContentRepository;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ProjectManagementPageContentServiceImpl implements ProjectManagementPageContentService {

    private final ProjectManagementPageContentRepository repository;
    private final ObjectMapper objectMapper;

    public ProjectManagementPageContentServiceImpl(
            ProjectManagementPageContentRepository repository,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> getContentByLanguage(String language) {
        // Normalize language code
        String normalizedLanguage = normalizeLanguage(language);
        
        ProjectManagementPageContent content = repository.findByLanguage(normalizedLanguage)
                .orElseThrow(() -> new NotFoundException(
                        "Project management page content not found for language: " + normalizedLanguage));

        try {
            // Parse JSON string to Map
            TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
            return objectMapper.readValue(content.getContentJson(), typeRef);
        } catch (Exception e) {
            log.error("Error parsing JSON content for language: {}", normalizedLanguage, e);
            throw new RuntimeException("Failed to parse content JSON", e);
        }
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.isEmpty()) {
            return "en";
        }
        String lower = language.toLowerCase();
        if (lower.startsWith("fr")) {
            return "fr";
        }
        return "en";
    }
}


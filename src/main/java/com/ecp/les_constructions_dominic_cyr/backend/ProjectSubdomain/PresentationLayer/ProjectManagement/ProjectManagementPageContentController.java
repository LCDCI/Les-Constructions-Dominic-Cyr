package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.ProjectManagement;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.ProjectManagement.ProjectManagementPageContentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/project-management/content")
@CrossOrigin(origins = "http://localhost:3000")
public class ProjectManagementPageContentController {

    private final ProjectManagementPageContentService service;

    public ProjectManagementPageContentController(ProjectManagementPageContentService service) {
        this.service = service;
    }

    @GetMapping("/{language}")
    public ResponseEntity<Map<String, Object>> getContentByLanguage(@PathVariable String language) {
        Map<String, Object> content = service.getContentByLanguage(language);
        return ResponseEntity.ok(content);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getContentDefault() {
        // Default to English if no language specified
        Map<String, Object> content = service.getContentByLanguage("en");
        return ResponseEntity.ok(content);
    }
}


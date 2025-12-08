package com.ecp.les_constructions_dominic_cyr.backend.utils.translation.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.dataaccesslayer.TranslationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing translation file registry.
 * Allows dynamic registration of translation file IDs.
 */
@RestController
@RequestMapping("/api/v1/translations/registry")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class TranslationRegistryController {

    private final TranslationRegistry registry;

    /**
     * Registers a translation file ID for a language and page.
     * 
     * @param language the language code (e.g., "en", "fr")
     * @param page the page name (e.g., "home", "projects")
     * @param fileId the file ID from the file service
     * @return success response
     */
    @PostMapping("/{language}/{page}")
    public ResponseEntity<String> registerFileId(
            @PathVariable String language,
            @PathVariable String page,
            @RequestBody String fileId) {
        
        registry.registerFileId(language, page, fileId.trim());
        return ResponseEntity.ok("File ID registered successfully for " + language + "." + page);
    }

    /**
     * Gets the file ID for a language and page.
     * 
     * @param language the language code
     * @param page the page name
     * @return the file ID or 404 if not found
     */
    @GetMapping("/{language}/{page}")
    public ResponseEntity<String> getFileId(
            @PathVariable String language,
            @PathVariable String page) {
        
        String fileId = registry.getFileId(language, page);
        if (fileId == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fileId);
    }
}


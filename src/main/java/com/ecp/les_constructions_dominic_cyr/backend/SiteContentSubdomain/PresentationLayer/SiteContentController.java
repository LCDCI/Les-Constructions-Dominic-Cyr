package com.ecp.les_constructions_dominic_cyr.backend.SiteContentSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.SiteContentSubdomain.DataAccessLayer.SiteContent;
import com.ecp.les_constructions_dominic_cyr.backend.SiteContentSubdomain.DataAccessLayer.SiteContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/site-content")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class SiteContentController {
    private final SiteContentRepository repository;

    @GetMapping
    public ResponseEntity<List<SiteContent>> getContent(
            @RequestParam(defaultValue = "en") String language,
            @RequestParam(required = false) String pageGroup
    ) {
        String normalizedLanguage = normalizeLanguage(language);
        if (pageGroup == null || pageGroup.isBlank()) {
            return ResponseEntity.ok(repository.findAllByLanguageOrderByPageGroupAscSortOrderAscPageKeyAsc(
                normalizedLanguage
            ));
        }
        return ResponseEntity.ok(repository.findAllByLanguageAndPageGroupOrderBySortOrderAscPageKeyAsc(
            normalizedLanguage,
            pageGroup
        ));
        }

        @GetMapping("/groups")
        public ResponseEntity<List<String>> getGroups(
            @RequestParam(defaultValue = "en") String language
        ) {
        return ResponseEntity.ok(repository.findPageGroupsByLanguage(
            normalizeLanguage(language)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SiteContent> updateContent(
            @PathVariable Long id,
            @RequestBody UpdateSiteContentRequest request
    ) {
        return repository.findById(id)
                .map(content -> {
                    content.setContentText(request.contentText());
                    content.setImageIdentifier(request.imageIdentifier());
                    return ResponseEntity.ok(repository.save(content));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SiteContent> createContent(
            @RequestBody CreateSiteContentRequest request
    ) {
        SiteContent content = SiteContent.builder()
                .pageGroup(request.pageGroup())
                .pageKey(request.pageKey())
                .language(normalizeLanguage(request.language()))
                .contentText(request.contentText())
                .imageIdentifier(request.imageIdentifier())
                .sortOrder(request.sortOrder() == null ? 0 : request.sortOrder())
                .build();
        return ResponseEntity.ok(repository.save(content));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private String normalizeLanguage(String language) {
        return language != null && language.toLowerCase().startsWith("fr") ? "fr" : "en";
    }

    public record UpdateSiteContentRequest(String contentText, String imageIdentifier) {}

    public record CreateSiteContentRequest(
            String pageGroup,
            String pageKey,
            String language,
            String contentText,
            String imageIdentifier,
            Integer sortOrder
    ) {}
}

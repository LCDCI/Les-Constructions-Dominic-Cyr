package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project.IndividualProjectMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class IndividualProjectMetadataController {
    private final IndividualProjectMetadataService projectMetadataService;

    @GetMapping("/{projectIdentifier}/metadata")
    public ResponseEntity<IndividualProjectResponseModel> getProjectMetadata(
            @PathVariable String projectIdentifier,
            @AuthenticationPrincipal Jwt jwt
    ) {
        IndividualProjectResponseModel metadata = projectMetadataService.getProjectMetadata(projectIdentifier, jwt.getSubject());
        return ResponseEntity.ok(metadata);
    }
}

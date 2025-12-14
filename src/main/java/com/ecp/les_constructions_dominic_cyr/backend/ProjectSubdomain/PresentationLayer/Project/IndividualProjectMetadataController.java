package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project.IndividualProjectMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class IndividualProjectMetadataController {
    private final IndividualProjectMetadataService projectMetadataService;

    @GetMapping("/{projectIdentifier}/metadata")
    public ResponseEntity<IndividualProjectResponseModel> getProjectMetadata(
            @PathVariable String projectIdentifier
    ) {
        IndividualProjectResponseModel metadata = projectMetadataService.getProjectMetadata(projectIdentifier);
        return ResponseEntity.ok(metadata);
    }
}

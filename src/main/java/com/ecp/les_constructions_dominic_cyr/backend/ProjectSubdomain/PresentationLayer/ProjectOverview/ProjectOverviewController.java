package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.ProjectOverview;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.ProjectOverview.ProjectOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ProjectOverviewController {

    private final ProjectOverviewService projectOverviewService;

    @GetMapping("/{projectIdentifier}/overview")
    public ResponseEntity<ProjectOverviewResponseModel> getProjectOverview(
            @PathVariable String projectIdentifier) {
        ProjectOverviewResponseModel overview = projectOverviewService.getProjectOverview(projectIdentifier);
        return ResponseEntity.ok(overview);
    }
}
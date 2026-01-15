package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project.ProjectService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private static final SimpleGrantedAuthority ROLE_OWNER = new SimpleGrantedAuthority("ROLE_OWNER");

    @GetMapping
    public ResponseEntity<List<ProjectResponseModel>> getAllProjects(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat. ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO. DATE) LocalDate endDate,
            @RequestParam(required = false) String customerId,
            Authentication authentication
    ) {
        List<ProjectResponseModel> projects;
        boolean isOwner = isOwner(authentication);

        if (status != null || startDate != null || endDate != null || customerId != null) {
            projects = projectService.filterProjects(status, startDate, endDate, customerId, isOwner);
        } else {
            projects = projectService.getAllProjects(isOwner);
        }

        return ResponseEntity.ok(projects);
    }

    private boolean isOwner(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities != null && authorities.contains(ROLE_OWNER);
    }

    @GetMapping("/{projectIdentifier}")
    public ResponseEntity<ProjectResponseModel> getProjectByIdentifier(@PathVariable String projectIdentifier) {
        ProjectResponseModel project = projectService.getProjectByIdentifier(projectIdentifier);
        return ResponseEntity.ok(project);
    }

    @PutMapping("/{projectIdentifier}")
    public ResponseEntity<ProjectResponseModel> updateProject(
            @PathVariable String projectIdentifier,
            @RequestBody ProjectRequestModel requestModel
    ) {
        ProjectResponseModel updatedProject = projectService.updateProject(projectIdentifier, requestModel);
        return ResponseEntity.ok(updatedProject);
    }

    @PostMapping
    public ResponseEntity<ProjectResponseModel> createProject(@RequestBody ProjectRequestModel requestModel) {
        ProjectResponseModel createdProject = projectService.createProject(requestModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    @DeleteMapping("/{projectIdentifier}")
    public ResponseEntity<Void> deleteProject(@PathVariable String projectIdentifier) {
        projectService.deleteProject(projectIdentifier);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{projectIdentifier}/contractor")
    public ResponseEntity<ProjectResponseModel> assignContractor(
            @PathVariable String projectIdentifier,
            @RequestParam String contractorId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ProjectResponseModel updatedProject = projectService.assignContractorToProject(projectIdentifier, contractorId, jwt.getSubject());
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{projectIdentifier}/contractor")
    public ResponseEntity<ProjectResponseModel> removeContractor(
            @PathVariable String projectIdentifier,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ProjectResponseModel updatedProject = projectService.removeContractorFromProject(projectIdentifier, jwt.getSubject());
        return ResponseEntity.ok(updatedProject);
    }

    @PutMapping("/{projectIdentifier}/salesperson")
    public ResponseEntity<ProjectResponseModel> assignSalesperson(
            @PathVariable String projectIdentifier,
            @RequestParam String salespersonId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ProjectResponseModel updatedProject = projectService.assignSalespersonToProject(projectIdentifier, salespersonId, jwt.getSubject());
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{projectIdentifier}/salesperson")
    public ResponseEntity<ProjectResponseModel> removeSalesperson(
            @PathVariable String projectIdentifier,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ProjectResponseModel updatedProject = projectService.removeSalespersonFromProject(projectIdentifier, jwt.getSubject());
        return ResponseEntity.ok(updatedProject);
    }

    @GetMapping("/{projectIdentifier}/activity-log")
    public ResponseEntity<List<ProjectActivityLogResponseModel>> getProjectActivityLog(
            @PathVariable String projectIdentifier
    ) {
        List<ProjectActivityLogResponseModel> activityLog = projectService.getProjectActivityLog(projectIdentifier);
        return ResponseEntity.ok(activityLog);
    }
}
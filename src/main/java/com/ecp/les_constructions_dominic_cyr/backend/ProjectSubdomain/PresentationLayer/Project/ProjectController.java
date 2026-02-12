package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project.ProjectService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectStatus;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import java.util.UUID;
import java.util.Set;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;
    private final LotRepository lotRepository;
    private static final SimpleGrantedAuthority ROLE_OWNER = new SimpleGrantedAuthority("ROLE_OWNER");
    private static final SimpleGrantedAuthority ROLE_CUSTOMER = new SimpleGrantedAuthority("ROLE_CUSTOMER");
    private static final SimpleGrantedAuthority ROLE_CONTRACTOR = new SimpleGrantedAuthority("ROLE_CONTRACTOR");
    private static final SimpleGrantedAuthority ROLE_SALESPERSON = new SimpleGrantedAuthority("ROLE_SALESPERSON");

    @GetMapping
    public ResponseEntity<List<ProjectResponseModel>> getAllProjects(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String customerId,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        List<ProjectResponseModel> projects;
        boolean isOwner = isOwner(authentication);

        // First, get projects based on filters or all projects
        if (status != null || startDate != null || endDate != null || customerId != null) {
            projects = projectService.filterProjects(status, startDate, endDate, customerId, isOwner);
        } else {
            projects = projectService.getAllProjects(isOwner);
        }

        // Then filter by user role and assigned projects — include projects where the
        // user is assigned to any lot within the project.
        if (!isOwner && jwt != null && authentication != null) {
            String auth0UserId = jwt.getSubject();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            // Get the user's identifier for filtering
            UserResponseModel currentUser = null;
            try {
                currentUser = userService.getUserByAuth0Id(auth0UserId);
            } catch (Exception e) {
                log.warn("Authenticated user not found in database. Auth0 ID: {}", auth0UserId, e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(List.of());
            }

            final String userIdentifier = currentUser.getUserIdentifier();

            // Build a filtered list that only contains projects where the user is
            // explicitly assigned (project-level) or assigned to a lot inside the project.
            final java.util.Set<String> projectIdsFromLots = new java.util.HashSet<>();
            try {
                UUID userUuid = UUID.fromString(userIdentifier);
                var lots = lotRepository.findByAssignedUserId(userUuid);
                projectIdsFromLots.addAll(lots.stream()
                        .map(l -> l.getProject().getProjectIdentifier())
                        .filter(pid -> pid != null)
                        .collect(Collectors.toSet()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid UUID for user identifier: {}", userIdentifier);
            }

            List<ProjectResponseModel> filtered = projects.stream()
                    .filter(p -> {
                        boolean matchesProjectLevel = false;
                        if (authorities.contains(ROLE_CUSTOMER)) {
                            matchesProjectLevel = userIdentifier.equals(p.getCustomerId());
                        } else if (authorities.contains(ROLE_CONTRACTOR)) {
                            matchesProjectLevel = p.getContractorIds() != null && p.getContractorIds().contains(userIdentifier);
                        } else if (authorities.contains(ROLE_SALESPERSON)) {
                            matchesProjectLevel = p.getSalespersonIds() != null && p.getSalespersonIds().contains(userIdentifier);
                        }
                        boolean matchesLotAssignment = projectIdsFromLots.contains(p.getProjectIdentifier());
                        return matchesProjectLevel || matchesLotAssignment;
                    })
                    .collect(Collectors.toList());

            projects = filtered;
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
    public ResponseEntity<ProjectResponseModel> getProjectByIdentifier(
            @PathVariable String projectIdentifier,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        // Check if user is authorized to view this project
        boolean isOwner = isOwner(authentication);

        if (!isOwner && jwt != null && authentication != null) {
            String auth0UserId = jwt.getSubject();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            // Get the user's identifier for filtering
            UserResponseModel currentUser = null;
            try {
                currentUser = userService.getUserByAuth0Id(auth0UserId);
            } catch (Exception e) {
                log.warn("Authenticated user not found in database. Auth0 ID: {}", auth0UserId, e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            final String userIdentifier = currentUser.getUserIdentifier();

            // Check if user has project-level access or lot-level access
            ProjectResponseModel project = projectService.getProjectByIdentifier(projectIdentifier);

            boolean hasAccess = false;

            // Check project-level assignments
            if (authorities.contains(ROLE_CUSTOMER)) {
                hasAccess = userIdentifier.equals(project.getCustomerId());
            } else if (authorities.contains(ROLE_CONTRACTOR)) {
                hasAccess = project.getContractorIds() != null && project.getContractorIds().contains(userIdentifier);
            } else if (authorities.contains(ROLE_SALESPERSON)) {
                hasAccess = project.getSalespersonIds() != null && project.getSalespersonIds().contains(userIdentifier);
            }

            // If no project-level access, check lot-level access
            if (!hasAccess) {
                try {
                    UUID userUuid = UUID.fromString(userIdentifier);
                    var lots = lotRepository.findByAssignedUserId(userUuid);
                    hasAccess = lots.stream()
                            .anyMatch(lot -> projectIdentifier.equals(lot.getProject().getProjectIdentifier()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid UUID for user identifier: {}", userIdentifier);
                }
            }

            if (!hasAccess) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

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

    @PutMapping("/{projectIdentifier}/customer")
    public ResponseEntity<ProjectResponseModel> assignCustomer(
            @PathVariable String projectIdentifier,
            @RequestParam String customerId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ProjectResponseModel updatedProject = projectService.assignCustomerToProject(projectIdentifier, customerId, jwt.getSubject());
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{projectIdentifier}/customer")
    public ResponseEntity<ProjectResponseModel> removeCustomer(
            @PathVariable String projectIdentifier,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ProjectResponseModel updatedProject = projectService.removeCustomerFromProject(projectIdentifier, jwt.getSubject());
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
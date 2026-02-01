package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LotDocument;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.LotDocument.LotDocumentService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/lots/{lotId}/documents")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LotDocumentController {

    private final LotDocumentService lotDocumentService;
    private final UserService userService;
    private final LotRepository lotRepository;

    private static final SimpleGrantedAuthority ROLE_OWNER = new SimpleGrantedAuthority("ROLE_OWNER");

    /**
     * GET /api/v1/lots/{lotId}/documents
     * Get all documents for a lot with optional filtering.
     * Query params: search (filename substring), type (image|file|all), sort, page, size
     */
    @GetMapping
    public ResponseEntity<List<LotDocumentResponseModel>> getLotDocuments(
            @PathVariable String lotId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "all") String type,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        log.info("GET /api/v1/lots/{}/documents - search: {}, type: {}", lotId, search, type);

        // Validate user is authenticated and assigned to lot (or is owner)
        validateLotAccess(lotId, jwt, authentication);

        List<LotDocumentResponseModel> documents = lotDocumentService.getLotDocuments(lotId, search, type);
        return ResponseEntity.ok(documents);
    }

    /**
     * POST /api/v1/lots/{lotId}/documents
     * Upload one or more documents to a lot.
     * Only Owner or Contractor assigned to lot can upload.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<LotDocumentResponseModel>> uploadDocuments(
            @PathVariable String lotId,
            @RequestParam("files") MultipartFile[] files,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        log.info("POST /api/v1/lots/{}/documents - uploading {} files", lotId, files.length);

        if (files == null || files.length == 0) {
            throw new InvalidInputException("No files provided");
        }

        // Get current user
        String auth0UserId = jwt.getSubject();
        UserResponseModel currentUser = getUserByAuth0Id(auth0UserId);
        String uploaderUserId = currentUser.getUserIdentifier();

        // Validate user is authenticated and has upload permissions
        validateLotAccess(lotId, jwt, authentication);

        List<LotDocumentResponseModel> uploadedDocuments = lotDocumentService.uploadDocuments(
                lotId,
                files,
                uploaderUserId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedDocuments);
    }

    /**
     * GET /api/v1/lots/{lotId}/documents/{documentId}/download
     * Download a document. Any team member assigned to lot can download.
     */
    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable String lotId,
            @PathVariable UUID documentId,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        log.info("GET /api/v1/lots/{}/documents/{}/download", lotId, documentId);

        // Get current user
        String auth0UserId = jwt.getSubject();
        UserResponseModel currentUser = getUserByAuth0Id(auth0UserId);
        String requestingUserId = currentUser.getUserIdentifier();

        // Validate user is authenticated and assigned to lot
        validateLotAccess(lotId, jwt, authentication);

        byte[] fileData = lotDocumentService.downloadDocument(lotId, documentId, requestingUserId);
        String contentType = lotDocumentService.getDocumentContentType(lotId, documentId);

        ByteArrayResource resource = new ByteArrayResource(fileData);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .body(resource);
    }

    /**
     * DELETE /api/v1/lots/{lotId}/documents/{documentId}
     * Delete a document. Only uploader or Owner assigned to lot can delete.
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Map<String, String>> deleteDocument(
            @PathVariable String lotId,
            @PathVariable UUID documentId,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        log.info("DELETE /api/v1/lots/{}/documents/{}", lotId, documentId);

        // Get current user
        String auth0UserId = jwt.getSubject();
        UserResponseModel currentUser = getUserByAuth0Id(auth0UserId);
        String requestingUserId = currentUser.getUserIdentifier();

        // Validate user is authenticated and assigned to lot
        validateLotAccess(lotId, jwt, authentication);

        lotDocumentService.deleteDocument(lotId, documentId, requestingUserId);

        return ResponseEntity.ok(Map.of("message", "Document deleted successfully"));
    }

    // ========== PRIVATE HELPER METHODS ==========

    private void validateLotAccess(String lotId, Jwt jwt, Authentication authentication) {
        if (jwt == null || authentication == null) {
            throw new AccessDeniedException("Unauthorized request");
        }

        // Owner has access to all lots
        if (isOwner(authentication)) {
            return;
        }

        // Non-owner: must be assigned to this specific lot
        UserResponseModel currentUser = getUserByAuth0Id(jwt.getSubject());
        UUID userId = UUID.fromString(currentUser.getUserIdentifier());
        UUID lotUuid = UUID.fromString(lotId);

        List<Lot> assignedLots = lotRepository.findByAssignedUserId(userId);
        boolean isAssignedToLot = assignedLots.stream()
                .anyMatch(lot -> lot.getLotIdentifier().getLotId().equals(lotUuid));

        if (!isAssignedToLot) {
            throw new AccessDeniedException("User is not assigned to this lot");
        }
    }

    private boolean isOwner(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities != null && authorities.contains(ROLE_OWNER);
    }

    private UserResponseModel getUserByAuth0Id(String auth0UserId) {
        try {
            return userService.getUserByAuth0Id(auth0UserId);
        } catch (Exception e) {
            log.error("User not found for Auth0 ID: {}", auth0UserId, e);
            throw new InvalidInputException("User not found");
        }
    }
}

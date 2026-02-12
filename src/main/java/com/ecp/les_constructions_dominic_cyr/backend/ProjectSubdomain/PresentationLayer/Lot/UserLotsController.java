package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Lot.LotService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for fetching lots assigned to the authenticated user
 * Used for lot documents feature
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/lots")
@CrossOrigin(origins = "*")
public class UserLotsController {
    
    private final LotService lotService;
    private final UserService userService;
    private final LotRepository lotRepository;
    private static final SimpleGrantedAuthority ROLE_OWNER = new SimpleGrantedAuthority("ROLE_OWNER");

    public UserLotsController(LotService lotService, UserService userService, LotRepository lotRepository) {
        this.lotService = lotService;
        this.userService = userService;
        this.lotRepository = lotRepository;
    }

    /**
     * Get all lots assigned to the authenticated user
     * Owners see all lots, other roles see only their assigned lots
     */
    @GetMapping
    public ResponseEntity<List<LotResponseModel>> getUserLots(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        if (jwt == null || authentication == null) {
            log.warn("Unauthenticated request to /api/v1/lots");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String auth0UserId = jwt.getSubject();
        
        // Check if user is owner - they see all lots
        boolean isOwner = isOwner(authentication);
        
        if (isOwner) {
            log.info("Owner user {} accessing all lots", auth0UserId);
            // Get all lots from all projects
            var allLots = lotRepository.findAll();
            List<LotResponseModel> responseModels = lotService.mapLotsToResponses(allLots);
            return ResponseEntity.ok().body(responseModels);
        }

        // For non-owners, get only assigned lots
        UserResponseModel currentUser;
        try {
            currentUser = userService.getUserByAuth0Id(auth0UserId);
        } catch (Exception e) {
            log.warn("Authenticated user not found in database. Auth0 ID: {}", auth0UserId, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final String userIdentifier = currentUser.getUserIdentifier();
        
        try {
            UUID userUuid = UUID.fromString(userIdentifier);
            log.info("Fetching lots for user {} (UUID: {})", auth0UserId, userUuid);
            
            var userLots = lotRepository.findByAssignedUserId(userUuid);
            log.info("Found {} lots assigned to user", userLots.size());
            
            List<LotResponseModel> responseModels = lotService.mapLotsToResponses(userLots);
            return ResponseEntity.ok().body(responseModels);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID for user identifier: {}", userIdentifier, e);
            return ResponseEntity.ok().body(List.of());
        }
    }

    /**
     * Get a specific lot by ID
     * Owners can access any lot, other roles can only access lots assigned to them
     */
    @GetMapping("/{lotId}")
    public ResponseEntity<LotResponseModel> getLotById(
            @PathVariable String lotId,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        if (jwt == null || authentication == null) {
            log.warn("Unauthenticated request to /api/v1/lots/{}", lotId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String auth0UserId = jwt.getSubject();
        
        // Check if user is owner - they can access any lot
        boolean isOwner = isOwner(authentication);
        
        try {
            log.info("User {} requesting lot {}", auth0UserId, lotId);
            LotResponseModel lot = lotService.getLotById(lotId);
            
            if (isOwner) {
                log.info("Owner user {} accessing lot {}", auth0UserId, lotId);
                return ResponseEntity.ok().body(lot);
            }

            // For non-owners, verify they have access to this lot
            UserResponseModel currentUser;
            try {
                currentUser = userService.getUserByAuth0Id(auth0UserId);
            } catch (Exception e) {
                log.warn("Authenticated user not found in database. Auth0 ID: {}", auth0UserId, e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            final String userIdentifier = currentUser.getUserIdentifier();
            UUID userUuid = UUID.fromString(userIdentifier);
            UUID lotUuid = UUID.fromString(lotId);
            
            // Check if this lot is assigned to the user
            var userLots = lotRepository.findByAssignedUserId(userUuid);
            boolean hasAccess = userLots.stream()
                    .anyMatch(l -> l.getLotIdentifier().getLotId().equals(lotUuid));
            
            if (!hasAccess) {
                log.warn("User {} does not have access to lot {}", auth0UserId, lotId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            return ResponseEntity.ok().body(lot);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format: {}", lotId, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching lot {}", lotId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean isOwner(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        java.util.Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities != null && authorities.contains(ROLE_OWNER);
    }
}

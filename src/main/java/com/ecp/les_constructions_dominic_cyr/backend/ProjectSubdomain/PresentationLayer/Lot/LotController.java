package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Lot.LotService;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects/{projectIdentifier}/lots")
@CrossOrigin(origins = "*")
public class LotController {
    private final LotService lotService;
    private final UserService userService;
    private final LotRepository lotRepository;
    private static final int UUID_LENGTH = 36;
    private static final SimpleGrantedAuthority ROLE_OWNER = new SimpleGrantedAuthority("ROLE_OWNER");
    private static final SimpleGrantedAuthority ROLE_CUSTOMER = new SimpleGrantedAuthority("ROLE_CUSTOMER");
    private static final SimpleGrantedAuthority ROLE_CONTRACTOR = new SimpleGrantedAuthority("ROLE_CONTRACTOR");
    private static final SimpleGrantedAuthority ROLE_SALESPERSON = new SimpleGrantedAuthority("ROLE_SALESPERSON");

    public LotController(LotService lotService, UserService userService, LotRepository lotRepository) {
        this.lotService = lotService;
        this.userService = userService;
        this.lotRepository = lotRepository;
    }

    @GetMapping()
    public ResponseEntity<List<LotResponseModel>> getAllLotsByProject(
            @PathVariable String projectIdentifier,
            @RequestParam(required = false) String customerId,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ){
        if(projectIdentifier == null || projectIdentifier.isBlank()){
            throw new InvalidInputException("Project identifier must not be blank");
        }

        // Check if user is authorized to view lots in this project
        boolean isOwner = isOwner(authentication);

        if (!isOwner && jwt != null && authentication != null) {
            String auth0UserId = jwt.getSubject();
            java.util.Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            // Get the user's identifier for filtering
            UserResponseModel currentUser = null;
            try {
                currentUser = userService.getUserByAuth0Id(auth0UserId);
            } catch (Exception e) {
                log.warn("Authenticated user not found in database. Auth0 ID: {}", auth0UserId, e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            final String userIdentifier = currentUser.getUserIdentifier();

            // If customerId is provided, filter lots where both current user and customer are assigned
            if (customerId != null && !customerId.isBlank()) {
                try {
                    List<LotResponseModel> sharedLots = lotService.getLotsByProjectAndBothUsersAssigned(
                        projectIdentifier, 
                        userIdentifier, 
                        customerId
                    );
                    return ResponseEntity.ok().body(sharedLots);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid UUID for user identifier or customer ID: {} / {}", userIdentifier, customerId);
                    return ResponseEntity.ok().body(List.of());
                }
            }

            // Check if user has project-level access or lot-level access
            // For lots, we only return lots the user is assigned to
            try {
                UUID userUuid = UUID.fromString(userIdentifier);
                var userLots = lotRepository.findByAssignedUserId(userUuid);
                var projectLots = userLots.stream()
                        .filter(lot -> projectIdentifier.equals(lot.getProject().getProjectIdentifier()))
                        .collect(Collectors.toList());

                // Use the service to map to response models
                List<LotResponseModel> responseModels = lotService.mapLotsToResponses(projectLots);

                return ResponseEntity.ok().body(responseModels);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid UUID for user identifier: {}", userIdentifier);
                return ResponseEntity.ok().body(List.of());
            }
        }

        // Owners see all lots
        return ResponseEntity.ok().body(lotService.getAllLotsByProject(projectIdentifier));
    }

    @GetMapping("/{lotId}")
    public ResponseEntity<LotResponseModel> getLotById(@PathVariable String lotId){
        if(lotId.length() != UUID_LENGTH){
            throw new InvalidInputException("Invalid lot ID: " + lotId);
        }
        validateUUID(lotId);
        return ResponseEntity.ok().body(lotService.getLotById(lotId));
    }

    @PostMapping
    public ResponseEntity<LotResponseModel> addLot(@PathVariable String projectIdentifier,
                                                   @RequestBody LotRequestModel lotRequestModel){
        if(projectIdentifier == null || projectIdentifier.isBlank()){
            throw new InvalidInputException("Project identifier must not be blank");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(lotService.addLotToProject(projectIdentifier, lotRequestModel));
    }

    @PutMapping("/{lotId}")
    public ResponseEntity<LotResponseModel> updateLot(@RequestBody LotRequestModel lotRequestModel,
                                                     @PathVariable String lotId){
        if(lotId.length() != UUID_LENGTH){
            throw new InvalidInputException("Invalid lot ID: " + lotId);
        }
        validateUUID(lotId);
        return ResponseEntity.ok().body(lotService.updateLot(lotRequestModel, lotId));
    }
    @DeleteMapping("/{lotId}")
    public ResponseEntity<Void> deleteLot(@PathVariable String lotId){
        if(lotId.length() != UUID_LENGTH){
            throw new InvalidInputException("Invalid lot ID: " + lotId);
        }
        validateUUID(lotId);
        lotService.deleteLot(lotId);
        return ResponseEntity.noContent().build();
    }

    private void validateUUID(String lotId) {
        try {
            UUID.fromString(lotId);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid UUID format: " + lotId);
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

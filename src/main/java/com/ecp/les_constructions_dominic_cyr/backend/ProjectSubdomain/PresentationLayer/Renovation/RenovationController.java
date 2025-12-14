package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Renovation;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Renovation.RenovationService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/renovations")
@CrossOrigin(origins = "http://localhost:3000")
public class RenovationController {
    private final RenovationService renovationService;

    public RenovationController(RenovationService renovationService) {
        this.renovationService = renovationService;
    }

    @GetMapping()
    public ResponseEntity<List<RenovationResponseModel>> getAllRenovations() {
        List<RenovationResponseModel> renovations = renovationService.getAllRenovations();
        return ResponseEntity.ok(renovations);
    }

    @GetMapping("/{renovationId}")
    public ResponseEntity<RenovationResponseModel> getRenovationById(@PathVariable String renovationId) {
        validateUUID(renovationId);
        return ResponseEntity.ok().body(renovationService.getRenovationById(renovationId));
    }

    @PostMapping()
    public ResponseEntity<RenovationResponseModel> createRenovation(@RequestBody RenovationRequestModel renovationRequestModel){
        return ResponseEntity.status(HttpStatus.CREATED).body(renovationService.createRenovation(renovationRequestModel));
    }

    @PutMapping("/{renovationId}")
    public ResponseEntity<RenovationResponseModel> updateRenovation(@RequestBody RenovationRequestModel renovationRequestModel, @PathVariable String renovationId){
        validateUUID(renovationId);
        return ResponseEntity.ok().body(renovationService.updateRenovation(renovationRequestModel, renovationId));
    }

    @DeleteMapping("/{renovationId}")
    public ResponseEntity<Void> deleteRenovation(@PathVariable String renovationId){
        validateUUID(renovationId);
        renovationService.deleteRenovation(renovationId);
        return ResponseEntity.noContent().build();
    }

    private void validateUUID(String lotId) {
        try {
            UUID.fromString(lotId);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid UUID format: " + lotId);
        }
    }
}

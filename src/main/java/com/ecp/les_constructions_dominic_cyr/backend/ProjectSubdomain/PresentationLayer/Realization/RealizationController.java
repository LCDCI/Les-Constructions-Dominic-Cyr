package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Realization;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Realization.RealizationService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/realizations")
@CrossOrigin(origins = "http://localhost:3000")
public class RealizationController {
    private final RealizationService realizationService;
    private static final int UUID_LENGTH = 36;

    public RealizationController(RealizationService realizationService) {
        this.realizationService = realizationService;
    }

    @GetMapping()
    public ResponseEntity<List<RealizationResponseModel>> getAllRealizations(){
        return ResponseEntity.ok().body(realizationService.getAllRealizations());
    }

    @GetMapping("/{realizationId}")
    public ResponseEntity<RealizationResponseModel> getRealizationById(@PathVariable String realizationId){
        if(realizationId.length() != UUID_LENGTH){
            throw new InvalidInputException("Invalid realization ID: " + realizationId);
        }
        validateUUID(realizationId);
        return ResponseEntity.ok().body(realizationService.getRealizationById(realizationId));
    }

    private void validateUUID(String realizationId){
        try{
            UUID.fromString(realizationId);
        } catch (IllegalArgumentException e){
            throw new InvalidInputException("Invalid realization ID: " + realizationId);
        }
    }
}

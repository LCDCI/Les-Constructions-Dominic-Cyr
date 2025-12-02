package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.BusinessLayer.LotService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/lots")
public class LotController {
    private LotService lotService;
    private static final int UUID_LENGTH = 36;

    public LotController(LotService lotService) {
        this.lotService = lotService;
    }

    @GetMapping()
    public ResponseEntity<List<LotResponseModel>> getAllLots(){
        return ResponseEntity.ok().body(lotService.getAllLots());
    }

    @GetMapping("/{lotId}")
    public ResponseEntity<LotResponseModel> getLotById(@PathVariable String lotId){
        if(lotId.length() != UUID_LENGTH){
            throw new InvalidInputException("Invalid lot ID: " + lotId);
        }
        return ResponseEntity.ok().body(lotService.getLotById(lotId));
    }

    @PostMapping
    public ResponseEntity<LotResponseModel> addLot(@RequestBody LotRequestModel lotRequestModel){
        return ResponseEntity.status(HttpStatus.CREATED).body(lotService.addLot(lotRequestModel));
    }

    @PutMapping("/{lotId}")
    public ResponseEntity<LotResponseModel> updateLot(@RequestBody LotRequestModel lotRequestModel,
                                                     @PathVariable String lotId){
        if(lotId.length() != UUID_LENGTH){
            throw new InvalidInputException("Invalid lot ID: " + lotId);
        }
        return ResponseEntity.ok().body(lotService.updateLot(lotRequestModel, lotId));
    }
    @DeleteMapping("/{lotId}")
    public ResponseEntity<Void> deleteLot(@PathVariable String lotId){
        if(lotId.length() != UUID_LENGTH){
            throw new InvalidInputException("Invalid lot ID: " + lotId);
        }
        lotService.deleteLot(lotId);
        return ResponseEntity.noContent().build();
    }
}

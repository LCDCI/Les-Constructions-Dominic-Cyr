package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.House;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.House.HouseService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/houses")
@CrossOrigin(origins = "http://localhost:3000")
public class HouseController {
    private final HouseService houseService;
    private static final int UUID_LENGTH = 36;

    public HouseController(HouseService houseService) {
        this.houseService = houseService;
    }

    @GetMapping()
    public ResponseEntity<List<HouseResponseModel>> getAllHouses(){
        return ResponseEntity.ok().body(houseService.getAllHouses());
    }

    @GetMapping("/{houseId}")
    public ResponseEntity<HouseResponseModel> getHouseById(@PathVariable String houseId){
        if(houseId.length() != UUID_LENGTH){
            throw new InvalidInputException("Invalid house ID: " + houseId);
        }
        validateUUID(houseId);
        return ResponseEntity.ok().body(houseService.getHouseById(houseId));
    }

    private void validateUUID(String houseId){
        try{
            UUID.fromString(houseId);
        } catch (IllegalArgumentException e){
            throw new InvalidInputException("Invalid house ID: " + houseId);
        }
    }
}

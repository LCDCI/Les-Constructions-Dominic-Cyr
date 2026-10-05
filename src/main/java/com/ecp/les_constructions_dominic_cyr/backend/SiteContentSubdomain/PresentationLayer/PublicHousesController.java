package com.ecp.les_constructions_dominic_cyr.backend.SiteContentSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.SiteContentSubdomain.DataAccessLayer.SiteContentRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization.RealizationRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/houses")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class PublicHousesController {
    private final SiteContentRepository repository;
    private final RealizationRepository realizationRepository;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getHouses() {
        List<Map<String, Object>> houses = new ArrayList<>();
        realizationRepository.findAll().forEach(realization -> houses.add(Map.of(
                "houseId", realization.getRealizationIdentifier().getRealizationId(),
                "houseName", realization.getRealizationName(),
                "location", realization.getLocation(),
                "description", realization.getDescription(),
                "imageIdentifier", realization.getImageIdentifier() == null ? "" : realization.getImageIdentifier(),
                "numberOfRooms", realization.getNumberOfRooms(),
                "numberOfBedrooms", realization.getNumberOfBedrooms(),
                "numberOfBathrooms", realization.getNumberOfBathrooms(),
                "constructionYear", realization.getConstructionYear()
        )));

        if (!houses.isEmpty()) {
            return ResponseEntity.ok(houses);
        }

        repository.findAllByLanguageOrderByPageGroupAscSortOrderAscPageKeyAsc("en")
                .stream()
                .filter(item -> "houses".equals(item.getPageGroup()))
                .forEach(item -> {
                    try {
                        houses.add(objectMapper.readValue(
                                item.getContentText(),
                                new TypeReference<>() {
                                }
                        ));
                    } catch (Exception ignored) {
                        // Invalid editorial records are skipped instead of breaking the public page.
                    }
                });
        return ResponseEntity.ok(houses);
    }
}

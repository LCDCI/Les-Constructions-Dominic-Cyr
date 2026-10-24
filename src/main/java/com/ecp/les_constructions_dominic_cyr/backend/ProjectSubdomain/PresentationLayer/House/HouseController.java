package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.House;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House.House;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House.HouseRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/houses")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class HouseController {
    private final HouseRepository repository;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${files.service.base-url}")
    private String filesServiceBaseUrl;

    @GetMapping
    public ResponseEntity<List<House>> getHouses(
            @RequestParam(required = false) String projectIdentifier
    ) {
        if (projectIdentifier == null || projectIdentifier.isBlank()) {
            return ResponseEntity.ok(repository.findAll());
        }
        return ResponseEntity.ok(repository.findAllByProjectIdentifierOrderByHouseNameAsc(projectIdentifier));
    }

    @PostMapping
    public ResponseEntity<House> createHouse(@Valid @RequestBody HouseRequest request) {
        House house = toHouse(request, UUID.randomUUID());
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(house));
    }

    @PutMapping("/{houseIdentifier}")
    public ResponseEntity<House> updateHouse(
            @PathVariable UUID houseIdentifier,
            @Valid @RequestBody HouseRequest request
    ) {
        House existing = repository.findByHouseIdentifier(houseIdentifier);
        if (existing == null) return ResponseEntity.notFound().build();
        existing.setProjectIdentifier(request.projectIdentifier().trim());
        existing.setHouseName(request.houseName().trim());
        existing.setLocation(trimToNull(request.location()));
        existing.setDescription(trimToNull(request.description()));
        existing.setDescriptionEn(trimToNull(request.descriptionEn()));
        existing.setDescriptionFr(trimToNull(request.descriptionFr()));
        existing.setImageIdentifier(trimToNull(request.imageIdentifier()));
        existing.setNumberOfRooms(request.numberOfRooms());
        existing.setNumberOfBedrooms(request.numberOfBedrooms());
        existing.setNumberOfBathrooms(request.numberOfBathrooms());
        existing.setConstructionYear(request.constructionYear());
        return ResponseEntity.ok(repository.save(existing));
    }

    @PostMapping(value = "/{houseIdentifier}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<House> uploadHouseImage(
            @PathVariable UUID houseIdentifier,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) {
        House house = repository.findByHouseIdentifier(houseIdentifier);
        if (house == null) return ResponseEntity.notFound().build();
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("file", file.getResource())
                .filename(file.getOriginalFilename())
                .contentType(file.getContentType() == null
                        ? MediaType.IMAGE_JPEG
                        : MediaType.parseMediaType(file.getContentType()));
        body.part("category", "PHOTO");
        body.part("projectId", house.getProjectIdentifier());
        body.part("uploadedBy", jwt == null ? "owner" : jwt.getSubject());
        body.part("uploaderRole", "OWNER");

        String response = webClientBuilder
                .baseUrl(filesServiceBaseUrl)
                .build()
                .post()
                .uri("/files")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body.build()))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode json = objectMapper.readTree(response);
            String imageIdentifier = json.path("fileId").asText(null);
            if (imageIdentifier == null || imageIdentifier.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
            }
            house.setImageIdentifier(imageIdentifier);
            return ResponseEntity.ok(repository.save(house));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    @DeleteMapping("/{houseIdentifier}")
    public ResponseEntity<Void> deleteHouse(@PathVariable UUID houseIdentifier) {
        House existing = repository.findByHouseIdentifier(houseIdentifier);
        if (existing == null) return ResponseEntity.notFound().build();
        repository.delete(existing);
        return ResponseEntity.noContent().build();
    }

    private House toHouse(HouseRequest request, UUID identifier) {
        return House.builder()
                .houseIdentifier(identifier)
                .projectIdentifier(request.projectIdentifier().trim())
                .houseName(request.houseName().trim())
                .location(trimToNull(request.location()))
                .description(trimToNull(request.description()))
                .descriptionEn(trimToNull(request.descriptionEn()))
                .descriptionFr(trimToNull(request.descriptionFr()))
                .imageIdentifier(trimToNull(request.imageIdentifier()))
                .numberOfRooms(request.numberOfRooms())
                .numberOfBedrooms(request.numberOfBedrooms())
                .numberOfBathrooms(request.numberOfBathrooms())
                .constructionYear(request.constructionYear())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    public record HouseRequest(
            @NotBlank @Size(max = 255) @Pattern(regexp = "[A-Za-z0-9._:-]+") String projectIdentifier,
            @NotBlank @Size(max = 255) String houseName,
            @Size(max = 255) String location,
            @Size(max = 100_000) String description,
            @Size(max = 100_000) String descriptionEn,
            @Size(max = 100_000) String descriptionFr,
            @Size(max = 500) String imageIdentifier,
            @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal numberOfRooms,
            @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal numberOfBedrooms,
            @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal numberOfBathrooms,
            @Min(1800) @Max(2500) Integer constructionYear
    ) {}
}

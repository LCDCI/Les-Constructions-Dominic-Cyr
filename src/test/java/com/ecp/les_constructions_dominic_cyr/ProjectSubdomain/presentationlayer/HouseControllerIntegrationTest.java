package com.ecp.les_constructions_dominic_cyr.ProjectSubdomain.presentationlayer;

import static org.junit.jupiter.api.Assertions.*;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House.House;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House.HouseIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House.HouseRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.House.HouseResponseModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;


import java.util.UUID;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(com.ecp.les_constructions_dominic_cyr.config.TestcontainersPostgresConfig.class)
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
class HouseControllerIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    HouseRepository houseRepository;

    private final String BASE_URI = "/api/v1/houses";

    @Test
    void whenGetAll_thenReturnList() {
        // Arrange - ensure at least one entity exists
        House entity = new House();
        entity.setHouseIdentifier(new HouseIdentifier(UUID.randomUUID().toString()));
        entity.setHouseName("Integration House");
        entity.setLocation("Integration Loc");
        entity.setDescription("Integration Desc");
        entity.setNumberOfRooms(5);
        entity.setNumberOfBedrooms(3);
        entity.setNumberOfBathrooms(2);
        entity.setConstructionYear(2020);
        houseRepository.save(entity);

        // Act & Assert
        webClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(HouseResponseModel.class)
                .value(list -> {
                    assertNotNull(list);
                    assertTrue(list.size() >= 1);
                });
    }

    @Test
    void whenGetByIdExists_thenReturn() {
        String idVal = UUID.randomUUID().toString();
        House entity = new House();
        entity.setHouseIdentifier(new HouseIdentifier(idVal));
        entity.setHouseName("ById House");
        entity.setLocation("ById Loc");
        entity.setDescription("ById Desc");
        entity.setNumberOfRooms(4);
        entity.setNumberOfBedrooms(2);
        entity.setNumberOfBathrooms(1);
        entity.setConstructionYear(2019);
        houseRepository.save(entity);

        webClient.get()
                .uri(BASE_URI + "/{id}", idVal)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(HouseResponseModel.class)
                .value(resp -> {
                    assertNotNull(resp);
                    assertEquals(idVal, resp.getHouseId());
                    assertEquals("ById House", resp.getHouseName());
                    assertEquals("ById Loc", resp.getLocation());
                    assertEquals("ById Desc", resp.getDescription());
                    assertEquals(4, resp.getNumberOfRooms());
                    assertEquals(2, resp.getNumberOfBedrooms());
                    assertEquals(1, resp.getNumberOfBathrooms());
                    assertEquals(2019, resp.getConstructionYear());
                });
    }

    @Test
    void whenGetByIdNotExists_thenReturnNotFound() {
        String validUUID = UUID.randomUUID().toString();
        webClient.get()
                .uri(BASE_URI + "/{id}", validUUID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}
package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import static org.junit.jupiter.api.Assertions.*;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization.Realization;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization.RealizationIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization.RealizationRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Realization.RealizationResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;


import java.util.UUID;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersPostgresConfig.class)
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
class RealizationControllerIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    RealizationRepository realizationRepository;

    private final String BASE_URI = "/api/v1/realizations";

    @Test
    void whenGetAll_thenReturnList() {
        // Arrange - ensure at least one entity exists
        Realization entity = new Realization();
        entity.setRealizationIdentifier(new RealizationIdentifier(UUID.randomUUID().toString()));
        entity.setRealizationName("Integration Realization");
        entity.setLocation("Integration Loc");
        entity.setDescription("Integration Desc");
        entity.setNumberOfRooms(5);
        entity.setNumberOfBedrooms(3);
        entity.setNumberOfBathrooms(2);
        entity.setConstructionYear(2020);
        realizationRepository.save(entity);

        // Act & Assert
        webClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RealizationResponseModel.class)
                .value(list -> {
                    assertNotNull(list);
                    assertTrue(list.size() >= 1);
                });
    }

    @Test
    void whenGetByIdExists_thenReturn() {
        String idVal = UUID.randomUUID().toString();
        Realization entity = new Realization();
        entity.setRealizationIdentifier(new RealizationIdentifier(idVal));
        entity.setRealizationName("ById Realization");
        entity.setLocation("ById Loc");
        entity.setDescription("ById Desc");
        entity.setNumberOfRooms(4);
        entity.setNumberOfBedrooms(2);
        entity.setNumberOfBathrooms(1);
        entity.setConstructionYear(2019);
        realizationRepository.save(entity);

        webClient.get()
                .uri(BASE_URI + "/{id}", idVal)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RealizationResponseModel.class)
                .value(resp -> {
                    assertNotNull(resp);
                    assertEquals(idVal, resp.getRealizationId());
                    assertEquals("ById Realization", resp.getRealizationName());
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

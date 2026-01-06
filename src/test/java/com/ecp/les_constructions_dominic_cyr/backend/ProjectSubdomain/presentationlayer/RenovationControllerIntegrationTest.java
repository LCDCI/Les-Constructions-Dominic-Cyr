package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import static org.junit.jupiter.api.Assertions.*;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.Renovation;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.RenovationIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.RenovationRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Renovation.RenovationResponseModel;
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
class RenovationControllerIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    RenovationRepository renovationRepository;

    private final String BASE_URI = "/api/v1/renovations";

    @Test
    void whenGetAll_thenReturnList() {
        // Arrange - ensure at least one entity exists
        Renovation entity = new Renovation();
        entity.setRenovationIdentifier(new RenovationIdentifier(UUID.randomUUID().toString()));
        entity.setBeforeImageIdentifier("before-integration");
        entity.setAfterImageIdentifier("after-integration");
        entity.setDescription("Integration Desc");
        renovationRepository.save(entity);

        // Act & Assert
        webClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RenovationResponseModel.class)
                .value(list -> {
                    assertNotNull(list);
                    assertTrue(list.size() >= 1);
                });
    }

    @Test
    void whenGetByIdExists_thenReturn() {
        String idVal = UUID.randomUUID().toString();
        Renovation entity = new Renovation();
        entity.setRenovationIdentifier(new RenovationIdentifier(idVal));
        entity.setBeforeImageIdentifier("before-byid");
        entity.setAfterImageIdentifier("after-byid");
        entity.setDescription("ById Desc");
        renovationRepository.save(entity);

        webClient.get()
                .uri(BASE_URI + "/{id}", idVal)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RenovationResponseModel.class)
                .value(resp -> {
                    assertNotNull(resp);
                    assertEquals(idVal, resp.getRenovationId());
                    assertEquals("before-byid", resp.getBeforeImageIdentifier());
                    assertEquals("after-byid", resp.getAfterImageIdentifier());
                    assertEquals("ById Desc", resp.getDescription());
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

    @Test
    void whenGetByIdInvalidUUID_thenReturnBadRequest() {
        String invalidId = "not-a-uuid";
        webClient.get()
                .uri(BASE_URI + "/{id}", invalidId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }
}

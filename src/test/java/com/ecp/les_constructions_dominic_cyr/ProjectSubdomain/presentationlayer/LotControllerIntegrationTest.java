package com.ecp.les_constructions_dominic_cyr.ProjectSubdomain.presentationlayer;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(com.ecp.les_constructions_dominic_cyr.config.TestcontainersPostgresConfig.class)
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
class LotControllerIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    LotRepository lotRepository;

    private MockRestServiceServer mockRestServiceServer;

    private ObjectMapper mapper = new ObjectMapper();

    private final String BASE_URI = "/api/v1/lots";
    private final String FOUND_LOT_ID = "c80a5ba7-2a24-42ad-a989-27bb1cb1bbd6";
    private final String NOT_FOUND_LOT_ID = "c80a5ba7-2a24-42ad-a989-27bb1cb1bbd0";
    private final String INVALID_FOUND_LOT_ID = "c80a5ba7-2a24-42ad-a989-27bb1cb1bbd";

    @BeforeEach
    void init() {
        mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
        // don't require preloaded data; tests will create what they need
    }

    @Test
    void whenGetAll_thenReturnList() {
        // Arrange - ensure at least one entity exists
        LotIdentifier id = new LotIdentifier(UUID.randomUUID().toString());
        Lot entity = new Lot(id, "Integration Loc", 777f, "77x77", LotStatus.AVAILABLE);
        lotRepository.save(entity);

        // Act & Assert
        webClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(LotResponseModel.class)
                .value(list -> {
                    assertNotNull(list);
                    assertTrue(list.size() >= 1);
                });
    }

    @Test
    void whenGetByIdExists_thenReturn() {
        String idVal = UUID.randomUUID().toString();
        LotIdentifier id = new LotIdentifier(idVal);
        Lot entity = new Lot(id, "ById Loc", 111f, "11x11", LotStatus.AVAILABLE);
        lotRepository.save(entity);

        webClient.get()
                .uri(BASE_URI + "/{id}", idVal)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LotResponseModel.class)
                .value(resp -> {
                    assertNotNull(resp);
                    assertEquals(idVal, resp.getLotId());
                    assertEquals("ById Loc", resp.getLocation());
                });
    }

    @Test
    void whenValidCreate_thenReturnCreated() {
        LotRequestModel req = new LotRequestModel();
        req.setLocation("Created Loc");
        req.setPrice(55f);
        req.setDimensions("5x5");
        req.setLotStatus(LotStatus.AVAILABLE);

        webClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LotResponseModel.class)
                .value(resp -> {
                    assertNotNull(resp.getLotId());
                    assertEquals(req.getLocation(), resp.getLocation());
                });
    }

    @Test
    void whenValidUpdate_thenReturnOk() {
        // create an entity to update
        String idVal = UUID.randomUUID().toString();
        LotIdentifier id = new LotIdentifier(idVal);
        Lot entity = new Lot(id, "Old Loc", 50f, "5x5", LotStatus.AVAILABLE);
        lotRepository.save(entity);

        LotRequestModel update = new LotRequestModel();
        update.setLocation("Updated Loc");
        update.setPrice(60f);
        update.setDimensions("6x6");
        update.setLotStatus(LotStatus.SOLD);

        webClient.put()
                .uri(BASE_URI + "/{id}", idVal)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LotResponseModel.class)
                .value(resp -> {
                    assertEquals(idVal, resp.getLotId());
                    assertEquals("Updated Loc", resp.getLocation());
                    assertEquals(LotStatus.SOLD, resp.getLotStatus());
                });
    }


    @Test
    void whenDeleteExists_thenNoContent() {
        String idVal = UUID.randomUUID().toString();
        LotIdentifier id = new LotIdentifier(idVal);
        Lot entity = new Lot(id, "ToDelete", 11f, "11x11", LotStatus.AVAILABLE);
        lotRepository.save(entity);

        webClient.delete()
                .uri(BASE_URI + "/{id}", idVal)
                .exchange()
                .expectStatus().isNoContent();

        // ensure it's removed
        Lot found = lotRepository.findByLotIdentifier_LotId(idVal);
        assertNull(found);
    }



    @Test
    void testInvalidInputExceptionConstructors() {
        InvalidInputException ex1 = new InvalidInputException();
        assertNull(ex1.getMessage());
        assertNull(ex1.getCause());

        String msg = "Invalid";
        InvalidInputException ex2 = new InvalidInputException(msg);
        assertEquals(msg, ex2.getMessage());

        RuntimeException cause = new RuntimeException("boom");
        InvalidInputException ex3 = new InvalidInputException(cause);
        assertSame(cause, ex3.getCause());

        String msg2 = "MsgCause";
        RuntimeException c2 = new RuntimeException("kaboom");
        InvalidInputException ex4 = new InvalidInputException(msg2, c2);
        assertEquals(msg2, ex4.getMessage());
        assertSame(c2, ex4.getCause());
    }

    @Test
    void testNotFoundExceptionConstructors() {
        NotFoundException ex1 = new NotFoundException();
        assertNull(ex1.getMessage());
        assertNull(ex1.getCause());

        String msg = "Not found";
        NotFoundException ex2 = new NotFoundException(msg);
        assertEquals(msg, ex2.getMessage());

        RuntimeException cause = new RuntimeException("boom");
        NotFoundException ex3 = new NotFoundException(cause);
        assertSame(cause, ex3.getCause());

        String msg2 = "MsgCause";
        RuntimeException c2 = new RuntimeException("kaboom");
        NotFoundException ex4 = new NotFoundException(msg2, c2);
        assertEquals(msg2, ex4.getMessage());
        assertSame(c2, ex4.getCause());
    }

    // Helper to build a request
    private LotRequestModel createLotRequestModel() {
        LotRequestModel req = new LotRequestModel();
        req.setLocation("Helper Loc");
        req.setPrice(29.99f);
        req.setDimensions("3x3");
        req.setLotStatus(LotStatus.AVAILABLE);
        return req;
    }
}
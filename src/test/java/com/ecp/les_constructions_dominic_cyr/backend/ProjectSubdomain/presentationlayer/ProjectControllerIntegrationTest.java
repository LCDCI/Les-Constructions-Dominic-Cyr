package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project.FileServiceClient;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersPostgresConfig.class)
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
class ProjectControllerIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    private LotRepository lotRepository;

    @MockBean
    private FileServiceClient fileServiceClient;

    private final String BASE_URI = "/api/v1/projects";

    @BeforeEach
    void setUp() {
        lotRepository.deleteAll();
        lotRepository.save(new Lot(new LotIdentifier("lot-001"), "Loc", 100f, "10x10", LotStatus.AVAILABLE));
        
        // Stub FileServiceClient to return true for any image identifier validation
        when(fileServiceClient.validateFileExists(anyString())).thenReturn(Mono.just(true));
    }

    @Test
    void whenValidCreate_thenReturnCreated() {

        ProjectRequestModel req = new ProjectRequestModel();
        req.setProjectName("Integration Project");
        req.setProjectDescription("Integration Desc");
        req.setStatus(ProjectStatus.IN_PROGRESS);
        req.setStartDate(LocalDate.of(2025, 1, 1));
        req.setEndDate(LocalDate.of(2025, 12, 31));
        req.setPrimaryColor("#FFFFFF");
        req.setTertiaryColor("#000000");
        req.setBuyerColor("#FF0000");
        req.setImageIdentifier("image-integration-001");
        req.setBuyerName("Buyer Name");
        req.setCustomerId("cust-001");
        req.setLotIdentifiers(java.util.List.of("lot-001"));
        req.setProgressPercentage(10);

        webClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProjectResponseModel.class)
                .value(resp -> {
                    org.junit.jupiter.api.Assertions.assertNotNull(resp);
                    org.junit.jupiter.api.Assertions.assertNotNull(resp.getProjectIdentifier());
                    org.junit.jupiter.api.Assertions.assertEquals("Integration Project", resp.getProjectName());
                });
    }

    @Test
    void whenStartDateAfterEndDate_thenBadRequest() {
        ProjectRequestModel req = new ProjectRequestModel();
        req.setProjectName("Invalid Dates");
        req.setProjectDescription("Invalid date range");
        req.setStatus(ProjectStatus.IN_PROGRESS);
        req.setStartDate(LocalDate.of(2026, 1, 1));
        req.setEndDate(LocalDate.of(2025, 1, 1));
        req.setPrimaryColor("#FFFFFF");
        req.setTertiaryColor("#000000");
        req.setBuyerColor("#FF0000");
        req.setImageIdentifier("image-integration-002");
        req.setBuyerName("Buyer Name");
        req.setCustomerId("cust-001");
        req.setLotIdentifiers(java.util.List.of("lot-001"));
        req.setProgressPercentage(10);

        webClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isBadRequest();
    }
}



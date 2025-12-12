package com.ecp.les_constructions_dominic_cyr.ProjectSubdomain.presentationlayer;

import static org.junit.jupiter.api.Assertions.*;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectResponseModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(com.ecp.les_constructions_dominic_cyr.config.TestcontainersPostgresConfig.class)
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
class ProjectControllerIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    ProjectRepository projectRepository;

    private final String BASE_URI = "/api/v1/projects";

    @Test
    void whenGetAll_thenReturnList() {
        // Arrange - ensure at least one entity exists
        Project entity = new Project();
        entity.setProjectIdentifier(UUID.randomUUID().toString());
        entity.setProjectName("Integration Project");
        entity.setProjectDescription("Integration Desc");
        entity.setStatus(ProjectStatus.IN_PROGRESS);
        entity.setStartDate(LocalDate.of(2025, 1, 1));
        entity.setEndDate(LocalDate.of(2025, 12, 31));
        entity.setPrimaryColor("#FFFFFF");
        entity.setTertiaryColor("#000000");
        entity.setBuyerColor("#FF0000");
        entity.setBuyerName("Integration Buyer");
        entity.setCustomerId("cust-integration");
        entity.setLotIdentifier("lot-integration");
        entity.setProgressPercentage(50);
        projectRepository.save(entity);

        // Act & Assert
        webClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProjectResponseModel.class)
                .value(list -> {
                    assertNotNull(list);
                    assertTrue(list.size() >= 1);
                });
    }

    @Test
    void whenGetByIdExists_thenReturn() {
        String idVal = UUID.randomUUID().toString();
        Project entity = new Project();
        entity.setProjectIdentifier(idVal);
        entity.setProjectName("ById Project");
        entity.setProjectDescription("ById Desc");
        entity.setStatus(ProjectStatus.PLANNED);
        entity.setStartDate(LocalDate.of(2025, 2, 1));
        entity.setEndDate(LocalDate.of(2025, 11, 30));
        entity.setPrimaryColor("#AAAAAA");
        entity.setTertiaryColor("#BBBBBB");
        entity.setBuyerColor("#CCCCCC");
        entity.setBuyerName("ById Buyer");
        entity.setCustomerId("cust-byid");
        entity.setLotIdentifier("lot-byid");
        entity.setProgressPercentage(25);
        projectRepository.save(entity);

        webClient.get()
                .uri(BASE_URI + "/{id}", idVal)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProjectResponseModel.class)
                .value(resp -> {
                    assertNotNull(resp);
                    assertEquals(idVal, resp.getProjectIdentifier());
                    assertEquals("ById Project", resp.getProjectName());
                    assertEquals("ById Desc", resp.getProjectDescription());
                    assertEquals(ProjectStatus.PLANNED, resp.getStatus());
                    assertEquals(LocalDate.of(2025, 2, 1), resp.getStartDate());
                    assertEquals(LocalDate.of(2025, 11, 30), resp.getEndDate());
                    assertEquals(25, resp.getProgressPercentage());
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
    void whenValidCreate_thenReturnCreated() {
        ProjectRequestModel req = new ProjectRequestModel();
        req.setProjectName("Created Project");
        req.setProjectDescription("Created Desc");
        req.setStatus(ProjectStatus.IN_PROGRESS);
        req.setStartDate(LocalDate.of(2025, 3, 1));
        req.setEndDate(LocalDate.of(2025, 10, 31));
        req.setPrimaryColor("#111111");
        req.setTertiaryColor("#222222");
        req.setBuyerColor("#333333");
        req.setBuyerName("Created Buyer");
        req.setCustomerId("cust-created");
        req.setLotIdentifier("lot-created");
        req.setProgressPercentage(10);

        webClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProjectResponseModel.class)
                .value(resp -> {
                    assertNotNull(resp.getProjectIdentifier());
                    assertEquals(req.getProjectName(), resp.getProjectName());
                    assertEquals(req.getProjectDescription(), resp.getProjectDescription());
                });
    }

    @Test
    void whenValidUpdate_thenReturnOk() {
        // create an entity to update
        String idVal = UUID.randomUUID().toString();
        Project entity = new Project();
        entity.setProjectIdentifier(idVal);
        entity.setProjectName("Old Project");
        entity.setProjectDescription("Old Desc");
        entity.setStatus(ProjectStatus.PLANNED);
        entity.setStartDate(LocalDate.of(2025, 1, 1));
        entity.setEndDate(LocalDate.of(2025, 12, 31));
        entity.setPrimaryColor("#000000");
        entity.setTertiaryColor("#FFFFFF");
        entity.setBuyerColor("#FF0000");
        entity.setBuyerName("Old Buyer");
        entity.setCustomerId("cust-old");
        entity.setLotIdentifier("lot-old");
        entity.setProgressPercentage(0);
        projectRepository.save(entity);

        ProjectRequestModel update = new ProjectRequestModel();
        update.setProjectName("Updated Project");
        update.setProjectDescription("Updated Desc");
        update.setStatus(ProjectStatus.COMPLETED);
        update.setStartDate(LocalDate.of(2025, 1, 1));
        update.setEndDate(LocalDate.of(2025, 12, 31));
        update.setPrimaryColor("#AAAAAA");
        update.setTertiaryColor("#BBBBBB");
        update.setBuyerColor("#CCCCCC");
        update.setBuyerName("Updated Buyer");
        update.setCustomerId("cust-updated");
        update.setLotIdentifier("lot-updated");
        update.setProgressPercentage(100);

        webClient.put()
                .uri(BASE_URI + "/{id}", idVal)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProjectResponseModel.class)
                .value(resp -> {
                    assertEquals(idVal, resp.getProjectIdentifier());
                    assertEquals("Updated Project", resp.getProjectName());
                    assertEquals(ProjectStatus.COMPLETED, resp.getStatus());
                    assertEquals(100, resp.getProgressPercentage());
                });
    }

    @Test
    void whenDeleteExists_thenNoContent() {
        String idVal = UUID.randomUUID().toString();
        Project entity = new Project();
        entity.setProjectIdentifier(idVal);
        entity.setProjectName("ToDelete");
        entity.setProjectDescription("ToDelete Desc");
        entity.setStatus(ProjectStatus.CANCELLED);
        entity.setStartDate(LocalDate.of(2025, 1, 1));
        entity.setEndDate(LocalDate.of(2025, 12, 31));
        entity.setPrimaryColor("#000000");
        entity.setTertiaryColor("#FFFFFF");
        entity.setBuyerColor("#FF0000");
        entity.setBuyerName("ToDelete Buyer");
        entity.setCustomerId("cust-delete");
        entity.setLotIdentifier("lot-delete");
        entity.setProgressPercentage(0);
        projectRepository.save(entity);

        webClient.delete()
                .uri(BASE_URI + "/{id}", idVal)
                .exchange()
                .expectStatus().isNoContent();

        // ensure it's removed
        assertTrue(projectRepository.findByProjectIdentifier(idVal).isEmpty());
    }
}

package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.mapperlayer;

import com.ecp.les_constructions_dominic_cyr. backend.ProjectSubdomain.DataAccessLayer.Project. Project;
import com. ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer. Project.ProjectStatus;
import com.ecp.les_constructions_dominic_cyr.backend. ProjectSubdomain. MapperLayer.ProjectMapper;
import com. ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectRequestModel;
import com.ecp.les_constructions_dominic_cyr. backend.ProjectSubdomain.PresentationLayer.Project.ProjectResponseModel;
import org.junit.jupiter. api.BeforeEach;
import org.junit. jupiter.api.Test;

import java.time. LocalDate;

import static org.junit.jupiter.api. Assertions.*;

public class ProjectMapperTest {

    private ProjectMapper projectMapper;

    @BeforeEach
    void setUp() {
        projectMapper = new ProjectMapper();
    }

    @Test
    void requestModelToEntity_WithAllFields_MapsCorrectly() {
        ProjectRequestModel requestModel = new ProjectRequestModel();
        requestModel. setProjectName("Test Project");
        requestModel.setProjectDescription("Test Description");
        requestModel.setStatus(ProjectStatus. IN_PROGRESS);
        requestModel. setStartDate(LocalDate.of(2025, 1, 1));
        requestModel.setEndDate(LocalDate. of(2025, 12, 31));
        requestModel.setCompletionDate(LocalDate.of(2025, 11, 30));
        requestModel.setPrimaryColor("#FFFFFF");
        requestModel.setTertiaryColor("#000000");
        requestModel.setBuyerColor("#FF0000");
        requestModel. setBuyerName("Test Buyer");
        requestModel.setImageIdentifier("img-001");
        requestModel.setCustomerId("cust-001");
        requestModel.setLotIdentifiers(java.util.List.of("lot-001"));
        requestModel.setProgressPercentage(50);

        Project result = projectMapper. requestModelToEntity(requestModel);

        assertNotNull(result);
        assertNotNull(result. getProjectIdentifier());
        assertEquals(36, result.getProjectIdentifier(). length());
        assertEquals("Test Project", result.getProjectName());
        assertEquals("Test Description", result.getProjectDescription());
        assertEquals(ProjectStatus. IN_PROGRESS, result.getStatus());
        assertEquals(LocalDate.of(2025, 1, 1), result. getStartDate());
        assertEquals(LocalDate.of(2025, 12, 31), result.getEndDate());
        assertEquals(LocalDate. of(2025, 11, 30), result.getCompletionDate());
        assertEquals("#FFFFFF", result.getPrimaryColor());
        assertEquals("#000000", result.getTertiaryColor());
        assertEquals("#FF0000", result.getBuyerColor());
        assertEquals("Test Buyer", result.getBuyerName());
        assertEquals("img-001", result.getImageIdentifier());
        assertEquals("cust-001", result.getCustomerId());
        assertNotNull(result.getLotIdentifiers());
        assertTrue(result.getLotIdentifiers().contains("lot-001"));
        assertEquals(50, result. getProgressPercentage());
    }

    @Test
    void requestModelToEntity_GeneratesUniqueIdentifiers() {
        ProjectRequestModel requestModel = new ProjectRequestModel();
        requestModel. setProjectName("Test");
        requestModel. setStatus(ProjectStatus. PLANNED);
        requestModel.setStartDate(LocalDate.now());
        requestModel.setPrimaryColor("#FFF");
        requestModel. setTertiaryColor("#000");
        requestModel.setBuyerColor("#F00");
        requestModel.setImageIdentifier("img-test");
        requestModel.setBuyerName("Buyer");
        requestModel.setCustomerId("cust");
        requestModel.setLotIdentifiers(java.util.List.of("lot"));

        Project result1 = projectMapper. requestModelToEntity(requestModel);
        Project result2 = projectMapper.requestModelToEntity(requestModel);

        assertNotEquals(result1. getProjectIdentifier(), result2.getProjectIdentifier());
    }

    @Test
    void entityToResponseModel_WithAllFields_MapsCorrectly() {
        Project project = new Project();
        project.setProjectIdentifier("proj-001");
        project.setProjectName("Test Project");
        project. setProjectDescription("Test Description");
        project.setStatus(ProjectStatus. COMPLETED);
        project. setStartDate(LocalDate.of(2025, 1, 1));
        project.setEndDate(LocalDate.of(2025, 12, 31));
        project.setCompletionDate(LocalDate.of(2025, 11, 30));
        project.setPrimaryColor("#FFFFFF");
        project.setTertiaryColor("#000000");
        project.setBuyerColor("#FF0000");
        project.setBuyerName("Test Buyer");
        project.setImageIdentifier("img-001");
        project.setCustomerId("cust-001");
        project.setLotIdentifiers(java.util.List.of("lot-001"));
        project.setProgressPercentage(100);

        ProjectResponseModel result = projectMapper.entityToResponseModel(project);

        assertNotNull(result);
        assertEquals("proj-001", result.getProjectIdentifier());
        assertEquals("Test Project", result.getProjectName());
        assertEquals("Test Description", result.getProjectDescription());
        assertEquals(ProjectStatus. COMPLETED, result. getStatus());
        assertEquals(LocalDate. of(2025, 1, 1), result.getStartDate());
        assertEquals(LocalDate.of(2025, 12, 31), result.getEndDate());
        assertEquals(LocalDate.of(2025, 11, 30), result.getCompletionDate());
        assertEquals("#FFFFFF", result.getPrimaryColor());
        assertEquals("#000000", result.getTertiaryColor());
        assertEquals("#FF0000", result. getBuyerColor());
        assertEquals("Test Buyer", result. getBuyerName());
        assertEquals("img-001", result. getImageIdentifier());
        assertEquals("cust-001", result.getCustomerId());
        assertNotNull(result.getLotIdentifiers());
        assertTrue(result.getLotIdentifiers().contains("lot-001"));
        assertEquals(100, result.getProgressPercentage());
    }

    @Test
    void entityToResponseModel_WithNullOptionalFields_MapsCorrectly() {
        Project project = new Project();
        project.setProjectIdentifier("proj-001");
        project.setProjectName("Test Project");
        project.setStatus(ProjectStatus. PLANNED);
        project. setStartDate(LocalDate.of(2025, 1, 1));
        project.setPrimaryColor("#FFFFFF");
        project.setTertiaryColor("#000000");
        project. setBuyerColor("#FF0000");
        project.setBuyerName("Test Buyer");
        project.setCustomerId("cust-001");
        project.setLotIdentifiers(java.util.List.of("lot-001"));

        ProjectResponseModel result = projectMapper.entityToResponseModel(project);

        assertNotNull(result);
        assertNull(result.getProjectDescription());
        assertNull(result.getEndDate());
        assertNull(result.getCompletionDate());
        assertNull(result.getImageIdentifier());
        assertNull(result.getProgressPercentage());
    }

    @Test
    void updateEntityFromRequestModel_WithAllFields_UpdatesEntity() {
        Project project = new Project();
        project.setProjectIdentifier("proj-001");
        project.setProjectName("Original Name");
        project.setStatus(ProjectStatus. PLANNED);

        ProjectRequestModel requestModel = new ProjectRequestModel();
        requestModel.setProjectName("Updated Name");
        requestModel.setProjectDescription("Updated Description");
        requestModel.setStatus(ProjectStatus.IN_PROGRESS);
        requestModel.setStartDate(LocalDate.of(2025, 6, 1));
        requestModel.setEndDate(LocalDate.of(2025, 12, 31));
        requestModel.setCompletionDate(LocalDate.of(2025, 11, 30));
        requestModel.setPrimaryColor("#111111");
        requestModel.setTertiaryColor("#222222");
        requestModel.setBuyerColor("#333333");
        requestModel. setBuyerName("New Buyer");
        requestModel.setImageIdentifier("new-img");
        requestModel.setCustomerId("new-cust");
        requestModel.setLotIdentifiers(java.util.List.of("new-lot"));
        requestModel.setProgressPercentage(75);

        projectMapper.updateEntityFromRequestModel(requestModel, project);

        assertEquals("Updated Name", project.getProjectName());
        assertEquals("Updated Description", project.getProjectDescription());
        assertEquals(ProjectStatus.IN_PROGRESS, project.getStatus());
        assertEquals(LocalDate.of(2025, 6, 1), project.getStartDate());
        assertEquals(LocalDate.of(2025, 12, 31), project.getEndDate());
        assertEquals(LocalDate.of(2025, 11, 30), project. getCompletionDate());
        assertEquals("#111111", project. getPrimaryColor());
        assertEquals("#222222", project.getTertiaryColor());
        assertEquals("#333333", project.getBuyerColor());
        assertEquals("New Buyer", project.getBuyerName());
        assertEquals("new-img", project.getImageIdentifier());
        assertEquals("new-cust", project.getCustomerId());
        assertNotNull(project.getLotIdentifiers());
        assertTrue(project.getLotIdentifiers().contains("new-lot"));
        assertEquals(75, project.getProgressPercentage());
    }

    @Test
    void updateEntityFromRequestModel_WithNullFields_PreservesOriginalValues() {
        Project project = new Project();
        project.setProjectIdentifier("proj-001");
        project.setProjectName("Original Name");
        project. setProjectDescription("Original Description");
        project.setStatus(ProjectStatus. PLANNED);
        project.setStartDate(LocalDate.of(2025, 1, 1));
        project.setPrimaryColor("#FFFFFF");
        project.setTertiaryColor("#000000");
        project. setBuyerColor("#FF0000");
        project.setBuyerName("Original Buyer");
        project.setCustomerId("original-cust");
        project.setLotIdentifier("original-lot");
        project.setProgressPercentage(50);

        ProjectRequestModel requestModel = new ProjectRequestModel();

        projectMapper.updateEntityFromRequestModel(requestModel, project);

        assertEquals("Original Name", project.getProjectName());
        assertEquals("Original Description", project.getProjectDescription());
        assertEquals(ProjectStatus.PLANNED, project.getStatus());
        assertEquals(LocalDate.of(2025, 1, 1), project.getStartDate());
        assertEquals("#FFFFFF", project. getPrimaryColor());
        assertEquals("Original Buyer", project.getBuyerName());
        assertEquals(50, project.getProgressPercentage());
    }

    @Test
    void updateEntityFromRequestModel_WithPartialUpdate_UpdatesOnlyProvidedFields() {
        Project project = new Project();
        project.setProjectName("Original Name");
        project.setStatus(ProjectStatus. PLANNED);
        project.setProgressPercentage(25);

        ProjectRequestModel requestModel = new ProjectRequestModel();
        requestModel.setProjectName("Updated Name");
        requestModel.setProgressPercentage(75);

        projectMapper.updateEntityFromRequestModel(requestModel, project);

        assertEquals("Updated Name", project.getProjectName());
        assertEquals(ProjectStatus. PLANNED, project. getStatus());
        assertEquals(75, project.getProgressPercentage());
    }
}
package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectStatus;
import com.ecp.les_constructions_dominic_cyr.backend.TestcontainersPostgresConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersPostgresConfig.class)
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
public class ProjectRepositoryIntegrationTest {

    @Autowired
    private ProjectRepository projectRepository;

    private Project testProject;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();

        testProject = new Project();
        testProject.setProjectIdentifier("proj-test-001");
        testProject.setProjectName("Test Project");
        testProject.setProjectDescription("Test Description");
        testProject.setStatus(ProjectStatus.IN_PROGRESS);
        testProject.setStartDate(LocalDate.of(2025, 1, 1));
        testProject.setEndDate(LocalDate.of(2025, 12, 31));
        testProject.setPrimaryColor("#FFFFFF");
        testProject.setTertiaryColor("#000000");
        testProject.setBuyerColor("#FF0000");
        testProject.setImageIdentifier("image-001");
        testProject.setBuyerName("Test Buyer");
        testProject.setCustomerId("cust-001");
        testProject.setLotIdentifiers(new java.util.ArrayList<>(java.util.List.of("lot-001")));
        testProject.setProgressPercentage(50);
    }

    @Test
    void save_WhenValidProject_SavesSuccessfully() {
        Project saved = projectRepository.save(testProject);

        assertNotNull(saved.getProjectId());
        assertEquals("proj-test-001", saved.getProjectIdentifier());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void findByProjectIdentifier_WhenExists_ReturnsProject() {
        projectRepository.save(testProject);

        Optional<Project> found = projectRepository.findByProjectIdentifier("proj-test-001");

        assertTrue(found.isPresent());
        assertEquals("Test Project", found.get().getProjectName());
    }

    @Test
    void findByProjectIdentifier_WhenNotExists_ReturnsEmpty() {
        Optional<Project> found = projectRepository.findByProjectIdentifier("non-existent");

        assertTrue(found.isEmpty());
    }

    @Test
    void findByStatus_WhenProjectsExist_ReturnsMatchingProjects() {
        projectRepository. save(testProject);

        Project completedProject = new Project();
        completedProject.setProjectIdentifier("proj-test-002");
        completedProject.setProjectName("Completed Project");
        completedProject.setStatus(ProjectStatus.COMPLETED);
        completedProject.setStartDate(LocalDate.of(2024, 1, 1));
        completedProject.setPrimaryColor("#FFF");
        completedProject.setTertiaryColor("#000");
        completedProject.setBuyerColor("#F00");
        completedProject.setImageIdentifier("image-002");
        completedProject.setBuyerName("Buyer");
        completedProject.setCustomerId("cust-002");
        completedProject.setLotIdentifiers(new java.util.ArrayList<>(java.util.List.of("lot-002")));
        projectRepository.save(completedProject);

        List<Project> inProgressProjects = projectRepository.findByStatus(ProjectStatus.IN_PROGRESS);
        List<Project> completedProjects = projectRepository.findByStatus(ProjectStatus.COMPLETED);

        assertEquals(1, inProgressProjects.size());
        assertEquals(1, completedProjects.size());
        assertEquals("Test Project", inProgressProjects.get(0).getProjectName());
        assertEquals("Completed Project", completedProjects.get(0).getProjectName());
    }

    @Test
    void findByStatus_WhenNoMatches_ReturnsEmptyList() {
        projectRepository.save(testProject);

        List<Project> cancelledProjects = projectRepository.findByStatus(ProjectStatus.CANCELLED);

        assertTrue(cancelledProjects.isEmpty());
    }

    @Test
    void findByCustomerId_WhenExists_ReturnsProjects() {
        projectRepository.save(testProject);

        List<Project> found = projectRepository.findByCustomerId("cust-001");

        assertEquals(1, found.size());
        assertEquals("Test Project", found.get(0).getProjectName());
    }

    @Test
    void findByCustomerId_WhenNotExists_ReturnsEmptyList() {
        projectRepository.save(testProject);

        List<Project> found = projectRepository.findByCustomerId("non-existent");

        assertTrue(found.isEmpty());
    }

    @Test
    void findByStartDateBetween_WhenInRange_ReturnsProjects() {
        projectRepository.save(testProject);

        List<Project> found = projectRepository.findByStartDateBetween(
                LocalDate.of(2024, 12, 1),
                LocalDate.of(2025, 6, 1)
        );

        assertEquals(1, found.size());
    }

    @Test
    void findByStartDateBetween_WhenOutOfRange_ReturnsEmptyList() {
        projectRepository. save(testProject);

        List<Project> found = projectRepository.findByStartDateBetween(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );

        assertTrue(found.isEmpty());
    }

    @Test
    void findByEndDateBetween_WhenInRange_ReturnsProjects() {
        projectRepository.save(testProject);

        List<Project> found = projectRepository.findByEndDateBetween(
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2026, 1, 1)
        );

        assertEquals(1, found.size());
    }

    @Test
    void delete_WhenExists_RemovesProject() {
        Project saved = projectRepository.save(testProject);
        Long id = saved.getProjectId();

        projectRepository.delete(saved);

        assertFalse(projectRepository.findById(id).isPresent());
    }

    @Test
    void findAll_ReturnsAllProjects() {
        projectRepository.save(testProject);

        Project secondProject = new Project();
        secondProject.setProjectIdentifier("proj-test-002");
        secondProject.setProjectName("Second Project");
        secondProject.setStatus(ProjectStatus.PLANNED);
        secondProject.setStartDate(LocalDate.of(2025, 6, 1));
        secondProject.setPrimaryColor("#FFF");
        secondProject.setTertiaryColor("#000");
        secondProject.setBuyerColor("#F00");
        secondProject.setImageIdentifier("image-003");
        secondProject.setBuyerName("Buyer");
        secondProject.setCustomerId("cust-002");
        secondProject.setLotIdentifiers(new java.util.ArrayList<>(java.util.List.of("lot-002")));
        projectRepository.save(secondProject);

        List<Project> all = projectRepository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void update_WhenExists_UpdatesProject() {
        Project saved = projectRepository.save(testProject);
        saved.setProjectName("Updated Name");
        saved.setProgressPercentage(100);

        Project updated = projectRepository.save(saved);

        assertEquals("Updated Name", updated.getProjectName());
        assertEquals(100, updated.getProgressPercentage());
    }
}
package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain. businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project.ProjectServiceImpl;
import com.ecp.les_constructions_dominic_cyr. backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com. ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer. Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend. ProjectSubdomain. DataAccessLayer.Project.ProjectStatus;
import com. ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.ProjectMapper;
import com. ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectRequestModel;
import com.ecp.les_constructions_dominic_cyr. backend.ProjectSubdomain.PresentationLayer.Project.ProjectResponseModel;
import com.ecp. les_constructions_dominic_cyr.backend.utils.Exception.InvalidProjectDataException;
import com.ecp.les_constructions_dominic_cyr. backend.utils.Exception.ProjectNotFoundException;
import jakarta.persistence.criteria.CriteriaQuery;
import org.junit.jupiter. api.BeforeEach;
import org.junit. jupiter.api.Test;
import org. junit.jupiter.api. extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org. mockito.InjectMocks;
import org.mockito. Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework. data.jpa.domain. Specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta. persistence.criteria. Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java. util.Arrays;
import java.util. Collections;
import java. util.List;
import java.util. Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org. mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
public class ProjectServiceUnitTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private Root<Project> root;

    @Mock
    private CriteriaQuery<? > criteriaQuery;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<Object> path;

    @Mock
    private Predicate predicate;

    @Mock
    private com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository lotRepository;

    @Mock
    private com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project.FileServiceClient fileServiceClient;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Captor
    private ArgumentCaptor<Specification<Project>> specificationCaptor;

    private Project testProject;
    private ProjectRequestModel testRequestModel;
    private ProjectResponseModel testResponseModel;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject. setProjectId(1L);
        testProject.setProjectIdentifier("proj-001");
        testProject.setProjectName("Test Project");
        testProject.setProjectDescription("Test Description");
        testProject.setStatus(ProjectStatus.IN_PROGRESS);
        testProject.setStartDate(LocalDate. now());
        testProject.setEndDate(LocalDate.now().plusMonths(6));
        testProject.setPrimaryColor("#FFFFFF");
        testProject.setTertiaryColor("#000000");
        testProject.setBuyerColor("#FF0000");
        testProject.setImageIdentifier("473f9e87-3415-491c-98a9-9d017c251911");
        testProject.setBuyerName("Test Buyer");
        testProject.setCustomerId("cust-001");
        testProject.setLotIdentifier("lot-001");
        testProject.setProgressPercentage(50);

        testRequestModel = new ProjectRequestModel();
        testRequestModel.setProjectName("Test Project");
        testRequestModel. setProjectDescription("Test Description");
        testRequestModel.setStatus(ProjectStatus.IN_PROGRESS);
        testRequestModel. setStartDate(LocalDate.now());
        testRequestModel. setEndDate(LocalDate.now().plusMonths(6));
        testRequestModel.setPrimaryColor("#FFFFFF");
        testRequestModel.setTertiaryColor("#000000");
        testRequestModel. setBuyerColor("#FF0000");
        testProject.setImageIdentifier("473f9e87-3415-491c-98a9-9d017c251911");
        testRequestModel. setBuyerName("Test Buyer");
        testRequestModel. setCustomerId("cust-001");
        testRequestModel.setLotIdentifiers(java.util.Arrays.asList("lot-001"));
        testRequestModel.setProgressPercentage(50);

        testResponseModel = ProjectResponseModel.builder()
                .projectIdentifier("proj-001")
                .projectName("Test Project")
                .projectDescription("Test Description")
                .status(ProjectStatus.IN_PROGRESS)
                . startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .primaryColor("#FFFFFF")
                .tertiaryColor("#000000")
                .buyerColor("#FF0000")
                .imageIdentifier("473f9e87-3415-491c-98a9-9d017c251911")
                .buyerName("Test Buyer")
                . customerId("cust-001")
                .lotIdentifiers(java.util.Arrays.asList("lot-001"))
                .progressPercentage(50)
                .build();

        // Stub lot existence validation to pass
        when(lotRepository.findByLotIdentifier_LotId(any())).thenReturn(
                new com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot(
                        new com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier("lot-001"),
                        "Loc", 100f, "10x10",
                        com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus.AVAILABLE
                )
        );
        // Default file validation to false (won't be used unless imageIdentifier set)
        when(fileServiceClient.validateFileExists(any())).thenReturn(reactor.core.publisher.Mono.just(true));
    }

    @Test
    void createProject_WithImageIdentifierExisting_Succeeds() {
        // Arrange
        testRequestModel.setImageIdentifier("img-123");
        when(projectMapper.requestModelToEntity(testRequestModel)).thenReturn(testProject);
        when(projectRepository.save(testProject)).thenReturn(testProject);
        when(projectMapper.entityToResponseModel(testProject)).thenReturn(testResponseModel);
        when(fileServiceClient.validateFileExists("img-123")).thenReturn(reactor.core.publisher.Mono.just(true));

        // Act
        ProjectResponseModel result = projectService.createProject(testRequestModel);

        // Assert
        assertNotNull(result);
        verify(fileServiceClient, times(1)).validateFileExists("img-123");
        verify(projectRepository, times(1)).save(testProject);
    }

    @Test
    void createProject_WithImageIdentifierMissing_ThrowsInvalidProjectDataException() {
        // Arrange
        testRequestModel.setImageIdentifier("missing-img");
        when(fileServiceClient.validateFileExists("missing-img")).thenReturn(reactor.core.publisher.Mono.just(false));

        // Act & Assert
        assertThrows(InvalidProjectDataException.class, () -> projectService.createProject(testRequestModel));
        verify(fileServiceClient, times(1)).validateFileExists("missing-img");
        verify(projectRepository, never()).save(any());
    }

    @Test
    void createProject_ImageIdentifierNullOrEmpty_DoesNotInvokeValidation() {
        // Arrange base stubs for happy path
        when(projectMapper.requestModelToEntity(any())).thenReturn(testProject);
        when(projectRepository.save(any())).thenReturn(testProject);
        when(projectMapper.entityToResponseModel(any())).thenReturn(testResponseModel);

        // Case: null
        testRequestModel.setImageIdentifier(null);
        ProjectResponseModel r1 = projectService.createProject(testRequestModel);
        assertNotNull(r1);
        verify(fileServiceClient, never()).validateFileExists(any());

        // Case: empty/blank
        testRequestModel.setImageIdentifier("   ");
        ProjectResponseModel r2 = projectService.createProject(testRequestModel);
        assertNotNull(r2);
        verify(fileServiceClient, never()).validateFileExists(any());

        // Case: literal "null" (case-insensitive)
        testRequestModel.setImageIdentifier("NuLl");
        ProjectResponseModel r3 = projectService.createProject(testRequestModel);
        assertNotNull(r3);
        verify(fileServiceClient, never()).validateFileExists(any());
    }

    @Test
    void createProject_WithLotIdentifiersContainingNull_ThrowsInvalidProjectDataException() {
        testRequestModel.setLotIdentifiers(java.util.Arrays.asList("lot-001", null));
        assertThrows(InvalidProjectDataException.class, () -> projectService.createProject(testRequestModel));
    }

    @Test
    void createProject_WithMissingLot_ThrowsNotFoundException() {
        when(lotRepository.findByLotIdentifier_LotId("missing-lot")).thenReturn(null);
        testRequestModel.setLotIdentifiers(java.util.Arrays.asList("missing-lot"));
        assertThrows(com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException.class,
                () -> projectService.createProject(testRequestModel));
    }

    @Test
    void updateProject_WithImageIdentifierExisting_Succeeds() {
        when(projectRepository.findByProjectIdentifier("proj-001")).thenReturn(java.util.Optional.ofNullable(testProject));
        testRequestModel.setImageIdentifier("img-123");
        when(fileServiceClient.validateFileExists("img-123")).thenReturn(reactor.core.publisher.Mono.just(true));
        when(projectRepository.save(testProject)).thenReturn(testProject);
        when(projectMapper.entityToResponseModel(testProject)).thenReturn(testResponseModel);

        ProjectResponseModel result = projectService.updateProject("proj-001", testRequestModel);

        assertNotNull(result);
        verify(fileServiceClient, times(1)).validateFileExists("img-123");
        verify(projectRepository, times(1)).save(testProject);
    }

    @Test
    void updateProject_WithImageIdentifierMissing_ThrowsInvalidProjectDataException() {
        when(projectRepository.findByProjectIdentifier("proj-001")).thenReturn(java.util.Optional.ofNullable(testProject));
        testRequestModel.setImageIdentifier("missing-img");
        when(fileServiceClient.validateFileExists("missing-img")).thenReturn(reactor.core.publisher.Mono.just(false));

        assertThrows(InvalidProjectDataException.class, () -> projectService.updateProject("proj-001", testRequestModel));
    }

    @Test
    @SuppressWarnings("unchecked")
    void filterProjects_SpecificationLambda_AllParams_Executes() {
        // Arrange
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(1);
        when(projectRepository.findAll(any(Specification.class))).thenReturn(java.util.Collections.emptyList());

        // Act
        projectService.filterProjects(ProjectStatus.IN_PROGRESS, start, end, "cust-1");

        // Capture and execute lambda
        org.mockito.ArgumentCaptor<Specification<Project>> captor = org.mockito.ArgumentCaptor.forClass(Specification.class);
        verify(projectRepository).findAll(captor.capture());
        Specification<Project> spec = captor.getValue();

        when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
        when(criteriaBuilder.greaterThanOrEqualTo(any(), any(LocalDate.class))).thenReturn(predicate);
        when(criteriaBuilder.lessThanOrEqualTo(any(), any(LocalDate.class))).thenReturn(predicate);
        when(criteriaBuilder.and(any(jakarta.persistence.criteria.Predicate[].class))).thenReturn(predicate);

        jakarta.persistence.criteria.Predicate built = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
        assertNotNull(built);
    }
    @Test
    void getAllProjects_WhenProjectsExist_ReturnsListOfProjects() {
        List<Project> projects = Arrays.asList(testProject);
        when(projectRepository. findAll()).thenReturn(projects);
        when(projectMapper.entityToResponseModel(testProject)).thenReturn(testResponseModel);

        List<ProjectResponseModel> result = projectService.getAllProjects();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("proj-001", result. get(0).getProjectIdentifier());
        verify(projectRepository, times(1)). findAll();
    }

    @Test
    void getAllProjects_WhenNoProjects_ReturnsEmptyList() {
        when(projectRepository.findAll()). thenReturn(Collections.emptyList());

        List<ProjectResponseModel> result = projectService.getAllProjects();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(projectRepository, times(1)).findAll();
    }

    @Test
    void getProjectByIdentifier_WhenProjectExists_ReturnsProject() {
        when(projectRepository.findByProjectIdentifier("proj-001")).thenReturn(Optional.of(testProject));
        when(projectMapper.entityToResponseModel(testProject)).thenReturn(testResponseModel);

        ProjectResponseModel result = projectService.getProjectByIdentifier("proj-001");

        assertNotNull(result);
        assertEquals("proj-001", result.getProjectIdentifier());
        verify(projectRepository, times(1)).findByProjectIdentifier("proj-001");
    }

    @Test
    void getProjectByIdentifier_WhenProjectNotFound_ThrowsException() {
        when(projectRepository.findByProjectIdentifier("invalid")). thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () ->
                projectService. getProjectByIdentifier("invalid")
        );
        verify(projectRepository, times(1)).findByProjectIdentifier("invalid");
    }

    @Test
    void createProject_WithValidData_ReturnsCreatedProject() {
        when(projectMapper.requestModelToEntity(testRequestModel)).thenReturn(testProject);
        when(projectRepository.save(testProject)).thenReturn(testProject);
        when(projectMapper. entityToResponseModel(testProject)).thenReturn(testResponseModel);

        ProjectResponseModel result = projectService. createProject(testRequestModel);

        assertNotNull(result);
        assertEquals("proj-001", result. getProjectIdentifier());
        verify(projectRepository, times(1)).save(testProject);
    }

    @Test
    void createProject_WithNullProjectName_ThrowsInvalidProjectDataException() {
        testRequestModel.setProjectName(null);

        assertThrows(InvalidProjectDataException. class, () ->
                projectService. createProject(testRequestModel)
        );
        verify(projectRepository, never()).save(any());
    }

    @Test
    void createProject_WithEmptyProjectName_ThrowsInvalidProjectDataException() {
        testRequestModel. setProjectName("   ");

        assertThrows(InvalidProjectDataException.class, () ->
                projectService.createProject(testRequestModel)
        );
        verify(projectRepository, never()).save(any());
    }

    @Test
    void createProject_WithNullStartDate_ThrowsInvalidProjectDataException() {
        testRequestModel.setStartDate(null);

        assertThrows(InvalidProjectDataException. class, () ->
                projectService. createProject(testRequestModel)
        );
        verify(projectRepository, never()).save(any());
    }

    @Test
    void createProject_WithStartDateAfterEndDate_ThrowsInvalidProjectDataException() {
        testRequestModel. setStartDate(LocalDate.now().plusMonths(12));
        testRequestModel.setEndDate(LocalDate.now());

        assertThrows(InvalidProjectDataException.class, () ->
                projectService.createProject(testRequestModel)
        );
        verify(projectRepository, never()). save(any());
    }

    @Test
    void createProject_WithNullStatus_ThrowsInvalidProjectDataException() {
        testRequestModel.setStatus(null);

        assertThrows(InvalidProjectDataException.class, () ->
                projectService.createProject(testRequestModel)
        );
        verify(projectRepository, never()).save(any());
    }

    @Test
    void createProject_WithNullEndDate_Succeeds() {
        testRequestModel.setEndDate(null);
        when(projectMapper.requestModelToEntity(testRequestModel)). thenReturn(testProject);
        when(projectRepository.save(testProject)).thenReturn(testProject);
        when(projectMapper.entityToResponseModel(testProject)).thenReturn(testResponseModel);

        ProjectResponseModel result = projectService.createProject(testRequestModel);

        assertNotNull(result);
        verify(projectRepository, times(1)).save(testProject);
    }

    @Test
    void updateProject_WithValidData_ReturnsUpdatedProject() {
        when(projectRepository.findByProjectIdentifier("proj-001")).thenReturn(Optional.of(testProject));
        when(projectRepository.save(testProject)).thenReturn(testProject);
        when(projectMapper. entityToResponseModel(testProject)).thenReturn(testResponseModel);

        ProjectResponseModel result = projectService.updateProject("proj-001", testRequestModel);

        assertNotNull(result);
        verify(projectMapper, times(1)). updateEntityFromRequestModel(testRequestModel, testProject);
        verify(projectRepository, times(1)).save(testProject);
    }

    @Test
    void updateProject_WhenProjectNotFound_ThrowsException() {
        when(projectRepository.findByProjectIdentifier("invalid")).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () ->
                projectService.updateProject("invalid", testRequestModel)
        );
        verify(projectRepository, never()).save(any());
    }

    @Test
    void updateProject_WithEmptyProjectName_ThrowsInvalidProjectDataException() {
        testRequestModel. setProjectName("");
        when(projectRepository.findByProjectIdentifier("proj-001")).thenReturn(Optional.of(testProject));

        assertThrows(InvalidProjectDataException.class, () ->
                projectService.updateProject("proj-001", testRequestModel)
        );
        verify(projectRepository, never()).save(any());
    }

    @Test
    void updateProject_WithStartDateAfterEndDate_ThrowsInvalidProjectDataException() {
        testRequestModel.setStartDate(LocalDate.now().plusYears(1));
        testRequestModel. setEndDate(LocalDate.now());
        when(projectRepository.findByProjectIdentifier("proj-001")).thenReturn(Optional.of(testProject));

        assertThrows(InvalidProjectDataException.class, () ->
                projectService.updateProject("proj-001", testRequestModel)
        );
    }

    @Test
    void updateProject_WithNullProjectName_Succeeds() {
        testRequestModel. setProjectName(null);
        when(projectRepository.findByProjectIdentifier("proj-001")).thenReturn(Optional.of(testProject));
        when(projectRepository.save(testProject)).thenReturn(testProject);
        when(projectMapper. entityToResponseModel(testProject)).thenReturn(testResponseModel);

        ProjectResponseModel result = projectService.updateProject("proj-001", testRequestModel);

        assertNotNull(result);
    }

    @Test
    void deleteProject_WhenProjectExists_DeletesSuccessfully() {
        when(projectRepository.findByProjectIdentifier("proj-001")).thenReturn(Optional.of(testProject));

        projectService.deleteProject("proj-001");

        verify(projectRepository, times(1)).delete(testProject);
    }


    @Test
    void getProjectsByStatus_ReturnsFilteredProjects() {
        List<Project> projects = Arrays.asList(testProject);
        when(projectRepository.findByStatus(ProjectStatus.IN_PROGRESS)).thenReturn(projects);
        when(projectMapper.entityToResponseModel(testProject)).thenReturn(testResponseModel);

        List<ProjectResponseModel> result = projectService.getProjectsByStatus(ProjectStatus.IN_PROGRESS);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(projectRepository, times(1)).findByStatus(ProjectStatus. IN_PROGRESS);
    }

    @Test
    void getProjectsByCustomerId_ReturnsFilteredProjects() {
        List<Project> projects = Arrays. asList(testProject);
        when(projectRepository.findByCustomerId("cust-001")).thenReturn(projects);
        when(projectMapper.entityToResponseModel(testProject)).thenReturn(testResponseModel);

        List<ProjectResponseModel> result = projectService.getProjectsByCustomerId("cust-001");

        assertNotNull(result);
        assertEquals(1, result. size());
        verify(projectRepository, times(1)).findByCustomerId("cust-001");
    }

    @Test
    void getProjectsByDateRange_ReturnsFilteredProjects() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusMonths(6);
        List<Project> projects = Arrays.asList(testProject);
        when(projectRepository. findByStartDateBetween(start, end)).thenReturn(projects);
        when(projectMapper.entityToResponseModel(testProject)).thenReturn(testResponseModel);

        List<ProjectResponseModel> result = projectService. getProjectsByDateRange(start, end);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(projectRepository, times(1)).findByStartDateBetween(start, end);
    }

    @Test
    @SuppressWarnings("unchecked")
    void filterProjects_WithAllParameters_ReturnsFilteredProjects() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusMonths(6);
        List<Project> projects = Arrays. asList(testProject);
        when(projectRepository.findAll(any(Specification. class))).thenReturn(projects);
        when(projectMapper.entityToResponseModel(testProject)).thenReturn(testResponseModel);

        List<ProjectResponseModel> result = projectService.filterProjects(
                ProjectStatus.IN_PROGRESS, start, end, "cust-001"
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(projectRepository, times(1)).findAll(any(Specification. class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void filterProjects_WithNullParameters_ReturnsAllProjects() {
        List<Project> projects = Arrays.asList(testProject);
        when(projectRepository. findAll(any(Specification.class))).thenReturn(projects);
        when(projectMapper.entityToResponseModel(testProject)).thenReturn(testResponseModel);

        List<ProjectResponseModel> result = projectService.filterProjects(null, null, null, null);

        assertNotNull(result);
        verify(projectRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void filterProjects_WithEmptyCustomerId_ReturnsProjects() {
        List<Project> projects = Arrays.asList(testProject);
        when(projectRepository.findAll(any(Specification.class))).thenReturn(projects);
        when(projectMapper. entityToResponseModel(testProject)).thenReturn(testResponseModel);

        List<ProjectResponseModel> result = projectService.filterProjects(null, null, null, "");

        assertNotNull(result);
        verify(projectRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void filterProjects_WithOnlyStatus_ReturnsFilteredProjects() {
        List<Project> projects = Arrays. asList(testProject);
        when(projectRepository.findAll(any(Specification.class))).thenReturn(projects);
        when(projectMapper.entityToResponseModel(testProject)). thenReturn(testResponseModel);

        List<ProjectResponseModel> result = projectService.filterProjects(
                ProjectStatus. COMPLETED, null, null, null
        );

        assertNotNull(result);
        verify(projectRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void filterProjects_WithOnlyStartDate_ReturnsFilteredProjects() {
        List<Project> projects = Arrays.asList(testProject);
        when(projectRepository.findAll(any(Specification.class))). thenReturn(projects);
        when(projectMapper.entityToResponseModel(testProject)).thenReturn(testResponseModel);

        List<ProjectResponseModel> result = projectService.filterProjects(
                null, LocalDate.now(), null, null
        );

        assertNotNull(result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void filterProjects_WithOnlyEndDate_ReturnsFilteredProjects() {
        List<Project> projects = Arrays. asList(testProject);
        when(projectRepository.findAll(any(Specification.class))).thenReturn(projects);
        when(projectMapper.entityToResponseModel(testProject)). thenReturn(testResponseModel);

        List<ProjectResponseModel> result = projectService.filterProjects(
                null, null, LocalDate.now(). plusMonths(6), null
        );

        assertNotNull(result);
    }

    @Test
    void deleteProject_LambdaThrowsException_VerifyExceptionMessage() {
        String projectId = "test-project-id";
        when(projectRepository.findByProjectIdentifier(projectId))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(ProjectNotFoundException.class, () -> {
            projectService.deleteProject(projectId);
        });

        assertEquals("Project not found with identifier: " + projectId, exception.getMessage());
    }


    @Test
    @SuppressWarnings("unchecked")
    void filterProjects_WithStartDateOnly_BuildsCorrectSpecification() {
        List<Project> projects = Arrays.asList(testProject);
        when(projectRepository.findAll(any(Specification.class))).thenReturn(projects);
        when(projectMapper.entityToResponseModel(testProject)). thenReturn(testResponseModel);

        List<ProjectResponseModel> result = projectService. filterProjects(
                null, LocalDate.now(), null, null
        );

        assertNotNull(result);
        verify(projectRepository).findAll(any(Specification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void filterProjects_WithEndDateOnly_BuildsCorrectSpecification() {
        List<Project> projects = Arrays.asList(testProject);
        when(projectRepository.findAll(any(Specification.class))). thenReturn(projects);
        when(projectMapper.entityToResponseModel(testProject)).thenReturn(testResponseModel);

        List<ProjectResponseModel> result = projectService.filterProjects(
                null, null, LocalDate. now().plusMonths(6), null
        );

        assertNotNull(result);
        verify(projectRepository).findAll(any(Specification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void filterProjects_WithCustomerIdOnly_BuildsCorrectSpecification() {
        List<Project> projects = Arrays.asList(testProject);
        when(projectRepository.findAll(any(Specification. class))).thenReturn(projects);
        when(projectMapper.entityToResponseModel(testProject)).thenReturn(testResponseModel);

        List<ProjectResponseModel> result = projectService. filterProjects(
                null, null, null, "cust-001"
        );

        assertNotNull(result);
        verify(projectRepository).findAll(any(Specification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void filterProjects_WithEmptyCustomerId_DoesNotAddPredicate() {
        List<Project> projects = Arrays.asList(testProject);
        when(projectRepository.findAll(any(Specification. class))).thenReturn(projects);
        when(projectMapper.entityToResponseModel(testProject)).thenReturn(testResponseModel);

        List<ProjectResponseModel> result = projectService. filterProjects(
                null, null, null, ""
        );

        assertNotNull(result);
        verify(projectRepository). findAll(any(Specification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void filterProjects_WithAllParametersNull_ReturnsAllProjects() {
        List<Project> projects = Arrays.asList(testProject);
        when(projectRepository.findAll(any(Specification. class))).thenReturn(projects);
        when(projectMapper.entityToResponseModel(testProject)).thenReturn(testResponseModel);

        List<ProjectResponseModel> result = projectService. filterProjects(null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(projectRepository).findAll(any(Specification. class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void filterProjects_WithAllParameters_BuildsCompleteSpecification() {
        List<Project> projects = Arrays. asList(testProject);
        when(projectRepository.findAll(any(Specification.class))).thenReturn(projects);
        when(projectMapper.entityToResponseModel(testProject)). thenReturn(testResponseModel);

        List<ProjectResponseModel> result = projectService.filterProjects(
                ProjectStatus. COMPLETED,
                LocalDate. of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                "customer-123"
        );

        assertNotNull(result);
        verify(projectRepository).findAll(any(Specification.class));
    }


    @Test
    @SuppressWarnings("unchecked")
    void filterProjects_WithDifferentStatuses_HandlesCorrectly() {
        when(projectRepository.findAll(any(Specification.class))). thenReturn(Collections.emptyList());

        for (ProjectStatus status : ProjectStatus.values()) {
            List<ProjectResponseModel> result = projectService.filterProjects(status, null, null, null);
            assertNotNull(result);
        }

        verify(projectRepository, times(ProjectStatus.values().length)).findAll(any(Specification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void filterProjects_ReturnsEmptyList_WhenNoMatches() {
        when(projectRepository. findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        List<ProjectResponseModel> result = projectService.filterProjects(
                ProjectStatus. CANCELLED, null, null, null
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void filterProjects_WithMultipleProjects_ReturnsAll() {
        Project project2 = new Project();
        project2. setProjectIdentifier("proj-002");
        project2.setProjectName("Project 2");

        ProjectResponseModel response2 = ProjectResponseModel.builder()
                .projectIdentifier("proj-002")
                .projectName("Project 2")
                .build();

        List<Project> projects = Arrays. asList(testProject, project2);
        when(projectRepository.findAll(any(Specification. class))).thenReturn(projects);
        when(projectMapper.entityToResponseModel(testProject)).thenReturn(testResponseModel);
        when(projectMapper.entityToResponseModel(project2)).thenReturn(response2);

        List<ProjectResponseModel> result = projectService.filterProjects(null, null, null, null);

        assertEquals(2, result. size());
    }
}
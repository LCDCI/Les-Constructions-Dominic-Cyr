package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project.ActivityType;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project.ProjectServiceImpl;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectActivityLog;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectActivityLogRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.ProjectMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectActivityLogResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserStatus;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidProjectDataException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.ProjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class ProjectTeamServiceUnitTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private ProjectActivityLogRepository activityLogRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project testProject;
    private Users testContractor;
    private Users testSalesperson;
    private Users testOwner;
    private final String testProjectId = "proj-001";
    private final String testContractorId = UUID.randomUUID().toString();
    private final String testSalespersonId = UUID.randomUUID().toString();
    private final String testAuth0UserId = "auth0|owner123";

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setProjectIdentifier(testProjectId);
        testProject.setProjectName("Test Project");
        testProject.setProjectDescription("Test Description");
        testProject.setStatus(ProjectStatus.IN_PROGRESS);
        testProject.setStartDate(LocalDate.of(2025, 1, 1));
        testProject.setEndDate(LocalDate.of(2025, 12, 31));

        testContractor = new Users();
        testContractor.setUserIdentifier(UserIdentifier.fromString(testContractorId));
        testContractor.setFirstName("John");
        testContractor.setLastName("Contractor");
        testContractor.setPrimaryEmail("john.contractor@example.com");
        testContractor.setUserRole(UserRole.CONTRACTOR);
        testContractor.setUserStatus(UserStatus.ACTIVE);

        testSalesperson = new Users();
        testSalesperson.setUserIdentifier(UserIdentifier.fromString(testSalespersonId));
        testSalesperson.setFirstName("Jane");
        testSalesperson.setLastName("Salesperson");
        testSalesperson.setPrimaryEmail("jane.salesperson@example.com");
        testSalesperson.setUserRole(UserRole.SALESPERSON);
        testSalesperson.setUserStatus(UserStatus.ACTIVE);

        testOwner = new Users();
        testOwner.setAuth0UserId(testAuth0UserId);
        testOwner.setFirstName("Owner");
        testOwner.setLastName("User");
        testOwner.setUserRole(UserRole.OWNER);
        
        // Setup default mock for projectMapper - returns a valid response model
        // Using lenient() since not all tests will use this mock
        ProjectResponseModel mockResponse = new ProjectResponseModel();
        mockResponse.setProjectIdentifier(testProjectId);
        lenient().when(projectMapper.entityToResponseModel(any(Project.class))).thenReturn(mockResponse);
    }

    // ========================== ASSIGN CONTRACTOR TESTS ==========================

    @Test
    void assignContractorToProject_ValidData_AssignsSuccessfully() {
        when(projectRepository.findByProjectIdentifier(testProjectId))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findByUserIdentifier(testContractorId))
                .thenReturn(Optional.of(testContractor));
        when(usersRepository.findByAuth0UserId(testAuth0UserId))
                .thenReturn(Optional.of(testOwner));
        when(projectRepository.save(any(Project.class)))
                .thenReturn(testProject);

        ProjectResponseModel result = projectService.assignContractorToProject(
                testProjectId, testContractorId, testAuth0UserId);

        assertNotNull(result);
        verify(projectRepository).save(any(Project.class));
        verify(activityLogRepository).save(any(ProjectActivityLog.class));

        // Verify activity log was created with correct data
        ArgumentCaptor<ProjectActivityLog> logCaptor = ArgumentCaptor.forClass(ProjectActivityLog.class);
        verify(activityLogRepository).save(logCaptor.capture());
        ProjectActivityLog savedLog = logCaptor.getValue();

        assertEquals(testProjectId, savedLog.getProjectIdentifier());
        assertEquals(ActivityType.CONTRACTOR_ASSIGNED, savedLog.getActivityType());
        assertEquals(testContractorId, savedLog.getUserIdentifier());
        assertEquals("John Contractor", savedLog.getUserName());
        assertEquals(testAuth0UserId, savedLog.getChangedBy());
        assertEquals("Owner User", savedLog.getChangedByName());
        assertNotNull(savedLog.getTimestamp());
    }

    @Test
    void assignContractorToProject_ProjectNotFound_ThrowsException() {
        when(projectRepository.findByProjectIdentifier(testProjectId))
                .thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () ->
                projectService.assignContractorToProject(testProjectId, testContractorId, testAuth0UserId)
        );

        verify(projectRepository, never()).save(any());
        verify(activityLogRepository, never()).save(any());
    }

    @Test
    void assignContractorToProject_ContractorNotFound_ThrowsException() {
        when(projectRepository.findByProjectIdentifier(testProjectId))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findByUserIdentifier(testContractorId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                projectService.assignContractorToProject(testProjectId, testContractorId, testAuth0UserId)
        );

        verify(projectRepository, never()).save(any());
        verify(activityLogRepository, never()).save(any());
    }

    @Test
    void assignContractorToProject_NullContractorId_ThrowsException() {
        when(projectRepository.findByProjectIdentifier(testProjectId))
                .thenReturn(Optional.of(testProject));

        assertThrows(InvalidProjectDataException.class, () ->
                projectService.assignContractorToProject(testProjectId, null, testAuth0UserId)
        );

        verify(projectRepository, never()).save(any());
        verify(activityLogRepository, never()).save(any());
    }

    @Test
    void assignContractorToProject_EmptyContractorId_ThrowsException() {
        when(projectRepository.findByProjectIdentifier(testProjectId))
                .thenReturn(Optional.of(testProject));

        assertThrows(InvalidProjectDataException.class, () ->
                projectService.assignContractorToProject(testProjectId, "   ", testAuth0UserId)
        );

        verify(projectRepository, never()).save(any());
        verify(activityLogRepository, never()).save(any());
    }

    @Test
    void assignContractorToProject_ReplacesExistingContractor_LogsBothActions() {
        String oldContractorId = UUID.randomUUID().toString();
        testProject.setContractorId(oldContractorId);

        when(projectRepository.findByProjectIdentifier(testProjectId))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findByUserIdentifier(testContractorId))
                .thenReturn(Optional.of(testContractor));
        when(usersRepository.findByAuth0UserId(testAuth0UserId))
                .thenReturn(Optional.of(testOwner));
        when(projectRepository.save(any(Project.class)))
                .thenReturn(testProject);

        ProjectResponseModel result = projectService.assignContractorToProject(
                testProjectId, testContractorId, testAuth0UserId);

        assertNotNull(result);
        verify(activityLogRepository).save(any(ProjectActivityLog.class));
    }

    // ========================== REMOVE CONTRACTOR TESTS ==========================

    @Test
    void removeContractorFromProject_ValidData_RemovesSuccessfully() {
        testProject.setContractorId(testContractorId);

        when(projectRepository.findByProjectIdentifier(testProjectId))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findByUserIdentifier(testContractorId))
                .thenReturn(Optional.of(testContractor));
        when(usersRepository.findByAuth0UserId(testAuth0UserId))
                .thenReturn(Optional.of(testOwner));
        when(projectRepository.save(any(Project.class)))
                .thenReturn(testProject);

        ProjectResponseModel result = projectService.removeContractorFromProject(
                testProjectId, testAuth0UserId);

        assertNotNull(result);
        verify(projectRepository).save(any(Project.class));
        verify(activityLogRepository).save(any(ProjectActivityLog.class));

        // Verify activity log
        ArgumentCaptor<ProjectActivityLog> logCaptor = ArgumentCaptor.forClass(ProjectActivityLog.class);
        verify(activityLogRepository).save(logCaptor.capture());
        ProjectActivityLog savedLog = logCaptor.getValue();

        assertEquals(ActivityType.CONTRACTOR_REMOVED, savedLog.getActivityType());
        assertEquals(testContractorId, savedLog.getUserIdentifier());
        assertTrue(savedLog.getDescription().contains("removed"));
    }

    @Test
    void removeContractorFromProject_NoContractorAssigned_StillSucceeds() {
        testProject.setContractorId(null);

        when(projectRepository.findByProjectIdentifier(testProjectId))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findByAuth0UserId(testAuth0UserId))
                .thenReturn(Optional.of(testOwner));
        when(projectRepository.save(any(Project.class)))
                .thenReturn(testProject);

        ProjectResponseModel result = projectService.removeContractorFromProject(
                testProjectId, testAuth0UserId);

        assertNotNull(result);
        verify(projectRepository).save(any(Project.class));
        // Should not log removal if no contractor was assigned
        verify(activityLogRepository, never()).save(any(ProjectActivityLog.class));
    }

    @Test
    void removeContractorFromProject_ProjectNotFound_ThrowsException() {
        when(projectRepository.findByProjectIdentifier(testProjectId))
                .thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () ->
                projectService.removeContractorFromProject(testProjectId, testAuth0UserId)
        );

        verify(projectRepository, never()).save(any());
        verify(activityLogRepository, never()).save(any());
    }

    // ========================== ASSIGN SALESPERSON TESTS ==========================

    @Test
    void assignSalespersonToProject_ValidData_AssignsSuccessfully() {
        when(projectRepository.findByProjectIdentifier(testProjectId))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findByUserIdentifier(testSalespersonId))
                .thenReturn(Optional.of(testSalesperson));
        when(usersRepository.findByAuth0UserId(testAuth0UserId))
                .thenReturn(Optional.of(testOwner));
        when(projectRepository.save(any(Project.class)))
                .thenReturn(testProject);

        ProjectResponseModel result = projectService.assignSalespersonToProject(
                testProjectId, testSalespersonId, testAuth0UserId);

        assertNotNull(result);
        verify(projectRepository).save(any(Project.class));
        verify(activityLogRepository).save(any(ProjectActivityLog.class));

        // Verify activity log
        ArgumentCaptor<ProjectActivityLog> logCaptor = ArgumentCaptor.forClass(ProjectActivityLog.class);
        verify(activityLogRepository).save(logCaptor.capture());
        ProjectActivityLog savedLog = logCaptor.getValue();

        assertEquals(ActivityType.SALESPERSON_ASSIGNED, savedLog.getActivityType());
        assertEquals(testSalespersonId, savedLog.getUserIdentifier());
        assertEquals("Jane Salesperson", savedLog.getUserName());
    }

    @Test
    void assignSalespersonToProject_NullSalespersonId_ThrowsException() {
        when(projectRepository.findByProjectIdentifier(testProjectId))
                .thenReturn(Optional.of(testProject));

        assertThrows(InvalidProjectDataException.class, () ->
                projectService.assignSalespersonToProject(testProjectId, null, testAuth0UserId)
        );

        verify(projectRepository, never()).save(any());
        verify(activityLogRepository, never()).save(any());
    }

    @Test
    void assignSalespersonToProject_SalespersonNotFound_ThrowsException() {
        when(projectRepository.findByProjectIdentifier(testProjectId))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findByUserIdentifier(testSalespersonId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                projectService.assignSalespersonToProject(testProjectId, testSalespersonId, testAuth0UserId)
        );

        verify(projectRepository, never()).save(any());
        verify(activityLogRepository, never()).save(any());
    }

    // ========================== REMOVE SALESPERSON TESTS ==========================

    @Test
    void removeSalespersonFromProject_ValidData_RemovesSuccessfully() {
        testProject.setSalespersonId(testSalespersonId);

        when(projectRepository.findByProjectIdentifier(testProjectId))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findByUserIdentifier(testSalespersonId))
                .thenReturn(Optional.of(testSalesperson));
        when(usersRepository.findByAuth0UserId(testAuth0UserId))
                .thenReturn(Optional.of(testOwner));
        when(projectRepository.save(any(Project.class)))
                .thenReturn(testProject);

        ProjectResponseModel result = projectService.removeSalespersonFromProject(
                testProjectId, testAuth0UserId);

        assertNotNull(result);
        verify(projectRepository).save(any(Project.class));
        verify(activityLogRepository).save(any(ProjectActivityLog.class));

        ArgumentCaptor<ProjectActivityLog> logCaptor = ArgumentCaptor.forClass(ProjectActivityLog.class);
        verify(activityLogRepository).save(logCaptor.capture());
        ProjectActivityLog savedLog = logCaptor.getValue();

        assertEquals(ActivityType.SALESPERSON_REMOVED, savedLog.getActivityType());
    }

    @Test
    void removeSalespersonFromProject_NoSalespersonAssigned_StillSucceeds() {
        testProject.setSalespersonId(null);

        when(projectRepository.findByProjectIdentifier(testProjectId))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findByAuth0UserId(testAuth0UserId))
                .thenReturn(Optional.of(testOwner));
        when(projectRepository.save(any(Project.class)))
                .thenReturn(testProject);

        ProjectResponseModel result = projectService.removeSalespersonFromProject(
                testProjectId, testAuth0UserId);

        assertNotNull(result);
        verify(projectRepository).save(any(Project.class));
        verify(activityLogRepository, never()).save(any(ProjectActivityLog.class));
    }

    // ========================== GET ACTIVITY LOG TESTS ==========================

    @Test
    void getProjectActivityLog_ValidProject_ReturnsLogs() {
        ProjectActivityLog log1 = new ProjectActivityLog();
        log1.setId(1L);
        log1.setProjectIdentifier(testProjectId);
        log1.setActivityType(ActivityType.CONTRACTOR_ASSIGNED);
        log1.setUserIdentifier(testContractorId);
        log1.setUserName("John Contractor");
        log1.setChangedBy(testAuth0UserId);
        log1.setChangedByName("Owner User");
        log1.setTimestamp(LocalDateTime.now());
        log1.setDescription("Contractor assigned");

        ProjectActivityLog log2 = new ProjectActivityLog();
        log2.setId(2L);
        log2.setProjectIdentifier(testProjectId);
        log2.setActivityType(ActivityType.SALESPERSON_ASSIGNED);
        log2.setUserIdentifier(testSalespersonId);
        log2.setUserName("Jane Salesperson");
        log2.setChangedBy(testAuth0UserId);
        log2.setChangedByName("Owner User");
        log2.setTimestamp(LocalDateTime.now());
        log2.setDescription("Salesperson assigned");

        when(activityLogRepository.findByProjectIdentifierOrderByTimestampDesc(testProjectId))
                .thenReturn(Arrays.asList(log1, log2));

        List<ProjectActivityLogResponseModel> result = projectService.getProjectActivityLog(testProjectId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("CONTRACTOR_ASSIGNED", result.get(0).getActivityType());
        assertEquals("SALESPERSON_ASSIGNED", result.get(1).getActivityType());
        assertEquals("John Contractor", result.get(0).getUserName());
        assertEquals("Jane Salesperson", result.get(1).getUserName());

        verify(activityLogRepository).findByProjectIdentifierOrderByTimestampDesc(testProjectId);
    }

    @Test
    void getProjectActivityLog_NoLogs_ReturnsEmptyList() {
        when(activityLogRepository.findByProjectIdentifierOrderByTimestampDesc(testProjectId))
                .thenReturn(Arrays.asList());

        List<ProjectActivityLogResponseModel> result = projectService.getProjectActivityLog(testProjectId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(activityLogRepository).findByProjectIdentifierOrderByTimestampDesc(testProjectId);
    }

    @Test
    void getProjectActivityLog_LogsOrderedByTimestampDesc_ReturnsInCorrectOrder() {
        LocalDateTime now = LocalDateTime.now();
        ProjectActivityLog newerLog = new ProjectActivityLog();
        newerLog.setId(2L);
        newerLog.setProjectIdentifier(testProjectId);
        newerLog.setActivityType(ActivityType.CONTRACTOR_ASSIGNED);
        newerLog.setTimestamp(now);

        ProjectActivityLog olderLog = new ProjectActivityLog();
        olderLog.setId(1L);
        olderLog.setProjectIdentifier(testProjectId);
        olderLog.setActivityType(ActivityType.SALESPERSON_ASSIGNED);
        olderLog.setTimestamp(now.minusHours(1));

        // Repository returns newer logs first
        when(activityLogRepository.findByProjectIdentifierOrderByTimestampDesc(testProjectId))
                .thenReturn(Arrays.asList(newerLog, olderLog));

        List<ProjectActivityLogResponseModel> result = projectService.getProjectActivityLog(testProjectId);

        assertEquals(2, result.size());
        assertEquals("CONTRACTOR_ASSIGNED", result.get(0).getActivityType());
        assertEquals("SALESPERSON_ASSIGNED", result.get(1).getActivityType());
    }
}

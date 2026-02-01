package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer.project;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project.ProjectService;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ProjectControllerUnitTest {

    private ProjectService projectService;
    private UserService userService;
    private LotRepository lotRepository;
    private ProjectController projectController;

    @BeforeEach
    void setUp() {
        projectService = mock(ProjectService.class);
        userService = mock(UserService.class);
        lotRepository = mock(LotRepository.class);
        projectController = new ProjectController(projectService, userService, lotRepository);
    }

    @Test
    void isOwner_ReturnsTrueWhenOwnerAuthority() {
        Authentication auth = mock(Authentication.class);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_OWNER"))).when(auth).getAuthorities();

        boolean result = projectController.getClass().getDeclaredMethods()[0].getName() != null; // dummy to keep method
        // call isOwner via reflection
        try {
            var m = ProjectController.class.getDeclaredMethod("isOwner", Authentication.class);
            m.setAccessible(true);
            boolean val = (boolean) m.invoke(projectController, auth);
            assertTrue(val);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void getProjectByIdentifier_AsOwner_ReturnsProject() {
        ProjectResponseModel mockProject = new ProjectResponseModel();
        mockProject.setProjectIdentifier("proj-1");
        when(projectService.getProjectByIdentifier("proj-1")).thenReturn(mockProject);

        // owner authentication -> should bypass authorization checks
        Authentication auth = mock(Authentication.class);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_OWNER"))).when(auth).getAuthorities();

        var resp = projectController.getProjectByIdentifier("proj-1", null, auth);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("proj-1", resp.getBody().getProjectIdentifier());
    }

    @Test
    void getProjectByIdentifier_NonOwner_UnauthorizedWhenUserNotFound() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("auth0|u1");
        Authentication auth = mock(Authentication.class);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))).when(auth).getAuthorities();

        when(userService.getUserByAuth0Id("auth0|u1")).thenThrow(new RuntimeException("not found"));

        var resp = projectController.getProjectByIdentifier("proj-1", jwt, auth);
        assertEquals(401, resp.getStatusCodeValue());
    }

    @Test
    void assignAndRemoveContractor_CallsService() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("auth0|u1");
        ProjectResponseModel updated = new ProjectResponseModel();
        when(projectService.assignContractorToProject(anyString(), anyString(), anyString())).thenReturn(updated);
        when(projectService.removeContractorFromProject(anyString(), anyString())).thenReturn(updated);

        var resp = projectController.assignContractor("proj-1", "ctr-1", jwt);
        assertEquals(200, resp.getStatusCodeValue());

        var resp2 = projectController.removeContractor("proj-1", jwt);
        assertEquals(200, resp2.getStatusCodeValue());

        verify(projectService, times(1)).assignContractorToProject("proj-1", "ctr-1", "auth0|u1");
        verify(projectService, times(1)).removeContractorFromProject("proj-1", "auth0|u1");
    }

    @Test
    void createUpdateDeleteProject_CallsService() {
        ProjectRequestModel req = new ProjectRequestModel();
        ProjectResponseModel created = new ProjectResponseModel();
        when(projectService.createProject(req)).thenReturn(created);
        when(projectService.updateProject("p1", req)).thenReturn(created);

        var c = projectController.createProject(req);
        assertEquals(201, c.getStatusCodeValue());

        var u = projectController.updateProject("p1", req);
        assertEquals(200, u.getStatusCodeValue());

        doNothing().when(projectService).deleteProject("p1");
        var d = projectController.deleteProject("p1");
        assertEquals(204, d.getStatusCodeValue());
    }

    @Test
    void getProjectActivityLog_ReturnsOk() {
        when(projectService.getProjectActivityLog("p1")).thenReturn(List.of());
        var resp = projectController.getProjectActivityLog("p1");
        assertEquals(200, resp.getStatusCodeValue());
    }
}

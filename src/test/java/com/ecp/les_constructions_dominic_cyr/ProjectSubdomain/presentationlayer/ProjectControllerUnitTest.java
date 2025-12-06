package com. ecp.les_constructions_dominic_cyr.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project.ProjectService;
import com. ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer. Project.ProjectStatus;
import com. ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectController;
import com. ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectRequestModel;
import com.ecp.les_constructions_dominic_cyr. backend.ProjectSubdomain.PresentationLayer.Project.ProjectResponseModel;
import com.fasterxml.jackson. databind.ObjectMapper;
import com.fasterxml. jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api. BeforeEach;
import org.junit.jupiter.api. Test;
import org.springframework.beans. factory.annotation. Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet. WebMvcTest;
import org.springframework.http.MediaType;
import org. springframework.test.context.bean.override. mockito.MockitoBean;
import org.springframework.test.web.servlet. MockMvc;

import java.time.LocalDate;
import java.util. Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers. any;
import static org.mockito. ArgumentMatchers.eq;
import static org.mockito. Mockito.*;
import static org. springframework.test.web.servlet.request. MockMvcRequestBuilders.*;
import static org.springframework.test.web. servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
public class ProjectControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    private ObjectMapper objectMapper;
    private ProjectResponseModel testResponseModel;
    private ProjectRequestModel testRequestModel;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testResponseModel = ProjectResponseModel.builder()
                .projectIdentifier("proj-001")
                . projectName("Test Project")
                .projectDescription("Test Description")
                .status(ProjectStatus.IN_PROGRESS)
                .startDate(LocalDate.of(2025, 1, 1))
                . endDate(LocalDate.of(2025, 12, 31))
                . primaryColor("#FFFFFF")
                .tertiaryColor("#000000")
                . buyerColor("#FF0000")
                .buyerName("Test Buyer")
                .customerId("cust-001")
                . lotIdentifier("lot-001")
                .progressPercentage(50)
                .build();

        testRequestModel = new ProjectRequestModel();
        testRequestModel. setProjectName("Test Project");
        testRequestModel.setProjectDescription("Test Description");
        testRequestModel.setStatus(ProjectStatus.IN_PROGRESS);
        testRequestModel.setStartDate(LocalDate. of(2025, 1, 1));
        testRequestModel.setEndDate(LocalDate.of(2025, 12, 31));
        testRequestModel.setPrimaryColor("#FFFFFF");
        testRequestModel.setTertiaryColor("#000000");
        testRequestModel. setBuyerColor("#FF0000");
        testRequestModel. setBuyerName("Test Buyer");
        testRequestModel. setCustomerId("cust-001");
        testRequestModel. setLotIdentifier("lot-001");
        testRequestModel.setProgressPercentage(50);
    }

    @Test
    void getAllProjects_WithNoFilters_ReturnsAllProjects() throws Exception {
        List<ProjectResponseModel> projects = Arrays.asList(testResponseModel);
        when(projectService.getAllProjects()).thenReturn(projects);

        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                . andExpect(jsonPath("$[0].projectIdentifier").value("proj-001"))
                . andExpect(jsonPath("$[0].projectName").value("Test Project"));

        verify(projectService, times(1)). getAllProjects();
        verify(projectService, never()).filterProjects(any(), any(), any(), any());
    }

    @Test
    void getAllProjects_WithStatusFilter_ReturnsFilteredProjects() throws Exception {
        List<ProjectResponseModel> projects = Arrays. asList(testResponseModel);
        when(projectService.filterProjects(eq(ProjectStatus.IN_PROGRESS), any(), any(), any()))
                .thenReturn(projects);

        mockMvc. perform(get("/api/v1/projects")
                        .param("status", "IN_PROGRESS"))
                .andExpect(status(). isOk())
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));

        verify(projectService, times(1)).filterProjects(eq(ProjectStatus. IN_PROGRESS), any(), any(), any());
    }

    @Test
    void getAllProjects_WithDateFilters_ReturnsFilteredProjects() throws Exception {
        List<ProjectResponseModel> projects = Arrays.asList(testResponseModel);
        when(projectService.filterProjects(any(), any(), any(), any())).thenReturn(projects);

        mockMvc.perform(get("/api/v1/projects")
                        .param("startDate", "2025-01-01")
                        . param("endDate", "2025-12-31"))
                .andExpect(status().isOk());

        verify(projectService, times(1)).filterProjects(any(), any(), any(), any());
    }

    @Test
    void getAllProjects_WithCustomerIdFilter_ReturnsFilteredProjects() throws Exception {
        List<ProjectResponseModel> projects = Arrays. asList(testResponseModel);
        when(projectService.filterProjects(any(), any(), any(), eq("cust-001"))).thenReturn(projects);

        mockMvc.perform(get("/api/v1/projects")
                        .param("customerId", "cust-001"))
                .andExpect(status().isOk());

        verify(projectService, times(1)).filterProjects(any(), any(), any(), eq("cust-001"));
    }

    @Test
    void getAllProjects_WithAllFilters_ReturnsFilteredProjects() throws Exception {
        List<ProjectResponseModel> projects = Arrays.asList(testResponseModel);
        when(projectService. filterProjects(any(), any(), any(), any())).thenReturn(projects);

        mockMvc. perform(get("/api/v1/projects")
                        . param("status", "IN_PROGRESS")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31")
                        .param("customerId", "cust-001"))
                .andExpect(status().isOk());

        verify(projectService, times(1)).filterProjects(
                eq(ProjectStatus. IN_PROGRESS),
                eq(LocalDate.of(2025, 1, 1)),
                eq(LocalDate.of(2025, 12, 31)),
                eq("cust-001")
        );
    }

    @Test
    void getAllProjects_WhenEmpty_ReturnsEmptyList() throws Exception {
        when(projectService.getAllProjects()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                . andExpect(jsonPath("$").isArray())
                . andExpect(jsonPath("$"). isEmpty());
    }

    @Test
    void getProjectByIdentifier_WhenExists_ReturnsProject() throws Exception {
        when(projectService.getProjectByIdentifier("proj-001")).thenReturn(testResponseModel);

        mockMvc. perform(get("/api/v1/projects/proj-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectIdentifier").value("proj-001"))
                .andExpect(jsonPath("$.projectName").value("Test Project"));

        verify(projectService, times(1)). getProjectByIdentifier("proj-001");
    }

    @Test
    void createProject_WithValidData_ReturnsCreatedProject() throws Exception {
        when(projectService.createProject(any(ProjectRequestModel.class))). thenReturn(testResponseModel);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType. APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestModel)))
                . andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectIdentifier"). value("proj-001"));

        verify(projectService, times(1)).createProject(any(ProjectRequestModel.class));
    }

    @Test
    void updateProject_WithValidData_ReturnsUpdatedProject() throws Exception {
        when(projectService.updateProject(eq("proj-001"), any(ProjectRequestModel.class)))
                .thenReturn(testResponseModel);

        mockMvc.perform(put("/api/v1/projects/proj-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        . content(objectMapper. writeValueAsString(testRequestModel)))
                .andExpect(status().isOk())
                . andExpect(jsonPath("$.projectIdentifier").value("proj-001"));

        verify(projectService, times(1)).updateProject(eq("proj-001"), any(ProjectRequestModel.class));
    }

    @Test
    void deleteProject_WhenExists_ReturnsNoContent() throws Exception {
        doNothing().when(projectService).deleteProject("proj-001");

        mockMvc.perform(delete("/api/v1/projects/proj-001"))
                .andExpect(status(). isNoContent());

        verify(projectService, times(1)). deleteProject("proj-001");
    }
}
package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project.ProjectService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectActivityLogResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.ProjectNotFoundException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.GlobalControllerExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc
@Import(GlobalControllerExceptionHandler.class)
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
public class ProjectTeamControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    private ObjectMapper objectMapper;
    private ProjectResponseModel testProjectResponse;
    private final String testProjectId = "proj-001";
    private final String testContractorId = "contractor-001";
    private final String testSalespersonId = "salesperson-001";
    private final String testCustomerId = "customer-001";
    private final String testAuth0UserId = "auth0|123456";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testProjectResponse = ProjectResponseModel.builder()
                .projectIdentifier(testProjectId)
                .projectName("Test Project")
                .projectDescription("Test Description")
                .status(ProjectStatus.IN_PROGRESS)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .primaryColor("#FFFFFF")
                .tertiaryColor("#000000")
                .buyerColor("#FF0000")
                .buyerName("Test Buyer")
                .customerId("cust-001")
                .contractorIds(Arrays.asList(testContractorId))
                .salespersonIds(Arrays.asList(testSalespersonId))
                .lotIdentifiers(Arrays.asList("lot-001"))
                .progressPercentage(50)
                .build();
    }

    // ========================== ASSIGN CONTRACTOR TESTS ==========================

    @Test
    void assignContractorToProject_ValidRequest_ReturnsOk() throws Exception {
        when(projectService.assignContractorToProject(eq(testProjectId), eq(testContractorId), any()))
                .thenReturn(testProjectResponse);

        mockMvc.perform(put("/api/v1/projects/{projectIdentifier}/contractor", testProjectId)
                        .param("contractorId", testContractorId)
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectIdentifier").value(testProjectId))
                .andExpect(jsonPath("$.contractorIds[0]").value(testContractorId));

        verify(projectService, times(1)).assignContractorToProject(eq(testProjectId), eq(testContractorId), any());
    }

    @Test
    void assignContractorToProject_ProjectNotFound_Returns404() throws Exception {
        when(projectService.assignContractorToProject(eq(testProjectId), eq(testContractorId), any()))
                .thenThrow(new ProjectNotFoundException("Project not found"));

        mockMvc.perform(put("/api/v1/projects/{projectIdentifier}/contractor", testProjectId)
                        .param("contractorId", testContractorId)
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).assignContractorToProject(eq(testProjectId), eq(testContractorId), any());
    }

    @Test
    void assignContractorToProject_ContractorNotFound_Returns404() throws Exception {
        when(projectService.assignContractorToProject(eq(testProjectId), eq("invalid-contractor"), any()))
                .thenThrow(new NotFoundException("Contractor not found"));

        mockMvc.perform(put("/api/v1/projects/{projectIdentifier}/contractor", testProjectId)
                        .param("contractorId", "invalid-contractor")
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).assignContractorToProject(eq(testProjectId), eq("invalid-contractor"), any());
    }

    // ========================== REMOVE CONTRACTOR TESTS ==========================

    @Test
    void removeContractorFromProject_ValidRequest_ReturnsOk() throws Exception {
        ProjectResponseModel responseWithoutContractor = ProjectResponseModel.builder()
                .projectIdentifier(testProjectId)
                .projectName("Test Project")
                .contractorIds(new ArrayList<>())
                .build();

        when(projectService.removeContractorFromProject(eq(testProjectId), any()))
                .thenReturn(responseWithoutContractor);

        mockMvc.perform(delete("/api/v1/projects/{projectIdentifier}/contractor", testProjectId)
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectIdentifier").value(testProjectId))
                .andExpect(jsonPath("$.contractorIds").isEmpty());

        verify(projectService, times(1)).removeContractorFromProject(eq(testProjectId), any());
    }

    @Test
    void removeContractorFromProject_ProjectNotFound_Returns404() throws Exception {
        when(projectService.removeContractorFromProject(eq("invalid-project"), any()))
                .thenThrow(new ProjectNotFoundException("Project not found"));

        mockMvc.perform(delete("/api/v1/projects/{projectIdentifier}/contractor", "invalid-project")
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).removeContractorFromProject(eq("invalid-project"), any());
    }

    @Test
    void removeContractorFromProject_NoContractorAssigned_ReturnsOk() throws Exception {
        ProjectResponseModel responseWithoutContractor = ProjectResponseModel.builder()
                .projectIdentifier(testProjectId)
                .contractorIds(new ArrayList<>())
                .build();

        when(projectService.removeContractorFromProject(eq(testProjectId), any()))
                .thenReturn(responseWithoutContractor);

        mockMvc.perform(delete("/api/v1/projects/{projectIdentifier}/contractor", testProjectId)
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(projectService, times(1)).removeContractorFromProject(eq(testProjectId), any());
    }

    // ========================== ASSIGN SALESPERSON TESTS ==========================

    @Test
    void assignSalespersonToProject_ValidRequest_ReturnsOk() throws Exception {
        when(projectService.assignSalespersonToProject(eq(testProjectId), eq(testSalespersonId), any()))
                .thenReturn(testProjectResponse);

        mockMvc.perform(put("/api/v1/projects/{projectIdentifier}/salesperson", testProjectId)
                        .param("salespersonId", testSalespersonId)
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectIdentifier").value(testProjectId))
                .andExpect(jsonPath("$.salespersonIds[0]").value(testSalespersonId));

        verify(projectService, times(1)).assignSalespersonToProject(eq(testProjectId), eq(testSalespersonId), any());
    }

    @Test
    void assignSalespersonToProject_ProjectNotFound_Returns404() throws Exception {
        when(projectService.assignSalespersonToProject(eq(testProjectId), eq(testSalespersonId), any()))
                .thenThrow(new ProjectNotFoundException("Project not found"));

        mockMvc.perform(put("/api/v1/projects/{projectIdentifier}/salesperson", testProjectId)
                        .param("salespersonId", testSalespersonId)
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).assignSalespersonToProject(eq(testProjectId), eq(testSalespersonId), any());
    }

    @Test
    void assignSalespersonToProject_SalespersonNotFound_Returns404() throws Exception {
        when(projectService.assignSalespersonToProject(eq(testProjectId), eq("invalid-salesperson"), any()))
                .thenThrow(new NotFoundException("Salesperson not found"));

        mockMvc.perform(put("/api/v1/projects/{projectIdentifier}/salesperson", testProjectId)
                        .param("salespersonId", "invalid-salesperson")
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).assignSalespersonToProject(eq(testProjectId), eq("invalid-salesperson"), any());
    }

    // ========================== REMOVE SALESPERSON TESTS ==========================

    @Test
    void removeSalespersonFromProject_ValidRequest_ReturnsOk() throws Exception {
        ProjectResponseModel responseWithoutSalesperson = ProjectResponseModel.builder()
                .projectIdentifier(testProjectId)
                .projectName("Test Project")
                .salespersonIds(new ArrayList<>())
                .build();

        when(projectService.removeSalespersonFromProject(eq(testProjectId), any()))
                .thenReturn(responseWithoutSalesperson);

        mockMvc.perform(delete("/api/v1/projects/{projectIdentifier}/salesperson", testProjectId)
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectIdentifier").value(testProjectId))
                .andExpect(jsonPath("$.salespersonIds").isEmpty());

        verify(projectService, times(1)).removeSalespersonFromProject(eq(testProjectId), any());
    }

    @Test
    void removeSalespersonFromProject_ProjectNotFound_Returns404() throws Exception {
        when(projectService.removeSalespersonFromProject(eq("invalid-project"), any()))
                .thenThrow(new ProjectNotFoundException("Project not found"));

        mockMvc.perform(delete("/api/v1/projects/{projectIdentifier}/salesperson", "invalid-project")
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).removeSalespersonFromProject(eq("invalid-project"), any());
    }

    // ========================== GET ACTIVITY LOG TESTS ==========================

    @Test
    void getProjectActivityLog_ValidProject_ReturnsActivityLog() throws Exception {
        List<ProjectActivityLogResponseModel> activityLogs = Arrays.asList(
                new ProjectActivityLogResponseModel(
                        1L,
                        testProjectId,
                        "CONTRACTOR_ASSIGNED",
                        testContractorId,
                        "John Contractor",
                        testAuth0UserId,
                        "Owner User",
                        LocalDateTime.now(),
                        "John Contractor was assigned as contractor"
                ),
                new ProjectActivityLogResponseModel(
                        2L,
                        testProjectId,
                        "SALESPERSON_ASSIGNED",
                        testSalespersonId,
                        "Jane Salesperson",
                        testAuth0UserId,
                        "Owner User",
                        LocalDateTime.now(),
                        "Jane Salesperson was assigned as salesperson"
                )
        );

        when(projectService.getProjectActivityLog(eq(testProjectId)))
                .thenReturn(activityLogs);

        mockMvc.perform(get("/api/v1/projects/{projectIdentifier}/activity-log", testProjectId)
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].activityType").value("CONTRACTOR_ASSIGNED"))
                .andExpect(jsonPath("$[0].userName").value("John Contractor"))
                .andExpect(jsonPath("$[1].activityType").value("SALESPERSON_ASSIGNED"))
                .andExpect(jsonPath("$[1].userName").value("Jane Salesperson"));

        verify(projectService, times(1)).getProjectActivityLog(eq(testProjectId));
    }

    @Test
    void getProjectActivityLog_EmptyLog_ReturnsEmptyArray() throws Exception {
        when(projectService.getProjectActivityLog(eq(testProjectId)))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/v1/projects/{projectIdentifier}/activity-log", testProjectId)
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(projectService, times(1)).getProjectActivityLog(eq(testProjectId));
    }

    @Test
    void getProjectActivityLog_InvalidProject_Returns404() throws Exception {
        when(projectService.getProjectActivityLog(eq("invalid-project")))
                .thenThrow(new ProjectNotFoundException("Project not found"));

        mockMvc.perform(get("/api/v1/projects/{projectIdentifier}/activity-log", "invalid-project")
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).getProjectActivityLog(eq("invalid-project"));
    }

    // ========================== ASSIGN CUSTOMER TESTS ==========================

    @Test
    void assignCustomerToProject_ValidRequest_ReturnsOk() throws Exception {
        String testCustomerId = "customer-001";
        ProjectResponseModel responseWithCustomer = ProjectResponseModel.builder()
                .projectIdentifier(testProjectId)
                .projectName("Test Project")
                .customerId(testCustomerId)
                .build();

        when(projectService.assignCustomerToProject(eq(testProjectId), eq(testCustomerId), any()))
                .thenReturn(responseWithCustomer);

        mockMvc.perform(put("/api/v1/projects/{projectIdentifier}/customer", testProjectId)
                        .param("customerId", testCustomerId)
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectIdentifier").value(testProjectId))
                .andExpect(jsonPath("$.customerId").value(testCustomerId));

        verify(projectService, times(1)).assignCustomerToProject(eq(testProjectId), eq(testCustomerId), any());
    }

    @Test
    void assignCustomerToProject_ProjectNotFound_Returns404() throws Exception {
        String testCustomerId = "customer-001";
        when(projectService.assignCustomerToProject(eq(testProjectId), eq(testCustomerId), any()))
                .thenThrow(new ProjectNotFoundException("Project not found"));

        mockMvc.perform(put("/api/v1/projects/{projectIdentifier}/customer", testProjectId)
                        .param("customerId", testCustomerId)
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).assignCustomerToProject(eq(testProjectId), eq(testCustomerId), any());
    }

    @Test
    void assignCustomerToProject_CustomerNotFound_Returns404() throws Exception {
        when(projectService.assignCustomerToProject(eq(testProjectId), eq("invalid-customer"), any()))
                .thenThrow(new NotFoundException("Customer not found"));

        mockMvc.perform(put("/api/v1/projects/{projectIdentifier}/customer", testProjectId)
                        .param("customerId", "invalid-customer")
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).assignCustomerToProject(eq(testProjectId), eq("invalid-customer"), any());
    }

    @Test
    void assignCustomerToProject_ReplacesExistingCustomer_ReturnsOk() throws Exception {
        String newCustomerId = "customer-002";
        ProjectResponseModel responseWithNewCustomer = ProjectResponseModel.builder()
                .projectIdentifier(testProjectId)
                .projectName("Test Project")
                .customerId(newCustomerId)
                .build();

        when(projectService.assignCustomerToProject(eq(testProjectId), eq(newCustomerId), any()))
                .thenReturn(responseWithNewCustomer);

        mockMvc.perform(put("/api/v1/projects/{projectIdentifier}/customer", testProjectId)
                        .param("customerId", newCustomerId)
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(newCustomerId));

        verify(projectService, times(1)).assignCustomerToProject(eq(testProjectId), eq(newCustomerId), any());
    }

    // ========================== REMOVE CUSTOMER TESTS ==========================

    @Test
    void removeCustomerFromProject_ValidRequest_ReturnsOk() throws Exception {
        ProjectResponseModel responseWithoutCustomer = ProjectResponseModel.builder()
                .projectIdentifier(testProjectId)
                .projectName("Test Project")
                .customerId(null)
                .build();

        when(projectService.removeCustomerFromProject(eq(testProjectId), any()))
                .thenReturn(responseWithoutCustomer);

        mockMvc.perform(delete("/api/v1/projects/{projectIdentifier}/customer", testProjectId)
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectIdentifier").value(testProjectId));

        verify(projectService, times(1)).removeCustomerFromProject(eq(testProjectId), any());
    }

    @Test
    void removeCustomerFromProject_ProjectNotFound_Returns404() throws Exception {
        when(projectService.removeCustomerFromProject(eq("invalid-project"), any()))
                .thenThrow(new ProjectNotFoundException("Project not found"));

        mockMvc.perform(delete("/api/v1/projects/{projectIdentifier}/customer", "invalid-project")
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).removeCustomerFromProject(eq("invalid-project"), any());
    }

    @Test
    void removeCustomerFromProject_NoCustomerAssigned_ReturnsOk() throws Exception {
        ProjectResponseModel responseWithoutCustomer = ProjectResponseModel.builder()
                .projectIdentifier(testProjectId)
                .customerId(null)
                .build();

        when(projectService.removeCustomerFromProject(eq(testProjectId), any()))
                .thenReturn(responseWithoutCustomer);

        mockMvc.perform(delete("/api/v1/projects/{projectIdentifier}/customer", testProjectId)
                        .with(jwt().jwt(j -> j.subject(testAuth0UserId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(projectService, times(1)).removeCustomerFromProject(eq(testProjectId), any());
    }
}

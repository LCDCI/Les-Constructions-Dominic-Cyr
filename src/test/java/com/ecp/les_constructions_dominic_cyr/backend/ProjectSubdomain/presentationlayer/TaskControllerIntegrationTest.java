package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Task.Task;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Task.TaskRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Task.TaskRequestDTO;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Auth0ManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersPostgresConfig.class)
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private Auth0ManagementService auth0ManagementService;

    @MockitoBean
    private UserService userService;

    private final SimpleGrantedAuthority OWNER_ROLE = new SimpleGrantedAuthority("ROLE_OWNER");
    private final SimpleGrantedAuthority CONTRACTOR_ROLE = new SimpleGrantedAuthority("ROLE_CONTRACTOR");

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        Task task1 = new Task();
        task1.setTaskIdentifier("TASK-TEST-001");
        task1.setTaskDate(startOfWeek);
        task1.setTaskDescription("Foundation Work");
        task1.setLotNumber("Lot 53");
        task1.setDayOfWeek("Monday");
        task1.setAssignedTo("contractor-123");

        Task task2 = new Task();
        task2.setTaskIdentifier("TASK-TEST-002");
        task2.setTaskDate(startOfWeek.plusDays(1));
        task2.setTaskDescription("Framing");
        task2.setLotNumber("Lot 57");
        task2.setDayOfWeek("Tuesday");
        task2.setAssignedTo("contractor-123");

        Task task3 = new Task();
        task3.setTaskIdentifier("TASK-TEST-003");
        task3.setTaskDate(startOfWeek.plusDays(10));
        task3.setTaskDescription("Future Task");
        task3.setLotNumber("Lot 99");
        task3.setDayOfWeek("Thursday");
        task3.setAssignedTo("contractor-456");

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);

        // Mock UserService to return contractor role
        UserResponseModel contractorUser = new UserResponseModel();
        contractorUser.setUserRole(UserRole.CONTRACTOR);
        when(userService.getUserById(anyString())).thenReturn(contractorUser);
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();
    }

    @Test
    void getOwnerAllTasks_shouldReturnAllTasks() throws Exception {
        mockMvc.perform(get("/api/v1/owners/tasks/all")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].taskIdentifier",
                        containsInAnyOrder("TASK-TEST-001", "TASK-TEST-002", "TASK-TEST-003")));
    }

    @Test
    void getOwnerTaskByIdentifier_shouldReturnTaskWhenExists() throws Exception {
        mockMvc.perform(get("/api/v1/owners/tasks/TASK-TEST-001")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskIdentifier").value("TASK-TEST-001"))
                .andExpect(jsonPath("$.taskDescription").value("Foundation Work"))
                .andExpect(jsonPath("$.lotNumber").value("Lot 53"))
                .andExpect(jsonPath("$.assignedTo").value("contractor-123"));
    }

    @Test
    void getOwnerTaskByIdentifier_shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/owners/tasks/TASK-INVALID")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTask_shouldCreateNewTask() throws Exception {
        TaskRequestDTO requestDTO = TaskRequestDTO.builder()
                .taskDate(LocalDate.now())
                .taskDescription("New Task")
                .lotNumber("Lot 100")
                .dayOfWeek("Wednesday")
                .assignedTo("contractor-789")
                .build();

        mockMvc.perform(post("/api/v1/owners/tasks")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskIdentifier").exists())
                .andExpect(jsonPath("$.taskDescription").value("New Task"))
                .andExpect(jsonPath("$.lotNumber").value("Lot 100"))
                .andExpect(jsonPath("$.assignedTo").value("contractor-789"));
    }

    @Test
    void updateTask_shouldUpdateExistingTask() throws Exception {
        TaskRequestDTO requestDTO = TaskRequestDTO.builder()
                .taskDate(LocalDate.now().plusDays(5))
                .taskDescription("Updated Task")
                .lotNumber("Lot 200")
                .dayOfWeek("Friday")
                .assignedTo("contractor-999")
                .build();

        mockMvc.perform(put("/api/v1/owners/tasks/TASK-TEST-001")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskIdentifier").value("TASK-TEST-001"))
                .andExpect(jsonPath("$.taskDescription").value("Updated Task"))
                .andExpect(jsonPath("$.lotNumber").value("Lot 200"))
                .andExpect(jsonPath("$.assignedTo").value("contractor-999"));
    }

    @Test
    void updateTask_shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        TaskRequestDTO requestDTO = TaskRequestDTO.builder()
                .taskDate(LocalDate.now())
                .taskDescription("Updated Task")
                .lotNumber("Lot 100")
                .dayOfWeek("Wednesday")
                .assignedTo("contractor-123")
                .build();

        mockMvc.perform(put("/api/v1/owners/tasks/TASK-INVALID")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_shouldDeleteExistingTask() throws Exception {
        mockMvc.perform(delete("/api/v1/owners/tasks/TASK-TEST-001")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verify task is deleted
        mockMvc.perform(get("/api/v1/owners/tasks/TASK-TEST-001")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/v1/owners/tasks/TASK-INVALID")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getContractorTasks_shouldReturnOnlyAssignedTasks() throws Exception {
        mockMvc.perform(get("/api/v1/contractors/tasks/all")
                        .param("contractorId", "contractor-123")
                        .with(jwt().authorities(CONTRACTOR_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].taskIdentifier",
                        containsInAnyOrder("TASK-TEST-001", "TASK-TEST-002")));
    }

    @Test
    void getContractorTaskByIdentifier_shouldReturnTask() throws Exception {
        mockMvc.perform(get("/api/v1/contractors/tasks/TASK-TEST-001")
                        .with(jwt().authorities(CONTRACTOR_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskIdentifier").value("TASK-TEST-001"))
                .andExpect(jsonPath("$.assignedTo").value("contractor-123"));
    }
}

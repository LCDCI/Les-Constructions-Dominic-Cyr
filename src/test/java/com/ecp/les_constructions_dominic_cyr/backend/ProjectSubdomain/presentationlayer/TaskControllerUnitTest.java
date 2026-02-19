package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule.TaskService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskPriority;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskDetailResponseDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskRequestDTO;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerUnitTest {

    @Mock
    private TaskService taskService;

    @Mock
    private UserService userService;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private TaskController taskController;

    private TaskDetailResponseDTO taskResponseDTO1;
    private TaskDetailResponseDTO taskResponseDTO2;
    private TaskRequestDTO taskRequestDTO;
    private UserResponseModel customerUser;
    private UserResponseModel salespersonUser;

    @BeforeEach
    void setUp() {
        taskResponseDTO1 = TaskDetailResponseDTO.builder()
                .taskId("TASK-001")
                .taskStatus(TaskStatus.IN_PROGRESS)
                .taskTitle("Foundation Work")
                .periodStart(new Date())
                .periodEnd(new Date())
                .taskDescription("Complete foundation work")
                .taskPriority(TaskPriority.HIGH)
                .estimatedHours(40.0)
                .hoursSpent(20.0)
                .taskProgress(50.0)
                .assignedToUserId("user-123")
                .assignedToUserName("John Doe")
                .scheduleId("SCH-001")
                .build();

        taskResponseDTO2 = TaskDetailResponseDTO.builder()
                .taskId("TASK-002")
                .taskStatus(TaskStatus.TO_DO)
                .taskTitle("Framing")
                .periodStart(new Date())
                .periodEnd(new Date())
                .taskDescription("Frame the building")
                .taskPriority(TaskPriority.MEDIUM)
                .estimatedHours(60.0)
                .hoursSpent(0.0)
                .taskProgress(0.0)
                .assignedToUserId("user-456")
                .assignedToUserName("Jane Smith")
                .scheduleId("SCH-002")
                .build();

        taskRequestDTO = TaskRequestDTO.builder()
                .taskStatus(TaskStatus.TO_DO)
                .taskTitle("New Task")
                .periodStart(new Date())
                .periodEnd(new Date())
                .taskDescription("Task description")
                .taskPriority(TaskPriority.HIGH)
                .estimatedHours(30.0)
                .hoursSpent(0.0)
                .taskProgress(0.0)
                .assignedToUserId("user-123")
                .scheduleId("SCH-001")
                .build();

        customerUser = new UserResponseModel();
        customerUser.setUserIdentifier("customer-123");
        customerUser.setUserRole(UserRole.CUSTOMER);
        customerUser.setFirstName("John");
        customerUser.setLastName("Customer");
        customerUser.setPrimaryEmail("customer@test.com");

        salespersonUser = new UserResponseModel();
        salespersonUser.setUserIdentifier("salesperson-456");
        salespersonUser.setUserRole(UserRole.SALESPERSON);
        salespersonUser.setFirstName("Jane");
        salespersonUser.setLastName("Sales");
        salespersonUser.setPrimaryEmail("sales@test.com");
    }

    // Owner Endpoints Tests
    @Test
    void getOwnerAllTasks_shouldReturnAllTasksWithOkStatus() {
        List<TaskDetailResponseDTO> tasks = Arrays.asList(taskResponseDTO1, taskResponseDTO2);
        when(taskService.getAllTasks()).thenReturn(tasks);

        ResponseEntity<List<TaskDetailResponseDTO>> response = taskController.getOwnerAllTasks();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("TASK-001", response.getBody().get(0).getTaskId());
        assertEquals("TASK-002", response.getBody().get(1).getTaskId());

        verify(taskService).getAllTasks();
    }

    @Test
    void getOwnerAllTasks_shouldReturnEmptyListWithOkStatus() {
        when(taskService.getAllTasks()).thenReturn(Collections.emptyList());

        ResponseEntity<List<TaskDetailResponseDTO>> response = taskController.getOwnerAllTasks();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(taskService).getAllTasks();
    }

    @Test
    void getOwnerTaskById_shouldReturnTaskWithOkStatus() {
        String taskId = "TASK-001";
        when(taskService.getTaskByIdentifier(taskId)).thenReturn(taskResponseDTO1);

        ResponseEntity<?> response = taskController.getOwnerTaskById(taskId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(taskResponseDTO1, response.getBody());

        verify(taskService).getTaskByIdentifier(taskId);
    }

    @Test
    void getOwnerTaskById_shouldReturnNotFoundWhenTaskNotFound() {
        String taskId = "TASK-999";
        String errorMessage = "Task not found with identifier: TASK-999";
        when(taskService.getTaskByIdentifier(taskId)).thenThrow(new NotFoundException(errorMessage));

        ResponseEntity<?> response = taskController.getOwnerTaskById(taskId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(taskService).getTaskByIdentifier(taskId);
    }

    @Test
    void createOwnerTask_shouldReturnCreatedWhenSuccessful() {
        when(taskService.createTask(taskRequestDTO)).thenReturn(taskResponseDTO1);

        ResponseEntity<?> response = taskController.createOwnerTask(taskRequestDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(taskResponseDTO1, response.getBody());

        verify(taskService).createTask(taskRequestDTO);
    }

    @Test
    void createOwnerTask_shouldReturnBadRequestWhenInvalidInput() {
        String errorMessage = "Task title must not be blank";
        when(taskService.createTask(taskRequestDTO)).thenThrow(new InvalidInputException(errorMessage));

        ResponseEntity<?> response = taskController.createOwnerTask(taskRequestDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(taskService).createTask(taskRequestDTO);
    }

    @Test
    void createOwnerTask_shouldReturnNotFoundWhenScheduleNotFound() {
        String errorMessage = "Schedule not found";
        when(taskService.createTask(taskRequestDTO)).thenThrow(new NotFoundException(errorMessage));

        ResponseEntity<?> response = taskController.createOwnerTask(taskRequestDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(taskService).createTask(taskRequestDTO);
    }

    @Test
    void updateOwnerTask_shouldReturnOkWhenSuccessful() {
        String taskId = "TASK-001";
        when(taskService.updateTask(taskId, taskRequestDTO)).thenReturn(taskResponseDTO1);

        ResponseEntity<?> response = taskController.updateOwnerTask(taskId, taskRequestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(taskResponseDTO1, response.getBody());

        verify(taskService).updateTask(taskId, taskRequestDTO);
    }

    @Test
    void updateOwnerTask_shouldReturnNotFoundWhenTaskNotFound() {
        String taskId = "TASK-999";
        String errorMessage = "Task not found with identifier: TASK-999";
        when(taskService.updateTask(taskId, taskRequestDTO))
                .thenThrow(new NotFoundException(errorMessage));

        ResponseEntity<?> response = taskController.updateOwnerTask(taskId, taskRequestDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(taskService).updateTask(taskId, taskRequestDTO);
    }

    @Test
    void updateOwnerTask_shouldReturnBadRequestWhenInvalidInput() {
        String taskId = "TASK-001";
        String errorMessage = "Invalid input";
        when(taskService.updateTask(taskId, taskRequestDTO))
                .thenThrow(new InvalidInputException(errorMessage));

        ResponseEntity<?> response = taskController.updateOwnerTask(taskId, taskRequestDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(taskService).updateTask(taskId, taskRequestDTO);
    }

    @Test
    void deleteOwnerTask_shouldReturnNoContentWhenSuccessful() {
        String taskId = "TASK-001";
        doNothing().when(taskService).deleteTask(taskId);

        ResponseEntity<?> response = taskController.deleteOwnerTask(taskId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(taskService).deleteTask(taskId);
    }

    @Test
    void deleteOwnerTask_shouldReturnNotFoundWhenTaskNotFound() {
        String taskId = "TASK-999";
        String errorMessage = "Task not found";
        doThrow(new NotFoundException(errorMessage)).when(taskService).deleteTask(taskId);

        ResponseEntity<?> response = taskController.deleteOwnerTask(taskId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(taskService).deleteTask(taskId);
    }

    // Contractor Endpoints Tests
    @Test
    void getContractorTasks_shouldReturnTasksForContractorWithOkStatus() {
        String contractorId = "contractor-123";
        List<TaskDetailResponseDTO> tasks = Arrays.asList(taskResponseDTO1, taskResponseDTO2);
        when(taskService.getTasksForContractor(contractorId)).thenReturn(tasks);

        ResponseEntity<?> response = taskController.getContractorTasks(contractorId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tasks, response.getBody());

        verify(taskService).getTasksForContractor(contractorId);
    }

    @Test
    void getContractorTasks_shouldReturnEmptyListWithOkStatus() {
        String contractorId = "contractor-123";
        when(taskService.getTasksForContractor(contractorId)).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = taskController.getContractorTasks(contractorId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((List<?>) response.getBody()).isEmpty());

        verify(taskService).getTasksForContractor(contractorId);
    }

    @Test
    void getContractorTasks_shouldReturnNotFoundWhenContractorNotFound() {
        String contractorId = "contractor-999";
        String errorMessage = "User not found with identifier: contractor-999";
        when(taskService.getTasksForContractor(contractorId))
                .thenThrow(new NotFoundException(errorMessage));

        ResponseEntity<?> response = taskController.getContractorTasks(contractorId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(taskService).getTasksForContractor(contractorId);
    }

    @Test
    void getContractorTasks_shouldReturnBadRequestWhenInvalidInput() {
        String contractorId = "invalid";
        String errorMessage = "User is not a contractor";
        when(taskService.getTasksForContractor(contractorId))
                .thenThrow(new InvalidInputException(errorMessage));

        ResponseEntity<?> response = taskController.getContractorTasks(contractorId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(taskService).getTasksForContractor(contractorId);
    }

    @Test
    void getContractorTaskById_shouldReturnTaskWithOkStatus() {
        String taskId = "TASK-001";
        when(taskService.getTaskByIdentifier(taskId)).thenReturn(taskResponseDTO1);

        ResponseEntity<?> response = taskController.getContractorTaskById(taskId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(taskResponseDTO1, response.getBody());

        verify(taskService).getTaskByIdentifier(taskId);
    }

    @Test
    void getContractorTaskById_shouldReturnNotFoundWhenTaskNotFound() {
        String taskId = "TASK-999";
        String errorMessage = "Task not found";
        when(taskService.getTaskByIdentifier(taskId)).thenThrow(new NotFoundException(errorMessage));

        ResponseEntity<?> response = taskController.getContractorTaskById(taskId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(taskService).getTaskByIdentifier(taskId);
    }

    @Test
    void updateContractorTask_shouldReturnOkWhenSuccessful() {
        String taskId = "TASK-001";
        when(taskService.updateTask(taskId, taskRequestDTO)).thenReturn(taskResponseDTO1);

        ResponseEntity<?> response = taskController.updateContractorTask(taskId, taskRequestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(taskResponseDTO1, response.getBody());

        verify(taskService).updateTask(taskId, taskRequestDTO);
    }

    @Test
    void updateContractorTask_shouldReturnBadRequestWhenInvalidInput() {
        String taskId = "TASK-001";
        String errorMessage = "Invalid input for contractor update";
        when(taskService.updateTask(taskId, taskRequestDTO)).thenThrow(new InvalidInputException(errorMessage));

        ResponseEntity<?> response = taskController.updateContractorTask(taskId, taskRequestDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(taskService).updateTask(taskId, taskRequestDTO);
    }

    // Schedule Task Endpoints Tests
    @Test
    void getTasksForSchedule_shouldReturnTasksForScheduleWithOkStatus() {
        String scheduleIdentifier = "SCH-001";
        List<TaskDetailResponseDTO> tasks = Arrays.asList(taskResponseDTO1);
        when(taskService.getTasksForSchedule(scheduleIdentifier)).thenReturn(tasks);

        ResponseEntity<List<TaskDetailResponseDTO>> response = taskController.getTasksForSchedule(scheduleIdentifier);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("TASK-001", response.getBody().get(0).getTaskId());
        assertEquals(scheduleIdentifier, response.getBody().get(0).getScheduleId());

        verify(taskService).getTasksForSchedule(scheduleIdentifier);
    }

    @Test
    void getTasksForSchedule_shouldReturnEmptyListWhenNoTasksForSchedule() {
        String scheduleIdentifier = "SCH-999";
        when(taskService.getTasksForSchedule(scheduleIdentifier)).thenReturn(Collections.emptyList());

        ResponseEntity<List<TaskDetailResponseDTO>> response = taskController.getTasksForSchedule(scheduleIdentifier);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(taskService).getTasksForSchedule(scheduleIdentifier);
    }

    // ========================
    // Contractor View All Tasks Endpoint Tests
    // ========================

    @Test
    void getAllTasksForContractorView_shouldReturnAllTasksWithOkStatus() {
        String auth0UserId = "auth0|contractor123";
        String backendUserId = "contractor-backend-123";
        
        UserResponseModel contractorUser = new UserResponseModel();
        contractorUser.setUserIdentifier(backendUserId);
        contractorUser.setUserRole(UserRole.CONTRACTOR);
        
        List<TaskDetailResponseDTO> tasks = Arrays.asList(taskResponseDTO1, taskResponseDTO2);
        
        when(jwt.getSubject()).thenReturn(auth0UserId);
        when(userService.getUserByAuth0Id(auth0UserId)).thenReturn(contractorUser);
        when(taskService.getTasksForContractor(backendUserId)).thenReturn(tasks);

        ResponseEntity<?> response = taskController.getAllTasksForContractorView(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        List<TaskDetailResponseDTO> responseBody = (List<TaskDetailResponseDTO>) response.getBody();
        assertEquals(2, responseBody.size());
        assertEquals("TASK-001", responseBody.get(0).getTaskId());
        assertEquals("TASK-002", responseBody.get(1).getTaskId());

        verify(userService).getUserByAuth0Id(auth0UserId);
        verify(taskService).getTasksForContractor(backendUserId);
    }

    @Test
    void getAllTasksForContractorView_shouldReturnEmptyListWithOkStatus() {
        String auth0UserId = "auth0|contractor123";
        String backendUserId = "contractor-backend-123";
        
        UserResponseModel contractorUser = new UserResponseModel();
        contractorUser.setUserIdentifier(backendUserId);
        contractorUser.setUserRole(UserRole.CONTRACTOR);
        
        when(jwt.getSubject()).thenReturn(auth0UserId);
        when(userService.getUserByAuth0Id(auth0UserId)).thenReturn(contractorUser);
        when(taskService.getTasksForContractor(backendUserId)).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = taskController.getAllTasksForContractorView(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        List<TaskDetailResponseDTO> responseBody = (List<TaskDetailResponseDTO>) response.getBody();
        assertTrue(responseBody.isEmpty());

        verify(userService).getUserByAuth0Id(auth0UserId);
        verify(taskService).getTasksForContractor(backendUserId);
    }

    @Test
    void getAllTasksForContractorView_shouldReturnTasksWithDifferentStatuses() {
        TaskDetailResponseDTO completedTask = TaskDetailResponseDTO.builder()
                .taskId("TASK-003")
                .taskStatus(TaskStatus.COMPLETED)
                .taskTitle("Completed Task")
                .taskProgress(100.0)
                .build();

        TaskDetailResponseDTO onHoldTask = TaskDetailResponseDTO.builder()
                .taskId("TASK-004")
                .taskStatus(TaskStatus.ON_HOLD)
                .taskTitle("On Hold Task")
                .taskProgress(25.0)
                .build();

        String auth0UserId = "auth0|contractor123";
        String backendUserId = "contractor-backend-123";
        
        UserResponseModel contractorUser = new UserResponseModel();
        contractorUser.setUserIdentifier(backendUserId);
        contractorUser.setUserRole(UserRole.CONTRACTOR);
        
        List<TaskDetailResponseDTO> tasks = Arrays.asList(taskResponseDTO1, taskResponseDTO2, completedTask, onHoldTask);
        
        when(jwt.getSubject()).thenReturn(auth0UserId);
        when(userService.getUserByAuth0Id(auth0UserId)).thenReturn(contractorUser);
        when(taskService.getTasksForContractor(backendUserId)).thenReturn(tasks);

        ResponseEntity<?> response = taskController.getAllTasksForContractorView(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        List<TaskDetailResponseDTO> responseBody = (List<TaskDetailResponseDTO>) response.getBody();
        assertEquals(4, responseBody.size());

        verify(userService).getUserByAuth0Id(auth0UserId);
        verify(taskService).getTasksForContractor(backendUserId);
    }

    // ========================
    // Generic Task Endpoints Tests
    // ========================

    @Test
    void getTaskById_shouldReturnTaskWithOkStatus() {
        String taskId = "TASK-001";
        when(taskService.getTaskByIdentifier(taskId)).thenReturn(taskResponseDTO1);

        ResponseEntity<?> response = taskController.getTaskById(taskId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(taskResponseDTO1, response.getBody());

        verify(taskService).getTaskByIdentifier(taskId);
    }

    @Test
    void getTaskById_shouldReturnNotFoundWhenTaskNotFound() {
        String taskId = "TASK-999";
        String errorMessage = "Task not found with identifier: TASK-999";
        when(taskService.getTaskByIdentifier(taskId)).thenThrow(new NotFoundException(errorMessage));

        ResponseEntity<?> response = taskController.getTaskById(taskId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(taskService).getTaskByIdentifier(taskId);
    }

    @Test
    void updateTask_shouldReturnOkWhenSuccessful() {
        String taskId = "TASK-001";
        when(taskService.updateTask(taskId, taskRequestDTO)).thenReturn(taskResponseDTO1);

        ResponseEntity<?> response = taskController.updateTask(taskId, taskRequestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(taskResponseDTO1, response.getBody());

        verify(taskService).updateTask(taskId, taskRequestDTO);
    }

    @Test
    void updateTask_shouldReturnNotFoundWhenTaskNotFound() {
        String taskId = "TASK-999";
        String errorMessage = "Task not found with identifier: TASK-999";
        when(taskService.updateTask(taskId, taskRequestDTO)).thenThrow(new NotFoundException(errorMessage));

        ResponseEntity<?> response = taskController.updateTask(taskId, taskRequestDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(taskService).updateTask(taskId, taskRequestDTO);
    }

    @Test
    void updateTask_shouldReturnBadRequestWhenInvalidInput() {
        String taskId = "TASK-001";
        String errorMessage = "Invalid task data provided";
        when(taskService.updateTask(taskId, taskRequestDTO)).thenThrow(new InvalidInputException(errorMessage));

        ResponseEntity<?> response = taskController.updateTask(taskId, taskRequestDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(taskService).updateTask(taskId, taskRequestDTO);
    }

    @Test
    void deleteTask_shouldReturnNoContentWhenSuccessful() {
        String taskId = "TASK-001";
        doNothing().when(taskService).deleteTask(taskId);

        ResponseEntity<?> response = taskController.deleteTask(taskId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(taskService).deleteTask(taskId);
    }

    @Test
    void deleteTask_shouldReturnNotFoundWhenTaskNotFound() {
        String taskId = "TASK-999";
        String errorMessage = "Task not found with identifier: TASK-999";
        doThrow(new NotFoundException(errorMessage)).when(taskService).deleteTask(taskId);

        ResponseEntity<?> response = taskController.deleteTask(taskId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(taskService).deleteTask(taskId);
    }

    // ========================
    // Contractor Update Task with Exception Tests
    // ========================

    @Test
    void updateContractorTask_shouldReturnNotFoundWhenTaskNotFound() {
        String taskId = "TASK-999";
        String errorMessage = "Task not found with identifier: TASK-999";
        when(taskService.updateTask(taskId, taskRequestDTO)).thenThrow(new NotFoundException(errorMessage));

        ResponseEntity<?> response = taskController.updateContractorTask(taskId, taskRequestDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(taskService).updateTask(taskId, taskRequestDTO);
    }

    @Test
    void updateContractorTask_shouldReturnInternalServerErrorOnUnexpectedException() {
        String taskId = "TASK-001";
        when(taskService.updateTask(taskId, taskRequestDTO)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = taskController.updateContractorTask(taskId, taskRequestDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An internal error occurred", response.getBody());

        verify(taskService).updateTask(taskId, taskRequestDTO);
    }

    // ========================
    // Edge Cases and Boundary Tests
    // ========================

    @Test
    void getAllTasksForContractorView_shouldHandleSingleTask() {
        String auth0UserId = "auth0|contractor123";
        String backendUserId = "contractor-backend-123";
        
        UserResponseModel contractorUser = new UserResponseModel();
        contractorUser.setUserIdentifier(backendUserId);
        contractorUser.setUserRole(UserRole.CONTRACTOR);
        
        List<TaskDetailResponseDTO> tasks = Collections.singletonList(taskResponseDTO1);
        
        when(jwt.getSubject()).thenReturn(auth0UserId);
        when(userService.getUserByAuth0Id(auth0UserId)).thenReturn(contractorUser);
        when(taskService.getTasksForContractor(backendUserId)).thenReturn(tasks);

        ResponseEntity<?> response = taskController.getAllTasksForContractorView(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        List<TaskDetailResponseDTO> responseBody = (List<TaskDetailResponseDTO>) response.getBody();
        assertEquals(1, responseBody.size());

        verify(userService).getUserByAuth0Id(auth0UserId);
        verify(taskService).getTasksForContractor(backendUserId);
    }

    @Test
    void getTaskById_shouldReturnTaskWithAllFields() {
        String taskId = "TASK-001";
        TaskDetailResponseDTO fullTask = TaskDetailResponseDTO.builder()
                .taskId(taskId)
                .taskStatus(TaskStatus.IN_PROGRESS)
                .taskTitle("Complete Task")
                .periodStart(new Date())
                .periodEnd(new Date())
                .taskDescription("Full task description")
                .taskPriority(TaskPriority.VERY_HIGH)
                .estimatedHours(100.0)
                .hoursSpent(50.0)
                .taskProgress(50.0)
                .assignedToUserId("user-123")
                .assignedToUserName("Test User")
                .scheduleId("SCH-001")
                .build();

        when(taskService.getTaskByIdentifier(taskId)).thenReturn(fullTask);

        ResponseEntity<?> response = taskController.getTaskById(taskId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        TaskDetailResponseDTO responseTask = (TaskDetailResponseDTO) response.getBody();
        assertNotNull(responseTask);
        assertEquals(taskId, responseTask.getTaskId());
        assertEquals(TaskStatus.IN_PROGRESS, responseTask.getTaskStatus());
        assertEquals(TaskPriority.VERY_HIGH, responseTask.getTaskPriority());
        assertEquals(100.0, responseTask.getEstimatedHours());
        assertEquals(50.0, responseTask.getHoursSpent());
        assertEquals(50.0, responseTask.getTaskProgress());
        assertEquals("SCH-001", responseTask.getScheduleId());

        verify(taskService).getTaskByIdentifier(taskId);
    }

    @Test
    void updateTask_shouldUpdateAllTaskFields() {
        String taskId = "TASK-001";
        TaskRequestDTO updateRequest = TaskRequestDTO.builder()
                .taskStatus(TaskStatus.COMPLETED)
                .taskTitle("Updated Task Title")
                .periodStart(new Date())
                .periodEnd(new Date())
                .taskDescription("Updated description")
                .taskPriority(TaskPriority.LOW)
                .estimatedHours(20.0)
                .hoursSpent(20.0)
                .taskProgress(100.0)
                .assignedToUserId("user-456")
                .scheduleId("SCH-001")
                .build();

        TaskDetailResponseDTO updatedTask = TaskDetailResponseDTO.builder()
                .taskId(taskId)
                .taskStatus(TaskStatus.COMPLETED)
                .taskTitle("Updated Task Title")
                .taskProgress(100.0)
                .build();

        when(taskService.updateTask(taskId, updateRequest)).thenReturn(updatedTask);

        ResponseEntity<?> response = taskController.updateTask(taskId, updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        TaskDetailResponseDTO responseTask = (TaskDetailResponseDTO) response.getBody();
        assertNotNull(responseTask);
        assertEquals(TaskStatus.COMPLETED, responseTask.getTaskStatus());
        assertEquals(100.0, responseTask.getTaskProgress());

        verify(taskService).updateTask(taskId, updateRequest);
    }

    @Test
    void getContractorTasks_shouldReturnTasksWithAssigneeInfo() {
        String contractorId = "contractor-123";
        TaskDetailResponseDTO taskWithAssigneeInfo = TaskDetailResponseDTO.builder()
                .taskId("TASK-WITH-ASSIGNEE")
                .taskStatus(TaskStatus.IN_PROGRESS)
                .taskTitle("Task with Assignee Info")
                .scheduleId("SCH-001")
                .assignedToUserId(contractorId)
                .assignedToUserName("Contractor Name")
                .build();

        List<TaskDetailResponseDTO> tasks = Collections.singletonList(taskWithAssigneeInfo);
        when(taskService.getTasksForContractor(contractorId)).thenReturn(tasks);

        ResponseEntity<?> response = taskController.getContractorTasks(contractorId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<TaskDetailResponseDTO> responseTasks = (List<TaskDetailResponseDTO>) response.getBody();
        assertNotNull(responseTasks);
        assertEquals(1, responseTasks.size());
        assertEquals("Contractor Name", responseTasks.get(0).getAssignedToUserName());
        assertEquals(contractorId, responseTasks.get(0).getAssignedToUserId());

        verify(taskService).getTasksForContractor(contractorId);
    }

    // Customer Endpoints Tests
    @Test
    void getCustomerTasks_shouldReturnTasksForAssignedLots() {
        String auth0UserId = "auth0|customer123";
        List<TaskDetailResponseDTO> tasks = Arrays.asList(taskResponseDTO1, taskResponseDTO2);

        when(jwt.getSubject()).thenReturn(auth0UserId);
        when(userService.getUserByAuth0Id(auth0UserId)).thenReturn(customerUser);
        when(taskService.getTasksForUserAssignedLots(customerUser.getUserIdentifier())).thenReturn(tasks);

        ResponseEntity<?> response = taskController.getCustomerTasks(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<TaskDetailResponseDTO> responseTasks = (List<TaskDetailResponseDTO>) response.getBody();
        assertNotNull(responseTasks);
        assertEquals(2, responseTasks.size());

        verify(jwt).getSubject();
        verify(userService).getUserByAuth0Id(auth0UserId);
        verify(taskService).getTasksForUserAssignedLots(customerUser.getUserIdentifier());
    }

    @Test
    void getCustomerTasks_shouldReturnEmptyListWhenNoTasks() {
        String auth0UserId = "auth0|customer123";

        when(jwt.getSubject()).thenReturn(auth0UserId);
        when(userService.getUserByAuth0Id(auth0UserId)).thenReturn(customerUser);
        when(taskService.getTasksForUserAssignedLots(customerUser.getUserIdentifier())).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = taskController.getCustomerTasks(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<TaskDetailResponseDTO> responseTasks = (List<TaskDetailResponseDTO>) response.getBody();
        assertNotNull(responseTasks);
        assertTrue(responseTasks.isEmpty());

        verify(taskService).getTasksForUserAssignedLots(customerUser.getUserIdentifier());
    }

    @Test
    void getCustomerTasks_shouldReturnNotFoundWhenUserNotFound() {
        String auth0UserId = "auth0|customer999";

        when(jwt.getSubject()).thenReturn(auth0UserId);
        when(userService.getUserByAuth0Id(auth0UserId)).thenThrow(new NotFoundException("User not found"));

        ResponseEntity<?> response = taskController.getCustomerTasks(jwt);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).getUserByAuth0Id(auth0UserId);
        verify(taskService, never()).getTasksForUserAssignedLots(any());
    }

    // Salesperson Endpoints Tests
    @Test
    void getSalespersonTasks_shouldReturnTasksForAssignedLots() {
        String auth0UserId = "auth0|sales456";
        List<TaskDetailResponseDTO> tasks = Collections.singletonList(taskResponseDTO1);

        when(jwt.getSubject()).thenReturn(auth0UserId);
        when(userService.getUserByAuth0Id(auth0UserId)).thenReturn(salespersonUser);
        when(taskService.getTasksForUserAssignedLots(salespersonUser.getUserIdentifier())).thenReturn(tasks);

        ResponseEntity<?> response = taskController.getSalespersonTasks(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<TaskDetailResponseDTO> responseTasks = (List<TaskDetailResponseDTO>) response.getBody();
        assertNotNull(responseTasks);
        assertEquals(1, responseTasks.size());

        verify(jwt).getSubject();
        verify(userService).getUserByAuth0Id(auth0UserId);
        verify(taskService).getTasksForUserAssignedLots(salespersonUser.getUserIdentifier());
    }

    @Test
    void getSalespersonTasks_shouldReturnEmptyListWhenNoTasks() {
        String auth0UserId = "auth0|sales456";

        when(jwt.getSubject()).thenReturn(auth0UserId);
        when(userService.getUserByAuth0Id(auth0UserId)).thenReturn(salespersonUser);
        when(taskService.getTasksForUserAssignedLots(salespersonUser.getUserIdentifier())).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = taskController.getSalespersonTasks(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<TaskDetailResponseDTO> responseTasks = (List<TaskDetailResponseDTO>) response.getBody();
        assertNotNull(responseTasks);
        assertTrue(responseTasks.isEmpty());

        verify(taskService).getTasksForUserAssignedLots(salespersonUser.getUserIdentifier());
    }

    @Test
    void getSalespersonTasks_shouldReturnNotFoundWhenUserNotFound() {
        String auth0UserId = "auth0|sales999";

        when(jwt.getSubject()).thenReturn(auth0UserId);
        when(userService.getUserByAuth0Id(auth0UserId)).thenThrow(new NotFoundException("User not found"));

        ResponseEntity<?> response = taskController.getSalespersonTasks(jwt);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).getUserByAuth0Id(auth0UserId);
        verify(taskService, never()).getTasksForUserAssignedLots(any());
    }
}

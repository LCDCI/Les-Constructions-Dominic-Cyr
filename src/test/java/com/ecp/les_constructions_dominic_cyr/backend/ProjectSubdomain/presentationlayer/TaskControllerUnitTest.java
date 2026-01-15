package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule.TaskService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskPriority;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskDetailResponseDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskRequestDTO;
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

    @InjectMocks
    private TaskController taskController;

    private TaskDetailResponseDTO taskResponseDTO1;
    private TaskDetailResponseDTO taskResponseDTO2;
    private TaskRequestDTO taskRequestDTO;

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
}

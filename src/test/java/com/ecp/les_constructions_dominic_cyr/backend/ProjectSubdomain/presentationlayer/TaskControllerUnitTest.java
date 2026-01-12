package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Task.TaskService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Task.TaskController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Task.TaskRequestDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Task.TaskResponseDTO;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerUnitTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private TaskResponseDTO responseDTO1;
    private TaskResponseDTO responseDTO2;
    private TaskRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO1 = TaskResponseDTO.builder()
                .taskIdentifier("TASK-001")
                .taskDate(LocalDate.now())
                .taskDescription("Foundation Work")
                .lotNumber("Lot 53")
                .dayOfWeek("Monday")
                .assignedTo("contractor-123")
                .build();

        responseDTO2 = TaskResponseDTO.builder()
                .taskIdentifier("TASK-002")
                .taskDate(LocalDate.now().plusDays(1))
                .taskDescription("Framing")
                .lotNumber("Lot 57")
                .dayOfWeek("Tuesday")
                .assignedTo("contractor-456")
                .build();

        requestDTO = TaskRequestDTO.builder()
                .taskDate(LocalDate.now())
                .taskDescription("New Task")
                .lotNumber("Lot 100")
                .dayOfWeek("Wednesday")
                .assignedTo("contractor-123")
                .build();
    }

    // Owner Endpoints Tests
    @Test
    void getOwnerCurrentWeekTasks_shouldReturnTasksWithOkStatus() {
        List<TaskResponseDTO> tasks = Arrays.asList(responseDTO1, responseDTO2);
        when(taskService.getCurrentWeekTasks()).thenReturn(tasks);

        ResponseEntity<List<TaskResponseDTO>> response = taskController.getOwnerCurrentWeekTasks();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("TASK-001", response.getBody().get(0).getTaskIdentifier());

        verify(taskService).getCurrentWeekTasks();
    }

    @Test
    void getOwnerAllTasks_shouldReturnAllTasksWithOkStatus() {
        List<TaskResponseDTO> tasks = Arrays.asList(responseDTO1, responseDTO2);
        when(taskService.getAllTasks()).thenReturn(tasks);

        ResponseEntity<List<TaskResponseDTO>> response = taskController.getOwnerAllTasks();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(taskService).getAllTasks();
    }

    @Test
    void getOwnerTaskByIdentifier_shouldReturnTaskWithOkStatus() {
        when(taskService.getTaskByIdentifier("TASK-001")).thenReturn(responseDTO1);

        ResponseEntity<TaskResponseDTO> response = taskController.getOwnerTaskByIdentifier("TASK-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TASK-001", response.getBody().getTaskIdentifier());

        verify(taskService).getTaskByIdentifier("TASK-001");
    }

    @Test
    void getOwnerTaskByIdentifier_shouldReturnNotFoundWhenTaskDoesNotExist() {
        when(taskService.getTaskByIdentifier("TASK-999")).thenThrow(new NotFoundException("Task not found"));

        ResponseEntity<TaskResponseDTO> response = taskController.getOwnerTaskByIdentifier("TASK-999");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(taskService).getTaskByIdentifier("TASK-999");
    }

    @Test
    void createTask_shouldReturnCreatedTask() {
        when(taskService.createTask(requestDTO)).thenReturn(responseDTO1);

        ResponseEntity<TaskResponseDTO> response = taskController.createTask(requestDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TASK-001", response.getBody().getTaskIdentifier());

        verify(taskService).createTask(requestDTO);
    }

    @Test
    void createTask_shouldReturnBadRequestForInvalidInput() {
        when(taskService.createTask(requestDTO)).thenThrow(new InvalidInputException("Invalid contractor"));

        ResponseEntity<TaskResponseDTO> response = taskController.createTask(requestDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(taskService).createTask(requestDTO);
    }

    @Test
    void updateTask_shouldReturnUpdatedTask() {
        when(taskService.updateTask("TASK-001", requestDTO)).thenReturn(responseDTO1);

        ResponseEntity<TaskResponseDTO> response = taskController.updateTask("TASK-001", requestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TASK-001", response.getBody().getTaskIdentifier());

        verify(taskService).updateTask("TASK-001", requestDTO);
    }

    @Test
    void updateTask_shouldReturnNotFoundWhenTaskDoesNotExist() {
        when(taskService.updateTask("TASK-999", requestDTO)).thenThrow(new NotFoundException("Task not found"));

        ResponseEntity<TaskResponseDTO> response = taskController.updateTask("TASK-999", requestDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(taskService).updateTask("TASK-999", requestDTO);
    }

    @Test
    void deleteTask_shouldReturnNoContent() {
        doNothing().when(taskService).deleteTask("TASK-001");

        ResponseEntity<Void> response = taskController.deleteTask("TASK-001");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(taskService).deleteTask("TASK-001");
    }

    @Test
    void deleteTask_shouldReturnNotFoundWhenTaskDoesNotExist() {
        doThrow(new NotFoundException("Task not found")).when(taskService).deleteTask("TASK-999");

        ResponseEntity<Void> response = taskController.deleteTask("TASK-999");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(taskService).deleteTask("TASK-999");
    }

    // Contractor Endpoints Tests
    @Test
    void getContractorCurrentWeekTasks_shouldReturnTasksForContractor() {
        List<TaskResponseDTO> tasks = Arrays.asList(responseDTO1);
        when(taskService.getCurrentWeekTasksAssignedToContractor("contractor-123")).thenReturn(tasks);

        ResponseEntity<List<TaskResponseDTO>> response = taskController.getContractorCurrentWeekTasks("contractor-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("contractor-123", response.getBody().get(0).getAssignedTo());

        verify(taskService).getCurrentWeekTasksAssignedToContractor("contractor-123");
    }

    @Test
    void getContractorAllTasks_shouldReturnAllTasksForContractor() {
        List<TaskResponseDTO> tasks = Arrays.asList(responseDTO1);
        when(taskService.getTasksAssignedToContractor("contractor-123")).thenReturn(tasks);

        ResponseEntity<List<TaskResponseDTO>> response = taskController.getContractorAllTasks("contractor-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        verify(taskService).getTasksAssignedToContractor("contractor-123");
    }

    @Test
    void getContractorTaskByIdentifier_shouldReturnTask() {
        when(taskService.getTaskByIdentifier("TASK-001")).thenReturn(responseDTO1);

        ResponseEntity<TaskResponseDTO> response = taskController.getContractorTaskByIdentifier("TASK-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TASK-001", response.getBody().getTaskIdentifier());

        verify(taskService).getTaskByIdentifier("TASK-001");
    }

    @Test
    void getContractorCurrentWeekTasks_shouldReturnEmptyListWhenNoTasks() {
        when(taskService.getCurrentWeekTasksAssignedToContractor("contractor-999")).thenReturn(Collections.emptyList());

        ResponseEntity<List<TaskResponseDTO>> response = taskController.getContractorCurrentWeekTasks("contractor-999");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(taskService).getCurrentWeekTasksAssignedToContractor("contractor-999");
    }
}

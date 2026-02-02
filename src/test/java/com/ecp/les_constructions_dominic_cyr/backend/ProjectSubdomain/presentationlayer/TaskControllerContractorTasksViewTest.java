package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule.TaskService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ContractorTaskViewDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskController;
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

/**
 * Unit tests for contractor tasks view endpoints in TaskController
 */
@ExtendWith(MockitoExtension.class)
class TaskControllerContractorTasksViewTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private ContractorTaskViewDTO task1;
    private ContractorTaskViewDTO task2;
    private ContractorTaskViewDTO task3;
    private Date periodStart;
    private Date periodEnd;

    @BeforeEach
    void setUp() {
        periodStart = new Date();
        periodEnd = new Date(periodStart.getTime() + 86400000 * 5); // 5 days later

        task1 = ContractorTaskViewDTO.builder()
                .taskId("TASK-001")
                .taskTitle("Site Preparation")
                .taskDescription("Prepare the construction site")
                .taskStatus("COMPLETED")
                .taskPriority("HIGH")
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .estimatedHours(16.0)
                .hoursSpent(16.0)
                .taskProgress(100)
                .assignedToUserId("contractor-123")
                .assignedToUserName("Jane Contractor")
                .projectIdentifier("foresta")
                .projectName("Fôresta")
                .lotId("53")
                .lotNumber("Lot 53")
                .scheduleId("SCH-001")
                .build();

        task2 = ContractorTaskViewDTO.builder()
                .taskId("TASK-002")
                .taskTitle("Foundation Work")
                .taskDescription("Pour foundation")
                .taskStatus("IN_PROGRESS")
                .taskPriority("VERY_HIGH")
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .estimatedHours(40.0)
                .hoursSpent(20.0)
                .taskProgress(50)
                .assignedToUserId("contractor-123")
                .assignedToUserName("Jane Contractor")
                .projectIdentifier("foresta")
                .projectName("Fôresta")
                .lotId("53")
                .lotNumber("Lot 53")
                .scheduleId("SCH-002")
                .build();

        task3 = ContractorTaskViewDTO.builder()
                .taskId("TASK-003")
                .taskTitle("Framing")
                .taskDescription("Frame the structure")
                .taskStatus("TO_DO")
                .taskPriority("MEDIUM")
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .estimatedHours(60.0)
                .hoursSpent(0.0)
                .taskProgress(0)
                .assignedToUserId("contractor-456")
                .assignedToUserName("John Builder")
                .projectIdentifier("maple-hills")
                .projectName("Maple Hills")
                .lotId("10")
                .lotNumber("Lot 10")
                .scheduleId("SCH-003")
                .build();
    }

    @Test
    void getAllTasksForContractorView_ReturnsAllTasks_Success() {
        // Arrange
        List<ContractorTaskViewDTO> expectedTasks = Arrays.asList(task1, task2, task3);
        when(taskService.getAllTasksForContractorView()).thenReturn(expectedTasks);

        // Act
        ResponseEntity<List<ContractorTaskViewDTO>> response = taskController.getAllTasksForContractorView();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        assertEquals("TASK-001", response.getBody().get(0).getTaskId());
        assertEquals("Site Preparation", response.getBody().get(0).getTaskTitle());
        assertEquals("Fôresta", response.getBody().get(0).getProjectName());

        verify(taskService, times(1)).getAllTasksForContractorView();
    }

    @Test
    void getAllTasksForContractorView_ReturnsEmptyList_WhenNoTasks() {
        // Arrange
        when(taskService.getAllTasksForContractorView()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<ContractorTaskViewDTO>> response = taskController.getAllTasksForContractorView();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(taskService, times(1)).getAllTasksForContractorView();
    }

    @Test
    void getAllTasksForContractorView_ReturnsTasksWithAllStatuses() {
        // Arrange
        List<ContractorTaskViewDTO> expectedTasks = Arrays.asList(task1, task2, task3);
        when(taskService.getAllTasksForContractorView()).thenReturn(expectedTasks);

        // Act
        ResponseEntity<List<ContractorTaskViewDTO>> response = taskController.getAllTasksForContractorView();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ContractorTaskViewDTO> tasks = response.getBody();
        assertNotNull(tasks);

        // Verify different statuses are present
        assertTrue(tasks.stream().anyMatch(t -> "COMPLETED".equals(t.getTaskStatus())));
        assertTrue(tasks.stream().anyMatch(t -> "IN_PROGRESS".equals(t.getTaskStatus())));
        assertTrue(tasks.stream().anyMatch(t -> "TO_DO".equals(t.getTaskStatus())));

        verify(taskService, times(1)).getAllTasksForContractorView();
    }

    @Test
    void getAllTasksForContractorView_ReturnsTasksWithAllPriorities() {
        // Arrange
        List<ContractorTaskViewDTO> expectedTasks = Arrays.asList(task1, task2, task3);
        when(taskService.getAllTasksForContractorView()).thenReturn(expectedTasks);

        // Act
        ResponseEntity<List<ContractorTaskViewDTO>> response = taskController.getAllTasksForContractorView();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ContractorTaskViewDTO> tasks = response.getBody();
        assertNotNull(tasks);

        // Verify different priorities are present
        assertTrue(tasks.stream().anyMatch(t -> "HIGH".equals(t.getTaskPriority())));
        assertTrue(tasks.stream().anyMatch(t -> "VERY_HIGH".equals(t.getTaskPriority())));
        assertTrue(tasks.stream().anyMatch(t -> "MEDIUM".equals(t.getTaskPriority())));

        verify(taskService, times(1)).getAllTasksForContractorView();
    }

    @Test
    void getAllTasksForContractorView_ReturnsTasksFromMultipleProjects() {
        // Arrange
        List<ContractorTaskViewDTO> expectedTasks = Arrays.asList(task1, task2, task3);
        when(taskService.getAllTasksForContractorView()).thenReturn(expectedTasks);

        // Act
        ResponseEntity<List<ContractorTaskViewDTO>> response = taskController.getAllTasksForContractorView();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ContractorTaskViewDTO> tasks = response.getBody();
        assertNotNull(tasks);

        // Verify tasks from different projects
        assertTrue(tasks.stream().anyMatch(t -> "foresta".equals(t.getProjectIdentifier())));
        assertTrue(tasks.stream().anyMatch(t -> "maple-hills".equals(t.getProjectIdentifier())));

        verify(taskService, times(1)).getAllTasksForContractorView();
    }

    @Test
    void getAllTasksForContractorView_ReturnsTasksFromMultipleLots() {
        // Arrange
        List<ContractorTaskViewDTO> expectedTasks = Arrays.asList(task1, task2, task3);
        when(taskService.getAllTasksForContractorView()).thenReturn(expectedTasks);

        // Act
        ResponseEntity<List<ContractorTaskViewDTO>> response = taskController.getAllTasksForContractorView();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ContractorTaskViewDTO> tasks = response.getBody();
        assertNotNull(tasks);

        // Verify tasks from different lots
        assertTrue(tasks.stream().anyMatch(t -> "53".equals(t.getLotId())));
        assertTrue(tasks.stream().anyMatch(t -> "10".equals(t.getLotId())));

        verify(taskService, times(1)).getAllTasksForContractorView();
    }

    @Test
    void getAllTasksForContractorView_ReturnsTasksWithAssigneeInformation() {
        // Arrange
        List<ContractorTaskViewDTO> expectedTasks = Arrays.asList(task1, task2, task3);
        when(taskService.getAllTasksForContractorView()).thenReturn(expectedTasks);

        // Act
        ResponseEntity<List<ContractorTaskViewDTO>> response = taskController.getAllTasksForContractorView();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ContractorTaskViewDTO> tasks = response.getBody();
        assertNotNull(tasks);

        // Verify assignee information is present
        tasks.forEach(task -> {
            assertNotNull(task.getAssignedToUserId());
            assertNotNull(task.getAssignedToUserName());
        });

        verify(taskService, times(1)).getAllTasksForContractorView();
    }

    @Test
    void getAllTasksForContractorView_ReturnsTasksWithProgressInformation() {
        // Arrange
        List<ContractorTaskViewDTO> expectedTasks = Arrays.asList(task1, task2, task3);
        when(taskService.getAllTasksForContractorView()).thenReturn(expectedTasks);

        // Act
        ResponseEntity<List<ContractorTaskViewDTO>> response = taskController.getAllTasksForContractorView();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ContractorTaskViewDTO> tasks = response.getBody();
        assertNotNull(tasks);

        // Verify progress information
        ContractorTaskViewDTO completedTask = tasks.stream()
                .filter(t -> "TASK-001".equals(t.getTaskId()))
                .findFirst()
                .orElse(null);
        assertNotNull(completedTask);
        assertEquals(100, completedTask.getTaskProgress());
        assertEquals(16.0, completedTask.getHoursSpent());

        ContractorTaskViewDTO inProgressTask = tasks.stream()
                .filter(t -> "TASK-002".equals(t.getTaskId()))
                .findFirst()
                .orElse(null);
        assertNotNull(inProgressTask);
        assertEquals(50, inProgressTask.getTaskProgress());
        assertEquals(20.0, inProgressTask.getHoursSpent());

        verify(taskService, times(1)).getAllTasksForContractorView();
    }

    @Test
    void getAllTasksForContractorView_ReturnsTasksWithDateInformation() {
        // Arrange
        List<ContractorTaskViewDTO> expectedTasks = Arrays.asList(task1, task2, task3);
        when(taskService.getAllTasksForContractorView()).thenReturn(expectedTasks);

        // Act
        ResponseEntity<List<ContractorTaskViewDTO>> response = taskController.getAllTasksForContractorView();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ContractorTaskViewDTO> tasks = response.getBody();
        assertNotNull(tasks);

        // Verify date information is present
        tasks.forEach(task -> {
            assertNotNull(task.getPeriodStart());
            assertNotNull(task.getPeriodEnd());
            assertTrue(task.getPeriodEnd().after(task.getPeriodStart()) ||
                      task.getPeriodEnd().equals(task.getPeriodStart()));
        });

        verify(taskService, times(1)).getAllTasksForContractorView();
    }

    @Test
    void getAllTasksForContractorView_ServiceThrowsException_PropagatesException() {
        // Arrange
        when(taskService.getAllTasksForContractorView())
                .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            taskController.getAllTasksForContractorView();
        });

        verify(taskService, times(1)).getAllTasksForContractorView();
    }
}


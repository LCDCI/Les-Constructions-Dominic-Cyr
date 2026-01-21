package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule.TaskService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskDetailResponseDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskRequestDTO;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskControllerContractorUpdateTest {

    @InjectMocks
    private TaskController taskController;

    @Mock
    private TaskService taskService;

    @Test
    public void updateContractorTask_shouldReturnOkWhenSuccessful() {
        // Arrange
        String taskId = "task-123";
        TaskRequestDTO request = mock(TaskRequestDTO.class);
        TaskDetailResponseDTO expectedResponse = mock(TaskDetailResponseDTO.class);

        when(taskService.updateTask(eq(taskId), any(TaskRequestDTO.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<?> response = taskController.updateContractorTask(taskId, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
        verify(taskService, times(1)).updateTask(eq(taskId), eq(request));
    }

    @Test
    public void updateContractorTask_shouldReturnBadRequestWhenInvalidInput() {
        // Arrange
        String taskId = "task-456";
        TaskRequestDTO request = mock(TaskRequestDTO.class);

        when(taskService.updateTask(eq(taskId), any(TaskRequestDTO.class)))
                .thenThrow(new InvalidInputException("Invalid input"));

        // Act
        ResponseEntity<?> response = taskController.updateContractorTask(taskId, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid input", response.getBody());
        verify(taskService, times(1)).updateTask(eq(taskId), any(TaskRequestDTO.class));
    }
}


package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.mapperlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Task.Task;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.TaskMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Task.TaskRequestDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Task.TaskResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskMapperUnitTest {

    private TaskMapper taskMapper;

    @BeforeEach
    void setUp() {
        taskMapper = new TaskMapper();
    }

    @Test
    void entityToResponseDTO_shouldMapAllFields() {
        // Arrange
        Task task = Task.builder()
                .id(1L)
                .taskIdentifier("TASK-001")
                .taskDate(LocalDate.of(2025, 12, 5))
                .taskDescription("Foundation Work")
                .lotNumber("Lot 53")
                .dayOfWeek("Wednesday")
                .assignedTo("contractor-123")
                .build();

        // Act
        TaskResponseDTO result = taskMapper.entityToResponseDTO(task);

        // Assert
        assertNotNull(result);
        assertEquals("TASK-001", result.getTaskIdentifier());
        assertEquals(LocalDate.of(2025, 12, 5), result.getTaskDate());
        assertEquals("Foundation Work", result.getTaskDescription());
        assertEquals("Lot 53", result.getLotNumber());
        assertEquals("Wednesday", result.getDayOfWeek());
        assertEquals("contractor-123", result.getAssignedTo());
    }

    @Test
    void entitiesToResponseDTOs_shouldMapMultipleTasks() {
        // Arrange
        Task task1 = Task.builder()
                .taskIdentifier("TASK-001")
                .taskDate(LocalDate.of(2025, 12, 5))
                .taskDescription("Foundation Work")
                .lotNumber("Lot 53")
                .dayOfWeek("Wednesday")
                .assignedTo("contractor-123")
                .build();

        Task task2 = Task.builder()
                .taskIdentifier("TASK-002")
                .taskDate(LocalDate.of(2025, 12, 6))
                .taskDescription("Framing")
                .lotNumber("Lot 57")
                .dayOfWeek("Thursday")
                .assignedTo("contractor-456")
                .build();

        List<Task> tasks = Arrays.asList(task1, task2);

        // Act
        List<TaskResponseDTO> result = taskMapper.entitiesToResponseDTOs(tasks);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("TASK-001", result.get(0).getTaskIdentifier());
        assertEquals("TASK-002", result.get(1).getTaskIdentifier());
    }

    @Test
    void requestDTOToEntity_shouldMapAllFieldsAndGenerateIdentifier() {
        // Arrange
        TaskRequestDTO requestDTO = TaskRequestDTO.builder()
                .taskDate(LocalDate.of(2025, 12, 5))
                .taskDescription("Foundation Work")
                .lotNumber("Lot 53")
                .dayOfWeek("Wednesday")
                .assignedTo("contractor-123")
                .build();

        // Act
        Task result = taskMapper.requestDTOToEntity(requestDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getTaskIdentifier());
        assertTrue(result.getTaskIdentifier().startsWith("TASK-"));
        assertEquals(LocalDate.of(2025, 12, 5), result.getTaskDate());
        assertEquals("Foundation Work", result.getTaskDescription());
        assertEquals("Lot 53", result.getLotNumber());
        assertEquals("Wednesday", result.getDayOfWeek());
        assertEquals("contractor-123", result.getAssignedTo());
    }

    @Test
    void requestDTOToEntity_shouldHandleNullAssignedTo() {
        // Arrange
        TaskRequestDTO requestDTO = TaskRequestDTO.builder()
                .taskDate(LocalDate.of(2025, 12, 5))
                .taskDescription("Foundation Work")
                .lotNumber("Lot 53")
                .dayOfWeek("Wednesday")
                .assignedTo(null)
                .build();

        // Act
        Task result = taskMapper.requestDTOToEntity(requestDTO);

        // Assert
        assertNotNull(result);
        assertNull(result.getAssignedTo());
    }

    @Test
    void updateEntityFromRequestDTO_shouldUpdateAllFields() {
        // Arrange
        Task task = Task.builder()
                .id(1L)
                .taskIdentifier("TASK-001")
                .taskDate(LocalDate.of(2025, 12, 5))
                .taskDescription("Old Description")
                .lotNumber("Lot 53")
                .dayOfWeek("Wednesday")
                .assignedTo("old-contractor")
                .build();

        TaskRequestDTO requestDTO = TaskRequestDTO.builder()
                .taskDate(LocalDate.of(2025, 12, 10))
                .taskDescription("New Description")
                .lotNumber("Lot 60")
                .dayOfWeek("Friday")
                .assignedTo("new-contractor")
                .build();

        // Act
        taskMapper.updateEntityFromRequestDTO(task, requestDTO);

        // Assert
        assertEquals(1L, task.getId()); // Should not change
        assertEquals("TASK-001", task.getTaskIdentifier()); // Should not change
        assertEquals(LocalDate.of(2025, 12, 10), task.getTaskDate());
        assertEquals("New Description", task.getTaskDescription());
        assertEquals("Lot 60", task.getLotNumber());
        assertEquals("Friday", task.getDayOfWeek());
        assertEquals("new-contractor", task.getAssignedTo());
    }
}

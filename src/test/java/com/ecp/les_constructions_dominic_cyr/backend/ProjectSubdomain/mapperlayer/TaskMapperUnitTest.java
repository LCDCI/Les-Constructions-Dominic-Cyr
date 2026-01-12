package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.mapperlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Task;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskPriority;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.TaskMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskDetailResponseDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskRequestDTO;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskMapperUnitTest {

    private TaskMapper taskMapper;
    private Users testContractor;
    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        taskMapper = new TaskMapper();

        testContractor = new Users(
                UserIdentifier.newId(),
                "John",
                "Contractor",
                "john@contractor.com",
                null,
                "555-1234",
                UserRole.CONTRACTOR,
                "auth0|test"
        );

        task1 = Task.builder()
                .id(1)
                .taskIdentifier(new TaskIdentifier("TASK-001"))
                .taskStatus(TaskStatus.TO_DO)
                .taskTitle("Install Foundation")
                .periodStart(LocalDate.now())
                .periodEnd(LocalDate.now().plusDays(3))
                .taskDescription("Pour concrete foundation")
                .taskPriority(TaskPriority.HIGH)
                .estimatedHours(16.0)
                .hoursSpent(0.0)
                .taskProgress(0.0)
                .assignedTo(testContractor)
                .build();

        task2 = Task.builder()
                .id(2)
                .taskIdentifier(new TaskIdentifier("TASK-002"))
                .taskStatus(TaskStatus.IN_PROGRESS)
                .taskTitle("Framing Work")
                .periodStart(LocalDate.now())
                .periodEnd(LocalDate.now().plusDays(6))
                .taskDescription("Complete structural framing")
                .taskPriority(TaskPriority.VERY_HIGH)
                .estimatedHours(40.0)
                .hoursSpent(15.0)
                .taskProgress(37.5)
                .assignedTo(testContractor)
                .build();
    }

    @Test
    void entityToResponseDTO_shouldMapAllFieldsCorrectly() {
        TaskDetailResponseDTO result = taskMapper.entityToResponseDTO(task1);

        assertNotNull(result);
        assertEquals("TASK-001", result.getTaskId());
        assertEquals(TaskStatus.TO_DO, result.getTaskStatus());
        assertEquals("Install Foundation", result.getTaskTitle());
        assertEquals("Pour concrete foundation", result.getTaskDescription());
        assertEquals(TaskPriority.HIGH, result.getTaskPriority());
        assertEquals(16.0, result.getEstimatedHours());
        assertEquals(0.0, result.getHoursSpent());
        assertEquals(0.0, result.getTaskProgress());
        assertNotNull(result.getAssignedToUserId());
        assertEquals("John Contractor", result.getAssignedToUserName());
    }

    @Test
    void entityToResponseDTO_shouldHandleNullAssignedUser() {
        task1.setAssignedTo(null);

        TaskDetailResponseDTO result = taskMapper.entityToResponseDTO(task1);

        assertNotNull(result);
        assertEquals("TASK-001", result.getTaskId());
        assertNull(result.getAssignedToUserId());
        assertNull(result.getAssignedToUserName());
    }

    @Test
    void entitiesToResponseDTOs_shouldMapMultipleTasks() {
        List<Task> tasks = Arrays.asList(task1, task2);

        List<TaskDetailResponseDTO> result = taskMapper.entitiesToResponseDTOs(tasks);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("TASK-001", result.get(0).getTaskId());
        assertEquals("Install Foundation", result.get(0).getTaskTitle());
        assertEquals(TaskStatus.TO_DO, result.get(0).getTaskStatus());

        assertEquals("TASK-002", result.get(1).getTaskId());
        assertEquals("Framing Work", result.get(1).getTaskTitle());
        assertEquals(TaskStatus.IN_PROGRESS, result.get(1).getTaskStatus());
    }

    @Test
    void requestDTOToEntity_shouldCreateEntityWithAllFields() {
        TaskRequestDTO requestDTO = TaskRequestDTO.builder()
                .taskStatus(TaskStatus.TO_DO)
                .taskTitle("New Task")
                .periodStart(new Date())
                .periodEnd(new Date())
                .taskDescription("Task description")
                .taskPriority(TaskPriority.MEDIUM)
                .estimatedHours(10.0)
                .hoursSpent(0.0)
                .taskProgress(0.0)
                .assignedToUserId(testContractor.getUserIdentifier().getUserId().toString())
                .build();

        Task result = taskMapper.requestDTOToEntity(requestDTO, testContractor);

        assertNotNull(result);
        assertNotNull(result.getTaskIdentifier());
        assertEquals(TaskStatus.TO_DO, result.getTaskStatus());
        assertEquals("New Task", result.getTaskTitle());
        assertEquals("Task description", result.getTaskDescription());
        assertEquals(TaskPriority.MEDIUM, result.getTaskPriority());
        assertEquals(10.0, result.getEstimatedHours());
        assertEquals(0.0, result.getHoursSpent());
        assertEquals(0.0, result.getTaskProgress());
        assertEquals(testContractor, result.getAssignedTo());
    }

    @Test
    void updateEntityFromRequestDTO_shouldUpdateAllFields() {
        TaskRequestDTO requestDTO = TaskRequestDTO.builder()
                .taskStatus(TaskStatus.COMPLETED)
                .taskTitle("Updated Task")
                .periodStart(new Date())
                .periodEnd(new Date())
                .taskDescription("Updated description")
                .taskPriority(TaskPriority.LOW)
                .estimatedHours(20.0)
                .hoursSpent(20.0)
                .taskProgress(100.0)
                .assignedToUserId(testContractor.getUserIdentifier().getUserId().toString())
                .build();

        taskMapper.updateEntityFromRequestDTO(task1, requestDTO, testContractor);

        assertEquals(TaskStatus.COMPLETED, task1.getTaskStatus());
        assertEquals("Updated Task", task1.getTaskTitle());
        assertEquals("Updated description", task1.getTaskDescription());
        assertEquals(TaskPriority.LOW, task1.getTaskPriority());
        assertEquals(20.0, task1.getEstimatedHours());
        assertEquals(20.0, task1.getHoursSpent());
        assertEquals(100.0, task1.getTaskProgress());
        assertEquals(testContractor, task1.getAssignedTo());
    }
}

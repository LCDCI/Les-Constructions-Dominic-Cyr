package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Task.TaskServiceImpl;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Task.Task;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Task.TaskRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.TaskMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Task.TaskRequestDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Task.TaskResponseDTO;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplUnitTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task task1;
    private Task task2;
    private TaskResponseDTO responseDTO1;
    private TaskResponseDTO responseDTO2;
    private TaskRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        task1 = Task.builder()
                .id(1L)
                .taskIdentifier("TASK-001")
                .taskDate(LocalDate.now())
                .taskDescription("Foundation Work")
                .lotNumber("Lot 53")
                .dayOfWeek("Monday")
                .assignedTo("contractor-123")
                .build();

        task2 = Task.builder()
                .id(2L)
                .taskIdentifier("TASK-002")
                .taskDate(LocalDate.now().plusDays(1))
                .taskDescription("Framing")
                .lotNumber("Lot 57")
                .dayOfWeek("Tuesday")
                .assignedTo("contractor-456")
                .build();

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

    @Test
    void getCurrentWeekTasks_shouldReturnTasksForCurrentWeek() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<Task> tasks = Arrays.asList(task1, task2);
        List<TaskResponseDTO> responseDTOs = Arrays.asList(responseDTO1, responseDTO2);

        when(taskRepository.findCurrentWeekTasks(startOfWeek, endOfWeek)).thenReturn(tasks);
        when(taskMapper.entitiesToResponseDTOs(tasks)).thenReturn(responseDTOs);

        List<TaskResponseDTO> result = taskService.getCurrentWeekTasks();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("TASK-001", result.get(0).getTaskIdentifier());
        assertEquals("TASK-002", result.get(1).getTaskIdentifier());

        verify(taskRepository).findCurrentWeekTasks(startOfWeek, endOfWeek);
        verify(taskMapper).entitiesToResponseDTOs(tasks);
    }

    @Test
    void getCurrentWeekTasks_shouldReturnEmptyListWhenNoTasks() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        when(taskRepository.findCurrentWeekTasks(startOfWeek, endOfWeek)).thenReturn(Collections.emptyList());
        when(taskMapper.entitiesToResponseDTOs(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<TaskResponseDTO> result = taskService.getCurrentWeekTasks();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository).findCurrentWeekTasks(startOfWeek, endOfWeek);
        verify(taskMapper).entitiesToResponseDTOs(Collections.emptyList());
    }

    @Test
    void getAllTasks_shouldReturnAllTasks() {
        List<Task> tasks = Arrays.asList(task1, task2);
        List<TaskResponseDTO> responseDTOs = Arrays.asList(responseDTO1, responseDTO2);

        when(taskRepository.findAll()).thenReturn(tasks);
        when(taskMapper.entitiesToResponseDTOs(tasks)).thenReturn(responseDTOs);

        List<TaskResponseDTO> result = taskService.getAllTasks();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("TASK-001", result.get(0).getTaskIdentifier());
        assertEquals("TASK-002", result.get(1).getTaskIdentifier());

        verify(taskRepository).findAll();
        verify(taskMapper).entitiesToResponseDTOs(tasks);
    }

    @Test
    void getTaskByIdentifier_shouldReturnTaskWhenFound() {
        String identifier = "TASK-001";

        when(taskRepository.findByTaskIdentifier(identifier)).thenReturn(Optional.of(task1));
        when(taskMapper.entityToResponseDTO(task1)).thenReturn(responseDTO1);

        TaskResponseDTO result = taskService.getTaskByIdentifier(identifier);

        assertNotNull(result);
        assertEquals("TASK-001", result.getTaskIdentifier());
        assertEquals("Foundation Work", result.getTaskDescription());
        assertEquals("Lot 53", result.getLotNumber());

        verify(taskRepository).findByTaskIdentifier(identifier);
        verify(taskMapper).entityToResponseDTO(task1);
    }

    @Test
    void getTaskByIdentifier_shouldThrowExceptionWhenNotFound() {
        String identifier = "TASK-999";

        when(taskRepository.findByTaskIdentifier(identifier)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            taskService.getTaskByIdentifier(identifier);
        });

        assertEquals("Task not found with identifier: TASK-999", exception.getMessage());

        verify(taskRepository).findByTaskIdentifier(identifier);
        verify(taskMapper, never()).entityToResponseDTO(any());
    }

    @Test
    void createTask_shouldCreateTaskWithValidContractor() {
        UserResponseModel contractor = new UserResponseModel();
        contractor.setUserRole(UserRole.CONTRACTOR);

        when(userService.getUserById("contractor-123")).thenReturn(contractor);
        when(taskMapper.requestDTOToEntity(requestDTO)).thenReturn(task1);
        when(taskRepository.save(task1)).thenReturn(task1);
        when(taskMapper.entityToResponseDTO(task1)).thenReturn(responseDTO1);

        TaskResponseDTO result = taskService.createTask(requestDTO);

        assertNotNull(result);
        assertEquals("TASK-001", result.getTaskIdentifier());

        verify(userService).getUserById("contractor-123");
        verify(taskMapper).requestDTOToEntity(requestDTO);
        verify(taskRepository).save(task1);
        verify(taskMapper).entityToResponseDTO(task1);
    }

    @Test
    void createTask_shouldThrowExceptionForNonContractorAssignment() {
        UserResponseModel customer = new UserResponseModel();
        customer.setUserRole(UserRole.CUSTOMER);

        when(userService.getUserById("contractor-123")).thenReturn(customer);

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            taskService.createTask(requestDTO);
        });

        assertTrue(exception.getMessage().contains("is not a contractor"));

        verify(userService).getUserById("contractor-123");
        verify(taskMapper, never()).requestDTOToEntity(any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_shouldCreateTaskWithNullAssignedTo() {
        TaskRequestDTO requestDTONoAssignment = TaskRequestDTO.builder()
                .taskDate(LocalDate.now())
                .taskDescription("New Task")
                .lotNumber("Lot 100")
                .dayOfWeek("Wednesday")
                .assignedTo(null)
                .build();

        Task taskNoAssignment = Task.builder()
                .taskIdentifier("TASK-003")
                .taskDate(LocalDate.now())
                .taskDescription("New Task")
                .lotNumber("Lot 100")
                .dayOfWeek("Wednesday")
                .assignedTo(null)
                .build();

        TaskResponseDTO responseDTONoAssignment = TaskResponseDTO.builder()
                .taskIdentifier("TASK-003")
                .taskDate(LocalDate.now())
                .taskDescription("New Task")
                .lotNumber("Lot 100")
                .dayOfWeek("Wednesday")
                .assignedTo(null)
                .build();

        when(taskMapper.requestDTOToEntity(requestDTONoAssignment)).thenReturn(taskNoAssignment);
        when(taskRepository.save(taskNoAssignment)).thenReturn(taskNoAssignment);
        when(taskMapper.entityToResponseDTO(taskNoAssignment)).thenReturn(responseDTONoAssignment);

        TaskResponseDTO result = taskService.createTask(requestDTONoAssignment);

        assertNotNull(result);
        assertNull(result.getAssignedTo());

        verify(userService, never()).getUserById(any());
        verify(taskMapper).requestDTOToEntity(requestDTONoAssignment);
        verify(taskRepository).save(taskNoAssignment);
    }

    @Test
    void updateTask_shouldUpdateTaskWithValidContractor() {
        String identifier = "TASK-001";
        UserResponseModel contractor = new UserResponseModel();
        contractor.setUserRole(UserRole.CONTRACTOR);

        when(taskRepository.findByTaskIdentifier(identifier)).thenReturn(Optional.of(task1));
        when(userService.getUserById("contractor-123")).thenReturn(contractor);
        when(taskRepository.save(task1)).thenReturn(task1);
        when(taskMapper.entityToResponseDTO(task1)).thenReturn(responseDTO1);

        TaskResponseDTO result = taskService.updateTask(identifier, requestDTO);

        assertNotNull(result);

        verify(taskRepository).findByTaskIdentifier(identifier);
        verify(userService).getUserById("contractor-123");
        verify(taskMapper).updateEntityFromRequestDTO(task1, requestDTO);
        verify(taskRepository).save(task1);
    }

    @Test
    void updateTask_shouldThrowExceptionWhenTaskNotFound() {
        String identifier = "TASK-999";

        when(taskRepository.findByTaskIdentifier(identifier)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            taskService.updateTask(identifier, requestDTO);
        });

        assertEquals("Task not found with identifier: TASK-999", exception.getMessage());

        verify(taskRepository).findByTaskIdentifier(identifier);
        verify(taskMapper, never()).updateEntityFromRequestDTO(any(), any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void deleteTask_shouldDeleteExistingTask() {
        String identifier = "TASK-001";

        when(taskRepository.findByTaskIdentifier(identifier)).thenReturn(Optional.of(task1));

        taskService.deleteTask(identifier);

        verify(taskRepository).findByTaskIdentifier(identifier);
        verify(taskRepository).delete(task1);
    }

    @Test
    void deleteTask_shouldThrowExceptionWhenTaskNotFound() {
        String identifier = "TASK-999";

        when(taskRepository.findByTaskIdentifier(identifier)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            taskService.deleteTask(identifier);
        });

        assertEquals("Task not found with identifier: TASK-999", exception.getMessage());

        verify(taskRepository).findByTaskIdentifier(identifier);
        verify(taskRepository, never()).delete(any());
    }

    @Test
    void getTasksAssignedToContractor_shouldReturnContractorTasks() {
        String contractorId = "contractor-123";
        List<Task> tasks = Arrays.asList(task1);
        List<TaskResponseDTO> responseDTOs = Arrays.asList(responseDTO1);

        when(taskRepository.findByAssignedTo(contractorId)).thenReturn(tasks);
        when(taskMapper.entitiesToResponseDTOs(tasks)).thenReturn(responseDTOs);

        List<TaskResponseDTO> result = taskService.getTasksAssignedToContractor(contractorId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("contractor-123", result.get(0).getAssignedTo());

        verify(taskRepository).findByAssignedTo(contractorId);
        verify(taskMapper).entitiesToResponseDTOs(tasks);
    }

    @Test
    void getCurrentWeekTasksAssignedToContractor_shouldReturnContractorWeekTasks() {
        String contractorId = "contractor-123";
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<Task> tasks = Arrays.asList(task1);
        List<TaskResponseDTO> responseDTOs = Arrays.asList(responseDTO1);

        when(taskRepository.findCurrentWeekTasksByAssignedTo(contractorId, startOfWeek, endOfWeek)).thenReturn(tasks);
        when(taskMapper.entitiesToResponseDTOs(tasks)).thenReturn(responseDTOs);

        List<TaskResponseDTO> result = taskService.getCurrentWeekTasksAssignedToContractor(contractorId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("contractor-123", result.get(0).getAssignedTo());

        verify(taskRepository).findCurrentWeekTasksByAssignedTo(contractorId, startOfWeek, endOfWeek);
        verify(taskMapper).entitiesToResponseDTOs(tasks);
    }
}

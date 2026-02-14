package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.NotificationService;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.MailerServiceClient;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule.TaskServiceImpl;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Task;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskPriority;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.TaskMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskDetailResponseDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskRequestDTO;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private UsersRepository usersRepository;

    @Mock
    private LotRepository lotRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private MailerServiceClient mailerServiceClient;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task task1;
    private Task task2;
    private TaskDetailResponseDTO responseDTO1;
    private TaskDetailResponseDTO responseDTO2;
    private TaskRequestDTO requestDTO;
    private Users contractor;

    @BeforeEach
    void setUp() {
        UUID contractorId = UUID.randomUUID();
        contractor = new Users();
        contractor.setUserRole(UserRole.CONTRACTOR);

        TaskIdentifier taskId1 = new TaskIdentifier("TASK-001");
        task1 = Task.builder()
                .id(1)
                .taskIdentifier(taskId1)
                .taskStatus(TaskStatus.IN_PROGRESS)
                .taskTitle("Foundation Work")
                .periodStart(LocalDate.now())
                .periodEnd(LocalDate.now().plusDays(7))
                .taskDescription("Complete foundation work")
                .taskPriority(TaskPriority.HIGH)
                .estimatedHours(40.0)
                .hoursSpent(20.0)
                .taskProgress(50.0)
                .assignedTo(contractor)
                .scheduleId("SCH-001")
                .build();

        TaskIdentifier taskId2 = new TaskIdentifier("TASK-002");
        task2 = Task.builder()
                .id(2)
                .taskIdentifier(taskId2)
                .taskStatus(TaskStatus.TO_DO)
                .taskTitle("Framing")
                .periodStart(LocalDate.now().plusDays(7))
                .periodEnd(LocalDate.now().plusDays(14))
                .taskDescription("Frame the building")
                .taskPriority(TaskPriority.MEDIUM)
                .estimatedHours(60.0)
                .hoursSpent(0.0)
                .taskProgress(0.0)
                .assignedTo(contractor)
                .scheduleId("SCH-002")
                .build();

        responseDTO1 = TaskDetailResponseDTO.builder()
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
                .assignedToUserId(contractorId.toString())
                .scheduleId("SCH-001")
                .build();

        responseDTO2 = TaskDetailResponseDTO.builder()
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
                .assignedToUserId(contractorId.toString())
                .scheduleId("SCH-002")
                .build();

        requestDTO = TaskRequestDTO.builder()
                .taskStatus(TaskStatus.TO_DO)
                .taskTitle("New Task")
                .periodStart(new Date())
                .periodEnd(new Date())
                .taskDescription("Task description")
                .taskPriority(TaskPriority.HIGH)
                .estimatedHours(30.0)
                .hoursSpent(0.0)
                .taskProgress(0.0)
                .assignedToUserId(contractorId.toString())
                .scheduleId("SCH-001")
                .build();
    }

    @Test
    void getAllTasks_shouldReturnAllTasks() {
        List<Task> tasks = Arrays.asList(task1, task2);
        List<TaskDetailResponseDTO> responseDTOs = Arrays.asList(responseDTO1, responseDTO2);

        when(taskRepository.findAll()).thenReturn(tasks);
        when(taskMapper.entitiesToResponseDTOs(tasks)).thenReturn(responseDTOs);

        List<TaskDetailResponseDTO> result = taskService.getAllTasks();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("TASK-001", result.get(0).getTaskId());
        assertEquals("TASK-002", result.get(1).getTaskId());

        verify(taskRepository).findAll();
        verify(taskMapper).entitiesToResponseDTOs(tasks);
    }

    @Test
    void getAllTasks_shouldReturnEmptyListWhenNoTasks() {
        when(taskRepository.findAll()).thenReturn(Collections.emptyList());
        when(taskMapper.entitiesToResponseDTOs(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<TaskDetailResponseDTO> result = taskService.getAllTasks();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository).findAll();
        verify(taskMapper).entitiesToResponseDTOs(Collections.emptyList());
    }

    @Test
    void getTaskByIdentifier_shouldReturnTaskWhenFound() {
        String taskId = "TASK-001";

        when(taskRepository.findByTaskIdentifier_TaskId(taskId)).thenReturn(Optional.of(task1));
        when(taskMapper.entityToResponseDTO(task1)).thenReturn(responseDTO1);

        TaskDetailResponseDTO result = taskService.getTaskByIdentifier(taskId);

        assertNotNull(result);
        assertEquals("TASK-001", result.getTaskId());
        assertEquals("Foundation Work", result.getTaskTitle());
        assertEquals(TaskStatus.IN_PROGRESS, result.getTaskStatus());

        verify(taskRepository).findByTaskIdentifier_TaskId(taskId);
        verify(taskMapper).entityToResponseDTO(task1);
    }

    @Test
    void getTaskByIdentifier_shouldThrowNotFoundExceptionWhenTaskNotFound() {
        String taskId = "TASK-999";

        when(taskRepository.findByTaskIdentifier_TaskId(taskId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            taskService.getTaskByIdentifier(taskId);
        });

        assertEquals("Task not found with identifier: TASK-999", exception.getMessage());

        verify(taskRepository).findByTaskIdentifier_TaskId(taskId);
        verify(taskMapper, never()).entityToResponseDTO(any());
    }

    @Test
    void createTask_shouldCreateTaskSuccessfullyWithAssignedUser() {
        UUID userId = UUID.randomUUID();
        requestDTO.setAssignedToUserId(userId.toString());

        when(usersRepository.findByUserIdentifier_UserId(userId)).thenReturn(Optional.of(contractor));
        when(taskMapper.requestDTOToEntity(requestDTO, contractor)).thenReturn(task1);
        when(taskRepository.save(task1)).thenReturn(task1);
        when(taskMapper.entityToResponseDTO(task1)).thenReturn(responseDTO1);

        TaskDetailResponseDTO result = taskService.createTask(requestDTO);

        assertNotNull(result);
        assertEquals("TASK-001", result.getTaskId());
        assertEquals("Foundation Work", result.getTaskTitle());

        verify(usersRepository).findByUserIdentifier_UserId(userId);
        verify(taskMapper).requestDTOToEntity(requestDTO, contractor);
        verify(taskRepository).save(task1);
        verify(taskMapper).entityToResponseDTO(task1);
    }

    @Test
    void createTask_shouldCreateTaskSuccessfullyWithoutAssignedUser() {
        requestDTO.setAssignedToUserId(null);

        when(taskMapper.requestDTOToEntity(requestDTO, null)).thenReturn(task1);
        when(taskRepository.save(task1)).thenReturn(task1);
        when(taskMapper.entityToResponseDTO(task1)).thenReturn(responseDTO1);

        TaskDetailResponseDTO result = taskService.createTask(requestDTO);

        assertNotNull(result);
        assertEquals("TASK-001", result.getTaskId());

        verify(usersRepository, never()).findByUserIdentifier_UserId(any());
        verify(taskMapper).requestDTOToEntity(requestDTO, null);
        verify(taskRepository).save(task1);
        verify(taskMapper).entityToResponseDTO(task1);
    }

    @Test
    void createTask_shouldThrowInvalidInputExceptionWhenTaskTitleIsBlank() {
        requestDTO.setTaskTitle("");

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            taskService.createTask(requestDTO);
        });

        assertEquals("Task title must not be blank", exception.getMessage());

        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_shouldThrowInvalidInputExceptionWhenTaskDescriptionIsBlank() {
        requestDTO.setTaskDescription("");

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            taskService.createTask(requestDTO);
        });

        assertEquals("Task description must not be blank", exception.getMessage());

        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_shouldThrowInvalidInputExceptionWhenTaskStatusIsNull() {
        requestDTO.setTaskStatus(null);

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            taskService.createTask(requestDTO);
        });

        assertEquals("Task status must not be null", exception.getMessage());

        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_shouldThrowInvalidInputExceptionWhenTaskPriorityIsNull() {
        requestDTO.setTaskPriority(null);

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            taskService.createTask(requestDTO);
        });

        assertEquals("Task priority must not be null", exception.getMessage());

        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_shouldThrowInvalidInputExceptionWhenPeriodStartIsNull() {
        requestDTO.setPeriodStart(null);

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            taskService.createTask(requestDTO);
        });

        assertEquals("Period start must not be null", exception.getMessage());

        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_shouldThrowInvalidInputExceptionWhenPeriodEndIsNull() {
        requestDTO.setPeriodEnd(null);

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            taskService.createTask(requestDTO);
        });

        assertEquals("Period end must not be null", exception.getMessage());

        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_shouldThrowInvalidInputExceptionWhenScheduleIdIsBlank() {
        requestDTO.setScheduleId("");

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            taskService.createTask(requestDTO);
        });

        assertEquals("Schedule identifier must not be null or blank", exception.getMessage());

        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_shouldThrowNotFoundExceptionWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        requestDTO.setAssignedToUserId(userId.toString());

        when(usersRepository.findByUserIdentifier_UserId(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            taskService.createTask(requestDTO);
        });

        assertTrue(exception.getMessage().contains("User not found with identifier"));

        verify(usersRepository).findByUserIdentifier_UserId(userId);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_shouldThrowInvalidInputExceptionWhenUserIsNotContractor() {
        UUID userId = UUID.randomUUID();
        requestDTO.setAssignedToUserId(userId.toString());

        Users nonContractor = new Users();
        nonContractor.setUserRole(UserRole.SALESPERSON);

        when(usersRepository.findByUserIdentifier_UserId(userId)).thenReturn(Optional.of(nonContractor));

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            taskService.createTask(requestDTO);
        });

        assertTrue(exception.getMessage().contains("Only contractors can be assigned to tasks"));

        verify(usersRepository).findByUserIdentifier_UserId(userId);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_shouldUpdateTaskSuccessfully() {
        String taskId = "TASK-001";
        UUID userId = UUID.randomUUID();
        requestDTO.setAssignedToUserId(userId.toString());

        when(taskRepository.findByTaskIdentifier_TaskId(taskId)).thenReturn(Optional.of(task1));
        when(usersRepository.findByUserIdentifier_UserId(userId)).thenReturn(Optional.of(contractor));
        when(taskRepository.save(task1)).thenReturn(task1);
        when(taskMapper.entityToResponseDTO(task1)).thenReturn(responseDTO1);

        TaskDetailResponseDTO result = taskService.updateTask(taskId, requestDTO);

        assertNotNull(result);
        assertEquals("TASK-001", result.getTaskId());

        verify(taskRepository).findByTaskIdentifier_TaskId(taskId);
        verify(usersRepository).findByUserIdentifier_UserId(userId);
        verify(taskMapper).updateEntityFromRequestDTO(task1, requestDTO, contractor);
        verify(taskRepository).save(task1);
        verify(taskMapper).entityToResponseDTO(task1);
    }

    @Test
    void updateTask_shouldUpdateTaskSuccessfullyWithoutAssignedUser() {
        String taskId = "TASK-001";
        requestDTO.setAssignedToUserId(null);

        when(taskRepository.findByTaskIdentifier_TaskId(taskId)).thenReturn(Optional.of(task1));
        when(taskRepository.save(task1)).thenReturn(task1);
        when(taskMapper.entityToResponseDTO(task1)).thenReturn(responseDTO1);

        TaskDetailResponseDTO result = taskService.updateTask(taskId, requestDTO);

        assertNotNull(result);
        assertEquals("TASK-001", result.getTaskId());

        verify(taskRepository).findByTaskIdentifier_TaskId(taskId);
        verify(usersRepository, never()).findByUserIdentifier_UserId(any());
        verify(taskMapper).updateEntityFromRequestDTO(task1, requestDTO, null);
        verify(taskRepository).save(task1);
        verify(taskMapper).entityToResponseDTO(task1);
    }

    @Test
    void updateTask_shouldThrowNotFoundExceptionWhenTaskNotFound() {
        String taskId = "TASK-999";

        when(taskRepository.findByTaskIdentifier_TaskId(taskId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            taskService.updateTask(taskId, requestDTO);
        });

        assertEquals("Task not found with identifier: TASK-999", exception.getMessage());

        verify(taskRepository).findByTaskIdentifier_TaskId(taskId);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_shouldThrowInvalidInputExceptionWhenTaskTitleIsBlank() {
        String taskId = "TASK-001";
        requestDTO.setTaskTitle("");

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            taskService.updateTask(taskId, requestDTO);
        });

        assertEquals("Task title must not be blank", exception.getMessage());

        verify(taskRepository, never()).findByTaskIdentifier_TaskId(any());
    }

    @Test
    void updateTask_shouldSendNotificationsToAssignedLotUsers() {
        // Setup
        String taskId = "TASK-001";
        UUID lotId = UUID.randomUUID();
        requestDTO.setAssignedToUserId(null); // No user assignment change in this test
        
        // Create users
        Users user1 = new Users();
        user1.setUserIdentifier(com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier.newId());
        user1.setFirstName("John");
        user1.setPrimaryEmail("john@example.com");
        
        Users user2 = new Users();
        user2.setUserIdentifier(com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier.newId());
        user2.setFirstName("Jane");
        user2.setPrimaryEmail("jane@example.com");
        
        // Create project
        Project project = new Project();
        project.setProjectIdentifier("PROJ-001");
        
        // Create lot with assigned users and project
        Lot lot = new Lot();
        lot.setProject(project);
        lot.setAssignedUsers(Arrays.asList(user1, user2));
        
        // Setup task with lot ID
        task1.setLotId(lotId);
        
        // Mock behavior
        when(taskRepository.findByTaskIdentifier_TaskId(taskId)).thenReturn(Optional.of(task1));
        when(taskRepository.save(task1)).thenReturn(task1);
        when(taskMapper.entityToResponseDTO(task1)).thenReturn(responseDTO1);
        when(lotRepository.findByLotIdentifier_LotId(lotId)).thenReturn(lot);
        when(notificationService.createNotification(any(UUID.class), anyString(), anyString(), any(), anyString())).thenReturn(null);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString())).thenReturn(Mono.empty());
        
        // Execute
        TaskDetailResponseDTO result = taskService.updateTask(taskId, requestDTO);
        
        // Verify
        assertNotNull(result);
        assertEquals("TASK-001", result.getTaskId());
        
        // Verify notifications were sent to both users
        verify(notificationService, times(2)).createNotification(
            any(UUID.class), 
            eq("Task Updated"), 
            anyString(), 
            any(), 
            eq("/projects/PROJ-001/lots/" + lotId + "/metadata")
        );
        
        // Verify emails were sent to both users
        verify(mailerServiceClient, times(2)).sendEmail(
            anyString(), 
            eq("Task Updated"), 
            anyString(), 
            eq("Les Constructions Dominic Cyr")
        );
    }

    @Test
    void updateTask_shouldHandleNotificationFailureGracefully() {
        // Setup
        String taskId = "TASK-001";
        UUID lotId = UUID.randomUUID();
        requestDTO.setAssignedToUserId(null); // No user assignment change
        
        Users user1 = new Users();
        user1.setUserIdentifier(com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier.newId());
        user1.setFirstName("John");
        user1.setPrimaryEmail("john@example.com");
        
        Project project = new Project();
        project.setProjectIdentifier("PROJ-001");
        
        Lot lot = new Lot();
        lot.setProject(project);
        lot.setAssignedUsers(Arrays.asList(user1));
        
        task1.setLotId(lotId);
        
        when(taskRepository.findByTaskIdentifier_TaskId(taskId)).thenReturn(Optional.of(task1));
        when(taskRepository.save(task1)).thenReturn(task1);
        when(taskMapper.entityToResponseDTO(task1)).thenReturn(responseDTO1);
        when(lotRepository.findByLotIdentifier_LotId(lotId)).thenReturn(lot);
        when(notificationService.createNotification(any(UUID.class), anyString(), anyString(), any(), anyString()))
            .thenThrow(new RuntimeException("Notification service down"));
        
        // Execute - should not throw exception
        TaskDetailResponseDTO result = taskService.updateTask(taskId, requestDTO);
        
        // Verify - task update succeeds even if notification fails
        assertNotNull(result);
        assertEquals("TASK-001", result.getTaskId());
        
        verify(notificationService).createNotification(any(UUID.class), anyString(), anyString(), any(), anyString());
    }

    @Test
    void deleteTask_shouldDeleteTaskSuccessfully() {
        String taskId = "TASK-001";

        when(taskRepository.findByTaskIdentifier_TaskId(taskId)).thenReturn(Optional.of(task1));

        taskService.deleteTask(taskId);

        verify(taskRepository).findByTaskIdentifier_TaskId(taskId);
        verify(taskRepository).delete(task1);
    }

    @Test
    void deleteTask_shouldThrowNotFoundExceptionWhenTaskNotFound() {
        String taskId = "TASK-999";

        when(taskRepository.findByTaskIdentifier_TaskId(taskId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            taskService.deleteTask(taskId);
        });

        assertEquals("Task not found with identifier: TASK-999", exception.getMessage());

        verify(taskRepository).findByTaskIdentifier_TaskId(taskId);
        verify(taskRepository, never()).delete(any());
    }

    @Test
    void getTasksForContractor_shouldReturnTasksForContractor() {
        UUID contractorId = UUID.randomUUID();
        String contractorIdStr = contractorId.toString();
        List<Task> tasks = Arrays.asList(task1, task2);
        List<TaskDetailResponseDTO> responseDTOs = Arrays.asList(responseDTO1, responseDTO2);

        when(usersRepository.findByUserIdentifier_UserId(contractorId)).thenReturn(Optional.of(contractor));
        when(taskRepository.findByAssignedTo(contractor)).thenReturn(tasks);
        when(taskMapper.entitiesToResponseDTOs(tasks)).thenReturn(responseDTOs);

        List<TaskDetailResponseDTO> result = taskService.getTasksForContractor(contractorIdStr);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(usersRepository).findByUserIdentifier_UserId(contractorId);
        verify(taskRepository).findByAssignedTo(contractor);
        verify(taskMapper).entitiesToResponseDTOs(tasks);
    }

    @Test
    void getTasksForContractor_shouldReturnEmptyListWhenNoTasks() {
        UUID contractorId = UUID.randomUUID();
        String contractorIdStr = contractorId.toString();

        when(usersRepository.findByUserIdentifier_UserId(contractorId)).thenReturn(Optional.of(contractor));
        when(taskRepository.findByAssignedTo(contractor)).thenReturn(Collections.emptyList());
        when(taskMapper.entitiesToResponseDTOs(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<TaskDetailResponseDTO> result = taskService.getTasksForContractor(contractorIdStr);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(usersRepository).findByUserIdentifier_UserId(contractorId);
        verify(taskRepository).findByAssignedTo(contractor);
        verify(taskMapper).entitiesToResponseDTOs(Collections.emptyList());
    }

    @Test
    void getTasksForContractor_shouldThrowNotFoundExceptionWhenContractorNotFound() {
        UUID contractorId = UUID.randomUUID();
        String contractorIdStr = contractorId.toString();

        when(usersRepository.findByUserIdentifier_UserId(contractorId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            taskService.getTasksForContractor(contractorIdStr);
        });

        assertTrue(exception.getMessage().contains("User not found with identifier"));

        verify(usersRepository).findByUserIdentifier_UserId(contractorId);
        verify(taskRepository, never()).findByAssignedTo(any());
    }

    @Test
    void getTasksForContractor_shouldThrowInvalidInputExceptionWhenUserIsNotContractor() {
        UUID userId = UUID.randomUUID();
        String userIdStr = userId.toString();

        Users nonContractor = new Users();
        nonContractor.setUserRole(UserRole.CUSTOMER);

        when(usersRepository.findByUserIdentifier_UserId(userId)).thenReturn(Optional.of(nonContractor));

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            taskService.getTasksForContractor(userIdStr);
        });

        assertTrue(exception.getMessage().contains("User is not a contractor"));

        verify(usersRepository).findByUserIdentifier_UserId(userId);
        verify(taskRepository, never()).findByAssignedTo(any());
    }

    @Test
    void getTasksForSchedule_shouldReturnTasksForSchedule() {
        String scheduleIdentifier = "SCH-001";
        List<Task> tasks = Arrays.asList(task1);
        List<TaskDetailResponseDTO> responseDTOs = Arrays.asList(responseDTO1);

        when(taskRepository.findByScheduleId(scheduleIdentifier)).thenReturn(tasks);
        when(taskMapper.entitiesToResponseDTOs(tasks)).thenReturn(responseDTOs);

        List<TaskDetailResponseDTO> result = taskService.getTasksForSchedule(scheduleIdentifier);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TASK-001", result.get(0).getTaskId());
        assertEquals(scheduleIdentifier, result.get(0).getScheduleId());

        verify(taskRepository).findByScheduleId(scheduleIdentifier);
        verify(taskMapper).entitiesToResponseDTOs(tasks);
    }

    @Test
    void getTasksForSchedule_shouldReturnEmptyListWhenNoTasksForSchedule() {
        String scheduleIdentifier = "SCH-999";

        when(taskRepository.findByScheduleId(scheduleIdentifier)).thenReturn(Collections.emptyList());
        when(taskMapper.entitiesToResponseDTOs(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<TaskDetailResponseDTO> result = taskService.getTasksForSchedule(scheduleIdentifier);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository).findByScheduleId(scheduleIdentifier);
        verify(taskMapper).entitiesToResponseDTOs(Collections.emptyList());
    }

    @Test
    void getTasksForScheduleByStatus_shouldReturnFilteredTasks() {
        String scheduleIdentifier = "SCH-001";
        TaskStatus status = TaskStatus.COMPLETED;

        Task completedTask = Task.builder()
                .id(3)
                .taskIdentifier(new TaskIdentifier("TASK-003"))
                .taskStatus(TaskStatus.COMPLETED)
                .taskTitle("Completed Task")
                .scheduleId(scheduleIdentifier)
                .build();

        List<Task> tasks = Arrays.asList(completedTask);
        TaskDetailResponseDTO completedDTO = TaskDetailResponseDTO.builder()
                .taskId("TASK-003")
                .taskStatus(TaskStatus.COMPLETED)
                .scheduleId(scheduleIdentifier)
                .build();
        List<TaskDetailResponseDTO> responseDTOs = Arrays.asList(completedDTO);

        when(taskRepository.findByScheduleIdAndTaskStatus(scheduleIdentifier, status)).thenReturn(tasks);
        when(taskMapper.entitiesToResponseDTOs(tasks)).thenReturn(responseDTOs);

        List<TaskDetailResponseDTO> result = taskService.getTasksForScheduleByStatus(scheduleIdentifier, status);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TaskStatus.COMPLETED, result.get(0).getTaskStatus());

        verify(taskRepository).findByScheduleIdAndTaskStatus(scheduleIdentifier, status);
        verify(taskMapper).entitiesToResponseDTOs(tasks);
    }

    @Test
    void getTasksForProject_shouldReturnAllTasksForProject() {
        String projectIdentifier = "PRJ-001";
        List<Task> tasks = Arrays.asList(task1, task2);
        List<TaskDetailResponseDTO> responseDTOs = Arrays.asList(responseDTO1, responseDTO2);

        when(taskRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(tasks);
        when(taskMapper.entitiesToResponseDTOs(tasks)).thenReturn(responseDTOs);

        List<TaskDetailResponseDTO> result = taskService.getTasksForProject(projectIdentifier);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(taskRepository).findByProjectIdentifier(projectIdentifier);
        verify(taskMapper).entitiesToResponseDTOs(tasks);
    }

    @Test
    void getTasksForProjectByStatus_shouldReturnFilteredTasksForProject() {
        String projectIdentifier = "PRJ-001";
        TaskStatus status = TaskStatus.IN_PROGRESS;
        List<Task> tasks = Arrays.asList(task1);
        List<TaskDetailResponseDTO> responseDTOs = Arrays.asList(responseDTO1);

        when(taskRepository.findByProjectIdentifierAndTaskStatus(projectIdentifier, status)).thenReturn(tasks);
        when(taskMapper.entitiesToResponseDTOs(tasks)).thenReturn(responseDTOs);

        List<TaskDetailResponseDTO> result = taskService.getTasksForProjectByStatus(projectIdentifier, status);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TaskStatus.IN_PROGRESS, result.get(0).getTaskStatus());

        verify(taskRepository).findByProjectIdentifierAndTaskStatus(projectIdentifier, status);
        verify(taskMapper).entitiesToResponseDTOs(tasks);
    }

    @Test
    void taskProgressCalculation_shouldBeBasedOnHoursSpentOverEstimatedHours() {
        // Test that task progress is calculated correctly
        Task testTask = Task.builder()
                .estimatedHours(100.0)
                .hoursSpent(50.0)
                .build();

        double progress = testTask.calculateProgress();

        assertEquals(50.0, progress, 0.01);
    }

    @Test
    void taskProgressCalculation_shouldReturnZeroWhenEstimatedHoursIsNull() {
        Task testTask = Task.builder()
                .estimatedHours(null)
                .hoursSpent(50.0)
                .build();

        double progress = testTask.calculateProgress();

        assertEquals(0.0, progress, 0.01);
    }

    @Test
    void taskProgressCalculation_shouldClampProgressAt100() {
        Task testTask = Task.builder()
                .estimatedHours(100.0)
                .hoursSpent(150.0)
                .build();

        double progress = testTask.calculateProgress();

        assertEquals(100.0, progress, 0.01);
    }

    @Test
    void getTasksForUserAssignedLots_shouldReturnTasksForAssignedLots() {
        String userId = UUID.randomUUID().toString();
        UUID userUuid = UUID.fromString(userId);
        UUID lotId1 = UUID.randomUUID();
        UUID lotId2 = UUID.randomUUID();

        // Setup lots
        Lot lot1 = new Lot();
        lot1.setLotIdentifier(new com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier(lotId1.toString()));
        Lot lot2 = new Lot();
        lot2.setLotIdentifier(new com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier(lotId2.toString()));

        List<Lot> assignedLots = Arrays.asList(lot1, lot2);

        // Setup tasks
        Task task1 = Task.builder()
                .taskIdentifier(new TaskIdentifier("TASK-001"))
                .taskTitle("Task 1")
                .lotId(lotId1)
                .build();
        Task task2 = Task.builder()
                .taskIdentifier(new TaskIdentifier("TASK-002"))
                .taskTitle("Task 2")
                .lotId(lotId2)
                .build();

        List<Task> tasksLot1 = Collections.singletonList(task1);
        List<Task> tasksLot2 = Collections.singletonList(task2);

        TaskDetailResponseDTO responseDTO1 = TaskDetailResponseDTO.builder()
                .taskId("TASK-001")
                .taskTitle("Task 1")
                .build();
        TaskDetailResponseDTO responseDTO2 = TaskDetailResponseDTO.builder()
                .taskId("TASK-002")
                .taskTitle("Task 2")
                .build();

        when(lotRepository.findByAssignedUserId(userUuid)).thenReturn(assignedLots);
        when(taskRepository.findByLotId(lotId1)).thenReturn(tasksLot1);
        when(taskRepository.findByLotId(lotId2)).thenReturn(tasksLot2);
        when(taskMapper.entitiesToResponseDTOs(anyList())).thenReturn(Arrays.asList(responseDTO1, responseDTO2));

        List<TaskDetailResponseDTO> result = taskService.getTasksForUserAssignedLots(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(lotRepository).findByAssignedUserId(userUuid);
        verify(taskRepository).findByLotId(lotId1);
        verify(taskRepository).findByLotId(lotId2);
    }

    @Test
    void getTasksForUserAssignedLots_shouldReturnEmptyListWhenNoAssignedLots() {
        String userId = UUID.randomUUID().toString();
        UUID userUuid = UUID.fromString(userId);

        when(lotRepository.findByAssignedUserId(userUuid)).thenReturn(Collections.emptyList());

        List<TaskDetailResponseDTO> result = taskService.getTasksForUserAssignedLots(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(lotRepository).findByAssignedUserId(userUuid);
        verify(taskRepository, never()).findByLotId(any());
    }

    @Test
    void getTasksForUserAssignedLots_shouldReturnEmptyListWhenLotsHaveNoTasks() {
        String userId = UUID.randomUUID().toString();
        UUID userUuid = UUID.fromString(userId);
        UUID lotId1 = UUID.randomUUID();

        Lot lot1 = new Lot();
        lot1.setLotIdentifier(new com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier(lotId1.toString()));

        when(lotRepository.findByAssignedUserId(userUuid)).thenReturn(Collections.singletonList(lot1));
        when(taskRepository.findByLotId(lotId1)).thenReturn(Collections.emptyList());
        when(taskMapper.entitiesToResponseDTOs(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<TaskDetailResponseDTO> result = taskService.getTasksForUserAssignedLots(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(lotRepository).findByAssignedUserId(userUuid);
        verify(taskRepository).findByLotId(lotId1);
    }
}

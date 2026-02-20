package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.NotificationService;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.MailerServiceClient;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.NotificationCategory;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Schedule;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.ScheduleRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Task;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UsersRepository usersRepository;
    private final LotRepository lotRepository;
    private final ScheduleRepository scheduleRepository;
    private final NotificationService notificationService;
    private final MailerServiceClient mailerServiceClient;

    @Override
    public List<TaskDetailResponseDTO> getAllTasks() {
        log.info("Fetching all tasks");
        List<Task> tasks = taskRepository.findAll();
        return taskMapper.entitiesToResponseDTOs(tasks);
    }

    @Override
    public TaskDetailResponseDTO getTaskByIdentifier(String taskId) {
        log.info("Fetching task with identifier: {}", taskId);
        Task task = taskRepository.findByTaskIdentifier_TaskId(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with identifier: " + taskId));
        return taskMapper.entityToResponseDTO(task);
    }

    @Override
    @Transactional
    public TaskDetailResponseDTO createTask(TaskRequestDTO taskRequestDTO) {
        log.info("Creating new task");
        validateTaskRequest(taskRequestDTO);
        
        Users assignedUser = null;
        if (taskRequestDTO.getAssignedToUserId() != null) {
            assignedUser = getUserAndValidateRole(taskRequestDTO.getAssignedToUserId());
        }

        Task task = taskMapper.requestDTOToEntity(taskRequestDTO, assignedUser);
        
        // Set lotId from the schedule
        if (taskRequestDTO.getScheduleId() != null && !taskRequestDTO.getScheduleId().isBlank()) {
            Schedule schedule = scheduleRepository.findByScheduleIdentifier(taskRequestDTO.getScheduleId())
                    .orElseThrow(() -> new NotFoundException("Schedule not found with identifier: " + taskRequestDTO.getScheduleId()));
            task.setLotId(schedule.getLotId());
        }
        
        Task savedTask = taskRepository.save(task);

        log.info("Task created with identifier: {}", savedTask.getTaskIdentifier().getTaskId());
        return taskMapper.entityToResponseDTO(savedTask);
    }

    @Override
    @Transactional
    public TaskDetailResponseDTO updateTask(String taskId, TaskRequestDTO taskRequestDTO) {
        log.info("Updating task with identifier: {}", taskId);
        validateTaskRequest(taskRequestDTO);

        Task existingTask = taskRepository.findByTaskIdentifier_TaskId(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with identifier: " + taskId));

        Users assignedUser = null;
        if (taskRequestDTO.getAssignedToUserId() != null) {
            assignedUser = getUserAndValidateRole(taskRequestDTO.getAssignedToUserId());
        }

        taskMapper.updateEntityFromRequestDTO(existingTask, taskRequestDTO, assignedUser);
        
        // Update lotId from the schedule if scheduleId was changed
        if (taskRequestDTO.getScheduleId() != null && !taskRequestDTO.getScheduleId().isBlank()) {
            Schedule schedule = scheduleRepository.findByScheduleIdentifier(taskRequestDTO.getScheduleId())
                    .orElseThrow(() -> new NotFoundException("Schedule not found with identifier: " + taskRequestDTO.getScheduleId()));
            existingTask.setLotId(schedule.getLotId());
        }
        
        Task updatedTask = taskRepository.save(existingTask);

        // --- Notification logic ---
        // Find the lot associated with this task to get assigned users
        if (updatedTask.getLotId() != null) {
            try {
                Lot lot = lotRepository.findByLotIdentifier_LotId(updatedTask.getLotId());
                if (lot != null && lot.getAssignedUsers() != null && !lot.getAssignedUsers().isEmpty()) {
                    List<Users> assignedUsers = lot.getAssignedUsers();
                    
                    // Prepare notification content
                    String message = String.format(
                        "Task \"%s\" was updated. Period: %s to %s. Status: %s.",
                        updatedTask.getTaskTitle(),
                        updatedTask.getPeriodStart(),
                        updatedTask.getPeriodEnd(),
                        updatedTask.getTaskStatus()
                    );
                    String title = "Task Updated";
                    
                    // Build link to the lot metadata page
                    String link = null;
                    if (lot.getProject() != null) {
                        String projectIdentifier = lot.getProject().getProjectIdentifier();
                        link = String.format("/projects/%s/lots/%s/metadata", projectIdentifier, updatedTask.getLotId());
                    }
                    
                    // Send notifications to all assigned users
                    for (Users user : assignedUsers) {
                        try {
                            notificationService.createNotification(
                                user.getUserIdentifier().getUserId(),
                                title,
                                message,
                                NotificationCategory.TASK_UPDATED,
                                link
                            );
                            
                            // Send email
                            if (user.getPrimaryEmail() != null && !user.getPrimaryEmail().isEmpty()) {
                                String emailBody = String.format(
                                    "Hello %s,<br><br>%s<br><br>Task Description: %s",
                                    user.getFirstName(),
                                    message,
                                    updatedTask.getTaskDescription() != null ? updatedTask.getTaskDescription() : "N/A"
                                );
                                mailerServiceClient.sendEmail(
                                    user.getPrimaryEmail(),
                                    title,
                                    emailBody,
                                    "Les Constructions Dominic Cyr"
                                ).subscribe(
                                    null,
                                    error -> log.error("Failed to send email to {}: {}", user.getPrimaryEmail(), error.getMessage(), error)
                                );
                            }
                        } catch (Exception e) {
                            log.warn("Failed to notify user {}: {}", user.getUserIdentifier().getUserId(), e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to send notifications for task update {}: {}", taskId, e.getMessage());
            }
        }

        log.info("Task updated: {}", taskId);
        return taskMapper.entityToResponseDTO(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(String taskId) {
        log.info("Deleting task with identifier: {}", taskId);
        Task task = taskRepository.findByTaskIdentifier_TaskId(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with identifier: " + taskId));

        taskRepository.delete(task);
        log.info("Task deleted: {}", taskId);
    }

    @Override
    public List<TaskDetailResponseDTO> getTasksForContractor(String contractorId) {
        log.info("Fetching tasks for contractor: {}", contractorId);
        
        Users contractor = usersRepository.findByUserIdentifier_UserId(UUID.fromString(contractorId))
                .orElseThrow(() -> new NotFoundException("User not found with identifier: " + contractorId));

        if (contractor.getUserRole() != UserRole.CONTRACTOR) {
            throw new InvalidInputException("User is not a contractor: " + contractorId);
        }

        List<Task> tasks = taskRepository.findByAssignedTo(contractor);
        return taskMapper.entitiesToResponseDTOs(tasks);
    }

    @Override
    public List<TaskDetailResponseDTO> getTasksForSchedule(String scheduleIdentifier) {
        log.info("Fetching tasks for schedule: {}", scheduleIdentifier);
        List<Task> tasks = taskRepository.findByScheduleId(scheduleIdentifier);
        return taskMapper.entitiesToResponseDTOs(tasks);
    }

    @Override
    public List<TaskDetailResponseDTO> getTasksForScheduleByStatus(String scheduleIdentifier, TaskStatus taskStatus) {
        log.info("Fetching tasks for schedule: {} with status: {}", scheduleIdentifier, taskStatus);
        List<Task> tasks = taskRepository.findByScheduleIdAndTaskStatus(scheduleIdentifier, taskStatus);
        return taskMapper.entitiesToResponseDTOs(tasks);
    }

    @Override
    public List<TaskDetailResponseDTO> getTasksForProject(String projectIdentifier) {
        log.info("Fetching tasks for project: {}", projectIdentifier);
        List<Task> tasks = taskRepository.findByProjectIdentifier(projectIdentifier);
        return taskMapper.entitiesToResponseDTOs(tasks);
    }

    @Override
    public List<TaskDetailResponseDTO> getTasksForProjectByStatus(String projectIdentifier, TaskStatus taskStatus) {
        log.info("Fetching tasks for project: {} with status: {}", projectIdentifier, taskStatus);
        List<Task> tasks = taskRepository.findByProjectIdentifierAndTaskStatus(projectIdentifier, taskStatus);
        return taskMapper.entitiesToResponseDTOs(tasks);
    }

    @Override
    public List<TaskDetailResponseDTO> getTasksForUserAssignedLots(String userId) {
        log.info("Fetching tasks for user's assigned lots: {}", userId);
        
        // Find all lots assigned to this user
        UUID userUuid = UUID.fromString(userId);
        List<Lot> assignedLots = lotRepository.findByAssignedUserId(userUuid);
        
        if (assignedLots.isEmpty()) {
            log.info("No assigned lots found for user: {}", userId);
            return List.of();
        }
        
        // Get all tasks for these lots
        List<Task> allTasks = assignedLots.stream()
                .flatMap(lot -> taskRepository.findByLotId(lot.getLotIdentifier().getLotId()).stream())
                .toList();
        
        log.info("Found {} tasks for user's {} assigned lots", allTasks.size(), assignedLots.size());
        return taskMapper.entitiesToResponseDTOs(allTasks);
    }

    private void validateTaskRequest(TaskRequestDTO taskRequestDTO) {
        if (taskRequestDTO.getTaskTitle() == null || taskRequestDTO.getTaskTitle().isBlank()) {
            throw new InvalidInputException("Task title must not be blank");
        }
        if (taskRequestDTO.getTaskDescription() == null || taskRequestDTO.getTaskDescription().isBlank()) {
            throw new InvalidInputException("Task description must not be blank");
        }
        if (taskRequestDTO.getTaskStatus() == null) {
            throw new InvalidInputException("Task status must not be null");
        }
        if (taskRequestDTO.getTaskPriority() == null) {
            throw new InvalidInputException("Task priority must not be null");
        }
        if (taskRequestDTO.getPeriodStart() == null) {
            throw new InvalidInputException("Period start must not be null");
        }
        if (taskRequestDTO.getPeriodEnd() == null) {
            throw new InvalidInputException("Period end must not be null");
        }
        if (taskRequestDTO.getScheduleId() == null || taskRequestDTO.getScheduleId().isBlank()) {
            throw new InvalidInputException("Schedule identifier must not be null or blank");
        }
    }

    private Users getUserAndValidateRole(String userId) {
        Users user = usersRepository.findByUserIdentifier_UserId(UUID.fromString(userId))
                .orElseThrow(() -> new NotFoundException("User not found with identifier: " + userId));

        if (user.getUserRole() != UserRole.CONTRACTOR) {
            throw new InvalidInputException("Only contractors can be assigned to tasks. User role: " + user.getUserRole());
        }

        return user;
    }
}

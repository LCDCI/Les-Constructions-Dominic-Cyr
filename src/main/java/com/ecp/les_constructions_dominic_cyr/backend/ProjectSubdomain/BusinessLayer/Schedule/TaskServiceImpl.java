package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Task;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskRepository;
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
        Task updatedTask = taskRepository.save(existingTask);

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
    public List<Task> getTasksForSchedule(String scheduleIdentifier) {
        return List.of();
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

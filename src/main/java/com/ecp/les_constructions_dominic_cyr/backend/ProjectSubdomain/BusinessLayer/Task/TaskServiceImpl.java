package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Task;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserService userService;

    @Override
    public List<TaskResponseDTO> getCurrentWeekTasks() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        log.info("Fetching tasks for week: {} to {}", startOfWeek, endOfWeek);

        List<Task> tasks = taskRepository.findCurrentWeekTasks(startOfWeek, endOfWeek);
        return taskMapper.entitiesToResponseDTOs(tasks);
    }

    @Override
    public List<TaskResponseDTO> getAllTasks() {
        log.info("Fetching all tasks");
        List<Task> tasks = taskRepository.findAll();
        return taskMapper.entitiesToResponseDTOs(tasks);
    }

    @Override
    public TaskResponseDTO getTaskByIdentifier(String taskIdentifier) {
        log.info("Fetching task with identifier: {}", taskIdentifier);
        Task task = taskRepository.findByTaskIdentifier(taskIdentifier)
                .orElseThrow(() -> new NotFoundException("Task not found with identifier: " + taskIdentifier));
        return taskMapper.entityToResponseDTO(task);
    }

    @Override
    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO) {
        log.info("Creating new task");
        
        // Validate that assignedTo user is a contractor if provided
        if (taskRequestDTO.getAssignedTo() != null && !taskRequestDTO.getAssignedTo().isEmpty()) {
            validateContractorAssignment(taskRequestDTO.getAssignedTo());
        }

        Task task = taskMapper.requestDTOToEntity(taskRequestDTO);
        Task savedTask = taskRepository.save(task);
        log.info("Task created with identifier: {}", savedTask.getTaskIdentifier());
        return taskMapper.entityToResponseDTO(savedTask);
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTask(String taskIdentifier, TaskRequestDTO taskRequestDTO) {
        log.info("Updating task with identifier: {}", taskIdentifier);
        
        Task task = taskRepository.findByTaskIdentifier(taskIdentifier)
                .orElseThrow(() -> new NotFoundException("Task not found with identifier: " + taskIdentifier));

        // Validate that assignedTo user is a contractor if provided
        if (taskRequestDTO.getAssignedTo() != null && !taskRequestDTO.getAssignedTo().isEmpty()) {
            validateContractorAssignment(taskRequestDTO.getAssignedTo());
        }

        taskMapper.updateEntityFromRequestDTO(task, taskRequestDTO);
        Task updatedTask = taskRepository.save(task);
        log.info("Task updated with identifier: {}", updatedTask.getTaskIdentifier());
        return taskMapper.entityToResponseDTO(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(String taskIdentifier) {
        log.info("Deleting task with identifier: {}", taskIdentifier);
        Task task = taskRepository.findByTaskIdentifier(taskIdentifier)
                .orElseThrow(() -> new NotFoundException("Task not found with identifier: " + taskIdentifier));
        taskRepository.delete(task);
        log.info("Task deleted with identifier: {}", taskIdentifier);
    }

    @Override
    public List<TaskResponseDTO> getTasksAssignedToContractor(String contractorId) {
        log.info("Fetching tasks assigned to contractor: {}", contractorId);
        List<Task> tasks = taskRepository.findByAssignedTo(contractorId);
        return taskMapper.entitiesToResponseDTOs(tasks);
    }

    @Override
    public List<TaskResponseDTO> getCurrentWeekTasksAssignedToContractor(String contractorId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        log.info("Fetching tasks for contractor {} for week: {} to {}", contractorId, startOfWeek, endOfWeek);

        List<Task> tasks = taskRepository.findCurrentWeekTasksByAssignedTo(contractorId, startOfWeek, endOfWeek);
        return taskMapper.entitiesToResponseDTOs(tasks);
    }

    private void validateContractorAssignment(String userId) {
        try {
            UserResponseModel user = userService.getUserById(userId);
            if (user.getUserRole() != UserRole.CONTRACTOR) {
                throw new InvalidInputException("User " + userId + " is not a contractor and cannot be assigned to tasks");
            }
        } catch (NotFoundException e) {
            throw new InvalidInputException("User " + userId + " not found");
        }
    }
}

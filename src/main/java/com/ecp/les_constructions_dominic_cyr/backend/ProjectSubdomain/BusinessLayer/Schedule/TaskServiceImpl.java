package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Schedule;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.ScheduleRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Task;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.TaskMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ContractorTaskViewDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.LotTaskGroupDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ProjectTaskGroupDTO;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UsersRepository usersRepository;
    private final ScheduleRepository scheduleRepository;

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
    public List<TaskDetailResponseDTO> getTasksForSchedule(String scheduleIdentifier) {
        log.info("Fetching tasks for schedule: {}", scheduleIdentifier);
        List<Task> tasks = taskRepository.findByScheduleId(scheduleIdentifier);
        return taskMapper.entitiesToResponseDTOs(tasks);
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

    @Override
    public List<ProjectTaskGroupDTO> getAllTasksGroupedByProjectAndLot() {
        log.info("Fetching all tasks grouped by project and lot");

        List<Schedule> allSchedules = scheduleRepository.findAll();

        // Group schedules by project
        Map<String, List<Schedule>> schedulesByProject = allSchedules.stream()
                .filter(s -> s.getProject() != null)
                .collect(Collectors.groupingBy(s -> s.getProject().getProjectIdentifier()));

        List<ProjectTaskGroupDTO> result = new ArrayList<>();

        for (Map.Entry<String, List<Schedule>> projectEntry : schedulesByProject.entrySet()) {
            String projectIdentifier = projectEntry.getKey();
            List<Schedule> projectSchedules = projectEntry.getValue();

            String projectName = projectSchedules.stream()
                    .filter(s -> s.getProject() != null)
                    .findFirst()
                    .map(s -> s.getProject().getProjectName())
                    .orElse("Unknown Project");

            // Group by lot within the project
            Map<String, List<Schedule>> schedulesByLot = projectSchedules.stream()
                    .collect(Collectors.groupingBy(Schedule::getLotId));

            List<LotTaskGroupDTO> lotGroups = new ArrayList<>();

            for (Map.Entry<String, List<Schedule>> lotEntry : schedulesByLot.entrySet()) {
                String lotId = lotEntry.getKey();
                List<Schedule> lotSchedules = lotEntry.getValue();

                for (Schedule schedule : lotSchedules) {
                    List<Task> tasks = taskRepository.findByScheduleId(schedule.getScheduleIdentifier());
                    List<ContractorTaskViewDTO> taskDTOs = tasks.stream()
                            .map(task -> mapToContractorTaskView(task, schedule))
                            .collect(Collectors.toList());

                    lotGroups.add(LotTaskGroupDTO.builder()
                            .lotId(lotId)
                            .lotNumber(lotId)
                            .scheduleId(schedule.getScheduleIdentifier())
                            .scheduleDescription(schedule.getScheduleDescription())
                            .tasks(taskDTOs)
                            .build());
                }
            }

            result.add(ProjectTaskGroupDTO.builder()
                    .projectIdentifier(projectIdentifier)
                    .projectName(projectName)
                    .lots(lotGroups)
                    .build());
        }

        return result;
    }

    @Override
    public List<LotTaskGroupDTO> getTasksForProjectGroupedByLot(String projectIdentifier) {
        log.info("Fetching tasks for project {} grouped by lot", projectIdentifier);

        List<Schedule> projectSchedules = scheduleRepository.findByProjectIdentifier(projectIdentifier);

        if (projectSchedules.isEmpty()) {
            return new ArrayList<>();
        }

        // Group schedules by lot
        Map<String, List<Schedule>> schedulesByLot = projectSchedules.stream()
                .collect(Collectors.groupingBy(Schedule::getLotId));

        List<LotTaskGroupDTO> result = new ArrayList<>();

        for (Map.Entry<String, List<Schedule>> lotEntry : schedulesByLot.entrySet()) {
            String lotId = lotEntry.getKey();
            List<Schedule> lotSchedules = lotEntry.getValue();

            for (Schedule schedule : lotSchedules) {
                List<Task> tasks = taskRepository.findByScheduleId(schedule.getScheduleIdentifier());
                List<ContractorTaskViewDTO> taskDTOs = tasks.stream()
                        .map(task -> mapToContractorTaskView(task, schedule))
                        .collect(Collectors.toList());

                result.add(LotTaskGroupDTO.builder()
                        .lotId(lotId)
                        .lotNumber(lotId)
                        .scheduleId(schedule.getScheduleIdentifier())
                        .scheduleDescription(schedule.getScheduleDescription())
                        .tasks(taskDTOs)
                        .build());
            }
        }

        return result;
    }

    @Override
    public List<ContractorTaskViewDTO> getTasksForLot(String projectIdentifier, String lotId) {
        log.info("Fetching tasks for project {} and lot {}", projectIdentifier, lotId);

        List<Schedule> projectSchedules = scheduleRepository.findByProjectIdentifier(projectIdentifier);

        List<ContractorTaskViewDTO> result = new ArrayList<>();

        for (Schedule schedule : projectSchedules) {
            if (schedule.getLotId().equals(lotId)) {
                List<Task> tasks = taskRepository.findByScheduleId(schedule.getScheduleIdentifier());
                for (Task task : tasks) {
                    result.add(mapToContractorTaskView(task, schedule));
                }
            }
        }

        return result;
    }

    @Override
    public List<ContractorTaskViewDTO> getAllTasksForContractorView() {
        log.info("Fetching all tasks for contractor view");

        List<Task> allTasks = taskRepository.findAll();
        List<ContractorTaskViewDTO> result = new ArrayList<>();

        for (Task task : allTasks) {
            Schedule schedule = scheduleRepository.findByScheduleIdentifier(task.getScheduleId())
                    .orElse(null);
            result.add(mapToContractorTaskView(task, schedule));
        }

        // Sort by project, then lot, then date
        result.sort(Comparator
                .comparing((ContractorTaskViewDTO t) -> t.getProjectName() != null ? t.getProjectName() : "")
                .thenComparing(t -> t.getLotId() != null ? t.getLotId() : "")
                .thenComparing(t -> t.getPeriodStart() != null ? t.getPeriodStart() : new Date(0)));

        return result;
    }

    @Override
    public List<ContractorTaskViewDTO> getContractorAssignedTasksWithDetails(String contractorId) {
        log.info("Fetching assigned tasks for contractor {} with details", contractorId);

        Users contractor = usersRepository.findByUserIdentifier_UserId(UUID.fromString(contractorId))
                .orElseThrow(() -> new NotFoundException("User not found with identifier: " + contractorId));

        if (contractor.getUserRole() != UserRole.CONTRACTOR) {
            throw new InvalidInputException("User is not a contractor: " + contractorId);
        }

        List<Task> tasks = taskRepository.findByAssignedTo(contractor);
        List<ContractorTaskViewDTO> result = new ArrayList<>();

        for (Task task : tasks) {
            Schedule schedule = scheduleRepository.findByScheduleIdentifier(task.getScheduleId())
                    .orElse(null);
            result.add(mapToContractorTaskView(task, schedule));
        }

        // Sort by project, then lot, then date
        result.sort(Comparator
                .comparing((ContractorTaskViewDTO t) -> t.getProjectName() != null ? t.getProjectName() : "")
                .thenComparing(t -> t.getLotId() != null ? t.getLotId() : "")
                .thenComparing(t -> t.getPeriodStart() != null ? t.getPeriodStart() : new Date(0)));

        return result;
    }

    private ContractorTaskViewDTO mapToContractorTaskView(Task task, Schedule schedule) {
        String projectIdentifier = null;
        String projectName = null;
        String lotId = null;
        String lotNumber = null;

        if (schedule != null) {
            lotId = schedule.getLotId();
            lotNumber = schedule.getLotId();
            if (schedule.getProject() != null) {
                projectIdentifier = schedule.getProject().getProjectIdentifier();
                projectName = schedule.getProject().getProjectName();
            }
        }

        return ContractorTaskViewDTO.builder()
                .taskId(task.getTaskIdentifier() != null ? task.getTaskIdentifier().getTaskId() : null)
                .taskStatus(task.getTaskStatus() != null ? task.getTaskStatus().name() : null)
                .taskTitle(task.getTaskTitle())
                .periodStart(localDateToDate(task.getPeriodStart()))
                .periodEnd(localDateToDate(task.getPeriodEnd()))
                .taskDescription(task.getTaskDescription())
                .taskPriority(task.getTaskPriority() != null ? task.getTaskPriority().name() : null)
                .estimatedHours(task.getEstimatedHours())
                .hoursSpent(task.getHoursSpent())
                .taskProgress(task.getTaskProgress())
                .assignedToUserId(task.getAssignedTo() != null && task.getAssignedTo().getUserIdentifier() != null
                        ? task.getAssignedTo().getUserIdentifier().getUserId().toString() : null)
                .assignedToUserName(task.getAssignedTo() != null
                        ? task.getAssignedTo().getFirstName() + " " + task.getAssignedTo().getLastName() : null)
                .scheduleId(task.getScheduleId())
                .projectIdentifier(projectIdentifier)
                .projectName(projectName)
                .lotId(lotId)
                .lotNumber(lotNumber)
                .build();
    }

    private Date localDateToDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}

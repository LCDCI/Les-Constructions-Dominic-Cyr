package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Schedule;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.ScheduleRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Task;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskDetailResponseDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskRequestDTO;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskMapper {

    private final ScheduleRepository scheduleRepository;
    private final LotRepository lotRepository;

    public TaskDetailResponseDTO entityToResponseDTO(Task task) {
        TaskDetailResponseDTO.TaskDetailResponseDTOBuilder builder = TaskDetailResponseDTO.builder()
                .taskId(task.getTaskIdentifier() != null ? task.getTaskIdentifier().getTaskId() : null)
                .taskStatus(task.getTaskStatus())
                .taskTitle(task.getTaskTitle())
                .periodStart(localDateToDate(task.getPeriodStart()))
                .periodEnd(localDateToDate(task.getPeriodEnd()))
                .taskDescription(task.getTaskDescription())
                .taskPriority(task.getTaskPriority())
                .estimatedHours(task.getEstimatedHours())
                .hoursSpent(task.getHoursSpent())
                .taskProgress(task.calculateProgress()) // Use calculated progress instead of stored value
                .assignedToUserId(task.getAssignedTo() != null && task.getAssignedTo().getUserIdentifier() != null
                    ? task.getAssignedTo().getUserIdentifier().getUserId().toString() : null)
                .assignedToUserName(task.getAssignedTo() != null 
                    ? task.getAssignedTo().getFirstName() + " " + task.getAssignedTo().getLastName() : null)
                .scheduleId(task.getScheduleId());
        
        // Fetch schedule to get project information
        if (task.getScheduleId() != null) {
            scheduleRepository.findByScheduleIdentifier(task.getScheduleId()).ifPresent(schedule -> {
                if (schedule.getProject() != null) {
                    builder.projectIdentifier(schedule.getProject().getProjectIdentifier())
                           .projectName(schedule.getProject().getProjectName());
                }
            });
        }
        
        // Add lot information
        if (task.getLotId() != null) {
            builder.lotId(task.getLotId().toString());
            Lot lot = lotRepository.findByLotIdentifier_LotId(task.getLotId());
            if (lot != null) {
                builder.lotNumber(lot.getLotNumber());
            } else {
                builder.lotNumber(task.getLotId().toString());
            }
        }
        
        return builder.build();
    }

    public List<TaskDetailResponseDTO> entitiesToResponseDTOs(List<Task> tasks) {
        return tasks.stream()
                .map(this::entityToResponseDTO)
                .collect(Collectors.toList());
    }

    public Task requestDTOToEntity(TaskRequestDTO requestDTO, Users assignedUser) {
        // Auto-transition from TO_DO to IN_PROGRESS when hours are logged during creation
        TaskStatus taskStatus = requestDTO.getTaskStatus();
        Double hoursSpent = toDouble(requestDTO.getHoursSpent());

        if (taskStatus == TaskStatus.TO_DO && hoursSpent != null && hoursSpent > 0) {
            taskStatus = TaskStatus.IN_PROGRESS;
        }

        return Task.builder()
                .taskIdentifier(new TaskIdentifier())
                .taskStatus(taskStatus)
                .taskTitle(requestDTO.getTaskTitle())
                .periodStart(dateToLocalDate(requestDTO.getPeriodStart()))
                .periodEnd(dateToLocalDate(requestDTO.getPeriodEnd()))
                .taskDescription(requestDTO.getTaskDescription())
                .taskPriority(requestDTO.getTaskPriority())
                .estimatedHours(toDouble(requestDTO.getEstimatedHours()))
                .hoursSpent(hoursSpent)
                .taskProgress(toDouble(requestDTO.getTaskProgress()))
                .assignedTo(assignedUser)
                .scheduleId(requestDTO.getScheduleId())
                .build();
    }

    public void updateEntityFromRequestDTO(Task task, TaskRequestDTO requestDTO, Users assignedUser) {
        // Auto-transition from TO_DO to IN_PROGRESS when hours are logged
        // Only apply if the request is trying to keep status as TO_DO
        TaskStatus newStatus = requestDTO.getTaskStatus();
        Double newHoursSpent = toDouble(requestDTO.getHoursSpent());

        if (task.getTaskStatus() == TaskStatus.TO_DO &&
            newStatus == TaskStatus.TO_DO &&
            newHoursSpent != null && newHoursSpent > 0) {
            newStatus = TaskStatus.IN_PROGRESS;
        }

        task.setTaskStatus(newStatus);
        task.setTaskTitle(requestDTO.getTaskTitle());
        task.setPeriodStart(dateToLocalDate(requestDTO.getPeriodStart()));
        task.setPeriodEnd(dateToLocalDate(requestDTO.getPeriodEnd()));
        task.setTaskDescription(requestDTO.getTaskDescription());
        task.setTaskPriority(requestDTO.getTaskPriority());
        task.setEstimatedHours(toDouble(requestDTO.getEstimatedHours()));
        task.setHoursSpent(newHoursSpent);
        task.setTaskProgress(toDouble(requestDTO.getTaskProgress()));
        task.setAssignedTo(assignedUser);
        if (requestDTO.getScheduleId() != null && !requestDTO.getScheduleId().isBlank()) {
            task.setScheduleId(requestDTO.getScheduleId());
        }
    }

    private Double toDouble(Number number) {
        return number != null ? number.doubleValue() : null;
    }

    private LocalDate dateToLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Date localDateToDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}

package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Task;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskDetailResponseDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskRequestDTO;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskMapper {

    public TaskDetailResponseDTO entityToResponseDTO(Task task) {
        return TaskDetailResponseDTO.builder()
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
                .scheduleId(task.getScheduleId())
                .build();
    }

    public List<TaskDetailResponseDTO> entitiesToResponseDTOs(List<Task> tasks) {
        return tasks.stream()
                .map(this::entityToResponseDTO)
                .collect(Collectors.toList());
    }

    public Task requestDTOToEntity(TaskRequestDTO requestDTO, Users assignedUser) {
        return Task.builder()
                .taskIdentifier(new TaskIdentifier())
                .taskStatus(requestDTO.getTaskStatus())
                .taskTitle(requestDTO.getTaskTitle())
                .periodStart(dateToLocalDate(requestDTO.getPeriodStart()))
                .periodEnd(dateToLocalDate(requestDTO.getPeriodEnd()))
                .taskDescription(requestDTO.getTaskDescription())
                .taskPriority(requestDTO.getTaskPriority())
                .estimatedHours(toDouble(requestDTO.getEstimatedHours()))
                .hoursSpent(toDouble(requestDTO.getHoursSpent()))
                .taskProgress(toDouble(requestDTO.getTaskProgress()))
                .assignedTo(assignedUser)
                .scheduleId(requestDTO.getScheduleId())
                .build();
    }

    public void updateEntityFromRequestDTO(Task task, TaskRequestDTO requestDTO, Users assignedUser) {
        task.setTaskStatus(requestDTO.getTaskStatus());
        task.setTaskTitle(requestDTO.getTaskTitle());
        task.setPeriodStart(dateToLocalDate(requestDTO.getPeriodStart()));
        task.setPeriodEnd(dateToLocalDate(requestDTO.getPeriodEnd()));
        task.setTaskDescription(requestDTO.getTaskDescription());
        task.setTaskPriority(requestDTO.getTaskPriority());
        task.setEstimatedHours(toDouble(requestDTO.getEstimatedHours()));
        task.setHoursSpent(toDouble(requestDTO.getHoursSpent()));
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

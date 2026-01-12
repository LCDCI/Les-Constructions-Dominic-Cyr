package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Task;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskDetailResponseDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskRequestDTO;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskMapper {

    public TaskDetailResponseDTO entityToResponseDTO(Task task) {
        return TaskDetailResponseDTO.builder()
                .taskId(task.getTaskIdentifier() != null ? task.getTaskIdentifier().getTaskId() : null)
                .taskStatus(task.getTaskStatus())
                .taskTitle(task.getTaskTitle())
                .periodStart(task.getPeriod_start())
                .periodEnd(task.getPeriod_end())
                .taskDescription(task.getTaskDescription())
                .taskPriority(task.getTaskPriority())
                .estimatedHours(task.getEstimatedHours())
                .hoursSpent(task.getHoursSpent())
                .taskProgress(task.getTaskProgress())
                .assignedToUserId(task.getAssignedTo() != null && task.getAssignedTo().getUserIdentifier() != null 
                    ? task.getAssignedTo().getUserIdentifier().getUserId().toString() : null)
                .assignedToUserName(task.getAssignedTo() != null 
                    ? task.getAssignedTo().getFirstName() + " " + task.getAssignedTo().getLastName() : null)
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
                .period_start(requestDTO.getPeriodStart())
                .period_end(requestDTO.getPeriodEnd())
                .taskDescription(requestDTO.getTaskDescription())
                .taskPriority(requestDTO.getTaskPriority())
                .estimatedHours(requestDTO.getEstimatedHours())
                .hoursSpent(requestDTO.getHoursSpent())
                .taskProgress(requestDTO.getTaskProgress())
                .assignedTo(assignedUser)
                .build();
    }

    public void updateEntityFromRequestDTO(Task task, TaskRequestDTO requestDTO, Users assignedUser) {
        task.setTaskStatus(requestDTO.getTaskStatus());
        task.setTaskTitle(requestDTO.getTaskTitle());
        task.setPeriod_start(requestDTO.getPeriodStart());
        task.setPeriod_end(requestDTO.getPeriodEnd());
        task.setTaskDescription(requestDTO.getTaskDescription());
        task.setTaskPriority(requestDTO.getTaskPriority());
        task.setEstimatedHours(requestDTO.getEstimatedHours());
        task.setHoursSpent(requestDTO.getHoursSpent());
        task.setTaskProgress(requestDTO.getTaskProgress());
        task.setAssignedTo(assignedUser);
    }
}

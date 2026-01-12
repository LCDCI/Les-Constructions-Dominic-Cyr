package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Task.Task;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Task.TaskRequestDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Task.TaskResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TaskMapper {

    public TaskResponseDTO entityToResponseDTO(Task task) {
        return TaskResponseDTO.builder()
                .taskIdentifier(task.getTaskIdentifier())
                .taskDate(task.getTaskDate())
                .taskDescription(task.getTaskDescription())
                .lotNumber(task.getLotNumber())
                .dayOfWeek(task.getDayOfWeek())
                .assignedTo(task.getAssignedTo())
                .build();
    }

    public List<TaskResponseDTO> entitiesToResponseDTOs(List<Task> tasks) {
        return tasks.stream()
                .map(this::entityToResponseDTO)
                .collect(Collectors.toList());
    }

    public Task requestDTOToEntity(TaskRequestDTO requestDTO) {
        return Task.builder()
                .taskIdentifier("TASK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .taskDate(requestDTO.getTaskDate())
                .taskDescription(requestDTO.getTaskDescription())
                .lotNumber(requestDTO.getLotNumber())
                .dayOfWeek(requestDTO.getDayOfWeek())
                .assignedTo(requestDTO.getAssignedTo())
                .build();
    }

    public void updateEntityFromRequestDTO(Task task, TaskRequestDTO requestDTO) {
        task.setTaskDate(requestDTO.getTaskDate());
        task.setTaskDescription(requestDTO.getTaskDescription());
        task.setLotNumber(requestDTO.getLotNumber());
        task.setDayOfWeek(requestDTO.getDayOfWeek());
        task.setAssignedTo(requestDTO.getAssignedTo());
    }
}

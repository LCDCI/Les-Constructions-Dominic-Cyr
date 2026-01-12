package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Task;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Task.TaskRequestDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Task.TaskResponseDTO;

import java.util.List;

public interface TaskService {

    List<TaskResponseDTO> getCurrentWeekTasks();

    List<TaskResponseDTO> getAllTasks();

    TaskResponseDTO getTaskByIdentifier(String taskIdentifier);

    TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO);

    TaskResponseDTO updateTask(String taskIdentifier, TaskRequestDTO taskRequestDTO);

    void deleteTask(String taskIdentifier);

    List<TaskResponseDTO> getTasksAssignedToContractor(String contractorId);

    List<TaskResponseDTO> getCurrentWeekTasksAssignedToContractor(String contractorId);
}

package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule;

import java.util.List;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskDetailResponseDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskRequestDTO;

public interface TaskService {

    /**
     * Get all tasks (owner only)
     */
    List<TaskDetailResponseDTO> getAllTasks();

    /**
     * Get task by identifier
     */
    TaskDetailResponseDTO getTaskByIdentifier(String taskId);

    /**
     * Create a new task (owner only)
     */
    TaskDetailResponseDTO createTask(TaskRequestDTO taskRequestDTO);

    /**
     * Update an existing task (owner only)
     */
    TaskDetailResponseDTO updateTask(String taskId, TaskRequestDTO taskRequestDTO);

    /**
     * Delete a task (owner only)
     */
    void deleteTask(String taskId);

    /**
     * Get tasks assigned to a specific contractor
     */
    List<TaskDetailResponseDTO> getTasksForContractor(String contractorId);

    List<TaskDetailResponseDTO> getTasksForSchedule(String scheduleIdentifier);
}

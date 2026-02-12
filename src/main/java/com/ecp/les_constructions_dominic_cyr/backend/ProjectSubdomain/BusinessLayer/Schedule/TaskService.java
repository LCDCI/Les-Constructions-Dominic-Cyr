package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule;

import java.util.List;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskStatus;
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

    /**
     * Get all tasks for a specific schedule
     */
    List<TaskDetailResponseDTO> getTasksForSchedule(String scheduleIdentifier);

    /**
     * Get tasks for a schedule filtered by status
     */
    List<TaskDetailResponseDTO> getTasksForScheduleByStatus(String scheduleIdentifier, TaskStatus taskStatus);

    /**
     * Get all tasks for a specific project
     */
    List<TaskDetailResponseDTO> getTasksForProject(String projectIdentifier);

    /**
     * Get tasks for a project filtered by status
     */
    List<TaskDetailResponseDTO> getTasksForProjectByStatus(String projectIdentifier, TaskStatus taskStatus);

    /**
     * Get all tasks for lots assigned to a specific user (customer/salesperson view)
     */
    List<TaskDetailResponseDTO> getTasksForUserAssignedLots(String userId);
}

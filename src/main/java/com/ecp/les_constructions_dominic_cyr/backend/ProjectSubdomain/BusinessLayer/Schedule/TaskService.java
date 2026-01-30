package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule;

import java.util.List;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ContractorTaskViewDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.LotTaskGroupDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ProjectTaskGroupDTO;
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

    /**
     * Get all tasks with project and lot information for contractor view
     * Tasks are grouped by project, then by lot
     */
    List<ProjectTaskGroupDTO> getAllTasksGroupedByProjectAndLot();

    /**
     * Get all tasks for a specific project, grouped by lot
     */
    List<LotTaskGroupDTO> getTasksForProjectGroupedByLot(String projectIdentifier);

    /**
     * Get all tasks for a specific lot in a project
     */
    List<ContractorTaskViewDTO> getTasksForLot(String projectIdentifier, String lotId);

    /**
     * Get all tasks as a flat list with project/lot info for contractor view
     */
    List<ContractorTaskViewDTO> getAllTasksForContractorView();

    /**
     * Get all tasks assigned to a contractor with project/lot info
     */
    List<ContractorTaskViewDTO> getContractorAssignedTasksWithDetails(String contractorId);
}

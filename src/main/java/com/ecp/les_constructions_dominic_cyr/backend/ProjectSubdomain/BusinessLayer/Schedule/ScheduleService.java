package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleRequestDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleResponseDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {

    List<ScheduleResponseDTO> getCurrentWeekSchedules();

    List<ScheduleResponseDTO> getAllSchedules();

    ScheduleResponseDTO getScheduleByIdentifier(String scheduleIdentifier);

    ScheduleResponseDTO addSchedule(ScheduleRequestDTO scheduleRequestDTO);

    ScheduleResponseDTO updateSchedule(String scheduleIdentifier, ScheduleRequestDTO scheduleRequestDTO);

    void deleteSchedule(String scheduleIdentifier);

    /**
     * Get task summary for a contractor for a specific schedule and period.
     * Tasks are only viewable for contractors and owners.
     *
     * @param contractorId The contractor's user ID
     * @param scheduleId The schedule identifier
     * @param periodStart Start date of the period
     * @param periodEnd End date of the period
     * @return TaskResponseDTO containing the task summary
     */
    TaskResponseDTO getTaskSummaryForContractor(String contractorId, String scheduleId, 
                                                 LocalDate periodStart, LocalDate periodEnd);

    /**
     * Get task summary for the current week for a contractor.
     *
     * @param contractorId The contractor's user ID
     * @return TaskResponseDTO containing the task summary for current week
     */
    TaskResponseDTO getCurrentWeekTaskSummary(String contractorId);

    /**
     * Get all schedules for a specific project by project identifier.
     *
     * @param projectIdentifier The project identifier
     * @return List of ScheduleResponseDTO for the project
     */
    List<ScheduleResponseDTO> getSchedulesByProjectIdentifier(String projectIdentifier);

    /**
     * Get a specific schedule by project identifier and schedule identifier.
     *
     * @param projectIdentifier The project identifier
     * @param scheduleIdentifier The schedule identifier
     * @return ScheduleResponseDTO for the schedule
     */
    ScheduleResponseDTO getScheduleByProjectAndScheduleIdentifier(String projectIdentifier, String scheduleIdentifier);

    /**
     * Add a new schedule to a specific project.
     *
     * @param projectIdentifier The project identifier
     * @param scheduleRequestDTO The schedule data
     * @return ScheduleResponseDTO for the created schedule
     */
    ScheduleResponseDTO addScheduleToProject(String projectIdentifier, ScheduleRequestDTO scheduleRequestDTO);

    /**
     * Update a schedule for a specific project.
     *
     * @param projectIdentifier The project identifier
     * @param scheduleIdentifier The schedule identifier
     * @param scheduleRequestDTO The schedule data to update
     * @return ScheduleResponseDTO for the updated schedule
     */
    ScheduleResponseDTO updateScheduleForProject(String projectIdentifier, String scheduleIdentifier, ScheduleRequestDTO scheduleRequestDTO);

    /**
     * Delete a schedule from a specific project.
     *
     * @param projectIdentifier The project identifier
     * @param scheduleIdentifier The schedule identifier
     */
    void deleteScheduleFromProject(String projectIdentifier, String scheduleIdentifier);
}
package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Schedule;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.ScheduleRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.ScheduleMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleRequestDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleResponseDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskResponseDTO;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ProjectRepository projectRepository;
    private final LotRepository lotRepository;
    private final ScheduleMapper scheduleMapper;

    private static final int MAX_TASK_IDS = 50;
    private static final int MAX_TOP_PRIORITY_TASKS = 5;

    @Override
    public List<ScheduleResponseDTO> getCurrentWeekSchedules() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        log.info("Fetching schedules for week: {} to {}", startOfWeek, endOfWeek);

        List<Schedule> schedules = scheduleRepository.findCurrentWeekSchedules(startOfWeek, endOfWeek);
        return scheduleMapper.entitiesToResponseDTOs(schedules);
    }

    @Override
    public List<ScheduleResponseDTO> getAllSchedules() {
        log.info("Fetching all schedules");
        List<Schedule> schedules = scheduleRepository.findAll();
        return scheduleMapper.entitiesToResponseDTOs(schedules);
    }

    @Override
    public ScheduleResponseDTO getScheduleByIdentifier(String scheduleIdentifier) {
        log.info("Fetching schedule with identifier: {}", scheduleIdentifier);
        Schedule schedule = scheduleRepository.findByScheduleIdentifier(scheduleIdentifier)
                .orElseThrow(() -> new NotFoundException("Schedule not found with identifier: " + scheduleIdentifier));
        return scheduleMapper.entityToResponseDTO(schedule);
    }

    @Override
    @Transactional
    public ScheduleResponseDTO addSchedule(ScheduleRequestDTO scheduleRequestDTO) {
        log.info("Adding new schedule");
        validateScheduleRequest(scheduleRequestDTO);

        Lot lot = resolveLot(scheduleRequestDTO.getLotId());
        Schedule schedule = scheduleMapper.requestDTOToEntity(scheduleRequestDTO);
        schedule.setLotNumber(lot.getLotIdentifier().getLotId().toString());
        Schedule savedSchedule = scheduleRepository.save(schedule);

        log.info("Schedule created with identifier: {}", savedSchedule.getScheduleIdentifier());
        return scheduleMapper.entityToResponseDTO(savedSchedule);
    }

    @Override
    @Transactional
    public ScheduleResponseDTO updateSchedule(String scheduleIdentifier, ScheduleRequestDTO scheduleRequestDTO) {
        log.info("Updating schedule with identifier: {}", scheduleIdentifier);
        validateScheduleRequest(scheduleRequestDTO);

        Lot lot = resolveLot(scheduleRequestDTO.getLotId());
        Schedule existingSchedule = scheduleRepository.findByScheduleIdentifier(scheduleIdentifier)
                .orElseThrow(() -> new NotFoundException("Schedule not found with identifier: " + scheduleIdentifier));

        scheduleMapper.updateEntityFromRequestDTO(existingSchedule, scheduleRequestDTO);
        existingSchedule.setLotNumber(lot.getLotIdentifier().getLotId().toString());
        Schedule updatedSchedule = scheduleRepository.save(existingSchedule);

        log.info("Schedule updated: {}", scheduleIdentifier);
        return scheduleMapper.entityToResponseDTO(updatedSchedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(String scheduleIdentifier) {
        log.info("Deleting schedule with identifier: {}", scheduleIdentifier);
        Schedule schedule = scheduleRepository.findByScheduleIdentifier(scheduleIdentifier)
                .orElseThrow(() -> new NotFoundException("Schedule not found with identifier: " + scheduleIdentifier));

        scheduleRepository.delete(schedule);
        log.info("Schedule deleted: {}", scheduleIdentifier);
    }

    @Override
    public TaskResponseDTO getTaskSummaryForContractor(String contractorId, String scheduleId, 
                                                        LocalDate periodStart, LocalDate periodEnd) {
        log.info("Generating task summary for contractor: {}, schedule: {}, period: {} to {}", 
                 contractorId, scheduleId, periodStart, periodEnd);

        List<Schedule> schedules = scheduleRepository.findByScheduleStartDateBetween(periodStart, periodEnd);
        
        return buildTaskSummary(contractorId, scheduleId, periodStart, periodEnd, schedules);
    }

    @Override
    public TaskResponseDTO getCurrentWeekTaskSummary(String contractorId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        log.info("Generating current week task summary for contractor: {}", contractorId);

        List<Schedule> schedules = scheduleRepository.findCurrentWeekSchedules(startOfWeek, endOfWeek);
        
        return buildTaskSummary(contractorId, null, startOfWeek, endOfWeek, schedules);
    }

    private TaskResponseDTO buildTaskSummary(String contractorId, String scheduleId, 
                                              LocalDate periodStart, LocalDate periodEnd,
                                              List<Schedule> schedules) {
        LocalDate today = LocalDate.now();
        int totalTasks = schedules.size();
        
        // Calculate counts based on schedule start dates (simplified logic)
        // In a real implementation, these would come from task status fields
        int completedCount = (int) schedules.stream()
                .filter(s -> s.getScheduleStartDate().isBefore(today))
                .count();
        int openCount = totalTasks - completedCount;
        // Overdue tasks: tasks with past due dates that are not completed
        // Since we don't have status, we assume tasks before today are potentially overdue
        // In a real implementation, this would check status != COMPLETED && dueDate < today
        int overdueCount = completedCount; // Using same count as approximation since past dates could be overdue

        // Calculate estimated hours (using a default estimate per task for now)
        double defaultEstimatePerTask = 4.0;
        double totalEstimatedHours = totalTasks * defaultEstimatePerTask;
        double completedHours = completedCount * defaultEstimatePerTask;
        double totalRemainingHours = totalEstimatedHours - completedHours;

        // Find next due task
        TaskResponseDTO.TaskPreviewDTO nextDueTaskPreview = schedules.stream()
                .filter(s -> !s.getScheduleStartDate().isBefore(today))
                .min(Comparator.comparing(Schedule::getScheduleStartDate))
                .map(s -> TaskResponseDTO.TaskPreviewDTO.builder()
                        .id(s.getScheduleIdentifier())
                        .title(s.getScheduleDescription())
                        .dueDate(s.getScheduleStartDate())
                        .estimateHours(defaultEstimatePerTask)
                        .status("OPEN")
                        .assignedTo(contractorId)
                        .build())
                .orElse(null);

        // Get top priority tasks (first N upcoming tasks)
        List<TaskResponseDTO.TopPriorityTaskDTO> topPriorityTasks = schedules.stream()
                .filter(s -> !s.getScheduleStartDate().isBefore(today))
                .sorted(Comparator.comparing(Schedule::getScheduleStartDate))
                .limit(MAX_TOP_PRIORITY_TASKS)
                .map(s -> TaskResponseDTO.TopPriorityTaskDTO.builder()
                        .id(s.getScheduleIdentifier())
                        .title(s.getScheduleDescription())
                        .priority("HIGH")
                        .dueDate(s.getScheduleStartDate())
                        .build())
                .collect(Collectors.toList());

        // Get task IDs (limited)
        List<String> taskIds = schedules.stream()
                .limit(MAX_TASK_IDS)
                .map(Schedule::getScheduleIdentifier)
                .collect(Collectors.toList());

        // Check for milestones (simplified - could be based on specific task types)
        boolean milestonesPresent = !schedules.isEmpty();

        return TaskResponseDTO.builder()
                .contractorId(contractorId)
                .scheduleId(scheduleId)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .totalTasks(totalTasks)
                .openTasksCount(openCount)
                .completedTasksCount(completedCount)
                .overdueTasksCount(overdueCount)
                .totalEstimatedHours(totalEstimatedHours)
                .totalRemainingHours(totalRemainingHours)
                .nextDueTaskPreview(nextDueTaskPreview)
                .topPriorityTasks(topPriorityTasks)
                .blockedTasksCount(0) // No blocked tasks in current simple model
                .milestonesPresent(milestonesPresent)
                .taskIds(taskIds)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private void validateScheduleRequest(ScheduleRequestDTO scheduleRequestDTO) {
        if (scheduleRequestDTO.getScheduleStartDate() == null) {
            throw new InvalidInputException("Schedule start date must not be null");
        }
        if (scheduleRequestDTO.getScheduleEndDate() == null) {
            throw new InvalidInputException("Schedule end date must not be null");
        }
        if (scheduleRequestDTO.getScheduleDescription() == null || scheduleRequestDTO.getScheduleDescription().isBlank()) {
            throw new InvalidInputException("Schedule description must not be blank");
        }
        if (scheduleRequestDTO.getLotId() == null || scheduleRequestDTO.getLotId().isBlank()) {
            throw new InvalidInputException("Lot ID must not be blank");
        }
    }

    @Override
    public List<ScheduleResponseDTO> getSchedulesByProjectIdentifier(String projectIdentifier) {
        log.info("Fetching schedules for project: {}", projectIdentifier);
        List<Schedule> schedules = scheduleRepository.findByProjectIdentifier(projectIdentifier);
        return scheduleMapper.entitiesToResponseDTOs(schedules);
    }

    @Override
    public ScheduleResponseDTO getScheduleByProjectAndScheduleIdentifier(String projectIdentifier, String scheduleIdentifier) {
        log.info("Fetching schedule {} for project {}", scheduleIdentifier, projectIdentifier);
        
        // Verify project exists
        projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new NotFoundException("Project not found with identifier: " + projectIdentifier));
        
        Schedule schedule = scheduleRepository.findByScheduleIdentifier(scheduleIdentifier)
                .orElseThrow(() -> new NotFoundException("Schedule not found with identifier: " + scheduleIdentifier));
        
        validateScheduleBelongsToProject(schedule, projectIdentifier);
        
        return scheduleMapper.entityToResponseDTO(schedule);
    }

    @Override
    @Transactional
    public ScheduleResponseDTO addScheduleToProject(String projectIdentifier, ScheduleRequestDTO scheduleRequestDTO) {
        log.info("Adding new schedule to project: {}", projectIdentifier);
        validateScheduleRequest(scheduleRequestDTO);
        
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new NotFoundException("Project not found with identifier: " + projectIdentifier));
        
        Lot lot = resolveLot(scheduleRequestDTO.getLotId());
        Schedule schedule = scheduleMapper.requestDTOToEntity(scheduleRequestDTO);
        schedule.setProject(project);
        schedule.setLotNumber(lot.getLotIdentifier().getLotId().toString());
        Schedule savedSchedule = scheduleRepository.save(schedule);
        
        log.info("Schedule created with identifier: {} for project: {}", savedSchedule.getScheduleIdentifier(), projectIdentifier);
        return scheduleMapper.entityToResponseDTO(savedSchedule);
    }

    @Override
    @Transactional
    public ScheduleResponseDTO updateScheduleForProject(String projectIdentifier, String scheduleIdentifier, ScheduleRequestDTO scheduleRequestDTO) {
        log.info("Updating schedule {} for project {}", scheduleIdentifier, projectIdentifier);
        validateScheduleRequest(scheduleRequestDTO);
        
        // Verify project exists
        projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new NotFoundException("Project not found with identifier: " + projectIdentifier));
        
        Lot lot = resolveLot(scheduleRequestDTO.getLotId());
        Schedule existingSchedule = scheduleRepository.findByScheduleIdentifier(scheduleIdentifier)
                .orElseThrow(() -> new NotFoundException("Schedule not found with identifier: " + scheduleIdentifier));
        
        validateScheduleBelongsToProject(existingSchedule, projectIdentifier);
        
        scheduleMapper.updateEntityFromRequestDTO(existingSchedule, scheduleRequestDTO);
        existingSchedule.setLotNumber(lot.getLotIdentifier().getLotId().toString());
        Schedule updatedSchedule = scheduleRepository.save(existingSchedule);
        
        log.info("Schedule {} updated for project {}", scheduleIdentifier, projectIdentifier);
        return scheduleMapper.entityToResponseDTO(updatedSchedule);
    }

    private Lot resolveLot(String lotId) {
        UUID lotUuid = UUID.fromString(lotId);
        Lot lot = lotRepository.findByLotIdentifier_LotId(lotUuid);
        if (lot == null) {
            throw new NotFoundException("Lot not found with id: " + lotId);
        }
        return lot;
    }

    @Override
    @Transactional
    public void deleteScheduleFromProject(String projectIdentifier, String scheduleIdentifier) {
        log.info("Deleting schedule {} from project {}", scheduleIdentifier, projectIdentifier);
        
        // Verify project exists
        projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new NotFoundException("Project not found with identifier: " + projectIdentifier));
        
        Schedule schedule = scheduleRepository.findByScheduleIdentifier(scheduleIdentifier)
                .orElseThrow(() -> new NotFoundException("Schedule not found with identifier: " + scheduleIdentifier));
        
        validateScheduleBelongsToProject(schedule, projectIdentifier);
        
        scheduleRepository.delete(schedule);
        log.info("Schedule {} deleted from project {}", scheduleIdentifier, projectIdentifier);
    }

    private void validateScheduleBelongsToProject(Schedule schedule, String projectIdentifier) {
        if (schedule.getProject() == null || !projectIdentifier.equals(schedule.getProject().getProjectIdentifier())) {
            throw new NotFoundException("Schedule " + schedule.getScheduleIdentifier() + " does not belong to project " + projectIdentifier);
        }
    }
}

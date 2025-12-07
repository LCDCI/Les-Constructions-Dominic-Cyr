package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Schedule;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.ScheduleRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.ScheduleMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;

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
                .orElseThrow(() -> new RuntimeException("Schedule not found with identifier: " + scheduleIdentifier));
        return scheduleMapper.entityToResponseDTO(schedule);
    }
}
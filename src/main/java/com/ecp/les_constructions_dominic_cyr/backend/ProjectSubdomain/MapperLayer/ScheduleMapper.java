package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Schedule;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScheduleMapper {

    public ScheduleResponseDTO entityToResponseDTO(Schedule schedule) {
        return ScheduleResponseDTO.builder()
                .scheduleIdentifier(schedule.getScheduleIdentifier())
                .taskDate(schedule.getTaskDate())
                .taskDescription(schedule.getTaskDescription())
                .lotNumber(schedule.getLotNumber())
                .dayOfWeek(schedule.getDayOfWeek())
                .build();
    }

    public List<ScheduleResponseDTO> entitiesToResponseDTOs(List<Schedule> schedules) {
        return schedules.stream()
                .map(this::entityToResponseDTO)
                .collect(Collectors.toList());
    }
}
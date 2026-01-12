package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Schedule;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleRequestDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScheduleMapper {

    private final TaskMapper taskMapper;

    public ScheduleResponseDTO entityToResponseDTO(Schedule schedule) {
        return ScheduleResponseDTO.builder()
                .scheduleIdentifier(schedule.getScheduleIdentifier())
                .taskDate(schedule.getTaskDate())
                .taskDescription(schedule.getTaskDescription())
                .lotNumber(schedule.getLotNumber())
                .dayOfWeek(schedule.getDayOfWeek())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .tasks(schedule.getTasks() != null ? taskMapper.entitiesToResponseDTOs(schedule.getTasks()) : new ArrayList<>())
                .projectId(schedule.getProject() != null ? schedule.getProject().getProjectId() : null)
                .projectIdentifier(schedule.getProject() != null ? schedule.getProject().getProjectIdentifier() : null)
                .projectName(schedule.getProject() != null ? schedule.getProject().getProjectName() : null)
                .build();
    }

    public List<ScheduleResponseDTO> entitiesToResponseDTOs(List<Schedule> schedules) {
        return schedules.stream()
                .map(this::entityToResponseDTO)
                .collect(Collectors.toList());
    }

    public Schedule requestDTOToEntity(ScheduleRequestDTO requestDTO) {
        return Schedule.builder()
                .scheduleIdentifier(UUID.randomUUID().toString())
                .taskDate(requestDTO.getTaskDate())
                .taskDescription(requestDTO.getTaskDescription())
                .lotNumber(requestDTO.getLotNumber())
                .dayOfWeek(requestDTO.getDayOfWeek())
                .tasks(new ArrayList<>())
                .build();
    }

    public void updateEntityFromRequestDTO(Schedule schedule, ScheduleRequestDTO requestDTO) {
        schedule.setTaskDate(requestDTO.getTaskDate());
        schedule.setTaskDescription(requestDTO.getTaskDescription());
        schedule.setLotNumber(requestDTO.getLotNumber());
        schedule.setDayOfWeek(requestDTO.getDayOfWeek());
    }
}
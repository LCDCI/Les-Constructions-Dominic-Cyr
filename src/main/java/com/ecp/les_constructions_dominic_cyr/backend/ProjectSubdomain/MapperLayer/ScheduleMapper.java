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
                .scheduleStartDate(schedule.getScheduleStartDate())
                .scheduleEndDate(schedule.getScheduleEndDate())
                .scheduleDescription(schedule.getScheduleDescription())
                .lotId(schedule.getLotNumber())
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
                .scheduleStartDate(requestDTO.getScheduleStartDate())
                .scheduleEndDate(requestDTO.getScheduleEndDate())
                .scheduleDescription(requestDTO.getScheduleDescription())
                .lotId(requestDTO.getLotId() != null ? UUID.fromString(requestDTO.getLotId()) : null)
                .tasks(new ArrayList<>())
                .build();
    }

    public void updateEntityFromRequestDTO(Schedule schedule, ScheduleRequestDTO requestDTO) {
        schedule.setScheduleStartDate(requestDTO.getScheduleStartDate());
        schedule.setScheduleEndDate(requestDTO.getScheduleEndDate());
        schedule.setScheduleDescription(requestDTO.getScheduleDescription());
        schedule.setLotId(requestDTO.getLotId() != null ? UUID.fromString(requestDTO.getLotId()) : null);
    }
}
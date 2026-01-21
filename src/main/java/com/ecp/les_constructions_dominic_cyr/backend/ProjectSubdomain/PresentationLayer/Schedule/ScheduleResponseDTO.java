package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponseDTO {
    private String scheduleIdentifier;
    private LocalDate scheduleStartDate;
    private LocalDate scheduleEndDate;
    private String scheduleDescription;
    private String lotId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TaskDetailResponseDTO> tasks;
    private Long projectId;
    private String projectIdentifier;
    private String projectName;
}
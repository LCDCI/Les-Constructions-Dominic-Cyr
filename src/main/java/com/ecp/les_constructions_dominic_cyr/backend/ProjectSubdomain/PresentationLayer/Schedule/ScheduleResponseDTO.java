package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponseDTO {
    private String scheduleIdentifier;
    private LocalDate taskDate;
    private String taskDescription;
    private String lotNumber;
    private String dayOfWeek;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
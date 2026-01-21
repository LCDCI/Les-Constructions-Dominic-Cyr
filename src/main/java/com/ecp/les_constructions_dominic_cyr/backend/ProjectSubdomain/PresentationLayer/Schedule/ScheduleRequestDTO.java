package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleRequestDTO {
    private LocalDate scheduleStartDate;
    private LocalDate scheduleEndDate;
    private String scheduleDescription;
    private String lotId;
}

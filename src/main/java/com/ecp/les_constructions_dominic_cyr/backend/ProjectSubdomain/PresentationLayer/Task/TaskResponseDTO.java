package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDTO {

    private String taskIdentifier;
    private LocalDate taskDate;
    private String taskDescription;
    private String lotNumber;
    private String dayOfWeek;
    private String assignedTo;
}

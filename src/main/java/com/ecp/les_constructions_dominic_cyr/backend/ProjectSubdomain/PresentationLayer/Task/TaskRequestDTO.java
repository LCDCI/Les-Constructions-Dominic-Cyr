package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequestDTO {

    @NotNull(message = "Task date is required")
    private LocalDate taskDate;

    @NotBlank(message = "Task description is required")
    private String taskDescription;

    @NotBlank(message = "Lot number is required")
    private String lotNumber;

    @NotBlank(message = "Day of week is required")
    private String dayOfWeek;

    private String assignedTo;
}

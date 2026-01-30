package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing tasks grouped by lot within a project
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotTaskGroupDTO {
    private String lotId;
    private String lotNumber;
    private String scheduleId;
    private String scheduleDescription;
    private List<ContractorTaskViewDTO> tasks;
}


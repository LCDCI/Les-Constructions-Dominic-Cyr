package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing tasks grouped by project, then by lot
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectTaskGroupDTO {
    private String projectIdentifier;
    private String projectName;
    private List<LotTaskGroupDTO> lots;
}


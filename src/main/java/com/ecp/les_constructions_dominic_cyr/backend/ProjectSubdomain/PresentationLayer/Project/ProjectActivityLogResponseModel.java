package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectActivityLogResponseModel {
    private Long id;
    private String projectIdentifier;
    private String activityType;
    private String userIdentifier;
    private String userName;
    private String changedBy;
    private String changedByName;
    private LocalDateTime timestamp;
    private String description;
}

package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class ScheduleRequestDTO {
    Date updatedOn;
    Date completionDate;
    Boolean workStatus;
    Date workDate;
    String estimatedTime;
    Date workCompletionDate;
    String totalTime;
}

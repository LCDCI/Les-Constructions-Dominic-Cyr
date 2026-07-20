package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule;

import jakarta.persistence.*;
import lombok.Getter;
import java.util.List;

@Embeddable
@Getter
public class UpcomingWork {
    private List<UpcomingTaskItem> tasks;
}

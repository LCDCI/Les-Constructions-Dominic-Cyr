package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Embeddable
@Getter
public class UpcomingWork {
    private List<UpcomingTaskItem> tasks;
}

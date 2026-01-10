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
    @ElementCollection
    @CollectionTable(name = "schedule_task_refs", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "task_ref")
    private List<String> taskRefs;
    private WorkStatus workStatus;
    private Date workDate;
    private String estimatedTime;
    private Date workCompletionDate;
    private String totalTime;

    public UpcomingWork() {
    }

    public UpcomingWork(@NotNull WorkStatus workStatus,@NotNull Date workDate, @NotNull String estimatedTime, @NotNull Date workCompletionDate, @NotNull String totalTime) {
        Objects.requireNonNull(this.workStatus = workStatus);
        Objects.requireNonNull(this.workDate = workDate);
        Objects.requireNonNull(this.estimatedTime = estimatedTime);
        Objects.requireNonNull(this.workCompletionDate = workCompletionDate);
        Objects.requireNonNull(this.totalTime = totalTime);
    }
}

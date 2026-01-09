package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private ScheduleIdentifier scheduleIdentifier;

    private Date updatedOn;

    private Date completionDate;

    @Embedded
    private UpcomingWork upcomingWork;

    public Schedule(@NotNull Date updatedOn, @NotNull Date completionDate, @NotNull UpcomingWork upcomingWork) {
        this.scheduleIdentifier = new ScheduleIdentifier();
        this.updatedOn = updatedOn;
        this.completionDate = completionDate;
        this.upcomingWork = upcomingWork;
    }
}
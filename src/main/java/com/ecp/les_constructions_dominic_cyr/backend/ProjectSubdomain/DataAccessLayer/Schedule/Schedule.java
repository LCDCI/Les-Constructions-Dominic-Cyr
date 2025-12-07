package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_identifier", unique = true, nullable = false)
    private String scheduleIdentifier;

    @Column(nullable = false)
    private LocalDate taskDate;

    @Column(nullable = false)
    private String taskDescription;

    @Column(nullable = false)
    private String lotNumber;

    @Column(nullable = false)
    private String dayOfWeek;
}
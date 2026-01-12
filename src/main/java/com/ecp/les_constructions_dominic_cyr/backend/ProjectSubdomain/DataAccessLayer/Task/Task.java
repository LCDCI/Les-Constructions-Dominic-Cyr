package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Task;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_identifier", unique = true, nullable = false)
    private String taskIdentifier;

    @Column(nullable = false)
    private LocalDate taskDate;

    @Column(nullable = false)
    private String taskDescription;

    @Column(nullable = false)
    private String lotNumber;

    @Column(nullable = false)
    private String dayOfWeek;

    @Column(name = "assigned_to")
    private String assignedTo;
}

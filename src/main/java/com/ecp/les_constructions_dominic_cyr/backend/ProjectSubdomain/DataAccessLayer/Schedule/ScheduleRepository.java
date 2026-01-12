package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    Optional<Schedule> findByScheduleIdentifier(String scheduleIdentifier);

    List<Schedule> findByTaskDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT s FROM Schedule s WHERE s.taskDate >= :startDate AND s.taskDate <= :endDate ORDER BY s.taskDate ASC")
    List<Schedule> findCurrentWeekSchedules(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM Schedule s WHERE s.project.projectId = :projectId ORDER BY s.taskDate ASC")
    List<Schedule> findByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT s FROM Schedule s WHERE s.project.projectIdentifier = :projectIdentifier ORDER BY s.taskDate ASC")
    List<Schedule> findByProjectIdentifier(@Param("projectIdentifier") String projectIdentifier);
}
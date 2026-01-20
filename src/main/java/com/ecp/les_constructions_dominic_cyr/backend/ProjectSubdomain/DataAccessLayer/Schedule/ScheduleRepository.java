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

    List<Schedule> findByScheduleStartDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT s FROM Schedule s WHERE s.scheduleStartDate >= :startDate AND s.scheduleStartDate <= :endDate ORDER BY s.scheduleStartDate ASC")
    List<Schedule> findCurrentWeekSchedules(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM Schedule s WHERE s.project.projectId = :projectId ORDER BY s.scheduleStartDate ASC")
    List<Schedule> findByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT s FROM Schedule s LEFT JOIN FETCH s.project WHERE s.project.projectIdentifier = :projectIdentifier ORDER BY s.scheduleStartDate ASC")
    List<Schedule> findByProjectIdentifier(@Param("projectIdentifier") String projectIdentifier);
}
package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByTaskIdentifier(String taskIdentifier);

    List<Task> findByTaskDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT t FROM Task t WHERE t.taskDate >= :startDate AND t.taskDate <= :endDate ORDER BY t.taskDate ASC")
    List<Task> findCurrentWeekTasks(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<Task> findByAssignedTo(String assignedTo);

    @Query("SELECT t FROM Task t WHERE t.assignedTo = :assignedTo AND t.taskDate >= :startDate AND t.taskDate <= :endDate ORDER BY t.taskDate ASC")
    List<Task> findCurrentWeekTasksByAssignedTo(@Param("assignedTo") String assignedTo, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

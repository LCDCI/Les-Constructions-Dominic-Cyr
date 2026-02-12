package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    Optional<Task> findByTaskIdentifier_TaskId(String taskId);

    List<Task> findByAssignedTo(Users user);

    List<Task> findByAssignedTo_UserIdentifier_UserId(UUID userId);

    void deleteByTaskIdentifier_TaskId(String taskId);

    List<Task> findByScheduleId(String scheduleId);

    List<Task> findByScheduleIdAndTaskStatus(String scheduleId, TaskStatus taskStatus);

    @Query("SELECT t FROM Task t WHERE t.scheduleId IN " +
           "(SELECT s.scheduleIdentifier FROM Schedule s WHERE s.project.projectIdentifier = :projectIdentifier)")
    List<Task> findByProjectIdentifier(@Param("projectIdentifier") String projectIdentifier);

    @Query("SELECT t FROM Task t WHERE t.scheduleId IN " +
           "(SELECT s.scheduleIdentifier FROM Schedule s WHERE s.project.projectIdentifier = :projectIdentifier) " +
           "AND t.taskStatus = :taskStatus")
    List<Task> findByProjectIdentifierAndTaskStatus(@Param("projectIdentifier") String projectIdentifier,
                                                      @Param("taskStatus") TaskStatus taskStatus);

    // Find tasks by lot_id
    List<Task> findByLotId(UUID lotId);

    // Find completed tasks by lot_id
    List<Task> findByLotIdAndTaskStatus(UUID lotId, TaskStatus taskStatus);
}

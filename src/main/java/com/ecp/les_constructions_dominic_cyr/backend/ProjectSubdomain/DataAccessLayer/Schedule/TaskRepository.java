package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    Optional<Task> findByTaskIdentifier_TaskId(String taskId);

    List<Task> findByAssignedTo(Users user);

    List<Task> findByAssignedTo_UserIdentifier_UserId(String userId);

    void deleteByTaskIdentifier_TaskId(String taskId);
}

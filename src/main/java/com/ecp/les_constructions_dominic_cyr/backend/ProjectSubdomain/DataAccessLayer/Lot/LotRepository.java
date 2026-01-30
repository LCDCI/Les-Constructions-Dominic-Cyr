package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LotRepository extends JpaRepository<Lot, Integer> {
    Lot findByLotIdentifier_LotId(String lotId);
    List<Lot> findByProject_ProjectIdentifier(String projectIdentifier);

    // Find lots by assigned user (using the ManyToMany relationship)
    @Query("SELECT l FROM Lot l JOIN l.assignedUsers u WHERE u = :user")
    List<Lot> findByAssignedUser(@Param("user") Users user);

    @Query("SELECT l FROM Lot l JOIN l.assignedUsers u WHERE u.userIdentifier.userId = :userId")
    List<Lot> findByAssignedUserId(@Param("userId") UUID userId);
}

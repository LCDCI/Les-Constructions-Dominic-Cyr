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
    Lot findByLotIdentifier_LotId(UUID lotId);
    
    @Query("SELECT l FROM Lot l LEFT JOIN FETCH l.assignedUsers WHERE l.lotIdentifier.lotId = :lotId")
    Lot findByLotIdentifier_LotIdWithUsers(@Param("lotId") UUID lotId);
    
    List<Lot> findByProject_ProjectIdentifier(String projectIdentifier);

    // Find lots by assigned user (using the ManyToMany relationship)
    @Query("SELECT l FROM Lot l JOIN l.assignedUsers u WHERE u = :user")
    List<Lot> findByAssignedUser(@Param("user") Users user);

    @Query("SELECT l FROM Lot l JOIN l.assignedUsers u WHERE u.userIdentifier.userId = :userId")
    List<Lot> findByAssignedUserId(@Param("userId") UUID userId);

    // Find lots in a project where both salesperson and customer are assigned
    @Query("SELECT l FROM Lot l " +
           "JOIN l.assignedUsers u1 " +
           "JOIN l.assignedUsers u2 " +
           "WHERE u1.userIdentifier.userId = :salespersonId " +
           "AND u2.userIdentifier.userId = :customerId " +
           "AND l.project.projectIdentifier = :projectIdentifier")
    List<Lot> findByProjectAndBothUsersAssigned(
        @Param("projectIdentifier") String projectIdentifier,
        @Param("salespersonId") UUID salespersonId,
        @Param("customerId") UUID customerId);
}

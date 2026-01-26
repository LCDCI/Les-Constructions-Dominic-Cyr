package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LotRepository extends JpaRepository<Lot, Integer> {
    Lot findByLotIdentifier_LotId(String lotId);
    List<Lot> findByProjectId(String projectId);
    List<Lot> findByAssignedCustomer(Users customer);
    List<Lot> findByAssignedCustomer_UserIdentifier_UserId(UUID userId);
}

package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LotRepository extends JpaRepository<Lot, Integer> {
    Lot findByLotIdentifier_LotId(String lotId);
    List<Lot> findByProject_ProjectIdentifier(String projectIdentifier);
}

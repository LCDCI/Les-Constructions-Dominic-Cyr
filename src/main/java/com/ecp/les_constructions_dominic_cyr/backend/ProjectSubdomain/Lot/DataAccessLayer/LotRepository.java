package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.DataAccessLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LotRepository extends JpaRepository<Lot, Integer> {
    Lot findByLotIdentifier_LotId(String lotId);

    //this method can be uncommented if you need to find lots by project identifier
    //List<Lot> findLotsByProject_ProjectIdentifier(String projectId);
}

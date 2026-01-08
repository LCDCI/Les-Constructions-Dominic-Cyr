package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RealizationRepository extends JpaRepository<Realization, Integer> {
    Realization findRealizationByRealizationIdentifier_RealizationId(String realizationId);
}

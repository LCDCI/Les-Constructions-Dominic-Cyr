package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RenovationRepository extends JpaRepository< Renovation, Integer> {
    public Renovation findRenovationByRenovationIdentifier_RenovationId(String renovationId);
}

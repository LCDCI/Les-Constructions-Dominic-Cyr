package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HouseRepository extends JpaRepository<House, Long> {
    List<House> findAllByProjectIdentifierOrderByHouseNameAsc(String projectIdentifier);
    House findByHouseIdentifier(UUID houseIdentifier);
}

package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseRepository extends JpaRepository<House, Integer> {
    House findHouseByHouseIdentifier_HouseId(String houseId);
}

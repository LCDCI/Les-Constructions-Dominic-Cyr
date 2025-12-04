package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.House;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.House.HouseResponseModel;

import java.util.List;

public interface HouseService {
    List<HouseResponseModel> getAllHouses();
    HouseResponseModel getHouseById(String houseId);
}

package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.House;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House.House;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House.HouseRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.House.HouseResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HouseServiceImpl implements HouseService{
    private final HouseRepository houseRepository;

    public HouseServiceImpl(HouseRepository houseRepository) {
        this.houseRepository = houseRepository;
    }

    @Override
    public List<HouseResponseModel> getAllHouses() {
        List<House> houses = houseRepository.findAll();
        List<HouseResponseModel> houseResponseModels = new ArrayList<>();

        for (House house : houses) {
            houseResponseModels.add(mapToResponseModel(house));
        }

        return houseResponseModels;
    }

    @Override
    public HouseResponseModel getHouseById(String houseId) {
        House house = houseRepository.findHouseByHouseIdentifier_HouseId(houseId);

        if(house == null){
            throw new NotFoundException("Unknown House Id: " + houseId);
        }

        return mapToResponseModel(house);
    }

    private HouseResponseModel mapToResponseModel(House house){
        HouseResponseModel dto = new HouseResponseModel();
        dto.setHouseId(house.getHouseIdentifier().getHouseId());
        dto.setHouseName(house.getHouseName());
        dto.setLocation(house.getLocation());
        dto.setDescription(house.getDescription());
        dto.setNumberOfRooms(house.getNumberOfRooms());
        dto.setNumberOfBedrooms(house.getNumberOfBedrooms());
        dto.setNumberOfBathrooms(house.getNumberOfBathrooms());
        dto.setConstructionYear(house.getConstructionYear());
        return dto;
    }
}

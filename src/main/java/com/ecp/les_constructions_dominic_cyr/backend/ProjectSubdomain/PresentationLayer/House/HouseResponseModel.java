package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.House;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HouseResponseModel {
    private String houseId;
    private String houseName;
    private String location;
    private String description;
    private Integer numberOfRooms;
    private Integer numberOfBedrooms;
    private Integer numberOfBathrooms;
    private Integer constructionYear;
}

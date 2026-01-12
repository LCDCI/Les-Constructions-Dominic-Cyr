package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Realization;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RealizationResponseModel {
    private String realizationId;
    private String realizationName;
    private String location;
    private String description;
    private String imageIdentifier;
    private Integer numberOfRooms;
    private Integer numberOfBedrooms;
    private Integer numberOfBathrooms;
    private Integer constructionYear;
}

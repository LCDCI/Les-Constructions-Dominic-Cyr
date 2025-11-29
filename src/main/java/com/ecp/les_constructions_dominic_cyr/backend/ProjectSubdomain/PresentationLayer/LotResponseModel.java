package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LotResponseModel {
    private String lotIdentifier;
    private String location;
    private Float price;
    private String dimensions;
    private LotStatus status;
}

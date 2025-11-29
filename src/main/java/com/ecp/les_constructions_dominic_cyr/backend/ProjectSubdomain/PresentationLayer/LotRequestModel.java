package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class LotRequestModel {
    private String location;
    private Float price;
    private String dimensions;
    private LotStatus status;
}

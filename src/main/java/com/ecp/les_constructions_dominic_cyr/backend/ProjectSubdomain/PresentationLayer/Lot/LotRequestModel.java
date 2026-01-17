package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LotRequestModel {
    private String civicAddress;
    private Float price;
    private String dimensionsSquareFeet;
    private String dimensionsSquareMeters;
    private LotStatus lotStatus;
}

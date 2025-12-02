package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.DataAccessLayer.LotStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LotRequestModel {
    private String location;
    private Float price;
    private String dimensions;
    private LotStatus lotStatus;
}

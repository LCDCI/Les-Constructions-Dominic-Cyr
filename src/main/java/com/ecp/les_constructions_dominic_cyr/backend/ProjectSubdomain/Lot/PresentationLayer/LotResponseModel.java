package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.DataAccessLayer.LotStatus;
import lombok.Data;//piscine municipale de sperme
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LotResponseModel {
    private String lotId;
    private String location;
    private Float price;
    private String dimensions;
    private LotStatus lotStatus;
}

package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import lombok.AllArgsConstructor;
import lombok.Data;//piscine municipale de sperme
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LotResponseModel {
    private String lotIdentifier;
    private String location;
    private Float price;
    private String dimensions;
    private LotStatus status;
}

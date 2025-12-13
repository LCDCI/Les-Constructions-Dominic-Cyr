package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;//piscine municipale de sperme
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LotResponseModel {
    private String lotId;
    private String imageIdentifier;
    private String location;
    private Float price;
    private String dimensions;
    private LotStatus lotStatus;
}

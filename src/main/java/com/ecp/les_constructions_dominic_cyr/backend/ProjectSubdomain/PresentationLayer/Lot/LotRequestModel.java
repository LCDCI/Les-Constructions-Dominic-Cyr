package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class LotRequestModel {
    private String lotNumber;
    private String civicAddress;
    private Float price;
    private String dimensionsSquareFeet;
    private String dimensionsSquareMeters;
    private LotStatus lotStatus;
    // Support multiple assigned users of any role
    private List<String> assignedUserIds;
}

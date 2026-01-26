package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LotResponseModel {
    private String lotId;
    private String lotNumber;
    private String civicAddress;
    private Float price;
    private String dimensionsSquareFeet;
    private String dimensionsSquareMeters;
    private LotStatus lotStatus;
    private String assignedCustomerId;
    private String assignedCustomerName;
    
    // Project information (like Schedule does)
    private Long projectId;
    private String projectIdentifier;
    private String projectName;
}

package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Renovation;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RenovationResponseModel {
    private String renovationId;
    private String beforeImageIdentifier;
    private String afterImageIdentifier;
    private String description;
}

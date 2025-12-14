package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Renovation;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Renovation.RenovationRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Renovation.RenovationResponseModel;

import java.util.List;

public interface RenovationService {
    List<RenovationResponseModel> getAllRenovations();
    RenovationResponseModel getRenovationById(String renovationId);
    RenovationResponseModel createRenovation(RenovationRequestModel renovationRequestModel);
    RenovationResponseModel updateRenovation(RenovationRequestModel renovationRequestModel, String renovationId);
    void deleteRenovation(String renovationId);
}

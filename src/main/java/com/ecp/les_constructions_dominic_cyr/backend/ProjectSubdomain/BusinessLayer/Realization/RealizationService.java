package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Realization;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Realization.RealizationResponseModel;

import java.util.List;

public interface RealizationService {
    List<RealizationResponseModel> getAllRealizations();
    RealizationResponseModel getRealizationById(String realizationId);
}

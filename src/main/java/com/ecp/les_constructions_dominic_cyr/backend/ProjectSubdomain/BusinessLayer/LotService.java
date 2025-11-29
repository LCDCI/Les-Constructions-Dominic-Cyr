package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LotResponseModel;

import java.util.List;

public interface LotService {
    List<LotResponseModel> getAllLots();
    LotResponseModel getLotById(String lotId);
    LotResponseModel addLot(LotResponseModel lotRequestModel);
    LotResponseModel updateLot(LotResponseModel lotRequestModel, String lotId);
    void deleteLot(String lotId);
}

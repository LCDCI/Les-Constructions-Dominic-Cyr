package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.PresentationLayer.LotRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.PresentationLayer.LotResponseModel;

import java.util.List;

public interface LotService {
    List<LotResponseModel> getAllLots();
    LotResponseModel getLotById(String lotId);
    LotResponseModel addLot(LotRequestModel lotRequestModel);
    LotResponseModel updateLot(LotRequestModel lotRequestModel, String lotId);
    void deleteLot(String lotId);
}

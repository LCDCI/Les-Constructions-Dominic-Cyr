package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Lot;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotResponseModel;

import java.util.List;

public interface LotService {
    List<LotResponseModel> getAllLots();
    List<LotResponseModel> getAllLotsByProject(String projectIdentifier);
    List<LotResponseModel> getLotsByProjectAndBothUsersAssigned(String projectIdentifier, String salespersonId, String customerId);
    LotResponseModel getLotById(String lotId);
    LotResponseModel addLotToProject(String projectIdentifier, LotRequestModel lotRequestModel);
    LotResponseModel updateLot(LotRequestModel lotRequestModel, String lotId);
    void deleteLot(String lotId);
    List<LotResponseModel> mapLotsToResponses(List<Lot> lots);
}

package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LotResponseModel;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LotServiceImpl implements LotService{
    private LotRepository lotRepository;

    public LotServiceImpl(LotRepository lotRepository) {
        this.lotRepository = lotRepository;
    }

    @Override
    public List<LotResponseModel> getAllLots() {
        List<Lot> lotEntities = lotRepository.findAll();
        List<LotResponseModel> lotResponseModelList = new ArrayList<>();
        for(Lot lot: lotEntities){
            LotResponseModel lotResponseModel = new LotResponseModel();
            BeanUtils.copyProperties(lot, lotResponseModel);
            // Here you would typically copy properties from lot to lotResponseModel
            lotResponseModelList.add(lotResponseModel);
        }
        return lotResponseModelList;
    }

    @Override
    public LotResponseModel getLotById(String lotId) {
        Lot lot = lotRepository.findLotByLotIdentifier(lotId);
        if(lot == null){
            //will have to implement the necessary exception handling classes in a utils folder
            //throw new NotFoundExcetion("Unknown Lot Id: " + lotId);
        }
        LotResponseModel lotResponseModel = new LotResponseModel();
        BeanUtils.copyProperties(lot, lotResponseModel);
        return lotResponseModel;
    }

    @Override
    public LotResponseModel addLot(LotResponseModel lotRequestModel) {
        Lot lot = new Lot();
        BeanUtils.copyProperties(lotRequestModel, lot);
        lot.setLotIdentifier(new LotIdentifier());

        Lot savedLot = lotRepository.save(lot);
        LotResponseModel lotResponseModel = new LotResponseModel();
        BeanUtils.copyProperties(savedLot, lotResponseModel);
        return lotResponseModel;
    }

    @Override
    public LotResponseModel updateLot(LotResponseModel lotRequestModel, String lotId) {
        Lot foundLot = lotRepository.findLotByLotIdentifier(lotId);
        if(foundLot == null){
            //will have to implement the necessary exception handling classes in a utils folder
            //throw new NotFoundExcetion("Unknown Lot Id: " + lotId);
        }
        Lot lot = new Lot();
        BeanUtils.copyProperties(lotRequestModel, lot);
        lot.setId(foundLot.getId());
        Lot updatedLot = lotRepository.save(lot);

        LotResponseModel lotResponseModel = new LotResponseModel();
        BeanUtils.copyProperties(updatedLot, lotResponseModel);
        return lotResponseModel;
    }

    @Override
    public void deleteLot(String lotId) {
        Lot foundLot = lotRepository.findLotByLotIdentifier(lotId);
        if(foundLot == null){
            //will have to implement the necessary exception handling classes in a utils folder
            //throw new NotFoundExcetion("Unknown Lot Id: " + lotId);
        }
        try{
            lotRepository.delete(foundLot);
        } catch (Exception e){
            //will have to implement the necessary exception handling classes in a utils folder
            //throw new DatabaseException("Could not delete lot Id: " + lotId);
        }
    }
}

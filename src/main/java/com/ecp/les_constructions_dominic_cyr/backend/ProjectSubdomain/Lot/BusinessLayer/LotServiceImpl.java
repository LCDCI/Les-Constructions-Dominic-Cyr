package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.DataAccessLayer.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.DataAccessLayer.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.DataAccessLayer.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.PresentationLayer.LotRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.Lot.PresentationLayer.LotResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
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
            lotResponseModelList.add(lotResponseModel);
        }
        return lotResponseModelList;
    }

    @Override
    public LotResponseModel getLotById(String lotId) {
        Lot lot = lotRepository.findLotByLotIdentifier(lotId);
        if(lot == null){
            throw new NotFoundException("Unknown Lot Id: " + lotId);
        }
        LotResponseModel lotResponseModel = new LotResponseModel();
        BeanUtils.copyProperties(lot, lotResponseModel);
        return lotResponseModel;
    }

    @Override
    public LotResponseModel addLot(LotRequestModel lotRequestModel) {
        Lot lot = new Lot();
        BeanUtils.copyProperties(lotRequestModel, lot);
        lot.setLotIdentifier(new LotIdentifier());

        Lot savedLot = lotRepository.save(lot);
        LotResponseModel lotResponseModel = new LotResponseModel();
        BeanUtils.copyProperties(savedLot, lotResponseModel);
        return lotResponseModel;
    }

    @Override
    public LotResponseModel updateLot(LotRequestModel lotRequestModel, String lotId) {
        Lot foundLot = lotRepository.findLotByLotIdentifier(lotId);
        if(foundLot == null){
            throw new NotFoundException("Unknown Lot Id: " + lotId);
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
            throw new NotFoundException("Unknown Lot Id: " + lotId);
        }
        try{
            lotRepository.delete(foundLot);
        } catch (Exception e){
            //will have to implement the necessary exception handling classes in a utils folder
            //throw new DatabaseException("Could not delete lot Id: " + lotId);
        }
    }
}

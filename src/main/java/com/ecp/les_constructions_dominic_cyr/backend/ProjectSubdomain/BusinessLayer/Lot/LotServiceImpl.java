package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Lot;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LotServiceImpl implements LotService{
    private final LotRepository lotRepository;

    public LotServiceImpl(LotRepository lotRepository) {
        this.lotRepository = lotRepository;
    }

    @Override
    public List<LotResponseModel> getAllLots() {
        List<Lot> lots = lotRepository.findAll();
        List<LotResponseModel> responseList = new ArrayList<>();

        for (Lot lot : lots) {
            responseList.add(mapToResponse(lot));
        }

        return responseList;
    }

    @Override
    public LotResponseModel getLotById(String lotId) {
        Lot lot = lotRepository.findByLotIdentifier_LotId(lotId);

        if (lot == null)
            throw new NotFoundException("Unknown Lot Id: " + lotId);

        return mapToResponse(lot);
    }

    @Override
    public LotResponseModel addLot(LotRequestModel lotRequestModel) {
        Lot lot = new Lot();
        validateLotRequest(lotRequestModel);
        
        lot.setLocation(lotRequestModel.getLocation());
        lot.setPrice(lotRequestModel.getPrice());
        lot.setDimensions(lotRequestModel.getDimensions());
        lot.setLotStatus(lotRequestModel.getLotStatus());
        lot.setLotIdentifier(new LotIdentifier());   // always generate

        Lot savedLot = lotRepository.save(lot);
        return mapToResponse(savedLot);
    }

    @Override
    public LotResponseModel updateLot(LotRequestModel lotRequestModel, String lotId) {
        Lot foundLot = lotRepository.findByLotIdentifier_LotId(lotId);
        if(foundLot == null){
            throw new NotFoundException("Unknown Lot Id: " + lotId);
        }
        Lot updatedLot = lotRepository.save(foundLot);
        validateLotRequest(lotRequestModel);
        // Update the existing entity instead of creating a new one (preserve id and embedded lotIdentifier)
        foundLot.setLocation(lotRequestModel.getLocation());
        foundLot.setPrice(lotRequestModel.getPrice());
        foundLot.setDimensions(lotRequestModel.getDimensions());
        foundLot.setLotStatus(lotRequestModel.getLotStatus());

        return mapToResponse(updatedLot);
    }

    @Override
    public void deleteLot(String lotId) {
        Lot foundLot = lotRepository.findByLotIdentifier_LotId(lotId);
        if(foundLot == null){
            throw new NotFoundException("Unknown Lot Id: " + lotId);
        }
        lotRepository.delete(foundLot);
    }

    private LotResponseModel mapToResponse(Lot lot) {
        LotResponseModel dto = new LotResponseModel();
        dto.setLotId(lot.getLotIdentifier().getLotId());
        dto.setLocation(lot.getLocation());
        dto.setPrice(lot.getPrice());
        dto.setDimensions(lot.getDimensions());
        dto.setLotStatus(lot.getLotStatus());
        return dto;
    }

    private void validateLotRequest(LotRequestModel lotRequestModel) {
        if (lotRequestModel.getLocation() == null || lotRequestModel.getLocation().isBlank()) {
            throw new InvalidInputException("Location must not be blank");
        }
        if (lotRequestModel.getPrice() == null || lotRequestModel.getPrice() <= 0) {
            throw new InvalidInputException("Price must be positive");
        }
        if (lotRequestModel.getDimensions() == null || lotRequestModel.getDimensions().isBlank()) {
            throw new InvalidInputException("Dimensions must not be blank");
        }
        if (lotRequestModel.getLotStatus() == null) {
            throw new InvalidInputException("Lot status must not be null");
        }
    }
}

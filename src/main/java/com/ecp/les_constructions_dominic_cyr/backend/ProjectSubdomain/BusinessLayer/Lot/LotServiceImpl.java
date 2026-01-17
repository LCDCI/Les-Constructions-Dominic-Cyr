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

        lot.setCivicAddress(lotRequestModel.getCivicAddress());
        lot.setPrice(lotRequestModel.getPrice());
        lot.setDimensionsSquareFeet(lotRequestModel.getDimensionsSquareFeet());
        lot.setDimensionsSquareMeters(lotRequestModel.getDimensionsSquareMeters());
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

        validateLotRequest(lotRequestModel);
        // Update the existing entity instead of creating a new one (preserve id and embedded lotIdentifier)
        foundLot.setCivicAddress(lotRequestModel.getCivicAddress());
        foundLot.setPrice(lotRequestModel.getPrice());
        foundLot.setDimensionsSquareFeet(lotRequestModel.getDimensionsSquareFeet());
        foundLot.setDimensionsSquareMeters(lotRequestModel.getDimensionsSquareMeters());
        foundLot.setLotStatus(lotRequestModel.getLotStatus());
        Lot updatedLot = lotRepository.save(foundLot);
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
        dto.setCivicAddress(lot.getCivicAddress());
        dto.setPrice(lot.getPrice());
        dto.setDimensionsSquareFeet(lot.getDimensionsSquareFeet());
        dto.setDimensionsSquareMeters(lot.getDimensionsSquareMeters());
        dto.setLotStatus(lot.getLotStatus());
        return dto;
    }

    private void validateLotRequest(LotRequestModel lotRequestModel) {
        if (lotRequestModel.getCivicAddress() == null || lotRequestModel.getCivicAddress().isBlank()) {
            throw new InvalidInputException("Civic address must not be blank");
        }
        if (lotRequestModel.getPrice() == null || lotRequestModel.getPrice() <= 0) {
            throw new InvalidInputException("Price must be positive");
        }
        if (lotRequestModel.getDimensionsSquareFeet() == null || lotRequestModel.getDimensionsSquareFeet().isBlank()) {
            throw new InvalidInputException("Dimensions in square feet must not be blank");
        }
        if (lotRequestModel.getDimensionsSquareMeters() == null || lotRequestModel.getDimensionsSquareMeters().isBlank()) {
            throw new InvalidInputException("Dimensions in square meters must not be blank");
        }
        if (lotRequestModel.getLotStatus() == null) {
            throw new InvalidInputException("Lot status must not be null");
        }
    }
}

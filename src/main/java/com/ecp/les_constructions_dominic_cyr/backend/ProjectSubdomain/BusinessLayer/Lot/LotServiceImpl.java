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
        return mapLotsToResponses(lots);
    }

    @Override
    public List<LotResponseModel> getAllLotsByProject(String projectIdentifier) {
        if (projectIdentifier == null || projectIdentifier.isBlank()) {
            throw new InvalidInputException("Project identifier must not be blank");
        }
        List<Lot> lots = lotRepository.findByProject_ProjectIdentifier(projectIdentifier);
        return mapLotsToResponses(lots);
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
        validateLotRequest(lotRequestModel);

        Lot lot = new Lot(
                new LotIdentifier(),  // always generate new ID
                lotRequestModel.getLotNumber(),
                lotRequestModel.getCivicAddress(),
                lotRequestModel.getPrice(),
                lotRequestModel.getDimensionsSquareFeet(),
                lotRequestModel.getDimensionsSquareMeters(),
                lotRequestModel.getLotStatus()
        );

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
        foundLot.setLotNumber(lotRequestModel.getLotNumber());
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
        dto.setLotNumber(lot.getLotNumber());
        dto.setCivicAddress(lot.getCivicAddress());
        dto.setPrice(lot.getPrice());
        dto.setDimensionsSquareFeet(lot.getDimensionsSquareFeet());
        dto.setDimensionsSquareMeters(lot.getDimensionsSquareMeters());
        dto.setLotStatus(lot.getLotStatus());
        return dto;
    }

    private List<LotResponseModel> mapLotsToResponses(List<Lot> lots) {
        List<LotResponseModel> responseList = new ArrayList<>();
        for (Lot lot : lots) {
            responseList.add(mapToResponse(lot));
        }
        return responseList;
    }

    private void validateLotRequest(LotRequestModel lotRequestModel) {
        if (lotRequestModel.getLotNumber() == null || lotRequestModel.getLotNumber().isBlank()) {
            throw new InvalidInputException("Lot number must not be blank");
        }
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

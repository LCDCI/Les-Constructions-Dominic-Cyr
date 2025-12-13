package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Renovation;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.Renovation;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.RenovationIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.RenovationRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Renovation.RenovationRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Renovation.RenovationResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RenovationSericeImpl implements RenovationService{

    private final RenovationRepository renovationRepository;

    public RenovationSericeImpl(RenovationRepository renovationRepository) {
        this.renovationRepository = renovationRepository;
    }

    @Override
    public List<RenovationResponseModel> getAllRenovations() {
        List<Renovation> renovations = renovationRepository.findAll();
        List<RenovationResponseModel> responseList = new ArrayList<>();
        for (Renovation renovation: renovations){
            responseList.add(mapToResponse(renovation));
        }
        return responseList;
    }

    @Override
    public RenovationResponseModel getRenovationById(String renovationId) {
        Renovation renovation = renovationRepository.findRenovationByRenovationIdentifier_RenovationId(renovationId);
        if (renovation == null){
            throw new NotFoundException("Unknown Renovation Id: " + renovationId);
        }
        return mapToResponse(renovation);
    }

    @Override
    public RenovationResponseModel createRenovation(RenovationRequestModel renovationRequestModel) {
        Renovation renovation = new Renovation();
        validateRenovationRequest(renovationRequestModel);
        renovation.setBeforeImageIdentifier(renovationRequestModel.getBeforeImageIdentifier());
        renovation.setAfterImageIdentifier(renovationRequestModel.getAfterImageIdentifier());
        renovation.setDescription(renovationRequestModel.getDescription());
        renovation.setRenovationIdentifier(new RenovationIdentifier());
        Renovation savedRenovation = renovationRepository.save(renovation);
        return mapToResponse(savedRenovation);
    }

    @Override
    public RenovationResponseModel updateRenovation(RenovationRequestModel renovationRequestModel, String renovationId) {
        Renovation renovation = renovationRepository.findRenovationByRenovationIdentifier_RenovationId(renovationId);
        if (renovation == null){
            throw new NotFoundException("Unknown Renovation Id: " + renovationId);
        }
        validateRenovationRequest(renovationRequestModel);
        renovation.setBeforeImageIdentifier(renovationRequestModel.getBeforeImageIdentifier());
        renovation.setAfterImageIdentifier(renovationRequestModel.getAfterImageIdentifier());
        renovation.setDescription(renovationRequestModel.getDescription());
        Renovation updatedRenovation = renovationRepository.save(renovation);
        return mapToResponse(updatedRenovation);
    }

    @Override
    public void deleteRenovation(String renovationId) {
        Renovation foundRenovation = renovationRepository.findRenovationByRenovationIdentifier_RenovationId(renovationId);
        if (foundRenovation == null){
            throw new NotFoundException("Unknown Renovation Id: " + renovationId);
        }
        renovationRepository.delete(foundRenovation);
    }

    private RenovationResponseModel mapToResponse(Renovation renovation){
        RenovationResponseModel responseModel = new RenovationResponseModel();
        responseModel.setRenovationId(renovation.getRenovationIdentifier().getRenovationId());
        responseModel.setBeforeImageIdentifier(renovation.getBeforeImageIdentifier());
        responseModel.setAfterImageIdentifier(renovation.getAfterImageIdentifier());
        responseModel.setDescription(renovation.getDescription());
        return responseModel;
    }

    private void validateRenovationRequest(RenovationRequestModel renovationRequestModel) {
        if (renovationRequestModel.getBeforeImageIdentifier() == null || renovationRequestModel.getBeforeImageIdentifier().isEmpty()) {
            throw new IllegalArgumentException("Before Image Identifier cannot be null or empty.");
        }
        if (renovationRequestModel.getAfterImageIdentifier() == null || renovationRequestModel.getAfterImageIdentifier().isEmpty()) {
            throw new IllegalArgumentException("After Image Identifier cannot be null or empty.");
        }
        if (renovationRequestModel.getDescription() == null || renovationRequestModel.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }
    }
}

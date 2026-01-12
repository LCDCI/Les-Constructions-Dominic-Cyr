package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Realization;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization.Realization;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization.RealizationRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Realization.RealizationResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RealizationServiceImpl implements RealizationService{
    private final RealizationRepository realizationRepository;

    public RealizationServiceImpl(RealizationRepository realizationRepository) {
        this.realizationRepository = realizationRepository;
    }

    @Override
    public List<RealizationResponseModel> getAllRealizations() {
        List<Realization> realizations = realizationRepository.findAll();
        List<RealizationResponseModel> realizationResponseModels = new ArrayList<>();

        for (Realization realization : realizations) {
            realizationResponseModels.add(mapToResponseModel(realization));
        }

        return realizationResponseModels;
    }

    @Override
    public RealizationResponseModel getRealizationById(String realizationId) {
        Realization realization = realizationRepository.findRealizationByRealizationIdentifier_RealizationId(realizationId);

        if(realization == null){
            throw new NotFoundException("Unknown Realization Id: " + realizationId);
        }

        return mapToResponseModel(realization);
    }

    private RealizationResponseModel mapToResponseModel(Realization realization){
        RealizationResponseModel dto = new RealizationResponseModel();
        dto.setRealizationId(realization.getRealizationIdentifier().getRealizationId());
        dto.setRealizationName(realization.getRealizationName());
        dto.setLocation(realization.getLocation());
        dto.setDescription(realization.getDescription());
        dto.setImageIdentifier(realization.getImageIdentifier());
        dto.setNumberOfRooms(realization.getNumberOfRooms());
        dto.setNumberOfBedrooms(realization.getNumberOfBedrooms());
        dto.setNumberOfBathrooms(realization.getNumberOfBathrooms());
        dto.setConstructionYear(realization.getConstructionYear());
        return dto;
    }
}

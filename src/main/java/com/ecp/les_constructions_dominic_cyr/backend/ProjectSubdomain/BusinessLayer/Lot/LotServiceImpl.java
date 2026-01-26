package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Lot;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LotServiceImpl implements LotService{
    private final LotRepository lotRepository;
    private final UsersRepository usersRepository;
    private final ProjectRepository projectRepository;

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
    @Transactional
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

        // Handle customer assignment
        if (lotRequestModel.getAssignedCustomerId() != null && !lotRequestModel.getAssignedCustomerId().isBlank()) {
            Users customer = getCustomerAndValidateRole(lotRequestModel.getAssignedCustomerId());
            lot.setAssignedCustomer(customer);
        }

        Lot savedLot = lotRepository.save(lot);
        return mapToResponse(savedLot);
    }

    @Override
    @Transactional
    public LotResponseModel addLotToProject(String projectIdentifier, LotRequestModel lotRequestModel) {
        log.info("Creating lot for project identifier: {}", projectIdentifier);

        // Find the project
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new NotFoundException("Project not found with identifier: " + projectIdentifier));

        log.info("Found project: {} with projectId: {}", project.getProjectName(), project.getProjectIdentifier());

        validateLotRequest(lotRequestModel);

        Lot lot = new Lot(
                new LotIdentifier(),
                lotRequestModel.getLotNumber(),
                lotRequestModel.getCivicAddress(),
                lotRequestModel.getPrice(),
                lotRequestModel.getDimensionsSquareFeet(),
                lotRequestModel.getDimensionsSquareMeters(),
                lotRequestModel.getLotStatus()
        );

        // Set the project entity reference (like Schedule does)
        lot.setProject(project);

        log.info("Set project on lot: {}", project.getProjectIdentifier());

        // Handle customer assignment
        if (lotRequestModel.getAssignedCustomerId() != null && !lotRequestModel.getAssignedCustomerId().isBlank()) {
            Users customer = getCustomerAndValidateRole(lotRequestModel.getAssignedCustomerId());
            lot.setAssignedCustomer(customer);
        }

        Lot savedLot = lotRepository.save(lot);
        return mapToResponse(savedLot);
    }

    @Override
    @Transactional
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

        // Handle customer assignment update
        if (lotRequestModel.getAssignedCustomerId() != null && !lotRequestModel.getAssignedCustomerId().isBlank()) {
            Users customer = getCustomerAndValidateRole(lotRequestModel.getAssignedCustomerId());
            foundLot.setAssignedCustomer(customer);
        } else {
            foundLot.setAssignedCustomer(null); // Allow unassigning
        }

        Lot updatedLot = lotRepository.save(foundLot);
        return mapToResponse(updatedLot);
    }

    @Override
    @Transactional
    public void deleteLot(String lotId) {
        Lot foundLot = lotRepository.findByLotIdentifier_LotId(lotId);
        if(foundLot == null){
            throw new NotFoundException("Unknown Lot Id: " + lotId);
        }
        lotRepository.delete(foundLot);
    }

    private Users getCustomerAndValidateRole(String customerId) {
        try {
            UUID customerUuid = UUID.fromString(customerId);
            Users customer = usersRepository.findByUserIdentifier_UserId(customerUuid)
                    .orElseThrow(() -> new NotFoundException("Customer not found with ID: " + customerId));

            if (customer.getUserRole() != UserRole.CUSTOMER) {
                throw new InvalidInputException("User must have CUSTOMER role to be assigned to a lot");
            }

            return customer;
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid customer ID format: " + customerId);
        }
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

        // Map assigned customer
        if (lot.getAssignedCustomer() != null) {
            dto.setAssignedCustomerId(lot.getAssignedCustomer().getUserIdentifier().getUserId().toString());
            dto.setAssignedCustomerName(lot.getAssignedCustomer().getFirstName() + " " + lot.getAssignedCustomer().getLastName());
        }

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
        // Price is optional - only validate if provided
        if (lotRequestModel.getPrice() != null && lotRequestModel.getPrice() < 0) {
            throw new InvalidInputException("Price cannot be negative");
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

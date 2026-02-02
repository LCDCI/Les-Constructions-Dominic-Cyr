package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Quote;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Quote.Quote;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Quote.QuoteRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.QuoteMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Quote.QuoteRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Quote.QuoteResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidProjectDataException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final ProjectRepository projectRepository;
    private final LotRepository lotRepository;
    private final UsersRepository usersRepository;
    private final QuoteNumberGenerator quoteNumberGenerator;
    private final QuoteMapper quoteMapper;

    /**
     * Create a new quote for a project.
     * 
     * @param requestModel Quote data with line items (quote number NOT provided by frontend)
     * @param contractorId The contractor creating the quote
     * @return The created quote with system-generated quote number
     * @throws NotFoundException if project does not exist
     * @throws InvalidProjectDataException if validation fails
     */
    @Transactional
    public QuoteResponseModel createQuote(QuoteRequestModel requestModel, String contractorId) {
        log.info("Creating quote for project: {} by contractor: {}", 
            requestModel.getProjectIdentifier(), contractorId);

        // Validate project exists
        var project = projectRepository.findByProjectIdentifier(requestModel.getProjectIdentifier())
            .orElseThrow(() -> new NotFoundException(
                "Project not found: " + requestModel.getProjectIdentifier()
            ));

        // Validate lot exists and belongs to the project
        if (requestModel.getLotIdentifier() == null || requestModel.getLotIdentifier().isBlank()) {
            throw new InvalidProjectDataException("Lot identifier is required to create a quote");
        }

        UUID lotId;
        try {
            lotId = UUID.fromString(requestModel.getLotIdentifier());
        } catch (IllegalArgumentException e) {
            throw new InvalidProjectDataException("Invalid lot identifier format: " + requestModel.getLotIdentifier());
        }

        var lot = lotRepository.findByLotIdentifier_LotId(lotId);
        if (lot == null) {
            throw new NotFoundException("Lot not found: " + requestModel.getLotIdentifier());
        }

        if (lot.getProject() == null || !project.getProjectIdentifier().equals(lot.getProject().getProjectIdentifier())) {
            throw new InvalidProjectDataException("Lot does not belong to the specified project");
        }

        Users contractorUser = usersRepository.findByAuth0UserId(contractorId)
            .or(() -> usersRepository.findByUserIdentifier(contractorId))
            .orElseThrow(() -> new InvalidProjectDataException("Contractor user not found"));

        boolean isAssignedToLot = lot.getAssignedUsers().stream()
            .anyMatch(user -> user.getUserIdentifier().equals(contractorUser.getUserIdentifier()));

        if (!isAssignedToLot) {
            throw new InvalidProjectDataException(
                "Contractor is not assigned to this lot"
            );
        }

        // Validate line items
        validateLineItems(requestModel);

        // Generate next sequential quote number (atomic, no duplicates)
        String quoteNumber = quoteNumberGenerator.generateNextQuoteNumber();

        // Map request to entity (with generated quote number)
        Quote quote = quoteMapper.requestModelToEntity(requestModel, quoteNumber, contractorId);

        // Save quote to database
        Quote savedQuote = quoteRepository.save(quote);
        log.info("Quote created with number: {}", quoteNumber);

        return quoteMapper.entityToResponseModel(savedQuote);
    }

    /**
     * Get all quotes for a specific project.
     * Accessible to: Owner, Salesperson, Customer (who owns the project), assigned Contractors.
     */
    @Transactional(readOnly = true)
    public List<QuoteResponseModel> getQuotesByProject(String projectIdentifier) {
        log.info("Fetching quotes for project: {}", projectIdentifier);

        // Validate project exists
        projectRepository.findByProjectIdentifier(projectIdentifier)
            .orElseThrow(() -> new NotFoundException("Project not found"));

        List<Quote> quotes = quoteRepository.findByProjectIdentifier(projectIdentifier);
        return quotes.stream()
            .map(quoteMapper::entityToResponseModel)
            .collect(Collectors.toList());
    }

    /**
     * Get all quotes for a specific lot.
     * Accessible to: Owner, Salesperson, Customer (who owns the lot), assigned Contractors.
     */
    @Transactional(readOnly = true)
    public List<QuoteResponseModel> getQuotesByLot(String lotIdentifier) {
        log.info("Fetching quotes for lot: {}", lotIdentifier);

        List<Quote> quotes = quoteRepository.findByLotIdentifier(lotIdentifier);
        return quotes.stream()
            .map(quoteMapper::entityToResponseModel)
            .collect(Collectors.toList());
    }

    /**
     * Get a specific quote by its quote number.
     */
    @Transactional(readOnly = true)
    public QuoteResponseModel getQuoteByNumber(String quoteNumber) {
        log.info("Fetching quote: {}", quoteNumber);

        Quote quote = quoteRepository.findByQuoteNumber(quoteNumber)
            .orElseThrow(() -> new NotFoundException("Quote not found: " + quoteNumber));

        return quoteMapper.entityToResponseModel(quote);
    }

    /**
     * Get all quotes created by a specific contractor.
     */
    @Transactional(readOnly = true)
    public List<QuoteResponseModel> getQuotesByContractor(String contractorId) {
        log.info("Fetching quotes for contractor: {}", contractorId);

        List<Quote> quotes = quoteRepository.findByContractorId(contractorId);
        return quotes.stream()
            .map(quoteMapper::entityToResponseModel)
            .collect(Collectors.toList());
    }

    /**
     * Validate line items before creating quote.
     * 
     * Rules:
     * - At least 1 line item required
     * - All quantities must be > 0
     * - All rates must be >= 0
     */
    private void validateLineItems(QuoteRequestModel requestModel) {
        if (requestModel.getLineItems() == null || requestModel.getLineItems().isEmpty()) {
            throw new InvalidProjectDataException("At least one line item is required");
        }

        requestModel.getLineItems().forEach(item -> {
            if (item.getQuantity() == null || item.getQuantity().signum() <= 0) {
                throw new InvalidProjectDataException(
                    "Quantity must be greater than 0 for: " + item.getItemDescription()
                );
            }

            if (item.getRate() == null || item.getRate().signum() < 0) {
                throw new InvalidProjectDataException(
                    "Rate cannot be negative for: " + item.getItemDescription()
                );
            }

            if (item.getItemDescription() == null || item.getItemDescription().isBlank()) {
                throw new InvalidProjectDataException("Item description cannot be empty");
            }

            if (item.getDisplayOrder() == null || item.getDisplayOrder() < 0) {
                throw new InvalidProjectDataException("Display order must be >= 0");
            }
        });
    }
}

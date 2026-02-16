package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Quote;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Quote.Quote;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Quote.QuoteRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.NotificationService;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.NotificationCategory;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
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

import java.util.ArrayList;
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
    private final NotificationService notificationService;

    /**
     * Create a new quote for a project.
     * 
     * @param requestModel Quote data with line items (quote number NOT provided by
     *                     frontend)
     * @param contractorId The contractor creating the quote
     * @return The created quote with system-generated quote number
     * @throws NotFoundException           if project does not exist
     * @throws InvalidProjectDataException if validation fails
     */
    @Transactional
    public QuoteResponseModel createQuote(QuoteRequestModel requestModel, String contractorId) {
        log.info("Creating quote for project: {} by contractor: {}",
                requestModel.getProjectIdentifier(), contractorId);

        // Validate project exists
        var project = projectRepository.findByProjectIdentifier(requestModel.getProjectIdentifier())
                .orElseThrow(() -> new NotFoundException(
                        "Project not found: " + requestModel.getProjectIdentifier()));

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

        if (lot.getProject() == null
                || !project.getProjectIdentifier().equals(lot.getProject().getProjectIdentifier())) {
            throw new InvalidProjectDataException("Lot does not belong to the specified project");
        }

        Users contractorUser = usersRepository.findByAuth0UserId(contractorId)
                .or(() -> usersRepository.findByUserIdentifier(contractorId))
                .orElseThrow(() -> new InvalidProjectDataException("Contractor user not found"));

        boolean isAssignedToLot = lot.getAssignedUsers().stream()
                .anyMatch(user -> user.getUserIdentifier().equals(contractorUser.getUserIdentifier()));

        if (!isAssignedToLot) {
            throw new InvalidProjectDataException(
                    "Contractor is not assigned to this lot");
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
     * Accessible to: Owner, Salesperson, Customer (who owns the project), assigned
     * Contractors.
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
     * Accessible to: Owner, Salesperson, Customer (who owns the lot), assigned
     * Contractors.
     */
    @Transactional(readOnly = true)
    public List<QuoteResponseModel> getQuotesByLot(String lotIdentifier) {
        log.info("Fetching quotes for lot: {}", lotIdentifier);

        UUID lotId = UUID.fromString(lotIdentifier);
        List<Quote> quotes = quoteRepository.findByLotIdentifier(lotId);
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
     * Get all submitted (pending approval) quotes for owner view.
     */
    @Transactional(readOnly = true)
    public List<QuoteResponseModel> getSubmittedQuotes() {
        log.info("Fetching all submitted quotes");

        List<Quote> quotes = quoteRepository.findByStatus("SUBMITTED");
        return quotes.stream()
                .map(quoteMapper::entityToResponseModel)
                .collect(Collectors.toList());
    }

    /**
     * Get all quotes for owner (admin view), sorted by creation date.
     */
    @Transactional(readOnly = true)
    public List<QuoteResponseModel> getAllQuotes() {
        log.info("Fetching all quotes for owner");

        List<Quote> quotes = quoteRepository.findAllByOrderByCreatedAtDesc();
        return quotes.stream()
                .map(quoteMapper::entityToResponseModel)
                .collect(Collectors.toList());
    }

    /**
     * Get submitted quotes filtered by project.
     */
    @Transactional(readOnly = true)
    public List<QuoteResponseModel> getSubmittedQuotesByProject(String projectIdentifier) {
        log.info("Fetching submitted quotes for project: {}", projectIdentifier);

        List<Quote> quotes = quoteRepository.findByProjectAndStatus(projectIdentifier, "SUBMITTED");
        return quotes.stream()
                .map(quoteMapper::entityToResponseModel)
                .collect(Collectors.toList());
    }

    /**
     * Approve a quote.
     * 
     * @param quoteNumber The quote to approve
     * @param ownerId     The owner approving the quote
     * @return The updated quote
     * @throws NotFoundException           if quote not found
     * @throws InvalidProjectDataException if quote is not in SUBMITTED status
     */
    @Transactional
    public QuoteResponseModel approveQuote(String quoteNumber, String ownerId) {
        log.info("Approving quote: {} by owner: {}", quoteNumber, ownerId);

        Quote quote = quoteRepository.findByQuoteNumber(quoteNumber)
                .orElseThrow(() -> new NotFoundException("Quote not found: " + quoteNumber));

        if (!"SUBMITTED".equals(quote.getStatus())) {
            throw new InvalidProjectDataException("Quote is not in SUBMITTED status: " + quote.getStatus());
        }

        quote.setStatus("OWNER_APPROVED");
        quote.setApprovedBy(ownerId);
        quote.setApprovedAt(java.time.LocalDateTime.now());
        quote.setRejectionReason(null); // Clear any rejection reason

        Quote savedQuote = quoteRepository.save(quote);
        log.info("Quote approved: {}", quoteNumber);

        notifyCustomersOfOwnerApprovedQuote(savedQuote);

        return quoteMapper.entityToResponseModel(savedQuote);
    }

    /**
     * Notify customers assigned to the quote's lot (or the project's customer) that the owner has approved the quote.
     * Notification lists quote number and project; link goes to customer quote approval page.
     */
    private void notifyCustomersOfOwnerApprovedQuote(Quote quote) {
        List<Users> customers = new ArrayList<>();

        // 1) Customers assigned to the quote's lot (eagerly load assignedUsers to avoid lazy-load issues)
        if (quote.getLotIdentifier() != null) {
            Lot lot = lotRepository.findByLotIdentifier_LotIdWithUsers(quote.getLotIdentifier());
            if (lot != null) {
                List<Users> lotCustomers = lot.getAssignedUsers().stream()
                        .filter(user -> user.getUserRole() == UserRole.CUSTOMER)
                        .collect(Collectors.toList());
                customers.addAll(lotCustomers);
            }
        }

        // 2) Fallback: if no customers on the lot, notify the project's assigned customer
        if (customers.isEmpty() && quote.getProjectIdentifier() != null) {
            projectRepository.findByProjectIdentifier(quote.getProjectIdentifier())
                    .filter(project -> project.getCustomerId() != null && !project.getCustomerId().isBlank())
                    .flatMap(project -> usersRepository.findByUserIdentifier(project.getCustomerId()))
                    .filter(user -> user.getUserRole() == UserRole.CUSTOMER)
                    .ifPresent(customers::add);
        }

        if (customers.isEmpty()) {
            log.info("No customer to notify for quote {} (lot: {}, project: {}); ensure the lot has a CUSTOMER in lot_assigned_users or the project has customerId set.",
                    quote.getQuoteNumber(), quote.getLotIdentifier(), quote.getProjectIdentifier());
            return;
        }

        String projectLabel = projectRepository.findByProjectIdentifier(quote.getProjectIdentifier())
                .map(p -> p.getProjectName())
                .orElse(quote.getProjectIdentifier());
        String title = "Quote approved: " + quote.getQuoteNumber();
        String message = String.format("Quote %s for project %s has been approved by the owner. You can review and approve it from your dashboard.",
                quote.getQuoteNumber(), projectLabel);
        String link = "/customer/quotes/approval";

        for (Users customer : customers) {
            try {
                notificationService.createNotification(
                        customer.getUserIdentifier().getUserId(),
                        title,
                        message,
                        NotificationCategory.QUOTE_APPROVED,
                        link);
                log.info("Owner-approved quote notification created for customer: {}", customer.getPrimaryEmail());
            } catch (Exception e) {
                log.error("Failed to create quote-approved notification for customer {}: {}",
                        customer.getUserIdentifier().getUserId(), e.getMessage());
            }
        }
    }

    /**
     * Reject a quote with a reason.
     * 
     * @param quoteNumber     The quote to reject
     * @param rejectionReason The reason for rejection
     * @param ownerId         The owner rejecting the quote
     * @return The updated quote
     * @throws NotFoundException           if quote not found
     * @throws InvalidProjectDataException if quote is not in SUBMITTED status or
     *                                     reason is empty
     */
    @Transactional
    public QuoteResponseModel rejectQuote(String quoteNumber, String rejectionReason, String ownerId) {
        log.info("Rejecting quote: {} by owner: {}", quoteNumber, ownerId);

        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new InvalidProjectDataException("Rejection reason is required");
        }

        Quote quote = quoteRepository.findByQuoteNumber(quoteNumber)
                .orElseThrow(() -> new NotFoundException("Quote not found: " + quoteNumber));

        if (!"SUBMITTED".equals(quote.getStatus())) {
            throw new InvalidProjectDataException("Quote is not in SUBMITTED status: " + quote.getStatus());
        }

        quote.setStatus("REJECTED");
        quote.setRejectionReason(rejectionReason);
        quote.setApprovedBy(ownerId);
        quote.setApprovedAt(java.time.LocalDateTime.now());

        Quote savedQuote = quoteRepository.save(quote);
        log.info("Quote rejected: {}", quoteNumber);

        return quoteMapper.entityToResponseModel(savedQuote);
    }

    /**
     * Customer approves a quote.
     * Only customers who own the lot can approve.
     * 
     * @param quoteNumber     The quote to approve
     * @param customerAuth0Id The customer's Auth0 ID
     * @return The updated quote
     * @throws NotFoundException           if quote not found
     * @throws InvalidProjectDataException if quote is not in OWNER_APPROVED status
     * @throws SecurityException           if customer doesn't own the lot
     */
    @Transactional
    public QuoteResponseModel customerApproveQuote(String quoteNumber, String customerAuth0Id) {
        log.info("Customer approving quote: {} by customer: {}", quoteNumber, customerAuth0Id);

        Quote quote = quoteRepository.findByQuoteNumber(quoteNumber)
                .orElseThrow(() -> new NotFoundException("Quote not found: " + quoteNumber));

        if (!"OWNER_APPROVED".equals(quote.getStatus())) {
            throw new InvalidProjectDataException(
                    "Quote must be owner-approved before customer can approve. Current status: " + quote.getStatus());
        }

        Users customer = usersRepository.findByAuth0UserId(customerAuth0Id)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        String customerUserIdentifier = customer.getUserIdentifier().getUserId().toString();

        // Customer may approve if assigned to the quote's lot OR is the project's assigned customer
        if (!customerCanSeePendingQuote(quote, customerAuth0Id, customerUserIdentifier)) {
            throw new SecurityException("Customer is not assigned to this quote's lot or project");
        }

        quote.setStatus("CUSTOMER_APPROVED");
        quote.setCustomerApprovedBy(customerAuth0Id);
        quote.setCustomerApprovedAt(java.time.LocalDateTime.now());
        quote.setCustomerAcknowledged(true);

        Quote savedQuote = quoteRepository.save(quote);
        log.info("Quote customer-approved: {}", quoteNumber);

        return quoteMapper.entityToResponseModel(savedQuote);
    }

    /**
     * Get quotes pending customer approval for a specific customer.
     * Returns quotes that are OWNER_APPROVED and either:
     * - on a lot assigned to this customer, or
     * - on a project where this customer is the assigned project customer.
     */
    @Transactional(readOnly = true)
    public List<QuoteResponseModel> getCustomerPendingQuotes(String customerAuth0Id) {
        log.info("Fetching pending quotes for customer: {}", customerAuth0Id);

        Users customer = usersRepository.findByAuth0UserId(customerAuth0Id)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        String customerUserIdentifier = customer.getUserIdentifier().getUserId().toString();

        // Get all OWNER_APPROVED quotes
        List<Quote> ownerApprovedQuotes = quoteRepository.findByStatus("OWNER_APPROVED");

        // Include quote if customer is on the quote's lot OR is the project's assigned customer
        return ownerApprovedQuotes.stream()
                .filter(quote -> customerCanSeePendingQuote(quote, customerAuth0Id, customerUserIdentifier))
                .map(quoteMapper::entityToResponseModel)
                .collect(Collectors.toList());
    }

    /**
     * True if this customer is allowed to see and approve the OWNER_APPROVED quote
     * (assigned to the quote's lot, or assigned as the project's customer).
     */
    private boolean customerCanSeePendingQuote(Quote quote, String customerAuth0Id, String customerUserIdentifier) {
        // 1) Customer is assigned to the quote's lot
        if (quote.getLotIdentifier() != null) {
            Lot lot = lotRepository.findByLotIdentifier_LotId(quote.getLotIdentifier());
            if (lot != null && lot.getAssignedUsers().stream()
                    .anyMatch(u -> u.getAuth0UserId().equals(customerAuth0Id))) {
                return true;
            }
        }
        // 2) Customer is the project's assigned customer (same logic as notification fallback)
        if (quote.getProjectIdentifier() != null) {
            return projectRepository.findByProjectIdentifier(quote.getProjectIdentifier())
                    .filter(project -> project.getCustomerId() != null && project.getCustomerId().equals(customerUserIdentifier))
                    .isPresent();
        }
        return false;
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
                        "Quantity must be greater than 0 for: " + item.getItemDescription());
            }

            if (item.getRate() == null || item.getRate().signum() < 0) {
                throw new InvalidProjectDataException(
                        "Rate cannot be negative for: " + item.getItemDescription());
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

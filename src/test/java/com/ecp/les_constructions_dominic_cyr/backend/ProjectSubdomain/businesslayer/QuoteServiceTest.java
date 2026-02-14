package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Quote.QuoteNumberGenerator;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Quote.QuoteService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Quote.Quote;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Quote.QuoteRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.QuoteMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Quote.QuoteRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Quote.QuoteResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidProjectDataException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private LotRepository lotRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private QuoteNumberGenerator quoteNumberGenerator;

    @Mock
    private QuoteMapper quoteMapper;

    @InjectMocks
    private QuoteService quoteService;

    private String projectIdentifier;
    private String contractorId;
    private String lotIdentifier;
    private QuoteRequestModel validRequestModel;
    private Project project;
    private Lot lot;
    private Users contractorUser;

    @BeforeEach
    void setUp() {
        projectIdentifier = "proj-001";
        contractorId = "auth0|contractor-001";
        lotIdentifier = UUID.randomUUID().toString();
        project = new Project();
        project.setProjectIdentifier(projectIdentifier);
        lot = new Lot();
        lot.setLotIdentifier(new LotIdentifier(lotIdentifier));
        lot.setProject(project);
        UserIdentifier contractorUserIdentifier = UserIdentifier.newId();
        contractorUser = new Users();
        contractorUser.setUserIdentifier(contractorUserIdentifier);
        lot.setAssignedUsers(List.of(contractorUser));

        validRequestModel = QuoteRequestModel.builder()
            .projectIdentifier(projectIdentifier)
            .lotIdentifier(lotIdentifier)
            .lineItems(List.of(
                QuoteRequestModel.QuoteLineItemRequestModel.builder()
                    .itemDescription("Service 1")
                    .quantity(new BigDecimal("10"))
                    .rate(new BigDecimal("100.00"))
                    .displayOrder(1)
                    .build()
            ))
            .build();
    }

    /**
     * POSITIVE TEST: Successfully create a quote with valid data.
     */
    @Test
    void createQuote_WithValidData_SuccessfullyCreatesQuote() {
        Quote mockQuote = Quote.builder()
            .quoteNumber("QT-0000001")
            .projectIdentifier(projectIdentifier)
            .contractorId(contractorId)
            .totalAmount(new BigDecimal("1000.00"))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        QuoteResponseModel mockResponseModel = QuoteResponseModel.builder()
            .quoteNumber("QT-0000001")
            .projectIdentifier(projectIdentifier)
            .contractorId(contractorId)
            .totalAmount(new BigDecimal("1000.00"))
            .build();

        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.of(project));
        when(quoteNumberGenerator.generateNextQuoteNumber()).thenReturn("QT-0000001");
        when(quoteMapper.requestModelToEntity(validRequestModel, "QT-0000001", contractorId))
            .thenReturn(mockQuote);
        when(quoteRepository.save(any(Quote.class))).thenReturn(mockQuote);
        when(quoteMapper.entityToResponseModel(mockQuote)).thenReturn(mockResponseModel);

        QuoteResponseModel result = quoteService.createQuote(validRequestModel, contractorId);

        assertNotNull(result);
        assertEquals("QT-0000001", result.getQuoteNumber());
        assertEquals(projectIdentifier, result.getProjectIdentifier());
        assertEquals(contractorId, result.getContractorId());
        verify(quoteRepository, times(1)).save(any(Quote.class));
    }

    /**
     * NEGATIVE TEST: Fail when project does not exist.
     */
    @Test
    void createQuote_WhenProjectNotFound_ThrowsNotFoundException() {
        // Given: Project does not exist
        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.empty());

        // When/Then: Should throw NotFoundException
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> quoteService.createQuote(validRequestModel, contractorId)
        );

        assertTrue(exception.getMessage().contains("Project not found"));
        verify(quoteRepository, never()).save(any());
    }

    /**
     * NEGATIVE TEST: Fail when contractor is not assigned to lot.
     */
    @Test
    void createQuote_WhenContractorNotAssigned_ThrowsException() {
        Lot lotNoContractor = new Lot();
        lotNoContractor.setLotIdentifier(lot.getLotIdentifier());
        lotNoContractor.setProject(project);
        lotNoContractor.setAssignedUsers(new ArrayList<>()); // Contractor not in list

        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.of(project));
        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(lotIdentifier))).thenReturn(lotNoContractor);
        when(usersRepository.findByAuth0UserId(contractorId)).thenReturn(Optional.of(contractorUser));

        InvalidProjectDataException exception = assertThrows(
            InvalidProjectDataException.class,
            () -> quoteService.createQuote(validRequestModel, contractorId)
        );

        assertTrue(exception.getMessage().contains("not assigned to this lot"));
        verify(quoteRepository, never()).save(any());
    }

    /**
     * NEGATIVE TEST: Fail when line item quantity is zero or negative.
     */
    @Test
    void createQuote_WithInvalidQuantity_ThrowsException() {
        QuoteRequestModel invalidRequestModel = QuoteRequestModel.builder()
            .projectIdentifier(projectIdentifier)
            .lotIdentifier(lotIdentifier)
            .lineItems(List.of(
                QuoteRequestModel.QuoteLineItemRequestModel.builder()
                    .itemDescription("Invalid Item")
                    .quantity(new BigDecimal("0")) // Invalid: must be > 0
                    .rate(new BigDecimal("100.00"))
                    .displayOrder(1)
                    .build()
            ))
            .build();

        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.of(project));
        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(lotIdentifier))).thenReturn(lot);
        when(usersRepository.findByAuth0UserId(contractorId)).thenReturn(Optional.of(contractorUser));

        InvalidProjectDataException exception = assertThrows(
            InvalidProjectDataException.class,
            () -> quoteService.createQuote(invalidRequestModel, contractorId)
        );

        assertTrue(exception.getMessage().contains("Quantity must be greater than 0"));
        verify(quoteRepository, never()).save(any());
    }

    @Test
    void createQuote_WhenLotIdentifierBlank_ThrowsInvalidProjectDataException() {
        validRequestModel.setLotIdentifier("");
        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.of(project));

        InvalidProjectDataException exception = assertThrows(
            InvalidProjectDataException.class,
            () -> quoteService.createQuote(validRequestModel, contractorId)
        );
        assertTrue(exception.getMessage().contains("Lot identifier is required"));
    }

    @Test
    void createQuote_WhenLotNotFound_ThrowsNotFoundException() {
        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.of(project));
        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(lotIdentifier))).thenReturn(null);

        assertThrows(NotFoundException.class, () ->
            quoteService.createQuote(validRequestModel, contractorId));
    }

    @Test
    void createQuote_WhenContractorNotFound_ThrowsInvalidProjectDataException() {
        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.of(project));
        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(lotIdentifier))).thenReturn(lot);
        when(usersRepository.findByAuth0UserId(contractorId)).thenReturn(Optional.empty());
        when(usersRepository.findByUserIdentifier(contractorId)).thenReturn(Optional.empty());

        assertThrows(InvalidProjectDataException.class, () ->
            quoteService.createQuote(validRequestModel, contractorId));
    }

    @Test
    void createQuote_WithEmptyLineItems_ThrowsInvalidProjectDataException() {
        validRequestModel.setLineItems(List.of());
        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.of(project));
        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(lotIdentifier))).thenReturn(lot);
        when(usersRepository.findByAuth0UserId(contractorId)).thenReturn(Optional.of(contractorUser));

        InvalidProjectDataException exception = assertThrows(
            InvalidProjectDataException.class,
            () -> quoteService.createQuote(validRequestModel, contractorId)
        );
        assertTrue(exception.getMessage().contains("At least one line item is required"));
    }

    @Test
    void createQuote_WithNegativeRate_ThrowsInvalidProjectDataException() {
        validRequestModel.setLineItems(List.of(
            QuoteRequestModel.QuoteLineItemRequestModel.builder()
                .itemDescription("Item")
                .quantity(new BigDecimal("1"))
                .rate(new BigDecimal("-10"))
                .displayOrder(0)
                .build()
        ));
        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.of(project));
        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(lotIdentifier))).thenReturn(lot);
        when(usersRepository.findByAuth0UserId(contractorId)).thenReturn(Optional.of(contractorUser));

        InvalidProjectDataException exception = assertThrows(
            InvalidProjectDataException.class,
            () -> quoteService.createQuote(validRequestModel, contractorId)
        );
        assertTrue(exception.getMessage().contains("Rate cannot be negative"));
    }

    @Test
    void getQuotesByLot_ReturnsQuotes() {
        Quote quote = Quote.builder().quoteNumber("QT-0000001").lotIdentifier(UUID.fromString(lotIdentifier)).build();
        QuoteResponseModel responseModel = QuoteResponseModel.builder().quoteNumber("QT-0000001").build();
        when(quoteRepository.findByLotIdentifier(UUID.fromString(lotIdentifier))).thenReturn(List.of(quote));
        when(quoteMapper.entityToResponseModel(quote)).thenReturn(responseModel);

        List<QuoteResponseModel> result = quoteService.getQuotesByLot(lotIdentifier);

        assertEquals(1, result.size());
        assertEquals("QT-0000001", result.get(0).getQuoteNumber());
    }

    @Test
    void getQuoteByNumber_ReturnsQuote() {
        Quote quote = Quote.builder().quoteNumber("QT-0000001").build();
        QuoteResponseModel responseModel = QuoteResponseModel.builder().quoteNumber("QT-0000001").build();
        when(quoteRepository.findByQuoteNumber("QT-0000001")).thenReturn(Optional.of(quote));
        when(quoteMapper.entityToResponseModel(quote)).thenReturn(responseModel);

        QuoteResponseModel result = quoteService.getQuoteByNumber("QT-0000001");

        assertEquals("QT-0000001", result.getQuoteNumber());
    }

    @Test
    void getQuoteByNumber_NotFound_ThrowsNotFoundException() {
        when(quoteRepository.findByQuoteNumber("QT-9999999")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> quoteService.getQuoteByNumber("QT-9999999"));
    }

    @Test
    void getQuotesByContractor_ReturnsQuotes() {
        Quote quote = Quote.builder().quoteNumber("QT-0000001").contractorId(contractorId).build();
        QuoteResponseModel responseModel = QuoteResponseModel.builder().quoteNumber("QT-0000001").build();
        when(quoteRepository.findByContractorId(contractorId)).thenReturn(List.of(quote));
        when(quoteMapper.entityToResponseModel(quote)).thenReturn(responseModel);

        List<QuoteResponseModel> result = quoteService.getQuotesByContractor(contractorId);

        assertEquals(1, result.size());
        assertEquals("QT-0000001", result.get(0).getQuoteNumber());
    }

    @Test
    void getQuotesByProject_WithValidProject_ReturnsQuotes() {
        Quote quote1 = new Quote();
        quote1.setQuoteNumber("QT-0000001");

        QuoteResponseModel responseModel1 = QuoteResponseModel.builder()
            .quoteNumber("QT-0000001")
            .build();

        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.of(project));
        when(quoteRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(List.of(quote1));
        when(quoteMapper.entityToResponseModel(quote1)).thenReturn(responseModel1);

        List<QuoteResponseModel> results = quoteService.getQuotesByProject(projectIdentifier);

        assertEquals(1, results.size());
        assertEquals("QT-0000001", results.get(0).getQuoteNumber());
        verify(quoteRepository, times(1)).findByProjectIdentifier(projectIdentifier);
    }

    /**
     * NEGATIVE TEST: Fail when project does not exist for retrieval.
     */
    @Test
    void getQuotesByProject_WhenProjectNotFound_ThrowsNotFoundException() {
        // Given: Project does not exist
        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.empty());

        // When/Then: Should throw NotFoundException
        assertThrows(
            NotFoundException.class,
            () -> quoteService.getQuotesByProject(projectIdentifier)
        );

        verify(quoteRepository, never()).findByProjectIdentifier(any());
    }
}

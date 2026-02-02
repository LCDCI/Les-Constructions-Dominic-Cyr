package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Quote;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Quote.Quote;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Quote.QuoteRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.QuoteMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Quote.QuoteRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Quote.QuoteResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidProjectDataException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private QuoteNumberGenerator quoteNumberGenerator;

    @Mock
    private QuoteMapper quoteMapper;

    @InjectMocks
    private QuoteService quoteService;

    private String projectIdentifier;
    private String contractorId;
    private QuoteRequestModel validRequestModel;

    @BeforeEach
    void setUp() {
        projectIdentifier = "proj-001";
        contractorId = "contractor-001";

        // Create a valid request model
        validRequestModel = QuoteRequestModel.builder()
            .projectIdentifier(projectIdentifier)
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
        // Given: Project exists and contractor is assigned
        Project project = new Project();
        project.setProjectIdentifier(projectIdentifier);
        project.setContractorIds(List.of(contractorId));

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

        // When: Create quote
        QuoteResponseModel result = quoteService.createQuote(validRequestModel, contractorId);

        // Then: Quote should be created successfully
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
     * NEGATIVE TEST: Fail when contractor is not assigned to project.
     */
    @Test
    void createQuote_WhenContractorNotAssigned_ThrowsException() {
        // Given: Project exists but contractor is not assigned
        Project project = new Project();
        project.setProjectIdentifier(projectIdentifier);
        project.setContractorIds(new ArrayList<>()); // Empty - contractor not assigned

        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.of(project));

        // When/Then: Should throw InvalidProjectDataException
        InvalidProjectDataException exception = assertThrows(
            InvalidProjectDataException.class,
            () -> quoteService.createQuote(validRequestModel, contractorId)
        );

        assertTrue(exception.getMessage().contains("not assigned to this project"));
        verify(quoteRepository, never()).save(any());
    }

    /**
     * NEGATIVE TEST: Fail when line item quantity is zero or negative.
     */
    @Test
    void createQuote_WithInvalidQuantity_ThrowsException() {
        // Given: Project exists, but line item has invalid quantity
        Project project = new Project();
        project.setProjectIdentifier(projectIdentifier);
        project.setContractorIds(List.of(contractorId));

        QuoteRequestModel invalidRequestModel = QuoteRequestModel.builder()
            .projectIdentifier(projectIdentifier)
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

        // When/Then: Should throw InvalidProjectDataException
        InvalidProjectDataException exception = assertThrows(
            InvalidProjectDataException.class,
            () -> quoteService.createQuote(invalidRequestModel, contractorId)
        );

        assertTrue(exception.getMessage().contains("Quantity must be greater than 0"));
        verify(quoteRepository, never()).save(any());
    }

    /**
     * POSITIVE TEST: Successfully retrieve all quotes for a project.
     */
    @Test
    void getQuotesByProject_WithValidProject_ReturnsQuotes() {
        // Given: Project exists with quotes
        Project project = new Project();
        project.setProjectIdentifier(projectIdentifier);

        Quote quote1 = new Quote();
        quote1.setQuoteNumber("QT-0000001");

        QuoteResponseModel responseModel1 = QuoteResponseModel.builder()
            .quoteNumber("QT-0000001")
            .build();

        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.of(project));
        when(quoteRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(List.of(quote1));
        when(quoteMapper.entityToResponseModel(quote1)).thenReturn(responseModel1);

        // When: Get quotes
        List<QuoteResponseModel> results = quoteService.getQuotesByProject(projectIdentifier);

        // Then: Should return quotes
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

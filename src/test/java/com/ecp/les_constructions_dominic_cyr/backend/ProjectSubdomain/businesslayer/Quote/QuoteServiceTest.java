package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Quote;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Quote.Quote;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Quote.QuoteRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.QuoteMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Quote.QuoteResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidProjectDataException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private QuoteMapper quoteMapper;

    @InjectMocks
    private QuoteService quoteService;

    private Quote quote;
    private final String QUOTE_NUMBER = "QT-0000001";
    private final String OWNER_ID = "auth0|owner123";

    @BeforeEach
    void setUp() {
        quote = Quote.builder()
                .quoteNumber(QUOTE_NUMBER)
                .status("SUBMITTED")
                .totalAmount(java.math.BigDecimal.TEN)
                .build();
    }

    @Test
    void approveQuote_Success() {
        // Arrange
        when(quoteRepository.findByQuoteNumber(QUOTE_NUMBER)).thenReturn(Optional.of(quote));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(quoteMapper.entityToResponseModel(any(Quote.class))).thenReturn(QuoteResponseModel.builder()
                .quoteNumber(QUOTE_NUMBER)
                .status("APPROVED")
                .build());

        // Act
        QuoteResponseModel result = quoteService.approveQuote(QUOTE_NUMBER, OWNER_ID);

        // Assert
        assertEquals("APPROVED", result.getStatus());
        verify(quoteRepository).save(argThat(q -> 
            q.getStatus().equals("APPROVED") && 
            q.getApprovedBy().equals(OWNER_ID) &&
            q.getApprovedAt() != null
        ));
    }

    @Test
    void rejectQuote_Success() {
        // Arrange
        String reason = "Too expensive";
        when(quoteRepository.findByQuoteNumber(QUOTE_NUMBER)).thenReturn(Optional.of(quote));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(quoteMapper.entityToResponseModel(any(Quote.class))).thenReturn(QuoteResponseModel.builder()
                .quoteNumber(QUOTE_NUMBER)
                .status("REJECTED")
                .build());

        // Act
        QuoteResponseModel result = quoteService.rejectQuote(QUOTE_NUMBER, reason, OWNER_ID);

        // Assert
        assertEquals("REJECTED", result.getStatus());
        verify(quoteRepository).save(argThat(q -> q.getStatus().equals("REJECTED") &&
                q.getRejectionReason().equals(reason) &&
                q.getApprovedBy().equals(OWNER_ID)));
    }

    @Test
    void rejectQuote_Failure_NoReason() {
        // Act & Assert
        assertThrows(InvalidProjectDataException.class, () -> quoteService.rejectQuote(QUOTE_NUMBER, "", OWNER_ID));
        verify(quoteRepository, never()).save(any());
    }

    @Test
    void approveQuote_Failure_NotSubmitted() {
        // Arrange
        quote.setStatus("APPROVED");
        when(quoteRepository.findByQuoteNumber(QUOTE_NUMBER)).thenReturn(Optional.of(quote));

        // Act & Assert
        assertThrows(InvalidProjectDataException.class, () -> quoteService.approveQuote(QUOTE_NUMBER, OWNER_ID));
        verify(quoteRepository, never()).save(any());
    }

    @Test
    void approveQuote_Failure_NotFound() {
        // Arrange
        when(quoteRepository.findByQuoteNumber(QUOTE_NUMBER)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> 
            quoteService.approveQuote(QUOTE_NUMBER, OWNER_ID)
        );
    }
}

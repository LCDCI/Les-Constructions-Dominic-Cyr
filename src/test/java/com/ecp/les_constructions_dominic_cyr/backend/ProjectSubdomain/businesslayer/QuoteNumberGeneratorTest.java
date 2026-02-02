package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Quote;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Quote.QuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteNumberGeneratorTest {

    @Mock
    private QuoteRepository quoteRepository;

    @InjectMocks
    private QuoteNumberGenerator quoteNumberGenerator;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(quoteRepository);
    }

    /**
     * POSITIVE TEST: Generate first quote number when no quotes exist.
     */
    @Test
    void generateNextQuoteNumber_WhenNoQuotesExist_ReturnsFirstNumber() {
        // Given: No quotes exist in the repository
        when(quoteRepository.findMaxQuoteSequence()).thenReturn(Optional.empty());

        // When: Generate next quote number
        String quoteNumber = quoteNumberGenerator.generateNextQuoteNumber();

        // Then: Should return QT-0000001 (first quote)
        assertEquals("QT-0000001", quoteNumber);
        verify(quoteRepository, times(1)).findMaxQuoteSequence();
    }

    /**
     * NEGATIVE TEST: Handle case when max sequence exceeds limit.
     */
    @Test
    void generateNextQuoteNumber_WhenMaxSequenceReached_ThrowsException() {
        // Given: Max sequence is at the limit (9999999)
        when(quoteRepository.findMaxQuoteSequence()).thenReturn(Optional.of(9999999));

        // When/Then: Should throw IllegalStateException
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> quoteNumberGenerator.generateNextQuoteNumber()
        );

        assertTrue(exception.getMessage().contains("exceeded maximum value"));
        verify(quoteRepository, times(1)).findMaxQuoteSequence();
    }

    /**
     * POSITIVE TEST: Generate sequential quote numbers correctly.
     */
    @Test
    void generateNextQuoteNumber_WithExistingQuotes_ReturnsNextSequential() {
        // Given: Quote with number QT-0000005 exists
        when(quoteRepository.findMaxQuoteSequence()).thenReturn(Optional.of(5));

        // When: Generate next quote number
        String quoteNumber = quoteNumberGenerator.generateNextQuoteNumber();

        // Then: Should return QT-0000006
        assertEquals("QT-0000006", quoteNumber);
        verify(quoteRepository, times(1)).findMaxQuoteSequence();
    }

    /**
     * POSITIVE TEST: Correct zero-padding of sequential numbers.
     */
    @Test
    void generateNextQuoteNumber_VerifyZeroPadding_FormatsCorrectly() {
        // Given: Multiple scenarios of existing sequences
        testZeroPaddingForSequence(99, "QT-0000100");
        testZeroPaddingForSequence(999, "QT-0001000");
        testZeroPaddingForSequence(1000, "QT-0001001");
    }

    private void testZeroPaddingForSequence(int existingSequence, String expectedNextNumber) {
        reset(quoteRepository);
        when(quoteRepository.findMaxQuoteSequence()).thenReturn(Optional.of(existingSequence));

        String quoteNumber = quoteNumberGenerator.generateNextQuoteNumber();

        assertEquals(expectedNextNumber, quoteNumber);
        verify(quoteRepository, times(1)).findMaxQuoteSequence();
    }
}

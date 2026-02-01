package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Quote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

    /**
     * Find a quote by its sequential quote number (QT-XXXXXXX format).
     */
    Optional<Quote> findByQuoteNumber(String quoteNumber);

    /**
     * Find all quotes for a specific project.
     */
    List<Quote> findByProjectIdentifier(String projectIdentifier);

    /**
     * Find all quotes created by a specific contractor.
     */
    List<Quote> findByContractorId(String contractorId);

    /**
     * Find quotes by both project and contractor.
     */
    List<Quote> findByProjectIdentifierAndContractorId(String projectIdentifier, String contractorId);

    /**
     * Get the highest numeric part from existing quote numbers.
     * Assumes quote_number format: QT-XXXXXXX (7 digits).
     * Returns the highest number value (e.g., 5 if "QT-0000005" exists).
     * Returns NULL if no quotes exist (new series).
     * 
     * This is used for atomic sequential ID generation with database-level locking.
     */
    @Query(value = "SELECT MAX(CAST(SUBSTRING(quote_number, 4) AS INTEGER)) FROM quotes", nativeQuery = true)
    Optional<Integer> findMaxQuoteSequence();
}

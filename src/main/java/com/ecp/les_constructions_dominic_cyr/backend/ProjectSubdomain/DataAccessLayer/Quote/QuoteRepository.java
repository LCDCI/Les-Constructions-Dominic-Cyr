package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Quote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

    Optional<Quote> findByQuoteNumber(String quoteNumber);

    List<Quote> findByProjectIdentifier(String projectIdentifier);

    /**
     * Find all quotes for a specific lot.
     */
    List<Quote> findByLotIdentifier(UUID lotIdentifier);

    List<Quote> findByContractorId(String contractorId);

    List<Quote> findByProjectIdentifierAndContractorId(String projectIdentifier, String contractorId);

    @Query(value = "SELECT MAX(CAST(SUBSTRING(quote_number, 4) AS INTEGER)) FROM quotes", nativeQuery = true)
    Optional<Integer> findMaxQuoteSequence();

    List<Quote> findByStatus(String status);

    List<Quote> findByProjectIdentifierAndStatus(String projectIdentifier, String status);

    List<Quote> findByLotIdentifierAndStatus(String lotIdentifier, String status);

    @Query("SELECT q FROM Quote q WHERE q.projectIdentifier = :projectIdentifier AND q.status = :status ORDER BY q.createdAt DESC")
    List<Quote> findByProjectAndStatus(@Param("projectIdentifier") String projectIdentifier,
            @Param("status") String status);

    @Query("SELECT q FROM Quote q WHERE q.contractorId = :contractorId AND q.status = :status ORDER BY q.createdAt DESC")
    List<Quote> findByContractorAndStatus(@Param("contractorId") String contractorId, @Param("status") String status);
}

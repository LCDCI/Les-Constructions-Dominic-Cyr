package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for managing FormSubmissionHistory entities.
 */
@Repository
public interface FormSubmissionHistoryRepository extends JpaRepository<FormSubmissionHistory, Long> {

    /**
     * Find all submission history for a specific form, ordered by submission date
     */
    List<FormSubmissionHistory> findByFormIdentifierOrderBySubmittedAtDesc(String formIdentifier);

    /**
     * Find all submission history for a specific form, ordered by submission number
     */
    List<FormSubmissionHistory> findByFormIdentifierOrderBySubmissionNumberAsc(String formIdentifier);

    /**
     * Get the latest submission for a form
     */
    @Query("SELECT fsh FROM FormSubmissionHistory fsh WHERE fsh.formIdentifier = :formId " +
           "ORDER BY fsh.submissionNumber DESC LIMIT 1")
    FormSubmissionHistory findLatestSubmissionByFormId(@Param("formId") String formId);

    /**
     * Count total submissions for a form
     */
    Long countByFormIdentifier(String formIdentifier);

    /**
     * Find all submissions by a specific customer
     */
    List<FormSubmissionHistory> findBySubmittedByCustomerIdOrderBySubmittedAtDesc(UUID customerId);

    /**
     * Get submission history by form and submission number
     */
    FormSubmissionHistory findByFormIdentifierAndSubmissionNumber(String formIdentifier, Integer submissionNumber);
}

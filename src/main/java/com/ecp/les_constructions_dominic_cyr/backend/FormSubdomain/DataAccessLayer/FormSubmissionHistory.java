package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing a historical snapshot of form submissions.
 * Each time a customer submits or resubmits a form, a history entry is created.
 */
@Entity
@Table(name = "form_submission_history")
@Data
@NoArgsConstructor
public class FormSubmissionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the parent form
     */
    @Column(name = "form_identifier", nullable = false)
    private String formIdentifier;

    /**
     * Submission number (1 for first submission, 2 for first resubmission, etc.)
     */
    @Column(name = "submission_number", nullable = false)
    private Integer submissionNumber;

    /**
     * Status at the time of this submission
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status_at_submission", nullable = false)
    private FormStatus statusAtSubmission;

    /**
     * Snapshot of form data at the time of submission
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "form_data_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> formDataSnapshot = new HashMap<>();

    /**
     * Customer ID who submitted
     */
    @Column(name = "submitted_by_customer_id", nullable = false)
    private String submittedByCustomerId;

    /**
     * Customer name at time of submission
     */
    @Column(name = "submitted_by_customer_name")
    private String submittedByCustomerName;

    /**
     * Notes or comments from the customer at submission
     */
    @Column(name = "submission_notes", columnDefinition = "TEXT")
    private String submissionNotes;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false, nullable = false)
    private LocalDateTime submittedAt;

    public FormSubmissionHistory(String formIdentifier, Integer submissionNumber,
                                  FormStatus statusAtSubmission, Map<String, Object> formDataSnapshot,
                                  String submittedByCustomerId, String submittedByCustomerName) {
        this.formIdentifier = formIdentifier;
        this.submissionNumber = submissionNumber;
        this.statusAtSubmission = statusAtSubmission;
        this.formDataSnapshot = formDataSnapshot;
        this.submittedByCustomerId = submittedByCustomerId;
        this.submittedByCustomerName = submittedByCustomerName;
    }
}

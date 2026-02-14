package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing a customer form assignment.
 * Forms can be of various types (exterior doors, garage doors, windows, etc.)
 * and are assigned to customers by salespersons.
 */
@Entity
@Table(name = "forms")
@Data
@NoArgsConstructor
public class Form {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private FormIdentifier formIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormType formType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormStatus formStatus;

    /**
     * Project identifier this form is associated with
     */
    @Column(name = "project_identifier", nullable = false)
    private String projectIdentifier;

    /**
     * Lot identifier this form is associated with
     */
    @Column(name = "lot_identifier", nullable = false, columnDefinition = "UUID")
    private UUID lotIdentifier;

    /**
     * Customer user ID (UUID) this form is assigned to
     */
    @Column(name = "customer_id", nullable = false, columnDefinition = "UUID")
    private UUID customerId;

    /**
     * Customer's full name (denormalized for display)
     */
    @Column(name = "customer_name")
    private String customerName;

    /**
     * Customer's email (denormalized for notifications)
     */
    @Column(name = "customer_email")
    private String customerEmail;

    /**
     * Salesperson user ID who created/assigned this form
     */
    @Column(name = "assigned_by_user_id", nullable = false, columnDefinition = "UUID")
    private UUID assignedByUserId;

    /**
     * Salesperson's full name who assigned this form
     */
    @Column(name = "assigned_by_name")
    private String assignedByName;

    /**
     * Form title/name (optional custom name for the form)
     */
    @Column(name = "form_title")
    private String formTitle;

    /**
     * Optional instructions or notes for the customer
     */
    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    /**
     * Form data stored as JSON
     * This allows flexible storage of different form field structures
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "form_data", columnDefinition = "jsonb")
    private Map<String, Object> formData = new HashMap<>();

    /**
     * Date when form was assigned to customer
     */
    @Column(name = "assigned_date")
    private LocalDateTime assignedDate;

    /**
     * Date when customer first submitted the form
     */
    @Column(name = "first_submitted_date")
    private LocalDateTime firstSubmittedDate;

    /**
     * Date when form was last submitted (updated on resubmissions)
     */
    @Column(name = "last_submitted_date")
    private LocalDateTime lastSubmittedDate;

    /**
     * Date when form was marked as completed
     */
    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    /**
     * Date when form was last reopened (if applicable)
     */
    @Column(name = "reopened_date")
    private LocalDateTime reopenedDate;

    /**
     * User ID who reopened the form (if applicable)
     */
    @Column(name = "reopened_by_user_id", columnDefinition = "UUID")
    private UUID reopenedByUserId;

    /**
     * Reason for reopening the form
     */
    @Column(name = "reopen_reason", columnDefinition = "TEXT")
    private String reopenReason;

    /**
     * Number of times this form has been reopened
     */
    @Column(name = "reopen_count", nullable = false, columnDefinition = "int default 0")
    private Integer reopenCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Form(FormIdentifier formIdentifier, FormType formType, FormStatus formStatus,
                String projectIdentifier, String lotIdentifier, String customerId, String assignedByUserId) {
        this.formIdentifier = formIdentifier;
        this.formType = formType;
        this.formStatus = formStatus;
        this.projectIdentifier = projectIdentifier;
        this.lotIdentifier = UUID.fromString(lotIdentifier);
        this.customerId = UUID.fromString(customerId);
        this.assignedByUserId = UUID.fromString(assignedByUserId);
        this.reopenCount = 0;
    }
}

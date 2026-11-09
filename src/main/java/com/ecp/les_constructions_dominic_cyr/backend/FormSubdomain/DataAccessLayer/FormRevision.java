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
import java.util.UUID;

@Entity
@Table(name = "form_revisions")
@Data
@NoArgsConstructor
public class FormRevision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_identifier", nullable = false)
    private String formIdentifier;

    @Column(name = "customer_id", nullable = false, columnDefinition = "UUID")
    private UUID customerId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "form_data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> formData = new HashMap<>();

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FormRevisionStatus status = FormRevisionStatus.PENDING;

    @Column(name = "decision_reason", columnDefinition = "TEXT")
    private String decisionReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;
}

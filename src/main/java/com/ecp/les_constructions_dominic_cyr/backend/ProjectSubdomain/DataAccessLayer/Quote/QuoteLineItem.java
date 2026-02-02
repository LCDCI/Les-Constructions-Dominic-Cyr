package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Quote;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "quote_line_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_item_id")
    private Long lineItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @Column(name = "item_description", nullable = false, length = 500)
    private String itemDescription;

    /**
     * Quantity must be > 0.
     * Type: NUMERIC for precise calculations.
     */
    @Column(name = "quantity", nullable = false, columnDefinition = "NUMERIC(10,2)")
    private BigDecimal quantity;

    /**
     * Rate per unit (>= 0).
     * Precision: DECIMAL(15,2) for currency.
     */
    @Column(name = "rate", nullable = false, columnDefinition = "DECIMAL(15,2)")
    private BigDecimal rate;

    /**
     * Line total = quantity × rate.
     * Auto-calculated, never provided by frontend.
     * Precision: DECIMAL(15,2) for currency.
     */
    @Column(name = "line_total", nullable = false, columnDefinition = "DECIMAL(15,2)")
    private BigDecimal lineTotal;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    /**
     * Automatically calculate line total before persistence.
     */
    @PrePersist
    protected void calculateLineTotal() {
        if (quantity != null && rate != null) {
            this.lineTotal = quantity.multiply(rate);
        }
    }

    @PreUpdate
    protected void updateLineTotal() {
        if (quantity != null && rate != null) {
            this.lineTotal = quantity.multiply(rate);
        }
    }
}

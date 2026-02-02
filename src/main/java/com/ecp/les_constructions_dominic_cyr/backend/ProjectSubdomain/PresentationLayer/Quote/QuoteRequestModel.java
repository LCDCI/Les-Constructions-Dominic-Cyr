package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Quote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteRequestModel {

    /**
     * Project identifier - required to link the quote to a project.
     */
    @NotBlank(message = "Project identifier is required")
    private String projectIdentifier;

    /**
     * Lot identifier (optional) - if provided, the quote is linked to a specific lot within the project.
     */
    private String lotIdentifier;

    /**
     * Quote category - type of work being quoted (e.g., Kitchen, Bathroom, Flooring, etc.)
     */
    private String category;

    /**
     * List of line items for this quote.
     * At least 1 line item required.
     */
    @NotNull(message = "Line items cannot be null")
    private List<QuoteLineItemRequestModel> lineItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuoteLineItemRequestModel {
        /**
         * Description of the line item.
         */
        @NotBlank(message = "Item description is required")
        private String itemDescription;

        /**
         * Quantity must be positive.
         */
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        private BigDecimal quantity;

        /**
         * Rate per unit (>= 0).
         */
        @NotNull(message = "Rate is required")
        private BigDecimal rate;

        /**
         * Display order in the quote.
         */
        @NotNull(message = "Display order is required")
        private Integer displayOrder;
    }
}

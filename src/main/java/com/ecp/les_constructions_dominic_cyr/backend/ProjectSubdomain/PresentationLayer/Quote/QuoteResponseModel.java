package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Quote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteResponseModel {

    /**
     * System-generated sequential quote number (QT-XXXXXXX format).
     * Never provided by frontend, only by backend.
     */
    private String quoteNumber;

    private String projectIdentifier;

    private String lotIdentifier;

    private String category;

    private String contractorId;

    private List<QuoteLineItemResponseModel> lineItems;

    /**
     * Total amount auto-calculated from all line items.
     */
    private BigDecimal totalAmount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuoteLineItemResponseModel {
        private Long lineItemId;

        private String itemDescription;

        private BigDecimal quantity;

        private BigDecimal rate;

        /**
         * Line total = quantity × rate (auto-calculated).
         */
        private BigDecimal lineTotal;

        private Integer displayOrder;
    }
}

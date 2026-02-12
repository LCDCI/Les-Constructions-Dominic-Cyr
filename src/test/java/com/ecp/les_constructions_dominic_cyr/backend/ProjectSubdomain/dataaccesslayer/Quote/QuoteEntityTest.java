package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.dataaccesslayer.Quote;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Quote.Quote;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Quote.QuoteLineItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QuoteEntityTest {

    @Test
    void recalculateTotal_WithLineItems_SumsLineTotals() {
        Quote quote = new Quote();
        List<QuoteLineItem> items = new ArrayList<>();
        QuoteLineItem item1 = QuoteLineItem.builder()
            .itemDescription("Item 1")
            .quantity(new BigDecimal("2"))
            .rate(new BigDecimal("50.00"))
            .lineTotal(new BigDecimal("100.00"))
            .displayOrder(1)
            .build();
        item1.setQuote(quote);
        QuoteLineItem item2 = QuoteLineItem.builder()
            .itemDescription("Item 2")
            .quantity(new BigDecimal("1"))
            .rate(new BigDecimal("25.00"))
            .lineTotal(new BigDecimal("25.00"))
            .displayOrder(2)
            .build();
        item2.setQuote(quote);
        items.add(item1);
        items.add(item2);
        quote.setLineItems(items);

        quote.recalculateTotal();

        assertEquals(new BigDecimal("125.00"), quote.getTotalAmount());
    }

    @Test
    void recalculateTotal_EmptyLineItems_SetsZero() {
        Quote quote = new Quote();
        quote.setLineItems(new ArrayList<>());
        quote.setTotalAmount(new BigDecimal("999.00"));

        quote.recalculateTotal();

        assertEquals(BigDecimal.ZERO, quote.getTotalAmount());
    }

    @Test
    void recalculateTotal_NullLineItems_SetsZero() {
        Quote quote = new Quote();
        quote.setLineItems(null);
        quote.setTotalAmount(new BigDecimal("999.00"));

        quote.recalculateTotal();

        assertEquals(BigDecimal.ZERO, quote.getTotalAmount());
    }

    @Test
    void quoteBuilder_Works() {
        UUID lotId = UUID.randomUUID();
        Quote quote = Quote.builder()
            .quoteNumber("QT-0000001")
            .projectIdentifier("proj-1")
            .lotIdentifier(lotId)
            .category("Kitchen")
            .contractorId("c1")
            .totalAmount(new BigDecimal("500.00"))
            .build();

        assertEquals("QT-0000001", quote.getQuoteNumber());
        assertEquals("proj-1", quote.getProjectIdentifier());
        assertEquals(lotId, quote.getLotIdentifier());
        assertEquals("Kitchen", quote.getCategory());
        assertEquals("c1", quote.getContractorId());
        assertEquals(new BigDecimal("500.00"), quote.getTotalAmount());
    }

    @Test
    void quoteLineItem_BuilderAndGetters() {
        Quote quote = new Quote();
        QuoteLineItem item = QuoteLineItem.builder()
            .quote(quote)
            .lineItemId(1L)
            .itemDescription("Work")
            .quantity(new BigDecimal("3"))
            .rate(new BigDecimal("10.00"))
            .lineTotal(new BigDecimal("30.00"))
            .displayOrder(0)
            .build();
        assertEquals(1L, item.getLineItemId());
        assertEquals("Work", item.getItemDescription());
        assertEquals(new BigDecimal("3"), item.getQuantity());
        assertEquals(new BigDecimal("10.00"), item.getRate());
        assertEquals(new BigDecimal("30.00"), item.getLineTotal());
        assertEquals(0, item.getDisplayOrder());
    }
}

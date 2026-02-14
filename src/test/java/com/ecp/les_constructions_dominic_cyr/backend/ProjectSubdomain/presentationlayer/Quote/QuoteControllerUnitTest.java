package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer.Quote;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Quote.QuoteService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Quote.QuoteController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Quote.QuoteRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Quote.QuoteResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QuoteControllerUnitTest {

    private QuoteService quoteService;
    private QuoteController controller;

    private Authentication authentication;
    private QuoteRequestModel validRequest;
    private QuoteResponseModel responseModel;

    @BeforeEach
    void setUp() {
        quoteService = mock(QuoteService.class);
        controller = new QuoteController(quoteService);

        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("auth0|contractor1");
        org.springframework.security.oauth2.jwt.Jwt jwt = mock(org.springframework.security.oauth2.jwt.Jwt.class);
        when(jwt.getSubject()).thenReturn("auth0|contractor1");
        when(authentication.getPrincipal()).thenReturn(jwt);

        validRequest = QuoteRequestModel.builder()
            .projectIdentifier("proj-1")
            .lotIdentifier(java.util.UUID.randomUUID().toString())
            .lineItems(List.of(
                QuoteRequestModel.QuoteLineItemRequestModel.builder()
                    .itemDescription("Work")
                    .quantity(BigDecimal.ONE)
                    .rate(BigDecimal.TEN)
                    .displayOrder(0)
                    .build()
            ))
            .build();

        responseModel = QuoteResponseModel.builder()
            .quoteNumber("QT-0000001")
            .projectIdentifier("proj-1")
            .contractorId("auth0|contractor1")
            .totalAmount(new BigDecimal("10.00"))
            .build();
    }

    @Test
    void createQuote_Returns201() {
        when(quoteService.createQuote(any(QuoteRequestModel.class), eq("auth0|contractor1"))).thenReturn(responseModel);

        ResponseEntity<QuoteResponseModel> resp = controller.createQuote(validRequest, authentication);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertEquals("QT-0000001", resp.getBody().getQuoteNumber());
    }

    @Test
    void getQuotesByProject_ReturnsOk() {
        when(quoteService.getQuotesByProject("proj-1")).thenReturn(List.of(responseModel));

        ResponseEntity<List<QuoteResponseModel>> resp = controller.getQuotesByProject("proj-1");

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().size());
        assertEquals("QT-0000001", resp.getBody().get(0).getQuoteNumber());
    }

    @Test
    void getQuotesByLot_ReturnsOk() {
        String lotId = java.util.UUID.randomUUID().toString();
        when(quoteService.getQuotesByLot(lotId)).thenReturn(List.of(responseModel));

        ResponseEntity<List<QuoteResponseModel>> resp = controller.getQuotesByLot(lotId);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().size());
    }

    @Test
    void getQuoteByNumber_ReturnsOk() {
        when(quoteService.getQuoteByNumber("QT-0000001")).thenReturn(responseModel);

        ResponseEntity<QuoteResponseModel> resp = controller.getQuoteByNumber("QT-0000001");

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("QT-0000001", resp.getBody().getQuoteNumber());
    }

    @Test
    void getMyQuotes_ReturnsOk() {
        when(quoteService.getQuotesByContractor("auth0|contractor1")).thenReturn(List.of(responseModel));

        ResponseEntity<List<QuoteResponseModel>> resp = controller.getMyQuotes(authentication);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().size());
    }
}

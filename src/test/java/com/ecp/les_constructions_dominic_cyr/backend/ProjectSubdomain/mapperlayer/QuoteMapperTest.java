package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.mapperlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Quote.Quote;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Quote.QuoteLineItem;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.QuoteMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Quote.QuoteRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Quote.QuoteResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@org.junit.jupiter.api.extension.ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class QuoteMapperTest {

    private QuoteMapper quoteMapper;

    @BeforeEach
    void setUp() {
        quoteMapper = new QuoteMapper();
    }

    @Test
    void requestModelToEntity_WithLotIdentifierAndLineItems_MapsCorrectly() {
        String lotId = UUID.randomUUID().toString();
        QuoteRequestModel request = QuoteRequestModel.builder()
            .projectIdentifier("proj-1")
            .lotIdentifier(lotId)
            .category("Kitchen")
            .lineItems(List.of(
                QuoteRequestModel.QuoteLineItemRequestModel.builder()
                    .itemDescription("Install cabinets")
                    .quantity(new BigDecimal("2"))
                    .rate(new BigDecimal("500.00"))
                    .displayOrder(1)
                    .build()
            ))
            .build();

        Quote entity = quoteMapper.requestModelToEntity(request, "QT-0000001", "contractor-1");

        assertEquals("QT-0000001", entity.getQuoteNumber());
        assertEquals("proj-1", entity.getProjectIdentifier());
        assertEquals(UUID.fromString(lotId), entity.getLotIdentifier());
        assertEquals("Kitchen", entity.getCategory());
        assertEquals("contractor-1", entity.getContractorId());
        assertEquals(1, entity.getLineItems().size());
        assertEquals("Install cabinets", entity.getLineItems().get(0).getItemDescription());
        assertEquals(new BigDecimal("2"), entity.getLineItems().get(0).getQuantity());
        assertEquals(new BigDecimal("500.00"), entity.getLineItems().get(0).getRate());
        assertEquals(new BigDecimal("1000.00"), entity.getTotalAmount());
    }

    @Test
    void requestModelToEntity_WithBlankLotIdentifier_SetsNullLotIdentifier() {
        QuoteRequestModel request = QuoteRequestModel.builder()
            .projectIdentifier("proj-1")
            .lotIdentifier("   ")
            .lineItems(List.of(
                QuoteRequestModel.QuoteLineItemRequestModel.builder()
                    .itemDescription("Item")
                    .quantity(BigDecimal.ONE)
                    .rate(BigDecimal.TEN)
                    .displayOrder(0)
                    .build()
            ))
            .build();

        Quote entity = quoteMapper.requestModelToEntity(request, "QT-0000002", "c1");

        assertNull(entity.getLotIdentifier());
    }

    @Test
    void entityToResponseModel_MapsCorrectly() {
        UUID lotId = UUID.randomUUID();
        Quote quote = Quote.builder()
            .quoteNumber("QT-0000001")
            .projectIdentifier("proj-1")
            .lotIdentifier(lotId)
            .category("Bathroom")
            .contractorId("c1")
            .totalAmount(new BigDecimal("1500.00"))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        QuoteLineItem lineItem = QuoteLineItem.builder()
            .lineItemId(1L)
            .itemDescription("Tile work")
            .quantity(new BigDecimal("10"))
            .rate(new BigDecimal("15.00"))
            .lineTotal(new BigDecimal("150.00"))
            .displayOrder(1)
            .build();
        lineItem.setQuote(quote);
        quote.setLineItems(List.of(lineItem));

        QuoteResponseModel response = quoteMapper.entityToResponseModel(quote);

        assertEquals("QT-0000001", response.getQuoteNumber());
        assertEquals("proj-1", response.getProjectIdentifier());
        assertEquals(lotId.toString(), response.getLotIdentifier());
        assertEquals("Bathroom", response.getCategory());
        assertEquals("c1", response.getContractorId());
        assertEquals(new BigDecimal("1500.00"), response.getTotalAmount());
        assertEquals(1, response.getLineItems().size());
        assertEquals("Tile work", response.getLineItems().get(0).getItemDescription());
    }

    @Test
    void entityToResponseModel_NullLotIdentifier_ReturnsNullInResponse() {
        Quote quote = Quote.builder()
            .quoteNumber("QT-0000001")
            .projectIdentifier("proj-1")
            .lotIdentifier(null)
            .contractorId("c1")
            .totalAmount(BigDecimal.ZERO)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .lineItems(List.of())
            .build();

        QuoteResponseModel response = quoteMapper.entityToResponseModel(quote);

        assertNull(response.getLotIdentifier());
    }

    @Test
    void getContractorIdFromAuth_WithJwt_ReturnsSubject() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("auth0|user123");
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(jwt);

        String result = QuoteMapper.getContractorIdFromAuth(auth);

        assertEquals("auth0|user123", result);
    }

    @Test
    void getContractorIdFromAuth_NullAuth_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            QuoteMapper.getContractorIdFromAuth(null));
    }

    @Test
    void getContractorIdFromAuth_NonJwtPrincipal_ThrowsException() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("not-a-jwt");

        assertThrows(IllegalArgumentException.class, () ->
            QuoteMapper.getContractorIdFromAuth(auth));
    }

    /*@Test
    void isContractor_WithContractorRole_ReturnsTrue() {
        Authentication auth = mock(Authentication.class);
        when(auth.getAuthorities()).thenReturn(List.of(
            new SimpleGrantedAuthority("ROLE_CONTRACTOR")
        ));

        assertTrue(QuoteMapper.isContractor(auth));
    }

    @Test
    void isContractor_WithoutContractorRole_ReturnsFalse() {
        Authentication auth = mock(Authentication.class);
        when(auth.getAuthorities()).thenReturn(List.of(
            new SimpleGrantedAuthority("ROLE_CUSTOMER")
        ));

        assertFalse(QuoteMapper.isContractor(auth));
    }*/
}

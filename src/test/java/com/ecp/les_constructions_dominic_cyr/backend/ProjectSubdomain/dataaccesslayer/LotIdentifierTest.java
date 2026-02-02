package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LotIdentifierTest {

    private static final String CUSTOM_ID = UUID.randomUUID().toString();

    @Test
    @DisplayName("NoArgsConstructor (Default) should generate a random UUID")
    void noArgsConstructor_GeneratesUUID() {
        // ACT
        LotIdentifier identifier = new LotIdentifier();

        // ASSERT
        assertNotNull(identifier.getLotId(), "The LotId should not be null");
        assertDoesNotThrow(() -> UUID.fromString(identifier.getLotId().toString()), "The generated LotId should be a valid UUID");
    }

    @Test
    @DisplayName("Argument Constructor should set the provided LotId")
    void argumentConstructor_SetsProvidedLotId() {
        // ACT
        LotIdentifier identifier = new LotIdentifier(CUSTOM_ID);

        // ASSERT
        assertEquals(UUID.fromString(CUSTOM_ID), identifier.getLotId(), "The LotId should match the custom value provided");
    }

    @Test
    @DisplayName("toString() should return the expected formatted string with a value")
    void toString_WithLotId_ReturnsFormattedString() {
        // ARRANGE
        LotIdentifier identifier = new LotIdentifier(CUSTOM_ID);
        String expected = "LotIdentifier{lotId='" + CUSTOM_ID + "'}";

        // ACT
        String result = identifier.toString();

        // ASSERT
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Argument Constructor should throw for null LotId")
    void argumentConstructor_WithNullLotId_Throws() {
        assertThrows(NullPointerException.class, () -> new LotIdentifier(null));
    }
}
package com.ecp.les_constructions_dominic_cyr.ProjectSubdomain.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LotIdentifierTest {

    private static final String CUSTOM_ID = "Custom-Lot-ID-789";

    @Test
    @DisplayName("NoArgsConstructor (Default) should generate a random UUID")
    void noArgsConstructor_GeneratesUUID() {
        // ACT
        LotIdentifier identifier = new LotIdentifier();

        // ASSERT
        assertNotNull(identifier.getLotId(), "The LotId should not be null");
        assertDoesNotThrow(() -> java.util.UUID.fromString(identifier.getLotId()), "The generated LotId should be a valid UUID");
    }

    @Test
    @DisplayName("Argument Constructor should set the provided LotId")
    void argumentConstructor_SetsProvidedLotId() {
        // ACT
        LotIdentifier identifier = new LotIdentifier(CUSTOM_ID);

        // ASSERT
        assertEquals(CUSTOM_ID, identifier.getLotId(), "The LotId should match the custom value provided");
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
    @DisplayName("toString() should return the expected formatted string when LotId is null")
    void toString_WithNullLotId_ReturnsFormattedString() {
        // ARRANGE
        // Create an identifier instance and set lotId to null via reflection, or directly via constructor if possible.
        // Since we are fixing coverage, we can use the argument constructor for nullability testing.
        LotIdentifier identifier = new LotIdentifier(null);

        // Use reflection to set the field to null if the constructor guards against it,
        // but since the provided toString() handles null, we assume the constructor allows it.

        String expected = "LotIdentifier{lotId=''}"; // Expect empty quotes for null/empty handling

        // ACT
        String result = identifier.toString();

        // ASSERT
        assertEquals(expected, result);
    }
}
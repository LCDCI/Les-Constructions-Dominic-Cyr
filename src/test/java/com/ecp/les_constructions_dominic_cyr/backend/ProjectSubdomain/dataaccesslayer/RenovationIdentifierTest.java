package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.RenovationIdentifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RenovationIdentifierTest {

    private static final String CUSTOM_ID = "Custom-Renovation-ID-789";

    @Test
    @DisplayName("NoArgsConstructor (Default) should generate a random UUID")
    void noArgsConstructor_GeneratesUUID() {
        // ACT
        RenovationIdentifier identifier = new RenovationIdentifier();

        // ASSERT
        assertNotNull(identifier.getRenovationId(), "The RenovationId should not be null");
        assertDoesNotThrow(() -> java.util.UUID.fromString(identifier.getRenovationId()), "The generated RenovationId should be a valid UUID");
    }

    @Test
    @DisplayName("Argument Constructor should set the provided RenovationId")
    void argumentConstructor_SetsProvidedRenovationId() {
        // ACT
        RenovationIdentifier identifier = new RenovationIdentifier(CUSTOM_ID);

        // ASSERT
        assertEquals(CUSTOM_ID, identifier.getRenovationId(), "The RenovationId should match the custom value provided");
    }

    @Test
    @DisplayName("toString() should return the expected formatted string with a value")
    void toString_WithRenovationId_ReturnsFormattedString() {
        // ARRANGE
        RenovationIdentifier identifier = new RenovationIdentifier(CUSTOM_ID);
        String expected = "RenovationIdentifier{renovationId='" + CUSTOM_ID + "'}";

        // ACT
        String result = identifier.toString();

        // ASSERT
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("toString() should return the expected formatted string when RenovationId is null")
    void toString_WithNullRenovationId_ReturnsFormattedString() {
        // ARRANGE
        RenovationIdentifier identifier = new RenovationIdentifier(null);

        String expected = "RenovationIdentifier{renovationId=''}"; // Expect empty quotes for null/empty handling

        // ACT
        String result = identifier.toString();

        // ASSERT
        assertEquals(expected, result);
    }
}

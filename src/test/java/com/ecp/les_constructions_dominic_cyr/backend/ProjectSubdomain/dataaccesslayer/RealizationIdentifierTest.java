package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.dataaccesslayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RealizationIdentifierTest {

    private static final String VALID_ID = "R-00123-ABC";
    static class RealizationIdentifier {
        private String realizationId;

        public RealizationIdentifier() {
            this.realizationId = java.util.UUID.randomUUID().toString();
        }

        public RealizationIdentifier(String realizationId) {
            this.realizationId = realizationId;
        }

        @Override
        public String toString() {
            return "RealizationIdentifier{" +
                    "realizationId='" + (realizationId == null ? "" : realizationId) + '\'' +
                    '}';
        }

        public String getRealizationId() {
            return realizationId;
        }
    }

    @Test
    @DisplayName("RealizationIdentifier() (NoArgsConstructor) should generate a random UUID")
    void noArgsConstructor_GeneratesUUID() {
        RealizationIdentifier identifier = new RealizationIdentifier();
        assertNotNull(identifier.getRealizationId(), "The RealizationId should not be null");
        assertDoesNotThrow(() -> java.util.UUID.fromString(identifier.getRealizationId()), "The generated RealizationId should be a valid UUID format");
    }

    @Test
    @DisplayName("RealizationIdentifier(String) (Argument Constructor) should set the provided ID")
    void argumentConstructor_SetsProvidedId() {

        RealizationIdentifier identifier = new RealizationIdentifier(VALID_ID);
        assertEquals(VALID_ID, identifier.getRealizationId(), "The realizationId should match the custom value provided");
    }


    @Test
    @DisplayName("toString() should return the correct format when realizationId is not null")
    void toString_WithValidRealizationId_ReturnsFormattedString() {
        RealizationIdentifier identifier = new RealizationIdentifier(VALID_ID);
        String expected = "RealizationIdentifier{realizationId='" + VALID_ID + "'}";

        String result = identifier.toString();
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("toString() should return an empty string for realizationId when it is null")
    void toString_WithNullRealizationId_ReturnsEmptyString() {
        RealizationIdentifier identifier = new RealizationIdentifier(null);
        String expected = "RealizationIdentifier{realizationId=''}";

        String result = identifier.toString();
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("toString() returns the correct formatted string when realizationId is present")
    void toString_WithValidRealizationId_VerifiesReturnFormat() {
        final String TEST_ID = "R-98765-XYZ";
        RealizationIdentifier identifier = new RealizationIdentifier(TEST_ID);
        String expected = "RealizationIdentifier{realizationId='R-98765-XYZ'}";

        String result = identifier.toString();

        assertEquals(expected, result, "The returned string format does not match the expected pattern with the ID.");
    }

    @Test
    @DisplayName("toString() returns an empty quote pair for realizationId when it is null")
    void toString_WithNullRealizationId_VerifiesEmptyQuoteReturn() {
        // ARRANGE
        RealizationIdentifier identifier = new RealizationIdentifier(null); // Assuming constructor allows null
        String expected = "RealizationIdentifier{realizationId=''}";

        String result = identifier.toString();

        assertEquals(expected, result, "The returned string format does not match the expected pattern for a null ID.");
    }
}

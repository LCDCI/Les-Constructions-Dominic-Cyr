package com.ecp.les_constructions_dominic_cyr.ProjectSubdomain.dataaccesslayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HouseIdentifierTest {

    private static final String VALID_ID = "H-00123-ABC";
    static class HouseIdentifier {
        private String houseId;

        public HouseIdentifier() {
            this.houseId = java.util.UUID.randomUUID().toString();
        }

        public HouseIdentifier(String houseId) {
            this.houseId = houseId;
        }

        @Override
        public String toString() {
            return "HouseIdentifier{" +
                    "houseId='" + (houseId == null ? "" : houseId) + '\'' +
                    '}';
        }

        public String getHouseId() {
            return houseId;
        }
    }

    @Test
    @DisplayName("HouseIdentifier() (NoArgsConstructor) should generate a random UUID")
    void noArgsConstructor_GeneratesUUID() {
        HouseIdentifier identifier = new HouseIdentifier();
        assertNotNull(identifier.getHouseId(), "The LotId should not be null");
        assertDoesNotThrow(() -> java.util.UUID.fromString(identifier.getHouseId()), "The generated LotId should be a valid UUID format");
    }

    @Test
    @DisplayName("HouseIdentifier(String) (Argument Constructor) should set the provided ID")
    void argumentConstructor_SetsProvidedId() {

        HouseIdentifier identifier = new HouseIdentifier(VALID_ID);
        assertEquals(VALID_ID, identifier.getHouseId(), "The houseId should match the custom value provided");
    }


    @Test
    @DisplayName("toString() should return the correct format when houseId is not null")
    void toString_WithValidHouseId_ReturnsFormattedString() {
        HouseIdentifier identifier = new HouseIdentifier(VALID_ID);
        String expected = "HouseIdentifier{houseId='" + VALID_ID + "'}";

        String result = identifier.toString();
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("toString() should return an empty string for houseId when it is null")
    void toString_WithNullHouseId_ReturnsEmptyString() {
        HouseIdentifier identifier = new HouseIdentifier(null);
        String expected = "HouseIdentifier{houseId=''}";

        String result = identifier.toString();
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("toString() returns the correct formatted string when houseId is present")
    void toString_WithValidHouseId_VerifiesReturnFormat() {
        final String TEST_ID = "H-98765-XYZ";
        HouseIdentifier identifier = new HouseIdentifier(TEST_ID);
        String expected = "HouseIdentifier{houseId='H-98765-XYZ'}";

        String result = identifier.toString();

        assertEquals(expected, result, "The returned string format does not match the expected pattern with the ID.");
    }

    @Test
    @DisplayName("toString() returns an empty quote pair for houseId when it is null")
    void toString_WithNullHouseId_VerifiesEmptyQuoteReturn() {
        // ARRANGE
        HouseIdentifier identifier = new HouseIdentifier(null); // Assuming constructor allows null
        String expected = "HouseIdentifier{houseId=''}";

        String result = identifier.toString();

        assertEquals(expected, result, "The returned string format does not match the expected pattern for a null ID.");
    }
}

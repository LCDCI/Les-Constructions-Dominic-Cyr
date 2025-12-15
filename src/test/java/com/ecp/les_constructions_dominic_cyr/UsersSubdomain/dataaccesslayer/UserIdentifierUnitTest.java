package com.ecp.les_constructions_dominic_cyr.UsersSubdomain.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserIdentifierUnitTest {

    // ========================== POSITIVE TESTS ==========================

    @Test
    void newId_GeneratesValidIdentifier() {
        UserIdentifier identifier = UserIdentifier.newId();

        assertNotNull(identifier);
        assertNotNull(identifier.getUserId());
    }

    @Test
    void newId_GeneratesUniqueIdentifiers() {
        UserIdentifier id1 = UserIdentifier.newId();
        UserIdentifier id2 = UserIdentifier.newId();
        UserIdentifier id3 = UserIdentifier.newId();

        assertNotEquals(id1.getUserId(), id2.getUserId());
        assertNotEquals(id2.getUserId(), id3.getUserId());
        assertNotEquals(id1.getUserId(), id3.getUserId());
    }

    @Test
    void fromString_ParsesValidUUID() {
        String uuidString = "123e4567-e89b-12d3-a456-426614174000";
        
        UserIdentifier identifier = UserIdentifier.fromString(uuidString);

        assertNotNull(identifier);
        assertEquals(UUID.fromString(uuidString), identifier.getUserId());
    }

    @Test
    void fromString_TrimsWhitespace() {
        String uuidWithSpaces = "  123e4567-e89b-12d3-a456-426614174000  ";
        
        UserIdentifier identifier = UserIdentifier.fromString(uuidWithSpaces);

        assertNotNull(identifier);
        assertEquals(UUID.fromString(uuidWithSpaces.trim()), identifier.getUserId());
    }

    @Test
    void toString_ReturnsUUIDString() {
        String uuidString = "123e4567-e89b-12d3-a456-426614174000";
        UserIdentifier identifier = UserIdentifier.fromString(uuidString);

        assertEquals(uuidString, identifier.toString());
    }

    @Test
    void equals_ReturnsTrueForSameUUID() {
        String uuidString = "123e4567-e89b-12d3-a456-426614174000";
        UserIdentifier id1 = UserIdentifier.fromString(uuidString);
        UserIdentifier id2 = UserIdentifier.fromString(uuidString);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void equals_ReturnsFalseForDifferentUUID() {
        UserIdentifier id1 = UserIdentifier.newId();
        UserIdentifier id2 = UserIdentifier.newId();

        assertNotEquals(id1, id2);
    }

    @Test
    void equals_ReturnsTrueForSameInstance() {
        UserIdentifier id = UserIdentifier.newId();

        assertEquals(id, id);
    }

    @Test
    void equals_ReturnsFalseForNull() {
        UserIdentifier id = UserIdentifier.newId();

        assertNotEquals(id, null);
    }

    @Test
    void equals_ReturnsFalseForDifferentType() {
        UserIdentifier id = UserIdentifier.newId();

        assertNotEquals(id, "not a UserIdentifier");
        assertNotEquals(id, new Object());
    }

    @Test
    void hashCode_ConsistentForSameIdentifier() {
        UserIdentifier id = UserIdentifier.newId();
        int hashCode1 = id.hashCode();
        int hashCode2 = id.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void hashCode_EqualForEqualIdentifiers() {
        String uuidString = "123e4567-e89b-12d3-a456-426614174000";
        UserIdentifier id1 = UserIdentifier.fromString(uuidString);
        UserIdentifier id2 = UserIdentifier.fromString(uuidString);

        assertEquals(id1.hashCode(), id2.hashCode());
    }

    // ========================== NEGATIVE TESTS ==========================

    @Test
    void fromString_ThrowsException_ForNullInput() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> UserIdentifier.fromString(null)
        );

        assertEquals("User ID cannot be null or empty", exception.getMessage());
    }

    @Test
    void fromString_ThrowsException_ForEmptyString() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> UserIdentifier.fromString("")
        );

        assertEquals("User ID cannot be null or empty", exception.getMessage());
    }

    @Test
    void fromString_ThrowsException_ForWhitespaceOnly() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> UserIdentifier.fromString("   ")
        );

        assertEquals("User ID cannot be null or empty", exception.getMessage());
    }

    @Test
    void fromString_ThrowsException_ForInvalidUUIDFormat() {
        String invalidUuid = "not-a-valid-uuid";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> UserIdentifier.fromString(invalidUuid)
        );

        assertTrue(exception.getMessage().contains("Invalid user ID format"));
    }

    @Test
    void fromString_ThrowsException_ForMalformedUUID() {
        // Missing characters
        assertThrows(IllegalArgumentException.class, () -> UserIdentifier.fromString("123e4567-e89b-12d3-a456"));

        // Extra characters
        assertThrows(IllegalArgumentException.class, () -> UserIdentifier.fromString("123e4567-e89b-12d3-a456-426614174000-extra"));

        // Wrong format
        assertThrows(IllegalArgumentException.class, () -> UserIdentifier.fromString("123e4567e89b12d3a456426614174000")); // No dashes
    }

    @Test
    void fromString_ThrowsException_ForPartialUUID() {
        assertThrows(IllegalArgumentException.class, () -> UserIdentifier.fromString("123e4567"));
        assertThrows(IllegalArgumentException.class, () -> UserIdentifier.fromString("123e4567-e89b"));
        assertThrows(IllegalArgumentException.class, () -> UserIdentifier.fromString("123e4567-e89b-12d3"));
    }

    @Test
    void fromString_ThrowsException_ForSpecialCharacters() {
        assertThrows(IllegalArgumentException.class, () -> UserIdentifier.fromString("123e4567-e89b-12d3-a456-42661417400@"));
        assertThrows(IllegalArgumentException.class, () -> UserIdentifier.fromString("123e4567-e89b-12d3-a456-42661417400!"));
    }

    // ========================== SERIALIZATION TESTS ==========================

    @Test
    void getUserId_ReturnsCorrectUUID() {
        UUID expectedUuid = UUID.randomUUID();
        UserIdentifier identifier = UserIdentifier.fromString(expectedUuid.toString());

        assertEquals(expectedUuid, identifier.getUserId());
    }

    @Test
    void roundTrip_PreservesUUID() {
        UserIdentifier original = UserIdentifier.newId();
        String uuidString = original.toString();
        UserIdentifier restored = UserIdentifier.fromString(uuidString);

        assertEquals(original, restored);
        assertEquals(original.getUserId(), restored.getUserId());
    }
}

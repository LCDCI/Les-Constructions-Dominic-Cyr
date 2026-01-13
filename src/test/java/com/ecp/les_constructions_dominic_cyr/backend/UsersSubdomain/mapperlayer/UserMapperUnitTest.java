package com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.mapperlayer;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserStatus;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.MapperLayer.UserMapper;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserCreateRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserMapperUnitTest {

    // ========================== toEntity TESTS ==========================

    @Test
    void toEntity_MapsAllFieldsCorrectly() {
        UserCreateRequestModel request = new UserCreateRequestModel();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPrimaryEmail("john.doe@example.com");
        request.setSecondaryEmail("john.secondary@example.com");
        request.setPhone("514-555-1234");
        request.setUserRole(UserRole.CUSTOMER);

        Users result = UserMapper.toEntity(request);

        assertNotNull(result);
        assertNotNull(result.getUserIdentifier());
        assertNotNull(result.getUserIdentifier().getUserId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@example.com", result.getPrimaryEmail());
        assertEquals("john.secondary@example.com", result.getSecondaryEmail());
        assertEquals("514-555-1234", result.getPhone());
        assertEquals(UserRole.CUSTOMER, result.getUserRole());
        assertNull(result.getAuth0UserId()); // Not set during initial mapping
    }

    @Test
    void toEntity_GeneratesUniqueIdentifiers() {
        UserCreateRequestModel request = new UserCreateRequestModel();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPrimaryEmail("john@example.com");
        request.setUserRole(UserRole.CUSTOMER);

        Users result1 = UserMapper.toEntity(request);
        Users result2 = UserMapper.toEntity(request);

        assertNotEquals(result1.getUserIdentifier().getUserId(), result2.getUserIdentifier().getUserId());
    }

    @Test
    void toEntity_HandlesNullOptionalFields() {
        UserCreateRequestModel request = new UserCreateRequestModel();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPrimaryEmail("john.doe@example.com");
        request.setSecondaryEmail(null);
        request.setPhone(null);
        request.setUserRole(UserRole.OWNER);

        Users result = UserMapper.toEntity(request);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertNull(result.getSecondaryEmail());
        assertNull(result.getPhone());
        assertEquals(UserRole.OWNER, result.getUserRole());
    }

    @Test
    void toEntity_MapsAllRoles() {
        for (UserRole role : UserRole.values()) {
            UserCreateRequestModel request = new UserCreateRequestModel();
            request.setFirstName("Test");
            request.setLastName("User");
            request.setPrimaryEmail("test@example.com");
            request.setUserRole(role);

            Users result = UserMapper.toEntity(request);

            assertEquals(role, result.getUserRole());
        }
    }

    // ========================== toResponseModel TESTS ==========================

    @Test
    void toResponseModel_MapsAllFieldsCorrectly() {
        UserIdentifier identifier = UserIdentifier.newId();
        Users user = new Users(
                identifier,
                "Jane",
                "Smith",
                "jane.smith@example.com",
                "jane.secondary@example.com",
                "514-666-7890",
                UserRole.SALESPERSON,
                "auth0|abc123",
                UserStatus.ACTIVE
        );

        UserResponseModel result = UserMapper.toResponseModel(user, null);

        assertNotNull(result);
        assertEquals(identifier.getUserId().toString(), result.getUserIdentifier());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("jane.smith@example.com", result.getPrimaryEmail());
        assertEquals("jane.secondary@example.com", result.getSecondaryEmail());
        assertEquals("514-666-7890", result.getPhone());
        assertEquals(UserRole.SALESPERSON, result.getUserRole());
        assertNull(result.getInviteLink());
    }

    @Test
    void toResponseModel_IncludesInviteLink_WhenProvided() {
        UserIdentifier identifier = UserIdentifier.newId();
        Users user = new Users(
                identifier,
                "New",
                "User",
                "new.user@example.com",
                null,
                null,
                UserRole.CUSTOMER,
                "auth0|newuser",
                UserStatus.ACTIVE
        );
        String inviteLink = "https://auth0.com/invite/xyz789";

        UserResponseModel result = UserMapper.toResponseModel(user, inviteLink);

        assertNotNull(result);
        assertEquals(inviteLink, result.getInviteLink());
    }

    @Test
    void toResponseModel_HandlesNullOptionalFields() {
        UserIdentifier identifier = UserIdentifier.newId();
        Users user = new Users(
                identifier,
                "Minimal",
                "User",
                "minimal@example.com",
                null, // no secondary email
                null, // no phone
                UserRole.CONTRACTOR,
                null,// no auth0 id yet
                UserStatus.ACTIVE
        );

        UserResponseModel result = UserMapper.toResponseModel(user, null);

        assertNotNull(result);
        assertEquals("Minimal", result.getFirstName());
        assertNull(result.getSecondaryEmail());
        assertNull(result.getPhone());
        assertNull(result.getInviteLink());
    }

    @Test
    void toResponseModel_MapsAllRoles() {
        for (UserRole role : UserRole.values()) {
            UserIdentifier identifier = UserIdentifier.newId();
            Users user = new Users(
                    identifier,
                    "Test",
                    "User",
                    "test@example.com",
                    null,
                    null,
                    role,
                    "auth0|test",
                    UserStatus.ACTIVE
            );

            UserResponseModel result = UserMapper.toResponseModel(user, null);

            assertEquals(role, result.getUserRole());
        }
    }

    @Test
    void toResponseModel_HandlesNullUserIdentifier() {
        Users user = new Users(
                null,
                "NoId",
                "User",
                "noid@example.com",
                null,
                null,
                UserRole.CUSTOMER,
                "auth0|noid",
                UserStatus.ACTIVE
        );

        UserResponseModel result = UserMapper.toResponseModel(user, null);

        assertNotNull(result);
        assertNull(result.getUserIdentifier());
        assertEquals("NoId", result.getFirstName());
    }

    @Test
    void toResponseModel_HandlesUserIdentifierWithNullUserId() {
        // Create a user with a null inner userId (edge case)
        Users user = new Users();
        user.setFirstName("EdgeCase");
        user.setLastName("User");
        user.setPrimaryEmail("edge@example.com");
        user.setUserRole(UserRole.CUSTOMER);
        // userIdentifier is null

        UserResponseModel result = UserMapper.toResponseModel(user, null);

        assertNotNull(result);
        assertNull(result.getUserIdentifier());
        assertEquals("EdgeCase", result.getFirstName());
    }

    // ========================== EDGE CASE TESTS ==========================

    @Test
    void toEntity_HandleEmptyStrings() {
        UserCreateRequestModel request = new UserCreateRequestModel();
        request.setFirstName("");
        request.setLastName("");
        request.setPrimaryEmail("");
        request.setSecondaryEmail("");
        request.setPhone("");
        request.setUserRole(UserRole.CUSTOMER);

        Users result = UserMapper.toEntity(request);

        assertNotNull(result);
        assertEquals("", result.getFirstName());
        assertEquals("", result.getLastName());
        assertEquals("", result.getPrimaryEmail());
    }

    @Test
    void toEntity_HandlesSpecialCharacters() {
        UserCreateRequestModel request = new UserCreateRequestModel();
        request.setFirstName("José");
        request.setLastName("O'Connor-McDonald");
        request.setPrimaryEmail("jose.oconnor@example.com");
        request.setPhone("+1 (514) 555-1234");
        request.setUserRole(UserRole.OWNER);

        Users result = UserMapper.toEntity(request);

        assertEquals("José", result.getFirstName());
        assertEquals("O'Connor-McDonald", result.getLastName());
        assertEquals("+1 (514) 555-1234", result.getPhone());
    }

    @Test
    void toResponseModel_PreservesSpecialCharacters() {
        UserIdentifier identifier = UserIdentifier.newId();
        Users user = new Users(
                identifier,
                "François",
                "Müller",
                "francois.muller@example.com",
                null,
                "+41 79 123 45 67",
                UserRole.CUSTOMER,
                "auth0|special",
                UserStatus.ACTIVE
        );

        UserResponseModel result = UserMapper.toResponseModel(user, null);

        assertEquals("François", result.getFirstName());
        assertEquals("Müller", result.getLastName());
        assertEquals("+41 79 123 45 67", result.getPhone());
    }
}

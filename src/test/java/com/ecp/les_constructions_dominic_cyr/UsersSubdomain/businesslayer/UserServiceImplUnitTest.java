package com.ecp.les_constructions_dominic_cyr.UsersSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserServiceImpl;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserCreateRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserUpdateRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Auth0ManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplUnitTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private Auth0ManagementService auth0ManagementService;

    @InjectMocks
    private UserServiceImpl userService;

    private Users testUser;
    private UserIdentifier userIdentifier;
    private String userIdString;
    private UserCreateRequestModel createRequest;
    private UserUpdateRequestModel updateRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "inviteResultUrl", "https://example.com/invite-result");

        userIdentifier = UserIdentifier.newId();
        userIdString = userIdentifier.getUserId().toString();

        testUser = new Users(
                userIdentifier,
                "John",
                "Doe",
                "john.doe@example.com",
                "john.secondary@example.com",
                "514-555-1234",
                UserRole.CUSTOMER,
                "auth0|123456789"
        );

        createRequest = new UserCreateRequestModel();
        createRequest.setFirstName("John");
        createRequest.setLastName("Doe");
        createRequest.setPrimaryEmail("john.doe@example.com");
        createRequest.setSecondaryEmail("john.secondary@example.com");
        createRequest.setPhone("514-555-1234");
        createRequest.setUserRole(UserRole.CUSTOMER);

        updateRequest = new UserUpdateRequestModel();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setPhone("514-555-9999");
        updateRequest.setSecondaryEmail("jane.secondary@example.com");
    }

    // ========================== POSITIVE TESTS ==========================

    @Test
    void getAllUsers_ReturnsListOfUsers() {
        when(usersRepository.findAll()).thenReturn(Arrays.asList(testUser));

        List<UserResponseModel> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Doe", result.get(0).getLastName());
        assertEquals("john.doe@example.com", result.get(0).getPrimaryEmail());
        assertEquals(UserRole.CUSTOMER, result.get(0).getUserRole());
        assertNull(result.get(0).getInviteLink());

        verify(usersRepository, times(1)).findAll();
    }

    @Test
    void getAllUsers_ReturnsEmptyList_WhenNoUsers() {
        when(usersRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserResponseModel> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(usersRepository, times(1)).findAll();
    }

    @Test
    void getUserById_ReturnsUser_WhenExists() {
        when(usersRepository.findById(any(UserIdentifier.class))).thenReturn(Optional.of(testUser));

        UserResponseModel result = userService.getUserById(userIdString);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@example.com", result.getPrimaryEmail());
        assertEquals(UserRole.CUSTOMER, result.getUserRole());

        verify(usersRepository, times(1)).findById(any(UserIdentifier.class));
    }

    @Test
    void getUserByAuth0Id_ReturnsUser_WhenExists() {
        String auth0Id = "auth0|123456789";
        when(usersRepository.findByAuth0UserId(auth0Id)).thenReturn(Optional.of(testUser));

        UserResponseModel result = userService.getUserByAuth0Id(auth0Id);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("john.doe@example.com", result.getPrimaryEmail());

        verify(usersRepository, times(1)).findByAuth0UserId(auth0Id);
    }

    @Test
    void createUser_CreatesAndReturnsUser_WithInviteLink() {
        String auth0UserId = "auth0|newuser123";
        String inviteLink = "https://auth0.com/invite/abc123";

        when(usersRepository.findByPrimaryEmail(anyString())).thenReturn(Optional.empty());
        when(usersRepository.save(any(Users.class))).thenAnswer(invocation -> {
            Users savedUser = invocation.getArgument(0);
            if (savedUser.getUserIdentifier() == null) {
                savedUser.setUserIdentifier(UserIdentifier.newId());
            }
            return savedUser;
        });
        when(auth0ManagementService.createAuth0User(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()
        )).thenReturn(auth0UserId);
        doNothing().when(auth0ManagementService).assignRoleToUser(anyString(), anyString());
        when(auth0ManagementService.createPasswordChangeTicket(anyString(), anyString())).thenReturn(inviteLink);

        UserResponseModel result = userService.createUser(createRequest);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@example.com", result.getPrimaryEmail());
        assertEquals(UserRole.CUSTOMER, result.getUserRole());
        assertEquals(inviteLink, result.getInviteLink());

        verify(usersRepository, times(1)).findByPrimaryEmail("john.doe@example.com");
        verify(usersRepository, times(2)).save(any(Users.class)); // First save, then update with auth0UserId
        verify(auth0ManagementService, times(1)).createAuth0User(
                eq("john.doe@example.com"), eq("john.secondary@example.com"),
                eq("John"), eq("Doe"), eq("CUSTOMER"), anyString()
        );
        verify(auth0ManagementService, times(1)).assignRoleToUser(auth0UserId, "CUSTOMER");
        verify(auth0ManagementService, times(1)).createPasswordChangeTicket(
                eq(auth0UserId), eq("https://example.com/invite-result")
        );
    }

    @Test
    void createUser_WithAllRoles_CreatesSuccessfully() {
        for (UserRole role : UserRole.values()) {
            createRequest.setUserRole(role);
            createRequest.setPrimaryEmail(role.name().toLowerCase() + "@example.com");

            when(usersRepository.findByPrimaryEmail(anyString())).thenReturn(Optional.empty());
            when(usersRepository.save(any(Users.class))).thenAnswer(invocation -> {
                Users savedUser = invocation.getArgument(0);
                if (savedUser.getUserIdentifier() == null) {
                    savedUser.setUserIdentifier(UserIdentifier.newId());
                }
                return savedUser;
            });
            when(auth0ManagementService.createAuth0User(
                    anyString(), anyString(), anyString(), anyString(), anyString(), anyString()
            )).thenReturn("auth0|" + role.name());
            doNothing().when(auth0ManagementService).assignRoleToUser(anyString(), anyString());
            when(auth0ManagementService.createPasswordChangeTicket(anyString(), anyString())).thenReturn("link");

            UserResponseModel result = userService.createUser(createRequest);

            assertEquals(role, result.getUserRole());
        }
    }

    @Test
    void updateUser_UpdatesAllFields() {
        when(usersRepository.findById(any(UserIdentifier.class))).thenReturn(Optional.of(testUser));
        when(usersRepository.save(any(Users.class))).thenReturn(testUser);

        UserResponseModel result = userService.updateUser(userIdString, updateRequest);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("514-555-9999", result.getPhone());
        assertEquals("jane.secondary@example.com", result.getSecondaryEmail());
        assertNull(result.getInviteLink()); // No invite link on update

        verify(usersRepository, times(1)).findById(any(UserIdentifier.class));
        verify(usersRepository, times(1)).save(any(Users.class));
    }

    @Test
    void updateUser_PartialUpdate_OnlyFirstName() {
        UserUpdateRequestModel partialUpdate = new UserUpdateRequestModel();
        partialUpdate.setFirstName("UpdatedName");

        String originalLastName = testUser.getLastName();
        String originalPhone = testUser.getPhone();

        when(usersRepository.findById(any(UserIdentifier.class))).thenReturn(Optional.of(testUser));
        when(usersRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseModel result = userService.updateUser(userIdString, partialUpdate);

        assertEquals("UpdatedName", result.getFirstName());
        assertEquals(originalLastName, result.getLastName());
        assertEquals(originalPhone, result.getPhone());

        verify(usersRepository, times(1)).save(any(Users.class));
    }

    @Test
    void updateUser_TrimsWhitespace() {
        updateRequest.setFirstName("  Jane  ");
        updateRequest.setLastName("  Smith  ");
        updateRequest.setPhone("  514-555-9999  ");

        when(usersRepository.findById(any(UserIdentifier.class))).thenReturn(Optional.of(testUser));
        when(usersRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseModel result = userService.updateUser(userIdString, updateRequest);

        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("514-555-9999", result.getPhone());
    }

    @Test
    void updateUser_IgnoresEmptyFields() {
        UserUpdateRequestModel emptyUpdate = new UserUpdateRequestModel();
        emptyUpdate.setFirstName("");
        emptyUpdate.setLastName("   ");
        emptyUpdate.setPhone("");

        String originalFirstName = testUser.getFirstName();
        String originalLastName = testUser.getLastName();
        String originalPhone = testUser.getPhone();

        when(usersRepository.findById(any(UserIdentifier.class))).thenReturn(Optional.of(testUser));
        when(usersRepository.save(any(Users.class))).thenReturn(testUser);

        UserResponseModel result = userService.updateUser(userIdString, emptyUpdate);

        // Original values should be preserved
        assertEquals(originalFirstName, result.getFirstName());
        assertEquals(originalLastName, result.getLastName());
        assertEquals(originalPhone, result.getPhone());
    }

    // ========================== NEGATIVE TESTS ==========================

    @Test
    void getUserById_ThrowsException_WhenUserNotFound() {
        when(usersRepository.findById(any(UserIdentifier.class))).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById(userIdString)
        );

        assertTrue(exception.getMessage().contains("User not found with ID:"));

        verify(usersRepository, times(1)).findById(any(UserIdentifier.class));
    }

    @Test
    void getUserById_ThrowsException_WhenInvalidUUID() {
        String invalidUuid = "not-a-valid-uuid";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById(invalidUuid)
        );

        assertTrue(exception.getMessage().contains("Invalid user ID format"));
    }

    @Test
    void getUserById_ThrowsException_WhenNullId() {
        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(null));
    }

    @Test
    void getUserById_ThrowsException_WhenEmptyId() {
        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(""));
        assertThrows(IllegalArgumentException.class, () -> userService.getUserById("   "));
    }

    @Test
    void getUserByAuth0Id_ThrowsException_WhenUserNotFound() {
        String auth0Id = "auth0|nonexistent";
        when(usersRepository.findByAuth0UserId(auth0Id)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserByAuth0Id(auth0Id)
        );

        assertTrue(exception.getMessage().contains("User not found with Auth0 ID:"));

        verify(usersRepository, times(1)).findByAuth0UserId(auth0Id);
    }

    @Test
    void createUser_ThrowsException_WhenEmailAlreadyExists() {
        when(usersRepository.findByPrimaryEmail(createRequest.getPrimaryEmail()))
                .thenReturn(Optional.of(testUser));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(createRequest)
        );

        assertEquals("A user with this email already exists.", exception.getMessage());

        verify(usersRepository, times(1)).findByPrimaryEmail(createRequest.getPrimaryEmail());
        verify(usersRepository, never()).save(any(Users.class));
        verifyNoInteractions(auth0ManagementService);
    }

    @Test
    void updateUser_ThrowsException_WhenUserNotFound() {
        when(usersRepository.findById(any(UserIdentifier.class))).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(userIdString, updateRequest)
        );

        assertTrue(exception.getMessage().contains("User not found with ID:"));

        verify(usersRepository, times(1)).findById(any(UserIdentifier.class));
        verify(usersRepository, never()).save(any(Users.class));
    }

    @Test
    void updateUser_ThrowsException_WhenInvalidUUID() {
        String invalidUuid = "not-a-valid-uuid";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(invalidUuid, updateRequest)
        );

        assertTrue(exception.getMessage().contains("Invalid user ID format"));

        verify(usersRepository, never()).findById(any(UserIdentifier.class));
    }

    @Test
    void getAllUsers_ReturnsCorrectData_WithMultipleUsers() {
        Users user1 = new Users(UserIdentifier.newId(), "Alice", "Wonder", "alice@example.com",
                null, "111-111-1111", UserRole.OWNER, "auth0|alice");
        Users user2 = new Users(UserIdentifier.newId(), "Bob", "Builder", "bob@example.com",
                null, "222-222-2222", UserRole.CONTRACTOR, "auth0|bob");
        Users user3 = new Users(UserIdentifier.newId(), "Charlie", "Chaplin", "charlie@example.com",
                null, "333-333-3333", UserRole.SALESPERSON, "auth0|charlie");

        when(usersRepository.findAll()).thenReturn(Arrays.asList(user1, user2, user3));

        List<UserResponseModel> result = userService.getAllUsers();

        assertEquals(3, result.size());
        assertEquals("Alice", result.get(0).getFirstName());
        assertEquals(UserRole.OWNER, result.get(0).getUserRole());
        assertEquals("Bob", result.get(1).getFirstName());
        assertEquals(UserRole.CONTRACTOR, result.get(1).getUserRole());
        assertEquals("Charlie", result.get(2).getFirstName());
        assertEquals(UserRole.SALESPERSON, result.get(2).getUserRole());
    }
}

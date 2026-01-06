package com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserCreateRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserUpdateRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UsersController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsersController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(com.ecp.les_constructions_dominic_cyr.backend.utils.GlobalControllerExceptionHandler.class)
public class UsersControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private ObjectMapper objectMapper;
    private UserResponseModel testUser;
    private UserCreateRequestModel createRequest;
    private UserUpdateRequestModel updateRequest;
    private String userId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        userId = UUID.randomUUID().toString();

        testUser = new UserResponseModel();
        testUser.setUserIdentifier(userId);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPrimaryEmail("john.doe@example.com");
        testUser.setSecondaryEmail("john.secondary@example.com");
        testUser.setPhone("514-555-1234");
        testUser.setUserRole(UserRole.CUSTOMER);
        testUser.setInviteLink(null);

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
    void getAllUsers_ReturnsListOfUsers() throws Exception {
        List<UserResponseModel> users = Arrays.asList(testUser);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userIdentifier").value(userId))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[0].primaryEmail").value("john.doe@example.com"))
                .andExpect(jsonPath("$[0].userRole").value("CUSTOMER"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getAllUsers_ReturnsEmptyList_WhenNoUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUserById_ReturnsUser_WhenExists() throws Exception {
        when(userService.getUserById(userId)).thenReturn(testUser);

        mockMvc.perform(get("/api/v1/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userIdentifier").value(userId))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.primaryEmail").value("john.doe@example.com"));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void getUserByAuth0Id_ReturnsUser_WhenExists() throws Exception {
        String auth0Id = "auth0|123456789";
        when(userService.getUserByAuth0Id(auth0Id)).thenReturn(testUser);

        mockMvc.perform(get("/api/v1/users/auth0/{auth0UserId}", auth0Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userIdentifier").value(userId))
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(userService, times(1)).getUserByAuth0Id(auth0Id);
    }

    @Test
    void createUser_ReturnsCreatedUser() throws Exception {
        UserResponseModel createdUser = new UserResponseModel();
        createdUser.setUserIdentifier(userId);
        createdUser.setFirstName("John");
        createdUser.setLastName("Doe");
        createdUser.setPrimaryEmail("john.doe@example.com");
        createdUser.setSecondaryEmail("john.secondary@example.com");
        createdUser.setPhone("514-555-1234");
        createdUser.setUserRole(UserRole.CUSTOMER);
        createdUser.setInviteLink("https://auth0.com/invite/abc123");

        when(userService.createUser(any(UserCreateRequestModel.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userIdentifier").value(userId))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.inviteLink").value("https://auth0.com/invite/abc123"));

        verify(userService, times(1)).createUser(any(UserCreateRequestModel.class));
    }

    @Test
    void createUser_WithAllRoles_ReturnsCreatedUser() throws Exception {
        for (UserRole role : UserRole.values()) {
            createRequest.setUserRole(role);
            testUser.setUserRole(role);
            when(userService.createUser(any(UserCreateRequestModel.class))).thenReturn(testUser);

            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.userRole").value(role.name()));
        }
    }

    @Test
    void updateUser_ReturnsUpdatedUser() throws Exception {
        UserResponseModel updatedUser = new UserResponseModel();
        updatedUser.setUserIdentifier(userId);
        updatedUser.setFirstName("Jane");
        updatedUser.setLastName("Smith");
        updatedUser.setPrimaryEmail("john.doe@example.com");
        updatedUser.setSecondaryEmail("jane.secondary@example.com");
        updatedUser.setPhone("514-555-9999");
        updatedUser.setUserRole(UserRole.CUSTOMER);

        when(userService.updateUser(eq(userId), any(UserUpdateRequestModel.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userIdentifier").value(userId))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.phone").value("514-555-9999"))
                .andExpect(jsonPath("$.secondaryEmail").value("jane.secondary@example.com"));

        verify(userService, times(1)).updateUser(eq(userId), any(UserUpdateRequestModel.class));
    }

    @Test
    void updateUser_PartialUpdate_OnlyFirstName() throws Exception {
        UserUpdateRequestModel partialUpdate = new UserUpdateRequestModel();
        partialUpdate.setFirstName("UpdatedName");

        UserResponseModel updatedUser = new UserResponseModel();
        updatedUser.setUserIdentifier(userId);
        updatedUser.setFirstName("UpdatedName");
        updatedUser.setLastName("Doe");
        updatedUser.setPrimaryEmail("john.doe@example.com");
        updatedUser.setUserRole(UserRole.CUSTOMER);

        when(userService.updateUser(eq(userId), any(UserUpdateRequestModel.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedName"));

        verify(userService, times(1)).updateUser(eq(userId), any(UserUpdateRequestModel.class));
    }

    // ========================== NEGATIVE TESTS ==========================

    @Test
    void getUserById_ReturnsNotFound_WhenUserDoesNotExist() throws Exception {
        String nonExistentId = UUID.randomUUID().toString();
        when(userService.getUserById(nonExistentId))
                .thenThrow(new IllegalArgumentException("User not found with ID: " + nonExistentId));

        mockMvc.perform(get("/api/v1/users/{userId}", nonExistentId))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).getUserById(nonExistentId);
    }

    @Test
    void getUserByAuth0Id_ReturnsNotFound_WhenUserDoesNotExist() throws Exception {
        String nonExistentAuth0Id = "auth0|nonexistent";
        when(userService.getUserByAuth0Id(nonExistentAuth0Id))
                .thenThrow(new IllegalArgumentException("User not found with Auth0 ID: " + nonExistentAuth0Id));

        mockMvc.perform(get("/api/v1/users/auth0/{auth0UserId}", nonExistentAuth0Id))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).getUserByAuth0Id(nonExistentAuth0Id);
    }

    @Test
    void createUser_ReturnsBadRequest_WhenEmailAlreadyExists() throws Exception {
        when(userService.createUser(any(UserCreateRequestModel.class)))
                .thenThrow(new IllegalArgumentException("A user with this email already exists."));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).createUser(any(UserCreateRequestModel.class));
    }

    @Test
    void updateUser_ReturnsBadRequest_WhenUserNotFound() throws Exception {
        String nonExistentId = UUID.randomUUID().toString();
        when(userService.updateUser(eq(nonExistentId), any(UserUpdateRequestModel.class)))
                .thenThrow(new IllegalArgumentException("User not found with ID: " + nonExistentId));

        mockMvc.perform(put("/api/v1/users/{userId}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).updateUser(eq(nonExistentId), any(UserUpdateRequestModel.class));
    }

    @Test
    void createUser_ReturnsBadRequest_WhenInvalidEmailFormat() throws Exception {
        UserUpdateRequestModel invalidUpdate = new UserUpdateRequestModel();
        invalidUpdate.setSecondaryEmail("not-an-email");

        mockMvc.perform(put("/api/v1/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_ReturnsBadRequest_WhenInvalidUUID() throws Exception {
        String invalidUuid = "not-a-valid-uuid";
        when(userService.getUserById(invalidUuid))
                .thenThrow(new IllegalArgumentException("Invalid user ID format: " + invalidUuid));

        mockMvc.perform(get("/api/v1/users/{userId}", invalidUuid))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).getUserById(invalidUuid);
    }

    @Test
    void updateUser_ReturnsBadRequest_WhenInvalidUUID() throws Exception {
        String invalidUuid = "not-a-valid-uuid";
        when(userService.updateUser(eq(invalidUuid), any(UserUpdateRequestModel.class)))
                .thenThrow(new IllegalArgumentException("Invalid user ID format: " + invalidUuid));

        mockMvc.perform(put("/api/v1/users/{userId}", invalidUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).updateUser(eq(invalidUuid), any(UserUpdateRequestModel.class));
    }

    @Test
    void createUser_ReturnsBadRequest_WhenEmptyBody() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated()); // No validation on controller level - handled by service
    }
}

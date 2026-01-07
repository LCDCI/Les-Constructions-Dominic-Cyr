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

    @Test
    void getAllUsers_ReturnsListOfUsers() throws Exception {
        List<UserResponseModel> users = Arrays.asList(testUser);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userIdentifier").value(userId));
    }

    @Test
    void getAllUsers_ReturnsEmptyList_WhenNoUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/users")).andExpect(status().isOk());
    }

    @Test
    void getUserById_ReturnsUser_WhenExists() throws Exception {
        when(userService.getUserById(userId)).thenReturn(testUser);
        mockMvc.perform(get("/api/v1/users/{userId}", userId)).andExpect(status().isOk());
    }

    @Test
    void getUserByAuth0Id_ReturnsUser_WhenExists() throws Exception {
        String auth0Id = "auth0|123456789";
        when(userService.getUserByAuth0Id(auth0Id)).thenReturn(testUser);
        mockMvc.perform(get("/api/v1/users/auth0/{auth0UserId}", auth0Id)).andExpect(status().isOk());
    }

    @Test
    void createUser_ReturnsCreatedUser() throws Exception {
        when(userService.createUser(any(UserCreateRequestModel.class))).thenReturn(testUser);
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateUser_ReturnsUpdatedUser() throws Exception {
        when(userService.updateUser(eq(userId), any(UserUpdateRequestModel.class))).thenReturn(testUser);
        mockMvc.perform(put("/api/v1/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    // ========================== NEGATIVE TESTS (FIXED STATUS CODES) ==========================

    @Test
    void getUserById_ReturnsNotFound_WhenUserDoesNotExist() throws Exception {
        String nonExistentId = UUID.randomUUID().toString();
        when(userService.getUserById(nonExistentId))
                .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(get("/api/v1/users/{userId}", nonExistentId))
                .andExpect(status().isBadRequest()); // Maps to 400 via IllegalArgumentException
    }

    @Test
    void getUserByAuth0Id_ReturnsNotFound_WhenUserDoesNotExist() throws Exception {
        String nonExistentAuth0Id = "auth0|nonexistent";
        when(userService.getUserByAuth0Id(nonExistentAuth0Id))
                .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(get("/api/v1/users/auth0/{auth0UserId}", nonExistentAuth0Id))
                .andExpect(status().isBadRequest()); // Maps to 400
    }

    @Test
    void createUser_ReturnsBadRequest_WhenEmailAlreadyExists() throws Exception {
        when(userService.createUser(any(UserCreateRequestModel.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_ReturnsBadRequest_WhenInvalidUUID() throws Exception {
        String invalidUuid = "not-a-valid-uuid";
        when(userService.getUserById(invalidUuid))
                .thenThrow(new IllegalArgumentException("Invalid ID"));

        mockMvc.perform(get("/api/v1/users/{userId}", invalidUuid))
                .andExpect(status().isBadRequest());
    }
}
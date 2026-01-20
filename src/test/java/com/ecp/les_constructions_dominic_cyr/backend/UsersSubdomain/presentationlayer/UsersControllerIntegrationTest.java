package com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.*;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserCreateRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserUpdateRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Auth0ManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersPostgresConfig.class)
class UsersControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private Auth0ManagementService auth0ManagementService;

    private final String BASE_URI = "/api/v1/users";
    private final SimpleGrantedAuthority OWNER_ROLE = new SimpleGrantedAuthority("ROLE_OWNER");
    private String existingUserId;

    @BeforeEach
    void setUp() {
        usersRepository.deleteAll();

        UserIdentifier identifier = UserIdentifier.newId();
        existingUserId = identifier.getUserId().toString();
        Users existingUser = new Users(
                identifier, "Existing", "User", "existing.user@example.com",
                "existing.secondary@example.com", "514-555-0000", UserRole.CUSTOMER, "auth0|existing123", UserStatus.ACTIVE
        );
        usersRepository.save(existingUser);

        when(auth0ManagementService.createAuth0User(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("auth0|newuser");
        when(auth0ManagementService.createPasswordChangeTicket(anyString(), anyString()))
                .thenReturn("https://auth0.com/invite/testlink");
    }

    @Test
    void getAllUsers_ReturnsAllUsers() throws Exception {
        mockMvc.perform(get(BASE_URI).with(jwt().authorities(OWNER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Existing"));
    }

    @Test
    void getUserById_ReturnsBadRequest_WhenUserNotFound() throws Exception {
        mockMvc.perform(get(BASE_URI + "/" + UUID.randomUUID())
                .with(jwt().authorities(OWNER_ROLE)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deactivateUser_DeactivatesSuccessfully() throws Exception {
        UserIdentifier ownerIdentifier = UserIdentifier. newId();
        Users ownerUser = new Users(
                ownerIdentifier, "Owner", "User", "owner@example.com",
                null, null, UserRole.OWNER, "auth0|owner123", UserStatus.ACTIVE
        );
        usersRepository.save(ownerUser);

        doNothing().when(auth0ManagementService).blockAuth0User(anyString(), eq(true));

        mockMvc.perform(patch(BASE_URI + "/" + existingUserId + "/deactivate")
                        .with(jwt().jwt(jwt -> jwt.subject("auth0|owner123")).authorities(OWNER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userStatus").value("DEACTIVATED"));

        verify(auth0ManagementService, times(1)).blockAuth0User(eq("auth0|existing123"), eq(true));
    }

    @Test
    void reactivateUser_ReactivatesSuccessfully() throws Exception {
        UserIdentifier ownerIdentifier = UserIdentifier.newId();
        Users ownerUser = new Users(
                ownerIdentifier, "Owner", "User", "owner@example.com",
                null, null, UserRole. OWNER, "auth0|owner123", UserStatus.ACTIVE
        );
        usersRepository.save(ownerUser);

        // Set user to deactivated first
        Users userToReactivate = usersRepository.findById(UserIdentifier.fromString(existingUserId)).get();
        userToReactivate.setUserStatus(UserStatus.DEACTIVATED);
        usersRepository.save(userToReactivate);

        doNothing().when(auth0ManagementService).blockAuth0User(anyString(), eq(false));

        mockMvc.perform(patch(BASE_URI + "/" + existingUserId + "/reactivate")
                        .with(jwt().jwt(jwt -> jwt.subject("auth0|owner123")).authorities(OWNER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userStatus").value("ACTIVE"));

        verify(auth0ManagementService, times(1)).blockAuth0User(eq("auth0|existing123"), eq(false));
    }


    @Test
    void deactivateUser_ThrowsForbidden_WhenNotOwner() throws Exception {
        SimpleGrantedAuthority customerRole = new SimpleGrantedAuthority("ROLE_CUSTOMER");

        mockMvc.perform(patch(BASE_URI + "/" + existingUserId + "/deactivate")
                        .with(jwt().jwt(jwt -> jwt.subject("auth0|customer123")).authorities(customerRole)))
                .andExpect(status().isForbidden());
    }
}
package com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
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
import static org.mockito.Mockito.when;
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
                "existing.secondary@example.com", "514-555-0000", UserRole.CUSTOMER, "auth0|existing123"
        );
        usersRepository.save(existingUser);

        when(auth0ManagementService.createAuth0User(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("auth0|newuser");
        when(auth0ManagementService.createPasswordChangeTicket(anyString(), anyString()))
                .thenReturn("https://auth0.com/invite/testlink");
    }

    //@Test
    void getAllUsers_ReturnsAllUsers() throws Exception {
        mockMvc.perform(get(BASE_URI).with(jwt().authorities(OWNER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Existing"));
    }

    //@Test
    void createUser_CreatesAndReturnsUser() throws Exception {
        UserCreateRequestModel request = new UserCreateRequestModel();
        request.setFirstName("New");
        request.setLastName("Customer");
        request.setPrimaryEmail("new.customer@example.com");
        request.setUserRole(UserRole.CUSTOMER);

        mockMvc.perform(post(BASE_URI)
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.inviteLink").isNotEmpty());

        assertEquals(2, usersRepository.count());
    }

    //@Test
    void getUserById_ReturnsBadRequest_WhenUserNotFound() throws Exception {
        mockMvc.perform(get(BASE_URI + "/" + UUID.randomUUID())
                        .with(jwt().authorities(OWNER_ROLE)))
                .andExpect(status().isBadRequest());
    }
}
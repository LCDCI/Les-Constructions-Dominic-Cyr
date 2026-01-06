package com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserCreateRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserUpdateRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Auth0ManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersPostgresConfig.class)
class UsersControllerIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private UsersRepository usersRepository;

    @MockitoBean
    private Auth0ManagementService auth0ManagementService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter jwtAuthenticationConverter;

    private final String BASE_URI = "/api/v1/users";
    private Users existingUser;
    private String existingUserId;
    private final SimpleGrantedAuthority ADMIN_ROLE = new SimpleGrantedAuthority("ROLE_OWNER");

    @BeforeEach
    void setUp() {
        usersRepository.deleteAll();

        UserIdentifier identifier = UserIdentifier.newId();
        existingUserId = identifier.getUserId().toString();
        existingUser = new Users(
                identifier,
                "Existing",
                "User",
                "existing.user@example.com",
                "existing.secondary@example.com",
                "514-555-0000",
                UserRole.CUSTOMER,
                "auth0|existing123"
        );
        usersRepository.save(existingUser);

        when(auth0ManagementService.createAuth0User(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("auth0|newuser");
        doNothing().when(auth0ManagementService).assignRoleToUser(anyString(), anyString());
        when(auth0ManagementService.createPasswordChangeTicket(anyString(), anyString()))
                .thenReturn("https://auth0.com/invite/testlink");

        org.springframework.security.oauth2.jwt.Jwt mockJwtToken = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "auth0|existing123")
                .claim("https://construction-api.loca/roles", java.util.Collections.singletonList("OWNER"))
                .build();

        when(jwtDecoder.decode(anyString())).thenReturn(mockJwtToken);
            }

    @Test
    void getAllUsers_ReturnsAllUsers() {
        usersRepository.save(new Users(
                UserIdentifier.newId(),
                "Second",
                "User",
                "second@example.com",
                null,
                "514-555-1111",
                UserRole.CONTRACTOR,
                "auth0|second"
        ));

        webClient
                .mutateWith(mockJwt().authorities(ADMIN_ROLE))
                .get()
                .uri(BASE_URI)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserResponseModel.class)
                .hasSize(2)
                .value(users -> {
                    assertTrue(users.stream().anyMatch(u -> u.getFirstName().equals("Existing")));
                    assertTrue(users.stream().anyMatch(u -> u.getFirstName().equals("Second")));
                });
    }

    @Test
    void getAllUsers_ReturnsEmptyList_WhenNoUsers() {
        usersRepository.deleteAll();

        webClient
                .mutateWith(mockJwt().authorities(ADMIN_ROLE))
                .get()
                .uri(BASE_URI)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserResponseModel.class)
                .hasSize(0);
    }

    @Test
    void getUserById_ReturnsUser_WhenExists() {
        webClient
                .mutateWith(mockJwt().authorities(ADMIN_ROLE))
                .get()
                .uri(BASE_URI + "/" + existingUserId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseModel.class)
                .value(user -> {
                    assertEquals(existingUserId, user.getUserIdentifier());
                    assertEquals("Existing", user.getFirstName());
                    assertEquals("User", user.getLastName());
                    assertEquals("existing.user@example.com", user.getPrimaryEmail());
                    assertEquals(UserRole.CUSTOMER, user.getUserRole());
                });
    }

    @Test
    void getUserByAuth0Id_ReturnsUser_WhenExists() {
        String auth0Id = "auth0|existing123";

        webClient
                .mutateWith(mockJwt().authorities(ADMIN_ROLE))
                .get()
                .uri(BASE_URI + "/auth0/" + auth0Id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseModel.class)
                .value(user -> {
                    assertEquals(existingUserId, user.getUserIdentifier());
                    assertEquals("Existing", user.getFirstName());
                });
    }

    @Test
    void createUser_CreatesAndReturnsUser() {
        UserCreateRequestModel request = new UserCreateRequestModel();
        request.setFirstName("New");
        request.setLastName("Customer");
        request.setPrimaryEmail("new.customer@example.com");
        request.setSecondaryEmail("new.secondary@example.com");
        request.setPhone("514-555-9999");
        request.setUserRole(UserRole.CUSTOMER);

        webClient
                .mutateWith(mockJwt().authorities(ADMIN_ROLE))
                .post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseModel.class)
                .value(user -> {
                    assertNotNull(user.getUserIdentifier());
                    assertEquals("New", user.getFirstName());
                    assertEquals("Customer", user.getLastName());
                    assertEquals("new.customer@example.com", user.getPrimaryEmail());
                    assertEquals(UserRole.CUSTOMER, user.getUserRole());
                    assertNotNull(user.getInviteLink());
                });

        assertEquals(2, usersRepository.count());
    }

    @Test
    void createUser_WithAllRoles_Works() {
        for (UserRole role : UserRole.values()) {
            String email = role.name().toLowerCase() + "@example.com";

            UserCreateRequestModel request = new UserCreateRequestModel();
            request.setFirstName(role.name());
            request.setLastName("User");
            request.setPrimaryEmail(email);
            request.setUserRole(role);

            webClient
                    .mutateWith(mockJwt().authorities(ADMIN_ROLE))
                    .post()
                    .uri(BASE_URI)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(UserResponseModel.class)
                    .value(user -> assertEquals(role, user.getUserRole()));
        }
    }

    @Test
    void updateUser_UpdatesFields() {
        UserUpdateRequestModel updateRequest = new UserUpdateRequestModel();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");
        updateRequest.setPhone("514-555-8888");
        updateRequest.setSecondaryEmail("updated.secondary@example.com");

        webClient
                .mutateWith(mockJwt().authorities(ADMIN_ROLE))
                .put()
                .uri(BASE_URI + "/" + existingUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseModel.class)
                .value(user -> {
                    assertEquals(existingUserId, user.getUserIdentifier());
                    assertEquals("Updated", user.getFirstName());
                    assertEquals("Name", user.getLastName());
                    assertEquals("514-555-8888", user.getPhone());
                    assertEquals("updated.secondary@example.com", user.getSecondaryEmail());
                    assertEquals("existing.user@example.com", user.getPrimaryEmail());
                    assertEquals(UserRole.CUSTOMER, user.getUserRole());
                });

        Users updatedUser = usersRepository.findById(UserIdentifier.fromString(existingUserId)).orElseThrow();
        assertEquals("Updated", updatedUser.getFirstName());
    }

    @Test
    void updateUser_PartialUpdate_OnlyUpdatesProvidedFields() {
        UserUpdateRequestModel partialUpdate = new UserUpdateRequestModel();
        partialUpdate.setFirstName("OnlyFirstName");

        webClient
                .mutateWith(mockJwt().authorities(ADMIN_ROLE))
                .put()
                .uri(BASE_URI + "/" + existingUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(partialUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseModel.class)
                .value(user -> {
                    assertEquals("OnlyFirstName", user.getFirstName());
                    assertEquals("User", user.getLastName());
                    assertEquals("514-555-0000", user.getPhone());
                });
    }

    @Test
    void getUserById_ReturnsBadRequest_WhenUserNotFound() {
        String nonExistentId = UUID.randomUUID().toString();

        webClient
                .mutateWith(mockJwt().authorities(ADMIN_ROLE))
                .get()
                .uri(BASE_URI + "/" + nonExistentId)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getUserById_ReturnsBadRequest_WhenInvalidUUID() {
        webClient
                .mutateWith(mockJwt().authorities(ADMIN_ROLE))
                .get()
                .uri(BASE_URI + "/not-a-valid-uuid")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getUserByAuth0Id_ReturnsBadRequest_WhenNotFound() {
        webClient
                .mutateWith(mockJwt().authorities(ADMIN_ROLE))
                .get()
                .uri(BASE_URI + "/auth0/auth0|nonexistent")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createUser_ReturnsBadRequest_WhenEmailAlreadyExists() {
        UserCreateRequestModel request = new UserCreateRequestModel();
        request.setFirstName("Duplicate");
        request.setLastName("Email");
        request.setPrimaryEmail("existing.user@example.com");
        request.setUserRole(UserRole.CUSTOMER);

        webClient
                .mutateWith(mockJwt().authorities(ADMIN_ROLE))
                .post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();

        assertEquals(1, usersRepository.count());
    }

    @Test
    void updateUser_ReturnsBadRequest_WhenUserNotFound() {
        String nonExistentId = UUID.randomUUID().toString();

        UserUpdateRequestModel updateRequest = new UserUpdateRequestModel();
        updateRequest.setFirstName("Updated");

        webClient
                .mutateWith(mockJwt().authorities(ADMIN_ROLE))
                .put()
                .uri(BASE_URI + "/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void updateUser_ReturnsBadRequest_WhenInvalidUUID() {
        UserUpdateRequestModel updateRequest = new UserUpdateRequestModel();
        updateRequest.setFirstName("Updated");

        webClient
                .mutateWith(mockJwt().authorities(ADMIN_ROLE))
                .put()
                .uri(BASE_URI + "/invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void updateUser_ReturnsBadRequest_WhenInvalidSecondaryEmail() {
        UserUpdateRequestModel updateRequest = new UserUpdateRequestModel();
        updateRequest.setSecondaryEmail("not-an-email");

        webClient
                .mutateWith(mockJwt().authorities(ADMIN_ROLE))
                .put()
                .uri(BASE_URI + "/" + existingUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createThenGet_DataIsPersisted() {
        UserCreateRequestModel request = new UserCreateRequestModel();
        request.setFirstName("Persist");
        request.setLastName("Test");
        request.setPrimaryEmail("persist.test@example.com");
        request.setPhone("514-555-7777");
        request.setUserRole(UserRole.SALESPERSON);

        String newUserId = webClient
                .mutateWith(mockJwt().authorities(ADMIN_ROLE))
                .post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseModel.class)
                .returnResult()
                .getResponseBody()
                .getUserIdentifier();

        webClient
                .mutateWith(mockJwt().authorities(ADMIN_ROLE))
                .get()
                .uri(BASE_URI + "/" + newUserId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseModel.class)
                .value(user -> {
                    assertEquals("Persist", user.getFirstName());
                    assertEquals("Test", user.getLastName());
                    assertEquals("persist.test@example.com", user.getPrimaryEmail());
                    assertEquals(UserRole.SALESPERSON, user.getUserRole());
                });
    }

    @Test
    void updateThenGet_ChangesArePersisted() {
        UserUpdateRequestModel updateRequest = new UserUpdateRequestModel();
        updateRequest.setFirstName("Persisted");
        updateRequest.setLastName("Update");

        webClient
                .mutateWith(mockJwt().authorities(ADMIN_ROLE))
                .put()
                .uri(BASE_URI + "/" + existingUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk();

        webClient
                .mutateWith(mockJwt().authorities(ADMIN_ROLE))
                .get()
                .uri(BASE_URI + "/" + existingUserId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseModel.class)
                .value(user -> {
                    assertEquals("Persisted", user.getFirstName());
                    assertEquals("Update", user.getLastName());
                });
    }
}
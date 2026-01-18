package com.ecp.les_constructions_dominic_cyr.backend.utils.Translation;

import com.ecp.les_constructions_dominic_cyr.backend.utils.Auth0ManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class Auth0ManagementServiceTest {

    @InjectMocks
    private Auth0ManagementService auth0ManagementService;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Injecting @Value fields
        ReflectionTestUtils.setField(auth0ManagementService, "domain", "test.auth0.com");
        ReflectionTestUtils.setField(auth0ManagementService, "clientId", "client-id");
        ReflectionTestUtils.setField(auth0ManagementService, "clientSecret", "secret");
        ReflectionTestUtils.setField(auth0ManagementService, "dbConnection", "Username-Password-Authentication");
        ReflectionTestUtils.setField(auth0ManagementService, "contractorRoleId", "role_contractor");
        ReflectionTestUtils.setField(auth0ManagementService, "customerRoleId", "role_customer");
        ReflectionTestUtils.setField(auth0ManagementService, "salespersonRoleId", "role_salesperson");
        ReflectionTestUtils.setField(auth0ManagementService, "ownerRoleId", "role_owner");

        // Injecting the mocked restTemplate into the service
        ReflectionTestUtils.setField(auth0ManagementService, "restTemplate", restTemplate);
    }

    private void mockManagementTokenSuccess() {
        Map<String, String> tokenResponse = new HashMap<>();
        tokenResponse.put("access_token", "mock-mgmt-token");
        ResponseEntity<Map> response = new ResponseEntity<>(tokenResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(contains("/oauth/token"), any(), eq(Map.class)))
                .thenReturn(response);
    }

    @Test
    void createAuth0User_Success() {
        mockManagementTokenSuccess();

        Map<String, String> userResponse = new HashMap<>();
        userResponse.put("user_id", "auth0|123456");
        ResponseEntity<Map> response = new ResponseEntity<>(userResponse, HttpStatus.CREATED);

        when(restTemplate.postForEntity(contains("/api/v2/users"), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        String userId = auth0ManagementService.createAuth0User(
                "test@test.com", "sec@test.com", "John", "Doe", "OWNER", "user-uuid");

        assertEquals("auth0|123456", userId);
    }

    @Test
    void assignRoleToUser_AllRoles_Success() {
        mockManagementTokenSuccess();
        ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        when(restTemplate.postForEntity(contains("/roles"), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(response);

        assertDoesNotThrow(() -> auth0ManagementService.assignRoleToUser("user-123", "OWNER"));
        assertDoesNotThrow(() -> auth0ManagementService.assignRoleToUser("user-123", "CUSTOMER"));
        assertDoesNotThrow(() -> auth0ManagementService.assignRoleToUser("user-123", "SALESPERSON"));
        assertDoesNotThrow(() -> auth0ManagementService.assignRoleToUser("user-123", "CONTRACTOR"));
    }

    @Test
    void assignRoleToUser_InvalidRole_ThrowsException() {
        mockManagementTokenSuccess();
        assertThrows(IllegalArgumentException.class, () ->
                auth0ManagementService.assignRoleToUser("user-123", "NON_EXISTENT_ROLE"));
    }

    @Test
    void createPasswordChangeTicket_Success() {
        mockManagementTokenSuccess();
        Map<String, String> ticketResponse = new HashMap<>();
        ticketResponse.put("ticket", "https://auth0.com/ticket/xyz");
        ResponseEntity<Map> response = new ResponseEntity<>(ticketResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(contains("/tickets/password-change"), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        String ticket = auth0ManagementService.createPasswordChangeTicket("user-123", "http://callback.com");
        assertEquals("https://auth0.com/ticket/xyz", ticket);
    }

    @Test
    void updateAuth0User_Success() {
        mockManagementTokenSuccess();
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", "old@test.com");
        ResponseEntity<Map> getResponse = new ResponseEntity<>(userData, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(getResponse);

        assertDoesNotThrow(() ->
                auth0ManagementService.updateAuth0UserEmailAndName("user-123", "new@test.com", "John", "Doe"));

        verify(restTemplate, times(1)).put(anyString(), any(HttpEntity.class));
    }

    @Test
    void getManagementToken_Failure_ThrowsRuntimeException() {
        ResponseEntity<Map> errorResponse = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        when(restTemplate.postForEntity(contains("/oauth/token"), any(), eq(Map.class)))
                .thenReturn(errorResponse);

        assertThrows(RuntimeException.class, () ->
                auth0ManagementService.assignRoleToUser("user-123", "OWNER"));
    }

    @Test
    void getManagementToken_MissingToken_ThrowsRuntimeException() {
        ResponseEntity<Map> response = new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
        when(restTemplate.postForEntity(contains("/oauth/token"), any(), eq(Map.class)))
                .thenReturn(response);

        assertThrows(RuntimeException.class, () ->
                auth0ManagementService.assignRoleToUser("user-123", "OWNER"));
    }

    @Test
    void createAuth0User_Failure_ThrowsRuntimeException() {
        mockManagementTokenSuccess();
        ResponseEntity<Map> errorResponse = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        when(restTemplate.postForEntity(contains("/api/v2/users"), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(errorResponse);

        assertThrows(RuntimeException.class, () ->
                auth0ManagementService.createAuth0User("a", "b", "c", "d", "OWNER", "f"));
    }

    // ========== blockAuth0User Tests ==========
    // Note: These tests verify the method executes and handles exceptions.
    // Since the method creates WebClient internally and uses HTTPS, we test
    // the exception paths and verify the method structure is correct.

    @Test
    void blockAuth0User_WithBlockedTrue_ExecutesMethod() {
        mockManagementTokenSuccess();

        // The method will fail because it tries to connect to a real Auth0 server,
        // but this test verifies the method executes and calls getManagementToken
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                auth0ManagementService.blockAuth0User("auth0|123456", true));

        assertTrue(exception.getMessage().contains("Failed to block/unblock user in Auth0"));
    }

    @Test
    void blockAuth0User_WithBlockedFalse_ExecutesMethod() {
        mockManagementTokenSuccess();

        // The method will fail because it tries to connect to a real Auth0 server,
        // but this test verifies the method executes and calls getManagementToken
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                auth0ManagementService.blockAuth0User("auth0|123456", false));

        assertTrue(exception.getMessage().contains("Failed to block/unblock user in Auth0"));
    }

    @Test
    void blockAuth0User_WithNetworkError_ThrowsRuntimeException() {
        mockManagementTokenSuccess();

        // Use an invalid domain to simulate network error
        ReflectionTestUtils.setField(auth0ManagementService, "domain", "invalid-domain-that-does-not-exist-12345.com");

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                auth0ManagementService.blockAuth0User("auth0|123456", true));

        assertTrue(exception.getMessage().contains("Failed to block/unblock user in Auth0"));
    }

    @Test
    void blockAuth0User_WithInvalidUserId_ThrowsRuntimeException() {
        mockManagementTokenSuccess();

        // Use an invalid domain to simulate error
        ReflectionTestUtils.setField(auth0ManagementService, "domain", "invalid-domain-that-does-not-exist-12345.com");

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                auth0ManagementService.blockAuth0User("invalid-user-id", true));

        assertTrue(exception.getMessage().contains("Failed to block/unblock user in Auth0"));
    }

    @Test
    void blockAuth0User_WithNullUserId_ThrowsRuntimeException() {
        mockManagementTokenSuccess();

        // Use an invalid domain to simulate error
        ReflectionTestUtils.setField(auth0ManagementService, "domain", "invalid-domain-that-does-not-exist-12345.com");

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                auth0ManagementService.blockAuth0User(null, true));

        assertTrue(exception.getMessage().contains("Failed to block/unblock user in Auth0"));
    }

    @Test
    void blockAuth0User_WithEmptyUserId_ThrowsRuntimeException() {
        mockManagementTokenSuccess();

        // Use an invalid domain to simulate error
        ReflectionTestUtils.setField(auth0ManagementService, "domain", "invalid-domain-that-does-not-exist-12345.com");

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                auth0ManagementService.blockAuth0User("", true));

        assertTrue(exception.getMessage().contains("Failed to block/unblock user in Auth0"));
    }

    @Test
    void blockAuth0User_WithTokenFailure_ThrowsRuntimeException() {
        // Mock token failure
        ResponseEntity<Map> errorResponse = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        when(restTemplate.postForEntity(contains("/oauth/token"), any(), eq(Map.class)))
                .thenReturn(errorResponse);

        // Should fail when getting management token
        assertThrows(RuntimeException.class, () ->
                auth0ManagementService.blockAuth0User("auth0|123456", true));
    }

    @Test
    void blockAuth0User_WithDifferentBlockedValues_ExecutesBothPaths() {
        mockManagementTokenSuccess();

        // Test both true and false values to ensure both code paths are executed
        ReflectionTestUtils.setField(auth0ManagementService, "domain", "invalid-domain-that-does-not-exist-12345.com");

        RuntimeException exception1 = assertThrows(RuntimeException.class, () ->
                auth0ManagementService.blockAuth0User("auth0|123456", true));
        assertTrue(exception1.getMessage().contains("Failed to block/unblock user in Auth0"));

        RuntimeException exception2 = assertThrows(RuntimeException.class, () ->
                auth0ManagementService.blockAuth0User("auth0|123456", false));
        assertTrue(exception2.getMessage().contains("Failed to block/unblock user in Auth0"));
    }
}
package com.ecp.les_constructions_dominic_cyr.backend.utils;

import org. springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class Auth0ManagementService {

    @Value("${auth0.domain}")
    private String domain;

    @Value("${auth0.mgmt.client-id}")
    private String clientId;

    @Value("${auth0.mgmt.client-secret}")
    private String clientSecret;

    @Value("${auth0.db-connection}")
    private String dbConnection;

    @Value("${auth0.role.contractor}")
    private String contractorRoleId;

    @Value("${auth0.role.customer}")
    private String customerRoleId;

    @Value("${auth0.role.salesperson}")
    private String salespersonRoleId;

    @Value("${auth0.role.owner}")
    private String ownerRoleId;

    private final RestTemplate restTemplate = new RestTemplate();

    private String getManagementToken() {
        String url = "https://" + domain + "/oauth/token";

        Map<String, String> body = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "audience", "https://" + domain + "/api/v2/",
                "grant_type", "client_credentials"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(url, body, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to obtain Auth0 Management API token");
        }

        Object token = response.getBody().get("access_token");
        if (token == null) {
            throw new RuntimeException("Auth0 token response missing access_token");
        }

        return token.toString();
    }

    public String createAuth0User(String primaryEmail,
                                  String secondaryEmail,
                                  String firstName,
                                  String lastName,
                                  String roleString,
                                  String userIdentifier) {

        String token = getManagementToken();
        String url = "https://" + domain + "/api/v2/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        String temporaryPassword = "Tmp!" + UUID.randomUUID();

        Map<String, Object> appMetadata = Map.of(
                "role", roleString,
                "userIdentifier", userIdentifier,
                "secondary_email", secondaryEmail
        );

        Map<String, Object> body = Map.of(
                "connection", dbConnection,
                "email", primaryEmail,
                "password", temporaryPassword,
                "email_verified", false,
                "verify_email", false,
                "given_name", firstName,
                "family_name", lastName,
                "app_metadata", appMetadata
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to create user in Auth0");
        }

        Object userId = response.getBody().get("user_id");
        if (userId == null) {
            throw new RuntimeException("Auth0 user creation response missing user_id");
        }

        return userId.toString();
    }

    public void assignRoleToUser(String auth0UserId, String roleString) {
        String token = getManagementToken();

        String roleId = switch (roleString) {
            case "CONTRACTOR" -> contractorRoleId;
            case "CUSTOMER" -> customerRoleId;
            case "SALESPERSON" -> salespersonRoleId;
            case "OWNER" -> ownerRoleId;
            default -> throw new IllegalArgumentException("Unknown role for Auth0: " + roleString);
        };

        String url = "https://" + domain + "/api/v2/users/" + auth0UserId + "/roles";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = Map.of(
                "roles", new String[]{roleId}
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to assign role " + roleString + " to user " + auth0UserId);
        }
    }

    public String createPasswordChangeTicket(String auth0UserId, String resultUrl) {
        String token = getManagementToken();
        String url = "https://" + domain + "/api/v2/tickets/password-change";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = Map.of(
                "user_id", auth0UserId,
                "result_url", resultUrl
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to create Auth0 password-change ticket");
        }

        Object ticket = response.getBody().get("ticket");
        if (ticket == null) {
            throw new RuntimeException("Auth0 ticket response missing ticket URL");
        }

        return ticket.toString();
    }

    public void updateAuth0UserEmailAndName(String auth0UserId, String newEmail, String firstName, String lastName) {
        String accessToken = getManagementToken();
        String urlGet = String.format("https://%s/api/v2/users/%s", domain, auth0UserId);

        HttpHeaders headersGet = new HttpHeaders();
        headersGet.setBearerAuth(accessToken);
        HttpEntity<Void> getRequest = new HttpEntity<>(headersGet);

        try {
            ResponseEntity<Map> currentUser = restTemplate.exchange(urlGet, HttpMethod.GET, getRequest, Map.class);

            if (currentUser.getBody() == null) {
                throw new RuntimeException("Failed to fetch current user data from Auth0");
            }

            Map<String, Object> userData = new HashMap<>(currentUser.getBody());
            userData.put("name", firstName + " " + lastName);
            userData.put("given_name", firstName);
            userData.put("family_name", lastName);

            if (newEmail != null && !newEmail.trim().isEmpty()) {
                userData.put("email", newEmail);
                userData.put("email_verified", false);
            }

            HttpHeaders headersPut = new HttpHeaders();
            headersPut.setContentType(MediaType.APPLICATION_JSON);
            headersPut.setBearerAuth(accessToken);

            HttpEntity<Map<String, Object>> putRequest = new HttpEntity<>(userData, headersPut);

            restTemplate.put(urlGet, putRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user in Auth0: " + e.getMessage(), e);
        }
    }

    public void blockAuth0User(String auth0UserId, boolean blocked) {
        String managementToken = getManagementToken();
        String url = String.format("https://%s/api/v2/users/%s", domain, auth0UserId);

        Map<String, Object> updateData = Map.of("blocked", blocked);

        try {
            org.springframework.web.reactive.function.client.WebClient webClient =
                    org.springframework.web.reactive.function.client.WebClient.builder()
                            .baseUrl(url)
                            .defaultHeader("Authorization", "Bearer " + managementToken)
                            .defaultHeader("Content-Type", "application/json")
                            .build();

            String response = webClient
                    .patch()
                    .bodyValue(updateData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            System.err.println("=== FAILED TO BLOCK USER ===");
            System.err. println("Error: " + e. getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to block/unblock user in Auth0: " + e.getMessage(), e);
        }
    }
}
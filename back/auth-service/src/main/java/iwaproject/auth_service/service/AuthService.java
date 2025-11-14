package iwaproject.auth_service.service;

import iwaproject.auth_service.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import jakarta.ws.rs.core.Response;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final Keycloak keycloakAdmin;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${user-service.url}")
    private String userServiceUrl;

    /**
     * Login user and get tokens
     */
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        try {
            String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", clientId);
            body.add("username", request.getUsername());
            body.add("password", request.getPassword());

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, requestEntity, Map.class);
            Map<String, Object> tokenResponse = response.getBody();

            if (tokenResponse == null) {
                throw new RuntimeException("Failed to get token response");
            }

            // Extract user info from token
            String accessToken = (String) tokenResponse.get("access_token");
            Map<String, Object> userInfo = getUserInfo(accessToken);

            log.info("Login successful for user: {}", request.getUsername());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken((String) tokenResponse.get("refresh_token"))
                    .tokenType((String) tokenResponse.get("token_type"))
                    .expiresIn((Integer) tokenResponse.get("expires_in"))
                    .refreshExpiresIn((Integer) tokenResponse.get("refresh_expires_in"))
                    .userId((String) userInfo.get("sub"))
                    .username((String) userInfo.get("preferred_username"))
                    .email((String) userInfo.get("email"))
                    .build();

        } catch (HttpClientErrorException e) {
            log.error("Login failed for user {}: {}", request.getUsername(), e.getMessage());
            throw new RuntimeException("Invalid credentials");
        } catch (Exception e) {
            log.error("Unexpected error during login: {}", e.getMessage(), e);
            throw new RuntimeException("Authentication service error");
        }
    }

    /**
     * Register a new user
     */
    public RegisterResponse register(RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());

        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Check if user already exists
            List<UserRepresentation> existingUsers = usersResource.search(request.getUsername());
            if (!existingUsers.isEmpty()) {
                throw new RuntimeException("Username already exists");
            }

            // Check if email already exists
            existingUsers = usersResource.search(null, null, null, request.getEmail(), null, null);
            if (!existingUsers.isEmpty()) {
                throw new RuntimeException("Email already exists");
            }

            // Create user
            UserRepresentation user = new UserRepresentation();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEnabled(true);
            user.setEmailVerified(false);

            Response response = usersResource.create(user);

            if (response.getStatus() != 201) {
                throw new RuntimeException("Failed to create user in Keycloak");
            }

            // Get created user ID
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

            // Set password
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.getPassword());
            credential.setTemporary(false);

            usersResource.get(userId).resetPassword(credential);

            log.info("User registered successfully: {} (ID: {})", request.getUsername(), userId);

            // Note: User will be auto-synced to user-service via webhook

            return RegisterResponse.builder()
                    .userId(userId)
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .message("User registered successfully")
                    .build();

        } catch (Exception e) {
            log.error("Registration failed for {}: {}", request.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Refresh access token
     */
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        log.info("Token refresh attempt");

        try {
            String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("client_id", clientId);
            body.add("refresh_token", request.getRefreshToken());

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, requestEntity, Map.class);
            Map<String, Object> tokenResponse = response.getBody();

            if (tokenResponse == null) {
                throw new RuntimeException("Failed to refresh token");
            }

            String accessToken = (String) tokenResponse.get("access_token");
            Map<String, Object> userInfo = getUserInfo(accessToken);

            log.info("Token refreshed successfully");

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken((String) tokenResponse.get("refresh_token"))
                    .tokenType((String) tokenResponse.get("token_type"))
                    .expiresIn((Integer) tokenResponse.get("expires_in"))
                    .refreshExpiresIn((Integer) tokenResponse.get("refresh_expires_in"))
                    .userId((String) userInfo.get("sub"))
                    .username((String) userInfo.get("preferred_username"))
                    .email((String) userInfo.get("email"))
                    .build();

        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Invalid refresh token");
        }
    }

    /**
     * Logout user
     */
    public void logout(String refreshToken) {
        log.info("Logout attempt");

        try {
            String logoutUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/logout";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
            
            restTemplate.postForEntity(logoutUrl, requestEntity, Void.class);

            log.info("Logout successful");

        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            throw new RuntimeException("Logout failed");
        }
    }

    /**
     * Get user info from access token
     */
    private Map<String, Object> getUserInfo(String accessToken) {
        try {
            String userInfoUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/userinfo";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl, 
                HttpMethod.GET, 
                requestEntity, 
                Map.class
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to get user info: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}

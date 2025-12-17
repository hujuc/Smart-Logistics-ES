package Operimus.SLMS.business_layer.user_management_module.service;

import Operimus.SLMS.business_layer.user_management_module.dto.LoginRequest;
import Operimus.SLMS.business_layer.user_management_module.dto.LoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class AuthService {

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Authenticate user with Keycloak using Resource Owner Password Credentials grant
     */
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
                    keycloakServerUrl, realm);

            // Prepare request body
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("username", loginRequest.getUsername());
            body.add("password", loginRequest.getPassword());

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            // Call Keycloak token endpoint
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenResponse = response.getBody();

                // Extract user info from token
                String accessToken = (String) tokenResponse.get("access_token");
                String refreshToken = (String) tokenResponse.get("refresh_token");
                Integer expiresIn = (Integer) tokenResponse.get("expires_in");

                // Decode JWT to extract all user claims (email, name, roles, etc.)
                Map<String, Object> tokenClaims = decodeJwtToken(accessToken);

                // Build response
                return LoginResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .tokenType("Bearer")
                        .expiresIn(expiresIn != null ? expiresIn.longValue() : 300L)
                        .username((String) tokenClaims.get("preferred_username"))
                        .email((String) tokenClaims.get("email"))
                        .firstName((String) tokenClaims.get("given_name"))
                        .lastName((String) tokenClaims.get("family_name"))
                        .roles(extractRoles(tokenClaims))
                        .build();
            }

            throw new RuntimeException("Failed to authenticate with Keycloak");

        } catch (HttpClientErrorException e) {
            log.error("Authentication failed: {}", e.getMessage());
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new RuntimeException("Invalid username or password. Please check your credentials and try again.");
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new RuntimeException("Invalid login request. Please ensure all required fields are provided.");
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new RuntimeException("Your account has been disabled. Please contact support for assistance.");
            }
            throw new RuntimeException("Authentication service is temporarily unavailable. Please try again later.");
        } catch (Exception e) {
            log.error("Error during authentication: {}", e.getMessage(), e);
            throw new RuntimeException("Unable to complete login. Please try again later or contact support if the issue persists.");
        }
    }

    /**
     * Get user information by using token introspection endpoint and Admin API for roles
     * This endpoint returns full token information including roles
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getUserInfo(String accessToken) {
        String username = null;

        try {
            // Use token introspection endpoint to get basic user info
            String introspectUrl = String.format("%s/realms/%s/protocol/openid-connect/token/introspect",
                    keycloakServerUrl, realm);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("token", accessToken);
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(introspectUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> introspectionResult = response.getBody();

                // Check if token is active
                Boolean active = (Boolean) introspectionResult.get("active");
                if (active != null && active) {
                    log.debug("Successfully introspected token");
                    username = (String) introspectionResult.get("username");

                    // Get roles from Admin API
                    if (username != null) {
                        List<String> roles = getUserRolesFromAdminApi(username);
                        if (!roles.isEmpty()) {
                            Map<String, Object> realmAccess = new java.util.HashMap<>();
                            realmAccess.put("roles", roles);
                            introspectionResult.put("realm_access", realmAccess);
                        }
                    }

                    log.info("User info with roles: username={}, roles={}", username, introspectionResult.get("realm_access"));
                    return introspectionResult;
                } else {
                    log.warn("Token is not active");
                }
            }
        } catch (Exception e) {
            log.error("Error introspecting token: {}", e.getMessage());
        }

        return Map.of();
    }

    /**
     * Get user roles from Keycloak Admin API
     */
    @SuppressWarnings("unchecked")
    private List<String> getUserRolesFromAdminApi(String username) {
        try {
            // First, get an admin token
            String adminToken = getAdminToken();

            // Search for user by username
            String searchUrl = String.format("%s/admin/realms/%s/users?username=%s&exact=true",
                    keycloakServerUrl, realm, username);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<List> userResponse = restTemplate.exchange(
                    searchUrl, HttpMethod.GET, request, List.class);

            if (userResponse.getStatusCode() == HttpStatus.OK && userResponse.getBody() != null) {
                List<Map<String, Object>> users = userResponse.getBody();
                if (!users.isEmpty()) {
                    String userId = (String) users.get(0).get("id");

                    // Get user's realm roles
                    String rolesUrl = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm",
                            keycloakServerUrl, realm, userId);

                    ResponseEntity<List> rolesResponse = restTemplate.exchange(
                            rolesUrl, HttpMethod.GET, request, List.class);

                    if (rolesResponse.getStatusCode() == HttpStatus.OK && rolesResponse.getBody() != null) {
                        List<Map<String, Object>> roleMappings = rolesResponse.getBody();
                        List<String> roleNames = new ArrayList<>();
                        for (Map<String, Object> role : roleMappings) {
                            roleNames.add((String) role.get("name"));
                        }
                        log.debug("Found roles for user {}: {}", username, roleNames);
                        return roleNames;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching user roles from Admin API: {}", e.getMessage());
        }

        return new ArrayList<>();
    }

    /**
     * Get admin access token for Keycloak Admin API
     */
    @SuppressWarnings("unchecked")
    private String getAdminToken() {
        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
                keycloakServerUrl, realm);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("access_token");
        }

        throw new RuntimeException("Failed to get admin token");
    }

    /**
     * Decode JWT token to extract claims
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> decodeJwtToken(String token) {
        try {
            // JWT tokens have 3 parts: header.payload.signature
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }

            // Decode the payload (second part)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            // Parse JSON payload
            return objectMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            log.error("Error decoding JWT token: {}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * Extract roles from user info realm_access claim
     */
    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Map<String, Object> userInfo) {
        try {
            Map<String, Object> realmAccess = (Map<String, Object>) userInfo.get("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                return (List<String>) realmAccess.get("roles");
            }
        } catch (Exception e) {
            log.warn("Error extracting roles: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Refresh access token using refresh token
     */
    public LoginResponse refreshToken(String refreshToken) {
        try {
            String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
                    keycloakServerUrl, realm);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("refresh_token", refreshToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenResponse = response.getBody();

                String accessToken = (String) tokenResponse.get("access_token");
                String newRefreshToken = (String) tokenResponse.get("refresh_token");
                Integer expiresIn = (Integer) tokenResponse.get("expires_in");

                // Decode JWT to extract all user claims
                Map<String, Object> tokenClaims = decodeJwtToken(accessToken);

                return LoginResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(newRefreshToken)
                        .tokenType("Bearer")
                        .expiresIn(expiresIn != null ? expiresIn.longValue() : 300L)
                        .username((String) tokenClaims.get("preferred_username"))
                        .email((String) tokenClaims.get("email"))
                        .firstName((String) tokenClaims.get("given_name"))
                        .lastName((String) tokenClaims.get("family_name"))
                        .roles(extractRoles(tokenClaims))
                        .build();
            }

            throw new RuntimeException("Unable to refresh session. Please try again.");

        } catch (HttpClientErrorException e) {
            log.error("Token refresh failed: {}", e.getMessage());
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new RuntimeException("Your session has expired. Please log in again.");
            }
            throw new RuntimeException("Unable to refresh session. Please log in again.");
        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage(), e);
            throw new RuntimeException("Unable to refresh session due to a technical error. Please log in again.");
        }
    }

    /**
     * Logout user by revoking tokens in Keycloak
     */
    public void logout(String refreshToken) {
        try {
            String logoutUrl = String.format("%s/realms/%s/protocol/openid-connect/logout",
                    keycloakServerUrl, realm);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("refresh_token", refreshToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(logoutUrl, request, String.class);

            log.info("User logged out successfully");

        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            // Don't throw exception, as user should be logged out anyway
        }
    }
}

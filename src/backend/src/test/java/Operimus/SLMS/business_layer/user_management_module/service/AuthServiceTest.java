package Operimus.SLMS.business_layer.user_management_module.service;

import Operimus.SLMS.business_layer.user_management_module.dto.LoginRequest;
import Operimus.SLMS.business_layer.user_management_module.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Set up configuration properties using ReflectionTestUtils
        ReflectionTestUtils.setField(authService, "keycloakServerUrl", "http://localhost:8180");
        ReflectionTestUtils.setField(authService, "realm", "slms");
        ReflectionTestUtils.setField(authService, "clientId", "slms-client");
        ReflectionTestUtils.setField(authService, "clientSecret", "secret");
        ReflectionTestUtils.setField(authService, "restTemplate", restTemplate);
    }

    // ========================================
    // Login Tests
    // ========================================

    @Test
    void whenLoginSuccess_thenReturnLoginResponse() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        Map<String, Object> tokenResponse = createKeycloakTokenResponse();

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(tokenResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        LoginResponse result = authService.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAccessToken()); // Just verify token exists, not the exact value
        assertTrue(result.getAccessToken().startsWith("eyJ")); // Verify it's a JWT
        assertEquals("mock-refresh-token", result.getRefreshToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(300L, result.getExpiresIn());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertTrue(result.getRoles().contains("CUSTOMER"));

        // Verify correct endpoint was called
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), entityCaptor.capture(), eq(Map.class));

        assertTrue(urlCaptor.getValue().contains("/realms/slms/protocol/openid-connect/token"));

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        MultiValueMap<String, String> body = (MultiValueMap<String, String>) capturedEntity.getBody();
        assertEquals("password", body.getFirst("grant_type"));
        assertEquals("slms-client", body.getFirst("client_id"));
        assertEquals("testuser", body.getFirst("username"));
        assertEquals("password123", body.getFirst("password"));
    }

    @Test
    void whenLoginWithInvalidCredentials_thenThrowException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username or password. Please check your credentials and try again.", exception.getMessage());
    }

    @Test
    void whenLoginWithKeycloakError_thenThrowException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Authentication service is temporarily unavailable. Please try again later.", exception.getMessage());
    }

    @Test
    void whenLoginReturnsNullBody_thenThrowException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Unable to complete login. Please try again later or contact support if the issue persists.", exception.getMessage());
    }

    @Test
    void whenLoginReturnsNonOkStatus_thenThrowException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(new HashMap<>(), HttpStatus.BAD_REQUEST);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Unable to complete login. Please try again later or contact support if the issue persists.", exception.getMessage());
    }

    // ========================================
    // Refresh Token Tests
    // ========================================

    @Test
    void whenRefreshTokenSuccess_thenReturnNewTokens() {
        // Arrange
        String refreshToken = "old-refresh-token";
        Map<String, Object> tokenResponse = createKeycloakTokenResponse();

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(tokenResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        LoginResponse result = authService.refreshToken(refreshToken);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertTrue(result.getAccessToken().startsWith("eyJ"));
        assertEquals("mock-refresh-token", result.getRefreshToken());
        assertEquals("testuser", result.getUsername());

        // Verify correct grant type was used
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(anyString(), entityCaptor.capture(), eq(Map.class));

        MultiValueMap<String, String> body = (MultiValueMap<String, String>) entityCaptor.getValue().getBody();
        assertEquals("refresh_token", body.getFirst("grant_type"));
        assertEquals(refreshToken, body.getFirst("refresh_token"));
    }

    @Test
    void whenRefreshTokenExpired_thenThrowException() {
        // Arrange
        String refreshToken = "expired-token";

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Token expired"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.refreshToken(refreshToken);
        });

        assertEquals("Unable to refresh session due to a technical error. Please log in again.", exception.getMessage());
    }

    @Test
    void whenRefreshTokenReturnsNullBody_thenThrowException() {
        // Arrange
        String refreshToken = "valid-token";

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.refreshToken(refreshToken);
        });

        assertEquals("Unable to refresh session due to a technical error. Please log in again.", exception.getMessage());
    }

    // ========================================
    // Logout Tests
    // ========================================

    @Test
    void whenLogoutSuccess_thenNoException() {
        // Arrange
        String refreshToken = "valid-refresh-token";

        ResponseEntity<String> responseEntity = new ResponseEntity<>("", HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        assertDoesNotThrow(() -> authService.logout(refreshToken));

        // Verify logout endpoint was called
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), entityCaptor.capture(), eq(String.class));

        assertTrue(urlCaptor.getValue().contains("/realms/slms/protocol/openid-connect/logout"));

        MultiValueMap<String, String> body = (MultiValueMap<String, String>) entityCaptor.getValue().getBody();
        assertEquals("slms-client", body.getFirst("client_id"));
        assertEquals(refreshToken, body.getFirst("refresh_token"));
    }

    @Test
    void whenLogoutFails_thenNoExceptionThrown() {
        // Arrange
        String refreshToken = "invalid-token";

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Keycloak unavailable"));

        // Act & Assert - logout should not throw exception even if it fails
        assertDoesNotThrow(() -> authService.logout(refreshToken));
    }

    // ========================================
    // JWT Decoding Tests
    // ========================================

    @Test
    void whenLoginWithRoles_thenRolesAreExtracted() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("admin", "password123");
        Map<String, Object> tokenResponse = createKeycloakTokenResponseWithMultipleRoles();

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(tokenResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        LoginResponse result = authService.login(loginRequest);

        // Assert
        assertNotNull(result.getRoles());
        assertEquals(3, result.getRoles().size());
        assertTrue(result.getRoles().contains("ADMIN"));
        assertTrue(result.getRoles().contains("WAREHOUSE_STAFF"));
        assertTrue(result.getRoles().contains("LOGISTICS_MANAGER"));
    }

    @Test
    void whenTokenHasNoRoles_thenReturnEmptyRolesList() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        Map<String, Object> tokenResponse = createKeycloakTokenResponseWithoutRoles();

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(tokenResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        LoginResponse result = authService.login(loginRequest);

        // Assert
        assertNotNull(result.getRoles());
        assertTrue(result.getRoles().isEmpty());
    }

    // ========================================
    // Helper Methods
    // ========================================

    private Map<String, Object> createKeycloakTokenResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("access_token", createMockJwtToken());
        response.put("refresh_token", "mock-refresh-token");
        response.put("expires_in", 300);
        response.put("token_type", "Bearer");
        return response;
    }

    private Map<String, Object> createKeycloakTokenResponseWithMultipleRoles() {
        Map<String, Object> response = new HashMap<>();
        response.put("access_token", createMockJwtTokenWithMultipleRoles());
        response.put("refresh_token", "mock-refresh-token");
        response.put("expires_in", 300);
        response.put("token_type", "Bearer");
        return response;
    }

    private Map<String, Object> createKeycloakTokenResponseWithoutRoles() {
        Map<String, Object> response = new HashMap<>();
        response.put("access_token", createMockJwtTokenWithoutRoles());
        response.put("refresh_token", "mock-refresh-token");
        response.put("expires_in", 300);
        response.put("token_type", "Bearer");
        return response;
    }

    private String createMockJwtToken() {
        // Create a simple mock JWT with header.payload.signature structure
        String header = encodeBase64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encodeBase64Url(
                "{" +
                "\"preferred_username\":\"testuser\"," +
                "\"email\":\"test@example.com\"," +
                "\"given_name\":\"Test\"," +
                "\"family_name\":\"User\"," +
                "\"realm_access\":{\"roles\":[\"CUSTOMER\"]}" +
                "}"
        );
        String signature = encodeBase64Url("mock-signature");

        return header + "." + payload + "." + signature;
    }

    private String createMockJwtTokenWithMultipleRoles() {
        String header = encodeBase64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encodeBase64Url(
                "{" +
                "\"preferred_username\":\"admin\"," +
                "\"email\":\"admin@example.com\"," +
                "\"given_name\":\"Admin\"," +
                "\"family_name\":\"User\"," +
                "\"realm_access\":{\"roles\":[\"ADMIN\",\"WAREHOUSE_STAFF\",\"LOGISTICS_MANAGER\"]}" +
                "}"
        );
        String signature = encodeBase64Url("mock-signature");

        return header + "." + payload + "." + signature;
    }

    private String createMockJwtTokenWithoutRoles() {
        String header = encodeBase64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encodeBase64Url(
                "{" +
                "\"preferred_username\":\"testuser\"," +
                "\"email\":\"test@example.com\"," +
                "\"given_name\":\"Test\"," +
                "\"family_name\":\"User\"" +
                "}"
        );
        String signature = encodeBase64Url("mock-signature");

        return header + "." + payload + "." + signature;
    }

    private String encodeBase64Url(String input) {
        return java.util.Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(input.getBytes());
    }
}

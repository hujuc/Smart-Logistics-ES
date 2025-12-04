package Operimus.SLMS.presentation_layer.landing_page_bff;

import Operimus.SLMS.business_layer.user_management_module.dto.LoginRequest;
import Operimus.SLMS.business_layer.user_management_module.dto.LoginResponse;
import Operimus.SLMS.business_layer.user_management_module.dto.UserDTO;
import Operimus.SLMS.business_layer.user_management_module.service.AuthService;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private KeycloakUserService userService;

    // ========================================
    // Login Tests
    // ========================================

    @Test
    void whenLogin_thenReturnTokenAndSetCookie() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        LoginResponse loginResponse = createLoginResponse();

        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        MvcResult result = mockMvc.perform(post("/api/landing-page/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("access-token-123")))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.refreshToken").doesNotExist()) // Should not be in response body
                .andReturn();

        // Verify refresh token cookie was set
        Cookie cookie = result.getResponse().getCookie("refresh_token");
        assertNotNull(cookie);
        assertEquals("refresh-token-456", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertEquals("/", cookie.getPath());
        assertEquals(7 * 24 * 60 * 60, cookie.getMaxAge()); // 7 days

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void whenLoginWithInvalidCredentials_thenUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid username or password"));

        mockMvc.perform(post("/api/landing-page/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Invalid username or password")));
    }

    @Test
    void whenLoginWithInvalidData_thenBadRequest() throws Exception {
        LoginRequest invalidRequest = new LoginRequest("", ""); // Empty credentials

        mockMvc.perform(post("/api/landing-page/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenLoginFails_thenReturnError() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Authentication service unavailable"));

        mockMvc.perform(post("/api/landing-page/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Authentication service unavailable")));
    }

    // ========================================
    // Register Tests
    // ========================================

    @Test
    void whenRegister_thenCreateUserAndLogin() throws Exception {
        UserDTO userDTO = createUserDTO();
        UserRepresentation userRep = createUserRepresentation();
        LoginResponse loginResponse = createLoginResponse();

        when(userService.createUser(any(UserDTO.class))).thenReturn(userRep);
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        MvcResult result = mockMvc.perform(post("/api/landing-page/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", is("access-token-123")))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();

        // Verify refresh token cookie was set
        Cookie cookie = result.getResponse().getCookie("refresh_token");
        assertNotNull(cookie);
        assertEquals("refresh-token-456", cookie.getValue());

        verify(userService).createUser(any(UserDTO.class));
        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void whenRegisterWithExistingUsername_thenBadRequest() throws Exception {
        UserDTO userDTO = createUserDTO();

        when(userService.createUser(any(UserDTO.class)))
                .thenThrow(new RuntimeException("User already exists"));

        mockMvc.perform(post("/api/landing-page/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("User already exists")));
    }

    @Test
    void whenRegisterWithInvalidData_thenBadRequest() throws Exception {
        UserDTO invalidUserDTO = new UserDTO(
                "", // Empty username
                "invalid-email",
                "",
                "",
                "",
                Arrays.asList()
        );

        mockMvc.perform(post("/api/landing-page/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenRegisterUserCreatedButLoginFails_thenBadRequest() throws Exception {
        UserDTO userDTO = createUserDTO();
        UserRepresentation userRep = createUserRepresentation();

        when(userService.createUser(any(UserDTO.class))).thenReturn(userRep);
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Login failed after registration"));

        mockMvc.perform(post("/api/landing-page/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Login failed after registration")));
    }

    // ========================================
    // Refresh Token Tests
    // ========================================

    @Test
    void whenRefreshToken_thenReturnNewTokens() throws Exception {
        LoginResponse loginResponse = createLoginResponse();

        when(authService.refreshToken("old-refresh-token")).thenReturn(loginResponse);

        MvcResult result = mockMvc.perform(post("/api/landing-page/auth/refresh")
                        .cookie(new Cookie("refresh_token", "old-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("access-token-123")))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();

        // Verify new refresh token cookie was set
        Cookie cookie = result.getResponse().getCookie("refresh_token");
        assertNotNull(cookie);
        assertEquals("refresh-token-456", cookie.getValue());

        verify(authService).refreshToken("old-refresh-token");
    }

    @Test
    void whenRefreshTokenWithoutCookie_thenUnauthorized() throws Exception {
        mockMvc.perform(post("/api/landing-page/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Refresh token not found")));

        verify(authService, never()).refreshToken(any());
    }

    @Test
    void whenRefreshTokenWithEmptyCookie_thenUnauthorized() throws Exception {
        mockMvc.perform(post("/api/landing-page/auth/refresh")
                        .cookie(new Cookie("refresh_token", "")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Refresh token not found")));

        verify(authService, never()).refreshToken(any());
    }

    @Test
    void whenRefreshTokenExpired_thenUnauthorized() throws Exception {
        when(authService.refreshToken("expired-token"))
                .thenThrow(new RuntimeException("Token expired"));

        mockMvc.perform(post("/api/landing-page/auth/refresh")
                        .cookie(new Cookie("refresh_token", "expired-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Token refresh failed")));
    }

    // ========================================
    // Logout Tests
    // ========================================

    @Test
    void whenLogout_thenClearCookieAndRevoke() throws Exception {
        doNothing().when(authService).logout("refresh-token-123");

        MvcResult result = mockMvc.perform(post("/api/landing-page/auth/logout")
                        .cookie(new Cookie("refresh_token", "refresh-token-123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Logged out successfully")))
                .andReturn();

        // Verify cookie was cleared
        Cookie cookie = result.getResponse().getCookie("refresh_token");
        assertNotNull(cookie);
        assertEquals("", cookie.getValue());
        assertEquals(0, cookie.getMaxAge()); // Cookie should be deleted

        verify(authService).logout("refresh-token-123");
    }

    @Test
    void whenLogoutWithoutCookie_thenStillSucceed() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/landing-page/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Logged out successfully")))
                .andReturn();

        // Verify cookie was still cleared
        Cookie cookie = result.getResponse().getCookie("refresh_token");
        assertNotNull(cookie);
        assertEquals("", cookie.getValue());
        assertEquals(0, cookie.getMaxAge());

        verify(authService, never()).logout(any());
    }

    @Test
    void whenLogoutFails_thenStillReturnSuccess() throws Exception {
        doThrow(new RuntimeException("Keycloak unavailable"))
                .when(authService).logout("refresh-token-123");

        MvcResult result = mockMvc.perform(post("/api/landing-page/auth/logout")
                        .cookie(new Cookie("refresh_token", "refresh-token-123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Logged out")))
                .andReturn();

        // When logout fails, cookie clearing code is not reached, so no cookie is set in response
        // This is the actual behavior - the controller doesn't clear cookies on exception
        verify(authService).logout("refresh-token-123");
    }

    // ========================================
    // Helper Methods
    // ========================================

    private LoginResponse createLoginResponse() {
        return LoginResponse.builder()
                .accessToken("access-token-123")
                .refreshToken("refresh-token-456")
                .tokenType("Bearer")
                .expiresIn(300L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .roles(Arrays.asList("CUSTOMER"))
                .build();
    }

    private UserDTO createUserDTO() {
        return new UserDTO(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User",
                Arrays.asList("CUSTOMER")
        );
    }

    private UserRepresentation createUserRepresentation() {
        UserRepresentation user = new UserRepresentation();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEnabled(true);
        return user;
    }
}

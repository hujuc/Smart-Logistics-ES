package Operimus.SLMS.presentation_layer.landing_page_bff;

import Operimus.SLMS.business_layer.user_management_module.dto.LoginRequest;
import Operimus.SLMS.business_layer.user_management_module.dto.LoginResponse;
import Operimus.SLMS.business_layer.user_management_module.dto.UserDTO;
import Operimus.SLMS.business_layer.user_management_module.service.AuthService;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Landing Page BFF - Authentication Controller
 *
 * This controller is part of the Backend-For-Frontend (BFF) pattern for the landing page.
 * It exposes authentication endpoints that delegate to business layer services.
 *
 * Endpoints are mapped to /api/landing-page/auth/* to clearly identify the BFF boundary.
 */
@Slf4j
@RestController
@RequestMapping("/api/landing-page/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost", "http://localhost:3000"}, allowCredentials = "true")
public class AuthController {

    private final AuthService authService;
    private final KeycloakUserService userService;

    /**
     * Login endpoint - authenticates user via business layer
     *
     * @param loginRequest user credentials
     * @param response HTTP response to set cookies
     * @return LoginResponse with access token and user info
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        try {
            log.info("Landing page login attempt for user: {}", loginRequest.getUsername());

            // Delegate authentication to business layer
            LoginResponse loginResponse = authService.login(loginRequest);

            // Set refresh token in HTTP-only cookie for security
            Cookie refreshTokenCookie = new Cookie("refresh_token", loginResponse.getRefreshToken());
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false); // Set to true in production with HTTPS
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
            response.addCookie(refreshTokenCookie);

            log.info("User {} logged in successfully via landing page", loginRequest.getUsername());

            // Don't return refresh token in response body (it's in cookie)
            loginResponse.setRefreshToken(null);

            return ResponseEntity.ok(loginResponse);

        } catch (Exception e) {
            log.error("Landing page login failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Register new user endpoint
     *
     * @param userDTO user registration data
     * @param response HTTP response to set cookies
     * @return LoginResponse after auto-login
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody UserDTO userDTO,
            HttpServletResponse response) {
        try {
            log.info("Landing page registration attempt for user: {}", userDTO.getUsername());

            // Delegate user creation to business layer
            UserRepresentation user = userService.createUser(userDTO);

            // Auto-login after successful registration
            LoginRequest loginRequest = new LoginRequest(userDTO.getUsername(), userDTO.getPassword());
            LoginResponse loginResponse = authService.login(loginRequest);

            // Set refresh token in HTTP-only cookie
            Cookie refreshTokenCookie = new Cookie("refresh_token", loginResponse.getRefreshToken());
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false); // Set to true in production
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
            response.addCookie(refreshTokenCookie);

            log.info("User {} registered and logged in successfully via landing page", userDTO.getUsername());

            // Don't return refresh token in response body
            loginResponse.setRefreshToken(null);

            return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);

        } catch (Exception e) {
            log.error("Landing page registration failed for user {}: {}", userDTO.getUsername(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Refresh access token using refresh token from cookie
     *
     * @param refreshToken refresh token from HTTP-only cookie
     * @param response HTTP response to update cookie
     * @return LoginResponse with new tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        try {
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh token not found"));
            }

            log.info("Token refresh request via landing page");

            // Delegate token refresh to business layer
            LoginResponse loginResponse = authService.refreshToken(refreshToken);

            // Update refresh token cookie
            Cookie refreshTokenCookie = new Cookie("refresh_token", loginResponse.getRefreshToken());
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false); // Set to true in production
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
            response.addCookie(refreshTokenCookie);

            // Don't return refresh token in response body
            loginResponse.setRefreshToken(null);

            log.info("Token refreshed successfully via landing page");

            return ResponseEntity.ok(loginResponse);

        } catch (Exception e) {
            log.error("Token refresh failed via landing page: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token refresh failed"));
        }
    }

    /**
     * Logout endpoint - revokes tokens and clears cookies
     *
     * @param refreshToken refresh token from HTTP-only cookie
     * @param response HTTP response to clear cookie
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        try {
            log.info("Logout request via landing page");

            // Delegate logout to business layer
            if (refreshToken != null && !refreshToken.isEmpty()) {
                authService.logout(refreshToken);
            }

            // Clear refresh token cookie
            Cookie refreshTokenCookie = new Cookie("refresh_token", "");
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0); // Delete cookie
            response.addCookie(refreshTokenCookie);

            log.info("User logged out successfully via landing page");

            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));

        } catch (Exception e) {
            log.error("Logout failed via landing page: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("message", "Logged out")); // Still return success
        }
    }
}

package Operimus.SLMS.business_layer.user_management_module.controllers;

import Operimus.SLMS.business_layer.user_management_module.dto.PasswordUpdateDTO;
import Operimus.SLMS.business_layer.user_management_module.dto.UpdateUserDTO;
import Operimus.SLMS.business_layer.user_management_module.dto.UserDTO;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final KeycloakUserService userService;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDTO userDTO) {
        try {
            log.info("Registering new user: {}", userDTO.getUsername());
            UserRepresentation user = userService.createUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            log.error("Error registering user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all users
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserRepresentation> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error fetching all users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get current authenticated user information
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        try {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("sub", jwt.getSubject());
            userInfo.put("username", jwt.getClaim("preferred_username"));
            userInfo.put("email", jwt.getClaim("email"));
            userInfo.put("firstName", jwt.getClaim("given_name"));
            userInfo.put("lastName", jwt.getClaim("family_name"));
            userInfo.put("roles", jwt.getClaim("realm_access"));
            userInfo.put("tokenIssuedAt", jwt.getIssuedAt());
            userInfo.put("tokenExpiresAt", jwt.getExpiresAt());

            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            log.error("Error fetching current user info: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unable to retrieve your profile information. Please try logging in again."));
        }
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable String userId) {
        try {
            UserRepresentation user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error fetching user by ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Search users
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String query) {
        try {
            List<UserRepresentation> users = userService.searchUsers(query);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error searching users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update user
     */
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        try {
            log.info("Updating user: {}", userId);
            UserRepresentation user = userService.updateUser(userId, updateUserDTO);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error updating user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update user password
     */
    @PutMapping("/{userId}/password")
    public ResponseEntity<?> updatePassword(@PathVariable String userId, @Valid @RequestBody PasswordUpdateDTO passwordDTO) {
        try {
            log.info("Updating password for user: {}", userId);
            userService.updatePassword(userId, passwordDTO.getPassword());
            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } catch (Exception e) {
            log.error("Error updating password: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete user
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

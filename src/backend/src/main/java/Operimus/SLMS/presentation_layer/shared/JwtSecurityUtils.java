package Operimus.SLMS.presentation_layer.shared;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Shared JWT security utilities for all BFF controllers.
 * Provides consistent JWT parsing and role extraction across the presentation layer.
 */
public class JwtSecurityUtils {

    /**
     * Extract the primary user role from JWT token's realm_access.roles claim.
     * Returns the first matching known role in priority order, or "ANONYMOUS" if no JWT.
     *
     * Priority order:
     * 1. LOGISTICS_MANAGER
     * 2. WAREHOUSE_STAFF
     * 3. CUSTOMER_SERVICE_REP
     * 4. DRIVER
     * 5. CUSTOMER
     *
     * @param jwt The JWT token (can be null for anonymous users)
     * @return The primary role as a string
     */
    public static String extractPrimaryRole(Jwt jwt) {
        if (jwt == null) {
            return "ANONYMOUS";
        }

        List<String> roles = extractAllRoles(jwt);

        // Return first known role in priority order
        if (roles.contains("LOGISTICS_MANAGER")) return "LOGISTICS_MANAGER";
        if (roles.contains("WAREHOUSE_STAFF")) return "WAREHOUSE_STAFF";
        if (roles.contains("CUSTOMER_SERVICE_REP")) return "CUSTOMER_SERVICE_REP";
        if (roles.contains("DRIVER")) return "DRIVER";
        if (roles.contains("CUSTOMER")) return "CUSTOMER";

        return "ANONYMOUS";
    }

    /**
     * Extract user ID from JWT.
     * Tries multiple claim sources in order: sub, user_id, preferred_username.
     *
     * @param jwt The JWT token (can be null)
     * @return The user ID, or null if not found or JWT is null
     */
    public static String extractUserId(Jwt jwt) {
        if (jwt == null) {
            return null;
        }

        // Try 'sub' claim first (standard JWT subject)
        String userId = jwt.getSubject();
        if (userId != null && !userId.isEmpty()) {
            return userId;
        }

        // Fallback to 'user_id' claim
        userId = jwt.getClaimAsString("user_id");
        if (userId != null && !userId.isEmpty()) {
            return userId;
        }

        // Last resort: use 'preferred_username'
        userId = jwt.getClaimAsString("preferred_username");
        return userId;
    }

    /**
     * Extract all roles from JWT token's realm_access.roles claim.
     *
     * @param jwt The JWT token (can be null)
     * @return List of role strings, or empty list if none found
     */
    @SuppressWarnings("unchecked")
    public static List<String> extractAllRoles(Jwt jwt) {
        if (jwt == null) {
            return Collections.emptyList();
        }

        // Extract realm_access claim
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return Collections.emptyList();
        }

        // Extract roles array
        Object rolesObj = realmAccess.get("roles");
        if (!(rolesObj instanceof List)) {
            return Collections.emptyList();
        }

        List<String> roles = (List<String>) rolesObj;
        return roles != null ? roles : Collections.emptyList();
    }

    /**
     * Check if the JWT contains a specific role.
     *
     * @param jwt The JWT token (can be null)
     * @param role The role to check for
     * @return true if the role is present, false otherwise
     */
    public static boolean hasRole(Jwt jwt, String role) {
        if (jwt == null || role == null) {
            return false;
        }

        List<String> roles = extractAllRoles(jwt);
        return roles.contains(role);
    }

    /**
     * Extract user's email from JWT.
     *
     * @param jwt The JWT token (can be null)
     * @return The email, or null if not found
     */
    public static String extractEmail(Jwt jwt) {
        if (jwt == null) {
            return null;
        }

        return jwt.getClaimAsString("email");
    }

    /**
     * Extract user's preferred username from JWT.
     *
     * @param jwt The JWT token (can be null)
     * @return The username, or null if not found
     */
    public static String extractUsername(Jwt jwt) {
        if (jwt == null) {
            return null;
        }

        return jwt.getClaimAsString("preferred_username");
    }
}

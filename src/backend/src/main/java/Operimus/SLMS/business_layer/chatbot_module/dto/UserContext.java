package Operimus.SLMS.business_layer.chatbot_module.dto;

import java.util.Set;

/**
 * User context for tool authorization.
 * Contains user identification and roles for security checks.
 */
public record UserContext(
    String userId,
    String role,
    Set<String> allRoles
) {
    /**
     * Check if user has a specific role
     */
    public boolean hasRole(String requiredRole) {
        return allRoles != null && allRoles.contains(requiredRole);
    }

    /**
     * Check if user has any of the specified roles
     */
    public boolean hasAnyRole(Set<String> requiredRoles) {
        if (allRoles == null || requiredRoles == null) {
            return false;
        }
        return allRoles.stream().anyMatch(requiredRoles::contains);
    }
}

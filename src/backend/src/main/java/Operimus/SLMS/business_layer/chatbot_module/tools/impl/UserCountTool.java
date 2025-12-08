package Operimus.SLMS.business_layer.chatbot_module.tools.impl;

import Operimus.SLMS.business_layer.chatbot_module.dto.ToolParameter;
import Operimus.SLMS.business_layer.chatbot_module.dto.ToolResult;
import Operimus.SLMS.business_layer.chatbot_module.dto.UserContext;
import Operimus.SLMS.business_layer.chatbot_module.tools.Tool;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tool for counting users in the system.
 * Can return total user count or filter by specific role.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserCountTool implements Tool {

    private final KeycloakUserService keycloakUserService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "user_count";
    }

    @Override
    public String getDescription() {
        return "Get the total number of users in the system or count users by role. " +
               "Optionally filter by role (CUSTOMER, DRIVER, WAREHOUSE_STAFF, CUSTOMER_SERVICE, LOGISTICS_MANAGER). " +
               "Returns the total count and role filter applied.";
    }

    @Override
    public List<ToolParameter> getParameters() {
        return List.of(
            ToolParameter.optionalString("role",
                "Optional role to filter users (e.g., DRIVER, CUSTOMER, WAREHOUSE_STAFF)")
        );
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("LOGISTICS_MANAGER", "CUSTOMER_SERVICE");
    }

    @Override
    public ToolResult execute(Map<String, Object> params, UserContext context) {
        try {
            String role = (String) params.get("role");

            log.debug("Fetching user count, role filter: {}", role);

            // Get users from Keycloak
            List<UserRepresentation> users;
            if (role != null && !role.isBlank()) {
                users = keycloakUserService.getUsersByRole(role);
                log.debug("Found {} users with role: {}", users.size(), role);
            } else {
                users = keycloakUserService.getAllUsers();
                log.debug("Found {} total users", users.size());
            }

            // Build result
            Map<String, Object> result = new HashMap<>();
            result.put("totalUsers", users.size());
            result.put("roleFilter", role != null && !role.isBlank() ? role : "all");

            String resultJson = objectMapper.writeValueAsString(result);

            log.info("Successfully retrieved user count: {} (role: {})",
                users.size(), role != null ? role : "all");
            return ToolResult.success(null, resultJson);

        } catch (Exception e) {
            log.error("Error in UserCountTool", e);
            return ToolResult.error(null, "Failed to get user count: " + e.getMessage());
        }
    }
}

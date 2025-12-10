package Operimus.SLMS.business_layer.chatbot_module.tools;

import Operimus.SLMS.business_layer.chatbot_module.dto.ToolDefinition;
import Operimus.SLMS.business_layer.chatbot_module.dto.ToolParameter;
import Operimus.SLMS.business_layer.chatbot_module.dto.ToolResult;
import Operimus.SLMS.business_layer.chatbot_module.dto.UserContext;
import Operimus.SLMS.business_layer.chatbot_module.tools.exception.ToolNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for ToolRegistry.
 * Verifies tool discovery, registration, and role-based filtering.
 */
class ToolRegistryTest {

    @Test
    void shouldAutoRegisterToolsFromSpringContext() {
        // Given
        List<Tool> toolBeans = List.of(
            createMockTool("tool1", Set.of("CUSTOMER")),
            createMockTool("tool2", Set.of("MANAGER")),
            createMockTool("tool3", Set.of("*"))
        );

        // When
        ToolRegistry registry = new ToolRegistry(toolBeans);

        // Then
        assertThat(registry.getToolCount()).isEqualTo(3);
        assertThat(registry.getAllToolNames()).containsExactlyInAnyOrder("tool1", "tool2", "tool3");
        assertThat(registry.hasAnyTools()).isTrue();
    }

    @Test
    void shouldHandleNullToolBeans() {
        // When
        ToolRegistry registry = new ToolRegistry(null);

        // Then
        assertThat(registry.getToolCount()).isZero();
        assertThat(registry.hasAnyTools()).isFalse();
    }

    @Test
    void shouldHandleEmptyToolBeans() {
        // When
        ToolRegistry registry = new ToolRegistry(Collections.emptyList());

        // Then
        assertThat(registry.getToolCount()).isZero();
        assertThat(registry.hasAnyTools()).isFalse();
    }

    @Test
    void shouldGetToolByName() {
        // Given
        Tool expectedTool = createMockTool("test_tool", Set.of("CUSTOMER"));
        ToolRegistry registry = new ToolRegistry(List.of(expectedTool));

        // When
        Tool actualTool = registry.getTool("test_tool");

        // Then
        assertThat(actualTool).isEqualTo(expectedTool);
        assertThat(actualTool.getName()).isEqualTo("test_tool");
    }

    @Test
    void shouldThrowToolNotFoundException_whenToolDoesNotExist() {
        // Given
        ToolRegistry registry = new ToolRegistry(List.of(
            createMockTool("existing_tool", Set.of("CUSTOMER"))
        ));

        // When/Then
        assertThatThrownBy(() -> registry.getTool("nonexistent_tool"))
            .isInstanceOf(ToolNotFoundException.class)
            .hasMessageContaining("nonexistent_tool");
    }

    @Test
    void shouldThrowIllegalArgumentException_whenToolNameIsNull() {
        // Given
        ToolRegistry registry = new ToolRegistry(List.of());

        // When/Then
        assertThatThrownBy(() -> registry.getTool(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Tool name cannot be null or blank");
    }

    @Test
    void shouldThrowIllegalArgumentException_whenToolNameIsBlank() {
        // Given
        ToolRegistry registry = new ToolRegistry(List.of());

        // When/Then
        assertThatThrownBy(() -> registry.getTool("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Tool name cannot be null or blank");
    }

    @Test
    void shouldFilterToolsByRole() {
        // Given
        List<Tool> toolBeans = List.of(
            createMockTool("customer_tool", Set.of("CUSTOMER")),
            createMockTool("manager_tool", Set.of("MANAGER")),
            createMockTool("staff_tool", Set.of("WAREHOUSE_STAFF")),
            createMockTool("universal_tool", Set.of("*"))
        );
        ToolRegistry registry = new ToolRegistry(toolBeans);

        // When
        List<Tool> customerTools = registry.getToolsForRole("CUSTOMER");

        // Then
        assertThat(customerTools).hasSize(2);
        assertThat(customerTools.stream().map(Tool::getName))
            .containsExactlyInAnyOrder("customer_tool", "universal_tool");
    }

    @Test
    void shouldAllowWildcardRoleToAccessAllTools() {
        // Given
        List<Tool> toolBeans = List.of(
            createMockTool("restricted_tool", Set.of("MANAGER")),
            createMockTool("universal_tool", Set.of("*"))
        );
        ToolRegistry registry = new ToolRegistry(toolBeans);

        // When - Any role should access wildcard tools
        List<Tool> customerTools = registry.getToolsForRole("CUSTOMER");
        List<Tool> managerTools = registry.getToolsForRole("MANAGER");

        // Then
        assertThat(customerTools).hasSize(1);
        assertThat(customerTools.get(0).getName()).isEqualTo("universal_tool");

        assertThat(managerTools).hasSize(2);
        assertThat(managerTools.stream().map(Tool::getName))
            .containsExactlyInAnyOrder("restricted_tool", "universal_tool");
    }

    @Test
    void shouldReturnEmptyList_whenRoleHasNoTools() {
        // Given
        List<Tool> toolBeans = List.of(
            createMockTool("customer_tool", Set.of("CUSTOMER")),
            createMockTool("manager_tool", Set.of("MANAGER"))
        );
        ToolRegistry registry = new ToolRegistry(toolBeans);

        // When
        List<Tool> driverTools = registry.getToolsForRole("DRIVER");

        // Then
        assertThat(driverTools).isEmpty();
    }

    @Test
    void shouldReturnEmptyList_whenRoleIsNull() {
        // Given
        ToolRegistry registry = new ToolRegistry(List.of(
            createMockTool("test_tool", Set.of("CUSTOMER"))
        ));

        // When
        List<Tool> tools = registry.getToolsForRole(null);

        // Then
        assertThat(tools).isEmpty();
    }

    @Test
    void shouldReturnEmptyList_whenRoleIsBlank() {
        // Given
        ToolRegistry registry = new ToolRegistry(List.of(
            createMockTool("test_tool", Set.of("CUSTOMER"))
        ));

        // When
        List<Tool> tools = registry.getToolsForRole("   ");

        // Then
        assertThat(tools).isEmpty();
    }

    @Test
    void shouldReturnToolDefinitionsForRole() {
        // Given
        List<Tool> toolBeans = List.of(
            createMockTool("customer_tool", Set.of("CUSTOMER")),
            createMockTool("manager_tool", Set.of("MANAGER"))
        );
        ToolRegistry registry = new ToolRegistry(toolBeans);

        // When
        List<ToolDefinition> definitions = registry.getToolDefinitionsForRole("CUSTOMER");

        // Then
        assertThat(definitions).hasSize(1);
        assertThat(definitions.get(0).name()).isEqualTo("customer_tool");
        assertThat(definitions.get(0).description()).isEqualTo("Mock tool: customer_tool");
    }

    @Test
    void shouldCheckIfToolExists() {
        // Given
        ToolRegistry registry = new ToolRegistry(List.of(
            createMockTool("existing_tool", Set.of("CUSTOMER"))
        ));

        // When/Then
        assertThat(registry.hasTool("existing_tool")).isTrue();
        assertThat(registry.hasTool("nonexistent_tool")).isFalse();
        assertThat(registry.hasTool(null)).isFalse();
    }

    @Test
    void shouldDenyAccessToToolsWithEmptyAllowedRoles() {
        // Given
        List<Tool> toolBeans = List.of(
            createMockTool("no_access_tool", Collections.emptySet())
        );
        ToolRegistry registry = new ToolRegistry(toolBeans);

        // When
        List<Tool> customerTools = registry.getToolsForRole("CUSTOMER");
        List<Tool> managerTools = registry.getToolsForRole("MANAGER");

        // Then - No one can access this tool
        assertThat(customerTools).isEmpty();
        assertThat(managerTools).isEmpty();
    }

    @Test
    void shouldHandleToolsWithMultipleRoles() {
        // Given
        List<Tool> toolBeans = List.of(
            createMockTool("multi_role_tool", Set.of("CUSTOMER", "DRIVER", "WAREHOUSE_STAFF"))
        );
        ToolRegistry registry = new ToolRegistry(toolBeans);

        // When
        List<Tool> customerTools = registry.getToolsForRole("CUSTOMER");
        List<Tool> driverTools = registry.getToolsForRole("DRIVER");
        List<Tool> warehouseTools = registry.getToolsForRole("WAREHOUSE_STAFF");
        List<Tool> managerTools = registry.getToolsForRole("MANAGER");

        // Then
        assertThat(customerTools).hasSize(1);
        assertThat(driverTools).hasSize(1);
        assertThat(warehouseTools).hasSize(1);
        assertThat(managerTools).isEmpty();
    }

    @Test
    void shouldOverwriteTool_whenDuplicateNameRegistered() {
        // Given
        Tool tool1 = createMockTool("duplicate_tool", Set.of("CUSTOMER"));
        Tool tool2 = createMockTool("duplicate_tool", Set.of("MANAGER"));

        // When
        ToolRegistry registry = new ToolRegistry(List.of(tool1, tool2));

        // Then - Last tool should be registered
        assertThat(registry.getToolCount()).isEqualTo(1);
        Tool registeredTool = registry.getTool("duplicate_tool");
        assertThat(registeredTool.getAllowedRoles()).containsExactly("MANAGER");
    }

    @Test
    void shouldIgnoreNullToolsInList() {
        // Given
        List<Tool> toolBeans = new ArrayList<>();
        toolBeans.add(createMockTool("valid_tool", Set.of("CUSTOMER")));
        toolBeans.add(null);

        // When
        ToolRegistry registry = new ToolRegistry(toolBeans);

        // Then - Should register only the valid tool
        assertThat(registry.getToolCount()).isEqualTo(1);
        assertThat(registry.hasTool("valid_tool")).isTrue();
    }

    // Helper method to create mock tools for testing
    private Tool createMockTool(String name, Set<String> allowedRoles) {
        return new Tool() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDescription() {
                return "Mock tool: " + name;
            }

            @Override
            public List<ToolParameter> getParameters() {
                return List.of(
                    ToolParameter.requiredString("param1", "Test parameter")
                );
            }

            @Override
            public Set<String> getAllowedRoles() {
                return allowedRoles;
            }

            @Override
            public ToolResult execute(Map<String, Object> params, UserContext context) {
                return ToolResult.success(null, "Mock execution result");
            }

            @Override
            public ToolDefinition getDefinition() {
                return new ToolDefinition(getName(), getDescription(), getParameters());
            }
        };
    }
}

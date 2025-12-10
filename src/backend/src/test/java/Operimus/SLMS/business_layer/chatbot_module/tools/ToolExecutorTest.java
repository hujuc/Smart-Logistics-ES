package Operimus.SLMS.business_layer.chatbot_module.tools;

import Operimus.SLMS.business_layer.chatbot_module.dto.*;
import Operimus.SLMS.business_layer.chatbot_module.tools.exception.ToolNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for ToolExecutor.
 * Verifies parameter validation, authorization, timeout protection, and error handling.
 */
@ExtendWith(MockitoExtension.class)
class ToolExecutorTest {

    @Mock
    private ToolRegistry toolRegistry;

    private ObjectMapper objectMapper;
    private ToolExecutor toolExecutor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        toolExecutor = new ToolExecutor(toolRegistry, objectMapper);
    }

    @Test
    void shouldExecuteToolSuccessfully() {
        // Given
        Tool mockTool = createMockTool("test_tool", Set.of("CUSTOMER"), List.of(
            ToolParameter.requiredString("name", "User name")
        ));
        when(mockTool.execute(any(), any())).thenReturn(ToolResult.success("call-1", "Success"));

        when(toolRegistry.getTool("test_tool")).thenReturn(mockTool);

        ToolCall toolCall = new ToolCall("call-1", "test_tool", Map.of("name", "John"));
        UserContext context = new UserContext("user123", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = toolExecutor.execute(toolCall, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.result()).isEqualTo("Success");
        verify(mockTool).execute(any(), eq(context));
    }

    @Test
    void shouldReturnError_whenToolNotFound() {
        // Given
        when(toolRegistry.getTool("nonexistent_tool"))
            .thenThrow(new ToolNotFoundException("nonexistent_tool"));

        ToolCall toolCall = new ToolCall("call-1", "nonexistent_tool", Map.of());
        UserContext context = new UserContext("user123", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = toolExecutor.execute(toolCall, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("nonexistent_tool");
    }

    @Test
    void shouldValidateRequiredParameters() {
        // Given
        Tool mockTool = createMockTool("test_tool", Set.of("CUSTOMER"), List.of(
            ToolParameter.requiredString("requiredParam", "Required parameter")
        ));
        when(toolRegistry.getTool("test_tool")).thenReturn(mockTool);

        // Missing required parameter
        ToolCall toolCall = new ToolCall("call-1", "test_tool", Map.of());
        UserContext context = new UserContext("user123", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = toolExecutor.execute(toolCall, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Missing required parameter: requiredParam");
        verify(mockTool, never()).execute(any(), any());
    }

    @Test
    void shouldAllowOptionalParameters() {
        // Given
        Tool mockTool = createMockTool("test_tool", Set.of("CUSTOMER"), List.of(
            ToolParameter.optionalString("optionalParam", "Optional parameter")
        ));
        when(mockTool.execute(any(), any())).thenReturn(ToolResult.success("call-1", "Success"));
        when(toolRegistry.getTool("test_tool")).thenReturn(mockTool);

        // No optional parameter provided
        ToolCall toolCall = new ToolCall("call-1", "test_tool", Map.of());
        UserContext context = new UserContext("user123", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = toolExecutor.execute(toolCall, context);

        // Then - Should succeed without the optional parameter
        assertThat(result.success()).isTrue();
        verify(mockTool).execute(any(), any());
    }

    @Test
    void shouldValidateParameterType_string() {
        // Given
        Tool mockTool = createMockTool("test_tool", Set.of("CUSTOMER"), List.of(
            ToolParameter.requiredString("name", "Name parameter")
        ));
        when(toolRegistry.getTool("test_tool")).thenReturn(mockTool);

        // Providing wrong type (number instead of string)
        ToolCall toolCall = new ToolCall("call-1", "test_tool", Map.of("name", 123));
        UserContext context = new UserContext("user123", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = toolExecutor.execute(toolCall, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("must be a string");
        verify(mockTool, never()).execute(any(), any());
    }

    @Test
    void shouldValidateParameterType_integer() {
        // Given
        Tool mockTool = createMockTool("test_tool", Set.of("CUSTOMER"), List.of(
            ToolParameter.requiredInteger("age", "Age parameter")
        ));
        when(toolRegistry.getTool("test_tool")).thenReturn(mockTool);

        // Providing wrong type (string instead of integer)
        ToolCall toolCall = new ToolCall("call-1", "test_tool", Map.of("age", "not a number"));
        UserContext context = new UserContext("user123", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = toolExecutor.execute(toolCall, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("must be a number");
        verify(mockTool, never()).execute(any(), any());
    }

    @Test
    void shouldValidateParameterType_boolean() {
        // Given
        Tool mockTool = createMockTool("test_tool", Set.of("CUSTOMER"), List.of(
            new ToolParameter("active", "boolean", "Active flag", true, null)
        ));
        when(toolRegistry.getTool("test_tool")).thenReturn(mockTool);

        // Providing wrong type (string instead of boolean)
        ToolCall toolCall = new ToolCall("call-1", "test_tool", Map.of("active", "yes"));
        UserContext context = new UserContext("user123", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = toolExecutor.execute(toolCall, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("must be a boolean");
        verify(mockTool, never()).execute(any(), any());
    }

    @Test
    void shouldValidateParameterType_array() {
        // Given
        Tool mockTool = createMockTool("test_tool", Set.of("CUSTOMER"), List.of(
            new ToolParameter("items", "array", "Array of items", true, null)
        ));
        when(toolRegistry.getTool("test_tool")).thenReturn(mockTool);

        // Providing wrong type (string instead of array)
        ToolCall toolCall = new ToolCall("call-1", "test_tool", Map.of("items", "not an array"));
        UserContext context = new UserContext("user123", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = toolExecutor.execute(toolCall, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("must be an array");
        verify(mockTool, never()).execute(any(), any());
    }

    @Test
    void shouldValidateParameterType_object() {
        // Given
        Tool mockTool = createMockTool("test_tool", Set.of("CUSTOMER"), List.of(
            new ToolParameter("metadata", "object", "Metadata object", true, null)
        ));
        when(toolRegistry.getTool("test_tool")).thenReturn(mockTool);

        // Providing wrong type (string instead of object)
        ToolCall toolCall = new ToolCall("call-1", "test_tool", Map.of("metadata", "not an object"));
        UserContext context = new UserContext("user123", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = toolExecutor.execute(toolCall, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("must be an object");
        verify(mockTool, never()).execute(any(), any());
    }

    @Test
    void shouldValidateEnumValues() {
        // Given
        Tool mockTool = createMockTool("test_tool", Set.of("CUSTOMER"), List.of(
            new ToolParameter("status", "string", "Status", true, List.of("ACTIVE", "INACTIVE", "PENDING"))
        ));
        when(toolRegistry.getTool("test_tool")).thenReturn(mockTool);

        // Providing invalid enum value
        ToolCall toolCall = new ToolCall("call-1", "test_tool", Map.of("status", "INVALID"));
        UserContext context = new UserContext("user123", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = toolExecutor.execute(toolCall, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("must be one of");
        assertThat(result.errorMessage()).contains("ACTIVE", "INACTIVE", "PENDING");
        verify(mockTool, never()).execute(any(), any());
    }

    @Test
    void shouldAllowValidEnumValue() {
        // Given
        Tool mockTool = createMockTool("test_tool", Set.of("CUSTOMER"), List.of(
            new ToolParameter("status", "string", "Status", true, List.of("ACTIVE", "INACTIVE"))
        ));
        when(mockTool.execute(any(), any())).thenReturn(ToolResult.success("call-1", "Success"));
        when(toolRegistry.getTool("test_tool")).thenReturn(mockTool);

        // Providing valid enum value
        ToolCall toolCall = new ToolCall("call-1", "test_tool", Map.of("status", "ACTIVE"));
        UserContext context = new UserContext("user123", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = toolExecutor.execute(toolCall, context);

        // Then
        assertThat(result.success()).isTrue();
        verify(mockTool).execute(any(), any());
    }

    @Test
    void shouldCheckAuthorization_allowedRole() {
        // Given
        Tool mockTool = createMockTool("test_tool", Set.of("MANAGER"), List.of());
        when(mockTool.execute(any(), any())).thenReturn(ToolResult.success("call-1", "Success"));
        when(toolRegistry.getTool("test_tool")).thenReturn(mockTool);

        ToolCall toolCall = new ToolCall("call-1", "test_tool", Map.of());
        UserContext context = new UserContext("admin", "MANAGER", Set.of("MANAGER"));

        // When
        ToolResult result = toolExecutor.execute(toolCall, context);

        // Then
        assertThat(result.success()).isTrue();
        verify(mockTool).execute(any(), any());
    }

    @Test
    void shouldCheckAuthorization_deniedRole() {
        // Given
        Tool mockTool = createMockTool("test_tool", Set.of("MANAGER"), List.of());
        when(toolRegistry.getTool("test_tool")).thenReturn(mockTool);

        ToolCall toolCall = new ToolCall("call-1", "test_tool", Map.of());
        UserContext context = new UserContext("user123", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = toolExecutor.execute(toolCall, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("don't have permission");
        verify(mockTool, never()).execute(any(), any());
    }

    @Test
    void shouldAllowWildcardRole() {
        // Given
        Tool mockTool = createMockTool("test_tool", Set.of("*"), List.of());
        when(mockTool.execute(any(), any())).thenReturn(ToolResult.success("call-1", "Success"));
        when(toolRegistry.getTool("test_tool")).thenReturn(mockTool);

        ToolCall toolCall = new ToolCall("call-1", "test_tool", Map.of());
        UserContext context = new UserContext("user123", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = toolExecutor.execute(toolCall, context);

        // Then - Any role should be allowed
        assertThat(result.success()).isTrue();
        verify(mockTool).execute(any(), any());
    }

    @Test
    void shouldCheckAuthorizationInAllRoles() {
        // Given
        Tool mockTool = createMockTool("test_tool", Set.of("ADMIN"), List.of());
        when(mockTool.execute(any(), any())).thenReturn(ToolResult.success("call-1", "Success"));
        when(toolRegistry.getTool("test_tool")).thenReturn(mockTool);

        ToolCall toolCall = new ToolCall("call-1", "test_tool", Map.of());
        // User has CUSTOMER as primary role but also has ADMIN in allRoles
        UserContext context = new UserContext("user123", "CUSTOMER", Set.of("CUSTOMER", "ADMIN"));

        // When
        ToolResult result = toolExecutor.execute(toolCall, context);

        // Then - Should be allowed because of ADMIN in allRoles
        assertThat(result.success()).isTrue();
        verify(mockTool).execute(any(), any());
    }

    @Test
    void shouldHandleToolExecutionException() {
        // Given
        Tool mockTool = createMockTool("test_tool", Set.of("CUSTOMER"), List.of());
        when(mockTool.execute(any(), any())).thenThrow(new RuntimeException("Tool execution failed"));
        when(toolRegistry.getTool("test_tool")).thenReturn(mockTool);

        ToolCall toolCall = new ToolCall("call-1", "test_tool", Map.of());
        UserContext context = new UserContext("user123", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = toolExecutor.execute(toolCall, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Execution error: Tool execution failed");
    }

    @Test
    void shouldTimeoutLongRunningTool() {
        // Given
        Tool slowTool = createSlowTool("slow_tool", Set.of("CUSTOMER"), 10000); // 10 seconds
        when(toolRegistry.getTool("slow_tool")).thenReturn(slowTool);

        ToolCall toolCall = new ToolCall("call-1", "slow_tool", Map.of());
        UserContext context = new UserContext("user123", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        long startTime = System.currentTimeMillis();
        ToolResult result = toolExecutor.execute(toolCall, context);
        long duration = System.currentTimeMillis() - startTime;

        // Then - Should timeout after ~5 seconds (MAX_EXECUTION_TIME_MS)
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("timed out");
        assertThat(duration).isLessThan(7000); // Should not wait full 10 seconds
    }

    @Test
    void shouldExecuteMultipleToolCalls() {
        // Given
        Tool mockTool1 = createMockTool("tool1", Set.of("CUSTOMER"), List.of());
        Tool mockTool2 = createMockTool("tool2", Set.of("CUSTOMER"), List.of());

        when(mockTool1.execute(any(), any())).thenReturn(ToolResult.success("call-1", "Result 1"));
        when(mockTool2.execute(any(), any())).thenReturn(ToolResult.success("call-2", "Result 2"));

        when(toolRegistry.getTool("tool1")).thenReturn(mockTool1);
        when(toolRegistry.getTool("tool2")).thenReturn(mockTool2);

        List<ToolCall> toolCalls = List.of(
            new ToolCall("call-1", "tool1", Map.of()),
            new ToolCall("call-2", "tool2", Map.of())
        );
        UserContext context = new UserContext("user123", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        List<ToolResult> results = toolExecutor.executeAll(toolCalls, context);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).success()).isTrue();
        assertThat(results.get(0).result()).isEqualTo("Result 1");
        assertThat(results.get(1).success()).isTrue();
        assertThat(results.get(1).result()).isEqualTo("Result 2");
    }

    @Test
    void shouldHandleNullParameterValue() {
        // Given
        Tool mockTool = createMockTool("test_tool", Set.of("CUSTOMER"), List.of(
            ToolParameter.optionalString("optionalParam", "Optional parameter")
        ));
        when(mockTool.execute(any(), any())).thenReturn(ToolResult.success("call-1", "Success"));
        when(toolRegistry.getTool("test_tool")).thenReturn(mockTool);

        // Providing null value for optional parameter
        Map<String, Object> params = new HashMap<>();
        params.put("optionalParam", null);
        ToolCall toolCall = new ToolCall("call-1", "test_tool", params);
        UserContext context = new UserContext("user123", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = toolExecutor.execute(toolCall, context);

        // Then - Should not fail on null optional parameter
        assertThat(result.success()).isTrue();
        verify(mockTool).execute(any(), any());
    }

    // Helper methods
    private Tool createMockTool(String name, Set<String> allowedRoles, List<ToolParameter> parameters) {
        Tool tool = mock(Tool.class);
        // Use lenient() for stubs that may not be called in all tests
        lenient().when(tool.getName()).thenReturn(name);
        lenient().when(tool.getDescription()).thenReturn("Mock tool: " + name);
        lenient().when(tool.getAllowedRoles()).thenReturn(allowedRoles);
        lenient().when(tool.getParameters()).thenReturn(parameters);
        return tool;
    }

    private Tool createSlowTool(String name, Set<String> allowedRoles, long sleepMs) {
        Tool tool = mock(Tool.class);
        // Use lenient() for stubs that may not be called in all tests
        lenient().when(tool.getName()).thenReturn(name);
        lenient().when(tool.getDescription()).thenReturn("Slow tool: " + name);
        lenient().when(tool.getAllowedRoles()).thenReturn(allowedRoles);
        lenient().when(tool.getParameters()).thenReturn(List.of());

        // Simulate slow execution
        when(tool.execute(any(), any())).thenAnswer(invocation -> {
            Thread.sleep(sleepMs);
            return ToolResult.success("call-1", "Slow result");
        });

        return tool;
    }
}

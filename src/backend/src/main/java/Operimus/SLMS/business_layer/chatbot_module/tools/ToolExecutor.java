package Operimus.SLMS.business_layer.chatbot_module.tools;

import Operimus.SLMS.business_layer.chatbot_module.dto.ToolCall;
import Operimus.SLMS.business_layer.chatbot_module.dto.ToolParameter;
import Operimus.SLMS.business_layer.chatbot_module.dto.ToolResult;
import Operimus.SLMS.business_layer.chatbot_module.dto.UserContext;
import Operimus.SLMS.business_layer.chatbot_module.tools.exception.ToolExecutionException;
import Operimus.SLMS.business_layer.chatbot_module.tools.exception.ToolNotFoundException;
import Operimus.SLMS.business_layer.chatbot_module.tools.exception.UnauthorizedToolAccessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Executes tools with security validation and timeout protection.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ToolExecutor {

    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    private static final int MAX_EXECUTION_TIME_MS = 5000;

    /**
     * Execute a tool call with authorization and validation.
     *
     * @param toolCall The tool call to execute
     * @param context User context for authorization
     * @return ToolResult with success or error
     */
    public ToolResult execute(ToolCall toolCall, UserContext context) {
        log.info("Executing tool: {} for user: {} (role: {})",
            toolCall.name(), context.userId(), context.role());

        try {
            // 1. Get tool
            Tool tool = toolRegistry.getTool(toolCall.name());

            // 2. Check authorization
            if (!isAuthorized(tool, context)) {
                log.warn("Unauthorized tool access: {} by role: {}",
                    toolCall.name(), context.role());
                return ToolResult.error(
                    toolCall.id(),
                    "You don't have permission to use this tool"
                );
            }

            // 3. Validate parameters
            validateParameters(tool, toolCall.arguments());

            // 4. Execute with timeout
            ToolResult result = executeWithTimeout(tool, toolCall, context);

            log.debug("Tool execution completed: {} - success: {}",
                toolCall.name(), result.success());

            return result;

        } catch (ToolNotFoundException e) {
            log.error("Tool not found: {}", toolCall.name());
            return ToolResult.error(toolCall.id(), e.getMessage());

        } catch (UnauthorizedToolAccessException e) {
            log.error("Unauthorized access: {}", e.getMessage());
            return ToolResult.error(toolCall.id(), e.getMessage());

        } catch (IllegalArgumentException e) {
            log.error("Invalid parameters for tool {}: {}", toolCall.name(), e.getMessage());
            return ToolResult.error(toolCall.id(), "Invalid parameters: " + e.getMessage());

        } catch (Exception e) {
            log.error("Tool execution failed: {}", toolCall.name(), e);
            return ToolResult.error(
                toolCall.id(),
                "Tool execution failed: " + e.getMessage()
            );
        }
    }

    /**
     * Execute multiple tool calls in sequence.
     *
     * @param toolCalls List of tool calls to execute
     * @param context User context for authorization
     * @return List of tool results
     */
    public List<ToolResult> executeAll(List<ToolCall> toolCalls, UserContext context) {
        log.info("Executing {} tools for user: {}", toolCalls.size(), context.userId());

        return toolCalls.stream()
            .map(toolCall -> execute(toolCall, context))
            .toList();
    }

    /**
     * Check if user is authorized to use the tool.
     *
     * @param tool The tool to check
     * @param context User context
     * @return true if authorized
     */
    private boolean isAuthorized(Tool tool, UserContext context) {
        Set<String> allowedRoles = tool.getAllowedRoles();

        // Check for wildcard
        if (allowedRoles.contains("*")) {
            return true;
        }

        // Check primary role
        if (allowedRoles.contains(context.role())) {
            return true;
        }

        // Check all roles
        if (context.allRoles() != null) {
            for (String userRole : context.allRoles()) {
                if (allowedRoles.contains(userRole)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Validate parameters against tool schema.
     *
     * @param tool The tool
     * @param arguments Arguments from LLM
     * @throws IllegalArgumentException if validation fails
     */
    private void validateParameters(Tool tool, Map<String, Object> arguments) {
        List<ToolParameter> params = tool.getParameters();

        // Check required parameters
        for (ToolParameter param : params) {
            if (param.required() && !arguments.containsKey(param.name())) {
                throw new IllegalArgumentException(
                    "Missing required parameter: " + param.name()
                );
            }

            // Type validation if parameter is present
            if (arguments.containsKey(param.name())) {
                Object value = arguments.get(param.name());
                if (value != null) {
                    validateParameterType(param, value);
                }
            }
        }
    }

    /**
     * Validate parameter type matches schema.
     *
     * @param param Parameter definition
     * @param value Actual value from LLM
     * @throws IllegalArgumentException if type doesn't match
     */
    private void validateParameterType(ToolParameter param, Object value) {
        String expectedType = param.type();

        switch (expectedType) {
            case "string" -> {
                if (!(value instanceof String)) {
                    throw new IllegalArgumentException(
                        "Parameter '%s' must be a string, got: %s".formatted(
                            param.name(), value.getClass().getSimpleName()
                        )
                    );
                }
            }
            case "integer", "number" -> {
                if (!(value instanceof Number)) {
                    throw new IllegalArgumentException(
                        "Parameter '%s' must be a number, got: %s".formatted(
                            param.name(), value.getClass().getSimpleName()
                        )
                    );
                }
            }
            case "boolean" -> {
                if (!(value instanceof Boolean)) {
                    throw new IllegalArgumentException(
                        "Parameter '%s' must be a boolean, got: %s".formatted(
                            param.name(), value.getClass().getSimpleName()
                        )
                    );
                }
            }
            case "array" -> {
                if (!(value instanceof List || value.getClass().isArray())) {
                    throw new IllegalArgumentException(
                        "Parameter '%s' must be an array, got: %s".formatted(
                            param.name(), value.getClass().getSimpleName()
                        )
                    );
                }
            }
            case "object" -> {
                if (!(value instanceof Map)) {
                    throw new IllegalArgumentException(
                        "Parameter '%s' must be an object, got: %s".formatted(
                            param.name(), value.getClass().getSimpleName()
                        )
                    );
                }
            }
            default -> log.warn("Unknown parameter type: {}", expectedType);
        }

        // Validate enum values if specified
        if (param.enumValues() != null && !param.enumValues().isEmpty()) {
            String stringValue = value.toString();
            if (!param.enumValues().contains(stringValue)) {
                throw new IllegalArgumentException(
                    "Parameter '%s' must be one of %s, got: %s".formatted(
                        param.name(), param.enumValues(), stringValue
                    )
                );
            }
        }
    }

    /**
     * Execute tool with timeout protection.
     *
     * @param tool The tool to execute
     * @param toolCall The tool call
     * @param context User context
     * @return ToolResult
     */
    private ToolResult executeWithTimeout(Tool tool, ToolCall toolCall, UserContext context) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            Future<ToolResult> future = executor.submit(() ->
                tool.execute(toolCall.arguments(), context)
            );

            ToolResult result = future.get(MAX_EXECUTION_TIME_MS, TimeUnit.MILLISECONDS);

            return result;

        } catch (TimeoutException e) {
            log.error("Tool execution timed out: {} after {}ms", toolCall.name(), MAX_EXECUTION_TIME_MS);
            return ToolResult.error(
                toolCall.id(),
                "Tool execution timed out after " + MAX_EXECUTION_TIME_MS + "ms"
            );

        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            log.error("Tool execution threw exception: {}", toolCall.name(), cause);
            return ToolResult.error(
                toolCall.id(),
                "Execution error: " + (cause != null ? cause.getMessage() : e.getMessage())
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Tool execution interrupted: {}", toolCall.name());
            return ToolResult.error(toolCall.id(), "Tool execution was interrupted");

        } finally {
            executor.shutdownNow();
        }
    }
}

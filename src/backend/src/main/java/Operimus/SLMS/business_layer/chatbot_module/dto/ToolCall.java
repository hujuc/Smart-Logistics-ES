package Operimus.SLMS.business_layer.chatbot_module.dto;

import java.util.Map;

/**
 * Represents a tool invocation request from the LLM.
 * Contains the tool name and arguments to execute.
 */
public record ToolCall(
    String id,                        // Unique call ID for tracking
    String name,                      // Tool name to invoke
    Map<String, Object> arguments     // Parameters from LLM
) {
    /**
     * Get a typed parameter value
     */
    @SuppressWarnings("unchecked")
    public <T> T getArgument(String name, Class<T> type) {
        Object value = arguments.get(name);
        if (value == null) {
            return null;
        }

        // Handle Number to Long conversion
        if (type == Long.class && value instanceof Number) {
            return (T) Long.valueOf(((Number) value).longValue());
        }

        // Handle Number to Integer conversion
        if (type == Integer.class && value instanceof Number) {
            return (T) Integer.valueOf(((Number) value).intValue());
        }

        return type.cast(value);
    }

    /**
     * Check if argument exists
     */
    public boolean hasArgument(String name) {
        return arguments.containsKey(name);
    }
}

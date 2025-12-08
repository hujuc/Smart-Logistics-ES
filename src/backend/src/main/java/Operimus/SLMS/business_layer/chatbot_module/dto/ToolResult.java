package Operimus.SLMS.business_layer.chatbot_module.dto;

/**
 * Result of tool execution.
 * Contains either successful result data or error information.
 */
public record ToolResult(
    String toolCallId,
    boolean success,
    String result,              // JSON string for LLM consumption
    String errorMessage
) {
    /**
     * Create a successful tool result
     */
    public static ToolResult success(String toolCallId, String result) {
        return new ToolResult(toolCallId, true, result, null);
    }

    /**
     * Create an error tool result
     */
    public static ToolResult error(String toolCallId, String errorMessage) {
        return new ToolResult(toolCallId, false, null, errorMessage);
    }

    /**
     * Get formatted result for LLM
     */
    public String getFormattedResult() {
        if (success) {
            return "Tool execution successful. Result: " + result;
        } else {
            return "Tool execution failed. Error: " + errorMessage;
        }
    }
}

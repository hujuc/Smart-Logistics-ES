package Operimus.SLMS.business_layer.chatbot_module.tools;

import Operimus.SLMS.business_layer.chatbot_module.dto.*;

import java.util.List;
import java.util.Set;

/**
 * Base interface for all chatbot tools.
 * Tools are functions that the LLM can invoke to fetch real data or perform actions.
 */
public interface Tool {
    /**
     * Unique tool identifier (e.g., "order_lookup")
     * Used by the LLM to reference this tool.
     */
    String getName();

    /**
     * Human-readable description for the LLM.
     * Should explain what the tool does and when to use it.
     */
    String getDescription();

    /**
     * Parameter schema for the tool.
     * Defines what parameters the tool accepts and their types.
     */
    List<ToolParameter> getParameters();

    /**
     * Roles allowed to use this tool.
     * Use "*" to allow all roles.
     * Returns empty set to deny access to all roles.
     */
    Set<String> getAllowedRoles();

    /**
     * Execute the tool with given parameters.
     *
     * @param params Arguments from the LLM
     * @param context User context for authorization
     * @return ToolResult with either success data or error message
     */
    ToolResult execute(java.util.Map<String, Object> params, UserContext context);

    /**
     * Get tool definition for Gemini API.
     * Default implementation constructs it from the interface methods.
     */
    default ToolDefinition getDefinition() {
        return new ToolDefinition(getName(), getDescription(), getParameters());
    }
}

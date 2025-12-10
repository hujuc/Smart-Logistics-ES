package Operimus.SLMS.business_layer.chatbot_module.tools;

import Operimus.SLMS.business_layer.chatbot_module.dto.ToolDefinition;
import Operimus.SLMS.business_layer.chatbot_module.tools.exception.ToolNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for all available chatbot tools.
 * Automatically discovers and registers Tool beans from Spring context.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "chatbot.tools.enabled", havingValue = "true", matchIfMissing = true)
public class ToolRegistry {

    private final Map<String, Tool> tools = new ConcurrentHashMap<>();

    /**
     * Constructor that auto-registers all Tool beans.
     * Spring will inject all beans implementing the Tool interface.
     *
     * @param toolBeans List of all Tool beans from Spring context (can be empty)
     */
    public ToolRegistry(List<Tool> toolBeans) {
        if (toolBeans == null || toolBeans.isEmpty()) {
            log.warn("No tool beans found in Spring context. Tool calling will be disabled.");
            return;
        }

        log.info("Initializing ToolRegistry with {} tool beans", toolBeans.size());

        for (Tool tool : toolBeans) {
            if (tool != null) {
                register(tool);
            } else {
                log.warn("Skipping null tool in tool beans list");
            }
        }

        log.info("Successfully registered {} tools: {}", tools.size(), tools.keySet());
    }

    /**
     * Register a tool in the registry.
     *
     * @param tool The tool to register
     */
    private void register(Tool tool) {
        if (tool == null) {
            log.error("Cannot register null tool");
            return;
        }

        String toolName = tool.getName();
        if (toolName == null || toolName.isBlank()) {
            log.error("Cannot register tool with null or blank name");
            return;
        }

        if (tools.containsKey(toolName)) {
            log.warn("Tool '{}' is already registered. Overwriting with new instance.", toolName);
        }

        tools.put(toolName, tool);
        log.debug("Registered tool: {} - Roles: {} - Description: {}",
            toolName, tool.getAllowedRoles(), tool.getDescription());
    }

    /**
     * Get a tool by name.
     *
     * @param name The tool name
     * @return The tool instance
     * @throws ToolNotFoundException if tool doesn't exist
     */
    public Tool getTool(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tool name cannot be null or blank");
        }

        Tool tool = tools.get(name);
        if (tool == null) {
            log.error("Tool not found: {}. Available tools: {}", name, tools.keySet());
            throw new ToolNotFoundException(name);
        }
        return tool;
    }

    /**
     * Get all tools available for a specific role.
     * Filters tools based on allowed roles.
     *
     * @param role The user role
     * @return List of tools the role can access (empty if no tools available)
     */
    public List<Tool> getToolsForRole(String role) {
        if (role == null || role.isBlank()) {
            log.warn("Cannot get tools for null or blank role");
            return Collections.emptyList();
        }

        List<Tool> availableTools = tools.values().stream()
            .filter(tool -> isToolAvailableForRole(tool, role))
            .toList();

        log.debug("Found {} tools available for role: {}", availableTools.size(), role);
        return availableTools;
    }

    /**
     * Get tool definitions for Gemini API filtered by role.
     *
     * @param role The user role
     * @return List of tool definitions in Gemini format
     */
    public List<ToolDefinition> getToolDefinitionsForRole(String role) {
        return getToolsForRole(role).stream()
            .map(Tool::getDefinition)
            .toList();
    }

    /**
     * Get all tool definitions regardless of role.
     * Useful for showing the chatbot what capabilities exist system-wide,
     * enabling it to provide proper authorization messages when users ask
     * about features they don't have access to.
     *
     * @return List of all tool definitions
     */
    public List<ToolDefinition> getAllToolDefinitions() {
        return tools.values().stream()
            .map(Tool::getDefinition)
            .toList();
    }

    /**
     * Check if a tool exists in the registry.
     *
     * @param name The tool name
     * @return true if tool exists, false otherwise
     */
    public boolean hasTool(String name) {
        return name != null && tools.containsKey(name);
    }

    /**
     * Get all registered tool names.
     *
     * @return Set of tool names
     */
    public Set<String> getAllToolNames() {
        return Collections.unmodifiableSet(tools.keySet());
    }

    /**
     * Get count of registered tools.
     *
     * @return Number of tools
     */
    public int getToolCount() {
        return tools.size();
    }

    /**
     * Check if any tools are registered.
     *
     * @return true if at least one tool is registered
     */
    public boolean hasAnyTools() {
        return !tools.isEmpty();
    }

    /**
     * Check if a tool is available for a specific role.
     *
     * @param tool The tool to check
     * @param role The user role
     * @return true if tool is available for this role
     */
    private boolean isToolAvailableForRole(Tool tool, String role) {
        Set<String> allowedRoles = tool.getAllowedRoles();

        if (allowedRoles == null || allowedRoles.isEmpty()) {
            log.debug("Tool '{}' has no allowed roles configured - denying access", tool.getName());
            return false;
        }

        // Wildcard allows all roles
        if (allowedRoles.contains("*")) {
            return true;
        }

        // Check if role is in allowed list
        return allowedRoles.contains(role);
    }
}

package Operimus.SLMS.business_layer.chatbot_module.dto;

import java.util.List;

/**
 * Tool parameter definition for JSON schema generation.
 * Describes a parameter that can be passed to a tool.
 */
public record ToolParameter(
    String name,
    String type,          // "string", "integer", "boolean", "number", "array", "object"
    String description,
    boolean required,
    List<String> enumValues  // Optional: for restricted values
) {
    /**
     * Create a required string parameter
     */
    public static ToolParameter requiredString(String name, String description) {
        return new ToolParameter(name, "string", description, true, null);
    }

    /**
     * Create an optional string parameter
     */
    public static ToolParameter optionalString(String name, String description) {
        return new ToolParameter(name, "string", description, false, null);
    }

    /**
     * Create a required integer parameter
     */
    public static ToolParameter requiredInteger(String name, String description) {
        return new ToolParameter(name, "integer", description, true, null);
    }

    /**
     * Create an optional integer parameter
     */
    public static ToolParameter optionalInteger(String name, String description) {
        return new ToolParameter(name, "integer", description, false, null);
    }
}

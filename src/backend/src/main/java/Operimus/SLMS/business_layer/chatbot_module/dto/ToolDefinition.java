package Operimus.SLMS.business_layer.chatbot_module.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tool definition for LLM function calling.
 * Contains metadata about a tool that can be invoked by the LLM.
 */
public record ToolDefinition(
    String name,
    String description,
    List<ToolParameter> parameters
) {
    /**
     * Convert to Gemini's function declaration format.
     * Generates JSON schema for the tool parameters.
     */
    public Map<String, Object> toGeminiFormat() {
        Map<String, Object> properties = new HashMap<>();
        List<String> required = new ArrayList<>();

        for (ToolParameter param : parameters) {
            Map<String, Object> paramDef = new HashMap<>();
            paramDef.put("type", param.type());
            paramDef.put("description", param.description());

            if (param.enumValues() != null && !param.enumValues().isEmpty()) {
                paramDef.put("enum", param.enumValues());
            }

            properties.put(param.name(), paramDef);

            if (param.required()) {
                required.add(param.name());
            }
        }

        Map<String, Object> parametersSchema = new HashMap<>();
        parametersSchema.put("type", "object");
        parametersSchema.put("properties", properties);
        if (!required.isEmpty()) {
            parametersSchema.put("required", required);
        }

        return Map.of(
            "name", name,
            "description", description,
            "parameters", parametersSchema
        );
    }
}

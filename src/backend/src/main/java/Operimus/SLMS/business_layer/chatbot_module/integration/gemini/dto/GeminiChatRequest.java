package Operimus.SLMS.business_layer.chatbot_module.integration.gemini.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiChatRequest(
        List<Content> contents,
        @JsonProperty("generationConfig") GenerationConfig generationConfig,
        List<Tool> tools  // NEW: Support for function calling
) {
    // Constructor for backward compatibility (without tools)
    public GeminiChatRequest(List<Content> contents, GenerationConfig generationConfig) {
        this(contents, generationConfig, null);
    }

    public record Content(
            String role,
            List<Part> parts
    ) {}

    public record Part(
            String text
    ) {}

    public record GenerationConfig(
            Double temperature,
            Integer maxOutputTokens
    ) {}

    // NEW: Tool definition for Gemini function calling
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Tool(
            @JsonProperty("functionDeclarations") List<FunctionDeclaration> functionDeclarations
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record FunctionDeclaration(
            String name,
            String description,
            Object parameters  // JSON schema object
    ) {}
}

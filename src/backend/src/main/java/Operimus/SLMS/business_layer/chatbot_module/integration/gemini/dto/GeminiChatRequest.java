package Operimus.SLMS.business_layer.chatbot_module.integration.gemini.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GeminiChatRequest(
        List<Content> contents,
        @JsonProperty("generationConfig") GenerationConfig generationConfig
) {
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
}

package Operimus.SLMS.business_layer.chatbot_module.integration.gemini.dto;

public record GeminiEmbeddingRequest(
        String model,
        Content content
) {
    public record Content(
            Part[] parts
    ) {}

    public record Part(
            String text
    ) {}
}

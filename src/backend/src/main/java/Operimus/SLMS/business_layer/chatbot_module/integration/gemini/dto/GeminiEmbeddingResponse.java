package Operimus.SLMS.business_layer.chatbot_module.integration.gemini.dto;

public record GeminiEmbeddingResponse(
        Embedding embedding
) {
    public record Embedding(
            float[] values
    ) {}
}

package Operimus.SLMS.business_layer.chatbot_module.integration.gemini;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chatbot.gemini")
public record GeminiProperties(
        String apiKey,
        String model,
        String embeddingModel
) {
}

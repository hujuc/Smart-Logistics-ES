package Operimus.SLMS.business_layer.chatbot_module.integration.groq;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chatbot.groq")
public record GroqProperties(
        String apiKey,
        String model
) {
}

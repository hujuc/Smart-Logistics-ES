package Operimus.SLMS.business_layer.chatbot_module.integration.pinecone;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chatbot.pinecone")
public record PineconeProperties(
        String apiKey,
        String host,
        String index,
        String namespace
) {
}

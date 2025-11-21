package Operimus.SLMS.assistant.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "assistant.pinecone")
public record PineconeProperties(
        String apiKey,
        String environment,
        String index,
        String namespace
) {
}

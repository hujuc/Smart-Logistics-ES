package Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.dto;

import java.util.Map;

public record PineconeQueryRequest(
        String namespace,
        float[] vector,
        int topK,
        boolean includeMetadata,
        Map<String, Object> filter
) {
}

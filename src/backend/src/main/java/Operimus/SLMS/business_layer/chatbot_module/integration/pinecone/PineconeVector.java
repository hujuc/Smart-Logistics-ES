package Operimus.SLMS.business_layer.chatbot_module.integration.pinecone;

import java.util.Map;

public record PineconeVector(
        String id,
        float[] values,
        Map<String, Object> metadata
) {
}

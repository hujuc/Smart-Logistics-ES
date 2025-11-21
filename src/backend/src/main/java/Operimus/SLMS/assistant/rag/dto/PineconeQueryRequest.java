package Operimus.SLMS.assistant.rag.dto;

import java.util.Map;

public record PineconeQueryRequest(
        String namespace,
        float[] vector,
        int topK,
        boolean includeMetadata,
        Map<String, Object> filter
) {
}

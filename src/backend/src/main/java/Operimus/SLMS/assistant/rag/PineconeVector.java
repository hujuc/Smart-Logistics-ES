package Operimus.SLMS.assistant.rag;

import java.util.Map;

public record PineconeVector(
        String id,
        float[] values,
        Map<String, Object> metadata
) {
}

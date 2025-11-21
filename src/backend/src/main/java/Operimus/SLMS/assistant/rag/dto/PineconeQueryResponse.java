package Operimus.SLMS.assistant.rag.dto;

import java.util.List;
import java.util.Map;

public record PineconeQueryResponse(List<Match> matches) {

    public record Match(
            String id,
            double score,
            Map<String, Object> metadata
    ) {
    }
}

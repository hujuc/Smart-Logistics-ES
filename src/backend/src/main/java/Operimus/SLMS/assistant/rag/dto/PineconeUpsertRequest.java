package Operimus.SLMS.assistant.rag.dto;

import Operimus.SLMS.assistant.rag.PineconeVector;
import java.util.List;

public record PineconeUpsertRequest(
        String namespace,
        List<PineconeVector> vectors
) {
}

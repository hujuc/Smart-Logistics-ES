package Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.dto;

import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeVector;
import java.util.List;

public record PineconeUpsertRequest(
        String namespace,
        List<PineconeVector> vectors
) {
}

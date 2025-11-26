package Operimus.SLMS.business_layer.chatbot_module.dto;

import java.util.List;

/**
 * Response payload for chatbot interactions.
 */
public record ChatResponse(
        String response,
        List<String> sourceDocuments  // List of chunk IDs used for context
) {
}

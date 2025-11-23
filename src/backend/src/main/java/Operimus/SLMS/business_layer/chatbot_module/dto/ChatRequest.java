package Operimus.SLMS.business_layer.chatbot_module.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Request payload for chatbot interactions.
 */
public record ChatRequest(
        @NotBlank(message = "Message is required")
        String message,

        List<ChatMessage> conversationHistory,

        String locale  // Optional, defaults to "en-US" if not provided
) {
    public ChatRequest {
        // Normalize locale
        if (locale == null || locale.isBlank()) {
            locale = "en-US";
        }
    }
}

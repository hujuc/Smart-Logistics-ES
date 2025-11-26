package Operimus.SLMS.business_layer.chatbot_module.dto;

/**
 * Represents a single message in a conversation.
 * Used to provide conversation context to the chatbot.
 */
public record ChatMessage(
        String role,    // "user" or "assistant"
        String content
) {
}

package Operimus.SLMS.business_layer.chatbot_module.integration.groq.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Simple request payload that carries the prompt to be enviado para o modelo Groq.
 */
public record GroqPromptRequest(
        @NotBlank(message = "Prompt is required")
        String prompt
) {
}

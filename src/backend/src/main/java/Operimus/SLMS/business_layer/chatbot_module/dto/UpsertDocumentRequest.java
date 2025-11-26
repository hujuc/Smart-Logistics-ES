package Operimus.SLMS.business_layer.chatbot_module.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

/**
 * Request payload for upserting assistant documents.
 */
public record UpsertDocumentRequest(
        @NotBlank(message = "Title is required")
        String title,

        Set<String> roles,

        Set<String> frontends,

        Set<String> routes,

        @NotBlank(message = "Locale is required")
        String locale,

        @NotBlank(message = "Content is required")
        String content,

        String metadataJson
) {
}

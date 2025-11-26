package Operimus.SLMS.business_layer.chatbot_module.dto;

import java.util.Set;

/**
 * DTO for reading assistant documents.
 */
public record DocumentDto(
        String id,
        String chunkId,
        String title,
        Set<String> roles,
        Set<String> frontends,
        Set<String> routes,
        String locale,
        String content,
        String metadataJson,
        String createdAt
) {
}

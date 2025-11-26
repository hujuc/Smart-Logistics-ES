package Operimus.SLMS.business_layer.chatbot_module.integration.gemini.dto;

import java.util.List;

public record GeminiChatResponse(
        List<Candidate> candidates
) {
    public record Candidate(
            Content content,
            String finishReason
    ) {}

    public record Content(
            List<Part> parts,
            String role
    ) {}

    public record Part(
            String text
    ) {}

    /**
     * Extract the text content from the first candidate.
     * @return The generated text, or empty string if no content available
     */
    public String getText() {
        if (candidates == null || candidates.isEmpty()) {
            return "";
        }

        Candidate firstCandidate = candidates.get(0);
        if (firstCandidate.content() == null || firstCandidate.content().parts() == null) {
            return "";
        }

        List<Part> parts = firstCandidate.content().parts();
        if (parts.isEmpty()) {
            return "";
        }

        return parts.get(0).text();
    }
}

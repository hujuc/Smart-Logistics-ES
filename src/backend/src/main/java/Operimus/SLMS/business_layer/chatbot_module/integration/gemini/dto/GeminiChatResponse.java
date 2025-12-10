package Operimus.SLMS.business_layer.chatbot_module.integration.gemini.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            String text,
            @JsonProperty("functionCall") FunctionCall functionCall  // NEW: Support for function calls
    ) {}

    // NEW: Function call representation from Gemini
    public record FunctionCall(
            String name,
            Map<String, Object> args
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

        // Find first text part (skip function calls)
        for (Part part : parts) {
            if (part.text() != null && !part.text().isEmpty()) {
                return part.text();
            }
        }

        return "";
    }

    /**
     * Check if response contains function calls.
     * @return true if LLM requested function calls
     */
    public boolean hasFunctionCalls() {
        if (candidates == null || candidates.isEmpty()) {
            return false;
        }

        Candidate candidate = candidates.get(0);
        if (candidate.content() == null || candidate.content().parts() == null) {
            return false;
        }

        return candidate.content().parts().stream()
                .anyMatch(part -> part.functionCall() != null);
    }

    /**
     * Extract all function calls from the response.
     * @return List of function calls (empty if none)
     */
    public List<FunctionCall> getFunctionCalls() {
        if (!hasFunctionCalls()) {
            return List.of();
        }

        List<FunctionCall> functionCalls = new ArrayList<>();
        Candidate candidate = candidates.get(0);

        for (Part part : candidate.content().parts()) {
            if (part.functionCall() != null) {
                functionCalls.add(part.functionCall());
            }
        }

        return functionCalls;
    }
}

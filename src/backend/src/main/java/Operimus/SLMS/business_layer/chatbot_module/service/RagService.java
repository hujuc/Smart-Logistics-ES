package Operimus.SLMS.business_layer.chatbot_module.service;

import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeClient;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.dto.PineconeQueryResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for Retrieval-Augmented Generation (RAG).
 * Handles document retrieval and prompt augmentation.
 */
@Service
public class RagService {

    private final PineconeClient pineconeClient;

    public RagService(PineconeClient pineconeClient) {
        this.pineconeClient = pineconeClient;
    }

    /**
     * Query relevant documents from Pinecone based on the query embedding.
     * Filters by frontend and role.
     *
     * @param queryEmbedding The embedding vector of the user's query
     * @param frontend The frontend making the request (e.g., "landing-page", "customer-portal")
     * @param role The user's role (e.g., "CUSTOMER", "DRIVER", "ADMIN")
     * @param locale The locale (e.g., "pt-PT")
     * @param topK Number of documents to retrieve
     * @return List of relevant document matches
     */
    public List<PineconeQueryResponse.Match> queryRelevantDocuments(
            float[] queryEmbedding,
            String frontend,
            String role,
            String locale,
            int topK) {

        // Build metadata filter for Pinecone query
        Map<String, Object> filter = new HashMap<>();

        if (frontend != null) {
            filter.put("frontends", Map.of("$in", List.of(frontend)));
        }

        if (role != null) {
            filter.put("roles", Map.of("$in", List.of(role)));
        }

        if (locale != null) {
            filter.put("locale", Map.of("$eq", locale));
        }

        return pineconeClient.query(queryEmbedding, topK, filter);
    }

    /**
     * Augment the user's prompt with retrieved document context.
     *
     * @param userMessage The user's original message
     * @param documents Retrieved documents from Pinecone
     * @return Augmented prompt with context
     */
    public String augmentPrompt(String userMessage, List<PineconeQueryResponse.Match> documents) {
        if (documents == null || documents.isEmpty()) {
            return userMessage;
        }

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("Contexto relevante:\n\n");

        for (int i = 0; i < documents.size(); i++) {
            PineconeQueryResponse.Match doc = documents.get(i);
            Map<String, Object> metadata = doc.metadata();

            if (metadata != null && metadata.containsKey("content")) {
                contextBuilder.append("[Documento ").append(i + 1).append("]\n");
                contextBuilder.append(metadata.get("content")).append("\n\n");
            }
        }

        contextBuilder.append("---\n\n");
        contextBuilder.append("Pergunta do utilizador: ").append(userMessage);

        return contextBuilder.toString();
    }

    /**
     * Extract chunk IDs from retrieved documents.
     *
     * @param documents Retrieved documents
     * @return List of chunk IDs
     */
    public List<String> extractChunkIds(List<PineconeQueryResponse.Match> documents) {
        if (documents == null) {
            return List.of();
        }
        return documents.stream()
                .map(PineconeQueryResponse.Match::id)
                .collect(Collectors.toList());
    }
}

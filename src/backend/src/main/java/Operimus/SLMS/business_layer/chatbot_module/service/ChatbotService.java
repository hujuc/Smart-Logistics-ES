package Operimus.SLMS.business_layer.chatbot_module.service;

import Operimus.SLMS.business_layer.chatbot_module.dto.ChatMessage;
import Operimus.SLMS.business_layer.chatbot_module.dto.ChatRequest;
import Operimus.SLMS.business_layer.chatbot_module.dto.ChatResponse;
import Operimus.SLMS.business_layer.chatbot_module.integration.gemini.GeminiClient;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.dto.PineconeQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main orchestrator service for chatbot interactions.
 * Coordinates embedding generation, RAG retrieval, and LLM completion.
 */
@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);
    private static final int DEFAULT_TOP_K = 5;

    private final EmbeddingService embeddingService;
    private final RagService ragService;
    private final GeminiClient geminiClient;

    public ChatbotService(EmbeddingService embeddingService,
                          RagService ragService,
                          GeminiClient geminiClient) {
        this.embeddingService = embeddingService;
        this.ragService = ragService;
        this.geminiClient = geminiClient;
    }

    /**
     * Generate a chatbot response with RAG context.
     *
     * @param request The chat request containing user message and conversation history
     * @param frontend The frontend making the request (e.g., "landing-page", "customer-portal")
     * @param role The user's role (e.g., "CUSTOMER", "DRIVER", "ADMIN", "ANONYMOUS")
     * @return ChatResponse with the assistant's reply and source documents
     */
    public ChatResponse generateResponse(ChatRequest request, String frontend, String role) {
        try {
            // 1. Generate embedding for the user's message
            log.debug("Generating embedding for message: {}", request.message());
            float[] queryEmbedding = embeddingService.generateEmbedding(request.message());

            // 2. Query relevant documents from Pinecone
            log.debug("Querying Pinecone for relevant documents (frontend={}, role={})", frontend, role);
            List<PineconeQueryResponse.Match> relevantDocs = ragService.queryRelevantDocuments(
                    queryEmbedding,
                    frontend,
                    role,
                    request.locale(),
                    DEFAULT_TOP_K
            );

            // 3. Augment prompt with retrieved context
            String augmentedPrompt = ragService.augmentPrompt(request.message(), relevantDocs);
            log.debug("Augmented prompt with {} documents", relevantDocs.size());

            // 4. Build conversation messages
            List<Map<String, String>> messages = buildConversationMessages(
                    request.conversationHistory(),
                    augmentedPrompt
            );

            // 5. Call Gemini LLM
            String systemPrompt = buildSystemPrompt(frontend, role);
            String geminiResponse = geminiClient.generateCompletion(systemPrompt, messages);

            // 6. Extract source document IDs
            List<String> sourceDocIds = ragService.extractChunkIds(relevantDocs);

            return new ChatResponse(geminiResponse, sourceDocIds);

        } catch (Exception ex) {
            // RAG failed - fall back to non-RAG response
            log.warn("RAG not available. Falling back to direct LLM call. Error: {}", ex.getMessage());
            return generateFallbackResponse(request, frontend, role);
        }
    }

    /**
     * Fallback response without RAG (when RAG is not available).
     */
    private ChatResponse generateFallbackResponse(ChatRequest request, String frontend, String role) {
        List<Map<String, String>> messages = buildConversationMessages(
                request.conversationHistory(),
                request.message()
        );

        String systemPrompt = buildSystemPrompt(frontend, role);
        String geminiResponse = geminiClient.generateCompletion(systemPrompt, messages);

        return new ChatResponse(geminiResponse, List.of());
    }

    /**
     * Build system prompt based on frontend and role.
     */
    private String buildSystemPrompt(String frontend, String role) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a helpful assistant specialized in logistics and supply chain management. ");
        prompt.append("You work for the Smart Logistics Management System (SLMS). ");

        // Customize based on frontend
        switch (frontend != null ? frontend : "landing-page") {
            case "customer-portal":
                prompt.append("You are helping a customer track orders and resolve delivery issues. ");
                break;
            case "driver-pwa":
                prompt.append("You are helping a driver with routes, deliveries, and operational procedures. ");
                break;
            case "admin-console":
                prompt.append("You are helping an administrator or logistics manager with operations and reports. ");
                break;
            case "landing-page":
            default:
                prompt.append("You are helping a visitor understand the SLMS system and its features. ");
                break;
        }

        prompt.append("Always respond in English (en-US) in a clear and professional manner.");

        return prompt.toString();
    }

    /**
     * Build conversation messages for Groq API.
     */
    private List<Map<String, String>> buildConversationMessages(
            List<ChatMessage> history,
            String currentMessage) {

        List<Map<String, String>> messages = new ArrayList<>();

        // Add conversation history
        if (history != null && !history.isEmpty()) {
            for (ChatMessage msg : history) {
                messages.add(Map.of("role", msg.role(), "content", msg.content()));
            }
        }

        // Add current user message
        messages.add(Map.of("role", "user", "content", currentMessage));

        return messages;
    }
}

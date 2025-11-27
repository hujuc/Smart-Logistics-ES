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
        // Check if this is a command (starts with /)
        if (request.message().trim().startsWith("/")) {
            return handleCommand(request.message().trim());
        }

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
     * Handle hardcoded chatbot commands.
     *
     * @param message The command message (starting with /)
     * @return ChatResponse with the command result
     */
    private ChatResponse handleCommand(String message) {
        String command = message.toLowerCase().split("\\s+")[0];

        switch (command) {
            case "/help":
                String helpText = buildHelpMessage();
                return new ChatResponse(helpText, List.of());

            default:
                String unknownCommandMsg = "Unknown command: " + command + "\n" +
                        "Type /help to see available commands.";
                return new ChatResponse(unknownCommandMsg, List.of());
        }
    }

    /**
     * Build the help message with available commands and information.
     */
    private String buildHelpMessage() {
        return """
                SLMS Chatbot Help

                Available Commands:
                /help - Show this help message

                About:
                I'm your Smart Logistics assistant. I can help you with:
                - Tracking orders and shipments
                - Delivery information
                - System features and capabilities
                - General logistics questions

                Just type your question and I'll assist you!
                """;
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

        // Response formatting instructions
        prompt.append("Always respond in English (en-US) in a clear and professional manner. ");
        prompt.append("IMPORTANT: Keep your responses brief and concise - aim for 2-3 sentences maximum. ");
        prompt.append("This is a chatbot conversation, so provide direct, conversational answers. ");
        prompt.append("Do not use markdown formatting - avoid asterisks (**), hashtags (##), or any markdown syntax. ");
        prompt.append("Respond in plain text only without any special formatting.");

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

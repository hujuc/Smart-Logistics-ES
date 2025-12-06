package Operimus.SLMS.business_layer.chatbot_module.service;

import Operimus.SLMS.business_layer.chatbot_module.dto.*;
import Operimus.SLMS.business_layer.chatbot_module.integration.gemini.GeminiClient;
import Operimus.SLMS.business_layer.chatbot_module.integration.gemini.dto.GeminiChatResponse;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.dto.PineconeQueryResponse;
import Operimus.SLMS.business_layer.chatbot_module.tools.ToolExecutor;
import Operimus.SLMS.business_layer.chatbot_module.tools.ToolRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main orchestrator service for chatbot interactions.
 * Coordinates embedding generation, RAG retrieval, and LLM completion.
 */
@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);
    private static final int DEFAULT_TOP_K = 5;
    private static final int MAX_TOOL_ITERATIONS = 3;  // Prevent infinite loops

    private final EmbeddingService embeddingService;
    private final RagService ragService;
    private final GeminiClient geminiClient;
    private final ToolRegistry toolRegistry;
    private final ToolExecutor toolExecutor;
    private final ObjectMapper objectMapper;

    public ChatbotService(EmbeddingService embeddingService,
                          RagService ragService,
                          GeminiClient geminiClient,
                          ToolRegistry toolRegistry,
                          ToolExecutor toolExecutor,
                          ObjectMapper objectMapper) {
        this.embeddingService = embeddingService;
        this.ragService = ragService;
        this.geminiClient = geminiClient;
        this.toolRegistry = toolRegistry;
        this.toolExecutor = toolExecutor;
        this.objectMapper = objectMapper;
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
        return generateResponse(request, frontend, role, null);
    }

    /**
     * Generate a chatbot response with RAG context and tool calling support.
     *
     * @param request The chat request containing user message and conversation history
     * @param frontend The frontend making the request (e.g., "landing-page", "customer-portal")
     * @param role The user's role (e.g., "CUSTOMER", "DRIVER", "ADMIN", "ANONYMOUS")
     * @param userId The user's ID for tool authorization (can be null for anonymous)
     * @return ChatResponse with the assistant's reply and source documents
     */
    public ChatResponse generateResponse(ChatRequest request, String frontend, String role, String userId) {
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

            // 5. Get all tools (not just role-specific) so LLM can provide authorization messages
            List<ToolDefinition> allTools = toolRegistry.getAllToolDefinitions();
            List<ToolDefinition> allowedTools = toolRegistry.getToolDefinitionsForRole(role);

            if (allTools.isEmpty()) {
                log.warn("No tools registered in system - chatbot cannot access database");
            } else {
                log.info("Providing {} total tools to LLM (user role {} has access to {}): {}",
                    allTools.size(), role, allowedTools.size(),
                    allowedTools.stream().map(ToolDefinition::name).toList());
            }

            // 6. Call Gemini LLM with all tools + authorization guidance in system prompt
            String systemPrompt = buildSystemPrompt(frontend, role, allTools, allowedTools);
            UserContext userContext = new UserContext(userId, role, Set.of(role));

            GeminiChatResponse response = geminiClient.generateCompletionWithTools(
                systemPrompt,
                messages,
                allTools.isEmpty() ? null : allTools
            );

            // 7. Tool calling loop
            int iterations = 0;
            while (response.hasFunctionCalls() && iterations < MAX_TOOL_ITERATIONS) {
                log.info("LLM requested {} tool calls (iteration {})",
                    response.getFunctionCalls().size(), iterations + 1);

                // Execute each tool call
                List<ToolResult> toolResults = new ArrayList<>();
                for (GeminiChatResponse.FunctionCall functionCall : response.getFunctionCalls()) {
                    ToolCall toolCall = new ToolCall(
                        UUID.randomUUID().toString(),
                        functionCall.name(),
                        functionCall.args()
                    );

                    ToolResult result = toolExecutor.execute(toolCall, userContext);
                    toolResults.add(result);

                    log.debug("Tool {} executed: success={}",
                        functionCall.name(), result.success());
                }

                // Add tool results to conversation and continue
                messages = addToolResultsToMessages(messages, toolResults);
                response = geminiClient.generateCompletionWithTools(
                    systemPrompt,
                    messages,
                    allTools.isEmpty() ? null : allTools
                );

                iterations++;
            }

            if (iterations >= MAX_TOOL_ITERATIONS && response.hasFunctionCalls()) {
                log.warn("Reached max tool iterations ({}) - stopping tool calling loop", MAX_TOOL_ITERATIONS);
            }

            // 8. Extract final text response
            String finalResponse = response.getText();
            List<String> sourceDocIds = ragService.extractChunkIds(relevantDocs);

            return new ChatResponse(finalResponse, sourceDocIds);

        } catch (Exception ex) {
            // Tool calling or RAG failed - fall back to non-RAG response
            log.warn("Tool-enabled response failed. Falling back to direct LLM call. Error: {}", ex.getMessage());
            return generateFallbackResponse(request, frontend, role);
        }
    }

    /**
     * Fallback response without RAG (when RAG is not available).
     * Still includes tools if available, just without RAG context.
     */
    private ChatResponse generateFallbackResponse(ChatRequest request, String frontend, String role) {
        List<Map<String, String>> messages = buildConversationMessages(
                request.conversationHistory(),
                request.message()
        );

        // Get all tools even in fallback mode for authorization-aware responses
        List<ToolDefinition> allTools = toolRegistry.getAllToolDefinitions();
        List<ToolDefinition> allowedTools = toolRegistry.getToolDefinitionsForRole(role);
        String systemPrompt = buildSystemPrompt(frontend, role, allTools, allowedTools);
        GeminiChatResponse response = geminiClient.generateCompletionWithTools(systemPrompt, messages,
                allTools.isEmpty() ? null : allTools);
        String geminiResponse = response.getText();

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
     * Includes tool usage instructions and authorization guidance.
     */
    private String buildSystemPrompt(String frontend, String role,
                                    List<ToolDefinition> allTools,
                                    List<ToolDefinition> allowedTools) {
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

        // Tool usage instructions (if tools are available)
        if (allTools != null && !allTools.isEmpty()) {
            prompt.append("\n\nIMPORTANT: You have access to database query tools to retrieve real-time information. ");
            prompt.append("When users ask about specific orders, shipments, warehouses, or delivery issues, ");
            prompt.append("you MUST use the available tools to fetch accurate data from the database. ");
            prompt.append("Do NOT guess or make up information about order IDs, tracking numbers, statuses, or delivery details. ");
            prompt.append("Always use the appropriate tool first, then provide your answer based on the actual tool results. ");

            prompt.append("\n\nAvailable tools in the system:\n");
            for (ToolDefinition tool : allTools) {
                prompt.append("- ").append(tool.name()).append(": ").append(tool.description()).append("\n");
            }

            // Add authorization guidance
            prompt.append(buildAuthorizationGuidance(role, allTools, allowedTools));

            prompt.append("\nTool usage guidelines:\n");
            prompt.append("- If asked about a specific order (by ID or tracking number), use order_lookup or order_status\n");
            prompt.append("- If asked about warehouse information or inventory, use warehouse_inventory_tool\n");
            prompt.append("- If asked about shipment tracking, use shipment_tracking_tool\n");
            prompt.append("- If asked about delivery problems or exceptions, use delivery_exception_tool\n");
            prompt.append("- Always verify the information exists in the database before responding\n");
        }

        // Response formatting instructions
        prompt.append("\nAlways respond in English (en-US) in a clear and professional manner. ");
        prompt.append("IMPORTANT: Keep your responses brief and concise - aim for 2-3 sentences maximum. ");
        prompt.append("This is a chatbot conversation, so provide direct, conversational answers. ");
        prompt.append("Do not use markdown formatting - avoid asterisks (**), hashtags (##), or any markdown syntax. ");
        prompt.append("Respond in plain text only without any special formatting.");

        return prompt.toString();
    }

    /**
     * Build authorization guidance for the system prompt.
     * Tells the LLM which tools the user can access and how to respond to unauthorized requests.
     */
    private String buildAuthorizationGuidance(String role,
                                             List<ToolDefinition> allTools,
                                             List<ToolDefinition> allowedTools) {
        StringBuilder guidance = new StringBuilder();

        // Get list of tool names the user can and cannot access
        Set<String> allowedToolNames = allowedTools.stream()
            .map(ToolDefinition::name)
            .collect(Collectors.toSet());

        List<String> unauthorizedToolNames = allTools.stream()
            .map(ToolDefinition::name)
            .filter(name -> !allowedToolNames.contains(name))
            .toList();

        guidance.append("\n\nAUTHORIZATION RULES:\n");
        guidance.append("Current user role: ").append(role).append("\n");

        if (!allowedToolNames.isEmpty()) {
            guidance.append("You CAN use these tools: ").append(String.join(", ", allowedToolNames)).append("\n");
        }

        if (!unauthorizedToolNames.isEmpty()) {
            guidance.append("You CANNOT use these tools: ").append(String.join(", ", unauthorizedToolNames)).append("\n");
            guidance.append("\nIMPORTANT: When a user asks for information that requires an unauthorized tool:\n");
            guidance.append("1. DO NOT attempt to call the unauthorized tool\n");
            guidance.append("2. Politely explain that the user doesn't have access to that information\n");
            guidance.append("3. Explain which user roles DO have access to that feature\n");
            guidance.append("4. Suggest alternative actions the user CAN take with their current role\n");

            // Add role-specific guidance
            guidance.append("\n").append(getRoleSpecificGuidance(role));
        }

        return guidance.toString();
    }

    /**
     * Get role-specific authorization guidance messages.
     */
    private String getRoleSpecificGuidance(String role) {
        return switch (role != null ? role : "ANONYMOUS") {
            case "CUSTOMER" -> """
                For CUSTOMER role:
                - Can access: Order tracking, shipment status, delivery information for their OWN orders
                - Cannot access: Warehouse inventory, other customers' orders, internal logistics information
                - If asked about warehouses: Explain this is restricted to warehouse staff and managers
                - If asked about other customers' orders: Explain privacy restrictions
                """;
            case "DRIVER" -> """
                For DRIVER role:
                - Can access: Delivery assignments, route information, order details for assigned deliveries
                - Cannot access: Warehouse inventory management, customer personal data beyond delivery needs
                - If asked about warehouse inventory: Explain this is restricted to warehouse staff and managers
                """;
            case "WAREHOUSE_STAFF" -> """
                For WAREHOUSE_STAFF role:
                - Can access: Warehouse inventory, order processing, warehouse operations
                - Cannot access: Some system-wide analytics or management features
                - If asked about restricted features: Explain these may require logistics manager access
                """;
            case "CUSTOMER_SERVICE" -> """
                For CUSTOMER_SERVICE role:
                - Can access: Customer orders, delivery tracking, issue resolution
                - Cannot access: Direct warehouse inventory management, some administrative functions
                - If asked about warehouse operations: Explain this requires warehouse staff access
                """;
            case "LOGISTICS_MANAGER" -> """
                For LOGISTICS_MANAGER role:
                - Can access: All tools and features (full system access)
                - This is the highest privilege level in the system
                """;
            case "ANONYMOUS" -> """
                For ANONYMOUS (landing page) users:
                - Can access: General information about the SLMS system and its features
                - Cannot access: Any database tools or real-time information
                - If asked about orders/shipments: Suggest they log in to the customer portal
                - If asked about warehouse/logistics: Explain these are internal features requiring authentication
                """;
            default -> """
                For your current role:
                - You have limited access to system features
                - Some tools require authentication or higher privilege levels
                - Suggest logging in or contacting an administrator for restricted features
                """;
        };
    }

    /**
     * Build conversation messages for Gemini API.
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

    /**
     * Add tool results to message history for next LLM call.
     */
    private List<Map<String, String>> addToolResultsToMessages(
            List<Map<String, String>> messages,
            List<ToolResult> toolResults) {

        List<Map<String, String>> updatedMessages = new ArrayList<>(messages);

        // Add tool results as assistant messages
        for (ToolResult result : toolResults) {
            String resultMessage = result.success() ?
                "Tool result: " + result.result() :
                "Tool error: " + result.errorMessage();

            updatedMessages.add(Map.of("role", "assistant", "content", resultMessage));
        }

        return updatedMessages;
    }
}

package Operimus.SLMS.business_layer.chatbot_module.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Operimus.SLMS.business_layer.chatbot_module.dto.*;
import Operimus.SLMS.business_layer.chatbot_module.integration.gemini.GeminiClient;
import Operimus.SLMS.business_layer.chatbot_module.integration.gemini.dto.GeminiChatResponse;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.dto.PineconeQueryResponse;
import Operimus.SLMS.business_layer.chatbot_module.tools.ToolExecutor;
import Operimus.SLMS.business_layer.chatbot_module.tools.ToolRegistry;
import Operimus.SLMS.business_layer.shared.exception.ExternalServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for ChatbotService.
 * Verifies the complete RAG orchestration flow including embedding, retrieval, and LLM completion.
 */
@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private RagService ragService;

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private ToolRegistry toolRegistry;

    @Mock
    private ToolExecutor toolExecutor;

    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<String> systemPromptCaptor;

    @Captor
    private ArgumentCaptor<List<Map<String, String>>> messagesCaptor;

    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        chatbotService = new ChatbotService(embeddingService, ragService, geminiClient,
            toolRegistry, toolExecutor, objectMapper);
    }

    @Test
    void generateResponse_shouldCompleteFullRagFlow() {
        // Given
        ChatRequest request = new ChatRequest(
                "How do I track my order?",
                List.of(),
                "en-US"
        );
        String frontend = "customer-portal";
        String role = "CUSTOMER";

        float[] embedding = new float[768];
        List<PineconeQueryResponse.Match> relevantDocs = List.of(
                createMatch("doc1", 0.95f, Map.of(
                        "content", "To track your order, go to My Orders page.",
                        "title", "Order Tracking"
                ))
        );
        String augmentedPrompt = "Context: To track your order...\nQuestion: How do I track my order?";
        String llmResponse = "You can track your order by navigating to the My Orders page in your customer portal.";

        when(embeddingService.generateEmbedding(request.message())).thenReturn(embedding);
        when(ragService.queryRelevantDocuments(embedding, frontend, role, "en-US", 5)).thenReturn(relevantDocs);
        when(ragService.augmentPrompt(request.message(), relevantDocs)).thenReturn(augmentedPrompt);
        when(ragService.extractChunkIds(relevantDocs)).thenReturn(List.of("doc1"));
        when(toolRegistry.getAllToolDefinitions()).thenReturn(List.of());
        when(toolRegistry.getToolDefinitionsForRole(role)).thenReturn(List.of());
        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), any()))
            .thenReturn(createTextResponse(llmResponse));

        // When
        ChatResponse response = chatbotService.generateResponse(request, frontend, role);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.response()).isEqualTo(llmResponse);
        assertThat(response.sourceDocuments()).containsExactly("doc1");

        // Verify the full flow
        verify(embeddingService).generateEmbedding("How do I track my order?");
        verify(ragService).queryRelevantDocuments(embedding, frontend, role, "en-US", 5);
        verify(ragService).augmentPrompt(request.message(), relevantDocs);
        verify(geminiClient).generateCompletionWithTools(anyString(), anyList(), any());
    }

    @Test
    void generateResponse_shouldUseAugmentedPrompt_inLlmCall() {
        // Given
        ChatRequest request = new ChatRequest("Test question", List.of(), "en-US");
        String frontend = "landing-page";
        String role = "ANONYMOUS";

        float[] embedding = new float[768];
        List<PineconeQueryResponse.Match> relevantDocs = List.of(
                createMatch("doc1", 0.9f, Map.of("content", "Context info", "title", "Test"))
        );
        String augmentedPrompt = "Augmented prompt with context";

        when(embeddingService.generateEmbedding(anyString())).thenReturn(embedding);
        when(ragService.queryRelevantDocuments(any(), anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(relevantDocs);
        when(ragService.augmentPrompt(anyString(), anyList())).thenReturn(augmentedPrompt);
        when(ragService.extractChunkIds(anyList())).thenReturn(List.of("doc1"));
        when(toolRegistry.getAllToolDefinitions()).thenReturn(List.of());
        when(toolRegistry.getToolDefinitionsForRole(role)).thenReturn(List.of());
        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), any()))
            .thenReturn(createTextResponse("Response"));

        // When
        chatbotService.generateResponse(request, frontend, role);

        // Then
        verify(geminiClient).generateCompletionWithTools(anyString(), messagesCaptor.capture(), any());
        List<Map<String, String>> messages = messagesCaptor.getValue();

        // Last message should contain the augmented prompt
        assertThat(messages).isNotEmpty();
        Map<String, String> lastMessage = messages.get(messages.size() - 1);
        assertThat(lastMessage.get("role")).isEqualTo("user");
        assertThat(lastMessage.get("content")).isEqualTo(augmentedPrompt);
    }

    @Test
    void generateResponse_shouldBuildSystemPrompt_forCustomerPortal() {
        // Given
        ChatRequest request = new ChatRequest("Test", List.of(), "en-US");
        String frontend = "customer-portal";
        String role = "CUSTOMER";

        setupMocksForSuccessfulFlow();

        // When
        chatbotService.generateResponse(request, frontend, role);

        // Then
        verify(geminiClient).generateCompletionWithTools(systemPromptCaptor.capture(), anyList(), any());
        String systemPrompt = systemPromptCaptor.getValue();

        assertThat(systemPrompt).contains("Smart Logistics Management System");
        assertThat(systemPrompt).contains("helping a customer track orders and resolve delivery issues");
    }

    @Test
    void generateResponse_shouldBuildSystemPrompt_forDriverPwa() {
        // Given
        ChatRequest request = new ChatRequest("Test", List.of(), "en-US");
        String frontend = "driver-pwa";
        String role = "DRIVER";

        setupMocksForSuccessfulFlow();

        // When
        chatbotService.generateResponse(request, frontend, role);

        // Then
        verify(geminiClient).generateCompletionWithTools(systemPromptCaptor.capture(), anyList(), any());
        String systemPrompt = systemPromptCaptor.getValue();

        assertThat(systemPrompt).contains("helping a driver with routes, deliveries, and operational procedures");
    }

    @Test
    void generateResponse_shouldBuildSystemPrompt_forAdminConsole() {
        // Given
        ChatRequest request = new ChatRequest("Test", List.of(), "en-US");
        String frontend = "admin-console";
        String role = "ADMIN";

        setupMocksForSuccessfulFlow();

        // When
        chatbotService.generateResponse(request, frontend, role);

        // Then
        verify(geminiClient).generateCompletionWithTools(systemPromptCaptor.capture(), anyList(), any());
        String systemPrompt = systemPromptCaptor.getValue();

        assertThat(systemPrompt).contains("helping an administrator or logistics manager");
    }

    @Test
    void generateResponse_shouldBuildSystemPrompt_forLandingPage() {
        // Given
        ChatRequest request = new ChatRequest("Test", List.of(), "en-US");
        String frontend = "landing-page";
        String role = "ANONYMOUS";

        setupMocksForSuccessfulFlow();

        // When
        chatbotService.generateResponse(request, frontend, role);

        // Then
        verify(geminiClient).generateCompletionWithTools(systemPromptCaptor.capture(), anyList(), any());
        String systemPrompt = systemPromptCaptor.getValue();

        assertThat(systemPrompt).contains("helping a visitor understand the SLMS system");
    }

    @Test
    void generateResponse_shouldIncludeConversationHistory() {
        // Given
        List<ChatMessage> history = List.of(
                new ChatMessage("user", "What is SLMS?"),
                new ChatMessage("assistant", "SLMS is a Smart Logistics Management System.")
        );
        ChatRequest request = new ChatRequest("Tell me more", history, "en-US");

        setupMocksForSuccessfulFlow();

        // When
        chatbotService.generateResponse(request, "landing-page", "ANONYMOUS");

        // Then
        verify(geminiClient).generateCompletionWithTools(anyString(), messagesCaptor.capture(), any());
        List<Map<String, String>> messages = messagesCaptor.getValue();

        // Should have history + current message
        assertThat(messages).hasSizeGreaterThanOrEqualTo(3);
        assertThat(messages.get(0).get("content")).isEqualTo("What is SLMS?");
        assertThat(messages.get(1).get("content")).isEqualTo("SLMS is a Smart Logistics Management System.");
    }

    @Test
    void generateResponse_shouldFallbackToNonRag_whenEmbeddingFails() {
        // Given
        ChatRequest request = new ChatRequest("Test question", List.of(), "en-US");
        String frontend = "customer-portal";
        String role = "CUSTOMER";

        when(embeddingService.generateEmbedding(anyString()))
                .thenThrow(new ExternalServiceException("Embedding service unavailable"));
        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), any()))
                .thenReturn(createTextResponse("Fallback response"));

        // When
        ChatResponse response = chatbotService.generateResponse(request, frontend, role);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.response()).isEqualTo("Fallback response");
        assertThat(response.sourceDocuments()).isEmpty(); // No RAG context

        // RAG services should not be called
        verify(ragService, never()).queryRelevantDocuments(any(), anyString(), anyString(), anyString(), anyInt());
        verify(ragService, never()).augmentPrompt(anyString(), anyList());

        // LLM should still be called with original prompt
        verify(geminiClient).generateCompletionWithTools(anyString(), argThat(messages -> {
            Map<String, String> lastMessage = messages.get(messages.size() - 1);
            return lastMessage.get("content").equals("Test question"); // Original, not augmented
        }), any());
    }

    @Test
    void generateResponse_shouldFallbackToNonRag_whenPineconeQueryFails() {
        // Given
        ChatRequest request = new ChatRequest("Test question", List.of(), "en-US");
        String frontend = "customer-portal";
        String role = "CUSTOMER";

        float[] embedding = new float[768];
        when(embeddingService.generateEmbedding(anyString())).thenReturn(embedding);
        when(ragService.queryRelevantDocuments(any(), anyString(), anyString(), anyString(), anyInt()))
                .thenThrow(new ExternalServiceException("Pinecone unavailable"));
        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), any()))
                .thenReturn(createTextResponse("Fallback response"));

        // When
        ChatResponse response = chatbotService.generateResponse(request, frontend, role);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.response()).isEqualTo("Fallback response");
        assertThat(response.sourceDocuments()).isEmpty();

        // augmentPrompt should not be called
        verify(ragService, never()).augmentPrompt(anyString(), anyList());
    }

    @Test
    void generateResponse_shouldHandleNullFrontend() {
        // Given
        ChatRequest request = new ChatRequest("Test", List.of(), "en-US");

        setupMocksForSuccessfulFlow();

        // When
        ChatResponse response = chatbotService.generateResponse(request, null, "CUSTOMER");

        // Then
        assertThat(response).isNotNull();
        verify(geminiClient).generateCompletionWithTools(systemPromptCaptor.capture(), anyList(), any());
        String systemPrompt = systemPromptCaptor.getValue();

        // Should default to landing-page
        assertThat(systemPrompt).contains("helping a visitor understand the SLMS system");
    }

    @Test
    void generateResponse_shouldUseDefaultLocale_whenNotProvided() {
        // Given
        ChatRequest request = new ChatRequest("Test", List.of(), null);

        setupMocksForSuccessfulFlow();

        // When
        chatbotService.generateResponse(request, "customer-portal", "CUSTOMER");

        // Then
        verify(ragService).queryRelevantDocuments(any(), anyString(), anyString(), eq("en-US"), anyInt());
    }

    // ==================== NEW: Tool Calling Tests ====================

    @Test
    void shouldCallToolWhenLLMRequestsIt() {
        // Given
        ChatRequest request = new ChatRequest("What's the status of order 123?", List.of(), "en-US");
        String frontend = "customer-portal";
        String role = "CUSTOMER";
        String userId = "customer-1";

        float[] embedding = new float[768];
        List<PineconeQueryResponse.Match> relevantDocs = List.of();
        List<ToolDefinition> tools = List.of(new ToolDefinition("order_status", "Check order status", List.of()));

        // Mock RAG flow
        when(embeddingService.generateEmbedding(anyString())).thenReturn(embedding);
        when(ragService.queryRelevantDocuments(any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(relevantDocs);
        when(ragService.augmentPrompt(anyString(), anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(ragService.extractChunkIds(anyList())).thenReturn(List.of());
        when(toolRegistry.getAllToolDefinitions()).thenReturn(tools);
        when(toolRegistry.getToolDefinitionsForRole(role)).thenReturn(tools);

        // Mock LLM requesting a tool call
        GeminiChatResponse firstResponse = createResponseWithFunctionCall("order_status", Map.of("orderId", 123));
        GeminiChatResponse finalResponse = createTextResponse("Order 123 is in transit");

        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), anyList()))
            .thenReturn(firstResponse)  // First call: LLM requests tool
            .thenReturn(finalResponse);  // Second call: LLM provides final answer

        // Mock tool execution
        ToolResult toolResult = ToolResult.success("call-1", "{\"status\": \"IN_TRANSIT\"}");
        when(toolExecutor.execute(any(ToolCall.class), any(UserContext.class))).thenReturn(toolResult);

        // When
        ChatResponse response = chatbotService.generateResponse(request, frontend, role, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.response()).isEqualTo("Order 123 is in transit");

        verify(toolRegistry).getToolDefinitionsForRole(role);
        verify(toolExecutor).execute(any(ToolCall.class), any(UserContext.class));
        verify(geminiClient, org.mockito.Mockito.times(2)).generateCompletionWithTools(anyString(), anyList(), anyList());
    }

    @Test
    void shouldExecuteMultipleToolsInSequence() {
        // Given
        ChatRequest request = new ChatRequest("Check order 123 and 456", List.of(), "en-US");

        setupMocksForToolCallingFlow();

        // Mock LLM requesting two tool calls
        GeminiChatResponse response1 = createResponseWithMultipleFunctionCalls(
            List.of(
                new GeminiChatResponse.FunctionCall("order_status", Map.of("orderId", 123)),
                new GeminiChatResponse.FunctionCall("order_status", Map.of("orderId", 456))
            )
        );
        GeminiChatResponse finalResponse = createTextResponse("Both orders are in transit");

        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), any()))
            .thenReturn(response1)
            .thenReturn(finalResponse);

        ToolResult result1 = ToolResult.success("call-1", "{\"status\": \"IN_TRANSIT\"}");
        ToolResult result2 = ToolResult.success("call-2", "{\"status\": \"IN_TRANSIT\"}");
        when(toolExecutor.execute(any(ToolCall.class), any(UserContext.class)))
            .thenReturn(result1)
            .thenReturn(result2);

        // When
        ChatResponse response = chatbotService.generateResponse(request, "customer-portal", "CUSTOMER", "user-1");

        // Then
        assertThat(response).isNotNull();
        verify(toolExecutor, org.mockito.Mockito.times(2)).execute(any(ToolCall.class), any(UserContext.class));
    }

    @Test
    void shouldStopAfterMaxIterations() {
        // Given - LLM keeps requesting tools beyond the limit
        ChatRequest request = new ChatRequest("Test", List.of(), "en-US");

        setupMocksForToolCallingFlow();

        // Mock LLM always requesting tools (infinite loop scenario)
        GeminiChatResponse toolCallResponse = createResponseWithFunctionCall("order_status", Map.of("orderId", 123));
        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), any()))
            .thenReturn(toolCallResponse); // Always returns a tool call

        when(toolExecutor.execute(any(ToolCall.class), any(UserContext.class)))
            .thenReturn(ToolResult.success("call-1", "{}"));

        // When
        ChatResponse response = chatbotService.generateResponse(request, "customer-portal", "CUSTOMER", "user-1");

        // Then - Should stop after 3 iterations (MAX_TOOL_ITERATIONS)
        verify(toolExecutor, org.mockito.Mockito.times(3)).execute(any(ToolCall.class), any(UserContext.class));
    }

    @Test
    void shouldHandleToolExecutionFailure() {
        // Given
        ChatRequest request = new ChatRequest("What's order 123?", List.of(), "en-US");

        setupMocksForToolCallingFlow();

        GeminiChatResponse toolCallResponse = createResponseWithFunctionCall("order_status", Map.of("orderId", 123));
        GeminiChatResponse finalResponse = createTextResponse("Sorry, I couldn't get order info");

        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), any()))
            .thenReturn(toolCallResponse)
            .thenReturn(finalResponse);

        // Tool execution fails
        ToolResult failedResult = ToolResult.error("call-1", "Order not found");
        when(toolExecutor.execute(any(ToolCall.class), any(UserContext.class))).thenReturn(failedResult);

        // When
        ChatResponse response = chatbotService.generateResponse(request, "customer-portal", "CUSTOMER", "user-1");

        // Then - Should still return a response
        assertThat(response).isNotNull();
        assertThat(response.response()).isEqualTo("Sorry, I couldn't get order info");
    }

    @Test
    void shouldWorkWithBothRagAndTools() {
        // Given - Request that uses both RAG context and tool calling
        ChatRequest request = new ChatRequest("How do I track order 123?", List.of(), "en-US");

        float[] embedding = new float[768];
        List<PineconeQueryResponse.Match> relevantDocs = List.of(
            createMatch("doc1", 0.95f, Map.of("content", "Tracking info", "title", "Guide"))
        );

        when(embeddingService.generateEmbedding(anyString())).thenReturn(embedding);
        when(ragService.queryRelevantDocuments(any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(relevantDocs);
        when(ragService.augmentPrompt(anyString(), anyList())).thenReturn("Augmented prompt");
        when(ragService.extractChunkIds(anyList())).thenReturn(List.of("doc1"));

        List<ToolDefinition> tools = List.of(new ToolDefinition("order_status", "Check order", List.of()));
        when(toolRegistry.getAllToolDefinitions()).thenReturn(tools);
        when(toolRegistry.getToolDefinitionsForRole(anyString())).thenReturn(tools);

        GeminiChatResponse toolCall = createResponseWithFunctionCall("order_status", Map.of("orderId", 123));
        GeminiChatResponse finalResponse = createTextResponse("Order 123 is in transit. Check My Orders page.");

        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), anyList()))
            .thenReturn(toolCall)
            .thenReturn(finalResponse);

        when(toolExecutor.execute(any(ToolCall.class), any(UserContext.class)))
            .thenReturn(ToolResult.success("call-1", "{\"status\": \"IN_TRANSIT\"}"));

        // When
        ChatResponse response = chatbotService.generateResponse(request, "customer-portal", "CUSTOMER", "user-1");

        // Then - Should use both RAG and tools
        assertThat(response).isNotNull();
        assertThat(response.sourceDocuments()).containsExactly("doc1");
        verify(ragService).queryRelevantDocuments(any(), anyString(), anyString(), anyString(), anyInt());
        verify(toolExecutor).execute(any(ToolCall.class), any(UserContext.class));
    }

    @Test
    void shouldPassUserIdToToolContext() {
        // Given
        ChatRequest request = new ChatRequest("Test", List.of(), "en-US");
        String userId = "customer-123";
        String role = "CUSTOMER";

        setupMocksForToolCallingFlow();

        GeminiChatResponse toolCallResponse = createResponseWithFunctionCall("order_status", Map.of("orderId", 123));
        GeminiChatResponse finalResponse = createTextResponse("Done");

        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), any()))
            .thenReturn(toolCallResponse)
            .thenReturn(finalResponse);

        when(toolExecutor.execute(any(ToolCall.class), any(UserContext.class)))
            .thenReturn(ToolResult.success("call-1", "{}"));

        // When
        chatbotService.generateResponse(request, "customer-portal", role, userId);

        // Then - Verify userId and role are passed correctly
        ArgumentCaptor<UserContext> contextCaptor = ArgumentCaptor.forClass(UserContext.class);
        verify(toolExecutor).execute(any(ToolCall.class), contextCaptor.capture());

        UserContext capturedContext = contextCaptor.getValue();
        assertThat(capturedContext.userId()).isEqualTo(userId);
        assertThat(capturedContext.role()).isEqualTo(role);
    }

    @Test
    void shouldHandleNullUserId() {
        // Given - Anonymous user (landing page)
        ChatRequest request = new ChatRequest("Test", List.of(), "en-US");

        setupMocksForToolCallingFlow();

        GeminiChatResponse finalResponse = createTextResponse("Response");
        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), any()))
            .thenReturn(finalResponse); // No tool calls

        // When
        ChatResponse response = chatbotService.generateResponse(request, "landing-page", "ANONYMOUS", null);

        // Then - Should work without userId
        assertThat(response).isNotNull();
    }

    // Helper methods
    private void setupMocksForSuccessfulFlow() {
        float[] embedding = new float[768];
        List<PineconeQueryResponse.Match> docs = List.of();

        when(embeddingService.generateEmbedding(anyString())).thenReturn(embedding);
        when(ragService.queryRelevantDocuments(any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(docs);
        when(ragService.augmentPrompt(anyString(), anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(ragService.extractChunkIds(anyList())).thenReturn(List.of());
        // Lenient stubs since only one of these methods will be called depending on tool availability
        lenient().when(geminiClient.generateCompletion(anyString(), anyList())).thenReturn("Response");
        when(toolRegistry.getAllToolDefinitions()).thenReturn(List.of());
        when(toolRegistry.getToolDefinitionsForRole(anyString())).thenReturn(List.of());
        lenient().when(geminiClient.generateCompletionWithTools(anyString(), anyList(), any()))
            .thenReturn(createTextResponse("Response"));
    }

    private void setupMocksForToolCallingFlow() {
        float[] embedding = new float[768];
        List<PineconeQueryResponse.Match> docs = List.of();
        List<ToolDefinition> tools = List.of(new ToolDefinition("order_status", "Check order", List.of()));

        when(embeddingService.generateEmbedding(anyString())).thenReturn(embedding);
        when(ragService.queryRelevantDocuments(any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(docs);
        when(ragService.augmentPrompt(anyString(), anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(ragService.extractChunkIds(anyList())).thenReturn(List.of());
        when(toolRegistry.getAllToolDefinitions()).thenReturn(tools);
        when(toolRegistry.getToolDefinitionsForRole(anyString())).thenReturn(tools);
    }

    private GeminiChatResponse createTextResponse(String text) {
        GeminiChatResponse.Content content = new GeminiChatResponse.Content(
                List.of(new GeminiChatResponse.Part(text, null)),
                "model"
        );
        GeminiChatResponse.Candidate candidate = new GeminiChatResponse.Candidate(content, "STOP");
        return new GeminiChatResponse(List.of(candidate));
    }

    private GeminiChatResponse createResponseWithFunctionCall(String functionName, Map<String, Object> args) {
        GeminiChatResponse.FunctionCall functionCall = new GeminiChatResponse.FunctionCall(functionName, args);
        GeminiChatResponse.Part part = new GeminiChatResponse.Part(null, functionCall);
        GeminiChatResponse.Content content = new GeminiChatResponse.Content(List.of(part), "model");
        GeminiChatResponse.Candidate candidate = new GeminiChatResponse.Candidate(content, "STOP");
        return new GeminiChatResponse(List.of(candidate));
    }

    private GeminiChatResponse createResponseWithMultipleFunctionCalls(List<GeminiChatResponse.FunctionCall> functionCalls) {
        List<GeminiChatResponse.Part> parts = functionCalls.stream()
            .map(fc -> new GeminiChatResponse.Part(null, fc))
            .toList();

        GeminiChatResponse.Content content = new GeminiChatResponse.Content(parts, "model");
        GeminiChatResponse.Candidate candidate = new GeminiChatResponse.Candidate(content, "STOP");
        return new GeminiChatResponse(List.of(candidate));
    }

    private PineconeQueryResponse.Match createMatch(String id, float score, Map<String, Object> metadata) {
        return new PineconeQueryResponse.Match(id, score, new HashMap<>(metadata));
    }
}

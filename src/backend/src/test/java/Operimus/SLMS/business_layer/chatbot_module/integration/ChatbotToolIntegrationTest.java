package Operimus.SLMS.business_layer.chatbot_module.integration;

import Operimus.SLMS.business_layer.chatbot_module.dto.*;
import Operimus.SLMS.business_layer.chatbot_module.integration.gemini.GeminiClient;
import Operimus.SLMS.business_layer.chatbot_module.integration.gemini.dto.GeminiChatResponse;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.dto.PineconeQueryResponse;
import Operimus.SLMS.business_layer.chatbot_module.service.ChatbotService;
import Operimus.SLMS.business_layer.chatbot_module.service.EmbeddingService;
import Operimus.SLMS.business_layer.chatbot_module.service.RagService;
import Operimus.SLMS.business_layer.chatbot_module.tools.ToolExecutor;
import Operimus.SLMS.business_layer.chatbot_module.tools.ToolRegistry;
import Operimus.SLMS.business_layer.chatbot_module.tools.impl.OrderLookupTool;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for chatbot tool calling functionality.
 * Tests the end-to-end flow from user request to tool execution and final response.
 */
@ExtendWith(MockitoExtension.class)
class ChatbotToolIntegrationTest {

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private RagService ragService;

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private OrderService orderService;

    private ObjectMapper objectMapper;
    private ToolRegistry toolRegistry;
    private ToolExecutor toolExecutor;
    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // Create real ToolRegistry with real tool implementation
        OrderLookupTool orderLookupTool = new OrderLookupTool(orderService, objectMapper);
        toolRegistry = new ToolRegistry(List.of(orderLookupTool));

        // Create real ToolExecutor
        toolExecutor = new ToolExecutor(toolRegistry, objectMapper);

        // Create ChatbotService with real tool components
        chatbotService = new ChatbotService(
            embeddingService,
            ragService,
            geminiClient,
            toolRegistry,
            toolExecutor,
            objectMapper
        );
    }

    @Test
    void shouldExecuteToolAndReturnFinalAnswer_endToEnd() throws Exception {
        // Given - User asks about order status
        ChatRequest request = new ChatRequest(
            "What's the status of order 123?",
            List.of(),
            "en-US"
        );
        String userId = "customer-1";
        String role = "CUSTOMER";

        // Mock RAG flow
        float[] embedding = new float[768];
        when(embeddingService.generateEmbedding(anyString())).thenReturn(embedding);
        when(ragService.queryRelevantDocuments(any(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(List.of());
        when(ragService.augmentPrompt(anyString(), anyList()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(ragService.extractChunkIds(anyList())).thenReturn(List.of());

        // Mock OrderService to return order data
        OrderResponse order = OrderResponse.builder()
                .id(123L)
                .customerId(userId)
                .trackingNumber("TRK-123")
                .status(OrderStatus.IN_TRANSIT)
                .reference("REF-123")
                .build();
        when(orderService.getById(123L)).thenReturn(order);

        // Mock Gemini LLM requesting tool call, then providing final answer
        GeminiChatResponse toolCallResponse = createResponseWithFunctionCall(
            "order_lookup",
            Map.of("orderId", 123)
        );
        GeminiChatResponse finalResponse = createTextResponse(
            "Your order 123 is currently IN_TRANSIT with tracking number TRK-123."
        );

        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), anyList()))
            .thenReturn(toolCallResponse)  // First call: requests tool
            .thenReturn(finalResponse);     // Second call: final answer

        // When
        ChatResponse response = chatbotService.generateResponse(request, "customer-portal", role, userId);

        // Then - End-to-end flow should complete successfully
        assertThat(response).isNotNull();
        assertThat(response.response()).contains("IN_TRANSIT");
        assertThat(response.response()).contains("TRK-123");

        // Verify the full flow executed
        verify(embeddingService).generateEmbedding(anyString());
        verify(ragService).queryRelevantDocuments(any(), eq("customer-portal"), eq(role), eq("en-US"), anyInt());
        verify(orderService).getById(123L);
        verify(geminiClient, times(2)).generateCompletionWithTools(anyString(), anyList(), anyList());
    }

    @Test
    void shouldEnforceRoleBasedAccessControl_endToEnd() throws Exception {
        // Given - Customer tries to access another customer's order
        ChatRequest request = new ChatRequest("Check order 456", List.of(), "en-US");
        String requestingUserId = "customer-1";
        String orderOwnerUserId = "customer-2";

        setupMocksForToolCallingFlow();

        // Mock order belonging to different customer
        OrderResponse order = OrderResponse.builder()
                .id(456L)
                .customerId(orderOwnerUserId)
                .status(OrderStatus.DELIVERED)
                .build();
        when(orderService.getById(456L)).thenReturn(order);

        // Mock LLM requesting order lookup
        GeminiChatResponse toolCallResponse = createResponseWithFunctionCall(
            "order_lookup",
            Map.of("orderId", 456)
        );
        GeminiChatResponse finalResponse = createTextResponse(
            "Sorry, you can only view your own orders."
        );

        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), anyList()))
            .thenReturn(toolCallResponse)
            .thenReturn(finalResponse);

        // When
        ChatResponse response = chatbotService.generateResponse(
            request,
            "customer-portal",
            "CUSTOMER",
            requestingUserId
        );

        // Then - Should deny access
        assertThat(response).isNotNull();
        // Tool execution should fail with authorization error
        verify(orderService).getById(456L);
    }

    @Test
    void shouldHandleMultipleToolCalls_endToEnd() throws Exception {
        // Given - Request requiring multiple tool calls
        ChatRequest request = new ChatRequest("Compare orders 123 and 456", List.of(), "en-US");
        String userId = "manager-1";

        setupMocksForToolCallingFlow();

        // Mock orders
        OrderResponse order1 = createOrder(123L, "customer-1", "IN_TRANSIT");
        OrderResponse order2 = createOrder(456L, "customer-2", "DELIVERED");

        when(orderService.getById(123L)).thenReturn(order1);
        when(orderService.getById(456L)).thenReturn(order2);

        // Mock LLM requesting multiple tool calls
        GeminiChatResponse toolCallsResponse = createResponseWithMultipleFunctionCalls(
            List.of(
                new GeminiChatResponse.FunctionCall("order_lookup", Map.of("orderId", 123)),
                new GeminiChatResponse.FunctionCall("order_lookup", Map.of("orderId", 456))
            )
        );
        GeminiChatResponse finalResponse = createTextResponse(
            "Order 123 is IN_TRANSIT, order 456 is DELIVERED."
        );

        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), any()))
            .thenReturn(toolCallsResponse)
            .thenReturn(finalResponse);

        // When
        ChatResponse response = chatbotService.generateResponse(
            request,
            "admin-console",
            "LOGISTICS_MANAGER",
            userId
        );

        // Then - Both tools should execute
        assertThat(response).isNotNull();
        assertThat(response.response()).contains("IN_TRANSIT");
        assertThat(response.response()).contains("DELIVERED");

        verify(orderService).getById(123L);
        verify(orderService).getById(456L);
    }

    @Test
    void shouldHandleToolExecutionTimeout() {
        // Given - Slow tool execution scenario
        ChatRequest request = new ChatRequest("Check order 999", List.of(), "en-US");

        setupMocksForToolCallingFlow();

        // Mock OrderService with very slow response (simulated by exception after timeout)
        when(orderService.getById(999L)).thenAnswer(invocation -> {
            Thread.sleep(10000); // Longer than tool timeout (5 seconds)
            return new OrderResponse();
        });

        GeminiChatResponse toolCallResponse = createResponseWithFunctionCall(
            "order_lookup",
            Map.of("orderId", 999)
        );
        GeminiChatResponse finalResponse = createTextResponse(
            "Sorry, the request timed out. Please try again."
        );

        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), anyList()))
            .thenReturn(toolCallResponse)
            .thenReturn(finalResponse);

        // When
        ChatResponse response = chatbotService.generateResponse(
            request,
            "customer-portal",
            "CUSTOMER",
            "user-1"
        );

        // Then - Should handle timeout gracefully
        assertThat(response).isNotNull();
    }

    @Test
    void shouldStopAfterMaxIterations_preventingInfiniteLoop() {
        // Given - LLM keeps requesting tools (simulated infinite loop)
        ChatRequest request = new ChatRequest("Test infinite loop", List.of(), "en-US");

        setupMocksForToolCallingFlow();

        OrderResponse order = createOrder(123L, "customer-1", "IN_TRANSIT");
        when(orderService.getById(anyLong())).thenReturn(order);

        // Mock LLM always returning tool calls (never stopping)
        GeminiChatResponse alwaysToolCall = createResponseWithFunctionCall(
            "order_lookup",
            Map.of("orderId", 123)
        );

        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), anyList()))
            .thenReturn(alwaysToolCall); // Always returns tool call

        // When
        ChatResponse response = chatbotService.generateResponse(
            request,
            "customer-portal",
            "CUSTOMER",
            "user-1"
        );

        // Then - Should stop after 3 iterations (MAX_TOOL_ITERATIONS)
        assertThat(response).isNotNull();
        verify(orderService, times(3)).getById(anyLong());
    }

    @Test
    void shouldIntegrateRagAndToolCalling() throws Exception {
        // Given - Request using both RAG context and tool calling
        ChatRequest request = new ChatRequest(
            "How do I track order 123?",
            List.of(),
            "en-US"
        );

        float[] embedding = new float[768];
        PineconeQueryResponse.Match ragDoc = new PineconeQueryResponse.Match(
            "doc1",
            0.95f,
            Map.of("content", "You can track orders in the My Orders section", "title", "Tracking Guide")
        );

        when(embeddingService.generateEmbedding(anyString())).thenReturn(embedding);
        when(ragService.queryRelevantDocuments(any(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(List.of(ragDoc));
        when(ragService.augmentPrompt(anyString(), anyList()))
            .thenReturn("Context: Tracking guide...\nQuestion: How do I track order 123?");
        when(ragService.extractChunkIds(anyList())).thenReturn(List.of("doc1"));

        OrderResponse order = createOrder(123L, "customer-1", "IN_TRANSIT");
        when(orderService.getById(123L)).thenReturn(order);

        GeminiChatResponse toolCall = createResponseWithFunctionCall(
            "order_lookup",
            Map.of("orderId", 123)
        );
        GeminiChatResponse finalResponse = createTextResponse(
            "Your order 123 is IN_TRANSIT. You can track it in the My Orders section."
        );

        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), anyList()))
            .thenReturn(toolCall)
            .thenReturn(finalResponse);

        // When
        ChatResponse response = chatbotService.generateResponse(
            request,
            "customer-portal",
            "CUSTOMER",
            "customer-1"
        );

        // Then - Should use both RAG and tools
        assertThat(response).isNotNull();
        assertThat(response.sourceDocuments()).containsExactly("doc1");
        assertThat(response.response()).contains("IN_TRANSIT");

        verify(ragService).queryRelevantDocuments(any(), anyString(), anyString(), anyString(), anyInt());
        verify(orderService).getById(123L);
    }

    // Helper methods
    private void setupMocksForToolCallingFlow() {
        float[] embedding = new float[768];
        when(embeddingService.generateEmbedding(anyString())).thenReturn(embedding);
        when(ragService.queryRelevantDocuments(any(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(List.of());
        when(ragService.augmentPrompt(anyString(), anyList()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(ragService.extractChunkIds(anyList())).thenReturn(List.of());
    }

    private OrderResponse createOrder(Long id, String customerId, String status) {
        return OrderResponse.builder()
                .id(id)
                .customerId(customerId)
                .status(OrderStatus.valueOf(status))
                .trackingNumber("TRK-" + id)
                .reference("REF-" + id)
                .build();
    }

    private GeminiChatResponse createTextResponse(String text) {
        GeminiChatResponse.Part textPart = new GeminiChatResponse.Part(text, null);
        GeminiChatResponse.Content content = new GeminiChatResponse.Content(List.of(textPart), "model");
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

    private GeminiChatResponse createResponseWithMultipleFunctionCalls(List<GeminiChatResponse.FunctionCall> calls) {
        List<GeminiChatResponse.Part> parts = calls.stream()
            .map(fc -> new GeminiChatResponse.Part(null, fc))
            .toList();

        GeminiChatResponse.Content content = new GeminiChatResponse.Content(parts, "model");
        GeminiChatResponse.Candidate candidate = new GeminiChatResponse.Candidate(content, "STOP");
        return new GeminiChatResponse(List.of(candidate));
    }
}

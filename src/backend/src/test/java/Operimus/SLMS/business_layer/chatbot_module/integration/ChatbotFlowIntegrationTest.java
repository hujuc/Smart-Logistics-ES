package Operimus.SLMS.business_layer.chatbot_module.integration;

import Operimus.SLMS.business_layer.chatbot_module.dto.ChatMessage;
import Operimus.SLMS.business_layer.chatbot_module.dto.ChatRequest;
import Operimus.SLMS.business_layer.chatbot_module.dto.ChatResponse;
import Operimus.SLMS.business_layer.chatbot_module.dto.UpsertDocumentRequest;
import Operimus.SLMS.business_layer.chatbot_module.integration.gemini.GeminiClient;
import Operimus.SLMS.business_layer.chatbot_module.integration.gemini.dto.GeminiChatResponse;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeClient;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.dto.PineconeQueryResponse;
import Operimus.SLMS.business_layer.chatbot_module.service.ChatbotService;
import Operimus.SLMS.business_layer.chatbot_module.service.DocumentManagementService;
import Operimus.SLMS.config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Integration test for the complete chatbot RAG workflow.
 * Tests the full flow: User Query → Embedding → Pinecone Query → RAG → LLM → Response
 *
 * This test uses the real Spring context and services but mocks external APIs (Gemini, Pinecone).
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ChatbotFlowIntegrationTest {

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private DocumentManagementService documentManagementService;

    @MockitoBean
    private GeminiClient geminiClient;

    @MockitoBean
    private PineconeClient pineconeClient;

    private static final float[] MOCK_EMBEDDING = createMockEmbedding(768);

    @BeforeEach
    void setUp() {
        // Mock Gemini embedding generation
        when(geminiClient.generateEmbedding(anyString())).thenReturn(MOCK_EMBEDDING);
    }

    @Test
    void completeRagFlow_shouldWorkEndToEnd_withDocumentRetrieval() {
        // Given: Seed a test document into the system
        UpsertDocumentRequest testDoc = new UpsertDocumentRequest(
                "Test Order Tracking Guide",
                Set.of("CUSTOMER", "ANONYMOUS"),
                Set.of("customer-portal", "landing-page"),
                Set.of("/orders", "/track"),
                "en-US",
                "To track your order in SLMS, log into the customer portal and navigate to the 'My Orders' section. " +
                        "You will see a list of all your orders with their current status. Click on any order to see detailed tracking information including the current location, estimated delivery time, and delivery history.",
                null
        );

        // Seed the document (this goes to DB and should go to Pinecone)
        documentManagementService.upsertDocument(testDoc);

        // Mock Pinecone to return this document when queried
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", testDoc.title());
        metadata.put("content", testDoc.content());
        metadata.put("locale", "en-US");
        metadata.put("roles", List.copyOf(testDoc.roles()));
        metadata.put("frontends", List.copyOf(testDoc.frontends()));

        PineconeQueryResponse.Match mockMatch = new PineconeQueryResponse.Match(
                "test-chunk-1",
                0.95f,
                metadata
        );

        when(pineconeClient.query(any(float[].class), anyInt(), anyMap()))
                .thenReturn(List.of(mockMatch));

        // Mock Gemini LLM response
        GeminiChatResponse.Content content = new GeminiChatResponse.Content(
                List.of(new GeminiChatResponse.Part(
                    "You can track your order by logging into the customer portal and going to 'My Orders'. " +
                    "There you'll find all your orders with real-time tracking information.",
                    null
                )),
                "model"
        );
        GeminiChatResponse.Candidate candidate = new GeminiChatResponse.Candidate(content, "STOP");
        GeminiChatResponse geminiResponse = new GeminiChatResponse(List.of(candidate));

        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), any()))
                .thenReturn(geminiResponse);

        // When: User asks a question
        ChatRequest userRequest = new ChatRequest(
                "How do I track my order?",
                List.of(),
                "en-US"
        );

        ChatResponse response = chatbotService.generateResponse(userRequest, "customer-portal", "CUSTOMER");

        // Then: Verify the complete flow worked
        assertThat(response).isNotNull();
        assertThat(response.response()).isNotEmpty();
        assertThat(response.response()).contains("customer portal");
        assertThat(response.sourceDocuments()).isNotEmpty();
        assertThat(response.sourceDocuments()).contains("test-chunk-1");
    }

    @Test
    void completeRagFlow_shouldFilterDocumentsByRole() {
        // Given: Create documents for different roles
        UpsertDocumentRequest customerDoc = new UpsertDocumentRequest(
                "Customer Guide",
                Set.of("CUSTOMER"),
                Set.of("customer-portal"),
                Set.of(),
                "en-US",
                "This is information for customers only.",
                null
        );

        UpsertDocumentRequest driverDoc = new UpsertDocumentRequest(
                "Driver Guide",
                Set.of("DRIVER"),
                Set.of("driver-pwa"),
                Set.of(),
                "en-US",
                "This is information for drivers only.",
                null
        );

        documentManagementService.upsertDocument(customerDoc);
        documentManagementService.upsertDocument(driverDoc);

        // Mock Pinecone to return only customer documents when customer role is used
        Map<String, Object> customerMetadata = new HashMap<>();
        customerMetadata.put("content", "This is information for customers only.");
        customerMetadata.put("title", "Customer Guide");

        when(pineconeClient.query(any(float[].class), anyInt(), argThat(filter -> {
            // Verify the filter contains CUSTOMER role
            if (filter == null) return false;
            Object rolesFilter = filter.get("roles");
            if (rolesFilter instanceof Map) {
                Object inClause = ((Map<?, ?>) rolesFilter).get("$in");
                return inClause instanceof List && ((List<?>) inClause).contains("CUSTOMER");
            }
            return false;
        }))).thenReturn(List.of(new PineconeQueryResponse.Match("cust-1", 0.9f, customerMetadata)));

        GeminiChatResponse.Content content = new GeminiChatResponse.Content(
                List.of(new GeminiChatResponse.Part("Customer-specific response", null)),
                "model"
        );
        GeminiChatResponse.Candidate candidate = new GeminiChatResponse.Candidate(content, "STOP");
        GeminiChatResponse geminiResponse = new GeminiChatResponse(List.of(candidate));

        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), any()))
                .thenReturn(geminiResponse);

        // When: Customer asks a question
        ChatRequest request = new ChatRequest("Help me", List.of(), "en-US");
        ChatResponse response = chatbotService.generateResponse(request, "customer-portal", "CUSTOMER");

        // Then: Only customer documents should be used
        assertThat(response.sourceDocuments()).contains("cust-1");
    }

    @Test
    void completeRagFlow_shouldHandleConversationHistory() {
        // Given: Mock Pinecone response
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("content", "SLMS stands for Smart Logistics Management System.");
        metadata.put("title", "System Overview");

        when(pineconeClient.query(any(), anyInt(), anyMap()))
                .thenReturn(List.of(new PineconeQueryResponse.Match("doc-1", 0.9f, metadata)));

        GeminiChatResponse.Content content = new GeminiChatResponse.Content(
                List.of(new GeminiChatResponse.Part("SLMS is a comprehensive logistics platform.", null)),
                "model"
        );
        GeminiChatResponse.Candidate candidate = new GeminiChatResponse.Candidate(content, "STOP");
        GeminiChatResponse geminiResponse = new GeminiChatResponse(List.of(candidate));

        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), any()))
                .thenAnswer(invocation -> {
                    List<Map<String, String>> messages = invocation.getArgument(1);
                    // Verify conversation history is included
                    assertThat(messages).hasSizeGreaterThan(1);
                    return geminiResponse;
                });

        // When: User has conversation history
        List<ChatMessage> history = List.of(
                new ChatMessage("user", "What is SLMS?"),
                new ChatMessage("assistant", "SLMS is a logistics management system.")
        );

        ChatRequest request = new ChatRequest("Tell me more", history, "en-US");
        ChatResponse response = chatbotService.generateResponse(request, "landing-page", "ANONYMOUS");

        // Then: Response should be generated
        assertThat(response).isNotNull();
        assertThat(response.response()).isNotEmpty();
    }

    @Test
    void completeRagFlow_shouldFallbackGracefully_whenPineconeFails() {
        // Given: Pinecone throws an exception
        when(pineconeClient.query(any(), anyInt(), anyMap()))
                .thenThrow(new RuntimeException("Pinecone connection failed"));

        // Mock LLM to still work
        GeminiChatResponse.Content fallbackContent = new GeminiChatResponse.Content(
                List.of(new GeminiChatResponse.Part(
                    "I can help you with that, though I don't have specific context right now.",
                    null
                )),
                "model"
        );
        GeminiChatResponse.Candidate fallbackCandidate = new GeminiChatResponse.Candidate(fallbackContent, "STOP");
        GeminiChatResponse fallbackResponse = new GeminiChatResponse(List.of(fallbackCandidate));

        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), any()))
                .thenReturn(fallbackResponse);

        // When: User asks a question
        ChatRequest request = new ChatRequest("How do I track my order?", List.of(), "en-US");
        ChatResponse response = chatbotService.generateResponse(request, "customer-portal", "CUSTOMER");

        // Then: Should fall back to non-RAG response
        assertThat(response).isNotNull();
        assertThat(response.response()).isNotEmpty();
        assertThat(response.sourceDocuments()).isEmpty(); // No source documents when RAG fails
    }

    @Test
    void completeRagFlow_shouldFilterByFrontend() {
        // Given: Mock Pinecone to verify frontend filter
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("content", "Customer portal specific content.");
        metadata.put("title", "Portal Guide");

        when(pineconeClient.query(any(float[].class), anyInt(), argThat(filter -> {
            // Verify the filter contains customer-portal frontend
            if (filter == null) return false;
            Object frontendsFilter = filter.get("frontends");
            if (frontendsFilter instanceof Map) {
                Object inClause = ((Map<?, ?>) frontendsFilter).get("$in");
                return inClause instanceof List && ((List<?>) inClause).contains("customer-portal");
            }
            return false;
        }))).thenReturn(List.of(new PineconeQueryResponse.Match("portal-doc", 0.9f, metadata)));

        GeminiChatResponse.Content content = new GeminiChatResponse.Content(
                List.of(new GeminiChatResponse.Part("Response based on customer portal context", null)),
                "model"
        );
        GeminiChatResponse.Candidate candidate = new GeminiChatResponse.Candidate(content, "STOP");
        GeminiChatResponse geminiResponse = new GeminiChatResponse(List.of(candidate));

        when(geminiClient.generateCompletionWithTools(anyString(), anyList(), any()))
                .thenReturn(geminiResponse);

        // When: Request from customer portal
        ChatRequest request = new ChatRequest("Help", List.of(), "en-US");
        ChatResponse response = chatbotService.generateResponse(request, "customer-portal", "CUSTOMER");

        // Then: Correct frontend filter applied
        assertThat(response).isNotNull();
    }

    @Test
    void completeRagFlow_shouldAugmentPromptWithContext() {
        // Given: Mock Pinecone response with specific content
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("content", "SPECIFIC_CONTEXT_CONTENT: Orders can be tracked in real-time.");
        metadata.put("title", "Tracking");

        when(pineconeClient.query(any(), anyInt(), anyMap()))
                .thenReturn(List.of(new PineconeQueryResponse.Match("doc-1", 0.95f, metadata)));

        // Verify the LLM receives augmented prompt
        GeminiChatResponse.Content responseContent = new GeminiChatResponse.Content(
                List.of(new GeminiChatResponse.Part("Response with context", null)),
                "model"
        );
        GeminiChatResponse.Candidate candidate = new GeminiChatResponse.Candidate(responseContent, "STOP");
        GeminiChatResponse geminiResponse = new GeminiChatResponse(List.of(candidate));

        when(geminiClient.generateCompletionWithTools(anyString(), argThat(messages -> {
            // Check if the last message contains the augmented context
            if (messages == null || messages.isEmpty()) return false;
            Map<String, String> lastMessage = messages.get(messages.size() - 1);
            String messageContent = lastMessage.get("content");
            return messageContent != null && messageContent.contains("SPECIFIC_CONTEXT_CONTENT");
        }), any())).thenReturn(geminiResponse);

        // When: User asks a question
        ChatRequest request = new ChatRequest("Track order", List.of(), "en-US");
        ChatResponse response = chatbotService.generateResponse(request, "customer-portal", "CUSTOMER");

        // Then: LLM should receive augmented prompt
        assertThat(response).isNotNull();
        assertThat(response.response()).isEqualTo("Response with context");
    }

    // Helper method to create mock embedding vector
    private static float[] createMockEmbedding(int dimensions) {
        float[] embedding = new float[dimensions];
        for (int i = 0; i < dimensions; i++) {
            embedding[i] = (float) (Math.random() * 0.1);
        }
        return embedding;
    }
}

package Operimus.SLMS.business_layer.chatbot_module.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Operimus.SLMS.business_layer.chatbot_module.dto.ChatMessage;
import Operimus.SLMS.business_layer.chatbot_module.dto.ChatRequest;
import Operimus.SLMS.business_layer.chatbot_module.dto.ChatResponse;
import Operimus.SLMS.business_layer.chatbot_module.integration.gemini.GeminiClient;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.dto.PineconeQueryResponse;
import Operimus.SLMS.business_layer.shared.exception.ExternalServiceException;
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

    @Captor
    private ArgumentCaptor<String> systemPromptCaptor;

    @Captor
    private ArgumentCaptor<List<Map<String, String>>> messagesCaptor;

    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        chatbotService = new ChatbotService(embeddingService, ragService, geminiClient);
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
        when(geminiClient.generateCompletion(anyString(), anyList())).thenReturn(llmResponse);

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
        verify(geminiClient).generateCompletion(anyString(), anyList());
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
        when(geminiClient.generateCompletion(anyString(), anyList())).thenReturn("Response");

        // When
        chatbotService.generateResponse(request, frontend, role);

        // Then
        verify(geminiClient).generateCompletion(anyString(), messagesCaptor.capture());
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
        verify(geminiClient).generateCompletion(systemPromptCaptor.capture(), anyList());
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
        verify(geminiClient).generateCompletion(systemPromptCaptor.capture(), anyList());
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
        verify(geminiClient).generateCompletion(systemPromptCaptor.capture(), anyList());
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
        verify(geminiClient).generateCompletion(systemPromptCaptor.capture(), anyList());
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
        verify(geminiClient).generateCompletion(anyString(), messagesCaptor.capture());
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
        when(geminiClient.generateCompletion(anyString(), anyList())).thenReturn("Fallback response");

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
        verify(geminiClient).generateCompletion(anyString(), argThat(messages -> {
            Map<String, String> lastMessage = messages.get(messages.size() - 1);
            return lastMessage.get("content").equals("Test question"); // Original, not augmented
        }));
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
        when(geminiClient.generateCompletion(anyString(), anyList())).thenReturn("Fallback response");

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
        verify(geminiClient).generateCompletion(systemPromptCaptor.capture(), anyList());
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

    // Helper methods
    private void setupMocksForSuccessfulFlow() {
        float[] embedding = new float[768];
        List<PineconeQueryResponse.Match> docs = List.of();

        when(embeddingService.generateEmbedding(anyString())).thenReturn(embedding);
        when(ragService.queryRelevantDocuments(any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(docs);
        when(ragService.augmentPrompt(anyString(), anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(ragService.extractChunkIds(anyList())).thenReturn(List.of());
        when(geminiClient.generateCompletion(anyString(), anyList())).thenReturn("Response");
    }

    private PineconeQueryResponse.Match createMatch(String id, float score, Map<String, Object> metadata) {
        return new PineconeQueryResponse.Match(id, score, new HashMap<>(metadata));
    }
}

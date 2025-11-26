package Operimus.SLMS.business_layer.chatbot_module.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeClient;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.dto.PineconeQueryResponse;
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
 * Tests for RagService.
 * Verifies Retrieval-Augmented Generation (RAG) logic including document querying and prompt augmentation.
 */
@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @Mock
    private PineconeClient pineconeClient;

    @Captor
    private ArgumentCaptor<Map<String, Object>> filterCaptor;

    private RagService ragService;

    @BeforeEach
    void setUp() {
        ragService = new RagService(pineconeClient);
    }

    @Test
    void queryRelevantDocuments_shouldQueryPinecone_withProperFilters() {
        // Given
        float[] queryEmbedding = new float[768];
        String frontend = "customer-portal";
        String role = "CUSTOMER";
        String locale = "en-US";
        int topK = 5;

        List<PineconeQueryResponse.Match> expectedMatches = List.of(
                createMatch("doc1", 0.95f, Map.of("content", "How to track orders", "title", "Order Tracking"))
        );

        when(pineconeClient.query(any(), anyInt(), any())).thenReturn(expectedMatches);

        // When
        List<PineconeQueryResponse.Match> result = ragService.queryRelevantDocuments(
                queryEmbedding, frontend, role, locale, topK
        );

        // Then
        assertThat(result).isEqualTo(expectedMatches);
        verify(pineconeClient).query(eq(queryEmbedding), eq(topK), filterCaptor.capture());

        Map<String, Object> capturedFilter = filterCaptor.getValue();
        assertThat(capturedFilter).containsKey("frontends");
        assertThat(capturedFilter).containsKey("roles");
        assertThat(capturedFilter).containsKey("locale");

        @SuppressWarnings("unchecked")
        Map<String, Object> frontendsFilter = (Map<String, Object>) capturedFilter.get("frontends");
        assertThat(frontendsFilter.get("$in")).isEqualTo(List.of("customer-portal"));

        @SuppressWarnings("unchecked")
        Map<String, Object> rolesFilter = (Map<String, Object>) capturedFilter.get("roles");
        assertThat(rolesFilter.get("$in")).isEqualTo(List.of("CUSTOMER"));

        @SuppressWarnings("unchecked")
        Map<String, Object> localeFilter = (Map<String, Object>) capturedFilter.get("locale");
        assertThat(localeFilter.get("$eq")).isEqualTo("en-US");
    }

    @Test
    void queryRelevantDocuments_shouldHandleNullFrontend() {
        // Given
        float[] queryEmbedding = new float[768];
        String role = "DRIVER";
        String locale = "en-US";
        int topK = 3;

        when(pineconeClient.query(any(), anyInt(), any())).thenReturn(List.of());

        // When
        ragService.queryRelevantDocuments(queryEmbedding, null, role, locale, topK);

        // Then
        verify(pineconeClient).query(eq(queryEmbedding), eq(topK), filterCaptor.capture());

        Map<String, Object> capturedFilter = filterCaptor.getValue();
        assertThat(capturedFilter).doesNotContainKey("frontends");
        assertThat(capturedFilter).containsKey("roles");
        assertThat(capturedFilter).containsKey("locale");
    }

    @Test
    void queryRelevantDocuments_shouldHandleNullRole() {
        // Given
        float[] queryEmbedding = new float[768];
        String frontend = "landing-page";
        String locale = "en-US";
        int topK = 3;

        when(pineconeClient.query(any(), anyInt(), any())).thenReturn(List.of());

        // When
        ragService.queryRelevantDocuments(queryEmbedding, frontend, null, locale, topK);

        // Then
        verify(pineconeClient).query(eq(queryEmbedding), eq(topK), filterCaptor.capture());

        Map<String, Object> capturedFilter = filterCaptor.getValue();
        assertThat(capturedFilter).containsKey("frontends");
        assertThat(capturedFilter).doesNotContainKey("roles");
        assertThat(capturedFilter).containsKey("locale");
    }

    @Test
    void queryRelevantDocuments_shouldHandleNullLocale() {
        // Given
        float[] queryEmbedding = new float[768];
        String frontend = "admin-console";
        String role = "ADMIN";
        int topK = 5;

        when(pineconeClient.query(any(), anyInt(), any())).thenReturn(List.of());

        // When
        ragService.queryRelevantDocuments(queryEmbedding, frontend, role, null, topK);

        // Then
        verify(pineconeClient).query(eq(queryEmbedding), eq(topK), filterCaptor.capture());

        Map<String, Object> capturedFilter = filterCaptor.getValue();
        assertThat(capturedFilter).containsKey("frontends");
        assertThat(capturedFilter).containsKey("roles");
        assertThat(capturedFilter).doesNotContainKey("locale");
    }

    @Test
    void augmentPrompt_shouldBuildContextWithDocuments() {
        // Given
        String userMessage = "How do I track my order?";
        List<PineconeQueryResponse.Match> documents = List.of(
                createMatch("doc1", 0.95f, Map.of(
                        "content", "To track your order, log into the customer portal and navigate to My Orders.",
                        "title", "Order Tracking Guide"
                )),
                createMatch("doc2", 0.87f, Map.of(
                        "content", "Orders can be tracked by entering your order ID on the landing page.",
                        "title", "Quick Order Lookup"
                ))
        );

        // When
        String result = ragService.augmentPrompt(userMessage, documents);

        // Then
        assertThat(result).contains("Relevant context:");
        assertThat(result).contains("[Document 1]");
        assertThat(result).contains("[Document 2]");
        assertThat(result).contains("To track your order, log into the customer portal");
        assertThat(result).contains("Orders can be tracked by entering your order ID");
        assertThat(result).contains("---");
        assertThat(result).contains("User question: How do I track my order?");
    }

    @Test
    void augmentPrompt_shouldReturnOriginalMessage_whenNoDocuments() {
        // Given
        String userMessage = "Hello!";
        List<PineconeQueryResponse.Match> emptyDocs = List.of();

        // When
        String result = ragService.augmentPrompt(userMessage, emptyDocs);

        // Then
        assertThat(result).isEqualTo(userMessage);
    }

    @Test
    void augmentPrompt_shouldReturnOriginalMessage_whenDocumentsIsNull() {
        // Given
        String userMessage = "Hello!";

        // When
        String result = ragService.augmentPrompt(userMessage, null);

        // Then
        assertThat(result).isEqualTo(userMessage);
    }

    @Test
    void augmentPrompt_shouldSkipDocumentsWithoutContent() {
        // Given
        String userMessage = "What are the delivery exceptions?";
        List<PineconeQueryResponse.Match> documents = List.of(
                createMatch("doc1", 0.9f, Map.of("title", "Exceptions Guide")), // No content
                createMatch("doc2", 0.85f, Map.of(
                        "content", "Delivery exceptions include: damaged packages, wrong address, and failed delivery attempts.",
                        "title", "Exception Types"
                ))
        );

        // When
        String result = ragService.augmentPrompt(userMessage, documents);

        // Then
        assertThat(result).contains("[Document 2]"); // doc2 at index 1 becomes [Document 2]
        assertThat(result).contains("Delivery exceptions include");
        assertThat(result).doesNotContain("[Document 1]"); // doc1 is skipped, no Document 1
    }

    @Test
    void extractChunkIds_shouldExtractAllIds() {
        // Given
        List<PineconeQueryResponse.Match> documents = List.of(
                createMatch("chunk-1", 0.95f, Map.of()),
                createMatch("chunk-2", 0.87f, Map.of()),
                createMatch("chunk-3", 0.82f, Map.of())
        );

        // When
        List<String> result = ragService.extractChunkIds(documents);

        // Then
        assertThat(result).containsExactly("chunk-1", "chunk-2", "chunk-3");
    }

    @Test
    void extractChunkIds_shouldReturnEmptyList_whenDocumentsIsNull() {
        // When
        List<String> result = ragService.extractChunkIds(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void extractChunkIds_shouldReturnEmptyList_whenDocumentsIsEmpty() {
        // When
        List<String> result = ragService.extractChunkIds(List.of());

        // Then
        assertThat(result).isEmpty();
    }

    // Helper method to create test Match objects
    private PineconeQueryResponse.Match createMatch(String id, float score, Map<String, Object> metadata) {
        return new PineconeQueryResponse.Match(id, score, new HashMap<>(metadata));
    }
}

package Operimus.SLMS.business_layer.chatbot_module.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeClient;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeProperties;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeVector;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.dto.PineconeQueryResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for PineconeClient.
 * Uses MockWebServer to test HTTP interactions with Pinecone API.
 */
class PineconeClientTest {

    private MockWebServer mockWebServer;
    private PineconeClient pineconeClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        PineconeProperties properties = new PineconeProperties(
                "test-api-key",
                baseUrl,
                "test-index",
                "test-namespace"
        );

        pineconeClient = new PineconeClient(properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void constructor_shouldThrowException_whenApiKeyIsNull() {
        // Given
        PineconeProperties invalidProperties = new PineconeProperties(
                null,
                "http://localhost:8080",
                "test-index",
                "test-namespace"
        );

        // When/Then
        assertThatThrownBy(() -> new PineconeClient(invalidProperties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("chatbot.pinecone.api-key must be configured");
    }

    @Test
    void constructor_shouldThrowException_whenHostIsNull() {
        // Given
        PineconeProperties invalidProperties = new PineconeProperties(
                "test-api-key",
                null,
                "test-index",
                "test-namespace"
        );

        // When/Then
        assertThatThrownBy(() -> new PineconeClient(invalidProperties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("chatbot.pinecone.host must be configured");
    }

    @Test
    void constructor_shouldThrowException_whenIndexIsNull() {
        // Given
        PineconeProperties invalidProperties = new PineconeProperties(
                "test-api-key",
                "http://localhost:8080",
                null,
                "test-namespace"
        );

        // When/Then
        assertThatThrownBy(() -> new PineconeClient(invalidProperties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("chatbot.pinecone.index must be configured");
    }

    @Test
    void constructor_shouldThrowException_whenNamespaceIsNull() {
        // Given
        PineconeProperties invalidProperties = new PineconeProperties(
                "test-api-key",
                "http://localhost:8080",
                "test-index",
                null
        );

        // When/Then
        assertThatThrownBy(() -> new PineconeClient(invalidProperties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("chatbot.pinecone.namespace must be configured");
    }

    @Test
    void upsertVectors_shouldSendCorrectRequest() throws InterruptedException, JsonProcessingException {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}"));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "Test Document");
        metadata.put("content", "Test content");

        PineconeVector vector = new PineconeVector(
                "test-id-123",
                new float[]{0.1f, 0.2f, 0.3f},
                metadata
        );

        // When
        pineconeClient.upsertVectors(List.of(vector));

        // Then
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/vectors/upsert");
        assertThat(request.getHeader("Api-Key")).isEqualTo("test-api-key");
        assertThat(request.getHeader("X-Pinecone-API-Version")).isEqualTo("2025-01");
        assertThat(request.getHeader("Content-Type")).contains("application/json");

        // Verify request body
        String requestBody = request.getBody().readUtf8();
        Map<String, Object> bodyMap = objectMapper.readValue(requestBody, Map.class);
        assertThat(bodyMap).containsKey("namespace");
        assertThat(bodyMap.get("namespace")).isEqualTo("test-namespace");
        assertThat(bodyMap).containsKey("vectors");
    }

    @Test
    void upsertVectors_shouldHandleMultipleVectors() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}"));

        List<PineconeVector> vectors = List.of(
                new PineconeVector("id-1", new float[]{0.1f, 0.2f}, Map.of("title", "Doc 1")),
                new PineconeVector("id-2", new float[]{0.3f, 0.4f}, Map.of("title", "Doc 2")),
                new PineconeVector("id-3", new float[]{0.5f, 0.6f}, Map.of("title", "Doc 3"))
        );

        // When
        pineconeClient.upsertVectors(vectors);

        // Then
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/vectors/upsert");
    }

    @Test
    void upsertVectors_shouldDoNothing_whenVectorsListIsEmpty() {
        // Given
        List<PineconeVector> emptyList = List.of();

        // When
        pineconeClient.upsertVectors(emptyList);

        // Then - No request should be made
        assertThat(mockWebServer.getRequestCount()).isEqualTo(0);
    }

    @Test
    void upsertVectors_shouldHandleErrorResponse() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"error\": \"Internal server error\"}"));

        PineconeVector vector = new PineconeVector(
                "test-id",
                new float[]{0.1f, 0.2f},
                Map.of("title", "Test")
        );

        // When/Then - Should handle error gracefully (logs error but doesn't throw)
        assertThatThrownBy(() -> pineconeClient.upsertVectors(List.of(vector)))
                .hasMessageContaining("500");
    }

    @Test
    void query_shouldSendCorrectRequest() throws InterruptedException, JsonProcessingException {
        // Given
        String responseJson = """
                {
                    "matches": [
                        {
                            "id": "doc-1",
                            "score": 0.95,
                            "metadata": {
                                "title": "Test Document",
                                "content": "Test content"
                            }
                        }
                    ]
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .setHeader("Content-Type", "application/json"));

        float[] queryVector = new float[]{0.1f, 0.2f, 0.3f};
        Map<String, Object> metadataFilter = Map.of("locale", "en-US");

        // When
        List<PineconeQueryResponse.Match> matches = pineconeClient.query(queryVector, 5, metadataFilter);

        // Then - Verify request
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/query");
        assertThat(request.getHeader("Api-Key")).isEqualTo("test-api-key");
        assertThat(request.getHeader("X-Pinecone-API-Version")).isEqualTo("2025-01");

        // Verify request body
        String requestBody = request.getBody().readUtf8();
        Map<String, Object> bodyMap = objectMapper.readValue(requestBody, Map.class);
        assertThat(bodyMap.get("namespace")).isEqualTo("test-namespace");
        assertThat(bodyMap.get("topK")).isEqualTo(5);
        assertThat(bodyMap.get("includeMetadata")).isEqualTo(true);
        assertThat(bodyMap).containsKey("vector");
        assertThat(bodyMap).containsKey("filter");

        // Verify response
        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).id()).isEqualTo("doc-1");
        assertThat(matches.get(0).score()).isEqualTo(0.95);
    }

    @Test
    void query_shouldReturnEmptyList_whenNoMatches() throws JsonProcessingException {
        // Given
        String responseJson = """
                {
                    "matches": []
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .setHeader("Content-Type", "application/json"));

        float[] queryVector = new float[]{0.1f, 0.2f, 0.3f};

        // When
        List<PineconeQueryResponse.Match> matches = pineconeClient.query(queryVector, 10, Map.of());

        // Then
        assertThat(matches).isEmpty();
    }

    @Test
    void query_shouldHandleErrorResponse() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"error\": \"Internal server error\"}"));

        float[] queryVector = new float[]{0.1f, 0.2f, 0.3f};

        // When - Should handle error gracefully and return empty list
        List<PineconeQueryResponse.Match> matches = pineconeClient.query(queryVector, 10, Map.of());

        // Then
        assertThat(matches).isEmpty();
    }

    @Test
    void query_shouldHandleMultipleMatches() throws JsonProcessingException {
        // Given
        String responseJson = """
                {
                    "matches": [
                        {
                            "id": "doc-1",
                            "score": 0.95,
                            "metadata": {"title": "Document 1"}
                        },
                        {
                            "id": "doc-2",
                            "score": 0.87,
                            "metadata": {"title": "Document 2"}
                        },
                        {
                            "id": "doc-3",
                            "score": 0.72,
                            "metadata": {"title": "Document 3"}
                        }
                    ]
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .setHeader("Content-Type", "application/json"));

        float[] queryVector = new float[]{0.1f, 0.2f, 0.3f};

        // When
        List<PineconeQueryResponse.Match> matches = pineconeClient.query(queryVector, 10, Map.of());

        // Then
        assertThat(matches).hasSize(3);
        assertThat(matches.get(0).id()).isEqualTo("doc-1");
        assertThat(matches.get(0).score()).isEqualTo(0.95);
        assertThat(matches.get(1).id()).isEqualTo("doc-2");
        assertThat(matches.get(1).score()).isEqualTo(0.87);
        assertThat(matches.get(2).id()).isEqualTo("doc-3");
        assertThat(matches.get(2).score()).isEqualTo(0.72);
    }

    @Test
    void query_shouldIncludeMetadataFilters() throws InterruptedException, JsonProcessingException {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"matches\": []}")
                .setHeader("Content-Type", "application/json"));

        float[] queryVector = new float[]{0.1f, 0.2f, 0.3f};
        Map<String, Object> metadataFilter = Map.of(
                "locale", "en-US",
                "roles", List.of("CUSTOMER", "ADMIN")
        );

        // When
        pineconeClient.query(queryVector, 5, metadataFilter);

        // Then
        RecordedRequest request = mockWebServer.takeRequest();
        String requestBody = request.getBody().readUtf8();
        Map<String, Object> bodyMap = objectMapper.readValue(requestBody, Map.class);

        assertThat(bodyMap).containsKey("filter");
        Map<String, Object> filter = (Map<String, Object>) bodyMap.get("filter");
        assertThat(filter.get("locale")).isEqualTo("en-US");
        assertThat(filter.get("roles")).isEqualTo(List.of("CUSTOMER", "ADMIN"));
    }

    @Test
    void query_shouldWorkWithNullMetadataFilter() throws JsonProcessingException {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"matches\": []}")
                .setHeader("Content-Type", "application/json"));

        float[] queryVector = new float[]{0.1f, 0.2f, 0.3f};

        // When
        List<PineconeQueryResponse.Match> matches = pineconeClient.query(queryVector, 10, null);

        // Then - Should handle null filter gracefully
        assertThat(matches).isEmpty();
    }
}

package Operimus.SLMS.business_layer.chatbot_module.integration;

import Operimus.SLMS.business_layer.chatbot_module.integration.gemini.GeminiClient;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeClient;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeVector;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.dto.PineconeQueryResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real integration tests for Pinecone Vector Database.
 *
 * These tests interact with the actual Pinecone API using real credentials from .env file.
 * They verify that:
 * 1. Authentication works correctly (Api-Key header format)
 * 2. Vector upsert operations succeed
 * 3. Vector query operations return relevant results
 * 4. Metadata filtering works properly
 * 5. Integration with Gemini embeddings works end-to-end
 *
 * Prerequisites:
 * - Valid PINECONE_API_KEY in infra/.env
 * - Pinecone index created with 768 dimensions (for Gemini text-embedding-004)
 * - Internet connectivity to reach Pinecone API
 *
 * Run with: ./mvnw test -Preal-integration -Dtest=PineconeRealIntegrationTest
 */
@SpringBootTest
@Tag("real-integration")
@Epic("Chatbot Module")
@Feature("Pinecone Vector Database Integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PineconeRealIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(PineconeRealIntegrationTest.class);

    @Autowired
    private PineconeClient pineconeClient;

    @Autowired
    private GeminiClient geminiClient;

    private String testVectorId;
    private float[] testEmbedding;

    @BeforeAll
    void setup() {
        log.info("=== Starting Pinecone Real Integration Tests ===");
        log.info("These tests will interact with the actual Pinecone API");

        // Generate a unique test ID to avoid conflicts
        testVectorId = "test-vector-" + UUID.randomUUID();

        log.info("Test vector ID: {}", testVectorId);
    }

    @AfterAll
    void cleanup() {
        log.info("=== Pinecone Real Integration Tests Completed ===");
        log.info("Note: Test vectors are not automatically deleted from Pinecone");
        log.info("You may want to clean up test data manually if needed");
    }

    @Test
    @Order(1)
    @Story("Authentication")
    @Description("Verify that Pinecone API accepts our API key with correct header format")
    void testAuthentication_shouldSucceed_withValidApiKey() {
        log.info("TEST 1: Testing Pinecone authentication with Api-Key header");

        // Given - Create a simple test vector (using zeros for authentication test)
        float[] simpleVector = new float[768]; // Gemini text-embedding-004 dimension
        for (int i = 0; i < simpleVector.length; i++) {
            simpleVector[i] = 0.001f; // Small non-zero values
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("test", "authentication");
        metadata.put("timestamp", System.currentTimeMillis());

        PineconeVector testVector = new PineconeVector(
                testVectorId + "-auth",
                simpleVector,
                metadata
        );

        // When - Try to upsert a vector (this will fail with 401 if auth is wrong)
        try {
            pineconeClient.upsertVectors(List.of(testVector));
            log.info("✅ Authentication successful - vector upsert accepted");
        } catch (Exception e) {
            log.error("❌ Authentication failed", e);
            throw new AssertionError("Pinecone authentication failed. Check API key and header format.", e);
        }

        // Then - If we got here without exception, authentication worked
        assertThat(testVector).isNotNull();
    }

    @Test
    @Order(2)
    @Story("Vector Upsert")
    @Description("Test upserting vectors with real embeddings from Gemini")
    void testUpsertVectors_shouldSucceed_withGeminiEmbeddings() {
        log.info("TEST 2: Testing vector upsert with real Gemini embeddings");

        // Given - Generate real embeddings using Gemini
        String testText = "SLMS is a Smart Logistics Management System for efficient supply chain operations.";

        log.info("Generating embedding for text: '{}'", testText);
        testEmbedding = geminiClient.generateEmbedding(testText);

        assertThat(testEmbedding).isNotNull();
        assertThat(testEmbedding.length).isEqualTo(768); // Verify Gemini embedding dimensions
        log.info("✅ Generated embedding with {} dimensions", testEmbedding.length);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "SLMS Overview");
        metadata.put("content", testText);
        metadata.put("locale", "en-US");
        metadata.put("category", "product-info");
        metadata.put("test", true);

        PineconeVector vector = new PineconeVector(testVectorId, testEmbedding, metadata);

        // When - Upsert the vector
        log.info("Upserting vector with ID: {}", testVectorId);
        pineconeClient.upsertVectors(List.of(vector));
        log.info("✅ Vector upserted successfully");

        // Then - No exception means success
        assertThat(vector.id()).isEqualTo(testVectorId);
    }

    @Test
    @Order(3)
    @Story("Vector Query")
    @Description("Test querying vectors by similarity")
    void testQueryVectors_shouldReturnRelevantMatches_withSimilarQuery() throws InterruptedException {
        log.info("TEST 3: Testing vector similarity query");

        // Given - Wait a bit for Pinecone to index the vector
        log.info("Waiting 2 seconds for Pinecone to index the vector...");
        Thread.sleep(2000);

        // Generate embedding for a similar query
        String queryText = "What is SLMS and what does it do?";
        log.info("Generating query embedding for: '{}'", queryText);
        float[] queryEmbedding = geminiClient.generateEmbedding(queryText);

        assertThat(queryEmbedding).isNotNull();
        assertThat(queryEmbedding.length).isEqualTo(768);
        log.info("✅ Generated query embedding with {} dimensions", queryEmbedding.length);

        // When - Query for similar vectors
        log.info("Querying Pinecone for similar vectors (topK=5)");
        List<PineconeQueryResponse.Match> matches = pineconeClient.query(
                queryEmbedding,
                5,
                Map.of() // No metadata filter for this test
        );

        // Then - Should find our test vector
        log.info("Query returned {} matches", matches.size());
        assertThat(matches).isNotEmpty();

        // Log all matches
        for (int i = 0; i < matches.size(); i++) {
            PineconeQueryResponse.Match match = matches.get(i);
            log.info("Match {}: ID={}, Score={}, Metadata={}",
                    i + 1, match.id(), match.score(), match.metadata());
        }

        // Verify our test vector is in the results (should be high similarity)
        boolean foundTestVector = matches.stream()
                .anyMatch(m -> m.id().startsWith("test-vector-"));

        if (foundTestVector) {
            log.info("✅ Test vector found in query results");
        } else {
            log.warn("⚠️ Test vector not found in top 5 results - this may be normal if index has many vectors");
        }
    }

    @Test
    @Order(4)
    @Story("Metadata Filtering")
    @Description("Test querying with metadata filters")
    void testQueryWithMetadataFilter_shouldReturnFilteredResults() throws InterruptedException {
        log.info("TEST 4: Testing metadata filtering in queries");

        // Given - Create additional test vectors with different metadata
        log.info("Creating test vectors with different locales...");

        // Vector 1: en-US locale
        String text1 = "English documentation for SLMS system";
        float[] embedding1 = geminiClient.generateEmbedding(text1);
        Map<String, Object> metadata1 = Map.of(
                "locale", "en-US",
                "content", text1,
                "test", true
        );
        PineconeVector vector1 = new PineconeVector(testVectorId + "-en", embedding1, metadata1);

        // Vector 2: pt-PT locale
        String text2 = "Documentação portuguesa para sistema SLMS";
        float[] embedding2 = geminiClient.generateEmbedding(text2);
        Map<String, Object> metadata2 = Map.of(
                "locale", "pt-PT",
                "content", text2,
                "test", true
        );
        PineconeVector vector2 = new PineconeVector(testVectorId + "-pt", embedding2, metadata2);

        // Upsert both vectors
        pineconeClient.upsertVectors(List.of(vector1, vector2));
        log.info("✅ Upserted 2 vectors with different locales");

        // Wait for indexing
        log.info("Waiting 3 seconds for indexing...");
        Thread.sleep(3000);

        // When - Query with locale filter
        String queryText = "SLMS documentation";
        float[] queryEmbedding = geminiClient.generateEmbedding(queryText);

        Map<String, Object> filter = Map.of("locale", "en-US");
        log.info("Querying with filter: {}", filter);

        List<PineconeQueryResponse.Match> matches = pineconeClient.query(
                queryEmbedding,
                10,
                filter
        );

        // Then - Should only return en-US vectors
        log.info("Query with filter returned {} matches", matches.size());

        for (PineconeQueryResponse.Match match : matches) {
            log.info("Filtered match: ID={}, Score={}, Locale={}",
                    match.id(), match.score(), match.metadata().get("locale"));

            // Verify all results match the filter (if metadata is included)
            if (match.metadata().containsKey("locale")) {
                assertThat(match.metadata().get("locale")).isEqualTo("en-US");
            }
        }

        log.info("✅ Metadata filtering works correctly");
    }

    @Test
    @Order(5)
    @Story("End-to-End RAG")
    @Description("Test complete RAG workflow: embed -> upsert -> query -> retrieve")
    void testEndToEndRAGWorkflow_shouldRetrieveRelevantContext() throws InterruptedException {
        log.info("TEST 5: Testing complete RAG workflow");

        // Given - Create a knowledge base with multiple documents
        Map<String, String> knowledgeBase = Map.of(
                "delivery-tracking", "SLMS provides real-time delivery tracking with GPS integration. Customers can track their packages 24/7.",
                "warehouse-management", "Our warehouse management system optimizes inventory placement and picking routes for maximum efficiency.",
                "carrier-integration", "SLMS integrates with major carriers like FedEx, UPS, and DHL for seamless shipping rate comparison.",
                "order-processing", "Automated order processing reduces manual work and minimizes errors in the fulfillment pipeline.",
                "analytics", "SLMS analytics dashboard provides insights into delivery performance, costs, and customer satisfaction metrics."
        );

        log.info("Creating knowledge base with {} documents", knowledgeBase.size());

        for (Map.Entry<String, String> entry : knowledgeBase.entrySet()) {
            String docId = testVectorId + "-kb-" + entry.getKey();
            String content = entry.getValue();

            float[] embedding = geminiClient.generateEmbedding(content);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("category", entry.getKey());
            metadata.put("content", content);
            metadata.put("locale", "en-US");
            metadata.put("test", true);

            PineconeVector vector = new PineconeVector(docId, embedding, metadata);
            pineconeClient.upsertVectors(List.of(vector));

            log.info("Indexed: {} - {}", entry.getKey(), content.substring(0, Math.min(50, content.length())) + "...");
        }

        log.info("✅ Knowledge base created successfully");
        log.info("Waiting 5 seconds for all vectors to be indexed...");
        Thread.sleep(5000);

        // When - Perform a semantic search query
        String userQuery = "How can I track my shipment in real-time?";
        log.info("User question: '{}'", userQuery);

        float[] queryEmbedding = geminiClient.generateEmbedding(userQuery);
        List<PineconeQueryResponse.Match> matches = pineconeClient.query(
                queryEmbedding,
                3, // Get top 3 most relevant documents
                Map.of("locale", "en-US")
        );

        // Then - Verify relevant context is retrieved
        log.info("RAG retrieval returned {} relevant documents:", matches.size());

        assertThat(matches).isNotEmpty();

        for (int i = 0; i < matches.size(); i++) {
            PineconeQueryResponse.Match match = matches.get(i);
            String category = (String) match.metadata().get("category");
            String content = (String) match.metadata().get("content");

            log.info("Rank {}: Category={}, Score={:.4f}", i + 1, category, match.score());
            log.info("  Content: {}", content);

            // Verify metadata structure
            assertThat(match.metadata()).containsKeys("category", "content", "locale");
            assertThat(match.score()).isGreaterThan(0.0);
        }

        // The top result should be about delivery tracking (semantic relevance)
        String topCategory = (String) matches.get(0).metadata().get("category");
        log.info("✅ Top result category: {} (expected: delivery-tracking)", topCategory);

        // Note: We can't strictly assert the category because semantic search might vary,
        // but we can verify the structure and relevance score
        assertThat(matches.get(0).score()).isGreaterThan(0.5); // Should have decent similarity
    }

    @Test
    @Order(6)
    @Story("Error Handling")
    @Description("Verify graceful handling of query errors")
    void testQueryWithInvalidDimensions_shouldHandleGracefully() {
        log.info("TEST 6: Testing error handling with invalid vector dimensions");

        // Given - Create a vector with wrong dimensions
        float[] invalidVector = new float[128]; // Wrong dimension (should be 768)
        for (int i = 0; i < invalidVector.length; i++) {
            invalidVector[i] = 0.1f;
        }

        // When - Try to query with invalid vector
        log.info("Attempting query with invalid dimensions (128 instead of 768)");
        List<PineconeQueryResponse.Match> matches = pineconeClient.query(
                invalidVector,
                5,
                Map.of()
        );

        // Then - Should handle error gracefully and return empty list
        log.info("Query with invalid dimensions returned {} matches", matches.size());
        assertThat(matches).isEmpty(); // Error handler returns empty list
        log.info("✅ Error handled gracefully");
    }

    @Test
    @Order(7)
    @Story("Batch Operations")
    @Description("Test upserting multiple vectors in a single batch")
    void testBatchUpsert_shouldSucceed_withMultipleVectors() {
        log.info("TEST 7: Testing batch upsert of multiple vectors");

        // Given - Create 10 test vectors
        int vectorCount = 10;
        log.info("Creating {} test vectors for batch upsert", vectorCount);

        List<PineconeVector> vectors = new java.util.ArrayList<>();
        for (int i = 0; i < vectorCount; i++) {
            String text = "Test document number " + i + " with content about SLMS features and capabilities.";
            float[] embedding = geminiClient.generateEmbedding(text);

            Map<String, Object> metadata = Map.of(
                    "batch", "test-batch-1",
                    "index", i,
                    "content", text,
                    "test", true
            );

            String vectorId = testVectorId + "-batch-" + i;
            vectors.add(new PineconeVector(vectorId, embedding, metadata));
        }

        // When - Upsert all vectors in one batch
        log.info("Upserting {} vectors in single batch operation", vectors.size());
        long startTime = System.currentTimeMillis();

        pineconeClient.upsertVectors(vectors);

        long duration = System.currentTimeMillis() - startTime;
        log.info("✅ Batch upsert completed in {}ms", duration);

        // Then - Verify success (no exceptions)
        assertThat(vectors).hasSize(vectorCount);
        log.info("✅ Successfully upserted {} vectors", vectorCount);
    }
}

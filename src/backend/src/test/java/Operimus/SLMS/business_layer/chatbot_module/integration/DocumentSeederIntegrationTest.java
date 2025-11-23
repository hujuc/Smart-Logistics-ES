package Operimus.SLMS.business_layer.chatbot_module.integration;

import Operimus.SLMS.business_layer.chatbot_module.dto.DocumentDto;
import Operimus.SLMS.business_layer.chatbot_module.integration.gemini.GeminiClient;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeClient;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeVector;
import Operimus.SLMS.business_layer.chatbot_module.repository.AssistantDocumentRepository;
import Operimus.SLMS.business_layer.chatbot_module.service.DocumentManagementService;
import Operimus.SLMS.business_layer.chatbot_module.service.DocumentSeederService;
import Operimus.SLMS.config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration test for document seeding workflow.
 * Tests that documents are properly seeded to both the database and Pinecone.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class DocumentSeederIntegrationTest {

    @Autowired
    private DocumentSeederService documentSeederService;

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private AssistantDocumentRepository documentRepository;

    @MockBean
    private GeminiClient geminiClient;

    @MockBean
    private PineconeClient pineconeClient;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        documentRepository.deleteAll();

        // Mock Gemini to return consistent embeddings
        when(geminiClient.generateEmbedding(anyString())).thenAnswer(invocation -> {
            float[] embedding = new float[768];
            for (int i = 0; i < embedding.length; i++) {
                embedding[i] = 0.01f;
            }
            return embedding;
        });

        // Mock Pinecone upsert to succeed
        doNothing().when(pineconeClient).upsertVectors(anyList());
    }

    @Test
    void seedInitialDocuments_shouldInsert39DocumentsIntoDatabase() {
        // When: Seed documents
        documentSeederService.seedInitialDocuments();

        // Then: All 39 documents should be in the database
        long count = documentRepository.count();
        assertThat(count).isEqualTo(39);
    }

    @Test
    void seedInitialDocuments_shouldCreateDocumentsWithCorrectTitles() {
        // When: Seed documents
        documentSeederService.seedInitialDocuments();

        // Then: Verify key documents exist
        List<DocumentDto> allDocs = documentManagementService.getAllDocuments();
        List<String> titles = allDocs.stream()
                .map(DocumentDto::title)
                .toList();

        assertThat(titles).contains(
                "What is SLMS - Smart Logistics Management System",
                "Getting Started Guide for Customers",
                "How to Track Your Orders in SLMS",
                "How to Report a Delivery Issue or Problem",
                "Warehouse Order Management - Processing and Fulfillment",
                "User Management for Administrators",
                "Using the Customer Service Dashboard",
                "Frequently Asked Questions for Customers"
        );
    }

    @Test
    void seedInitialDocuments_shouldGenerateEmbeddingsForAllDocuments() {
        // When: Seed documents
        documentSeederService.seedInitialDocuments();

        // Then: Gemini should be called 39 times for embeddings
        verify(geminiClient, times(39)).generateEmbedding(anyString());
    }

    @Test
    void seedInitialDocuments_shouldUpsertVectorsToPinecone() {
        // When: Seed documents
        documentSeederService.seedInitialDocuments();

        // Then: Pinecone upsert should be called 39 times (once per document)
        verify(pineconeClient, times(39)).upsertVectors(argThat(vectors -> {
            // Verify each upsert contains exactly one vector
            return vectors != null && vectors.size() == 1;
        }));
    }

    @Test
    void seedInitialDocuments_shouldCreateDocumentsWithRoleFilters() {
        // When: Seed documents
        documentSeederService.seedInitialDocuments();

        // Then: Verify documents have appropriate role filters
        List<DocumentDto> allDocs = documentManagementService.getAllDocuments();

        // Customer documents should have CUSTOMER role
        DocumentDto customerDoc = allDocs.stream()
                .filter(doc -> doc.title().equals("Getting Started Guide for Customers"))
                .findFirst()
                .orElseThrow();
        assertThat(customerDoc.roles()).contains("CUSTOMER", "ANONYMOUS");

        // Driver documents should have DRIVER role
        DocumentDto driverDoc = allDocs.stream()
                .filter(doc -> doc.title().equals("Getting Started Guide for Drivers"))
                .findFirst()
                .orElseThrow();
        assertThat(driverDoc.roles()).contains("DRIVER");

        // Admin documents should have ADMIN role
        DocumentDto adminDoc = allDocs.stream()
                .filter(doc -> doc.title().equals("User Management for Administrators"))
                .findFirst()
                .orElseThrow();
        assertThat(adminDoc.roles()).contains("ADMIN");
    }

    @Test
    void seedInitialDocuments_shouldCreateDocumentsWithFrontendFilters() {
        // When: Seed documents
        documentSeederService.seedInitialDocuments();

        // Then: Verify documents have appropriate frontend filters
        List<DocumentDto> allDocs = documentManagementService.getAllDocuments();

        // Customer portal documents
        DocumentDto customerDoc = allDocs.stream()
                .filter(doc -> doc.title().equals("Getting Started Guide for Customers"))
                .findFirst()
                .orElseThrow();
        assertThat(customerDoc.frontends()).contains("customer-portal", "landing-page");

        // Driver PWA documents
        DocumentDto driverDoc = allDocs.stream()
                .filter(doc -> doc.title().equals("Getting Started Guide for Drivers"))
                .findFirst()
                .orElseThrow();
        assertThat(driverDoc.frontends()).contains("driver-pwa");

        // Admin console documents
        DocumentDto adminDoc = allDocs.stream()
                .filter(doc -> doc.title().equals("Dashboard Statistics and Operational Reporting"))
                .findFirst()
                .orElseThrow();
        assertThat(adminDoc.frontends()).contains("admin-console");
    }

    @Test
    void seedInitialDocuments_shouldSetEnglishLocaleForAllDocuments() {
        // When: Seed documents
        documentSeederService.seedInitialDocuments();

        // Then: All documents should have en-US locale
        List<DocumentDto> allDocs = documentManagementService.getAllDocuments();
        assertThat(allDocs).allMatch(doc -> "en-US".equals(doc.locale()));
    }

    @Test
    void seedInitialDocuments_shouldCreateDocumentsWithContent() {
        // When: Seed documents
        documentSeederService.seedInitialDocuments();

        // Then: All documents should have non-empty content
        List<DocumentDto> allDocs = documentManagementService.getAllDocuments();
        assertThat(allDocs).allMatch(doc ->
                doc.content() != null &&
                !doc.content().isBlank() &&
                doc.content().length() > 100  // Substantial content
        );
    }

    @Test
    void seedInitialDocuments_shouldCreateDocumentsWithUniqueChunkIds() {
        // When: Seed documents
        documentSeederService.seedInitialDocuments();

        // Then: All chunk IDs should be unique
        List<DocumentDto> allDocs = documentManagementService.getAllDocuments();
        List<String> chunkIds = allDocs.stream()
                .map(DocumentDto::chunkId)
                .toList();

        long uniqueCount = chunkIds.stream().distinct().count();
        assertThat(uniqueCount).isEqualTo(39);
    }

    @Test
    void seedInitialDocuments_shouldIncludeVectorMetadataInPineconeUpsert() {
        // When: Seed documents
        documentSeederService.seedInitialDocuments();

        // Then: Verify Pinecone vectors include metadata
        verify(pineconeClient, atLeastOnce()).upsertVectors(argThat(vectors -> {
            if (vectors == null || vectors.isEmpty()) return false;

            PineconeVector vector = vectors.get(0);

            // Check that metadata exists and contains required fields
            return vector.metadata() != null &&
                   vector.metadata().containsKey("title") &&
                   vector.metadata().containsKey("content") &&
                   vector.metadata().containsKey("locale");
        }));
    }

    @Test
    void seedInitialDocuments_shouldNotReseedWhenDocumentsExist() {
        // Given: Documents already exist
        documentSeederService.seedInitialDocuments();
        long firstCount = documentRepository.count();

        // Reset mocks to track new calls
        reset(geminiClient, pineconeClient);

        // When: Try to seed again
        documentSeederService.seedInitialDocuments();

        // Then: Should not create duplicates
        long secondCount = documentRepository.count();
        assertThat(secondCount).isEqualTo(firstCount);

        // And: Should not call external services again
        verify(geminiClient, never()).generateEmbedding(anyString());
        verify(pineconeClient, never()).upsertVectors(anyList());
    }

    @Test
    void seedInitialDocuments_shouldContinueOnIndividualFailures() {
        // Given: One embedding fails but others succeed
        when(geminiClient.generateEmbedding(anyString()))
                .thenThrow(new RuntimeException("Embedding failed"))  // First fails
                .thenAnswer(invocation -> new float[768]);  // Rest succeed

        // When: Seed documents
        documentSeederService.seedInitialDocuments();

        // Then: Some documents should still be created (38 out of 39)
        long count = documentRepository.count();
        assertThat(count).isGreaterThan(0).isLessThan(39);
    }

    @Test
    void seedInitialDocuments_shouldCoverAllDocumentCategories() {
        // When: Seed documents
        documentSeederService.seedInitialDocuments();

        // Then: Verify we have documents from all major categories
        List<DocumentDto> allDocs = documentManagementService.getAllDocuments();
        List<String> titles = allDocs.stream().map(DocumentDto::title).toList();

        // System Overview
        assertThat(titles).anyMatch(t -> t.contains("SLMS") && t.contains("System"));

        // Getting Started
        assertThat(titles).anyMatch(t -> t.contains("Getting Started"));

        // Order Tracking
        assertThat(titles).anyMatch(t -> t.contains("Track") && t.contains("Order"));

        // Delivery Exceptions
        assertThat(titles).anyMatch(t -> t.contains("Delivery") && t.contains("Exception"));

        // Warehouse
        assertThat(titles).anyMatch(t -> t.contains("Warehouse"));

        // Administration
        assertThat(titles).anyMatch(t -> t.contains("Administrator") || t.contains("Management"));

        // FAQs
        assertThat(titles).anyMatch(t -> t.contains("Frequently Asked Questions"));
    }
}

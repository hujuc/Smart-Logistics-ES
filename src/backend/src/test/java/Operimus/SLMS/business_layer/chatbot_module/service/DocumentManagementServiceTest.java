package Operimus.SLMS.business_layer.chatbot_module.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Operimus.SLMS.business_layer.chatbot_module.dto.DocumentDto;
import Operimus.SLMS.business_layer.chatbot_module.dto.UpsertDocumentRequest;
import Operimus.SLMS.business_layer.chatbot_module.entities.AssistantDocument;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeClient;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeVector;
import Operimus.SLMS.business_layer.chatbot_module.repository.AssistantDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Tests for DocumentManagementService.
 * Verifies document CRUD operations and synchronization with Pinecone.
 */
@ExtendWith(MockitoExtension.class)
class DocumentManagementServiceTest {

    @Mock
    private AssistantDocumentRepository documentRepository;

    @Mock
    private PineconeClient pineconeClient;

    @Mock
    private EmbeddingService embeddingService;

    @Captor
    private ArgumentCaptor<AssistantDocument> documentCaptor;

    @Captor
    private ArgumentCaptor<List<PineconeVector>> vectorCaptor;

    private DocumentManagementService documentManagementService;

    @BeforeEach
    void setUp() {
        documentManagementService = new DocumentManagementService(
                documentRepository,
                pineconeClient,
                embeddingService
        );
    }

    @Test
    void upsertDocument_shouldSaveToDbAndPinecone() {
        // Given
        UpsertDocumentRequest request = new UpsertDocumentRequest(
                "Order Tracking Guide",
                Set.of("CUSTOMER", "ANONYMOUS"),
                Set.of("customer-portal", "landing-page"),
                Set.of("/orders", "/track"),
                "en-US",
                "To track your order, navigate to the My Orders page.",
                null
        );

        float[] embedding = new float[768];
        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = (float) (Math.random() * 0.1);
        }

        when(embeddingService.generateEmbedding(request.content())).thenReturn(embedding);
        when(documentRepository.save(any(AssistantDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        DocumentDto result = documentManagementService.upsertDocument(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Order Tracking Guide");
        assertThat(result.content()).isEqualTo("To track your order, navigate to the My Orders page.");
        assertThat(result.locale()).isEqualTo("en-US");
        assertThat(result.roles()).containsExactlyInAnyOrder("CUSTOMER", "ANONYMOUS");
        assertThat(result.frontends()).containsExactlyInAnyOrder("customer-portal", "landing-page");
        assertThat(result.routes()).containsExactlyInAnyOrder("/orders", "/track");

        // Verify DB save
        verify(documentRepository).save(documentCaptor.capture());
        AssistantDocument savedDoc = documentCaptor.getValue();
        assertThat(savedDoc.getTitle()).isEqualTo("Order Tracking Guide");
        assertThat(savedDoc.getContent()).isEqualTo(request.content());
        assertThat(savedDoc.getChunkId()).isNotNull();

        // Verify Pinecone upsert
        verify(pineconeClient).upsertVectors(vectorCaptor.capture());
        List<PineconeVector> vectors = vectorCaptor.getValue();
        assertThat(vectors).hasSize(1);

        PineconeVector vector = vectors.get(0);
        assertThat(vector.id()).isEqualTo(savedDoc.getChunkId());
        assertThat(vector.values()).isEqualTo(embedding);
        assertThat(vector.metadata()).containsEntry("title", "Order Tracking Guide");
        assertThat(vector.metadata()).containsEntry("content", request.content());
        assertThat(vector.metadata()).containsEntry("locale", "en-US");
        assertThat(vector.metadata()).containsKey("roles");
        assertThat(vector.metadata()).containsKey("frontends");
        assertThat(vector.metadata()).containsKey("routes");
    }

    @Test
    void upsertDocument_shouldGenerateEmbedding() {
        // Given
        UpsertDocumentRequest request = new UpsertDocumentRequest(
                "Test Doc",
                Set.of("CUSTOMER"),
                Set.of("customer-portal"),
                Set.of(),
                "en-US",
                "Test content for embedding",
                null
        );

        float[] embedding = new float[768];
        when(embeddingService.generateEmbedding("Test content for embedding")).thenReturn(embedding);
        when(documentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        documentManagementService.upsertDocument(request);

        // Then
        verify(embeddingService).generateEmbedding("Test content for embedding");
    }

    @Test
    void upsertDocument_shouldHandleEmptyMetadataSets() {
        // Given
        UpsertDocumentRequest request = new UpsertDocumentRequest(
                "Minimal Doc",
                Set.of(),  // Empty roles
                Set.of(),  // Empty frontends
                Set.of(),  // Empty routes
                "en-US",
                "Minimal content",
                null
        );

        float[] embedding = new float[768];
        when(embeddingService.generateEmbedding(anyString())).thenReturn(embedding);
        when(documentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        DocumentDto result = documentManagementService.upsertDocument(request);

        // Then
        assertThat(result).isNotNull();
        verify(pineconeClient).upsertVectors(argThat(vectors -> {
            PineconeVector vector = vectors.get(0);
            // Empty sets should not be included in metadata
            return !vector.metadata().containsKey("roles")
                    && !vector.metadata().containsKey("frontends")
                    && !vector.metadata().containsKey("routes");
        }));
    }

    @Test
    void getAllDocuments_shouldReturnAllDocuments() {
        // Given
        AssistantDocument doc1 = createTestDocument("Doc 1", "chunk-1");
        AssistantDocument doc2 = createTestDocument("Doc 2", "chunk-2");

        when(documentRepository.findAll()).thenReturn(List.of(doc1, doc2));

        // When
        List<DocumentDto> result = documentManagementService.getAllDocuments();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Doc 1");
        assertThat(result.get(1).title()).isEqualTo("Doc 2");
    }

    @Test
    void getAllDocuments_shouldReturnEmptyList_whenNoDocuments() {
        // Given
        when(documentRepository.findAll()).thenReturn(List.of());

        // When
        List<DocumentDto> result = documentManagementService.getAllDocuments();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getDocumentByChunkId_shouldReturnDocument_whenExists() {
        // Given
        String chunkId = "chunk-123";
        AssistantDocument document = createTestDocument("Test Doc", chunkId);

        when(documentRepository.findByChunkId(chunkId)).thenReturn(Optional.of(document));

        // When
        DocumentDto result = documentManagementService.getDocumentByChunkId(chunkId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.chunkId()).isEqualTo(chunkId);
        assertThat(result.title()).isEqualTo("Test Doc");
    }

    @Test
    void getDocumentByChunkId_shouldReturnNull_whenNotExists() {
        // Given
        String chunkId = "non-existent";
        when(documentRepository.findByChunkId(chunkId)).thenReturn(Optional.empty());

        // When
        DocumentDto result = documentManagementService.getDocumentByChunkId(chunkId);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void deleteDocument_shouldDeleteFromDb_whenExists() {
        // Given
        String chunkId = "chunk-to-delete";
        AssistantDocument document = createTestDocument("Doc to Delete", chunkId);

        when(documentRepository.findByChunkId(chunkId)).thenReturn(Optional.of(document));

        // When
        documentManagementService.deleteDocument(chunkId);

        // Then
        verify(documentRepository).delete(document);
    }

    @Test
    void deleteDocument_shouldDoNothing_whenNotExists() {
        // Given
        String chunkId = "non-existent";
        when(documentRepository.findByChunkId(chunkId)).thenReturn(Optional.empty());

        // When
        documentManagementService.deleteDocument(chunkId);

        // Then
        verify(documentRepository).findByChunkId(chunkId);
        // Delete should not be called
    }

    // Helper method to create test documents
    private AssistantDocument createTestDocument(String title, String chunkId) {
        return new AssistantDocument(
                UUID.randomUUID(),
                chunkId,
                title,
                Set.of("CUSTOMER"),
                Set.of("customer-portal"),
                Set.of("/test"),
                "en-US",
                "Test content",
                null,
                Instant.now()
        );
    }
}

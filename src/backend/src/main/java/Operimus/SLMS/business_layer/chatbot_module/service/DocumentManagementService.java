package Operimus.SLMS.business_layer.chatbot_module.service;

import Operimus.SLMS.business_layer.chatbot_module.dto.DocumentDto;
import Operimus.SLMS.business_layer.chatbot_module.dto.UpsertDocumentRequest;
import Operimus.SLMS.business_layer.chatbot_module.entities.AssistantDocument;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeClient;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeVector;
import Operimus.SLMS.business_layer.chatbot_module.repository.AssistantDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing assistant documents and syncing with Pinecone.
 */
@Service
public class DocumentManagementService {

    private final AssistantDocumentRepository documentRepository;
    private final PineconeClient pineconeClient;
    private final EmbeddingService embeddingService;

    public DocumentManagementService(AssistantDocumentRepository documentRepository,
                                     PineconeClient pineconeClient,
                                     EmbeddingService embeddingService) {
        this.documentRepository = documentRepository;
        this.pineconeClient = pineconeClient;
        this.embeddingService = embeddingService;
    }

    /**
     * Upsert a document: save to database and sync to Pinecone.
     *
     * @param request The document data
     * @return The created/updated document DTO
     */
    @Transactional
    public DocumentDto upsertDocument(UpsertDocumentRequest request) {
        // Generate embedding for the document content
        float[] embedding = embeddingService.generateEmbedding(request.content());

        // Create unique chunk ID
        String chunkId = UUID.randomUUID().toString();

        // Create entity
        AssistantDocument document = new AssistantDocument(
                UUID.randomUUID(),
                chunkId,
                request.title(),
                request.roles(),
                request.frontends(),
                request.routes(),
                request.locale(),
                request.content(),
                request.metadataJson(),
                Instant.now()
        );

        // Save to database
        documentRepository.save(document);

        // Prepare metadata for Pinecone
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", request.title());
        metadata.put("content", request.content());
        metadata.put("locale", request.locale());

        if (request.roles() != null && !request.roles().isEmpty()) {
            metadata.put("roles", List.copyOf(request.roles()));
        }
        if (request.frontends() != null && !request.frontends().isEmpty()) {
            metadata.put("frontends", List.copyOf(request.frontends()));
        }
        if (request.routes() != null && !request.routes().isEmpty()) {
            metadata.put("routes", List.copyOf(request.routes()));
        }

        // Upsert to Pinecone
        PineconeVector vector = new PineconeVector(chunkId, embedding, metadata);
        pineconeClient.upsertVectors(List.of(vector));

        return toDto(document);
    }

    /**
     * Get all documents.
     *
     * @return List of all documents
     */
    public List<DocumentDto> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get document by chunk ID.
     *
     * @param chunkId The chunk ID
     * @return Document DTO or null if not found
     */
    public DocumentDto getDocumentByChunkId(String chunkId) {
        return documentRepository.findByChunkId(chunkId)
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * Delete document by chunk ID.
     *
     * @param chunkId The chunk ID
     */
    @Transactional
    public void deleteDocument(String chunkId) {
        documentRepository.findByChunkId(chunkId)
                .ifPresent(doc -> {
                    documentRepository.delete(doc);
                    // TODO: Also delete from Pinecone
                    // (Pinecone client doesn't have delete method yet)
                });
    }

    /**
     * Convert entity to DTO.
     */
    private DocumentDto toDto(AssistantDocument document) {
        return new DocumentDto(
                document.getId().toString(),
                document.getChunkId(),
                document.getTitle(),
                document.getRoles(),
                document.getFrontends(),
                document.getRoutes(),
                document.getLocale(),
                document.getContent(),
                document.getMetadataJson(),
                document.getCreatedAt().toString()
        );
    }
}

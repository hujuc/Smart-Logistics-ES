package Operimus.SLMS.business_layer.chatbot_module.repository;

import Operimus.SLMS.business_layer.chatbot_module.entities.AssistantDocument;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistantDocumentRepository extends JpaRepository<AssistantDocument, UUID> {
    Optional<AssistantDocument> findByChunkId(String chunkId);
}

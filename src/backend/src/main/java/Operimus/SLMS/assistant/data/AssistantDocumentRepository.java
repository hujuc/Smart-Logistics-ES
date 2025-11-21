package Operimus.SLMS.assistant.data;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistantDocumentRepository extends JpaRepository<AssistantDocument, UUID> {
    Optional<AssistantDocument> findByChunkId(String chunkId);
}

package Operimus.SLMS.business_layer.chatbot_module.config;

import Operimus.SLMS.business_layer.chatbot_module.repository.AssistantDocumentRepository;
import Operimus.SLMS.business_layer.chatbot_module.service.DocumentSeederService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Initializes the chatbot knowledge base by seeding documents on application startup.
 * This only runs once if the database is empty and the feature is enabled.
 */
@Component
@ConditionalOnProperty(
        prefix = "chatbot.seeder",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class DocumentSeederInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DocumentSeederInitializer.class);

    private final DocumentSeederService documentSeederService;
    private final AssistantDocumentRepository documentRepository;

    public DocumentSeederInitializer(DocumentSeederService documentSeederService,
                                     AssistantDocumentRepository documentRepository) {
        this.documentSeederService = documentSeederService;
        this.documentRepository = documentRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            long existingDocCount = documentRepository.count();

            if (existingDocCount > 0) {
                log.info("Chatbot knowledge base already contains {} documents. Skipping seeding.", existingDocCount);
                return;
            }

            log.info("Chatbot knowledge base is empty. Starting document seeding...");
            documentSeederService.seedInitialDocuments();

            long finalCount = documentRepository.count();
            log.info("Document seeding completed successfully. Total documents: {}", finalCount);

        } catch (Exception e) {
            log.error("Failed to seed chatbot documents. The chatbot may not function properly.", e);
            // Don't throw exception - allow application to start even if seeding fails
        }
    }
}

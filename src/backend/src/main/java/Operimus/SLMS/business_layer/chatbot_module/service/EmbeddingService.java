package Operimus.SLMS.business_layer.chatbot_module.service;

import Operimus.SLMS.business_layer.chatbot_module.integration.gemini.GeminiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Service for generating text embeddings using Google Gemini.
 */
@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final GeminiClient geminiClient;

    public EmbeddingService(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    /**
     * Generate embedding vector for the given text.
     * @param text The text to embed
     * @return Float array representing the embedding vector
     * @throws IllegalArgumentException if text is null or empty
     */
    public float[] generateEmbedding(String text) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("Text to embed cannot be null or empty");
        }

        log.debug("Generating embedding for text of length: {}", text.length());
        float[] embedding = geminiClient.generateEmbedding(text);
        log.debug("Generated embedding with {} dimensions", embedding.length);

        return embedding;
    }
}

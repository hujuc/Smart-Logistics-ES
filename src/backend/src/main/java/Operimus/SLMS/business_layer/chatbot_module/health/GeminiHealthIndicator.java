package Operimus.SLMS.business_layer.chatbot_module.health;

import Operimus.SLMS.business_layer.chatbot_module.integration.gemini.GeminiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Health indicator for Google Gemini API connectivity.
 * Tests if the Gemini API is reachable and responding correctly.
 */
@Component("gemini")
@ConditionalOnProperty(prefix = "chatbot.gemini", name = "api-key")
public class GeminiHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(GeminiHealthIndicator.class);
    private static final String TEST_TEXT = "health check";

    private final GeminiClient geminiClient;

    public GeminiHealthIndicator(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    @Override
    public Health health() {
        try {
            // Test embedding generation (lighter than chat completion)
            float[] embedding = geminiClient.generateEmbedding(TEST_TEXT);

            if (embedding == null || embedding.length == 0) {
                return Health.down()
                        .withDetail("service", "Google Gemini AI")
                        .withDetail("error", "Empty embedding response")
                        .build();
            }

            return Health.up()
                    .withDetail("service", "Google Gemini AI")
                    .withDetail("embedding_dimensions", embedding.length)
                    .withDetail("status", "API is reachable and responding")
                    .build();

        } catch (Exception e) {
            log.warn("Gemini health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("service", "Google Gemini AI")
                    .withDetail("error", e.getMessage())
                    .withDetail("error_type", e.getClass().getSimpleName())
                    .build();
        }
    }
}

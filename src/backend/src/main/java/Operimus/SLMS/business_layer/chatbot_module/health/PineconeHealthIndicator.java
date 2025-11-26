package Operimus.SLMS.business_layer.chatbot_module.health;

import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeClient;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeProperties;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.dto.PineconeQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * Health indicator for Pinecone Vector Database connectivity.
 * Tests if the Pinecone index is reachable and can execute queries.
 */
@Component("pinecone")
@ConditionalOnProperty(prefix = "chatbot.pinecone", name = "api-key")
public class PineconeHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(PineconeHealthIndicator.class);

    private final PineconeClient pineconeClient;
    private final PineconeProperties properties;

    public PineconeHealthIndicator(PineconeClient pineconeClient, PineconeProperties properties) {
        this.pineconeClient = pineconeClient;
        this.properties = properties;
    }

    @Override
    public Health health() {
        try {
            // Execute a simple query with a dummy vector to test connectivity
            float[] testVector = new float[768];
            // Initialize with small values to avoid potential issues
            for (int i = 0; i < testVector.length; i++) {
                testVector[i] = 0.01f;
            }

            List<PineconeQueryResponse.Match> results = pineconeClient.query(
                    testVector,
                    1, // Just need one result to verify connectivity
                    new HashMap<>() // No filters
            );

            // If we got here without exception, Pinecone is reachable
            return Health.up()
                    .withDetail("service", "Pinecone Vector Database")
                    .withDetail("host", properties.host())
                    .withDetail("index", properties.index())
                    .withDetail("namespace", properties.namespace())
                    .withDetail("query_successful", true)
                    .withDetail("status", "Index is reachable and responding")
                    .build();

        } catch (Exception e) {
            log.warn("Pinecone health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("service", "Pinecone Vector Database")
                    .withDetail("host", properties.host())
                    .withDetail("index", properties.index())
                    .withDetail("namespace", properties.namespace())
                    .withDetail("error", e.getMessage())
                    .withDetail("error_type", e.getClass().getSimpleName())
                    .build();
        }
    }
}

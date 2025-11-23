package Operimus.SLMS.business_layer.chatbot_module.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import Operimus.SLMS.business_layer.chatbot_module.integration.gemini.GeminiClient;
import Operimus.SLMS.business_layer.shared.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

/**
 * Tests for GeminiHealthIndicator.
 */
@ExtendWith(MockitoExtension.class)
class GeminiHealthIndicatorTest {

    @Mock
    private GeminiClient geminiClient;

    private GeminiHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new GeminiHealthIndicator(geminiClient);
    }

    @Test
    void health_shouldReturnUp_whenGeminiIsReachable() {
        // Given
        float[] embedding = new float[768];
        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = 0.01f;
        }
        when(geminiClient.generateEmbedding(anyString())).thenReturn(embedding);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("service", "Google Gemini AI");
        assertThat(health.getDetails()).containsEntry("embedding_dimensions", 768);
        assertThat(health.getDetails()).containsKey("status");
    }

    @Test
    void health_shouldReturnDown_whenEmbeddingIsNull() {
        // Given
        when(geminiClient.generateEmbedding(anyString())).thenReturn(null);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("error", "Empty embedding response");
    }

    @Test
    void health_shouldReturnDown_whenEmbeddingIsEmpty() {
        // Given
        when(geminiClient.generateEmbedding(anyString())).thenReturn(new float[0]);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("error", "Empty embedding response");
    }

    @Test
    void health_shouldReturnDown_whenGeminiThrowsException() {
        // Given
        when(geminiClient.generateEmbedding(anyString()))
                .thenThrow(new ExternalServiceException("API key invalid"));

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("service", "Google Gemini AI");
        assertThat(health.getDetails()).containsEntry("error", "API key invalid");
        assertThat(health.getDetails()).containsEntry("error_type", "ExternalServiceException");
    }

    @Test
    void health_shouldReturnDown_whenNetworkError() {
        // Given
        when(geminiClient.generateEmbedding(anyString()))
                .thenThrow(new RuntimeException("Connection timeout"));

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("error", "Connection timeout");
    }
}

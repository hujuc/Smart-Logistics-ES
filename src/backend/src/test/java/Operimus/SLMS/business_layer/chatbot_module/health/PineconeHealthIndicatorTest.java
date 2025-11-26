package Operimus.SLMS.business_layer.chatbot_module.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeClient;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeProperties;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.dto.PineconeQueryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for PineconeHealthIndicator.
 */
@ExtendWith(MockitoExtension.class)
class PineconeHealthIndicatorTest {

    @Mock
    private PineconeClient pineconeClient;

    @Mock
    private PineconeProperties properties;

    private PineconeHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        when(properties.host()).thenReturn("https://test-index.svc.pinecone.io");
        when(properties.index()).thenReturn("test-index");
        when(properties.namespace()).thenReturn("test-namespace");

        healthIndicator = new PineconeHealthIndicator(pineconeClient, properties);
    }

    @Test
    void health_shouldReturnUp_whenPineconeIsReachable() {
        // Given
        PineconeQueryResponse.Match match = new PineconeQueryResponse.Match(
                "test-doc",
                0.9f,
                new HashMap<>()
        );
        when(pineconeClient.query(any(), anyInt(), anyMap())).thenReturn(List.of(match));

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("service", "Pinecone Vector Database");
        assertThat(health.getDetails()).containsEntry("host", "https://test-index.svc.pinecone.io");
        assertThat(health.getDetails()).containsEntry("index", "test-index");
        assertThat(health.getDetails()).containsEntry("namespace", "test-namespace");
        assertThat(health.getDetails()).containsEntry("query_successful", true);
    }

    @Test
    void health_shouldReturnUp_evenWithNoResults() {
        // Given - Empty index is still healthy
        when(pineconeClient.query(any(), anyInt(), anyMap())).thenReturn(List.of());

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("query_successful", true);
    }

    @Test
    void health_shouldReturnDown_whenQueryThrowsException() {
        // Given
        when(pineconeClient.query(any(), anyInt(), anyMap()))
                .thenThrow(new RuntimeException("Connection refused"));

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("service", "Pinecone Vector Database");
        assertThat(health.getDetails()).containsEntry("error", "Connection refused");
        assertThat(health.getDetails()).containsEntry("error_type", "RuntimeException");
    }

    @Test
    void health_shouldReturnDown_whenAuthenticationFails() {
        // Given
        when(pineconeClient.query(any(), anyInt(), anyMap()))
                .thenThrow(new RuntimeException("Unauthorized"));

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("error", "Unauthorized");
    }

    @Test
    void health_shouldIncludeIndexDetails_inDownState() {
        // Given
        when(pineconeClient.query(any(), anyInt(), anyMap()))
                .thenThrow(new RuntimeException("Index not found"));

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("host", "https://test-index.svc.pinecone.io");
        assertThat(health.getDetails()).containsEntry("index", "test-index");
        assertThat(health.getDetails()).containsEntry("namespace", "test-namespace");
    }
}

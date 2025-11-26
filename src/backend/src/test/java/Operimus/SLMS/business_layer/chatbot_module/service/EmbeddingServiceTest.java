package Operimus.SLMS.business_layer.chatbot_module.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Operimus.SLMS.business_layer.chatbot_module.integration.gemini.GeminiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for EmbeddingService.
 * Verifies text embedding generation and validation logic.
 */
@ExtendWith(MockitoExtension.class)
class EmbeddingServiceTest {

    @Mock
    private GeminiClient geminiClient;

    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingService(geminiClient);
    }

    @Test
    void generateEmbedding_shouldReturnEmbedding_whenTextIsValid() {
        // Given
        String text = "How do I track my order?";
        float[] expectedEmbedding = new float[768];
        for (int i = 0; i < expectedEmbedding.length; i++) {
            expectedEmbedding[i] = (float) (Math.random() * 0.1);
        }

        when(geminiClient.generateEmbedding(text)).thenReturn(expectedEmbedding);

        // When
        float[] result = embeddingService.generateEmbedding(text);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(768);
        assertThat(result).isEqualTo(expectedEmbedding);
        verify(geminiClient).generateEmbedding(text);
    }

    @Test
    void generateEmbedding_shouldThrowException_whenTextIsNull() {
        // When/Then
        assertThatThrownBy(() -> embeddingService.generateEmbedding(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Text to embed cannot be null or empty");
    }

    @Test
    void generateEmbedding_shouldThrowException_whenTextIsEmpty() {
        // When/Then
        assertThatThrownBy(() -> embeddingService.generateEmbedding(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Text to embed cannot be null or empty");
    }

    @Test
    void generateEmbedding_shouldThrowException_whenTextIsBlank() {
        // When/Then
        assertThatThrownBy(() -> embeddingService.generateEmbedding("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Text to embed cannot be null or empty");
    }

    @Test
    void generateEmbedding_shouldHandleLongText() {
        // Given
        String longText = "Lorem ipsum ".repeat(1000); // 12000 characters
        float[] expectedEmbedding = new float[768];

        when(geminiClient.generateEmbedding(longText)).thenReturn(expectedEmbedding);

        // When
        float[] result = embeddingService.generateEmbedding(longText);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(768);
        verify(geminiClient).generateEmbedding(longText);
    }

    @Test
    void generateEmbedding_shouldHandleSpecialCharacters() {
        // Given
        String textWithSpecialChars = "What's the status? Order #12345! @mention & <tag>";
        float[] expectedEmbedding = new float[768];

        when(geminiClient.generateEmbedding(textWithSpecialChars)).thenReturn(expectedEmbedding);

        // When
        float[] result = embeddingService.generateEmbedding(textWithSpecialChars);

        // Then
        assertThat(result).isNotNull();
        verify(geminiClient).generateEmbedding(textWithSpecialChars);
    }
}

package Operimus.SLMS.business_layer.chatbot_module.integration.gemini;

import Operimus.SLMS.business_layer.chatbot_module.integration.gemini.dto.*;
import Operimus.SLMS.business_layer.shared.exception.ExternalServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Client for Google Gemini API.
 * Handles both chat completions and text embeddings.
 */
@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    private static final String GEMINI_CHAT_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
    private static final String GEMINI_EMBEDDING_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:embedContent?key=%s";
    private static final String DEFAULT_CHAT_MODEL = "gemini-1.5-flash";
    private static final String DEFAULT_EMBEDDING_MODEL = "gemini-embedding-001";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GeminiProperties properties;

    public GeminiClient(RestTemplateBuilder restTemplateBuilder,
                        ObjectMapper objectMapper,
                        GeminiProperties properties) {

        Assert.hasText(properties.apiKey(), "chatbot.gemini.api-key must be configured");

        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /**
     * Generate a chat completion with optional conversation history.
     * @param systemPrompt The system prompt to set the assistant's behavior
     * @param messages List of conversation messages (role + content)
     * @return The assistant's response text
     */
    public String generateCompletion(String systemPrompt, List<Map<String, String>> messages) {
        try {
            String model = StringUtils.hasText(properties.model()) ? properties.model() : DEFAULT_CHAT_MODEL;
            String url = String.format(GEMINI_CHAT_URL, model, properties.apiKey());

            // Build Gemini content structure
            List<GeminiChatRequest.Content> contents = new ArrayList<>();

            // Add system prompt as first user message if provided
            if (StringUtils.hasText(systemPrompt)) {
                contents.add(new GeminiChatRequest.Content(
                        "user",
                        List.of(new GeminiChatRequest.Part(systemPrompt))
                ));
            }

            // Convert conversation messages to Gemini format
            if (messages != null && !messages.isEmpty()) {
                for (Map<String, String> msg : messages) {
                    String role = msg.get("role");
                    String content = msg.get("content");

                    // Map OpenAI-style roles to Gemini roles
                    String geminiRole = switch (role) {
                        case "system", "user" -> "user";
                        case "assistant" -> "model";
                        default -> "user";
                    };

                    contents.add(new GeminiChatRequest.Content(
                            geminiRole,
                            List.of(new GeminiChatRequest.Part(content))
                    ));
                }
            }

            // Build request with generation config
            GeminiChatRequest request = new GeminiChatRequest(
                    contents,
                    new GeminiChatRequest.GenerationConfig(0.7, 2048)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<GeminiChatRequest> requestEntity = new HttpEntity<>(request, headers);

            log.debug("Calling Gemini API at: {}", url);
            GeminiChatResponse response = restTemplate.postForObject(url, requestEntity, GeminiChatResponse.class);

            if (response == null) {
                throw new ExternalServiceException("Gemini API returned an empty response");
            }

            String responseText = response.getText();
            if (!StringUtils.hasText(responseText)) {
                throw new ExternalServiceException("Gemini API response did not contain any text output");
            }

            return responseText;

        } catch (RestClientException ex) {
            log.error("Failed to communicate with Gemini API", ex);
            throw new ExternalServiceException("Failed to communicate with the Gemini API", ex);
        }
    }

    /**
     * Generate embedding vector for the given text.
     * @param text The text to embed
     * @return Float array representing the embedding vector
     */
    public float[] generateEmbedding(String text) {
        try {
            String model = StringUtils.hasText(properties.embeddingModel())
                    ? properties.embeddingModel()
                    : DEFAULT_EMBEDDING_MODEL;
            String url = String.format(GEMINI_EMBEDDING_URL, model, properties.apiKey());

            // Build embedding request
            GeminiEmbeddingRequest request = new GeminiEmbeddingRequest(
                    model,
                    new GeminiEmbeddingRequest.Content(
                            new GeminiEmbeddingRequest.Part[]{
                                    new GeminiEmbeddingRequest.Part(text)
                            }
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<GeminiEmbeddingRequest> requestEntity = new HttpEntity<>(request, headers);

            log.debug("Calling Gemini Embedding API at: {}", url);
            GeminiEmbeddingResponse response = restTemplate.postForObject(url, requestEntity, GeminiEmbeddingResponse.class);

            if (response == null || response.embedding() == null || response.embedding().values() == null) {
                throw new ExternalServiceException("Gemini Embedding API returned an invalid response");
            }

            float[] embedding = response.embedding().values();
            log.debug("Generated embedding with {} dimensions", embedding.length);

            return embedding;

        } catch (RestClientException ex) {
            log.error("Failed to generate embedding with Gemini API", ex);
            throw new ExternalServiceException("Failed to generate embedding with Gemini API", ex);
        }
    }
}

package Operimus.SLMS.business_layer.chatbot_module.integration.groq;

import Operimus.SLMS.business_layer.chatbot_module.integration.groq.dto.GroqResponseDto;
import Operimus.SLMS.business_layer.shared.exception.ExternalServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class GroqClient {

    private static final String GROQ_CHAT_COMPLETIONS_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String DEFAULT_MODEL = "llama-3.3-70b-versatile";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GroqProperties properties;

    public GroqClient(RestTemplateBuilder restTemplateBuilder,
                      ObjectMapper objectMapper,
                      GroqProperties properties) {

        Assert.hasText(properties.apiKey(), "chatbot.groq.api-key must be configured");

        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /**
     * Generate a completion with optional conversation history.
     * @param systemPrompt The system prompt to set the assistant's behavior
     * @param messages List of conversation messages (role + content)
     * @return The assistant's response
     */
    public GroqResponseDto generateCompletion(String systemPrompt, List<Map<String, String>> messages) {
        List<Map<String, String>> allMessages = new ArrayList<>();

        // Add system message first
        if (StringUtils.hasText(systemPrompt)) {
            allMessages.add(Map.of("role", "system", "content", systemPrompt));
        }

        // Add conversation history
        if (messages != null && !messages.isEmpty()) {
            allMessages.addAll(messages);
        }

        String model = StringUtils.hasText(properties.model()) ? properties.model() : DEFAULT_MODEL;

        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", allMessages,
                "temperature", 0.6);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.apiKey());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        try {
            String responseBody = restTemplate.postForObject(GROQ_CHAT_COMPLETIONS_URL, requestEntity, String.class);
            if (responseBody == null) {
                throw new ExternalServiceException("Groq API returned an empty response");
            }

            JsonNode root = objectMapper.readTree(responseBody);
            String text = root
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText("");

            if (!StringUtils.hasText(text)) {
                throw new ExternalServiceException("Groq API response did not contain any text output");
            }

            return new GroqResponseDto(text);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Failed to communicate with the Groq API", ex);
        } catch (JsonProcessingException ex) {
            throw new ExternalServiceException("Unable to parse Groq API response", ex);
        }
    }
}

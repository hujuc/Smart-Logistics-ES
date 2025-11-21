package Operimus.SLMS.presentation_layer.groq;

import Operimus.SLMS.business_layer.shared.exception.ExternalServiceException;
import Operimus.SLMS.presentation_layer.groq.dto.GroqResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class GroqService {

    private static final String GROQ_CHAT_COMPLETIONS_URL = "https://api.groq.com/openai/v1/chat/completions";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GroqService(RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper,
            @Value("${groq.api.key:}") String apiKey,
            @Value("${groq.api.model:llama-3.3-70b-versatile}") String model) {

        if (!StringUtils.hasText(apiKey)) {
            throw new ExternalServiceException("Groq API key is not configured");
        }

        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    public GroqResponseDto generateCompletion(String prompt) {
        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "Voce e um assistente util especializado em logistica e supply chain."),
                        Map.of(
                                "role", "user",
                                "content", prompt)),
                "temperature", 0.6);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

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

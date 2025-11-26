package Operimus.SLMS.business_layer.chatbot_module.integration.pinecone;

import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.dto.PineconeQueryRequest;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.dto.PineconeQueryResponse;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.dto.PineconeUpsertRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PineconeClient {

    private static final Logger log = LoggerFactory.getLogger(PineconeClient.class);

    private final WebClient webClient;
    private final PineconeProperties properties;

    public PineconeClient(PineconeProperties properties) {
        Assert.hasText(properties.apiKey(), "chatbot.pinecone.api-key must be configured");
        Assert.hasText(properties.host(), "chatbot.pinecone.host must be configured");
        Assert.hasText(properties.index(), "chatbot.pinecone.index must be configured");
        Assert.hasText(properties.namespace(), "chatbot.pinecone.namespace must be configured");

        this.properties = properties;
        this.webClient = WebClient.builder()
                .baseUrl(properties.host())
                .defaultHeader("Api-Key", properties.apiKey())
                .defaultHeader("X-Pinecone-API-Version", "2025-01")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public void upsertVectors(List<PineconeVector> vectors) {
        if (vectors.isEmpty()) {
            return;
        }
        PineconeUpsertRequest payload = new PineconeUpsertRequest(properties.namespace(), vectors);
        webClient.post()
                .uri("/vectors/upsert")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(ex -> log.error("Failed to upsert {} Pinecone vectors", vectors.size(), ex))
                .block();
    }

    public List<PineconeQueryResponse.Match> query(float[] vector, int topK, Map<String, Object> metadataFilter) {
        PineconeQueryRequest request = new PineconeQueryRequest(
                properties.namespace(),
                vector,
                topK,
                true,
                metadataFilter
        );

        PineconeQueryResponse response = webClient.post()
                .uri("/query")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PineconeQueryResponse.class)
                .onErrorResume(ex -> {
                    log.error("Failed to query Pinecone", ex);
                    return Mono.just(new PineconeQueryResponse(List.of()));
                })
                .block();

        return response.matches();
    }
}

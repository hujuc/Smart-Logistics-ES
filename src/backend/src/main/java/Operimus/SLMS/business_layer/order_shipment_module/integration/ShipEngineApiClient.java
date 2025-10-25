package Operimus.SLMS.business_layer.order_shipment_module.integration;

import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierAddressView;
import Operimus.SLMS.business_layer.shared.exception.ExternalServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class ShipEngineApiClient {

    private final RestTemplate restTemplate;

    @Value("${integrations.shipengine.api-key:}")
    private String apiKey;

    @Value("${integrations.shipengine.base-url:https://api.shipengine.com/v1}")
    private String baseUrl;

    public ShipEngineApiClient(@Qualifier("shipEngineRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<ShipEngineCarrier> fetchCarriers() {
        if (!StringUtils.hasText(apiKey)) {
            log.debug("Skipping ShipEngine carrier fetch because API key is not configured");
            return Collections.emptyList();
        }

        try {
            JsonNode response = executeGet("/carriers");
            List<ShipEngineCarrier> carriers = new ArrayList<>();
            if (response == null) {
                return carriers;
            }

            JsonNode carriersNode = response.path("carriers");
            if (carriersNode.isArray()) {
                carriersNode.forEach(node -> mapCarrier(node).ifPresent(carriers::add));
            }

            return carriers;
        } catch (RestClientResponseException ex) {
            HttpStatus status = HttpStatus.resolve(ex.getRawStatusCode());
            String message = status != null
                    ? "ShipEngine API responded with %s (%d)".formatted(status.getReasonPhrase(), status.value())
                    : "ShipEngine API responded with HTTP %d".formatted(ex.getRawStatusCode());
            throw new ExternalServiceException(message, ex);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Unable to communicate with ShipEngine API", ex);
        }
    }

    private java.util.Optional<ShipEngineCarrier> mapCarrier(JsonNode node) {
        if (node == null) {
            return java.util.Optional.empty();
        }

        String carrierCode = node.path("carrier_code").asText(null);
        String carrierId = node.path("carrier_id").asText(null);
        String displayName = firstNonEmpty(
                node.path("friendly_name"), node.path("carrier_friendly_name"), node.path("name"));
        String accountNumber = node.path("account_number").asText(null);
        boolean active = !"disabled".equalsIgnoreCase(node.path("status").asText("active"));
        String supportEmail = node.path("support_email").asText(null);
        if (!StringUtils.hasText(supportEmail)) {
            supportEmail = node.path("email").asText(null);
        }
        String supportPhone = node.path("contact_phone_number").asText(null);
        if (!StringUtils.hasText(supportPhone)) {
            supportPhone = node.path("phone").asText(null);
        }
        String website = node.path("website_url").asText(null);

        List<String> serviceLevels = new ArrayList<>();
        JsonNode serviceLevelsNode = node.path("service_levels");
        if (serviceLevelsNode.isArray()) {
            serviceLevelsNode.forEach(service -> {
                String value = firstNonEmpty(service.path("name"), service.path("service_name"));
                if (!StringUtils.hasText(value)) {
                    value = service.path("token").asText(null);
                }
                if (StringUtils.hasText(value)) {
                    serviceLevels.add(value);
                }
            });
        }

        CarrierAddressView address = null;
        JsonNode addressNode = node.path("address");
        if (addressNode.isObject()) {
            address = CarrierAddressView.builder()
                    .addressLine1(firstNonEmpty(addressNode.path("street1"), addressNode.path("line1")))
                    .addressLine2(firstNonEmpty(addressNode.path("street2"), addressNode.path("line2")))
                    .city(addressNode.path("city").asText(null))
                    .stateOrProvince(firstNonEmpty(addressNode.path("state"), addressNode.path("province")))
                    .postalCode(firstNonEmpty(addressNode.path("postal_code"), addressNode.path("zip")))
                    .country(addressNode.path("country_code").asText(null))
                    .build();
        }

        if (!StringUtils.hasText(displayName)) {
            displayName = carrierCode;
        }

        return java.util.Optional.of(new ShipEngineCarrier(
                carrierCode,
                displayName,
                carrierId,
                accountNumber,
                active,
                supportEmail,
                supportPhone,
                serviceLevels,
                address,
                website));
    }

    private String firstNonEmpty(JsonNode... nodes) {
        if (nodes == null) {
            return null;
        }
        for (JsonNode node : nodes) {
            if (node != null && node.isValueNode() && StringUtils.hasText(node.asText())) {
                return node.asText();
            }
        }
        return null;
    }

    private JsonNode executeGet(String path) {
        String target = path;
        if (!target.startsWith("http")) {
            if (!target.startsWith("/")) {
                target = "/" + target;
            }
            target = baseUrl + target;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        if (StringUtils.hasText(apiKey)) {
            headers.set("API-Key", apiKey.trim());
        }

        RequestEntity<Void> request = new RequestEntity<>(headers, HttpMethod.GET, URI.create(target));
        ResponseEntity<JsonNode> response = restTemplate.exchange(request, JsonNode.class);
        return response.getBody();
    }
}

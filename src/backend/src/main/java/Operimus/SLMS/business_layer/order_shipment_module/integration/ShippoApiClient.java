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
public class ShippoApiClient {

    private final RestTemplate restTemplate;

    @Value("${integrations.shippo.api-key:}")
    private String apiKey;

    @Value("${integrations.shippo.base-url:https://api.goshippo.com}")
    private String baseUrl;

    public ShippoApiClient(@Qualifier("shippoRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<ShippoCarrierAccount> fetchCarrierAccounts() {
        if (!StringUtils.hasText(apiKey)) {
            log.debug("Skipping Shippo carrier fetch because API key is not configured");
            return Collections.emptyList();
        }

        String next = "/carrier_accounts/";
        List<ShippoCarrierAccount> carriers = new ArrayList<>();

        while (StringUtils.hasText(next)) {
            JsonNode payload = execute(next);
            if (payload == null) {
                break;
            }

            JsonNode results = payload.path("results");
            if (results.isArray()) {
                results.forEach(node -> mapAccount(node).ifPresent(carriers::add));
            }

            JsonNode nextNode = payload.path("next");
            next = (nextNode.isMissingNode() || nextNode.isNull()) ? null : nextNode.asText(null);
        }

        return carriers;
    }

    private JsonNode execute(String uri) {
        try {
            if (!StringUtils.hasText(uri)) {
                return null;
            }

            String target = uri;
            if (!target.startsWith("http")) {
                if (!target.startsWith("/")) {
                    target = "/" + target;
                }
                target = baseUrl + target;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            if (StringUtils.hasText(apiKey)) {
                headers.set(HttpHeaders.AUTHORIZATION, "ShippoToken " + apiKey.trim());
            }

            RequestEntity<Void> request = new RequestEntity<>(headers, HttpMethod.GET, URI.create(target));
            ResponseEntity<JsonNode> response = restTemplate.exchange(request, JsonNode.class);
            return response.getBody();
        } catch (RestClientResponseException ex) {
            HttpStatus status = HttpStatus.resolve(ex.getRawStatusCode());
            String message = status != null
                    ? "Shippo API responded with %s (%d)".formatted(status.getReasonPhrase(), status.value())
                    : "Shippo API responded with HTTP %d".formatted(ex.getRawStatusCode());
            throw new ExternalServiceException(message, ex);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Unable to communicate with Shippo API", ex);
        }
    }

    private java.util.Optional<ShippoCarrierAccount> mapAccount(JsonNode node) {
        if (node == null) {
            return java.util.Optional.empty();
        }

        String carrierCode = node.path("carrier").asText(null);
        String objectId = node.path("object_id").asText(null);
        String accountId = node.path("account_id").asText(null);
        boolean active = node.path("active").asBoolean(true);

        JsonNode parameters = node.path("parameters");
        if (parameters != null && !parameters.isMissingNode()) {
            if (!StringUtils.hasText(accountId)) {
                accountId = parameters.path("account_id").asText(accountId);
            }
        }

        String displayName = null;
        String supportEmail = null;
        String supportPhone = null;
        List<String> services = new ArrayList<>();
        CarrierAddressView address = null;

        if (parameters != null && !parameters.isMissingNode()) {
            displayName = firstText(parameters.path("company"), parameters.path("description"));
            supportEmail = firstText(parameters.path("email"), parameters.path("support_email"));
            supportPhone = firstText(parameters.path("phone"), parameters.path("support_phone"));

            JsonNode servicesNode = parameters.path("services");
            if (servicesNode.isArray()) {
                servicesNode.forEach(service -> {
                    String value = service.isObject() ? service.path("name").asText(null) : service.asText(null);
                    if (StringUtils.hasText(value)) {
                        services.add(value);
                    }
                });
            }

            JsonNode addressNode = parameters.path("address");
            if (addressNode.isObject()) {
                address = CarrierAddressView.builder()
                        .addressLine1(firstText(addressNode.path("street1"), addressNode.path("street_1")))
                        .addressLine2(firstText(addressNode.path("street2"), addressNode.path("street_2")))
                        .city(addressNode.path("city").asText(null))
                        .stateOrProvince(firstText(addressNode.path("state"), addressNode.path("province")))
                        .postalCode(firstText(addressNode.path("zip"), addressNode.path("postal_code")))
                        .country(addressNode.path("country").asText(null))
                        .build();
            }
        }

        if (!StringUtils.hasText(displayName)) {
            displayName = node.path("carrier_name").asText(null);
        }
        if (!StringUtils.hasText(displayName)) {
            displayName = carrierCode;
        }

        return java.util.Optional.of(new ShippoCarrierAccount(
                carrierCode,
                displayName,
                objectId,
                accountId,
                active,
                supportEmail,
                supportPhone,
                services,
                address));
    }

    private String firstText(JsonNode primary, JsonNode fallback) {
        if (primary != null && primary.isValueNode() && StringUtils.hasText(primary.asText())) {
            return primary.asText();
        }
        if (fallback != null && fallback.isValueNode() && StringUtils.hasText(fallback.asText())) {
            return fallback.asText();
        }
        return null;
    }
}

package Operimus.SLMS.business_layer.rate_carrier_module.integration;

import Operimus.SLMS.business_layer.rate_carrier_module.dto.AddressValidationRequest;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.AddressValidationResponse;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.CarrierAddressView;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.ShippoLabelResponse;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.ShippingRateRequest;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.ShippingRateResponse;
import Operimus.SLMS.business_layer.shared.exception.ExternalServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
            JsonNode payload = execute(HttpMethod.GET, next, null);
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

    public AddressValidationResponse validateAddress(AddressValidationRequest request) {
        if (!StringUtils.hasText(apiKey)) {
            throw new ExternalServiceException("Shippo API key is not configured");
        }

        Map<String, Object> payload = buildAddressPayload(request);
        payload.put("validate", Boolean.TRUE);

        JsonNode response = execute(HttpMethod.POST, "/addresses", payload);
        if (response == null) {
            return AddressValidationResponse.builder()
                    .valid(false)
                    .messages(Collections.singletonList("Shippo API returned an empty response"))
                    .build();
        }

        boolean valid = response.path("validation_results").path("is_valid").asBoolean(false);
        List<String> messages = extractValidationMessages(response.path("validation_results"));

        CarrierAddressView normalized = CarrierAddressView.builder()
                .addressLine1(firstText(response.path("street1"), response.path("street_1")))
                .addressLine2(firstText(response.path("street2"), response.path("street_2")))
                .city(response.path("city").asText(null))
                .stateOrProvince(firstText(response.path("state"), response.path("province")))
                .postalCode(firstText(response.path("zip"), response.path("postal_code")))
                .country(response.path("country").asText(null))
                .build();

        return AddressValidationResponse.builder()
                .valid(valid)
                .normalizedAddress(normalized)
                .messages(messages)
                .build();
    }

    public ShippingRateResponse calculateRates(ShippingRateRequest request) {
        if (!StringUtils.hasText(apiKey)) {
            throw new ExternalServiceException("Shippo API key is not configured");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("address_from", buildAddressPayload(request.getFromAddress()));
        payload.put("address_to", buildAddressPayload(request.getToAddress()));
        payload.put(
                "parcels",
                request.getParcels() == null
                        ? Collections.emptyList()
                        : request.getParcels().stream()
                                .map(this::buildParcelPayload)
                                .collect(Collectors.toList()));
        payload.put("async", Boolean.FALSE);

        JsonNode response = execute(HttpMethod.POST, "/shipments", payload);
        if (response == null) {
            return ShippingRateResponse.builder()
                    .messages(Collections.singletonList("Shippo API returned an empty response"))
                    .build();
        }

        List<ShippingRateResponse.RateQuote> rates = new ArrayList<>();
        JsonNode ratesNode = response.path("rates");
        if (ratesNode.isArray()) {
            ratesNode.forEach(rateNode -> {
                ShippingRateResponse.RateQuote quote = mapRate(rateNode);
                if (quote.getAmount() != null) {
                    rates.add(quote);
                }
            });
        }

        List<String> messages = new ArrayList<>();
        JsonNode messagesNode = response.path("messages");
        if (messagesNode.isArray()) {
            messagesNode.forEach(messageNode -> appendMessage(messages, messageNode));
        }

        return ShippingRateResponse.builder()
                .shipmentId(response.path("object_id").asText(null))
                .rates(rates)
                .messages(messages)
                .build();
    }

    public ShippoLabelResponse purchaseLabel(String rateId, String labelFileType) {
        if (!StringUtils.hasText(apiKey)) {
            throw new ExternalServiceException("Shippo API key is not configured");
        }
        if (!StringUtils.hasText(rateId)) {
            throw new IllegalArgumentException("Rate identifier is required to purchase a label");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("rate", rateId);
        payload.put("label_file_type", StringUtils.hasText(labelFileType) ? labelFileType : "PDF");
        payload.put("async", Boolean.FALSE);

        JsonNode response = execute(HttpMethod.POST, "/transactions", payload);
        if (response == null) {
            throw new ExternalServiceException("Shippo API returned an empty transaction response");
        }

        return mapTransaction(response);
    }

    public ShippoLabelResponse getTransaction(String transactionId) {
        if (!StringUtils.hasText(apiKey)) {
            throw new ExternalServiceException("Shippo API key is not configured");
        }
        if (!StringUtils.hasText(transactionId)) {
            throw new IllegalArgumentException("Shippo transaction identifier is required");
        }

        JsonNode response = execute(HttpMethod.GET, "/transactions/" + transactionId, null);
        if (response == null) {
            throw new ExternalServiceException("Shippo API returned an empty transaction response");
        }

        return mapTransaction(response);
    }

    public byte[] downloadLabel(String labelUrl) {
        if (!StringUtils.hasText(labelUrl)) {
            throw new ExternalServiceException("Shippo label URL is empty");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_PDF));
            if (StringUtils.hasText(apiKey)) {
                headers.set(HttpHeaders.AUTHORIZATION, "ShippoToken " + apiKey.trim());
            }
            RequestEntity<Void> request = new RequestEntity<>(headers, HttpMethod.GET, URI.create(labelUrl));
            ResponseEntity<byte[]> response = restTemplate.exchange(request, byte[].class);
            byte[] body = response.getBody();
            if (body == null || body.length == 0) {
                throw new ExternalServiceException("Shippo returned an empty label payload");
            }
            return body;
        } catch (RestClientResponseException ex) {
            HttpStatus status = HttpStatus.resolve(ex.getRawStatusCode());
            String message = status != null
                    ? "Shippo label download failed with %s (%d)".formatted(status.getReasonPhrase(), status.value())
                    : "Shippo label download failed with HTTP %d".formatted(ex.getRawStatusCode());
            throw new ExternalServiceException(message, ex);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Unable to download Shippo label", ex);
        }
    }

    private JsonNode execute(HttpMethod method, String uri, Object body) {
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
            headers.setContentType(MediaType.APPLICATION_JSON);

            RequestEntity<Object> request = new RequestEntity<>(body, headers, method, URI.create(target));
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

    private Map<String, Object> buildAddressPayload(AddressValidationRequest request) {
        Map<String, Object> payload = new HashMap<>();
        if (request == null) {
            return payload;
        }
        putIfText(payload, "name", request.getName());
        putIfText(payload, "company", request.getCompany());
        putIfText(payload, "street1", request.getAddressLine1());
        putIfText(payload, "street2", request.getAddressLine2());
        putIfText(payload, "city", request.getCity());
        putIfText(payload, "state", request.getStateOrProvince());
        putIfText(payload, "zip", request.getPostalCode());
        putIfText(payload, "country", request.getCountry());
        putIfText(payload, "phone", request.getPhone());
        putIfText(payload, "email", request.getEmail());
        if (request.getResidential() != null) {
            payload.put("is_residential", request.getResidential());
        }
        return payload;
    }

    private Map<String, Object> buildAddressPayload(ShippingRateRequest.Address address) {
        Map<String, Object> payload = new HashMap<>();
        if (address == null) {
            return payload;
        }
        putIfText(payload, "name", address.getName());
        putIfText(payload, "company", address.getCompany());
        putIfText(payload, "street1", address.getAddressLine1());
        putIfText(payload, "street2", address.getAddressLine2());
        putIfText(payload, "city", address.getCity());
        putIfText(payload, "state", address.getStateOrProvince());
        putIfText(payload, "zip", address.getPostalCode());
        putIfText(payload, "country", address.getCountry());
        putIfText(payload, "phone", address.getPhone());
        putIfText(payload, "email", address.getEmail());
        if (address.getResidential() != null) {
            payload.put("is_residential", address.getResidential());
        }
        return payload;
    }

    private Map<String, Object> buildParcelPayload(ShippingRateRequest.Parcel parcel) {
        Map<String, Object> payload = new HashMap<>();
        if (parcel == null) {
            return payload;
        }
        if (parcel.getWeight() != null) {
            payload.put("weight", parcel.getWeight());
        }
        putIfText(payload, "mass_unit", parcel.getWeightUnit());
        if (parcel.getLength() != null) {
            payload.put("length", parcel.getLength());
        }
        if (parcel.getWidth() != null) {
            payload.put("width", parcel.getWidth());
        }
        if (parcel.getHeight() != null) {
            payload.put("height", parcel.getHeight());
        }
        putIfText(payload, "distance_unit", parcel.getDistanceUnit());
        return payload;
    }

    private void putIfText(Map<String, Object> payload, String key, String value) {
        if (StringUtils.hasText(value)) {
            payload.put(key, value);
        }
    }

    private List<String> extractValidationMessages(JsonNode validationResults) {
        List<String> messages = new ArrayList<>();
        if (validationResults == null || validationResults.isMissingNode()) {
            return messages;
        }
        JsonNode errors = validationResults.path("errors");
        if (errors.isArray()) {
            errors.forEach(node -> appendMessage(messages, node));
        }
        JsonNode notes = validationResults.path("messages");
        if (notes.isArray()) {
            notes.forEach(node -> appendMessage(messages, node));
        }
        return messages;
    }

    private void appendMessage(List<String> messages, JsonNode node) {
        if (node == null) {
            return;
        }
        String text = firstText(node.path("text"), node.path("message"));
        if (!StringUtils.hasText(text)) {
            text = node.asText(null);
        }
        if (StringUtils.hasText(text)) {
            messages.add(text);
        }
    }

    private ShippingRateResponse.RateQuote mapRate(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return ShippingRateResponse.RateQuote.builder().build();
        }

        BigDecimal amount = null;
        String amountText = node.path("amount").asText(null);
        if (StringUtils.hasText(amountText)) {
            try {
                amount = new BigDecimal(amountText);
            } catch (NumberFormatException ex) {
                log.warn("Unable to parse Shippo rate amount '{}'", amountText, ex);
            }
        }

        Integer estimatedDays = node.path("estimated_days").isInt() ? node.path("estimated_days").asInt() : null;
        OffsetDateTime deliveryDate = parseOffsetDateTime(node.path("delivery_date").asText(null));

        String serviceLevel = null;
        JsonNode serviceNode = node.path("servicelevel");
        if (serviceNode.isObject()) {
            serviceLevel = firstText(serviceNode.path("name"), serviceNode.path("token"));
        }
        if (!StringUtils.hasText(serviceLevel)) {
            serviceLevel = firstText(node.path("servicelevel_name"), node.path("servicelevel_token"));
        }

        return ShippingRateResponse.RateQuote.builder()
                .rateId(node.path("object_id").asText(null))
                .provider(node.path("provider").asText(null))
                .carrierAccount(node.path("carrier_account").asText(null))
                .carrierCode(node.path("carrier").asText(null))
                .serviceLevel(serviceLevel)
                .amount(amount)
                .currency(node.path("currency").asText(null))
                .estimatedDays(estimatedDays)
                .deliveryDate(deliveryDate)
                .build();
    }

    private ShippoLabelResponse mapTransaction(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return ShippoLabelResponse.builder().build();
        }

        BigDecimal amount = parseDecimal(node.path("amount").asText(null));
        List<String> messages = new ArrayList<>();
        JsonNode messagesNode = node.path("messages");
        if (messagesNode.isArray()) {
            messagesNode.forEach(messageNode -> appendMessage(messages, messageNode));
        }

        JsonNode serviceLevelNode = node.path("servicelevel");
        String serviceLevel = firstText(serviceLevelNode.path("name"), serviceLevelNode.path("token"));

        return ShippoLabelResponse.builder()
                .transactionId(node.path("object_id").asText(null))
                .status(node.path("status").asText(null))
                .labelUrl(node.path("label_url").asText(null))
                .labelFileType(node.path("label_file_type").asText(null))
                .trackingNumber(node.path("tracking_number").asText(null))
                .trackingUrl(firstText(node.path("tracking_url_provider"), node.path("tracking_url")))
                .carrier(node.path("carrier" ).asText(null))
                .carrierAccount(node.path("carrier_account").asText(null))
                .serviceLevel(serviceLevel)
                .shipmentId(node.path("shipment").asText(null))
                .rateId(node.path("rate").asText(null))
                .amount(amount)
                .currency(node.path("currency").asText(null))
                .commercialInvoiceUrl(node.path("commercial_invoice_url").asText(null))
                .messages(messages)
                .build();
    }

    private BigDecimal parseDecimal(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            log.debug("Unable to parse decimal value '{}'", value, ex);
            return null;
        }
    }

    private OffsetDateTime parseOffsetDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            log.debug("Unable to parse offset date time '{}'", value, ex);
            return null;
        }
    }

    private java.util.Optional<ShippoCarrierAccount> mapAccount(JsonNode node) {
        if (node == null) {
            return java.util.Optional.empty();
        }

        String carrierCode = node.path("carrier").asText(null);
        String objectId = node.path("object_id").asText(null);
        String rawAccountId = node.path("account_id").asText(null);
        String accountId = rawAccountId;
        boolean active = node.path("active").asBoolean(true);

        JsonNode parameters = node.path("parameters");
        if (!StringUtils.hasText(accountId) && parameters != null && !parameters.isMissingNode()) {
            accountId = parameters.path("account_id").asText(null);
        }
        String parametersAccountId = accountId;
        if (!StringUtils.hasText(accountId)) {
            accountId = objectId;
        }

        log.debug("Shippo carrier payload: code={}, objectId={}, rawAccountId={}, parametersAccountId={}, resolvedAccountId={}",
                carrierCode, objectId, rawAccountId, parametersAccountId, accountId);

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

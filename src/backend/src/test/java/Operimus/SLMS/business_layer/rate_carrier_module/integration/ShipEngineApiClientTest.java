package Operimus.SLMS.business_layer.rate_carrier_module.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import Operimus.SLMS.business_layer.shared.exception.ExternalServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ShipEngineApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    private ShipEngineApiClient client;

    @BeforeEach
    void setUp() {
        client = new ShipEngineApiClient(restTemplate);
        ReflectionTestUtils.setField(client, "baseUrl", "https://api.shipengine.com/v1");
    }

    @Test
    void fetchCarriers_shouldReturnEmpty_whenApiKeyMissing() {
        ReflectionTestUtils.setField(client, "apiKey", "");

        List<ShipEngineCarrier> result = client.fetchCarriers();

        assertThat(result).isEmpty();
        verifyNoInteractions(restTemplate);
    }

    @Test
    void fetchCarriers_shouldMapAndFilterAllowedCarriers() throws Exception {
        ReflectionTestUtils.setField(client, "apiKey", "secret-key");

        String payload = """
                {
                  "carriers": [
                    {
                      "carrier_code": "ups",
                      "carrier_id": "se-338909",
                      "friendly_name": "UPS Friendly",
                      "account_number": "acc-1",
                      "status": "active",
                      "support_email": "help@ups.com",
                      "contact_phone_number": "+1 111",
                      "website_url": "https://ups.com",
                      "service_levels": [
                        { "name": "Express" },
                        { "service_name": "Economy" }
                      ],
                      "services": [
                        { "service_name": "Express" },
                        { "service_code": "Saver" }
                      ],
                      "address": {
                        "street1": "123 Road",
                        "city": "NYC",
                        "state": "NY",
                        "postal_code": "10001",
                        "country_code": "US"
                      }
                    },
                    {
                      "carrier_code": "dhl",
                      "carrier_id": "se-111111",
                      "friendly_name": "DHL",
                      "status": "disabled"
                    },
                    {
                      "carrier_code": "fedex",
                      "carrier_id": "se-other",
                      "name": "FedEx",
                      "status": "active"
                    }
                  ]
                }
                """;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode body = mapper.readTree(payload);
        when(restTemplate.exchange(any(RequestEntity.class), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(body));

        List<ShipEngineCarrier> carriers = client.fetchCarriers();

        assertThat(carriers).hasSize(2);

        ShipEngineCarrier ups = carriers.stream()
                .filter(c -> "ups".equals(c.carrierCode()))
                .findFirst()
                .orElseThrow();
        assertThat(ups.displayName()).isEqualTo("UPS Friendly");
        assertThat(ups.serviceLevels()).containsExactlyInAnyOrder("Express", "Economy", "Saver");
        assertThat(ups.address()).isNotNull();
        assertThat(ups.address().getCity()).isEqualTo("NYC");

        ShipEngineCarrier fedex = carriers.stream()
                .filter(c -> "fedex".equals(c.carrierCode()))
                .findFirst()
                .orElseThrow();
        assertThat(fedex.displayName()).isEqualTo("FedEx");
    }

    @Test
    void fetchCarriers_shouldWrapRestClientResponseException() {
        ReflectionTestUtils.setField(client, "apiKey", "secret-key");

        RestClientResponseException responseException = new RestClientResponseException(
                "error", 502, "Bad Gateway", null, new byte[0], StandardCharsets.UTF_8);
        when(restTemplate.exchange(any(RequestEntity.class), eq(JsonNode.class)))
                .thenThrow(responseException);

        assertThatThrownBy(() -> client.fetchCarriers())
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("ShipEngine API responded with");
    }

    @Test
    void fetchCarriers_shouldWrapRestClientException() {
        ReflectionTestUtils.setField(client, "apiKey", "secret-key");
        when(restTemplate.exchange(any(RequestEntity.class), eq(JsonNode.class)))
                .thenThrow(new RestClientException("network down"));

        assertThatThrownBy(() -> client.fetchCarriers())
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Unable to communicate with ShipEngine API");
    }
}

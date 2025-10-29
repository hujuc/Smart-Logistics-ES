package Operimus.SLMS.business_layer.order_shipment_module.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import Operimus.SLMS.business_layer.order_shipment_module.dto.AddressValidationRequest;
import Operimus.SLMS.business_layer.order_shipment_module.dto.AddressValidationResponse;
import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierAddressView;
import Operimus.SLMS.business_layer.order_shipment_module.dto.ShippingRateRequest;
import Operimus.SLMS.business_layer.order_shipment_module.dto.ShippingRateResponse;
import Operimus.SLMS.business_layer.shared.exception.ExternalServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ShippoApiClientTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ShippoApiClient shippoApiClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(shippoApiClient, "apiKey", "test-key");
        ReflectionTestUtils.setField(shippoApiClient, "baseUrl", "https://api.goshippo.com");
    }

    @Test
    void fetchCarrierAccountsShouldReturnMappedAccounts() {
        JsonNode payload = json("""
                {
                  "results": [
                    {
                      "carrier": "ups",
                      "object_id": "object-1",
                      "account_id": "",
                      "active": true,
                      "parameters": {
                        "account_id": "param-123",
                        "company": "UPS",
                        "email": "support@ups.com",
                        "support_phone": "+351 123 456 789",
                        "services": [
                          {"name": "Express"},
                          "Ground"
                        ],
                        "address": {
                          "street1": "123 Main St",
                          "street_2": "Suite 5",
                          "city": "Lisbon",
                          "province": "Lisboa",
                          "zip": "1000-001",
                          "country": "PT"
                        }
                      }
                    }
                  ]
                }
                """);

        when(restTemplate.exchange(any(RequestEntity.class), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(payload));

        List<ShippoCarrierAccount> result = shippoApiClient.fetchCarrierAccounts();

        assertThat(result).hasSize(1);
        ShippoCarrierAccount account = result.get(0);
        assertThat(account.carrierCode()).isEqualTo("ups");
        assertThat(account.objectId()).isEqualTo("object-1");
        assertThat(account.accountId()).isEqualTo("param-123");
        assertThat(account.displayName()).isEqualTo("UPS");
        assertThat(account.active()).isTrue();
        assertThat(account.supportEmail()).isEqualTo("support@ups.com");
        assertThat(account.supportPhone()).isEqualTo("+351 123 456 789");
        assertThat(account.serviceLevels()).containsExactly("Express", "Ground");
        CarrierAddressView address = account.address();
        assertThat(address).isNotNull();
        assertThat(address.getAddressLine1()).isEqualTo("123 Main St");
        assertThat(address.getAddressLine2()).isEqualTo("Suite 5");
        assertThat(address.getStateOrProvince()).isEqualTo("Lisboa");
        assertThat(address.getPostalCode()).isEqualTo("1000-001");
        assertThat(address.getCountry()).isEqualTo("PT");

        ArgumentCaptor<RequestEntity> captor = ArgumentCaptor.forClass(RequestEntity.class);
        verify(restTemplate).exchange(captor.capture(), eq(JsonNode.class));
        RequestEntity<?> request = captor.getValue();
        assertThat(request.getMethod()).isEqualTo(HttpMethod.GET);
        assertThat(request.getUrl().toString()).isEqualTo("https://api.goshippo.com/carrier_accounts/");
        assertThat(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("ShippoToken test-key");
        assertThat(request.getHeaders().getAccept()).containsExactly(MediaType.APPLICATION_JSON);
    }

    @Test
    void fetchCarrierAccountsShouldReturnEmptyWhenApiKeyMissing() {
        ReflectionTestUtils.setField(shippoApiClient, "apiKey", " ");

        List<ShippoCarrierAccount> result = shippoApiClient.fetchCarrierAccounts();

        assertThat(result).isEmpty();
        verifyNoInteractions(restTemplate);
    }

    @Test
    void validateAddressShouldMapResponseAndMessages() {
        AddressValidationRequest requestPayload = AddressValidationRequest.builder()
                .name("John Doe")
                .company("ACME")
                .addressLine1("Rua A")
                .addressLine2("Apt 2")
                .city("Lisbon")
                .stateOrProvince("Lisboa")
                .postalCode("1000-001")
                .country("PT")
                .residential(Boolean.TRUE)
                .phone("+351000000000")
                .email("john@example.com")
                .build();

        JsonNode responseBody = json("""
                {
                  "street_1": "Rua Validada",
                  "street_2": "Apt 20",
                  "city": "Lisbon",
                  "province": "Lisboa",
                  "postal_code": "1000-001",
                  "country": "PT",
                  "validation_results": {
                    "is_valid": true,
                    "errors": [
                      {"text": "Minor mismatch"}
                    ],
                    "messages": [
                      {"message": "Address normalized"}
                    ]
                  }
                }
                """);

        when(restTemplate.exchange(any(RequestEntity.class), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        AddressValidationResponse response = shippoApiClient.validateAddress(requestPayload);

        assertThat(response.isValid()).isTrue();
        CarrierAddressView normalized = response.getNormalizedAddress();
        assertThat(normalized).isNotNull();
        assertThat(normalized.getAddressLine1()).isEqualTo("Rua Validada");
        assertThat(normalized.getAddressLine2()).isEqualTo("Apt 20");
        assertThat(normalized.getCity()).isEqualTo("Lisbon");
        assertThat(normalized.getStateOrProvince()).isEqualTo("Lisboa");
        assertThat(normalized.getPostalCode()).isEqualTo("1000-001");
        assertThat(normalized.getCountry()).isEqualTo("PT");
        assertThat(response.getMessages()).containsExactly("Minor mismatch", "Address normalized");

        ArgumentCaptor<RequestEntity> captor = ArgumentCaptor.forClass(RequestEntity.class);
        verify(restTemplate).exchange(captor.capture(), eq(JsonNode.class));
        RequestEntity<?> request = captor.getValue();
        assertThat(request.getMethod()).isEqualTo(HttpMethod.POST);
        assertThat(request.getUrl().toString()).isEqualTo("https://api.goshippo.com/addresses");
        assertThat(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("ShippoToken test-key");
        assertThat(request.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(request.getHeaders().getAccept()).containsExactly(MediaType.APPLICATION_JSON);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) request.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("validate")).isEqualTo(Boolean.TRUE);
        assertThat(body.get("name")).isEqualTo("John Doe");
        assertThat(body.get("street1")).isEqualTo("Rua A");
        assertThat(body.get("street2")).isEqualTo("Apt 2");
        assertThat(body.get("zip")).isEqualTo("1000-001");
        assertThat(body.get("is_residential")).isEqualTo(Boolean.TRUE);
    }

    @Test
    void validateAddressShouldReturnFallbackWhenShippoReturnsEmptyBody() {
        when(restTemplate.exchange(any(RequestEntity.class), eq(JsonNode.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        AddressValidationRequest requestPayload = AddressValidationRequest.builder()
                .addressLine1("Rua A")
                .city("Lisbon")
                .postalCode("1000-001")
                .country("PT")
                .build();

        AddressValidationResponse response = shippoApiClient.validateAddress(requestPayload);

        assertThat(response.isValid()).isFalse();
        assertThat(response.getNormalizedAddress()).isNull();
        assertThat(response.getMessages()).containsExactly("Shippo API returned an empty response");
    }

    @Test
    void validateAddressShouldFailWhenApiKeyMissing() {
        ReflectionTestUtils.setField(shippoApiClient, "apiKey", "");

        AddressValidationRequest requestPayload = AddressValidationRequest.builder()
                .addressLine1("Rua A")
                .city("Lisbon")
                .postalCode("1000-001")
                .country("PT")
                .build();

        assertThatExceptionOfType(ExternalServiceException.class)
                .isThrownBy(() -> shippoApiClient.validateAddress(requestPayload))
                .withMessage("Shippo API key is not configured");
        verifyNoInteractions(restTemplate);
    }

    @Test
    void calculateRatesShouldMapRatesAndMessages() {
        ShippingRateRequest requestPayload = ShippingRateRequest.builder()
                .fromAddress(ShippingRateRequest.Address.builder()
                        .name("Sender")
                        .addressLine1("Rua Fonte")
                        .city("Lisbon")
                        .stateOrProvince("Lisboa")
                        .postalCode("1000-001")
                        .country("PT")
                        .phone("+351111111111")
                        .email("sender@example.com")
                        .build())
                .toAddress(ShippingRateRequest.Address.builder()
                        .name("Receiver")
                        .addressLine1("Rua Destino")
                        .city("Porto")
                        .stateOrProvince("Porto")
                        .postalCode("4000-222")
                        .country("PT")
                        .residential(Boolean.TRUE)
                        .build())
                .parcels(List.of(ShippingRateRequest.Parcel.builder()
                        .weight(BigDecimal.valueOf(2.5))
                        .weightUnit("kg")
                        .length(BigDecimal.valueOf(12))
                        .width(BigDecimal.valueOf(8))
                        .height(BigDecimal.valueOf(4))
                        .distanceUnit("cm")
                        .build()))
                .build();

        JsonNode responseBody = json("""
                {
                  "object_id": "shipment-123",
                  "rates": [
                    {
                      "provider": "UPS",
                      "carrier_account": "carrier-acc",
                      "carrier": "ups",
                      "amount": "12.45",
                      "currency": "EUR",
                      "estimated_days": 3,
                      "delivery_date": "2024-05-05T10:15:30Z",
                      "servicelevel": {
                        "name": "Express"
                      }
                    },
                    {
                      "provider": "UPS",
                      "carrier": "ups",
                      "amount": "invalid"
                    }
                  ],
                  "messages": [
                    {"text": "Note 1"},
                    {"message": "Note 2"}
                  ]
                }
                """);

        when(restTemplate.exchange(any(RequestEntity.class), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        ShippingRateResponse response = shippoApiClient.calculateRates(requestPayload);

        assertThat(response.getShipmentId()).isEqualTo("shipment-123");
        assertThat(response.getRates()).hasSize(1);
        ShippingRateResponse.RateQuote quote = response.getRates().get(0);
        assertThat(quote.getProvider()).isEqualTo("UPS");
        assertThat(quote.getCarrierAccount()).isEqualTo("carrier-acc");
        assertThat(quote.getCarrierCode()).isEqualTo("ups");
        assertThat(quote.getServiceLevel()).isEqualTo("Express");
        assertThat(quote.getAmount()).isEqualByComparingTo(new BigDecimal("12.45"));
        assertThat(quote.getCurrency()).isEqualTo("EUR");
        assertThat(quote.getEstimatedDays()).isEqualTo(3);
        assertThat(quote.getDeliveryDate()).isEqualTo(OffsetDateTime.parse("2024-05-05T10:15:30Z"));
        assertThat(response.getMessages()).containsExactly("Note 1", "Note 2");

        ArgumentCaptor<RequestEntity> captor = ArgumentCaptor.forClass(RequestEntity.class);
        verify(restTemplate).exchange(captor.capture(), eq(JsonNode.class));
        RequestEntity<?> request = captor.getValue();
        assertThat(request.getMethod()).isEqualTo(HttpMethod.POST);
        assertThat(request.getUrl().toString()).isEqualTo("https://api.goshippo.com/shipments");
        assertThat(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("ShippoToken test-key");
        assertThat(request.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(request.getHeaders().getAccept()).containsExactly(MediaType.APPLICATION_JSON);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) request.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("async")).isEqualTo(Boolean.FALSE);

        @SuppressWarnings("unchecked")
        Map<String, Object> fromAddress = (Map<String, Object>) body.get("address_from");
        assertThat(fromAddress.get("street1")).isEqualTo("Rua Fonte");
        assertThat(fromAddress.get("city")).isEqualTo("Lisbon");
        assertThat(fromAddress.get("state")).isEqualTo("Lisboa");
        assertThat(fromAddress.get("zip")).isEqualTo("1000-001");

        @SuppressWarnings("unchecked")
        Map<String, Object> toAddress = (Map<String, Object>) body.get("address_to");
        assertThat(toAddress.get("name")).isEqualTo("Receiver");
        assertThat(toAddress.get("is_residential")).isEqualTo(Boolean.TRUE);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> parcels = (List<Map<String, Object>>) body.get("parcels");
        assertThat(parcels).hasSize(1);
        Map<String, Object> parcel = parcels.get(0);
        assertThat(parcel.get("weight")).isEqualTo(BigDecimal.valueOf(2.5));
        assertThat(parcel.get("length")).isEqualTo(BigDecimal.valueOf(12));
        assertThat(parcel.get("distance_unit")).isEqualTo("cm");
    }

    @Test
    void calculateRatesShouldReturnFallbackWhenShippoReturnsEmptyBody() {
        when(restTemplate.exchange(any(RequestEntity.class), eq(JsonNode.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        ShippingRateRequest requestPayload = ShippingRateRequest.builder()
                .fromAddress(ShippingRateRequest.Address.builder()
                        .addressLine1("Rua Fonte")
                        .city("Lisbon")
                        .postalCode("1000-001")
                        .country("PT")
                        .build())
                .toAddress(ShippingRateRequest.Address.builder()
                        .addressLine1("Rua Destino")
                        .city("Porto")
                        .postalCode("4000-222")
                        .country("PT")
                        .build())
                .parcels(List.of(ShippingRateRequest.Parcel.builder()
                        .weight(BigDecimal.ONE)
                        .weightUnit("kg")
                        .build()))
                .build();

        ShippingRateResponse response = shippoApiClient.calculateRates(requestPayload);

        assertThat(response.getShipmentId()).isNull();
        assertThat(response.getRates()).isEmpty();
        assertThat(response.getMessages()).containsExactly("Shippo API returned an empty response");
    }

    @Test
    void calculateRatesShouldFailWhenApiKeyMissing() {
        ReflectionTestUtils.setField(shippoApiClient, "apiKey", "  ");

        ShippingRateRequest requestPayload = ShippingRateRequest.builder()
                .fromAddress(ShippingRateRequest.Address.builder()
                        .addressLine1("Rua Fonte")
                        .city("Lisbon")
                        .postalCode("1000-001")
                        .country("PT")
                        .build())
                .toAddress(ShippingRateRequest.Address.builder()
                        .addressLine1("Rua Destino")
                        .city("Porto")
                        .postalCode("4000-222")
                        .country("PT")
                        .build())
                .parcels(List.of(ShippingRateRequest.Parcel.builder()
                        .weight(BigDecimal.ONE)
                        .weightUnit("kg")
                        .build()))
                .build();

        assertThatExceptionOfType(ExternalServiceException.class)
                .isThrownBy(() -> shippoApiClient.calculateRates(requestPayload))
                .withMessage("Shippo API key is not configured");
        verifyNoInteractions(restTemplate);
    }

    private JsonNode json(String content) {
        try {
            return OBJECT_MAPPER.readTree(content);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to parse JSON payload", ex);
        }
    }
}

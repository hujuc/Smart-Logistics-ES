package Operimus.SLMS.business_layer.rate_carrier_module.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Operimus.SLMS.business_layer.rate_carrier_module.dto.ShippingRateRequest;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.ShippingRateResponse;
import Operimus.SLMS.business_layer.rate_carrier_module.integration.ShippoApiClient;
import Operimus.SLMS.business_layer.shared.exception.ExternalServiceException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShippingRateServiceTest {

    @Mock
    private ShippoApiClient shippoApiClient;

    @InjectMocks
    private ShippingRateService shippingRateService;

    @Test
    void calculateRatesShouldDelegateToShippoApiClient() {
        ShippingRateRequest request = buildRequest();
        ShippingRateResponse response = ShippingRateResponse.builder()
                .shipmentId("shippo-shipment-1")
                .rates(List.of(ShippingRateResponse.RateQuote.builder()
                        .rateId("rate-1")
                        .carrierCode("DHL")
                        .amount(BigDecimal.valueOf(12.5))
                        .currency("EUR")
                        .build()))
                .messages(List.of("ok"))
                .build();

        when(shippoApiClient.calculateRates(request)).thenReturn(response);

        ShippingRateResponse result = shippingRateService.calculateRates(request);

        assertThat(result).isSameAs(response);
        verify(shippoApiClient).calculateRates(request);
    }

    @Test
    void calculateRatesShouldPropagateShippoFailures() {
        ShippingRateRequest request = buildRequest();
        when(shippoApiClient.calculateRates(request)).thenThrow(new ExternalServiceException("Shippo unavailable"));

        assertThatThrownBy(() -> shippingRateService.calculateRates(request))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Shippo unavailable");
    }

    private ShippingRateRequest buildRequest() {
        ShippingRateRequest.Address origin = ShippingRateRequest.Address.builder()
                .name("SLMS Warehouse")
                .company("SLMS")
                .addressLine1("Warehouse Street 1")
                .city("Porto")
                .stateOrProvince("Porto")
                .postalCode("4000-001")
                .country("PT")
                .phone("+351123456789")
                .email("warehouse@example.com")
                .build();

        ShippingRateRequest.Address destination = ShippingRateRequest.Address.builder()
                .name("Customer One")
                .company("Customer One")
                .addressLine1("Rua Cliente 10")
                .city("Lisbon")
                .stateOrProvince("Lisbon")
                .postalCode("1000-001")
                .country("PT")
                .phone("+351987654321")
                .email("customer@example.com")
                .build();

        ShippingRateRequest.Parcel parcel = ShippingRateRequest.Parcel.builder()
                .weight(BigDecimal.valueOf(2.5))
                .weightUnit("kg")
                .length(BigDecimal.valueOf(30))
                .width(BigDecimal.valueOf(20))
                .height(BigDecimal.valueOf(15))
                .distanceUnit("cm")
                .build();

        return ShippingRateRequest.builder()
                .fromAddress(origin)
                .toAddress(destination)
                .parcels(List.of(parcel))
                .build();
    }
}

package Operimus.SLMS.business_layer.rate_carrier_module.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import Operimus.SLMS.business_layer.order_module.dto.ShipmentDocumentResponse;
import Operimus.SLMS.business_layer.order_module.entities.Order;
import Operimus.SLMS.business_layer.order_module.entities.Warehouse;
import Operimus.SLMS.business_layer.order_module.service.ShipmentDocumentService;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.ShippoLabelResponse;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.ShippingRateRequest;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.ShippingRateResponse;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Carrier;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Shipment;
import Operimus.SLMS.business_layer.rate_carrier_module.integration.ShippoApiClient;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShipmentLabelGenerationServiceTest {

    @Mock
    private ShippoApiClient shippoApiClient;

    @Mock
    private ShipmentDocumentService shipmentDocumentService;

    @InjectMocks
    private ShipmentLabelGenerationService shipmentLabelGenerationService;

    private Carrier carrier;
    private Shipment shipment;
    private Warehouse warehouse;
    private Order order;

    @BeforeEach
    void setUp() {
        carrier = Carrier.builder()
                .id(1L)
                .code("DHL")
                .name("DHL Express")
                .shippoCarrierId("carrier-123")
                .shippoAccountId("acc-123")
                .build();

        shipment = Shipment.builder()
                .id(10L)
                .shipmentCode("SHIP-001")
                .carrier(carrier)
                .driverId("driver-1")
                .totalOrders(1)
                .build();

        warehouse = Warehouse.builder()
                .id(2L)
                .code("WH-001")
                .name("North Hub")
                .addressLine1("Rua do Porto 100")
                .city("Porto")
                .stateOrProvince("Porto")
                .country("PT")
                .postalCode("4000-000")
                .build();

        order = Order.builder()
                .id(200L)
                .reference("ORD-200")
                .shippingAddress("Rua Cliente 5, Porto, Porto 4000-000, PT")
                .shippingAddressLine1("Rua Cliente 5")
                .shippingCity("Porto")
                .shippingPostalCode("4000-000")
                .shippingCountry("PT")
                .warehouse(warehouse)
                .customerId("customer-1")
                .weight(3.5)
                .weightUnit("kg")
                .length(30.0)
                .width(20.0)
                .height(10.0)
                .distanceUnit("cm")
                .build();
    }

    @Test
    void generateCarrierDocumentsShouldReturnWhenOrdersMissing() {
        shipmentLabelGenerationService.generateCarrierDocuments(shipment, List.of());

        verifyNoInteractions(shippoApiClient);
        verifyNoInteractions(shipmentDocumentService);
    }

    @Test
    void generateCarrierDocumentsShouldSkipWhenCarrierHasNoShippoAccount() {
        Carrier carrierWithoutShippo = carrier.toBuilder().shippoCarrierId(null).build();
        Shipment shipmentWithoutShippo = shipment.toBuilder().carrier(carrierWithoutShippo).build();

        shipmentLabelGenerationService.generateCarrierDocuments(shipmentWithoutShippo, List.of(order));

        verifyNoInteractions(shippoApiClient);
        verifyNoInteractions(shipmentDocumentService);
    }

    @Test
    void generateCarrierDocumentsShouldPersistLabelsCustomsAndSummary() {
        ShippingRateResponse response = ShippingRateResponse.builder()
                .rates(List.of(
                        rateQuote("rate-other", "UPS", "acc-999", "UPS Provider", BigDecimal.valueOf(25)),
                        rateQuote("rate-match-high", carrier.getCode(), carrier.getShippoAccountId(), carrier.getName(), BigDecimal.valueOf(20)),
                        rateQuote("rate-match-low", carrier.getCode(), carrier.getShippoAccountId(), carrier.getName(), BigDecimal.valueOf(12))
                ))
                .build();
        when(shippoApiClient.calculateRates(any())).thenReturn(response);

        ShippoLabelResponse labelResponse = ShippoLabelResponse.builder()
                .transactionId("txn-123")
                .status("SUCCESS")
                .labelUrl("https://shippo/labels/123.pdf")
                .trackingNumber("TRK-123")
                .serviceLevel("Express")
                .shipmentId("shippo-shipment-1")
                .rateId("rate-match-low")
                .commercialInvoiceUrl("https://shippo/customs/123.pdf")
                .build();
        when(shippoApiClient.purchaseLabel("rate-match-low", "PDF")).thenReturn(labelResponse);

        byte[] labelBytes = "label".getBytes(StandardCharsets.UTF_8);
        byte[] customsBytes = "customs".getBytes(StandardCharsets.UTF_8);
        when(shippoApiClient.downloadLabel("https://shippo/labels/123.pdf")).thenReturn(labelBytes);
        when(shippoApiClient.downloadLabel("https://shippo/customs/123.pdf")).thenReturn(customsBytes);

        when(shipmentDocumentService.saveDocument(
                anyLong(),
                any(),
                anyString(),
                anyString(),
                anyString(),
                any(byte[].class),
                anyMap()))
                .thenAnswer(invocation -> ShipmentDocumentResponse.builder()
                        .shipmentId(invocation.getArgument(0))
                        .orderId(invocation.getArgument(1))
                        .documentId(invocation.getArgument(2))
                        .build());

        shipmentLabelGenerationService.generateCarrierDocuments(shipment, List.of(order));

        ArgumentCaptor<ShippingRateRequest> requestCaptor = ArgumentCaptor.forClass(ShippingRateRequest.class);
        verify(shippoApiClient).calculateRates(requestCaptor.capture());
        ShippingRateRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getFromAddress().getCity()).isEqualTo(warehouse.getCity());
        assertThat(capturedRequest.getToAddress().getPostalCode()).isEqualTo(order.getShippingPostalCode());
        assertThat(capturedRequest.getParcels()).hasSize(1);
        assertThat(capturedRequest.getParcels().get(0).getWeight()).isEqualTo(BigDecimal.valueOf(order.getWeight()));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> labelMetadataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(shipmentDocumentService).saveDocument(
                eq(shipment.getId()),
                eq(order.getId()),
                eq("carrier-label-%d.pdf".formatted(order.getId())),
                eq("Carrier Label - %s.pdf".formatted(order.getReference())),
                eq("application/pdf"),
                eq(labelBytes),
                labelMetadataCaptor.capture());
        assertThat(labelMetadataCaptor.getValue())
                .containsEntry("shippo-rate-id", "rate-match-low")
                .containsEntry("tracking-number", "TRK-123")
                .containsEntry("carrier-code", carrier.getCode());

        verify(shippoApiClient).purchaseLabel("rate-match-low", "PDF");
        verify(shippoApiClient).downloadLabel("https://shippo/labels/123.pdf");
        verify(shippoApiClient).downloadLabel("https://shippo/customs/123.pdf");

        verify(shipmentDocumentService).saveDocument(
                eq(shipment.getId()),
                eq(order.getId()),
                eq("customs-declaration-%d.pdf".formatted(order.getId())),
                eq("Customs-Declaration-%s.pdf".formatted(order.getReference())),
                eq("application/pdf"),
                eq(customsBytes),
                anyMap());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> summaryMetadataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(shipmentDocumentService).saveDocument(
                eq(shipment.getId()),
                isNull(),
                eq("shipment-summary"),
                eq("Shipment-Summary-%s.pdf".formatted(shipment.getShipmentCode())),
                eq("application/pdf"),
                any(byte[].class),
                summaryMetadataCaptor.capture());
        assertThat(summaryMetadataCaptor.getValue())
                .containsEntry("document-type", "SHIPMENT_SUMMARY")
                .containsEntry("carrier-code", carrier.getCode())
                .containsEntry("total-orders", "1");

        assertThat(order.getTrackingNumber()).isEqualTo("TRK-123");
        assertThat(shipment.getShippoShipmentId()).isEqualTo("shippo-shipment-1");
        assertThat(shipment.getShippoTransactionId()).isEqualTo("txn-123");
    }

    @Test
    void generateCarrierDocumentsShouldNotRequestLabelWhenCarrierHasNoMatchingRates() {
        ShippingRateResponse response = ShippingRateResponse.builder()
                .rates(List.of(rateQuote("rate-other", "UPS", "acc-999", "UPS Provider", BigDecimal.valueOf(25))))
                .build();
        when(shippoApiClient.calculateRates(any())).thenReturn(response);

        when(shipmentDocumentService.saveDocument(
                anyLong(),
                any(),
                anyString(),
                anyString(),
                anyString(),
                any(byte[].class),
                anyMap()))
                .thenReturn(ShipmentDocumentResponse.builder().documentId("summary").build());

        shipmentLabelGenerationService.generateCarrierDocuments(shipment, List.of(order));

        verify(shippoApiClient, never()).purchaseLabel(anyString(), anyString());
        verify(shippoApiClient, never()).downloadLabel(anyString());
        verify(shipmentDocumentService).saveDocument(
                eq(shipment.getId()),
                isNull(),
                eq("shipment-summary"),
                eq("Shipment-Summary-%s.pdf".formatted(shipment.getShipmentCode())),
                eq("application/pdf"),
                any(byte[].class),
                anyMap());
        assertThat(order.getTrackingNumber()).isNull();
    }

    private ShippingRateResponse.RateQuote rateQuote(
            String rateId,
            String carrierCode,
            String carrierAccount,
            String provider,
            BigDecimal amount) {
        return ShippingRateResponse.RateQuote.builder()
                .rateId(rateId)
                .carrierCode(carrierCode)
                .carrierAccount(carrierAccount)
                .provider(provider)
                .amount(amount)
                .currency("EUR")
                .build();
    }
}

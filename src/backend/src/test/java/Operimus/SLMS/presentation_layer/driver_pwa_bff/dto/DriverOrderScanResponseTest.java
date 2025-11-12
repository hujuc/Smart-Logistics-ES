package Operimus.SLMS.presentation_layer.driver_pwa_bff.dto;

import Operimus.SLMS.business_layer.order_module.dto.OrderScanResult;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DriverOrderScanResponseTest {

    @Test
    void whenFromOrderScanResult_thenAllFieldsMapped() {
        // Given
        OffsetDateTime confirmedAt = OffsetDateTime.now();
        OrderScanResult scanResult = OrderScanResult.builder()
                .orderId(1L)
                .orderReference("ORD-001")
                .shipmentId(10L)
                .shipmentCode("SHP-001")
                .orderStatus(OrderStatus.IN_TRANSIT)
                .confirmedAt(confirmedAt)
                .alreadyConfirmed(false)
                .allOrdersConfirmed(false)
                .remainingOrdersToScan(4)
                .build();

        // When
        DriverOrderScanResponse response = DriverOrderScanResponse.from(scanResult);

        // Then
        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getOrderReference()).isEqualTo("ORD-001");
        assertThat(response.getShipmentId()).isEqualTo(10L);
        assertThat(response.getShipmentCode()).isEqualTo("SHP-001");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.IN_TRANSIT);
        assertThat(response.getConfirmedAt()).isEqualTo(confirmedAt);
        assertThat(response.isAlreadyConfirmed()).isFalse();
        assertThat(response.isAllOrdersConfirmed()).isFalse();
        assertThat(response.getRemainingOrdersToScan()).isEqualTo(4);
    }

    @Test
    void whenFromOrderScanResult_withAlreadyConfirmed_thenMapped() {
        // Given
        OrderScanResult scanResult = OrderScanResult.builder()
                .orderId(2L)
                .orderReference("ORD-002")
                .shipmentId(20L)
                .shipmentCode("SHP-002")
                .orderStatus(OrderStatus.DELIVERED)
                .confirmedAt(OffsetDateTime.now().minusHours(1))
                .alreadyConfirmed(true)
                .allOrdersConfirmed(false)
                .remainingOrdersToScan(3)
                .build();

        // When
        DriverOrderScanResponse response = DriverOrderScanResponse.from(scanResult);

        // Then
        assertThat(response.getOrderId()).isEqualTo(2L);
        assertThat(response.isAlreadyConfirmed()).isTrue();
        assertThat(response.getRemainingOrdersToScan()).isEqualTo(3);
    }

    @Test
    void whenFromOrderScanResult_withAllOrdersCompleted_thenMapped() {
        // Given
        OrderScanResult scanResult = OrderScanResult.builder()
                .orderId(5L)
                .orderReference("ORD-005")
                .shipmentId(20L)
                .shipmentCode("SHP-002")
                .orderStatus(OrderStatus.DELIVERED)
                .confirmedAt(OffsetDateTime.now())
                .alreadyConfirmed(false)
                .allOrdersConfirmed(true)
                .remainingOrdersToScan(0)
                .build();

        // When
        DriverOrderScanResponse response = DriverOrderScanResponse.from(scanResult);

        // Then
        assertThat(response.getOrderId()).isEqualTo(5L);
        assertThat(response.isAllOrdersConfirmed()).isTrue();
        assertThat(response.getRemainingOrdersToScan()).isZero();
    }

    @Test
    void whenBuilderUsed_thenPropertiesSet() {
        // Given & When
        OffsetDateTime confirmedAt = OffsetDateTime.now();
        DriverOrderScanResponse response = DriverOrderScanResponse.builder()
                .orderId(3L)
                .orderReference("ORD-003")
                .shipmentId(30L)
                .shipmentCode("SHP-003")
                .status(OrderStatus.DELIVERED)
                .confirmedAt(confirmedAt)
                .alreadyConfirmed(true)
                .allOrdersConfirmed(false)
                .remainingOrdersToScan(2)
                .build();

        // Then
        assertThat(response.getOrderId()).isEqualTo(3L);
        assertThat(response.getOrderReference()).isEqualTo("ORD-003");
        assertThat(response.getShipmentId()).isEqualTo(30L);
        assertThat(response.getShipmentCode()).isEqualTo("SHP-003");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(response.getConfirmedAt()).isEqualTo(confirmedAt);
        assertThat(response.isAlreadyConfirmed()).isTrue();
        assertThat(response.isAllOrdersConfirmed()).isFalse();
        assertThat(response.getRemainingOrdersToScan()).isEqualTo(2);
    }
}

package Operimus.SLMS.presentation_layer.driver_pwa_bff.dto;

import Operimus.SLMS.business_layer.order_module.dto.OrderScanResult;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DriverOrderScanResponse {

    private final Long orderId;
    private final String orderReference;
    private final Long shipmentId;
    private final String shipmentCode;
    private final OrderStatus status;
    private final OffsetDateTime confirmedAt;
    private final boolean alreadyConfirmed;
    private final boolean allOrdersConfirmed;
    private final long remainingOrdersToScan;

    public static DriverOrderScanResponse from(OrderScanResult result) {
        return DriverOrderScanResponse.builder()
                .orderId(result.getOrderId())
                .orderReference(result.getOrderReference())
                .shipmentId(result.getShipmentId())
                .shipmentCode(result.getShipmentCode())
                .status(result.getOrderStatus())
                .confirmedAt(result.getConfirmedAt())
                .alreadyConfirmed(result.isAlreadyConfirmed())
                .allOrdersConfirmed(result.isAllOrdersConfirmed())
                .remainingOrdersToScan(result.getRemainingOrdersToScan())
                .build();
    }
}

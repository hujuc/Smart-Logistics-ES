package Operimus.SLMS.business_layer.order_shipment_module.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String reference;
    private String description;
    private ShipmentOrderStatus status;
    private String shippingAddress;
    private LocalDate estimatedDeliveryDate;
    private Long warehouseId;
    private String shippoShipmentId;
    private String shippoTransactionId;
    private String shipstationOrderId;
    private String shipstationCarrierCode;
    private String shipstationServiceCode;
    private String trackingNumber;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

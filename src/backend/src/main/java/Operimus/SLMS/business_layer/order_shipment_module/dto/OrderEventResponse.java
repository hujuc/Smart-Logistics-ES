package Operimus.SLMS.business_layer.order_shipment_module.dto;

import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrderStatus;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderEventResponse {
    Long id;
    Long orderId;
    ShipmentOrderStatus eventType;
    OffsetDateTime occurredAt;
    String notes;
}

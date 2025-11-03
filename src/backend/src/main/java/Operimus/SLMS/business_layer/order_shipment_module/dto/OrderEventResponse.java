package Operimus.SLMS.business_layer.order_shipment_module.dto;

import Operimus.SLMS.business_layer.order_shipment_module.entities.OrderStatus;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderEventResponse {
    Long id;
    Long orderId;
    OrderStatus eventType;
    OffsetDateTime occurredAt;
    String notes;
}

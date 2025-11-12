package Operimus.SLMS.business_layer.order_module.dto;

import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderScanResult {

    private Long orderId;
    private String orderReference;
    private Long shipmentId;
    private String shipmentCode;
    private OrderStatus orderStatus;
    private OffsetDateTime confirmedAt;
    private boolean alreadyConfirmed;
    private boolean allOrdersConfirmed;
    private long remainingOrdersToScan;
}

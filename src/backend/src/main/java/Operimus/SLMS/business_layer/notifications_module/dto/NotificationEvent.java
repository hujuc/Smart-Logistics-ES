package Operimus.SLMS.business_layer.notifications_module.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String eventId;
    private String aggregateId; // Order ID or Shipment ID
    private String aggregateType; // "ORDER" or "SHIPMENT"
    private String eventType; // Status (e.g., CREATED, IN_TRANSIT)
    private String message;
    private String customerId; // For targeting specific users
    private OffsetDateTime occurredAt;
}

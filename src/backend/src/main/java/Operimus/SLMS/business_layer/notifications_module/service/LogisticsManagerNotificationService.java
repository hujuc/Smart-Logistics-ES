package Operimus.SLMS.business_layer.notifications_module.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Service to send notifications to logistics managers about system-wide events
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LogisticsManagerNotificationService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.order-events}")
    private String orderEventsTopic;

    @Value("${kafka.topic.shipment-events}")
    private String shipmentEventsTopic;

    /**
     * Notify logistics managers about a new order created
     */
    public void notifyNewOrderCreated(String orderId, String orderReference, String customerId) {
        log.info("🔔 Notifying logistics managers about new order: {} (ID: {})", orderReference, orderId);
        String message = String.format("New order %s has been created by customer", orderReference);
        publishNotificationToLogisticsManagers("ORDER", orderId, "NEW_ORDER_CREATED", message);
    }

    /**
     * Notify logistics managers about a new shipment created
     */
    public void notifyNewShipmentCreated(String shipmentId, String shipmentCode, int orderCount) {
        String message = String.format("New shipment %s created with %d orders", shipmentCode, orderCount);
        publishNotificationToLogisticsManagers("SHIPMENT", shipmentId, "NEW_SHIPMENT_CREATED", message);
    }

    /**
     * Notify logistics managers about shipment delays
     */
    public void notifyShipmentDelayed(String shipmentId, String shipmentCode, String reason) {
        String message = String.format("Shipment %s is delayed: %s", shipmentCode, reason);
        publishNotificationToLogisticsManagers("SHIPMENT", shipmentId, "SHIPMENT_DELAYED", message);
    }

    /**
     * Notify logistics managers about batch completion
     */
    public void notifyBatchCompleted(String batchId, int shipmentCount) {
        String message = String.format("Batch completed: %d shipments delivered successfully", shipmentCount);
        publishNotificationToLogisticsManagers("BATCH", batchId, "BATCH_COMPLETED", message);
    }

    private void publishNotificationToLogisticsManagers(String aggregateType, String aggregateId, String eventType, String message) {
        try {
            log.info("📤 Publishing notification to logistics managers - Type: {}, Event: {}, Message: {}", 
                aggregateType, eventType, message);
            
            Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent event =
                    Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent.builder()
                            .eventId(UUID.randomUUID().toString())
                            .aggregateId(aggregateId)
                            .aggregateType(aggregateType)
                            .eventType(eventType)
                            .message(message)
                            .customerId("LOGISTICS_MANAGER") // Special marker for all logistics managers
                            .occurredAt(OffsetDateTime.now())
                            .build();

            String payload = objectMapper.writeValueAsString(event);
            String topic = aggregateType.equals("ORDER") ? orderEventsTopic : shipmentEventsTopic;
            kafkaTemplate.send(topic, aggregateId, payload);
            
            log.info("✅ Published notification to logistics managers: {} to topic: {}", eventType, topic);
        } catch (Exception e) {
            log.error("❌ Failed to publish logistics manager notification", e);
        }
    }
}

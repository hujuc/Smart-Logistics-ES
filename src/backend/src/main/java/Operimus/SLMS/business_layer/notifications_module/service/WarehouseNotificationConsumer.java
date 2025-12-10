package Operimus.SLMS.business_layer.notifications_module.service;

import Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Kafka consumer that listens for warehouse-related events and triggers notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseNotificationConsumer {

    private final WarehouseNotificationService warehouseNotificationService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    /**
     * Listen to order events and trigger warehouse notifications
     */
    @KafkaListener(topics = "${kafka.topic.order-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeOrderEvent(String message) {
        try {
            log.debug("Warehouse Notification Consumer received order event: {}", message);
            JsonNode event = objectMapper.readTree(message);
            
            String aggregateType = event.has("aggregateType") ? event.get("aggregateType").asText() : "";
            String eventType = event.has("eventType") ? event.get("eventType").asText() : "";
            
            // If this is a WAREHOUSE event, process it directly as a notification
            if ("WAREHOUSE".equals(aggregateType)) {
                log.info("🏭 Processing WAREHOUSE notification event: {}", eventType);
                NotificationEvent notificationEvent = objectMapper.treeToValue(event, NotificationEvent.class);
                notificationService.processEvent(notificationEvent);
                return;
            }
            
            // Otherwise, trigger warehouse-specific notifications based on ORDER event type
            switch (eventType) {
                case "ORDER_CREATED" -> handleOrderCreated(event);
                case "ORDER_READY_FOR_PICKUP" -> handleOrderReadyForPickup(event);
                case "ORDER_COLLECTED" -> handleOrderCollected(event);
                default -> log.trace("No warehouse notification needed for event: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("Error processing order event for warehouse notifications: {}", e.getMessage(), e);
        }
    }

    /**
     * Listen to shipment events and trigger warehouse notifications
     */
    @KafkaListener(topics = "${kafka.topic.shipment-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeShipmentEvent(String message) {
        try {
            log.debug("Warehouse Notification Consumer received shipment event: {}", message);
            JsonNode event = objectMapper.readTree(message);
            
            String eventType = event.has("eventType") ? event.get("eventType").asText() : "";
            
            // Trigger notifications based on event type
            switch (eventType) {
                case "SHIPMENT_CREATED" -> handleShipmentCreated(event);
                case "BATCH_PREPARED" -> handleBatchPrepared(event);
                case "SHIPMENT_ASSIGNED_TO_DRIVER" -> handleShipmentAssigned(event);
                default -> log.trace("No warehouse notification needed for event: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("Error processing shipment event for warehouse notifications: {}", e.getMessage(), e);
        }
    }

    private void handleOrderCreated(JsonNode event) {
        // When a new order is created, notify warehouse staff about new packages to process
        String orderId = event.has("aggregateId") ? event.get("aggregateId").asText() : "unknown";
        log.info("📦 New order created, notifying warehouse staff to process: {}", orderId);
        
        // Count as a new package received
        warehouseNotificationService.notifyNewPackagesReceived(1);
    }

    private void handleOrderReadyForPickup(JsonNode event) {
        // When order is ready for pickup, it means warehouse prepared it successfully
        String orderId = event.has("aggregateId") ? event.get("aggregateId").asText() : "unknown";
        log.info("✅ Order ready for pickup, notifying warehouse staff: {}", orderId);
        
        // This could trigger a batch prepared notification if multiple orders are ready
        // For now, we'll use the order ID as batch ID
        warehouseNotificationService.notifyShipmentPrepared(orderId, "ORD-" + orderId.substring(0, 8));
    }

    private void handleOrderCollected(JsonNode event) {
        // When order is collected, warehouse staff should know it's been picked up
        String orderId = event.has("aggregateId") ? event.get("aggregateId").asText() : "unknown";
        log.info("🚚 Order collected by carrier: {}", orderId);
        
        // Optionally notify about successful handoff
        // This is informational, so we could create an inventory update notification
    }

    private void handleShipmentCreated(JsonNode event) {
        String shipmentId = event.has("aggregateId") ? event.get("aggregateId").asText() : "unknown";
        log.info("📋 Shipment created: {}", shipmentId);
        
        // Notify warehouse about new shipment to prepare
        int orderCount = event.has("orderCount") ? event.get("orderCount").asInt() : 1;
        warehouseNotificationService.notifyNewPackagesReceived(orderCount);
    }

    private void handleBatchPrepared(JsonNode event) {
        String batchId = event.has("aggregateId") ? event.get("aggregateId").asText() : "unknown";
        String batchCode = event.has("batchCode") ? event.get("batchCode").asText() : batchId;
        log.info("📦 Batch prepared: {}", batchCode);
        
        warehouseNotificationService.notifyShipmentPrepared(batchId, batchCode);
    }

    private void handleShipmentAssigned(JsonNode event) {
        String shipmentId = event.has("aggregateId") ? event.get("aggregateId").asText() : "unknown";
        log.info("👤 Shipment assigned to driver: {}", shipmentId);
        
        // This confirms the warehouse-to-driver handoff is complete
    }
}

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
 * Service to send notifications to warehouse staff about warehouse operations and alerts
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseNotificationService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.order-events}")
    private String orderEventsTopic;

    @Value("${kafka.topic.shipment-events}")
    private String shipmentEventsTopic;

    /**
     * Notify warehouse staff about low stock alert
     */
    public void notifyLowStockAlert(String warehouseId, String warehouseName, String materialName, int currentStock) {
        log.info("🔔 Notifying warehouse staff about low stock: {} at {}", materialName, warehouseName);
        
        String message = String.format("Warehouse %s is running low on %s. Current stock: %d units.", 
            warehouseName, materialName, currentStock);
        
        publishNotificationToWarehouseStaff("WAREHOUSE", warehouseId, "LOW_STOCK_ALERT", message);
    }

    /**
     * Notify warehouse staff about scanner malfunction
     */
    public void notifyScannerMalfunction(String scannerId, String scannerLocation) {
        log.info("🔔 Notifying warehouse staff about scanner malfunction: {} at {}", scannerId, scannerLocation);
        
        String message = String.format("QR Scanner %s is reporting errors. Immediate attention required.", scannerId);
        
        publishNotificationToWarehouseStaff("EQUIPMENT", scannerId, "SCANNER_MALFUNCTION", message);
    }

    /**
     * Notify warehouse staff about shipment prepared successfully
     */
    public void notifyShipmentPrepared(String batchId, String batchCode) {
        log.info("🔔 Notifying warehouse staff about shipment prepared: {}", batchCode);
        
        String message = String.format("Batch %s has been successfully prepared and is ready for pickup.", batchCode);
        
        publishNotificationToWarehouseStaff("BATCH", batchId, "SHIPMENT_PREPARED", message);
    }

    /**
     * Notify warehouse staff about maintenance due
     */
    public void notifyMaintenanceDue(String equipmentId, String equipmentName, int daysSinceLastService) {
        log.info("🔔 Notifying warehouse staff about maintenance due: {}", equipmentName);
        
        String message = String.format("Forklift %s requires scheduled maintenance. Last service: %d days ago", 
            equipmentName, daysSinceLastService);
        
        publishNotificationToWarehouseStaff("EQUIPMENT", equipmentId, "MAINTENANCE_DUE", message);
    }

    /**
     * Notify warehouse staff about new carrier rate available
     */
    public void notifyNewCarrierRate(String carrierId, String carrierName) {
        log.info("🔔 Notifying warehouse staff about new carrier rate: {}", carrierName);
        
        String message = String.format("Updated shipping rates are now available for %s. Review rates in carrier comparison.", 
            carrierName);
        
        publishNotificationToWarehouseStaff("CARRIER", carrierId, "NEW_CARRIER_RATE", message);
    }

    /**
     * Notify warehouse staff about inventory count complete
     */
    public void notifyInventoryCountComplete(String zoneId, String zoneName, int itemsAccounted) {
        log.info("🔔 Notifying warehouse staff about inventory count complete: {}", zoneName);
        
        String message = String.format("Monthly inventory count for %s completed. All items accounted for.", zoneName);
        
        publishNotificationToWarehouseStaff("ZONE", zoneId, "INVENTORY_COUNT_COMPLETE", message);
    }

    /**
     * Notify warehouse staff about package damaged
     */
    public void notifyPackageDamaged(String packageId, String packageCode) {
        log.info("🔔 Notifying warehouse staff about package damaged: {}", packageCode);
        
        String message = String.format("Package %s reported damaged during handling. Incident report...", packageCode);
        
        publishNotificationToWarehouseStaff("PACKAGE", packageId, "PACKAGE_DAMAGED", message);
    }

    /**
     * Notify warehouse staff about new packages received
     */
    public void notifyNewPackagesReceived(int packageCount) {
        log.info("🔔 Notifying warehouse staff about new packages received: {} packages", packageCount);
        
        String message = String.format("%d new packages received and checked into the warehouse system.", packageCount);
        
        publishNotificationToWarehouseStaff("WAREHOUSE", UUID.randomUUID().toString(), "NEW_PACKAGE_RECEIVED", message);
    }

    /**
     * Generic method to publish notification to all warehouse staff
     */
    private void publishNotificationToWarehouseStaff(String aggregateType, String aggregateId, String eventType, String message) {
        try {
            log.info("📤 Publishing notification to warehouse staff - Type: {}, Event: {}, Message: {}", 
                aggregateType, eventType, message);
            
            Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent event =
                    Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent.builder()
                            .eventId(UUID.randomUUID().toString())
                            .aggregateId(aggregateId)
                            .aggregateType(aggregateType)
                            .eventType(eventType)
                            .message(message)
                            .customerId("WAREHOUSE_STAFF") // Special marker for all warehouse staff
                            .occurredAt(OffsetDateTime.now())
                            .build();

            String payload = objectMapper.writeValueAsString(event);
            String topic = aggregateType.equals("WAREHOUSE") || aggregateType.equals("PACKAGE") || aggregateType.equals("ZONE") || aggregateType.equals("EQUIPMENT") || aggregateType.equals("CARRIER") 
                ? orderEventsTopic 
                : shipmentEventsTopic;
            kafkaTemplate.send(topic, aggregateId, payload);
            
            log.info("✅ Published notification to warehouse staff: {} to topic: {}", eventType, topic);
        } catch (Exception e) {
            log.error("❌ Failed to publish warehouse staff notification", e);
        }
    }
}

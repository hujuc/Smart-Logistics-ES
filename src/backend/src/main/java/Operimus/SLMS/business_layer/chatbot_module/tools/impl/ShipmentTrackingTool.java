package Operimus.SLMS.business_layer.chatbot_module.tools.impl;

import Operimus.SLMS.business_layer.chatbot_module.dto.ToolParameter;
import Operimus.SLMS.business_layer.chatbot_module.dto.ToolResult;
import Operimus.SLMS.business_layer.chatbot_module.dto.UserContext;
import Operimus.SLMS.business_layer.chatbot_module.tools.Tool;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Shipment;
import Operimus.SLMS.business_layer.rate_carrier_module.service.ShipmentService;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tool for tracking shipment information.
 * Drivers can only access their assigned shipments.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ShipmentTrackingTool implements Tool {

    private final ShipmentService shipmentService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "shipment_tracking";
    }

    @Override
    public String getDescription() {
        return "Get real-time shipment tracking information including carrier, driver assignment, " +
               "and shipment code. Requires shipment ID. " +
               "Drivers can only view shipments assigned to them.";
    }

    @Override
    public List<ToolParameter> getParameters() {
        return List.of(
            ToolParameter.requiredInteger("shipmentId",
                "The numeric ID of the shipment to track")
        );
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("CUSTOMER", "DRIVER", "CUSTOMER_SERVICE", "LOGISTICS_MANAGER");
    }

    @Override
    public ToolResult execute(Map<String, Object> params, UserContext context) {
        try {
            Long shipmentId = extractLongParam(params, "shipmentId");

            if (shipmentId == null) {
                return ToolResult.error(null, "shipmentId is required");
            }

            // Get shipment
            log.debug("Fetching shipment: {}", shipmentId);
            Shipment shipment = shipmentService.getById(shipmentId);

            // Authorization for DRIVER role
            if ("DRIVER".equals(context.role())) {
                if (!shipment.getDriverId().equals(context.userId())) {
                    log.warn("Driver {} attempted to access shipment {} assigned to driver {}",
                        context.userId(), shipmentId, shipment.getDriverId());
                    return ToolResult.error(null,
                        "You can only view shipments assigned to you");
                }
            }

            // Build result with shipment info
            Map<String, Object> result = new HashMap<>();
            result.put("shipmentId", shipment.getId());
            result.put("shipmentCode", shipment.getShipmentCode());
            result.put("driverId", shipment.getDriverId());
            result.put("carrierId", shipment.getCarrier().getId());

            // Note: Additional info like current location, ETA would go here
            // but the Shipment entity doesn't have these fields yet

            String resultJson = objectMapper.writeValueAsString(result);

            log.info("Successfully retrieved shipment: {}", shipmentId);
            return ToolResult.success(null, resultJson);

        } catch (ResourceNotFoundException e) {
            log.warn("Shipment not found: {}", e.getMessage());
            return ToolResult.error(null, "Shipment not found");

        } catch (Exception e) {
            log.error("Error in ShipmentTrackingTool", e);
            return ToolResult.error(null, "Failed to get shipment info: " + e.getMessage());
        }
    }

    private Long extractLongParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

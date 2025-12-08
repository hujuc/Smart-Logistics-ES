package Operimus.SLMS.business_layer.chatbot_module.tools.impl;

import Operimus.SLMS.business_layer.chatbot_module.dto.ToolParameter;
import Operimus.SLMS.business_layer.chatbot_module.dto.ToolResult;
import Operimus.SLMS.business_layer.chatbot_module.dto.UserContext;
import Operimus.SLMS.business_layer.chatbot_module.tools.Tool;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Shipment;
import Operimus.SLMS.business_layer.rate_carrier_module.service.ShipmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tool for counting shipments in the system.
 * Can return total shipment count or filter by specific driver.
 * Drivers are restricted to viewing only their own shipments.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ShipmentCountTool implements Tool {

    private final ShipmentService shipmentService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "shipment_count";
    }

    @Override
    public String getDescription() {
        return "Get the total number of shipments in the system or count shipments by driver. " +
               "Optionally filter by driverId to see how many shipments a specific driver has. " +
               "Drivers can only view their own shipment counts. " +
               "Returns the total count and driver filter applied.";
    }

    @Override
    public List<ToolParameter> getParameters() {
        return List.of(
            ToolParameter.optionalString("driverId",
                "Optional driver ID to filter shipments (drivers can only see their own)")
        );
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("LOGISTICS_MANAGER", "DRIVER", "CUSTOMER_SERVICE");
    }

    @Override
    public ToolResult execute(Map<String, Object> params, UserContext context) {
        try {
            String driverId = (String) params.get("driverId");

            // Security: Drivers can only see their own shipments
            if ("DRIVER".equals(context.role())) {
                driverId = context.userId();
                log.debug("Driver role detected - overriding driverId filter to: {}", driverId);
            }

            log.debug("Fetching shipment count, driver filter: {}", driverId);

            // Get shipments
            List<Shipment> shipments;
            if (driverId != null && !driverId.isBlank()) {
                shipments = shipmentService.getByDriverId(driverId);
                log.debug("Found {} shipments for driver: {}", shipments.size(), driverId);
            } else {
                shipments = shipmentService.getAll();
                log.debug("Found {} total shipments", shipments.size());
            }

            // Build result
            Map<String, Object> result = new HashMap<>();
            result.put("totalShipments", shipments.size());
            result.put("driverFilter", driverId != null && !driverId.isBlank() ? driverId : "all");

            String resultJson = objectMapper.writeValueAsString(result);

            log.info("Successfully retrieved shipment count: {} (driver: {})",
                shipments.size(), driverId != null ? driverId : "all");
            return ToolResult.success(null, resultJson);

        } catch (Exception e) {
            log.error("Error in ShipmentCountTool", e);
            return ToolResult.error(null, "Failed to get shipment count: " + e.getMessage());
        }
    }
}

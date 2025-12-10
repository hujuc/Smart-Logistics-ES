package Operimus.SLMS.business_layer.chatbot_module.tools.impl;

import Operimus.SLMS.business_layer.chatbot_module.dto.ToolParameter;
import Operimus.SLMS.business_layer.chatbot_module.dto.ToolResult;
import Operimus.SLMS.business_layer.chatbot_module.dto.UserContext;
import Operimus.SLMS.business_layer.chatbot_module.tools.Tool;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.dto.WarehouseResponse;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import Operimus.SLMS.business_layer.order_module.service.WarehouseService;
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
 * Tool for querying warehouse information and capacity.
 * Restricted to warehouse staff and managers.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WarehouseInventoryTool implements Tool {

    private final WarehouseService warehouseService;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "warehouse_inventory";
    }

    @Override
    public String getDescription() {
        return "Query warehouse information including name, location, capacity, and current order count. " +
               "Can retrieve specific warehouse by ID or list all warehouses. " +
               "Omit warehouseId parameter to get all warehouses.";
    }

    @Override
    public List<ToolParameter> getParameters() {
        return List.of(
            ToolParameter.optionalInteger("warehouseId",
                "The numeric ID of the warehouse (optional, omit to list all warehouses)")
        );
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("WAREHOUSE_STAFF", "LOGISTICS_MANAGER");
    }

    @Override
    public ToolResult execute(Map<String, Object> params, UserContext context) {
        try {
            Long warehouseId = extractLongParam(params, "warehouseId");

            if (warehouseId != null) {
                // Single warehouse query
                log.debug("Fetching warehouse: {}", warehouseId);
                WarehouseResponse warehouse = warehouseService.getById(warehouseId);

                // Get order count for this warehouse
                List<OrderResponse> orders = orderService.getByWarehouse(warehouseId);

                Map<String, Object> result = new HashMap<>();
                result.put("warehouse", warehouse);
                result.put("activeOrderCount", orders.size());

                String resultJson = objectMapper.writeValueAsString(result);

                log.info("Successfully retrieved warehouse: {}", warehouseId);
                return ToolResult.success(null, resultJson);

            } else {
                // All warehouses
                log.debug("Fetching all warehouses");
                List<WarehouseResponse> warehouses = warehouseService.getAll();

                String resultJson = objectMapper.writeValueAsString(warehouses);

                log.info("Successfully retrieved {} warehouses", warehouses.size());
                return ToolResult.success(null, resultJson);
            }

        } catch (ResourceNotFoundException e) {
            log.warn("Warehouse not found: {}", e.getMessage());
            return ToolResult.error(null, "Warehouse not found");

        } catch (Exception e) {
            log.error("Error in WarehouseInventoryTool", e);
            return ToolResult.error(null, "Failed to get warehouse info: " + e.getMessage());
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

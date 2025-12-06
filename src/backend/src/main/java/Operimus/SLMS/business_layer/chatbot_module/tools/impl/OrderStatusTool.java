package Operimus.SLMS.business_layer.chatbot_module.tools.impl;

import Operimus.SLMS.business_layer.chatbot_module.dto.ToolParameter;
import Operimus.SLMS.business_layer.chatbot_module.dto.ToolResult;
import Operimus.SLMS.business_layer.chatbot_module.dto.UserContext;
import Operimus.SLMS.business_layer.chatbot_module.tools.Tool;
import Operimus.SLMS.business_layer.order_module.dto.OrderEventResponse;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.service.OrderEventService;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
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
 * Tool for checking order status and event history.
 * Returns current status and recent status change events.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderStatusTool implements Tool {

    private final OrderService orderService;
    private final OrderEventService orderEventService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "order_status";
    }

    @Override
    public String getDescription() {
        return "Check the current status and recent event history of an order. " +
               "Returns current status and last 5 status change events with timestamps.";
    }

    @Override
    public List<ToolParameter> getParameters() {
        return List.of(
            ToolParameter.requiredInteger("orderId",
                "The numeric ID of the order to check status for")
        );
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("CUSTOMER", "DRIVER", "WAREHOUSE_STAFF", "CUSTOMER_SERVICE", "LOGISTICS_MANAGER");
    }

    @Override
    public ToolResult execute(Map<String, Object> params, UserContext context) {
        try {
            Long orderId = extractLongParam(params, "orderId");

            if (orderId == null) {
                return ToolResult.error(null, "orderId is required");
            }

            // Get order (for auth check and current status)
            log.debug("Fetching order {} for status check", orderId);
            OrderResponse order = orderService.getById(orderId);

            // Authorization for CUSTOMER
            if ("CUSTOMER".equals(context.role())) {
                if (!order.getCustomerId().equals(context.userId())) {
                    log.warn("Customer {} attempted to check status of order {} belonging to {}",
                        context.userId(), orderId, order.getCustomerId());
                    return ToolResult.error(null, "You can only view your own orders");
                }
            }

            // Get events
            log.debug("Fetching events for order {}", orderId);
            List<OrderEventResponse> events = orderEventService.getEventsForOrder(orderId);

            // Build result
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", orderId);
            result.put("orderReference", order.getReference());
            result.put("currentStatus", order.getStatus());
            result.put("recentEvents", events.stream().limit(5).toList());
            result.put("totalEvents", events.size());

            String resultJson = objectMapper.writeValueAsString(result);

            log.info("Successfully retrieved status for order: {}", orderId);
            return ToolResult.success(null, resultJson);

        } catch (ResourceNotFoundException e) {
            log.warn("Order not found: {}", e.getMessage());
            return ToolResult.error(null, "Order not found");

        } catch (Exception e) {
            log.error("Error in OrderStatusTool", e);
            return ToolResult.error(null, "Failed to get order status: " + e.getMessage());
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

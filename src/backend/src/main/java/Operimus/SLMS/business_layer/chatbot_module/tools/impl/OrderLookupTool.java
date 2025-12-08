package Operimus.SLMS.business_layer.chatbot_module.tools.impl;

import Operimus.SLMS.business_layer.chatbot_module.dto.ToolParameter;
import Operimus.SLMS.business_layer.chatbot_module.dto.ToolResult;
import Operimus.SLMS.business_layer.chatbot_module.dto.UserContext;
import Operimus.SLMS.business_layer.chatbot_module.tools.Tool;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tool for looking up order details by ID or tracking number.
 * Customers can only access their own orders.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderLookupTool implements Tool {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "order_lookup";
    }

    @Override
    public String getDescription() {
        return "Look up detailed information about an order by ID or tracking number. " +
               "Returns order details including status, customer info, addresses, and items. " +
               "Provide either orderId (number) or trackingNumber (string), not both.";
    }

    @Override
    public List<ToolParameter> getParameters() {
        return List.of(
            ToolParameter.optionalInteger("orderId",
                "The numeric ID of the order"),
            ToolParameter.optionalString("trackingNumber",
                "The tracking number of the order (e.g., TRK-ABC123)")
        );
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("CUSTOMER", "DRIVER", "WAREHOUSE_STAFF", "CUSTOMER_SERVICE", "LOGISTICS_MANAGER");
    }

    @Override
    public ToolResult execute(Map<String, Object> params, UserContext context) {
        try {
            // Extract parameters
            Long orderId = extractLongParam(params, "orderId");
            String trackingNumber = (String) params.get("trackingNumber");

            // Validate at least one parameter provided
            if (orderId == null && (trackingNumber == null || trackingNumber.isBlank())) {
                return ToolResult.error(null,
                    "Must provide either orderId or trackingNumber");
            }

            // Fetch order
            OrderResponse order;
            if (orderId != null) {
                log.debug("Looking up order by ID: {}", orderId);
                order = orderService.getById(orderId);
            } else {
                log.debug("Looking up order by tracking number: {}", trackingNumber);
                order = orderService.getByTrackingNumber(trackingNumber);
            }

            // Authorization check for CUSTOMER role
            if ("CUSTOMER".equals(context.role())) {
                if (!order.getCustomerId().equals(context.userId())) {
                    log.warn("Customer {} attempted to access order {} belonging to {}",
                        context.userId(), order.getId(), order.getCustomerId());
                    return ToolResult.error(null,
                        "You can only view your own orders");
                }
            }

            // Convert to JSON for LLM
            String resultJson = objectMapper.writeValueAsString(order);

            log.info("Successfully retrieved order: {}", order.getId());
            return ToolResult.success(null, resultJson);

        } catch (ResourceNotFoundException e) {
            log.warn("Order not found: {}", e.getMessage());
            return ToolResult.error(null, "Order not found");

        } catch (Exception e) {
            log.error("Error in OrderLookupTool", e);
            return ToolResult.error(null, "Failed to lookup order: " + e.getMessage());
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

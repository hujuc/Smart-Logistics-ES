package Operimus.SLMS.business_layer.chatbot_module.tools.impl;

import Operimus.SLMS.business_layer.chatbot_module.dto.ToolParameter;
import Operimus.SLMS.business_layer.chatbot_module.dto.ToolResult;
import Operimus.SLMS.business_layer.chatbot_module.dto.UserContext;
import Operimus.SLMS.business_layer.chatbot_module.tools.Tool;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import Operimus.SLMS.business_layer.visibility_module.exceptions.dto.DeliveryExceptionResponse;
import Operimus.SLMS.business_layer.visibility_module.exceptions.service.DeliveryExceptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tool for listing delivery exceptions for an order.
 * Returns exception details including severity, status, and resolution steps.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DeliveryExceptionTool implements Tool {

    private final DeliveryExceptionService exceptionService;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "delivery_exception";
    }

    @Override
    public String getDescription() {
        return "List active delivery exceptions and issues for a specific order. " +
               "Returns exception details including type, severity, status, priority, " +
               "and resolution steps. Customers can only view exceptions for their own orders.";
    }

    @Override
    public List<ToolParameter> getParameters() {
        return List.of(
            ToolParameter.requiredInteger("orderId",
                "The numeric ID of the order to check for delivery exceptions")
        );
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("CUSTOMER", "CUSTOMER_SERVICE", "LOGISTICS_MANAGER");
    }

    @Override
    public ToolResult execute(Map<String, Object> params, UserContext context) {
        try {
            Long orderId = extractLongParam(params, "orderId");

            if (orderId == null) {
                return ToolResult.error(null, "orderId is required");
            }

            // Get order (for auth check)
            log.debug("Fetching order {} for exception check", orderId);
            OrderResponse order = orderService.getById(orderId);

            // Authorization for CUSTOMER
            if ("CUSTOMER".equals(context.role())) {
                if (!order.getCustomerId().equals(context.userId())) {
                    log.warn("Customer {} attempted to view exceptions for order {} belonging to {}",
                        context.userId(), orderId, order.getCustomerId());
                    return ToolResult.error(null,
                        "You can only view exceptions for your own orders");
                }
            }

            // Get exceptions for the order
            log.debug("Fetching delivery exceptions for order {}", orderId);
            List<DeliveryExceptionResponse> exceptions = exceptionService.getByOrderId(orderId);

            // Build result
            String resultJson = objectMapper.writeValueAsString(Map.of(
                "orderId", orderId,
                "orderReference", order.getReference(),
                "exceptionsCount", exceptions.size(),
                "exceptions", exceptions
            ));

            log.info("Successfully retrieved {} exceptions for order: {}", exceptions.size(), orderId);
            return ToolResult.success(null, resultJson);

        } catch (ResourceNotFoundException e) {
            log.warn("Order not found: {}", e.getMessage());
            return ToolResult.error(null, "Order not found");

        } catch (Exception e) {
            log.error("Error in DeliveryExceptionTool", e);
            return ToolResult.error(null, "Failed to get exceptions: " + e.getMessage());
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

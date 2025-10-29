package Operimus.SLMS.presentation_layer.customer_portal_bff;

import Operimus.SLMS.business_layer.order_shipment_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_shipment_module.dto.OrderEventResponse;
import Operimus.SLMS.business_layer.order_shipment_module.service.OrderService;
import Operimus.SLMS.business_layer.order_shipment_module.service.ShipmentOrderEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer BFF Controller
 *
 * This controller is part of the Backend-For-Frontend (BFF) pattern for the customer portal.
 * It exposes customer-specific endpoints that delegate to business layer services.
 *
 * Endpoints are mapped to /api/customer/* to match the customer role permissions.
 */
@Slf4j
@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost", "http://localhost:3000"}, allowCredentials = "true")
public class CustomerBffController {

    private final OrderService orderService;
    private final ShipmentOrderEventService shipmentOrderEventService;

    /**
     * Health check endpoint
     *
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<java.util.Map<String, String>> health() {
        return ResponseEntity.ok(java.util.Map.of(
                "status", "UP",
                "service", "Customer BFF",
                "timestamp", java.time.Instant.now().toString()
        ));
    }

    /**
     * Get all orders for the customer
     * GET /api/customer/orders
     *
     * @param jwt JWT token with customer information
     * @return List of orders for the authenticated customer
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getCustomerOrders(@AuthenticationPrincipal Jwt jwt) {
        try {
            log.info("Customer BFF: JWT object is: {}", jwt != null ? "present" : "NULL");
            if (jwt != null) {
                log.info("Customer BFF: JWT subject: {}", jwt.getSubject());
                log.info("Customer BFF: JWT claims: {}", jwt.getClaims());
            }
            
            String customerId = jwt != null ? jwt.getSubject() : null;
            log.info("Customer BFF: Fetching orders for customer {}", customerId);
            
            if (customerId == null) {
                log.error("Customer BFF: customerId is null - JWT was null or had no subject");
                return ResponseEntity.ok(java.util.Collections.emptyList());
            }
            
            List<OrderResponse> orders = orderService.getByCustomerId(customerId);
            log.info("Customer BFF: Successfully fetched {} orders for customer {}", orders.size(), customerId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Failed to fetch customer orders: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get order by ID
     * GET /api/customer/orders/{id}
     *
     * @param id Order ID
     * @param jwt JWT token with customer information
     * @return Order details (only if it belongs to the authenticated customer)
     */
    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        try {
            String customerId = jwt.getSubject();
            log.info("Customer BFF: Fetching order {} for customer {}", id, customerId);
            
            OrderResponse order = orderService.getById(id);
            
            // Security check: verify that the order belongs to this customer
            if (order.getCustomerId() == null || !customerId.equals(order.getCustomerId())) {
                log.warn("Customer {} attempted to access order {} (order customerId: {})", 
                        customerId, id, order.getCustomerId());
                throw new RuntimeException("Access denied: Order does not belong to this customer");
            }
            
            log.info("Customer BFF: Successfully fetched order {}", id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Failed to fetch order {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Get order events
     * GET /api/customer/orders/{id}/events
     *
     * @param id Order ID
     * @param jwt JWT token with customer information
     * @return List of order events (only if the order belongs to the authenticated customer)
     */
    @GetMapping("/orders/{id}/events")
    public ResponseEntity<List<OrderEventResponse>> getOrderEvents(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        try {
            String customerId = jwt.getSubject();
            log.info("Customer BFF: Fetching events for order {} for customer {}", id, customerId);
            
            // First verify the order belongs to this customer
            OrderResponse order = orderService.getById(id);
            if (order.getCustomerId() == null || !customerId.equals(order.getCustomerId())) {
                log.warn("Customer {} attempted to access events for order {} (order customerId: {})", 
                        customerId, id, order.getCustomerId());
                throw new RuntimeException("Access denied: Order does not belong to this customer");
            }
            
            List<OrderEventResponse> events = shipmentOrderEventService.getEventsForOrder(id);
            log.info("Customer BFF: Successfully fetched {} events for order {}", events.size(), id);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Failed to fetch events for order {}: {}", id, e.getMessage());
            throw e;
        }
    }
}

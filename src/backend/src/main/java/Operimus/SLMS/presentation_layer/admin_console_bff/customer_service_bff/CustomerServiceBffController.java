package Operimus.SLMS.presentation_layer.admin_console_bff.customer_service_bff;

import Operimus.SLMS.business_layer.visibility_module.exceptions.dto.*;
import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.*;
import Operimus.SLMS.business_layer.visibility_module.exceptions.service.DeliveryExceptionService;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.dto.OrderEventResponse;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import Operimus.SLMS.business_layer.order_module.service.OrderEventService;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Customer Service BFF Controller
 *
 * This controller is part of the Backend-For-Frontend (BFF) pattern for the
 * customer service interface.
 * It exposes customer-service-specific endpoints that delegate to business
 * layer services.
 *
 * Endpoints are mapped to /api/customer-service/* to match the customer service
 * rep role permissions.
 */
@Slf4j
@RestController
@RequestMapping("/api/customer-service")
@RequiredArgsConstructor
public class CustomerServiceBffController {

    private final DeliveryExceptionService exceptionService;
    private final OrderService orderService;
    private final OrderEventService shipmentOrderEventService;
    private final KeycloakUserService userService;

    /**
     * Health check endpoint
     *
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Customer Service BFF",
                "timestamp", java.time.Instant.now().toString()));
    }

    // ========================================
    // Exception Management Endpoints
    // ========================================

    /**
     * Get all delivery exceptions with optional filters
     * GET
     * /api/customer-service/delivery-exceptions?status=OPEN&priority=HIGH&assignedTo={uuid}&severity=CRITICAL
     */
    @GetMapping("/delivery-exceptions")
    public ResponseEntity<List<DeliveryExceptionResponse>> getExceptions(
            @RequestParam(required = false) ExceptionStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) UUID assignedTo,
            @RequestParam(required = false) Severity severity) {
        try {
            log.info("Customer Service BFF: Fetching exceptions with filters");
            List<DeliveryExceptionResponse> exceptions = exceptionService.getAll(
                    status, priority, assignedTo, severity);
            return ResponseEntity.ok(exceptions);
        } catch (Exception e) {
            log.error("Failed to fetch exceptions: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get exception by ID
     * GET /api/customer-service/delivery-exceptions/{id}
     */
    @GetMapping("/delivery-exceptions/{id}")
    public ResponseEntity<DeliveryExceptionResponse> getException(@PathVariable UUID id) {
        try {
            log.info("Customer Service BFF: Fetching exception {}", id);
            DeliveryExceptionResponse exception = exceptionService.getById(id);
            return ResponseEntity.ok(exception);
        } catch (Exception e) {
            log.error("Failed to fetch exception {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Create a new delivery exception
     * POST /api/customer-service/delivery-exceptions
     */
    @PostMapping("/delivery-exceptions")
    public ResponseEntity<DeliveryExceptionResponse> createException(
            @Valid @RequestBody DeliveryExceptionRequest request) {
        try {
            log.info("Customer Service BFF: Creating new exception for order {}", request.getOrderId());
            DeliveryExceptionResponse response = exceptionService.create(request);
            return ResponseEntity
                    .created(URI.create("/api/customer-service/delivery-exceptions/" + response.getId()))
                    .body(response);
        } catch (Exception e) {
            log.error("Failed to create exception: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Update an existing exception
     * PATCH /api/customer-service/delivery-exceptions/{id}
     */
    @PatchMapping("/delivery-exceptions/{id}")
    public ResponseEntity<DeliveryExceptionResponse> updateException(
            @PathVariable UUID id,
            @Valid @RequestBody DeliveryExceptionUpdateRequest request) {
        try {
            log.info("Customer Service BFF: Updating exception {}", id);
            DeliveryExceptionResponse response = exceptionService.update(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to update exception {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Close an exception
     * POST /api/customer-service/delivery-exceptions/{id}/close
     */
    @PostMapping("/delivery-exceptions/{id}/close")
    public ResponseEntity<DeliveryExceptionResponse> closeException(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            log.info("Customer Service BFF: Closing exception {}", id);
            String resolutionNotes = body != null ? body.get("resolutionNotes") : null;
            DeliveryExceptionResponse response = exceptionService.close(id, resolutionNotes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to close exception {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Contact carrier (log action)
     * POST /api/customer-service/delivery-exceptions/{id}/contact-carrier
     */
    @PostMapping("/delivery-exceptions/{id}/contact-carrier")
    public ResponseEntity<Map<String, String>> contactCarrier(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        try {
            log.info("Customer Service BFF: Logging carrier contact for exception {}", id);

            // Verify exception exists
            exceptionService.getById(id);

            // TODO: Implement notification logging when notification service is available
            String message = body.get("message");
            log.info("Carrier contact logged for exception {}: {}", id, message);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Carrier contact logged successfully"));
        } catch (Exception e) {
            log.error("Failed to log carrier contact for exception {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Contact customer (send notification)
     * POST /api/customer-service/delivery-exceptions/{id}/contact-customer
     */
    @PostMapping("/delivery-exceptions/{id}/contact-customer")
    public ResponseEntity<Map<String, String>> contactCustomer(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        try {
            log.info("Customer Service BFF: Sending customer notification for exception {}", id);

            // Verify exception exists
            DeliveryExceptionResponse exception = exceptionService.getById(id);

            // TODO: Implement notification service integration
            String message = body.get("message");
            log.info("Customer notification sent for exception {} to customer {}: {}",
                    id, exception.getCustomerId(), message);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Customer notification sent successfully"));
        } catch (Exception e) {
            log.error("Failed to send customer notification for exception {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Reschedule delivery
     * POST /api/customer-service/delivery-exceptions/{id}/reschedule-delivery
     */
    @PostMapping("/delivery-exceptions/{id}/reschedule-delivery")
    public ResponseEntity<Map<String, String>> rescheduleDelivery(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        try {
            log.info("Customer Service BFF: Rescheduling delivery for exception {}", id);

            // Verify exception exists
            DeliveryExceptionResponse exception = exceptionService.getById(id);

            // TODO: Implement delivery manifest rescheduling when available
            String newDate = body.get("newDate");
            log.info("Delivery rescheduled for exception {} (order {}): new date {}",
                    id, exception.getOrderId(), newDate);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Delivery rescheduled successfully",
                    "newDate", newDate));
        } catch (Exception e) {
            log.error("Failed to reschedule delivery for exception {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Request return
     * POST /api/customer-service/delivery-exceptions/{id}/request-return
     */
    @PostMapping("/delivery-exceptions/{id}/request-return")
    public ResponseEntity<Map<String, String>> requestReturn(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        try {
            log.info("Customer Service BFF: Initiating return for exception {}", id);

            // Verify exception exists
            DeliveryExceptionResponse exception = exceptionService.getById(id);

            // TODO: Implement return workflow when available
            String reason = body.get("reason");
            log.info("Return initiated for exception {} (order {}): {}",
                    id, exception.getOrderId(), reason);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Return request initiated successfully"));
        } catch (Exception e) {
            log.error("Failed to request return for exception {}: {}", id, e.getMessage());
            throw e;
        }
    }

    // ========================================
    // Dashboard & KPI Endpoints
    // ========================================

    /**
     * Get dashboard summary
     * GET /api/customer-service/dashboard/exceptions/summary
     */
    @GetMapping("/dashboard/exceptions/summary")
    public ResponseEntity<ExceptionSummaryResponse> getDashboardSummary() {
        try {
            log.info("Customer Service BFF: Fetching dashboard summary");
            ExceptionSummaryResponse summary = exceptionService.getDashboardSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Failed to fetch dashboard summary: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get alerts (high priority and overdue cases)
     * GET /api/customer-service/dashboard/alerts
     */
    @GetMapping("/dashboard/alerts")
    public ResponseEntity<List<DeliveryExceptionResponse>> getAlerts() {
        try {
            log.info("Customer Service BFF: Fetching alerts");
            List<DeliveryExceptionResponse> alerts = exceptionService.getAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("Failed to fetch alerts: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get cases assigned to the logged-in CSR
     * GET /api/customer-service/dashboard/my-cases?csrId={uuid}
     */
    @GetMapping("/dashboard/my-cases")
    public ResponseEntity<List<DeliveryExceptionResponse>> getMyCases(
            @RequestParam UUID csrId) {
        try {
            log.info("Customer Service BFF: Fetching cases for CSR {}", csrId);
            List<DeliveryExceptionResponse> cases = exceptionService.getMyCases(csrId);
            return ResponseEntity.ok(cases);
        } catch (Exception e) {
            log.error("Failed to fetch cases for CSR {}: {}", csrId, e.getMessage());
            throw e;
        }
    }

    // ========================================
    // Order Search & Information Endpoints
    // ========================================

    /**
     * Search orders
     * GET /api/customer-service/orders/search?query=<term>
     */
    @GetMapping("/orders/search")
    public ResponseEntity<List<OrderResponse>> searchOrders(
            @RequestParam String query) {
        try {
            log.info("Customer Service BFF: Searching orders with query: {}", query);

            // TODO: Implement proper search in OrderService
            // For now, get all orders and filter client-side
            List<OrderResponse> allOrders = orderService.getAll();

            List<OrderResponse> filtered = allOrders.stream()
                    .filter(order -> order.getReference().toLowerCase().contains(query.toLowerCase()) ||
                            order.getTrackingNumber() != null &&
                                    order.getTrackingNumber().toLowerCase().contains(query.toLowerCase()))
                    .toList();

            return ResponseEntity.ok(filtered);
        } catch (Exception e) {
            log.error("Failed to search orders: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get order by ID
     * GET /api/customer-service/orders/{id}
     */
    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        try {
            log.info("Customer Service BFF: Fetching order {}", id);
            OrderResponse order = orderService.getById(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Failed to fetch order {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Get exceptions for a specific order
     * GET /api/customer-service/orders/{id}/exceptions
     */
    @GetMapping("/orders/{id}/exceptions")
    public ResponseEntity<List<DeliveryExceptionResponse>> getOrderExceptions(
            @PathVariable Long id) {
        try {
            log.info("Customer Service BFF: Fetching exceptions for order {}", id);
            List<DeliveryExceptionResponse> exceptions = exceptionService.getByOrderId(id);
            return ResponseEntity.ok(exceptions);
        } catch (Exception e) {
            log.error("Failed to fetch exceptions for order {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Get tracking information for an order
     * GET /api/customer-service/orders/{id}/tracking
     */
    @GetMapping("/orders/{id}/tracking")
    public ResponseEntity<Map<String, Object>> getOrderTracking(@PathVariable Long id) {
        try {
            log.info("Customer Service BFF: Fetching tracking for order {}", id);

            OrderResponse order = orderService.getById(id);

            // TODO: Implement proper tracking events when tracking service is available
            return ResponseEntity.ok(Map.of(
                    "orderId", order.getId(),
                    "trackingNumber", order.getTrackingNumber() != null ? order.getTrackingNumber() : "",
                    "status", order.getStatus().toString(),
                    "estimatedDelivery",
                    order.getEstimatedDeliveryDate() != null ? order.getEstimatedDeliveryDate().toString() : "",
                    "events", List.of() // TODO: Add tracking events
            ));
        } catch (Exception e) {
            log.error("Failed to fetch tracking for order {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Get order with events in a single optimized call (Version B - Feature Flag)
     * GET /api/customer-service/orders/{id}/with-events
     * 
     * This endpoint returns order details along with all events in a single
     * response,
     * reducing the number of API calls needed and improving performance.
     * Used by Version B of the order lookup interface for A/B testing.
     */
    @GetMapping("/orders/{id}/with-events")
    public ResponseEntity<Map<String, Object>> getOrderWithEvents(@PathVariable Long id) {
        try {
            log.info("Customer Service BFF (Version B): Fetching order {} with events", id);

            // Fetch order
            OrderResponse order = orderService.getById(id);

            // Fetch events
            List<OrderEventResponse> events = shipmentOrderEventService.getEventsForOrder(id);

            // Return combined response
            Map<String, Object> response = Map.of(
                    "order", order,
                    "events", events,
                    "version", "B",
                    "optimized", true);

            log.info("Customer Service BFF (Version B): Successfully fetched order {} with {} events",
                    id, events.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch order {} with events: {}", id, e.getMessage());
            throw e;
        }
    }

    // ========================================
    // User Management Endpoints
    // ========================================

    /**
     * Get all customer service representatives
     * GET /api/customer-service/users/customer-service-reps
     */
    @GetMapping("/users/customer-service-reps")
    public ResponseEntity<List<UserRepresentation>> getCustomerServiceReps() {
        try {
            log.info("Customer Service BFF: Fetching all customer service representatives");

            // Try both possible role names
            List<UserRepresentation> csrUsers = userService.getUsersByRole("CUSTOMER_SERVICE_REP");

            // If nothing found with CUSTOMER_SERVICE_REP, try alternative name
            if (csrUsers.isEmpty()) {
                log.info("No users found with role CUSTOMER_SERVICE_REP, trying customer_service_representative");
                csrUsers = userService.getUsersByRole("customer_service_representative");
            }

            log.info("Found {} customer service representatives", csrUsers.size());
            return ResponseEntity.ok(csrUsers);
        } catch (Exception e) {
            log.error("Failed to fetch customer service representatives: {}", e.getMessage(), e);
            throw e;
        }
    }
}

package Operimus.SLMS.presentation_layer.customer_portal_bff;

import Operimus.SLMS.business_layer.order_module.dto.DocumentDownloadResponse;
import Operimus.SLMS.business_layer.order_module.dto.DocumentUploadResponse;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.dto.OrderEventResponse;
import Operimus.SLMS.business_layer.order_module.dto.OrderRequest;
import Operimus.SLMS.business_layer.order_module.dto.WarehouseResponse;
import Operimus.SLMS.business_layer.order_module.service.OrderDocumentService;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import Operimus.SLMS.business_layer.order_module.service.OrderEventService;
import Operimus.SLMS.business_layer.order_module.service.WarehouseService;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;
import Operimus.SLMS.business_layer.visibility_module.exceptions.dto.DeliveryExceptionRequest;
import Operimus.SLMS.business_layer.visibility_module.exceptions.dto.DeliveryExceptionResponse;
import Operimus.SLMS.business_layer.visibility_module.exceptions.service.DeliveryExceptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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
    private final OrderEventService orderEventService;
    private final WarehouseService warehouseService;
    private final DeliveryExceptionService exceptionService;
    private final OrderDocumentService orderDocumentService;
    private final KeycloakUserService keycloakUserService;

    /**
     * Extract customer ID from JWT token
     * Tries 'sub' claim first, falls back to 'preferred_username'
     */
    private String extractCustomerId(Jwt jwt) {
        String customerId = jwt.getSubject();
        if (customerId == null) {
            log.warn("JWT sub claim is null. Available claims: {}", jwt.getClaims().keySet());
            customerId = jwt.getClaimAsString("preferred_username");
            log.info("Using preferred_username as customerId: {}", customerId);
        }
        return customerId;
    }

    /**
     * Get customer UUID from username (from JWT)
     * Converts username to UUID by looking up the user in Keycloak
     */
    private java.util.UUID getCustomerUUID(String username) {
        try {
            // Search for user by username
            List<UserRepresentation> users = keycloakUserService.searchUsers(username);
            
            if (users.isEmpty()) {
                log.error("No user found with username: {}", username);
                throw new RuntimeException("User not found: " + username);
            }
            
            // Find exact match (searchUsers can return partial matches)
            UserRepresentation user = users.stream()
                    .filter(u -> username.equals(u.getUsername()))
                    .findFirst()
                    .orElse(users.get(0)); // Fallback to first result
            
            return java.util.UUID.fromString(user.getId());
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID for username {}: {}", username, e.getMessage());
            throw new RuntimeException("Invalid user ID format for username: " + username, e);
        } catch (Exception e) {
            log.error("Failed to get UUID for username {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to resolve user ID: " + e.getMessage(), e);
        }
    }

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
     * Get all active warehouses
     * GET /api/customer/warehouses
     *
     * @return List of active warehouses
     */
    @GetMapping("/warehouses")
    public ResponseEntity<List<WarehouseResponse>> getWarehouses() {
        try {
            log.info("Customer BFF: Fetching all warehouses");
            List<WarehouseResponse> warehouses = warehouseService.getAll();
            log.info("Customer BFF: Successfully fetched {} warehouses", warehouses.size());
            return ResponseEntity.ok(warehouses);
        } catch (Exception e) {
            log.error("Failed to fetch warehouses: {}", e.getMessage(), e);
            throw e;
        }
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
            String customerId = extractCustomerId(jwt);
            log.info("Customer BFF: Fetching orders for customer {}", customerId);

            List<OrderResponse> orders = orderService.getByCustomerId(customerId);
            log.info("Customer BFF: Successfully fetched {} orders for customer {}", orders.size(), customerId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Failed to fetch customer orders: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get order by tracking number
     * GET /api/customer/orders/tracking/{trackingNumber}
     *
     * @param trackingNumber Order tracking number
     * @param jwt JWT token with customer information
     * @return Order details (only if it belongs to the authenticated customer)
     */
    @GetMapping("/orders/tracking/{trackingNumber}")
    public ResponseEntity<OrderResponse> getOrderByTrackingNumber(@PathVariable String trackingNumber, @AuthenticationPrincipal Jwt jwt) {
        try {
            String customerId = extractCustomerId(jwt);
            log.info("Customer BFF: Fetching order with tracking {} for customer {}", trackingNumber, customerId);

            OrderResponse order = orderService.getByTrackingNumber(trackingNumber);

            // Security check: verify that the order belongs to this customer
            if (order.getCustomerId() == null || !customerId.equals(order.getCustomerId())) {
                log.warn("Customer {} attempted to access order with tracking {} (order customerId: {})",
                        customerId, trackingNumber, order.getCustomerId());
                throw new RuntimeException("Access denied: Order does not belong to this customer");
            }

            log.info("Customer BFF: Successfully fetched order with tracking {}", trackingNumber);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Failed to fetch order with tracking {}: {}", trackingNumber, e.getMessage());
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
            String customerId = extractCustomerId(jwt);
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
            String customerId = extractCustomerId(jwt);
            log.info("Customer BFF: Fetching events for order {} for customer {}", id, customerId);
            
            // First verify the order belongs to this customer
            OrderResponse order = orderService.getById(id);
            if (order.getCustomerId() == null || !customerId.equals(order.getCustomerId())) {
                log.warn("Customer {} attempted to access events for order {} (order customerId: {})", 
                        customerId, id, order.getCustomerId());
                throw new RuntimeException("Access denied: Order does not belong to this customer");
            }
            
            List<OrderEventResponse> events = orderEventService.getEventsForOrder(id);
            log.info("Customer BFF: Successfully fetched {} events for order {}", events.size(), id);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Failed to fetch events for order {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Create a new order (Customer creates their own order)
     * POST /api/customer/orders
     *
     * @param request Order creation request
     * @param jwt JWT token with customer information
     * @return Created order
     */
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String customerId = extractCustomerId(jwt);
            log.info("Customer BFF: Customer {} creating new order", customerId);

            // Set the customer ID from the JWT token (security: customer can only create orders for themselves)
            request.setCustomerId(customerId);

            // Create the order via business layer
            OrderResponse response = orderService.create(request);

            log.info("Customer BFF: Successfully created order {} for customer {}", response.getId(), customerId);

            return ResponseEntity
                    .created(URI.create("/api/customer/orders/" + response.getId()))
                    .body(response);

        } catch (Exception e) {
            log.error("Customer BFF: Failed to create order: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ========================================
    // Delivery Exception (Support Tickets) Endpoints
    // ========================================

    /**
     * Get all delivery exceptions for the authenticated customer
     * GET /api/customer/delivery-exceptions
     *
     * @param jwt JWT token with customer information
     * @return List of delivery exceptions for this customer
     */
    @GetMapping("/delivery-exceptions")
    public ResponseEntity<List<DeliveryExceptionResponse>> getCustomerExceptions(@AuthenticationPrincipal Jwt jwt) {
        try {
            String customerId = extractCustomerId(jwt);
            log.info("Customer BFF: Fetching delivery exceptions for customer {}", customerId);

            if (customerId == null) {
                log.error("Customer BFF: customerId is null - JWT had no sub claim");
                return ResponseEntity.ok(java.util.Collections.emptyList());
            }

            // Try to convert String to UUID
            // If customerId is not a valid UUID (e.g., username), fetch exceptions by checking orders
            List<DeliveryExceptionResponse> exceptions;
            try {
                java.util.UUID customerUuid = java.util.UUID.fromString(customerId);
                exceptions = exceptionService.getByCustomerId(customerUuid);
            } catch (IllegalArgumentException e) {
                log.warn("Customer BFF: customerId '{}' is not a valid UUID, fetching exceptions via orders", customerId);
                // For non-UUID customers (like "customer" username), we need to fetch exceptions
                // by checking which orders belong to this customer first
                List<OrderResponse> customerOrders = orderService.getByCustomerId(customerId);
                exceptions = new java.util.ArrayList<>();
                for (OrderResponse order : customerOrders) {
                    exceptions.addAll(exceptionService.getByOrderId(order.getId()));
                }
                log.info("Customer BFF: Found {} exceptions across {} orders for customer {}",
                        exceptions.size(), customerOrders.size(), customerId);
            }

            log.info("Customer BFF: Successfully fetched {} exceptions for customer {}", exceptions.size(), customerId);
            return ResponseEntity.ok(exceptions);
        } catch (Exception e) {
            log.error("Customer BFF: Failed to fetch delivery exceptions: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get delivery exception by ID
     * GET /api/customer/delivery-exceptions/{id}
     *
     * @param id Exception ID (UUID)
     * @param jwt JWT token with customer information
     * @return Exception details (only if it belongs to the authenticated customer)
     */
    @GetMapping("/delivery-exceptions/{id}")
    public ResponseEntity<DeliveryExceptionResponse> getExceptionById(
            @PathVariable java.util.UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String customerId = extractCustomerId(jwt);
            log.info("Customer BFF: Fetching exception {} for customer {}", id, customerId);

            DeliveryExceptionResponse exception = exceptionService.getById(id);

            // Security check: verify that the exception belongs to this customer
            // Handle both UUID-based customerIds and string-based customerIds (like usernames)
            boolean isAuthorized = false;

            try {
                // Try UUID comparison first
                java.util.UUID customerUuid = java.util.UUID.fromString(customerId);
                isAuthorized = exception.getCustomerId() != null && customerUuid.equals(exception.getCustomerId());
            } catch (IllegalArgumentException e) {
                // If customerId is not a UUID (e.g., username), verify by checking if the order belongs to this customer
                log.info("Customer BFF: customerId '{}' is not a UUID, checking order ownership", customerId);
                try {
                    OrderResponse order = orderService.getById(exception.getOrderId());
                    isAuthorized = order.getCustomerId() != null && customerId.equals(order.getCustomerId());
                } catch (Exception ex) {
                    log.error("Customer BFF: Failed to verify order ownership: {}", ex.getMessage());
                    isAuthorized = false;
                }
            }

            if (!isAuthorized) {
                log.warn("Customer {} attempted to access exception {} (exception customerId: {})",
                        customerId, id, exception.getCustomerId());
                throw new RuntimeException("Access denied: Exception does not belong to this customer");
            }

            log.info("Customer BFF: Successfully fetched exception {}", id);
            return ResponseEntity.ok(exception);
        } catch (Exception e) {
            log.error("Customer BFF: Failed to fetch exception {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Create a new delivery exception (support ticket)
     * POST /api/customer/delivery-exceptions
     *
     * @param request Exception creation request
     * @param jwt JWT token with customer information
     * @return Created exception
     */
    @PostMapping("/delivery-exceptions")
    public ResponseEntity<DeliveryExceptionResponse> createException(
            @Valid @RequestBody DeliveryExceptionRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String customerId = extractCustomerId(jwt);
            log.info("Customer BFF: Customer {} creating new delivery exception for order {}",
                    customerId, request.getOrderId());

            // Security: Verify that the order belongs to this customer before creating exception
            OrderResponse order = orderService.getById(request.getOrderId());
            if (order.getCustomerId() == null || !customerId.equals(order.getCustomerId())) {
                log.warn("Customer {} attempted to create exception for order {} (order customerId: {})",
                        customerId, request.getOrderId(), order.getCustomerId());
                throw new RuntimeException("Access denied: Cannot create exception for orders that don't belong to you");
            }

            // Set customer-specific fields (security: prevent customers from impersonating)
            // Only set UUID fields if customerId is a valid UUID
            try {
                java.util.UUID customerUuid = java.util.UUID.fromString(customerId);
                request.setCustomerId(customerUuid);
                request.setReportedBy(customerUuid);
            } catch (IllegalArgumentException e) {
                log.warn("Customer BFF: customerId '{}' is not a valid UUID, setting UUID fields to null", customerId);
                request.setCustomerId(null);
                request.setReportedBy(null);
            }

            request.setCustomerReported(true);
            request.setSource(Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.Source.CUSTOMER);

            // Create the exception via business layer
            DeliveryExceptionResponse response = exceptionService.create(request);
            
            log.info("Customer BFF: Successfully created exception {} for customer {} (UUID: {})", 
                    response.getId(), customerId, request.getCustomerId());
            
            return ResponseEntity
                    .created(URI.create("/api/customer/delivery-exceptions/" + response.getId()))
                    .body(response);
        } catch (Exception e) {
            log.error("Customer BFF: Failed to create delivery exception: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get delivery exceptions for a specific order
     * GET /api/customer/orders/{orderId}/exceptions
     *
     * @param orderId Order ID
     * @param jwt JWT token with customer information
     * @return List of exceptions for the order (only if order belongs to customer)
     */
    @GetMapping("/orders/{orderId}/exceptions")
    public ResponseEntity<List<DeliveryExceptionResponse>> getOrderExceptions(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String customerId = extractCustomerId(jwt);
            log.info("Customer BFF: Fetching exceptions for order {} for customer {}", orderId, customerId);

            // Security check: verify that the order belongs to this customer
            OrderResponse order = orderService.getById(orderId);
            if (order.getCustomerId() == null || !customerId.equals(order.getCustomerId())) {
                log.warn("Customer {} attempted to access exceptions for order {} (order customerId: {})",
                        customerId, orderId, order.getCustomerId());
                throw new RuntimeException("Access denied: Order does not belong to this customer");
            }

            List<DeliveryExceptionResponse> exceptions = exceptionService.getByOrderId(orderId);
            log.info("Customer BFF: Successfully fetched {} exceptions for order {}", exceptions.size(), orderId);
            return ResponseEntity.ok(exceptions);
        } catch (Exception e) {
            log.error("Customer BFF: Failed to fetch exceptions for order {}: {}", orderId, e.getMessage());
            throw e;
        }
    }

    // ========================================
    // Order Document Endpoints
    // ========================================

    /**
     * Get all documents for an order
     * GET /api/customer/orders/{id}/documents
     *
     * @param id Order ID
     * @param jwt JWT token with customer information
     * @return List of documents (only if order belongs to customer)
     */
    @GetMapping("/orders/{id}/documents")
    public ResponseEntity<List<DocumentUploadResponse>> getOrderDocuments(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String customerId = extractCustomerId(jwt);
            log.info("Customer BFF: Fetching documents for order {} for customer {}", id, customerId);

            // Security check: verify that the order belongs to this customer
            OrderResponse order = orderService.getById(id);
            if (order.getCustomerId() == null || !customerId.equals(order.getCustomerId())) {
                log.warn("Customer {} attempted to access documents for order {} (order customerId: {})",
                        customerId, id, order.getCustomerId());
                throw new RuntimeException("Access denied: Order does not belong to this customer");
            }

            List<DocumentUploadResponse> documents = orderDocumentService.listDocuments(id);
            log.info("Customer BFF: Successfully fetched {} documents for order {}", documents.size(), id);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("Customer BFF: Failed to fetch documents for order {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Download a specific document for an order
     * GET /api/customer/orders/{id}/documents/{documentId}/download
     *
     * @param id Order ID
     * @param documentId Document ID (filename)
     * @param jwt JWT token with customer information
     * @return Document content (only if order belongs to customer)
     */
    @GetMapping("/orders/{id}/documents/{documentId}/download")
    public ResponseEntity<byte[]> downloadOrderDocument(
            @PathVariable Long id,
            @PathVariable String documentId,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String customerId = extractCustomerId(jwt);
            log.info("Customer BFF: Downloading document {} for order {} for customer {}",
                    documentId, id, customerId);

            // Security check: verify that the order belongs to this customer
            OrderResponse order = orderService.getById(id);
            if (order.getCustomerId() == null || !customerId.equals(order.getCustomerId())) {
                log.warn("Customer {} attempted to download document {} for order {} (order customerId: {})",
                        customerId, documentId, id, order.getCustomerId());
                throw new RuntimeException("Access denied: Order does not belong to this customer");
            }

            DocumentDownloadResponse doc = orderDocumentService.downloadDocument(id, documentId);
            log.info("Customer BFF: Successfully downloaded document {} for order {}", documentId, id);

            return ResponseEntity.ok()
                    .header("Content-Type", doc.getContentType())
                    .header("Content-Disposition", "inline; filename=\"" + documentId + "\"")
                    .body(doc.getContent());
        } catch (Exception e) {
            log.error("Customer BFF: Failed to download document {} for order {}: {}",
                    documentId, id, e.getMessage());
            throw e;
        }
    }
}

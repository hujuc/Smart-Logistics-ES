package Operimus.SLMS.presentation_layer.admin_console_bff.warehouse_bff;

import Operimus.SLMS.business_layer.order_module.dto.DocumentDownloadResponse;
import Operimus.SLMS.business_layer.order_module.dto.DocumentUploadResponse;
import Operimus.SLMS.business_layer.order_module.dto.OrderRequest;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.service.OrderDocumentService;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * Warehouse BFF Controller - Admin Console
 *
 * This controller is part of the Backend-For-Frontend (BFF) pattern for the warehouse staff interface.
 * It exposes warehouse-specific endpoints that delegate to business layer services.
 * Handles both order operations and document operations.
 *
 * Endpoints are mapped to /api/warehouse/* to match the warehouse staff role permissions.
 */
@Slf4j
@RestController
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost", "http://localhost:3000"}, allowCredentials = "true")
public class WarehouseBffController {

    private final OrderService orderService;
    private final OrderDocumentService orderDocumentService;
    private final KeycloakUserService keycloakUserService;

    /**
     * Health check endpoint
     *
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<java.util.Map<String, String>> health() {
        return ResponseEntity.ok(java.util.Map.of(
                "status", "UP",
                "service", "Warehouse BFF",
                "timestamp", java.time.Instant.now().toString()
        ));
    }

    /**
     * Get all orders (with optional filtering)
     *
     * @return List of orders
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        try {
            log.info("Warehouse BFF: Fetching orders - status={}, search={}", status, search);

            List<OrderResponse> orders;

            // Filter by status if provided
            if (status != null && !status.isBlank()) {
                // Parse comma-separated statuses
                List<OrderStatus> statuses = Arrays.stream(status.split(","))
                        .map(String::trim)
                        .map(OrderStatus::valueOf)
                        .toList();
                orders = orderService.getByStatus(statuses);
            } else {
                orders = orderService.getAll();
            }

            // TODO: Add search filtering if needed
            // if (search != null && !search.isBlank()) {
            //     orders = orders.stream()
            //         .filter(order -> order.getReference().contains(search) ||
            //                         order.getShippingAddress().contains(search))
            //         .toList();
            // }

            return ResponseEntity.ok(orders);

        } catch (IllegalArgumentException e) {
            log.error("Invalid status parameter: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to fetch orders via warehouse BFF: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get order by ID
     *
     * @param id Order ID
     * @return Order details
     */
    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        try {
            log.info("Warehouse BFF: Fetching order {}", id);

            // Delegate to business layer
            OrderResponse order = orderService.getById(id);

            return ResponseEntity.ok(order);

        } catch (Exception e) {
            log.error("Failed to fetch order {} via warehouse BFF: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Create a new order
     *
     * @param request Order creation request
     * @return Created order
     */
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        try {
            log.info("Warehouse BFF: Creating new order");

            // Delegate to business layer
            OrderResponse response = orderService.create(request);

            return ResponseEntity
                    .created(URI.create("/api/warehouse/orders/" + response.getId()))
                    .body(response);

        } catch (Exception e) {
            log.error("Failed to create order via warehouse BFF: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Update an existing order
     *
     * @param id Order ID
     * @param request Order update request
     * @return Updated order
     */
    @PutMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderRequest request) {
        try {
            log.info("Warehouse BFF: Updating order {}", id);

            // Delegate to business layer
            OrderResponse response = orderService.update(id, request);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to update order {} via warehouse BFF: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Delete an order
     *
     * @param id Order ID
     * @return No content
     */
    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        try {
            log.info("Warehouse BFF: Deleting order {}", id);

            // Delegate to business layer
            orderService.delete(id);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Failed to delete order {} via warehouse BFF: {}", id, e.getMessage());
            throw e;
        }
    }

    // ========================================
    // Driver Operations
    // ========================================

    /**
     * Get all drivers (users with DRIVER role)
     * This endpoint allows warehouse staff to view available drivers for shipment assignment
     *
     * @return List of drivers
     */
    @GetMapping("/drivers")
    public ResponseEntity<List<UserRepresentation>> getDrivers() {
        try {
            log.info("Warehouse BFF: Fetching all drivers");

            // Get all users from Keycloak
            List<UserRepresentation> allUsers = keycloakUserService.getAllUsers();

            // Filter for users in the "Drivers" group
            List<UserRepresentation> drivers = allUsers.stream()
                    .filter(user -> {
                        List<String> groups = user.getGroups();
                        return groups != null && (
                                groups.contains("Drivers") ||
                                groups.contains("DRIVER") ||
                                groups.contains("driver")
                        );
                    })
                    .toList();

            log.info("Warehouse BFF: Found {} drivers out of {} total users", drivers.size(), allUsers.size());

            return ResponseEntity.ok(drivers);

        } catch (Exception e) {
            log.error("Failed to fetch drivers via warehouse BFF: {}", e.getMessage());
            throw e;
        }
    }

    // ========================================
    // Document Operations
    // ========================================

    /**
     * List all documents for an order
     *
     * @param orderId Order ID
     * @return List of documents
     */
    @GetMapping("/orders/{orderId}/documents")
    public ResponseEntity<List<DocumentUploadResponse>> listDocuments(@PathVariable Long orderId) {
        try {
            log.info("Warehouse BFF: Listing documents for order {}", orderId);

            // Delegate to business layer
            List<DocumentUploadResponse> documents = orderDocumentService.listDocuments(orderId);

            return ResponseEntity.ok(documents);

        } catch (Exception e) {
            log.error("Failed to list documents for order {} via warehouse BFF: {}", orderId, e.getMessage());
            throw e;
        }
    }

    /**
     * Download a specific document
     *
     * @param orderId Order ID
     * @param documentId Document ID
     * @return Document file
     */
    @GetMapping("/orders/{orderId}/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long orderId,
            @PathVariable String documentId) {
        try {
            log.info("Warehouse BFF: Downloading document {} for order {}", documentId, orderId);

            // Delegate to business layer
            DocumentDownloadResponse response = orderDocumentService.downloadDocument(orderId, documentId);

            ByteArrayResource resource = new ByteArrayResource(response.getContent());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(response.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Failed to download document {} for order {} via warehouse BFF: {}",
                    documentId, orderId, e.getMessage());
            throw e;
        }
    }
}

package Operimus.SLMS.presentation_layer.admin_console_bff.warehouse_bff;

import Operimus.SLMS.business_layer.order_module.dto.DocumentDownloadResponse;
import Operimus.SLMS.business_layer.order_module.dto.DocumentUploadResponse;
import Operimus.SLMS.business_layer.order_module.dto.OrderRequest;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.dto.ShipmentDocumentResponse;
import Operimus.SLMS.business_layer.order_module.service.OrderDocumentService;
import Operimus.SLMS.business_layer.order_module.service.ShipmentDocumentService;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;
import Operimus.SLMS.business_layer.notifications_module.service.WarehouseNotificationService;
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
    private final ShipmentDocumentService shipmentDocumentService;
    private final KeycloakUserService keycloakUserService;
    private final WarehouseNotificationService warehouseNotificationService;

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
     * PUBLIC TEST ENDPOINT - Trigger warehouse notification immediately
     * NO AUTHENTICATION REQUIRED - For testing only
     */
    @PostMapping("/public-test-notification")
    public ResponseEntity<java.util.Map<String, String>> publicTestNotification() {
        try {
            log.info("🧪 PUBLIC TEST: Creating warehouse notification NOW!");
            warehouseNotificationService.notifyLowStockAlert(
                "TEST-WH-001",
                "Test Warehouse",
                "test items",
                5
            );
            return ResponseEntity.ok(java.util.Map.of(
                    "status", "SUCCESS",
                    "message", "Low stock notification sent to all WAREHOUSE_STAFF users!",
                    "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            log.error("❌ Failed to send test notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of(
                    "status", "ERROR",
                    "message", e.getMessage(),
                    "error", e.getClass().getSimpleName()
            ));
        }
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
     * DEPRECATED: Order creation has been moved to the Customer Portal BFF.
     * Customers now create orders directly through /api/customer/orders endpoint.
     * This endpoint is kept for backward compatibility and testing purposes only.
     *
     * @param request Order creation request
     * @return Created order
     */
    @Deprecated
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        try {
            log.warn("Warehouse BFF: DEPRECATED endpoint called - order creation should be done via Customer Portal BFF");
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

    /**
     * List all carrier shipping documents associated with the shipment that contains the order.
     */
    @GetMapping("/orders/{orderId}/shipment-documents")
    public ResponseEntity<List<ShipmentDocumentResponse>> listShipmentDocuments(@PathVariable Long orderId) {
        try {
            log.info("Warehouse BFF: Listing shipment documents for order {}", orderId);
            List<ShipmentDocumentResponse> documents = shipmentDocumentService.listByOrder(orderId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("Failed to list shipment documents for order {}: {}", orderId, e.getMessage());
            throw e;
        }
    }

    /**
     * Download a shipping document generated for the shipment containing the given order.
     */
    @GetMapping("/orders/{orderId}/shipment-documents/{documentId}/download")
    public ResponseEntity<Resource> downloadShipmentDocument(
            @PathVariable Long orderId,
            @PathVariable String documentId) {
        try {
            log.info("Warehouse BFF: Downloading shipment document {} for order {}", documentId, orderId);
            DocumentDownloadResponse response = shipmentDocumentService.downloadForOrder(orderId, documentId);
            ByteArrayResource resource = new ByteArrayResource(response.getContent());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(response.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Failed to download shipment document {} for order {}: {}", documentId, orderId, e.getMessage());
            throw e;
        }
    }

    // ========================================
    // Warehouse Notification Test Endpoints
    // ========================================

    /**
     * Test endpoint to trigger a low stock alert notification
     * POST /api/warehouse/notifications/test/low-stock
     */
    @PostMapping("/notifications/test/low-stock")
    public ResponseEntity<java.util.Map<String, String>> testLowStockAlert(
            @RequestParam(required = false, defaultValue = "WH-001") String warehouseId,
            @RequestParam(required = false, defaultValue = "Warehouse A") String warehouseName,
            @RequestParam(required = false, defaultValue = "packaging materials") String materialName,
            @RequestParam(required = false, defaultValue = "15") int currentStock) {
        try {
            log.info("Warehouse BFF: Testing low stock alert notification");
            warehouseNotificationService.notifyLowStockAlert(warehouseId, warehouseName, materialName, currentStock);
            return ResponseEntity.ok(java.util.Map.of(
                    "status", "success",
                    "message", "Low stock alert notification sent"
            ));
        } catch (Exception e) {
            log.error("Failed to send low stock alert: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Test endpoint to trigger a scanner malfunction notification
     * POST /api/warehouse/notifications/test/scanner-malfunction
     */
    @PostMapping("/notifications/test/scanner-malfunction")
    public ResponseEntity<java.util.Map<String, String>> testScannerMalfunction(
            @RequestParam(required = false, defaultValue = "SC-003") String scannerId,
            @RequestParam(required = false, defaultValue = "Loading Dock 3") String scannerLocation) {
        try {
            log.info("Warehouse BFF: Testing scanner malfunction notification");
            warehouseNotificationService.notifyScannerMalfunction(scannerId, scannerLocation);
            return ResponseEntity.ok(java.util.Map.of(
                    "status", "success",
                    "message", "Scanner malfunction notification sent"
            ));
        } catch (Exception e) {
            log.error("Failed to send scanner malfunction notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Test endpoint to trigger a shipment prepared notification
     * POST /api/warehouse/notifications/test/shipment-prepared
     */
    @PostMapping("/notifications/test/shipment-prepared")
    public ResponseEntity<java.util.Map<String, String>> testShipmentPrepared(
            @RequestParam(required = false, defaultValue = "BATCH-2024-089") String batchId,
            @RequestParam(required = false, defaultValue = "BATCH-2024-089") String batchCode) {
        try {
            log.info("Warehouse BFF: Testing shipment prepared notification");
            warehouseNotificationService.notifyShipmentPrepared(batchId, batchCode);
            return ResponseEntity.ok(java.util.Map.of(
                    "status", "success",
                    "message", "Shipment prepared notification sent"
            ));
        } catch (Exception e) {
            log.error("Failed to send shipment prepared notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Test endpoint to trigger a maintenance due notification
     * POST /api/warehouse/notifications/test/maintenance-due
     */
    @PostMapping("/notifications/test/maintenance-due")
    public ResponseEntity<java.util.Map<String, String>> testMaintenanceDue(
            @RequestParam(required = false, defaultValue = "FL-007") String equipmentId,
            @RequestParam(required = false, defaultValue = "FL-007") String equipmentName,
            @RequestParam(required = false, defaultValue = "180") int daysSinceLastService) {
        try {
            log.info("Warehouse BFF: Testing maintenance due notification");
            warehouseNotificationService.notifyMaintenanceDue(equipmentId, equipmentName, daysSinceLastService);
            return ResponseEntity.ok(java.util.Map.of(
                    "status", "success",
                    "message", "Maintenance due notification sent"
            ));
        } catch (Exception e) {
            log.error("Failed to send maintenance due notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Test endpoint to trigger a new carrier rate notification
     * POST /api/warehouse/notifications/test/new-carrier-rate
     */
    @PostMapping("/notifications/test/new-carrier-rate")
    public ResponseEntity<java.util.Map<String, String>> testNewCarrierRate(
            @RequestParam(required = false, defaultValue = "FEDEX-001") String carrierId,
            @RequestParam(required = false, defaultValue = "FedEx") String carrierName) {
        try {
            log.info("Warehouse BFF: Testing new carrier rate notification");
            warehouseNotificationService.notifyNewCarrierRate(carrierId, carrierName);
            return ResponseEntity.ok(java.util.Map.of(
                    "status", "success",
                    "message", "New carrier rate notification sent"
            ));
        } catch (Exception e) {
            log.error("Failed to send new carrier rate notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Test endpoint to trigger an inventory count complete notification
     * POST /api/warehouse/notifications/test/inventory-count-complete
     */
    @PostMapping("/notifications/test/inventory-count-complete")
    public ResponseEntity<java.util.Map<String, String>> testInventoryCountComplete(
            @RequestParam(required = false, defaultValue = "ZONE-B") String zoneId,
            @RequestParam(required = false, defaultValue = "Zone B") String zoneName,
            @RequestParam(required = false, defaultValue = "1250") int itemsAccounted) {
        try {
            log.info("Warehouse BFF: Testing inventory count complete notification");
            warehouseNotificationService.notifyInventoryCountComplete(zoneId, zoneName, itemsAccounted);
            return ResponseEntity.ok(java.util.Map.of(
                    "status", "success",
                    "message", "Inventory count complete notification sent"
            ));
        } catch (Exception e) {
            log.error("Failed to send inventory count complete notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Test endpoint to trigger a package damaged notification
     * POST /api/warehouse/notifications/test/package-damaged
     */
    @PostMapping("/notifications/test/package-damaged")
    public ResponseEntity<java.util.Map<String, String>> testPackageDamaged(
            @RequestParam(required = false, defaultValue = "PKG-2024-5678") String packageId,
            @RequestParam(required = false, defaultValue = "PKG-2024-5678") String packageCode) {
        try {
            log.info("Warehouse BFF: Testing package damaged notification");
            warehouseNotificationService.notifyPackageDamaged(packageId, packageCode);
            return ResponseEntity.ok(java.util.Map.of(
                    "status", "success",
                    "message", "Package damaged notification sent"
            ));
        } catch (Exception e) {
            log.error("Failed to send package damaged notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Test endpoint to trigger a new packages received notification
     * POST /api/warehouse/notifications/test/new-packages-received
     */
    @PostMapping("/notifications/test/new-packages-received")
    public ResponseEntity<java.util.Map<String, String>> testNewPackagesReceived(
            @RequestParam(required = false, defaultValue = "45") int packageCount) {
        try {
            log.info("Warehouse BFF: Testing new packages received notification");
            warehouseNotificationService.notifyNewPackagesReceived(packageCount);
            return ResponseEntity.ok(java.util.Map.of(
                    "status", "success",
                    "message", "New packages received notification sent"
            ));
        } catch (Exception e) {
            log.error("Failed to send new packages received notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}

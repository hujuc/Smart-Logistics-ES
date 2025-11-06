package Operimus.SLMS.presentation_layer.admin_console_bff.logistics_bff;

import Operimus.SLMS.business_layer.order_shipment_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_shipment_module.dto.WarehouseRequest;
import Operimus.SLMS.business_layer.order_shipment_module.dto.WarehouseResponse;
import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrderStatus;
import Operimus.SLMS.business_layer.order_shipment_module.service.OrderService;
import Operimus.SLMS.business_layer.order_shipment_module.service.WarehouseService;
import Operimus.SLMS.business_layer.user_management_module.dto.PasswordUpdateDTO;
import Operimus.SLMS.business_layer.user_management_module.dto.UpdateUserDTO;
import Operimus.SLMS.business_layer.user_management_module.dto.UserDTO;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;
import Operimus.SLMS.presentation_layer.admin_console_bff.logistics_bff.dto.LogisticsStatsResponse;
import Operimus.SLMS.presentation_layer.admin_console_bff.logistics_bff.service.LogisticsStatsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Logistics BFF Controller - Admin Console
 *
 * This controller is part of the Backend-For-Frontend (BFF) pattern for the logistics manager interface.
 * Provides user management capabilities for the logistics manager role.
 *
 * Endpoints are mapped to /api/logistics/* to match the logistics manager role permissions.
 */
@Slf4j
@RestController
@RequestMapping("/api/logistics")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost", "http://localhost:3000"}, allowCredentials = "true")
public class LogisticsBffController {

    private final KeycloakUserService userService;
    private final LogisticsStatsService statsService;
    private final WarehouseService warehouseService;
    private final OrderService orderService;

    /**
     * Health check endpoint
     *
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<java.util.Map<String, String>> health() {
        return ResponseEntity.ok(java.util.Map.of(
                "status", "UP",
                "service", "Logistics BFF",
                "timestamp", java.time.Instant.now().toString()
        ));
    }

    // ===========================
    // Dashboard Statistics Endpoints
    // ===========================

    /**
     * Get comprehensive logistics statistics for dashboard
     *
     * @param period Time period for trend calculation: "daily", "weekly", or "monthly" (default: "daily")
     * @return Comprehensive statistics with breakdowns
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStatistics(@RequestParam(defaultValue = "daily") String period) {
        try {
            log.info("Fetching logistics statistics for period: {}", period);

            // Validate period parameter
            if (!period.matches("daily|weekly|monthly")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid period. Must be 'daily', 'weekly', or 'monthly'"));
            }

            LogisticsStatsResponse stats = statsService.calculateStats(period);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching logistics statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch statistics: " + e.getMessage()));
        }
    }

    // ===========================
    // Warehouse Management Endpoints
    // ===========================

    /**
     * Get all warehouses
     *
     * @return List of all warehouses
     */
    @GetMapping("/warehouses")
    public ResponseEntity<?> getAllWarehouses() {
        try {
            log.info("Fetching all warehouses via Logistics BFF");
            List<WarehouseResponse> warehouses = warehouseService.getAll();
            return ResponseEntity.ok(warehouses);
        } catch (Exception e) {
            log.error("Error fetching all warehouses: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch warehouses"));
        }
    }

    /**
     * Get warehouse by ID
     *
     * @param id Warehouse ID
     * @return Warehouse details
     */
    @GetMapping("/warehouses/{id}")
    public ResponseEntity<?> getWarehouseById(@PathVariable Long id) {
        try {
            log.info("Fetching warehouse by ID: {}", id);
            WarehouseResponse warehouse = warehouseService.getById(id);
            return ResponseEntity.ok(warehouse);
        } catch (Exception e) {
            log.error("Error fetching warehouse by ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Warehouse not found"));
        }
    }

    /**
     * Create a new warehouse
     *
     * @param request Warehouse creation request
     * @return Created warehouse
     */
    @PostMapping("/warehouses")
    public ResponseEntity<?> createWarehouse(@Valid @RequestBody WarehouseRequest request) {
        try {
            log.info("Creating new warehouse via Logistics BFF: {}", request.getCode());
            WarehouseResponse warehouse = warehouseService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(warehouse);
        } catch (Exception e) {
            log.error("Error creating warehouse: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update warehouse
     *
     * @param id Warehouse ID
     * @param request Warehouse update request
     * @return Updated warehouse
     */
    @PutMapping("/warehouses/{id}")
    public ResponseEntity<?> updateWarehouse(@PathVariable Long id, @Valid @RequestBody WarehouseRequest request) {
        try {
            log.info("Updating warehouse via Logistics BFF: {}", id);
            WarehouseResponse warehouse = warehouseService.update(id, request);
            return ResponseEntity.ok(warehouse);
        } catch (Exception e) {
            log.error("Error updating warehouse: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete warehouse
     *
     * @param id Warehouse ID
     * @return Success message
     */
    @DeleteMapping("/warehouses/{id}")
    public ResponseEntity<?> deleteWarehouse(@PathVariable Long id) {
        try {
            log.info("Deleting warehouse via Logistics BFF: {}", id);
            warehouseService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Warehouse deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting warehouse: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete warehouse"));
        }
    }

    /**
     * Get orders by warehouse ID
     *
     * @param id Warehouse ID
     * @param status Optional status filter
     * @return List of orders for the warehouse
     */
    @GetMapping("/warehouses/{id}/orders")
    public ResponseEntity<?> getWarehouseOrders(
            @PathVariable Long id,
            @RequestParam(required = false) String status) {
        try {
            log.info("Fetching orders for warehouse {} via Logistics BFF, status={}", id, status);

            List<OrderResponse> orders;
            if (status != null && !status.isEmpty()) {
                try {
                    ShipmentOrderStatus orderStatus = ShipmentOrderStatus.valueOf(status.toUpperCase());
                    orders = orderService.getByWarehouseAndStatus(id, orderStatus);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Invalid status value: " + status));
                }
            } else {
                orders = orderService.getByWarehouse(id);
            }

            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error fetching warehouse orders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch warehouse orders"));
        }
    }

    // ===========================
    // User Management Endpoints
    // ===========================

    /**
     * Get all users
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            log.info("Fetching all users via Logistics BFF");
            List<UserRepresentation> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error fetching all users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch users"));
        }
    }

    /**
     * Get user by ID
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable String userId) {
        try {
            log.info("Fetching user by ID: {}", userId);
            UserRepresentation user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error fetching user by ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }
    }

    /**
     * Create a new user
     */
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO) {
        try {
            log.info("Creating new user via Logistics BFF: {}", userDTO.getUsername());
            UserRepresentation user = userService.createUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update user
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        try {
            log.info("Updating user via Logistics BFF: {}", userId);
            UserRepresentation user = userService.updateUser(userId, updateUserDTO);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error updating user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update user password
     */
    @PutMapping("/users/{userId}/password")
    public ResponseEntity<?> updatePassword(@PathVariable String userId, @Valid @RequestBody PasswordUpdateDTO passwordDTO) {
        try {
            log.info("Updating password for user via Logistics BFF: {}", userId);
            userService.updatePassword(userId, passwordDTO.getPassword());
            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } catch (Exception e) {
            log.error("Error updating password: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete user
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            log.info("Deleting user via Logistics BFF: {}", userId);
            userService.deleteUser(userId);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete user"));
        }
    }

    /**
     * Search users
     */
    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(@RequestParam String query) {
        try {
            log.info("Searching users via Logistics BFF: {}", query);
            List<UserRepresentation> users = userService.searchUsers(query);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error searching users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to search users"));
        }
    }
}

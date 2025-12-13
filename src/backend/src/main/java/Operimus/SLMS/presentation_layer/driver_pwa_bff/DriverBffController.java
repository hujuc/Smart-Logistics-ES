package Operimus.SLMS.presentation_layer.driver_pwa_bff;

import Operimus.SLMS.business_layer.order_module.dto.DocumentUploadResponse;
import Operimus.SLMS.business_layer.order_module.entities.Order;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.repository.OrderRepository;
import Operimus.SLMS.business_layer.order_module.service.OrderDocumentService;
import Operimus.SLMS.business_layer.order_module.service.OrderEventService;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.DeliveryCompletionRequest;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Shipment;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.ShipmentStatus;
import Operimus.SLMS.business_layer.rate_carrier_module.repository.ShipmentRepository;
import Operimus.SLMS.business_layer.rate_carrier_module.service.ShipmentService;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;
import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.ExceptionType;
import Operimus.SLMS.business_layer.visibility_module.exceptions.service.DeliveryExceptionService;
import Operimus.SLMS.presentation_layer.driver_pwa_bff.dto.DailyScheduleDeliveryResponse;
import Operimus.SLMS.presentation_layer.driver_pwa_bff.dto.DailyScheduleResponse;
import Operimus.SLMS.presentation_layer.driver_pwa_bff.dto.DriverDashboardResponse;
import Operimus.SLMS.presentation_layer.driver_pwa_bff.dto.DriverOrderScanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Driver BFF Controller
 *
 * This controller is part of the Backend-For-Frontend (BFF) pattern for the
 * driver interface.
 * It exposes driver-specific endpoints that delegate to business layer
 * services.
 *
 * Endpoints are mapped to /api/driver/* to match the driver role permissions.
 */
@Slf4j
@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverBffController {

    private final OrderService orderService;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentService shipmentService;
    private final OrderRepository orderRepository;
    private final KeycloakUserService keycloakUserService;
    private final OrderDocumentService orderDocumentService;
    private final OrderEventService orderEventService;
    private final DeliveryExceptionService deliveryExceptionService;

    /**
     * Health check endpoint
     *
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<java.util.Map<String, String>> health() {
        return ResponseEntity.ok(java.util.Map.of(
                "status", "UP",
                "service", "Driver BFF",
                "timestamp", java.time.Instant.now().toString()));
    }

    /**
     * Confirm that an order belonging to the driver's shipment was scanned
     *
     * @param orderId        Order identifier encoded in the QR code
     * @param authentication Authenticated driver token (provides driverId)
     * @return Confirmation status and shipment progress
     */
    @PostMapping("/orders/{orderId}/confirm")
    public ResponseEntity<DriverOrderScanResponse> confirmOrderScan(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt principal) {
        String driverId = extractDriverIdentifier(principal);
        if (!StringUtils.hasText(driverId)) {
            throw new AccessDeniedException("Driver authentication is required.");
        }

        log.info("Driver {} confirming scan for order {}", driverId, orderId);

        var result = orderService.confirmOrderScan(orderId, driverId);
        return ResponseEntity.ok(DriverOrderScanResponse.from(result));
    }

    /**
     * Complete delivery with proof of delivery photo
     *
     * @param orderId   Order identifier
     * @param file      Proof of delivery photo
     * @param principal Authenticated driver token
     * @return Order details with POD URL
     */
    @PostMapping("/orders/{orderId}/deliver")
    public ResponseEntity<java.util.Map<String, Object>> completeDelivery(
            @PathVariable Long orderId,
            @RequestParam("photo") MultipartFile file,
            @AuthenticationPrincipal Jwt principal) {
        String driverId = extractDriverIdentifier(principal);
        if (!StringUtils.hasText(driverId)) {
            throw new AccessDeniedException("Driver authentication is required.");
        }

        log.info("Driver {} completing delivery for order {} with POD", driverId, orderId);

        // Verify the order exists and belongs to the driver's shipment
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getShipmentId() == null) {
            throw new RuntimeException("Order is not assigned to a shipment");
        }

        Shipment shipment = shipmentRepository.findById(order.getShipmentId())
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        String resolvedDriverId = normalizeDriverIdentifier(driverId);
        if (!resolvedDriverId.equals(shipment.getDriverId())) {
            throw new AccessDeniedException("Driver is not assigned to this shipment");
        }

        // Upload the proof of delivery photo
        DocumentUploadResponse uploadResponse = orderDocumentService.uploadDocument(orderId, file);

        // Update order with POD URL and mark as delivered
        order.setProofOfDeliveryUrl(uploadResponse.getUrl());
        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        // Record the status change event
        orderEventService.recordStatusChange(order, previousStatus, OrderStatus.DELIVERED);

        log.info("Order {} marked as delivered with POD at {}", orderId, uploadResponse.getUrl());

        return ResponseEntity.ok(java.util.Map.of(
                "orderId", order.getId(),
                "status", order.getStatus().toString(),
                "proofOfDeliveryUrl", uploadResponse.getUrl(),
                "message", "Delivery completed successfully"));
    }

    /**
     * Report a delivery exception
     *
     * @param orderId       Order identifier
     * @param exceptionType Type of exception
     * @param description   Description of the exception
     * @param principal     Authenticated driver token
     * @return Exception details
     */
    @PostMapping("/orders/{orderId}/exception")
    public ResponseEntity<java.util.Map<String, Object>> reportException(
            @PathVariable Long orderId,
            @RequestParam("exceptionType") ExceptionType exceptionType,
            @RequestParam("description") String description,
            @AuthenticationPrincipal Jwt principal) {
        String driverId = extractDriverIdentifier(principal);
        if (!StringUtils.hasText(driverId)) {
            throw new AccessDeniedException("Driver authentication is required.");
        }

        log.info("Driver {} reporting exception for order {}: {}", driverId, orderId, exceptionType);

        // Verify the order exists and belongs to the driver's shipment
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getShipmentId() == null) {
            throw new RuntimeException("Order is not assigned to a shipment");
        }

        Shipment shipment = shipmentRepository.findById(order.getShipmentId())
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        String resolvedDriverId = normalizeDriverIdentifier(driverId);
        if (!resolvedDriverId.equals(shipment.getDriverId())) {
            throw new AccessDeniedException("Driver is not assigned to this shipment");
        }

        // Get the driver's UUID from Keycloak
        java.util.UUID driverUUID;
        try {
            String userId = keycloakUserService.getUserByUsername(resolvedDriverId).getId();
            driverUUID = java.util.UUID.fromString(userId);
        } catch (Exception e) {
            log.warn("Could not resolve driver UUID, using a generated one");
            driverUUID = java.util.UUID.randomUUID();
        }

        // Create the exception
        var exceptionResponse = deliveryExceptionService.createDriverException(
                orderId, exceptionType, description, driverUUID);

        log.info("Created exception {} for order {}", exceptionResponse.getId(), orderId);

        return ResponseEntity.ok(java.util.Map.of(
                "exceptionId", exceptionResponse.getId().toString(),
                "orderId", orderId,
                "type", exceptionType.toString(),
                "status", exceptionResponse.getStatus().toString(),
                "message", "Exception reported successfully"));
    }

    /**
     * Get the driver's daily schedule
     *
     * @param principal Authenticated driver token
     * @return Daily schedule with all deliveries for the driver
     */
    @GetMapping("/daily-schedule")
    public ResponseEntity<DailyScheduleResponse> getDailySchedule(
            @AuthenticationPrincipal Jwt principal) {
        String driverId = extractDriverIdentifier(principal);
        if (!StringUtils.hasText(driverId)) {
            throw new AccessDeniedException("Driver authentication is required.");
        }

        log.info("Driver {} requesting daily schedule", driverId);

        // Get all shipments for this driver that are IN_TRANSIT (accepted and active)
        List<Shipment> shipments = shipmentRepository.findByDriverId(driverId);
        log.info("Found {} total shipments for driver {}", shipments.size(), driverId);
        List<Shipment> activeShipments = shipments.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.IN_TRANSIT)
                .toList();
        log.info("Found {} IN_TRANSIT shipments for driver {}", activeShipments.size(), driverId);

        if (activeShipments.isEmpty()) {
            log.info("No active (IN_TRANSIT) shipments found for driver {}", driverId);
            return ResponseEntity.ok(DailyScheduleResponse.builder()
                    .totalStops(0)
                    .totalPackages(0)
                    .totalWeight(0.0)
                    .deliveries(List.of())
                    .build());
        }

        // Get all orders from all active shipments
        List<Order> allOrders = new java.util.ArrayList<>();
        for (Shipment s : activeShipments) {
            List<Order> shipmentOrders = orderRepository.findByShipmentId(s.getId());
            allOrders.addAll(shipmentOrders);
        }

        // Filter out orders that have active delivery exceptions
        // Active exception statuses: OPEN, ASSIGNED, IN_PROGRESS, ESCALATED
        List<Order> ordersWithoutActiveExceptions = new java.util.ArrayList<>(allOrders.stream()
                .filter(order -> {
                    var exceptions = deliveryExceptionService.getByOrderId(order.getId());

                    // Include order only if it has no exceptions or all exceptions are
                    // resolved/closed
                    return exceptions.stream().noneMatch(exception -> exception
                            .getStatus() == Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.ExceptionStatus.OPEN
                            ||
                            exception
                                    .getStatus() == Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.ExceptionStatus.ASSIGNED
                            ||
                            exception
                                    .getStatus() == Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.ExceptionStatus.IN_PROGRESS
                            ||
                            exception
                                    .getStatus() == Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.ExceptionStatus.ESCALATED);
                })
                .toList());

        log.info("Filtered {} orders with active exceptions out of {} total orders",
                allOrders.size() - ordersWithoutActiveExceptions.size(), allOrders.size());

        // Log order statuses for debugging
        java.util.Map<OrderStatus, Long> statusCounts = ordersWithoutActiveExceptions.stream()
                .collect(java.util.stream.Collectors.groupingBy(Order::getStatus,
                        java.util.stream.Collectors.counting()));
        log.info("Order statuses after filtering: {}", statusCounts);

        // Sort orders by estimated delivery date and shipping city
        ordersWithoutActiveExceptions.sort(Comparator
                .comparing((Order o) -> o.getEstimatedDeliveryDate() != null ? o.getEstimatedDeliveryDate()
                        : java.time.LocalDate.MAX)
                .thenComparing(o -> o.getShippingCity() != null ? o.getShippingCity() : ""));

        AtomicInteger stopNumber = new AtomicInteger(1);
        List<DailyScheduleDeliveryResponse> deliveries = ordersWithoutActiveExceptions.stream()
                .map(order -> DailyScheduleDeliveryResponse.builder()
                        .orderId(order.getId())
                        .orderReference(order.getReference())
                        .stopNumber(stopNumber.getAndIncrement())
                        .customerName(order.getCustomerId())
                        .address(order.getShippingAddress())
                        .phone(order.getShippingPhone())
                        .status(order.getStatus())
                        .weight(order.getWeight())
                        .weightUnit(order.getWeightUnit())
                        .estimatedDeliveryDate(order.getEstimatedDeliveryDate())
                        .shippingCity(order.getShippingCity())
                        .shippingPostalCode(order.getShippingPostalCode())
                        .shippingCountry(order.getShippingCountry())
                        .shippingStateOrProvince(order.getShippingStateOrProvince())
                        .warehouseAddress(DailyScheduleDeliveryResponse.WarehouseAddressDto.builder()
                                .name(order.getWarehouse().getName())
                                .addressLine1(order.getWarehouse().getAddressLine1())
                                .addressLine2(order.getWarehouse().getAddressLine2())
                                .city(order.getWarehouse().getCity())
                                .stateOrProvince(order.getWarehouse().getStateOrProvince())
                                .country(order.getWarehouse().getCountry())
                                .postalCode(order.getWarehouse().getPostalCode())
                                .build())
                        .build())
                .toList();

        // Use first shipment for summary info (or could aggregate)
        Shipment firstShipment = activeShipments.get(0);
        double totalWeight = activeShipments.stream()
                .mapToDouble(s -> s.getTotalWeight() != null ? s.getTotalWeight() : 0.0)
                .sum();

        DailyScheduleResponse response = DailyScheduleResponse.builder()
                .shipmentCode(firstShipment.getShipmentCode())
                .shipmentId(firstShipment.getId())
                .totalStops(ordersWithoutActiveExceptions.size())
                .totalPackages(ordersWithoutActiveExceptions.size()) // Assuming 1 package per order for now
                .totalWeight(totalWeight)
                .weightUnit(firstShipment.getWeightUnit())
                .deliveries(deliveries)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get pending shipments awaiting driver acceptance
     *
     * @param principal Authenticated driver token
     * @return List of shipments in PENDING_ACCEPTANCE status
     */
    @GetMapping("/pending-shipments")
    public ResponseEntity<List<java.util.Map<String, Object>>> getPendingShipments(
            @AuthenticationPrincipal Jwt principal) {
        String driverId = extractDriverIdentifier(principal);
        if (!StringUtils.hasText(driverId)) {
            throw new AccessDeniedException("Driver authentication is required.");
        }

        log.info("Driver {} requesting pending shipments", driverId);

        List<Shipment> shipments = shipmentRepository.findByDriverId(driverId);

        List<java.util.Map<String, Object>> pendingShipments = shipments.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.PENDING_ACCEPTANCE)
                .map(s -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", s.getId());
                    map.put("shipmentCode", s.getShipmentCode());
                    map.put("status", s.getStatus().toString());
                    map.put("carrierName", s.getCarrier().getName());
                    map.put("totalOrders", s.getTotalOrders() != null ? s.getTotalOrders() : 0);
                    map.put("estimatedDeliveryDate",
                            s.getEstimatedDeliveryDate() != null ? s.getEstimatedDeliveryDate().toString() : "");
                    return map;
                })
                .toList();

        return ResponseEntity.ok(pendingShipments);
    }

    /**
     * Get the driver's dashboard data
     *
     * @param principal Authenticated driver token
     * @return Dashboard data with today's statistics and next deliveries preview
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DriverDashboardResponse> getDashboard(
            @AuthenticationPrincipal Jwt principal) {
        String driverId = extractDriverIdentifier(principal);
        if (!StringUtils.hasText(driverId)) {
            throw new AccessDeniedException("Driver authentication is required.");
        }

        log.info("Driver {} requesting dashboard", driverId);

        // Get all shipments for this driver
        List<Shipment> shipments = shipmentRepository.findByDriverId(driverId);

        // Filter for IN_TRANSIT shipments only
        List<Shipment> activeShipments = shipments.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.IN_TRANSIT)
                .toList();

        if (activeShipments.isEmpty()) {
            log.info("No active shipments found for driver {}", driverId);
            return ResponseEntity.ok(DriverDashboardResponse.builder()
                    .todayStats(DriverDashboardResponse.TodayStats.builder()
                            .totalDeliveries(0)
                            .completed(0)
                            .remaining(0)
                            .nextStopETA("N/A")
                            .build())
                    .nextDeliveries(List.of())
                    .build());
        }

        // Get all orders from all active shipments
        List<Order> allOrders = new java.util.ArrayList<>();
        for (Shipment s : activeShipments) {
            List<Order> shipmentOrders = orderRepository.findByShipmentId(s.getId());
            allOrders.addAll(shipmentOrders);
        }

        // Filter out orders that have active delivery exceptions
        // Active exception statuses: OPEN, ASSIGNED, IN_PROGRESS, ESCALATED
        List<Order> ordersWithoutActiveExceptions = new java.util.ArrayList<>(allOrders.stream()
                .filter(order -> {
                    var exceptions = deliveryExceptionService.getByOrderId(order.getId());

                    // Include order only if it has no exceptions or all exceptions are
                    // resolved/closed
                    return exceptions.stream().noneMatch(exception -> exception
                            .getStatus() == Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.ExceptionStatus.OPEN
                            ||
                            exception
                                    .getStatus() == Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.ExceptionStatus.ASSIGNED
                            ||
                            exception
                                    .getStatus() == Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.ExceptionStatus.IN_PROGRESS
                            ||
                            exception
                                    .getStatus() == Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.ExceptionStatus.ESCALATED);
                })
                .toList());

        log.info("Dashboard: Filtered {} orders with active exceptions out of {} total orders",
                allOrders.size() - ordersWithoutActiveExceptions.size(), allOrders.size());

        // Sort orders by estimated delivery date and shipping city
        ordersWithoutActiveExceptions.sort(Comparator
                .comparing((Order o) -> o.getEstimatedDeliveryDate() != null ? o.getEstimatedDeliveryDate()
                        : java.time.LocalDate.MAX)
                .thenComparing(o -> o.getShippingCity() != null ? o.getShippingCity() : ""));

        // Calculate statistics
        long completed = ordersWithoutActiveExceptions.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .count();

        long remaining = ordersWithoutActiveExceptions.stream()
                .filter(o -> o.getStatus() != OrderStatus.DELIVERED)
                .count();

        // Get next 3 deliveries that are pending
        AtomicInteger stopNumber = new AtomicInteger(1);
        List<DriverDashboardResponse.NextDeliveryPreview> nextDeliveries = ordersWithoutActiveExceptions.stream()
                .filter(o -> o.getStatus() != OrderStatus.DELIVERED)
                .limit(3)
                .map(order -> DriverDashboardResponse.NextDeliveryPreview.builder()
                        .orderId(order.getId())
                        .stopNumber(stopNumber.getAndIncrement())
                        .customerName(order.getCustomerId() != null ? order.getCustomerId() : "Unknown Customer")
                        .address(order.getShippingAddress())
                        .priority("Medium") // Default priority for now
                        .build())
                .toList();

        DriverDashboardResponse.TodayStats stats = DriverDashboardResponse.TodayStats.builder()
                .totalDeliveries(ordersWithoutActiveExceptions.size())
                .completed((int) completed)
                .remaining((int) remaining)
                .nextStopETA("15 min") // Placeholder for now
                .build();

        DriverDashboardResponse response = DriverDashboardResponse.builder()
                .todayStats(stats)
                .nextDeliveries(nextDeliveries)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Driver accepts a shipment assigned to them
     *
     * @param shipmentId Shipment identifier
     * @param principal  Authenticated driver token
     * @return Confirmation and updated shipment status
     */
    @PostMapping("/shipments/{shipmentId}/accept")
    public ResponseEntity<java.util.Map<String, Object>> acceptShipment(
            @PathVariable Long shipmentId,
            @AuthenticationPrincipal Jwt principal) {
        String driverId = extractDriverIdentifier(principal);
        if (!StringUtils.hasText(driverId)) {
            throw new AccessDeniedException("Driver authentication is required.");
        }

        log.info("Driver {} accepting shipment {}", driverId, shipmentId);

        Shipment shipment = shipmentService.acceptShipment(shipmentId, driverId);

        // Get order count for response
        List<Order> orders = orderRepository.findByShipmentId(shipmentId);

        return ResponseEntity.ok(java.util.Map.of(
                "shipmentId", shipment.getId(),
                "shipmentCode", shipment.getShipmentCode(),
                "status", shipment.getStatus().toString(),
                "orderCount", orders.size(),
                "message", "Shipment accepted successfully. All orders are now IN_TRANSIT."));
    }

    /**
     * Driver completes delivery for entire shipment.
     * Can either deliver successfully or report exceptions.
     *
     * @param shipmentId Shipment identifier
     * @param request    Delivery completion details (hasException, exceptionType,
     *                   exceptionDescription)
     * @param principal  Authenticated driver token
     * @return Confirmation of delivery completion or exception reporting
     */
    @PostMapping("/shipments/{shipmentId}/complete")
    public ResponseEntity<java.util.Map<String, Object>> completeShipmentDelivery(
            @PathVariable Long shipmentId,
            @RequestBody DeliveryCompletionRequest request,
            @AuthenticationPrincipal Jwt principal) {
        String driverId = extractDriverIdentifier(principal);
        if (!StringUtils.hasText(driverId)) {
            throw new AccessDeniedException("Driver authentication is required.");
        }

        log.info("Driver {} completing delivery for shipment {} (hasException: {})",
                driverId, shipmentId, request.getHasException());

        // Get shipment and orders
        List<Order> orders = orderRepository.findByShipmentId(shipmentId);
        if (orders.isEmpty()) {
            throw new RuntimeException("No orders found for shipment " + shipmentId);
        }

        if (Boolean.TRUE.equals(request.getHasException())) {
            // Driver reported an exception during delivery
            if (request.getExceptionType() == null || request.getExceptionDescription() == null) {
                throw new IllegalArgumentException(
                        "Exception type and description are required when hasException is true");
            }

            // Mark shipment as EXCEPTION
            Shipment shipment = shipmentService.reportDeliveryException(shipmentId, driverId);

            // Create delivery exceptions for all orders in the shipment
            ExceptionType exceptionType = parseExceptionType(request.getExceptionType());
            java.util.UUID driverUuid = java.util.UUID.fromString(driverId);

            orders.forEach(order -> {
                deliveryExceptionService.createDriverException(
                        order.getId(),
                        exceptionType,
                        request.getExceptionDescription(),
                        driverUuid);
            });

            log.info("Shipment {} marked as EXCEPTION with {} delivery exceptions created",
                    shipmentId, orders.size());

            return ResponseEntity.ok(java.util.Map.of(
                    "shipmentId", shipment.getId(),
                    "status", shipment.getStatus().toString(),
                    "exceptionType", request.getExceptionType(),
                    "ordersAffected", orders.size(),
                    "message", "Delivery exception reported. CSR team has been notified."));
        } else {
            // Normal delivery completion
            Shipment shipment = shipmentService.completeDelivery(shipmentId, driverId);

            log.info("Shipment {} completed successfully with {} orders delivered",
                    shipmentId, orders.size());

            return ResponseEntity.ok(java.util.Map.of(
                    "shipmentId", shipment.getId(),
                    "status", shipment.getStatus().toString(),
                    "ordersDelivered", orders.size(),
                    "message", "All deliveries completed successfully!"));
        }
    }

    /**
     * Parse exception type string to enum
     */
    private ExceptionType parseExceptionType(String exceptionTypeStr) {
        return switch (exceptionTypeStr.toUpperCase()) {
            case "CUSTOMER_NOT_AVAILABLE" -> ExceptionType.CUSTOMER_NOT_AVAILABLE;
            case "WRONG_ADDRESS" -> ExceptionType.WRONG_ADDRESS;
            case "PACKAGE_DAMAGED", "DAMAGED" -> ExceptionType.DAMAGED;
            case "ACCESS_DENIED" -> ExceptionType.ACCESS_DENIED;
            case "WEATHER" -> ExceptionType.WEATHER;
            default -> ExceptionType.OTHER;
        };
    }

    private String extractDriverIdentifier(Jwt principal) {
        if (principal == null) {
            return null;
        }
        String identifier;
        if (StringUtils.hasText(principal.getSubject())) {
            identifier = principal.getSubject();
        } else {
            String preferredUsername = principal.getClaimAsString("preferred_username");
            if (StringUtils.hasText(preferredUsername)) {
                identifier = preferredUsername;
            } else {
                identifier = principal.getClaimAsString("email");
            }
        }
        // Normalize to UUID if it's a username
        return normalizeDriverIdentifier(identifier);
    }

    private String normalizeDriverIdentifier(String driverIdentifier) {
        if (!StringUtils.hasText(driverIdentifier)) {
            return driverIdentifier;
        }
        if (looksLikeUuid(driverIdentifier)) {
            return driverIdentifier;
        }
        try {
            return keycloakUserService.searchUsers(driverIdentifier).stream()
                    .filter(user -> driverIdentifier.equalsIgnoreCase(user.getUsername())
                            || driverIdentifier.equalsIgnoreCase(user.getEmail()))
                    .map(user -> user.getId())
                    .findFirst()
                    .orElse(driverIdentifier);
        } catch (Exception e) {
            log.warn("Failed to normalize driver identifier {}: {}", driverIdentifier, e.getMessage());
            return driverIdentifier;
        }
    }

    private boolean looksLikeUuid(String str) {
        if (str == null || str.length() != 36) {
            return false;
        }
        return str.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    }
}

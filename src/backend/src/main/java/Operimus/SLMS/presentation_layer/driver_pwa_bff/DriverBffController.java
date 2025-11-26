package Operimus.SLMS.presentation_layer.driver_pwa_bff;

import Operimus.SLMS.business_layer.order_module.dto.DocumentUploadResponse;
import Operimus.SLMS.business_layer.order_module.entities.Order;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.repository.OrderRepository;
import Operimus.SLMS.business_layer.order_module.service.OrderDocumentService;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Shipment;
import Operimus.SLMS.business_layer.rate_carrier_module.repository.ShipmentRepository;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;
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
 * This controller is part of the Backend-For-Frontend (BFF) pattern for the driver interface.
 * It exposes driver-specific endpoints that delegate to business layer services.
 *
 * Endpoints are mapped to /api/driver/* to match the driver role permissions.
 */
@Slf4j
@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost", "http://localhost:3000"}, allowCredentials = "true")
public class DriverBffController {

    private final OrderService orderService;
    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final KeycloakUserService keycloakUserService;
    private final OrderDocumentService orderDocumentService;

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
                "timestamp", java.time.Instant.now().toString()
        ));
    }

    /**
     * Confirm that an order belonging to the driver's shipment was scanned
     *
     * @param orderId Order identifier encoded in the QR code
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
     * @param orderId Order identifier
     * @param file Proof of delivery photo
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

        log.info("Order {} marked as delivered with POD at {}", orderId, uploadResponse.getUrl());

        return ResponseEntity.ok(java.util.Map.of(
                "orderId", order.getId(),
                "status", order.getStatus().toString(),
                "proofOfDeliveryUrl", uploadResponse.getUrl(),
                "message", "Delivery completed successfully"
        ));
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

        // Get all shipments for this driver
        List<Shipment> shipments = shipmentRepository.findByDriverId(driverId);

        if (shipments.isEmpty()) {
            log.info("No shipments found for driver {}", driverId);
            return ResponseEntity.ok(DailyScheduleResponse.builder()
                    .totalStops(0)
                    .totalPackages(0)
                    .totalWeight(0.0)
                    .deliveries(List.of())
                    .build());
        }

        // For now, return the first shipment (in future, could filter by date)
        Shipment shipment = shipments.get(0);
        List<Order> orders = orderRepository.findByShipmentId(shipment.getId());

        // Sort orders by estimated delivery date and shipping city
        orders.sort(Comparator
                .comparing((Order o) -> o.getEstimatedDeliveryDate() != null ? o.getEstimatedDeliveryDate() : java.time.LocalDate.MAX)
                .thenComparing(o -> o.getShippingCity() != null ? o.getShippingCity() : ""));

        AtomicInteger stopNumber = new AtomicInteger(1);
        List<DailyScheduleDeliveryResponse> deliveries = orders.stream()
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
                        .build())
                .toList();

        DailyScheduleResponse response = DailyScheduleResponse.builder()
                .shipmentCode(shipment.getShipmentCode())
                .shipmentId(shipment.getId())
                .totalStops(orders.size())
                .totalPackages(orders.size()) // Assuming 1 package per order for now
                .totalWeight(shipment.getTotalWeight())
                .weightUnit(shipment.getWeightUnit())
                .deliveries(deliveries)
                .build();

        return ResponseEntity.ok(response);
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

        if (shipments.isEmpty()) {
            log.info("No shipments found for driver {}", driverId);
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

        // For now, use the first shipment
        Shipment shipment = shipments.get(0);
        List<Order> orders = orderRepository.findByShipmentId(shipment.getId());

        // Sort orders by estimated delivery date and shipping city
        orders.sort(Comparator
                .comparing((Order o) -> o.getEstimatedDeliveryDate() != null ? o.getEstimatedDeliveryDate() : java.time.LocalDate.MAX)
                .thenComparing(o -> o.getShippingCity() != null ? o.getShippingCity() : ""));

        // Calculate statistics
        long completed = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .count();

        long remaining = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.DELIVERED)
                .count();

        // Get next 3 deliveries that are pending
        AtomicInteger stopNumber = new AtomicInteger(1);
        List<DriverDashboardResponse.NextDeliveryPreview> nextDeliveries = orders.stream()
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
                .totalDeliveries(orders.size())
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

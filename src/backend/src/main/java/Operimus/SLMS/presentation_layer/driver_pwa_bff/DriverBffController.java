package Operimus.SLMS.presentation_layer.driver_pwa_bff;

import Operimus.SLMS.business_layer.order_module.service.OrderService;
import Operimus.SLMS.presentation_layer.driver_pwa_bff.dto.DriverOrderScanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

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

    private String extractDriverIdentifier(Jwt principal) {
        if (principal == null) {
            return null;
        }
        if (StringUtils.hasText(principal.getSubject())) {
            return principal.getSubject();
        }
        String preferredUsername = principal.getClaimAsString("preferred_username");
        if (StringUtils.hasText(preferredUsername)) {
            return preferredUsername;
        }
        return principal.getClaimAsString("email");
    }
}

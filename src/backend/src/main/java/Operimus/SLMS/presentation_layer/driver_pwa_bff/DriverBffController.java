package Operimus.SLMS.presentation_layer.driver_bff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
}

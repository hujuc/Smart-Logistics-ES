package Operimus.SLMS.presentation_layer.admin_console_bff.logistics_bff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Logistics BFF Controller - Admin Console
 *
 * This controller is part of the Backend-For-Frontend (BFF) pattern for the logistics manager interface.
 * Currently contains only health endpoint - will be expanded when logistics frontend is connected.
 *
 * Endpoints are mapped to /api/logistics/* to match the logistics manager role permissions.
 */
@Slf4j
@RestController
@RequestMapping("/api/logistics")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost", "http://localhost:3000"}, allowCredentials = "true")
public class LogisticsBffController {

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
}

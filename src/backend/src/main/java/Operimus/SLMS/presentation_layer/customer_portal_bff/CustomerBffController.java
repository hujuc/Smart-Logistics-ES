package Operimus.SLMS.presentation_layer.customer_portal_bff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}

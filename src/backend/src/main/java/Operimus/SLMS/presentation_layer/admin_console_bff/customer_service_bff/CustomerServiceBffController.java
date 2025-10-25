package Operimus.SLMS.presentation_layer.admin_console_bff.customer_service_bff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Customer Service BFF Controller
 *
 * This controller is part of the Backend-For-Frontend (BFF) pattern for the customer service interface.
 * It exposes customer-service-specific endpoints that delegate to business layer services.
 *
 * Endpoints are mapped to /api/customer-service/* to match the customer service rep role permissions.
 */
@Slf4j
@RestController
@RequestMapping("/api/customer-service")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost", "http://localhost:3000"}, allowCredentials = "true")
public class CustomerServiceBffController {

    /**
     * Health check endpoint
     *
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<java.util.Map<String, String>> health() {
        return ResponseEntity.ok(java.util.Map.of(
                "status", "UP",
                "service", "Customer Service BFF",
                "timestamp", java.time.Instant.now().toString()
        ));
    }
}

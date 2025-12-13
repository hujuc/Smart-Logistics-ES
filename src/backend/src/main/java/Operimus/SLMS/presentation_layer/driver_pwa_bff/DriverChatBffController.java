package Operimus.SLMS.presentation_layer.driver_pwa_bff;

import Operimus.SLMS.business_layer.chatbot_module.dto.ChatRequest;
import Operimus.SLMS.business_layer.chatbot_module.dto.ChatResponse;
import Operimus.SLMS.business_layer.chatbot_module.service.ChatbotService;
import Operimus.SLMS.presentation_layer.shared.JwtSecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Driver PWA Chatbot BFF Controller
 *
 * Provides chatbot functionality for authenticated drivers.
 * The chatbot provides driver-specific information about routes, deliveries,
 * and procedures.
 *
 * Endpoints are mapped to /api/driver/chat
 */
@Slf4j
@RestController
@RequestMapping("/api/driver/chat")
@RequiredArgsConstructor
public class DriverChatBffController {

    private final ChatbotService chatbotService;
    private static final String FRONTEND = "driver-pwa";

    /**
     * Chat endpoint for driver PWA.
     * Requires DRIVER role authentication.
     *
     * POST /api/driver/chat
     *
     * @param request The chat request containing user message and conversation
     *                history
     * @param jwt     JWT token with driver information (required)
     * @return ChatResponse with the assistant's reply and source documents
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        // Extract role from JWT (should be "DRIVER")
        String role = JwtSecurityUtils.extractPrimaryRole(jwt);
        String driverId = JwtSecurityUtils.extractUserId(jwt);

        log.info("Driver PWA chat request - driverId: {}, role: {}, message length: {}",
                driverId, role, request.message().length());

        try {
            // Generate chatbot response with RAG context and tool calling
            ChatResponse response = chatbotService.generateResponse(
                    request,
                    FRONTEND,
                    role,
                    driverId // Pass driverId for tool authorization
            );

            log.debug("Chat response generated with {} source documents",
                    response.sourceDocuments().size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing chat request for driver {}: {}", driverId, e.getMessage(), e);
            throw e;
        }
    }
}

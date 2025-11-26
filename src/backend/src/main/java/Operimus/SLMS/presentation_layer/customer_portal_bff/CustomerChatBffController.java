package Operimus.SLMS.presentation_layer.customer_portal_bff;

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
 * Customer Portal Chatbot BFF Controller
 *
 * Provides chatbot functionality for authenticated customers.
 * The chatbot provides customer-specific information about orders, tracking, and support.
 *
 * Endpoints are mapped to /api/customer/chat
 */
@Slf4j
@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost", "http://localhost:3000"}, allowCredentials = "true")
public class CustomerChatBffController {

    private final ChatbotService chatbotService;
    private static final String FRONTEND = "customer-portal";

    /**
     * Chat endpoint for customer portal.
     * Requires CUSTOMER role authentication.
     *
     * POST /api/customer/chat
     *
     * @param request The chat request containing user message and conversation history
     * @param jwt JWT token with customer information (required)
     * @return ChatResponse with the assistant's reply and source documents
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        // Extract role from JWT (should be "CUSTOMER")
        String role = JwtSecurityUtils.extractPrimaryRole(jwt);
        String userId = JwtSecurityUtils.extractUserId(jwt);

        log.info("Customer portal chat request - userId: {}, role: {}, message length: {}",
                userId, role, request.message().length());

        try {
            // Generate chatbot response with RAG context
            ChatResponse response = chatbotService.generateResponse(
                    request,
                    FRONTEND,
                    role
            );

            log.debug("Chat response generated with {} source documents",
                    response.sourceDocuments().size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing chat request for customer {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
}

package Operimus.SLMS.presentation_layer.admin_console_bff.warehouse_bff;

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
 * Warehouse Staff Chatbot BFF Controller (Admin Console)
 *
 * Provides chatbot functionality for warehouse staff in the admin console.
 * The chatbot provides information about warehouse operations, inventory, and procedures.
 *
 * Endpoints are mapped to /api/warehouse/chat
 */
@Slf4j
@RestController
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost", "http://localhost:3000"}, allowCredentials = "true")
public class WarehouseChatBffController {

    private final ChatbotService chatbotService;
    private static final String FRONTEND = "admin-console";

    /**
     * Chat endpoint for warehouse staff.
     * Requires WAREHOUSE_STAFF role authentication.
     *
     * POST /api/warehouse/chat
     *
     * @param request The chat request containing user message and conversation history
     * @param jwt JWT token with warehouse staff information (required)
     * @return ChatResponse with the assistant's reply and source documents
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        // Extract role from JWT (should be "WAREHOUSE_STAFF")
        String role = JwtSecurityUtils.extractPrimaryRole(jwt);
        String userId = JwtSecurityUtils.extractUserId(jwt);

        log.info("Warehouse staff chat request - userId: {}, role: {}, message length: {}",
                userId, role, request.message().length());

        try {
            // Generate chatbot response with RAG context and tool calling
            ChatResponse response = chatbotService.generateResponse(
                    request,
                    FRONTEND,
                    role,
                    userId  // Pass userId for tool authorization
            );

            log.debug("Chat response generated with {} source documents",
                    response.sourceDocuments().size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing chat request for warehouse staff {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
}

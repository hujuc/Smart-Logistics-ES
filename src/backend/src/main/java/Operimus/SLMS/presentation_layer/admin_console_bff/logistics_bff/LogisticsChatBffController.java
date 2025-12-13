package Operimus.SLMS.presentation_layer.admin_console_bff.logistics_bff;

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
 * Logistics Manager Chatbot BFF Controller (Admin Console)
 *
 * Provides chatbot functionality for logistics managers in the admin console.
 * The chatbot provides information about operations, reporting, user
 * management, and logistics procedures.
 *
 * Endpoints are mapped to /api/logistics/chat
 */
@Slf4j
@RestController
@RequestMapping("/api/logistics/chat")
@RequiredArgsConstructor
public class LogisticsChatBffController {

    private final ChatbotService chatbotService;
    private static final String FRONTEND = "admin-console";

    /**
     * Chat endpoint for logistics managers.
     * Requires LOGISTICS_MANAGER role authentication.
     *
     * POST /api/logistics/chat
     *
     * @param request The chat request containing user message and conversation
     *                history
     * @param jwt     JWT token with logistics manager information (required)
     * @return ChatResponse with the assistant's reply and source documents
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        // Extract role from JWT (should be "LOGISTICS_MANAGER")
        String role = JwtSecurityUtils.extractPrimaryRole(jwt);
        String userId = JwtSecurityUtils.extractUserId(jwt);

        log.info("Logistics manager chat request - userId: {}, role: {}, message length: {}",
                userId, role, request.message().length());

        try {
            // Generate chatbot response with RAG context and tool calling
            ChatResponse response = chatbotService.generateResponse(
                    request,
                    FRONTEND,
                    role,
                    userId // Pass userId for tool authorization
            );

            log.debug("Chat response generated with {} source documents",
                    response.sourceDocuments().size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing chat request for logistics manager {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
}

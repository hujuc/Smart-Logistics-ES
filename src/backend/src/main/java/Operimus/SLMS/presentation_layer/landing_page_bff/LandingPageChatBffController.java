package Operimus.SLMS.presentation_layer.landing_page_bff;

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
 * Landing Page Chatbot BFF Controller
 *
 * Provides chatbot functionality for the landing page (public access).
 * Users can interact with the chatbot without authentication.
 *
 * Endpoints are mapped to /api/landing-page/chat
 */
@Slf4j
@RestController
@RequestMapping("/api/landing-page/chat")
@RequiredArgsConstructor
public class LandingPageChatBffController {

    private final ChatbotService chatbotService;
    private static final String FRONTEND = "landing-page";

    /**
     * Chat endpoint for landing page visitors.
     * No authentication required - accessible to anonymous users.
     *
     * POST /api/landing-page/chat
     *
     * @param request The chat request containing user message and conversation
     *                history
     * @param jwt     Optional JWT token (null for anonymous users)
     * @return ChatResponse with the assistant's reply and source documents
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        // Extract role (will be "ANONYMOUS" for landing page users without JWT)
        String role = JwtSecurityUtils.extractPrimaryRole(jwt);

        log.info("Landing page chat request - role: {}, message length: {}",
                role, request.message().length());

        try {
            // Generate chatbot response with RAG context (anonymous - no userId)
            ChatResponse response = chatbotService.generateResponse(
                    request,
                    FRONTEND,
                    role,
                    null // Landing page is anonymous, no userId for tool authorization
            );

            log.debug("Chat response generated with {} source documents",
                    response.sourceDocuments().size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing chat request on landing page: {}", e.getMessage(), e);
            throw e;
        }
    }
}

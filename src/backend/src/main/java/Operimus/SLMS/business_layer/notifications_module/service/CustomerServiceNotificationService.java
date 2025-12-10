package Operimus.SLMS.business_layer.notifications_module.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service to send notifications to customer service representatives about tickets and support requests
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceNotificationService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final KeycloakUserService userService;

    @Value("${kafka.topic.exception-events:exception-events}")
    private String exceptionEventsTopic;

    /**
     * Notify when a new ticket/exception is created (High Priority or Critical)
     */
    public void notifyNewHighPriorityTicket(String exceptionId, String ticketNumber, String priority, String description) {
        log.info("🎫 Notifying CSRs about new high priority ticket: {} (Priority: {})", ticketNumber, priority);
        String message = String.format("New %s priority ticket %s requires attention: %s", 
            priority, ticketNumber, truncateDescription(description));
        publishNotificationToCustomerService("TICKET", exceptionId, "NEW_HIGH_PRIORITY_TICKET", message);
    }

    /**
     * Notify specific CSR when a ticket is assigned to them
     */
    public void notifyTicketAssigned(String exceptionId, String ticketNumber, String assignedToId, String customerName) {
        log.info("👤 Notifying CSR {} about ticket assignment: {}", assignedToId, ticketNumber);
        String message = String.format("Ticket %s from %s has been assigned to you", ticketNumber, customerName);
        publishNotificationToSpecificUser("TICKET", exceptionId, "TICKET_ASSIGNED", message, assignedToId);
    }

    /**
     * Notify when a ticket is escalated (becomes critical or overdue)
     */
    public void notifyTicketEscalated(String exceptionId, String ticketNumber, String reason) {
        log.info("⚠️ Notifying CSRs about ticket escalation: {}", ticketNumber);
        String message = String.format("ESCALATED: Ticket %s - %s", ticketNumber, reason);
        publishNotificationToCustomerService("TICKET", exceptionId, "TICKET_ESCALATED", message);
    }

    /**
     * Notify when SLA deadline is approaching (e.g., 2 hours before)
     */
    public void notifySlaWarning(String exceptionId, String ticketNumber, String assignedToId, int hoursRemaining) {
        log.info("⏰ Notifying about SLA warning for ticket: {} ({} hours remaining)", ticketNumber, hoursRemaining);
        String message = String.format("SLA WARNING: Ticket %s must be resolved in %d hours", ticketNumber, hoursRemaining);
        
        if (assignedToId != null) {
            // Notify the assigned CSR
            publishNotificationToSpecificUser("TICKET", exceptionId, "SLA_WARNING", message, assignedToId);
        } else {
            // Notify all CSRs if unassigned
            publishNotificationToCustomerService("TICKET", exceptionId, "SLA_WARNING", message);
        }
    }

    /**
     * Notify when SLA has been breached
     */
    public void notifySlaBreached(String exceptionId, String ticketNumber, String assignedToId) {
        log.info("🚨 Notifying about SLA breach for ticket: {}", ticketNumber);
        String message = String.format("SLA BREACHED: Ticket %s has exceeded resolution deadline", ticketNumber);
        
        if (assignedToId != null) {
            publishNotificationToSpecificUser("TICKET", exceptionId, "SLA_BREACH", message, assignedToId);
        } else {
            publishNotificationToCustomerService("TICKET", exceptionId, "SLA_BREACH", message);
        }
    }

    /**
     * Notify when a customer adds feedback/rating to a resolved ticket
     */
    public void notifyCustomerFeedback(String exceptionId, String ticketNumber, String assignedToId, int rating) {
        log.info("⭐ Notifying about customer feedback on ticket: {} (Rating: {})", ticketNumber, rating);
        String emoji = rating >= 4 ? "😊" : rating >= 3 ? "😐" : "😞";
        String message = String.format("Customer feedback received for ticket %s: %d/5 %s", ticketNumber, rating, emoji);
        
        if (assignedToId != null) {
            publishNotificationToSpecificUser("TICKET", exceptionId, "CUSTOMER_FEEDBACK", message, assignedToId);
        }
    }

    /**
     * Notify when a ticket is successfully resolved
     */
    public void notifyTicketResolved(String exceptionId, String ticketNumber, String assignedToId, String customerName) {
        log.info("✅ Notifying about ticket resolution: {}", ticketNumber);
        String message = String.format("Ticket %s for %s has been successfully resolved", ticketNumber, customerName);
        
        if (assignedToId != null) {
            publishNotificationToSpecificUser("TICKET", exceptionId, "TICKET_RESOLVED", message, assignedToId);
        }
    }

    /**
     * Helper: Truncate description to max 50 characters for notifications
     */
    private String truncateDescription(String description) {
        if (description == null) return "";
        return description.length() > 50 ? description.substring(0, 47) + "..." : description;
    }

    /**
     * Publish notification to all Customer Service Representatives
     */
    private void publishNotificationToCustomerService(String aggregateType, String aggregateId, String eventType, String message) {
        try {
            log.info("📤 Publishing notification to all CSRs - Type: {}, Event: {}, Message: {}", 
                aggregateType, eventType, message);
            
            Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent event =
                    Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent.builder()
                            .eventId(UUID.randomUUID().toString())
                            .aggregateId(aggregateId)
                            .aggregateType(aggregateType)
                            .eventType(eventType)
                            .message(message)
                            .customerId("CUSTOMER_SERVICE_REP") // Special marker for all CSRs
                            .occurredAt(OffsetDateTime.now())
                            .build();

            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(exceptionEventsTopic, aggregateId, payload);
            
            log.info("✅ Published notification to all CSRs: {} to topic: {}", eventType, exceptionEventsTopic);
        } catch (Exception e) {
            log.error("❌ Failed to publish CSR notification", e);
        }
    }

    /**
     * Publish notification to a specific user (CSR)
     */
    private void publishNotificationToSpecificUser(String aggregateType, String aggregateId, String eventType, String message, String userId) {
        try {
            log.info("📤 Publishing notification to specific user {} - Type: {}, Event: {}", 
                userId, aggregateType, eventType);
            
            // Get username from Keycloak
            String username = getUsernameFromId(userId);
            
            Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent event =
                    Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent.builder()
                            .eventId(UUID.randomUUID().toString())
                            .aggregateId(aggregateId)
                            .aggregateType(aggregateType)
                            .eventType(eventType)
                            .message(message)
                            .customerId(username) // Use username for specific user
                            .occurredAt(OffsetDateTime.now())
                            .build();

            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(exceptionEventsTopic, aggregateId, payload);
            
            log.info("✅ Published notification to user {}: {}", username, eventType);
        } catch (Exception e) {
            log.error("❌ Failed to publish notification to user {}", userId, e);
        }
    }

    /**
     * Get username from Keycloak user ID
     */
    private String getUsernameFromId(String userId) {
        try {
            UserRepresentation user = userService.getUserById(userId);
            return user.getUsername();
        } catch (Exception e) {
            log.warn("⚠️ Failed to fetch username for user ID {}: {}", userId, e.getMessage());
            return userId; // Fallback to ID if can't get username
        }
    }
}

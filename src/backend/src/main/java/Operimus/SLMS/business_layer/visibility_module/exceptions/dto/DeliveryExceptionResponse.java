package Operimus.SLMS.business_layer.visibility_module.exceptions.dto;

import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for delivery exception
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryExceptionResponse {

    private UUID id;
    private Long orderId;
    
    // Exception details
    private ExceptionType type;
    private String category;
    private Severity severity;
    private String description;
    
    // Source
    private Source source;
    private UUID reportedBy;
    private LocalDateTime reportedAt;
    
    // Customer specifics
    private Boolean customerReported;
    private UUID customerId;
    
    // Assignment & Status
    private ExceptionStatus status;
    private UUID assignedTo;
    private Priority priority;
    
    // Resolution
    private String resolutionNotes;
    private String resolutionType;
    private UUID resolvedBy;
    private LocalDateTime resolvedAt;
    
    // SLA
    private LocalDateTime slaDeadline;
    private Boolean slaBreached;
    
    // Customer feedback
    private Integer customerFeedback;
    private String feedbackComment;
    
    // Audit
    private LocalDateTime createdAt;  // Alias for reportedAt for frontend compatibility
    private LocalDateTime updatedAt;
    private String history;
}

package Operimus.SLMS.business_layer.visibility_module.exceptions.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Delivery Exception Entity
 * 
 * Tracks and manages all delivery problems and issues reported by customers,
 * drivers, or detected by the system.
 */
@Entity
@Table(name = "delivery_exceptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryException {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    // Exception details
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ExceptionType type;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Severity severity = Severity.MEDIUM;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // Source
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Source source;

    @Column(name = "reported_by")
    private UUID reportedBy;

    @CreationTimestamp
    @Column(name = "reported_at", nullable = false, updatable = false)
    private LocalDateTime reportedAt;

    // Customer reported specifics
    @Column(name = "customer_reported")
    @Builder.Default
    private Boolean customerReported = false;

    @Column(name = "customer_id")
    private UUID customerId;

    // Assignment & Status
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExceptionStatus status = ExceptionStatus.OPEN;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    // Resolution
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "resolution_type", length = 50)
    private String resolutionType;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // SLA tracking
    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    @Column(name = "sla_breached")
    @Builder.Default
    private Boolean slaBreached = false;

    // Customer satisfaction
    @Column(name = "customer_feedback")
    private Integer customerFeedback;

    @Column(name = "feedback_comment", columnDefinition = "TEXT")
    private String feedbackComment;

    // Audit
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String history;

    // Enums
    public enum ExceptionType {
        DAMAGED,
        LOST,
        DELAYED,
        WRONG_ADDRESS,
        WRONG_ITEM,
        NOT_DELIVERED,
        CUSTOMER_NOT_AVAILABLE,  // Added for driver-reported exceptions
        ACCESS_DENIED,           // Added for driver-reported exceptions
        WEATHER,                 // Added for driver-reported exceptions
        OTHER
    }

    public enum ExceptionStatus {
        OPEN,
        ASSIGNED,
        IN_PROGRESS,
        RESOLVED,
        CLOSED,
        ESCALATED
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum Source {
        CUSTOMER,
        DRIVER,
        SYSTEM,
        CARRIER
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}

package Operimus.SLMS.business_layer.visibility_module.exceptions.service;

import Operimus.SLMS.business_layer.visibility_module.exceptions.dto.*;
import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException;
import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.*;
import Operimus.SLMS.business_layer.visibility_module.exceptions.repository.DeliveryExceptionRepository;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import Operimus.SLMS.business_layer.order_module.service.OrderEventService;
import Operimus.SLMS.business_layer.order_module.entities.Order;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing delivery exceptions
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DeliveryExceptionService {

    private final DeliveryExceptionRepository exceptionRepository;
    private final OrderRepository orderRepository;
    private final OrderEventService orderEventService;
    private final Operimus.SLMS.business_layer.notifications_module.service.CustomerServiceNotificationService customerServiceNotificationService;

    /**
     * Create a new delivery exception
     */
    public DeliveryExceptionResponse create(DeliveryExceptionRequest request) {
        log.info("Creating new delivery exception for order {}", request.getOrderId());

        DeliveryException exception = DeliveryException.builder()
                .orderId(request.getOrderId())
                .type(request.getType())
                .category(request.getCategory())
                .severity(request.getSeverity() != null ? request.getSeverity() : Severity.MEDIUM)
                .description(request.getDescription())
                .source(request.getSource())
                .reportedBy(request.getReportedBy())
                .customerReported(request.getCustomerReported() != null ? request.getCustomerReported() : false)
                .customerId(request.getCustomerId())
                .assignedTo(request.getAssignedTo())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .status(ExceptionStatus.OPEN)
                .build();

        // Calculate SLA deadline (24h for HIGH/CRITICAL, 48h for others)
        int hoursToAdd = (exception.getPriority() == Priority.HIGH || exception.getPriority() == Priority.CRITICAL) 
                ? 24 : 48;
        exception.setSlaDeadline(LocalDateTime.now().plusHours(hoursToAdd));

        DeliveryException saved = exceptionRepository.save(exception);
        log.info("Created delivery exception with ID: {}", saved.getId());

        // Send notifications for high priority tickets
        if (saved.getPriority() == Priority.HIGH || saved.getPriority() == Priority.CRITICAL) {
            String ticketNumber = "TKT-" + saved.getId().toString().substring(0, 8);
            customerServiceNotificationService.notifyNewHighPriorityTicket(
                saved.getId().toString(),
                ticketNumber,
                saved.getPriority().toString(),
                saved.getDescription()
            );
        }

        return mapToResponse(saved);
    }

    /**
     * Get exception by ID
     */
    @Transactional(readOnly = true)
    public DeliveryExceptionResponse getById(UUID id) {
        log.info("Fetching delivery exception with ID: {}", id);
        DeliveryException exception = findByIdOrThrow(id);
        return mapToResponse(exception);
    }

    /**
     * Get all exceptions with optional filters
     */
    @Transactional(readOnly = true)
    public List<DeliveryExceptionResponse> getAll(
            ExceptionStatus status,
            Priority priority,
            UUID assignedTo,
            Severity severity) {
        
        log.info("Fetching exceptions with filters - status: {}, priority: {}, assignedTo: {}, severity: {}", 
                status, priority, assignedTo, severity);

        List<DeliveryException> exceptions = exceptionRepository.findWithFilters(
                status, priority, assignedTo, severity);

        return exceptions.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Update an existing exception
     */
    public DeliveryExceptionResponse update(UUID id, DeliveryExceptionUpdateRequest request) {
        log.info("Updating delivery exception: {}", id);

        DeliveryException exception = findByIdOrThrow(id);
        
        // Track if assignment changed
        UUID previousAssignee = exception.getAssignedTo();
        boolean wasAssigned = false;

        if (request.getStatus() != null) {
            exception.setStatus(request.getStatus());
            
            // If resolved, set resolution timestamp
            if (request.getStatus() == ExceptionStatus.RESOLVED || 
                request.getStatus() == ExceptionStatus.CLOSED) {
                exception.setResolvedAt(LocalDateTime.now());
                
                // Check if SLA was breached
                if (exception.getSlaDeadline() != null && 
                    LocalDateTime.now().isAfter(exception.getSlaDeadline())) {
                    exception.setSlaBreached(true);
                    
                    // Notify SLA breach
                    String ticketNumber = "TKT-" + exception.getId().toString().substring(0, 8);
                    customerServiceNotificationService.notifySlaBreached(
                        exception.getId().toString(),
                        ticketNumber,
                        exception.getAssignedTo() != null ? exception.getAssignedTo().toString() : null
                    );
                }
                
                // Notify resolution
                if (request.getStatus() == ExceptionStatus.RESOLVED && exception.getAssignedTo() != null) {
                    String ticketNumber = "TKT-" + exception.getId().toString().substring(0, 8);
                    customerServiceNotificationService.notifyTicketResolved(
                        exception.getId().toString(),
                        ticketNumber,
                        exception.getAssignedTo().toString(),
                        exception.getCustomerId() != null ? exception.getCustomerId().toString() : "Customer"
                    );
                }
            }
        }

        if (request.getPriority() != null) {
            exception.setPriority(request.getPriority());
        }

        if (request.getAssignedTo() != null) {
            exception.setAssignedTo(request.getAssignedTo());
            if (exception.getStatus() == ExceptionStatus.OPEN) {
                exception.setStatus(ExceptionStatus.ASSIGNED);
            }
            wasAssigned = (previousAssignee == null || !previousAssignee.equals(request.getAssignedTo()));
        }

        if (request.getResolutionNotes() != null) {
            exception.setResolutionNotes(request.getResolutionNotes());
        }

        if (request.getResolutionType() != null) {
            exception.setResolutionType(request.getResolutionType());
        }

        if (request.getSeverity() != null) {
            exception.setSeverity(request.getSeverity());
        }

        DeliveryException updated = exceptionRepository.save(exception);
        log.info("Updated delivery exception: {}", id);

        // Send notification if ticket was newly assigned
        if (wasAssigned && updated.getAssignedTo() != null) {
            String ticketNumber = "TKT-" + updated.getId().toString().substring(0, 8);
            customerServiceNotificationService.notifyTicketAssigned(
                updated.getId().toString(),
                ticketNumber,
                updated.getAssignedTo().toString(),
                updated.getCustomerId() != null ? updated.getCustomerId().toString() : "Customer"
            );
        }

        return mapToResponse(updated);
    }

    /**
     * Close an exception
     */
    public DeliveryExceptionResponse close(UUID id, String resolutionNotes) {
        log.info("Closing delivery exception: {}", id);

        DeliveryException exception = findByIdOrThrow(id);
        exception.setStatus(ExceptionStatus.CLOSED);
        exception.setResolvedAt(LocalDateTime.now());
        
        if (resolutionNotes != null && !resolutionNotes.isBlank()) {
            exception.setResolutionNotes(resolutionNotes);
        }

        // Check SLA breach
        if (exception.getSlaDeadline() != null && 
            LocalDateTime.now().isAfter(exception.getSlaDeadline())) {
            exception.setSlaBreached(true);
        }

        // Reset order status to READY_FOR_PICKUP to restart shipping process
        try {
            Order order = orderRepository.findById(exception.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + exception.getOrderId()));
            
            OrderStatus previousStatus = order.getStatus();
            log.info("Resetting order {} status from {} to READY_FOR_PICKUP", 
                order.getId(), previousStatus);
            
            order.setStatus(OrderStatus.READY_FOR_PICKUP);
            orderRepository.save(order);
            
            // Record the status change event for shipping timeline
            orderEventService.recordStatusChange(order, previousStatus, OrderStatus.READY_FOR_PICKUP);
            
            log.info("Order {} status reset successfully and event recorded", order.getId());
        } catch (Exception e) {
            log.error("Failed to reset order status for exception {}: {}", id, e.getMessage());
            // Don't fail the exception closure if order update fails
        }

        DeliveryException closed = exceptionRepository.save(exception);
        log.info("Closed delivery exception: {}", id);

        return mapToResponse(closed);
    }

    /**
     * Get exceptions assigned to a specific CSR
     */
    @Transactional(readOnly = true)
    public List<DeliveryExceptionResponse> getMyCases(UUID csrId) {
        log.info("Fetching cases assigned to CSR: {}", csrId);
        
        List<DeliveryException> exceptions = exceptionRepository.findByAssignedTo(csrId);
        return exceptions.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get dashboard summary
     */
    @Transactional(readOnly = true)
    public ExceptionSummaryResponse getDashboardSummary() {
        log.info("Generating dashboard summary");

        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

        return ExceptionSummaryResponse.builder()
                .totalExceptions(exceptionRepository.count())
                .openExceptions(exceptionRepository.countByStatus(ExceptionStatus.OPEN))
                .inProgressExceptions(exceptionRepository.countByStatus(ExceptionStatus.IN_PROGRESS))
                .resolvedToday(exceptionRepository.countResolvedSince(today))
                .highPriorityExceptions(
                    exceptionRepository.countByPriority(Priority.HIGH) +
                    exceptionRepository.countByPriority(Priority.CRITICAL)
                )
                .slaBreachedExceptions(exceptionRepository.countBySlaBreached(true))
                .averageResolutionTimeHours(exceptionRepository.getAverageResolutionTimeHours())
                .customerSatisfactionRate(exceptionRepository.getAverageCustomerSatisfaction())
                .build();
    }

    /**
     * Get alerts (high priority and overdue cases)
     */
    @Transactional(readOnly = true)
    public List<DeliveryExceptionResponse> getAlerts() {
        log.info("Fetching alerts");

        List<DeliveryException> highPriority = exceptionRepository.findHighPriorityOpen();
        List<DeliveryException> overdue = exceptionRepository.findOverdueSla();

        // Combine and deduplicate
        List<DeliveryException> alerts = new java.util.ArrayList<>(highPriority);
        overdue.forEach(exception -> {
            if (!alerts.contains(exception)) {
                alerts.add(exception);
            }
        });

        return alerts.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get exceptions by order ID
     */
    @Transactional(readOnly = true)
    public List<DeliveryExceptionResponse> getByOrderId(Long orderId) {
        log.info("Fetching exceptions for order: {}", orderId);
        
        List<DeliveryException> exceptions = exceptionRepository.findByOrderId(orderId);
        return exceptions.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get exceptions by customer ID
     */
    @Transactional(readOnly = true)
    public List<DeliveryExceptionResponse> getByCustomerId(UUID customerId) {
        log.info("Fetching exceptions for customer: {}", customerId);
        
        List<DeliveryException> exceptions = exceptionRepository.findByCustomerId(customerId);
        return exceptions.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Submit customer feedback
     */
    public DeliveryExceptionResponse submitFeedback(UUID id, Integer rating, String comment) {
        log.info("Submitting feedback for exception: {}", id);

        DeliveryException exception = findByIdOrThrow(id);
        exception.setCustomerFeedback(rating);
        exception.setFeedbackComment(comment);

        DeliveryException updated = exceptionRepository.save(exception);
        log.info("Feedback submitted for exception: {}", id);

        return mapToResponse(updated);
    }

    /**
     * Create exception reported by driver during delivery
     */
    public DeliveryExceptionResponse createDriverException(
            Long orderId,
            ExceptionType exceptionType,
            String description,
            UUID driverId) {

        log.info("Driver {} reporting exception for order {}: {}", driverId, orderId, exceptionType);

        // Determine severity based on exception type
        Severity severity = switch (exceptionType) {
            case DAMAGED -> Severity.HIGH;
            case CUSTOMER_NOT_AVAILABLE, WRONG_ADDRESS -> Severity.MEDIUM;
            case ACCESS_DENIED, WEATHER -> Severity.MEDIUM;
            default -> Severity.MEDIUM;
        };

        // Set priority (driver-reported exceptions are typically high priority)
        Priority priority = switch (exceptionType) {
            case DAMAGED -> Priority.HIGH;
            case CUSTOMER_NOT_AVAILABLE, WRONG_ADDRESS, ACCESS_DENIED, WEATHER -> Priority.MEDIUM;
            default -> Priority.MEDIUM;
        };

        DeliveryException exception = DeliveryException.builder()
                .orderId(orderId)
                .type(exceptionType)
                .category("DRIVER_REPORTED")
                .severity(severity)
                .description(description)
                .source(Source.DRIVER)
                .reportedBy(driverId)
                .customerReported(false)
                .priority(priority)
                .status(ExceptionStatus.OPEN)
                .build();

        // Calculate SLA deadline
        int hoursToAdd = (priority == Priority.HIGH || priority == Priority.CRITICAL) ? 24 : 48;
        exception.setSlaDeadline(LocalDateTime.now().plusHours(hoursToAdd));

        DeliveryException saved = exceptionRepository.save(exception);
        log.info("Created driver-reported exception with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    // Helper methods

    private DeliveryException findByIdOrThrow(UUID id) {
        return exceptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Delivery exception %s not found".formatted(id)));
    }

    private DeliveryExceptionResponse mapToResponse(DeliveryException exception) {
        return DeliveryExceptionResponse.builder()
                .id(exception.getId())
                .orderId(exception.getOrderId())
                .type(exception.getType())
                .category(exception.getCategory())
                .severity(exception.getSeverity())
                .description(exception.getDescription())
                .source(exception.getSource())
                .reportedBy(exception.getReportedBy())
                .reportedAt(exception.getReportedAt())
                .customerReported(exception.getCustomerReported())
                .customerId(exception.getCustomerId())
                .status(exception.getStatus())
                .assignedTo(exception.getAssignedTo())
                .priority(exception.getPriority())
                .resolutionNotes(exception.getResolutionNotes())
                .resolutionType(exception.getResolutionType())
                .resolvedBy(exception.getResolvedBy())
                .resolvedAt(exception.getResolvedAt())
                .slaDeadline(exception.getSlaDeadline())
                .slaBreached(exception.getSlaBreached())
                .customerFeedback(exception.getCustomerFeedback())
                .feedbackComment(exception.getFeedbackComment())
                .createdAt(exception.getReportedAt())  // Map reportedAt to createdAt for frontend
                .updatedAt(exception.getUpdatedAt())
                .history(exception.getHistory())
                .build();
    }
}

package Operimus.SLMS.business_layer.visibility_module.exceptions.repository;

import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException;
import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for DeliveryException entity
 */
@Repository
public interface DeliveryExceptionRepository extends JpaRepository<DeliveryException, UUID> {

    // Find by order
    List<DeliveryException> findByOrderId(Long orderId);

    // Find by status
    List<DeliveryException> findByStatus(ExceptionStatus status);

    // Find by assigned CSR
    List<DeliveryException> findByAssignedTo(UUID assignedTo);

    // Find by customer
    List<DeliveryException> findByCustomerId(UUID customerId);

    // Find by priority
    List<DeliveryException> findByPriority(Priority priority);

    // Complex queries
    @Query("SELECT e FROM DeliveryException e WHERE " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:priority IS NULL OR e.priority = :priority) AND " +
           "(:assignedTo IS NULL OR e.assignedTo = :assignedTo) AND " +
           "(:severity IS NULL OR e.severity = :severity) " +
           "ORDER BY e.reportedAt DESC")
    List<DeliveryException> findWithFilters(
        @Param("status") ExceptionStatus status,
        @Param("priority") Priority priority,
        @Param("assignedTo") UUID assignedTo,
        @Param("severity") Severity severity
    );

    // Count queries for dashboard
    long countByStatus(ExceptionStatus status);

    long countByPriority(Priority priority);

    long countBySlaBreached(Boolean breached);

    @Query("SELECT COUNT(e) FROM DeliveryException e WHERE " +
           "e.status = 'RESOLVED' AND e.resolvedAt >= :startDate")
    long countResolvedSince(@Param("startDate") LocalDateTime startDate);

    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (resolved_at - reported_at)) / 3600) " +
           "FROM delivery_exceptions WHERE status = 'RESOLVED' AND resolved_at IS NOT NULL", 
           nativeQuery = true)
    Double getAverageResolutionTimeHours();

    @Query("SELECT AVG(e.customerFeedback) FROM DeliveryException e WHERE e.customerFeedback IS NOT NULL")
    Double getAverageCustomerSatisfaction();

    // High priority and overdue exceptions (alerts)
    @Query("SELECT e FROM DeliveryException e WHERE " +
           "(e.priority = 'HIGH' OR e.priority = 'CRITICAL') AND " +
           "e.status NOT IN ('RESOLVED', 'CLOSED') " +
           "ORDER BY e.priority DESC, e.reportedAt ASC")
    List<DeliveryException> findHighPriorityOpen();

    @Query("SELECT e FROM DeliveryException e WHERE " +
           "e.slaDeadline < CURRENT_TIMESTAMP AND " +
           "e.status NOT IN ('RESOLVED', 'CLOSED') " +
           "ORDER BY e.slaDeadline ASC")
    List<DeliveryException> findOverdueSla();
}

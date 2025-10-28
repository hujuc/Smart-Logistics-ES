package Operimus.SLMS.business_layer.visibility_module.exceptions.controller;

import Operimus.SLMS.business_layer.customer_support_module.dto.DeliveryExceptionRequest;
import Operimus.SLMS.business_layer.customer_support_module.dto.DeliveryExceptionResponse;
import Operimus.SLMS.business_layer.customer_support_module.dto.DeliveryExceptionUpdateRequest;
import Operimus.SLMS.business_layer.customer_support_module.dto.ExceptionSummaryResponse;
import Operimus.SLMS.business_layer.customer_support_module.entities.DeliveryException;
import Operimus.SLMS.business_layer.customer_support_module.service.DeliveryExceptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Internal admin controller for delivery exception management.
 * This controller is for internal/admin use and is accessible at /api/admin/delivery-exceptions
 */
@RestController
@RequestMapping("/api/admin/delivery-exceptions")
@RequiredArgsConstructor
public class DeliveryExceptionController {

    private final DeliveryExceptionService deliveryExceptionService;

    /**
     * Create a new delivery exception
     */
    @PostMapping
    public ResponseEntity<DeliveryExceptionResponse> createException(
            @Valid @RequestBody DeliveryExceptionRequest request) {
        DeliveryExceptionResponse response = deliveryExceptionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get exception by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeliveryExceptionResponse> getExceptionById(@PathVariable UUID id) {
        DeliveryExceptionResponse response = deliveryExceptionService.getById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all exceptions with optional filters
     */
    @GetMapping
    public ResponseEntity<List<DeliveryExceptionResponse>> getAllExceptions(
            @RequestParam(required = false) DeliveryException.ExceptionStatus status,
            @RequestParam(required = false) DeliveryException.Priority priority,
            @RequestParam(required = false) UUID assignedTo,
            @RequestParam(required = false) DeliveryException.Severity severity) {
        List<DeliveryExceptionResponse> responses = deliveryExceptionService.getAll(
                status, priority, assignedTo, severity);
        return ResponseEntity.ok(responses);
    }

    /**
     * Update an exception
     */
    @PutMapping("/{id}")
    public ResponseEntity<DeliveryExceptionResponse> updateException(
            @PathVariable UUID id,
            @Valid @RequestBody DeliveryExceptionUpdateRequest request) {
        DeliveryExceptionResponse response = deliveryExceptionService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Close an exception
     */
    @PostMapping("/{id}/close")
    public ResponseEntity<DeliveryExceptionResponse> closeException(
            @PathVariable UUID id,
            @RequestParam(required = false) String resolutionNotes) {
        DeliveryExceptionResponse response = deliveryExceptionService.close(id, resolutionNotes);
        return ResponseEntity.ok(response);
    }

    /**
     * Get dashboard summary
     */
    @GetMapping("/dashboard/summary")
    public ResponseEntity<ExceptionSummaryResponse> getDashboardSummary() {
        ExceptionSummaryResponse summary = deliveryExceptionService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Get critical alerts
     */
    @GetMapping("/dashboard/alerts")
    public ResponseEntity<List<DeliveryExceptionResponse>> getCriticalAlerts() {
        List<DeliveryExceptionResponse> alerts = deliveryExceptionService.getAlerts();
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get exceptions for a specific order
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<DeliveryExceptionResponse>> getExceptionsByOrderId(
            @PathVariable Long orderId) {
        List<DeliveryExceptionResponse> exceptions = deliveryExceptionService.getByOrderId(orderId);
        return ResponseEntity.ok(exceptions);
    }

    /**
     * Get exceptions assigned to a specific CSR
     */
    @GetMapping("/assigned/{csrId}")
    public ResponseEntity<List<DeliveryExceptionResponse>> getExceptionsByCsrId(
            @PathVariable UUID csrId) {
        List<DeliveryExceptionResponse> exceptions = deliveryExceptionService.getMyCases(csrId);
        return ResponseEntity.ok(exceptions);
    }

    /**
     * Submit customer feedback for an exception
     */
    @PostMapping("/{id}/feedback")
    public ResponseEntity<DeliveryExceptionResponse> submitFeedback(
            @PathVariable UUID id,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment) {
        DeliveryExceptionResponse response = deliveryExceptionService.submitFeedback(id, rating, comment);
        return ResponseEntity.ok(response);
    }
}

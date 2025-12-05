package Operimus.SLMS.business_layer.order_module.service;

import Operimus.SLMS.business_layer.order_module.dto.OrderEventResponse;
import Operimus.SLMS.business_layer.order_module.entities.Order;
import Operimus.SLMS.business_layer.order_module.entities.OrderEvent;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.repository.OrderEventRepository;
import Operimus.SLMS.business_layer.order_module.repository.OrderRepository;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderEventService {

    private final org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final OrderEventRepository orderEventRepository;
    private final OrderRepository orderRepository;

    @org.springframework.beans.factory.annotation.Value("${kafka.topic.order-events}")
    private String orderEventsTopic;

    public void recordStatusChange(
            Order order, OrderStatus previousStatus, OrderStatus newStatus) {
        if (newStatus == null) {
            return;
        }

        String notes = previousStatus == null
                ? "Order initialized with status %s".formatted(newStatus)
                : "Order status changed from %s to %s".formatted(previousStatus, newStatus);

        OrderEvent event = OrderEvent.builder()
                .order(order)
                .eventType(newStatus)
                .notes(notes)
                .build();

        orderEventRepository.save(event);

        // Publish to Kafka
        publishNotificationEvent(order, newStatus, notes);
    }

    private void publishNotificationEvent(Order order, OrderStatus status, String message) {
        try {
            Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent notificationEvent =
                    Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent.builder()
                            .eventId(java.util.UUID.randomUUID().toString())
                            .aggregateId(order.getId().toString())
                            .aggregateType("ORDER")
                            .eventType(status.name())
                            .message(message)
                            .customerId(order.getCustomerId())
                            .occurredAt(java.time.OffsetDateTime.now())
                            .build();

            String payload = objectMapper.writeValueAsString(notificationEvent);
            kafkaTemplate.send(orderEventsTopic, order.getId().toString(), payload);
        } catch (Exception e) {
            // Log error but don't fail the transaction
            // In a real system, we might want to use an outbox pattern here
            System.err.println("Failed to publish order event to Kafka: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<OrderEventResponse> getEventsForOrder(Long orderId) {
        ensureOrderExists(orderId);
        return orderEventRepository.findByOrderIdOrderByOccurredAtAsc(orderId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderEventResponse getLatestEventForOrder(Long orderId) {
        ensureOrderExists(orderId);
        return orderEventRepository
                .findFirstByOrderIdOrderByOccurredAtDesc(orderId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No events recorded for order %d".formatted(orderId)));
    }

    /**
     * Delete all events associated with an order
     * Used when deleting an order to avoid foreign key constraint violations
     *
     * @param orderId Order ID
     */
    public void deleteByOrderId(Long orderId) {
        List<OrderEvent> events = orderEventRepository.findByOrderIdOrderByOccurredAtAsc(orderId);
        orderEventRepository.deleteAll(events);
    }

    private void ensureOrderExists(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Order %d not found".formatted(orderId));
        }
    }

    private OrderEventResponse mapToResponse(OrderEvent event) {
        return OrderEventResponse.builder()
                .id(event.getId())
                .orderId(event.getOrder().getId())
                .eventType(event.getEventType())
                .occurredAt(event.getOccurredAt())
                .notes(event.getNotes())
                .build();
    }
}

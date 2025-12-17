package Operimus.SLMS.business_layer.rate_carrier_module.service;

import Operimus.SLMS.business_layer.rate_carrier_module.entities.Carrier;
import Operimus.SLMS.business_layer.order_module.entities.Order;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Shipment;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.ShipmentStatus;
import Operimus.SLMS.business_layer.order_module.repository.OrderRepository;
import Operimus.SLMS.business_layer.rate_carrier_module.repository.ShipmentRepository;
import Operimus.SLMS.business_layer.shared.exception.ResourceConflictException;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final CarrierService carrierService;
    private final ObjectProvider<ShipmentLabelGenerationService> shipmentLabelGenerationService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Operimus.SLMS.business_layer.notifications_module.service.LogisticsManagerNotificationService logisticsManagerNotificationService;
    private final Operimus.SLMS.business_layer.order_module.service.OrderEventService orderEventService;
    private final Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService keycloakUserService;

    @Value("${kafka.topic.shipment-events}")
    private String shipmentEventsTopic;

    @Transactional
    public Shipment create(String shipmentCode, Long carrierId, String driverId, List<Long> orderIds) {
        log.info("[ShipmentService] Creating shipment with code: {}, carrierId: {}, driverId: {}, orderIds: {}",
                shipmentCode, carrierId, driverId, orderIds);

        if (shipmentRepository.existsByShipmentCodeIgnoreCase(shipmentCode)) {
            throw new ResourceConflictException(
                    "A shipment with code '%s' already exists.".formatted(shipmentCode));
        }

        if (driverId == null || driverId.isBlank()) {
            throw new IllegalArgumentException("Driver ID is required when creating a shipment");
        }

        log.info("[ShipmentService] Looking for carrier with ID: {}", carrierId);
        Carrier carrier = carrierService.findEntityById(carrierId);
        log.info("[ShipmentService] Found carrier: id={}, name={}, code={}",
                carrier.getId(), carrier.getName(), carrier.getCode());

        // Fetch orders and validate they're available for shipment
        List<Order> orders = orderRepository.findAllById(orderIds);
        if (orders.size() != orderIds.size()) {
            throw new ResourceNotFoundException("Some orders were not found");
        }

        // Validate all orders are in CREATED status
        for (Order order : orders) {
            if (order.getStatus() != OrderStatus.CREATED) {
                throw new ResourceConflictException(
                        "Order %s is not available for shipment (status: %s)".formatted(
                                order.getReference(), order.getStatus()));
            }
            if (order.getShipmentId() != null) {
                throw new ResourceConflictException(
                        "Order %s is already assigned to shipment with ID %d".formatted(
                                order.getReference(), order.getShipmentId()));
            }
        }

        // Calculate total weight
        double totalWeight = orders.stream()
                .mapToDouble(order -> order.getWeight() != null ? order.getWeight() : 0.0)
                .sum();

        // Create shipment with carrier AND driver
        Shipment shipment = Shipment.builder()
                .shipmentCode(shipmentCode)
                .carrier(carrier)
                .driverId(driverId)
                .totalOrders(orders.size())
                .totalWeight(totalWeight)
                .weightUnit(orders.isEmpty() ? null : orders.get(0).getWeightUnit())
                .build();

        Shipment saved = shipmentRepository.save(shipment);

        // Assign orders to shipment
        for (Order order : orders) {
            OrderStatus oldStatus = order.getStatus();
            order.setShipmentId(saved.getId());
            order.setStatus(OrderStatus.ASSIGNED_TO_SHIPMENT);
            orderEventService.recordStatusChange(order, oldStatus, OrderStatus.ASSIGNED_TO_SHIPMENT);
        }
        orderRepository.saveAll(orders);

        triggerShipmentDocuments(saved, orders);

        // Notify logistics managers about new shipment
        logisticsManagerNotificationService.notifyNewShipmentCreated(
                saved.getId().toString(),
                saved.getShipmentCode(),
                orders.size());

        // Notify driver if assigned
        if (driverId != null && !driverId.isBlank()) {
            publishDriverAssignmentEvent(saved, driverId, null);
        }

        return saved;
    }

    @Transactional
    public Shipment updateDriver(Long shipmentId, String newDriverId) {
        Shipment shipment = shipmentRepository
                .findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment %d not found".formatted(shipmentId)));

        if (newDriverId == null || newDriverId.isBlank()) {
            throw new IllegalArgumentException("Driver ID cannot be empty");
        }

        String oldDriverId = shipment.getDriverId();
        shipment.setDriverId(newDriverId);

        // Publish driver assignment event
        publishDriverAssignmentEvent(shipment, newDriverId, oldDriverId);

        return shipment;
    }

    @Transactional(readOnly = true)
    public Shipment getById(Long id) {
        return shipmentRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment %d not found".formatted(id)));
    }

    @Transactional(readOnly = true)
    public List<Shipment> getAll() {
        return shipmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Shipment> getByCarrierId(Long carrierId) {
        return shipmentRepository.findAll().stream()
                .filter(shipment -> shipment.getCarrier().getId().equals(carrierId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Shipment> getByDriverId(String driverId) {
        return shipmentRepository.findAll().stream()
                .filter(shipment -> driverId.equals(shipment.getDriverId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Shipment findEntityById(Long id) {
        return shipmentRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment %d not found".formatted(id)));
    }

    /**
     * Driver accepts a shipment, transitioning it to ACCEPTED status and then to
     * IN_TRANSIT.
     * All orders in the shipment are moved to IN_TRANSIT status.
     */
    @Transactional
    public Shipment acceptShipment(Long shipmentId, String driverId) {
        log.info("[ShipmentService] Driver {} accepting shipment {}", driverId, shipmentId);

        Shipment shipment = shipmentRepository
                .findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment %d not found".formatted(shipmentId)));

        // Verify the driver is assigned to this shipment
        if (!driverId.equals(shipment.getDriverId())) {
            throw new ResourceConflictException(
                    "Driver %s is not assigned to shipment %d".formatted(driverId, shipmentId));
        }

        // Verify shipment is in PENDING_ACCEPTANCE status
        if (shipment.getStatus() != ShipmentStatus.PENDING_ACCEPTANCE) {
            throw new ResourceConflictException(
                    "Shipment %d cannot be accepted (current status: %s)".formatted(shipmentId, shipment.getStatus()));
        }

        // Update shipment status to ACCEPTED, then to IN_TRANSIT
        shipment.setStatus(ShipmentStatus.ACCEPTED);
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        shipment = shipmentRepository.save(shipment);

        // Update all orders in shipment to IN_TRANSIT
        List<Order> orders = orderRepository.findByShipmentId(shipmentId);
        for (Order order : orders) {
            OrderStatus oldStatus = order.getStatus();
            order.setStatus(OrderStatus.IN_TRANSIT);
            orderEventService.recordStatusChange(order, oldStatus, OrderStatus.IN_TRANSIT);
        }
        orderRepository.saveAll(orders);

        log.info("[ShipmentService] Shipment {} accepted and moved to IN_TRANSIT with {} orders",
                shipmentId, orders.size());

        return shipment;
    }

    /**
     * Driver completes delivery successfully.
     * All orders are marked as DELIVERED and shipment status is set to COMPLETED.
     */
    @Transactional
    public Shipment completeDelivery(Long shipmentId, String driverId) {
        log.info("[ShipmentService] Driver {} completing delivery for shipment {}", driverId, shipmentId);

        Shipment shipment = shipmentRepository
                .findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment %d not found".formatted(shipmentId)));

        // Verify the driver is assigned to this shipment
        if (!driverId.equals(shipment.getDriverId())) {
            throw new ResourceConflictException(
                    "Driver %s is not assigned to shipment %d".formatted(driverId, shipmentId));
        }

        // Verify shipment is in IN_TRANSIT status
        if (shipment.getStatus() != ShipmentStatus.IN_TRANSIT) {
            throw new ResourceConflictException(
                    "Shipment %d is not in transit (current status: %s)".formatted(shipmentId, shipment.getStatus()));
        }

        // Update all orders to DELIVERED
        List<Order> orders = orderRepository.findByShipmentId(shipmentId);
        for (Order order : orders) {
            OrderStatus oldStatus = order.getStatus();
            order.setStatus(OrderStatus.DELIVERED);
            orderEventService.recordStatusChange(order, oldStatus, OrderStatus.DELIVERED);
        }
        orderRepository.saveAll(orders);

        // Update shipment status to COMPLETED
        shipment.setStatus(ShipmentStatus.COMPLETED);
        shipment = shipmentRepository.save(shipment);

        log.info("[ShipmentService] Shipment {} completed successfully with {} orders",
                shipmentId, orders.size());

        return shipment;
    }

    /**
     * Driver reports a delivery exception.
     * Shipment status is set to EXCEPTION.
     * Orders remain in IN_TRANSIT status until CSR resolves the exception.
     */
    @Transactional
    public Shipment reportDeliveryException(Long shipmentId, String driverId) {
        log.info("[ShipmentService] Driver {} reporting exception for shipment {}", driverId, shipmentId);

        Shipment shipment = shipmentRepository
                .findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment %d not found".formatted(shipmentId)));

        // Verify the driver is assigned to this shipment
        if (!driverId.equals(shipment.getDriverId())) {
            throw new ResourceConflictException(
                    "Driver %s is not assigned to shipment %d".formatted(driverId, shipmentId));
        }

        // Verify shipment is in IN_TRANSIT status
        if (shipment.getStatus() != ShipmentStatus.IN_TRANSIT) {
            throw new ResourceConflictException(
                    "Shipment %d is not in transit (current status: %s)".formatted(shipmentId, shipment.getStatus()));
        }

        // Update shipment status to EXCEPTION
        shipment.setStatus(ShipmentStatus.EXCEPTION);
        shipment = shipmentRepository.save(shipment);

        log.info("[ShipmentService] Shipment {} marked as EXCEPTION", shipmentId);

        return shipment;
    }

    private void triggerShipmentDocuments(Shipment shipment, List<Order> orders) {
        ShipmentLabelGenerationService generator = shipmentLabelGenerationService.getIfAvailable();
        if (generator != null) {
            generator.generateCarrierDocuments(shipment, orders);
        }

        // Publish creation event
        publishNotificationEvent(shipment, "CREATED", "Shipment created with code " + shipment.getShipmentCode());
    }

    private String resolveDriverUsername(String driverId) {
        if (driverId == null || driverId.isBlank()) {
            return driverId;
        }

        // Check if it looks like a UUID
        // Regex for UUID: 8-4-4-4-12 hex digits
        if (driverId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            try {
                log.info("[ShipmentService] Resolving driver UUID {} to username", driverId);
                org.keycloak.representations.idm.UserRepresentation user = keycloakUserService.getUserById(driverId);
                if (user != null && user.getUsername() != null) {
                    log.info("[ShipmentService] Resolved driver UUID {} to username {}", driverId, user.getUsername());
                    return user.getUsername();
                }
            } catch (Exception e) {
                log.warn("[ShipmentService] Failed to resolve driver UUID {} to username: {}", driverId, e.getMessage());
                // Fallback to original ID if resolution fails
            }
        }
        
        return driverId;
    }

    private void publishDriverAssignmentEvent(Shipment shipment, String newDriverId, String oldDriverId) {
        String message = String.format("You have been assigned to shipment %s", shipment.getShipmentCode());

        // Ensure we are sending to the username, not UUID
        String driverUsername = resolveDriverUsername(newDriverId);

        try {
            Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent notificationEvent = Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent
                    .builder()
                    .eventId(java.util.UUID.randomUUID().toString())
                    .aggregateId(shipment.getId().toString())
                    .aggregateType("SHIPMENT")
                    .eventType("DRIVER_ASSIGNED")
                    .message(message)
                    .customerId(driverUsername) // Send notification to the driver username
                    .occurredAt(java.time.OffsetDateTime.now())
                    .build();

            String payload = objectMapper.writeValueAsString(notificationEvent);
            kafkaTemplate.send(shipmentEventsTopic, shipment.getId().toString(), payload);
            log.info("Published driver assignment event for shipment {} to driver {}", shipment.getId(), driverUsername);
        } catch (Exception e) {
            log.error("Failed to publish driver assignment event to Kafka", e);
        }
    }

    private void publishNotificationEvent(Shipment shipment, String eventType, String message) {
        try {
            Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent notificationEvent = Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent
                    .builder()
                    .eventId(java.util.UUID.randomUUID().toString())
                    .aggregateId(shipment.getId().toString())
                    .aggregateType("SHIPMENT")
                    .eventType(eventType)
                    .message(message)
                    .customerId(null) // Shipments might not be directly linked to a single customer in this context,
                                      // or we need to iterate orders
                    .occurredAt(java.time.OffsetDateTime.now())
                    .build();

            String payload = objectMapper.writeValueAsString(notificationEvent);
            kafkaTemplate.send(shipmentEventsTopic, shipment.getId().toString(), payload);
        } catch (Exception e) {
            log.error("Failed to publish shipment event to Kafka", e);
        }
    }
}

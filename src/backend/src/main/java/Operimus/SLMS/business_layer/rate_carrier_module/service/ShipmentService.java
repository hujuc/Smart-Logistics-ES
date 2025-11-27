package Operimus.SLMS.business_layer.rate_carrier_module.service;

import Operimus.SLMS.business_layer.rate_carrier_module.entities.Carrier;
import Operimus.SLMS.business_layer.order_module.entities.Order;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Shipment;
import Operimus.SLMS.business_layer.order_module.repository.OrderRepository;
import Operimus.SLMS.business_layer.rate_carrier_module.repository.ShipmentRepository;
import Operimus.SLMS.business_layer.shared.exception.ResourceConflictException;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
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
                .active(true)
                .build();

        Shipment saved = shipmentRepository.save(shipment);

        // Assign orders to shipment
        for (Order order : orders) {
            order.setShipmentId(saved.getId());
            order.setStatus(OrderStatus.ASSIGNED_TO_SHIPMENT);
        }
        orderRepository.saveAll(orders);

        triggerShipmentDocuments(saved, orders);

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

        shipment.setDriverId(newDriverId);
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

    private void triggerShipmentDocuments(Shipment shipment, List<Order> orders) {
        ShipmentLabelGenerationService generator = shipmentLabelGenerationService.getIfAvailable();
        if (generator != null) {
            generator.generateCarrierDocuments(shipment, orders);
        }
    }
}

package Operimus.SLMS.business_layer.rate_carrier_module.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import Operimus.SLMS.business_layer.order_module.entities.Order;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.entities.Warehouse;
import Operimus.SLMS.business_layer.order_module.repository.OrderRepository;
import Operimus.SLMS.business_layer.order_module.repository.WarehouseRepository;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Carrier;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Shipment;
import Operimus.SLMS.business_layer.rate_carrier_module.repository.CarrierRepository;
import Operimus.SLMS.business_layer.rate_carrier_module.repository.ShipmentRepository;
import Operimus.SLMS.business_layer.shared.exception.ResourceConflictException;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({ShipmentService.class, CarrierService.class})
class ShipmentServiceTest {

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private CarrierRepository carrierRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private Carrier carrier;
    private Warehouse warehouse;
    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() {
        carrier = carrierRepository.save(Carrier.builder()
                .code("DHL")
                .name("DHL Express")
                .active(true)
                .build());

        warehouse = warehouseRepository.save(Warehouse.builder()
                .code("WH-001")
                .name("Main Warehouse")
                .addressLine1("123 Logistics Ave")
                .city("Porto")
                .stateOrProvince("Porto")
                .country("Portugal")
                .postalCode("4000-000")
                .active(true)
                .build());

        order1 = orderRepository.save(Order.builder()
                .reference("ORD-001")
                .description("Order 1")
                .shippingAddress("Customer St 1")
                .warehouse(warehouse)
                .status(OrderStatus.CREATED)
                .weight(2.5)
                .weightUnit("kg")
                .build());

        order2 = orderRepository.save(Order.builder()
                .reference("ORD-002")
                .description("Order 2")
                .shippingAddress("Customer St 2")
                .warehouse(warehouse)
                .status(OrderStatus.CREATED)
                .weight(1.5)
                .weightUnit("kg")
                .build());
    }

    @Test
    void createShouldPersistShipmentWithDriverAndCarrier() {
        Shipment shipment = shipmentService.create(
                "SHIP-001",
                carrier.getId(),
                "driver-uuid-123",
                List.of(order1.getId(), order2.getId())
        );

        assertThat(shipment.getId()).isNotNull();
        assertThat(shipment.getShipmentCode()).isEqualTo("SHIP-001");
        assertThat(shipment.getCarrier().getId()).isEqualTo(carrier.getId());
        assertThat(shipment.getDriverId()).isEqualTo("driver-uuid-123");
        assertThat(shipment.getTotalOrders()).isEqualTo(2);
        assertThat(shipment.getTotalWeight()).isEqualTo(4.0);
        assertThat(shipment.getWeightUnit()).isEqualTo("kg");

        // Verify orders were assigned
        Order reloaded1 = orderRepository.findById(order1.getId()).orElseThrow();
        Order reloaded2 = orderRepository.findById(order2.getId()).orElseThrow();
        assertThat(reloaded1.getShipmentId()).isEqualTo(shipment.getId());
        assertThat(reloaded1.getStatus()).isEqualTo(OrderStatus.ASSIGNED_TO_SHIPMENT);
        assertThat(reloaded2.getShipmentId()).isEqualTo(shipment.getId());
        assertThat(reloaded2.getStatus()).isEqualTo(OrderStatus.ASSIGNED_TO_SHIPMENT);
    }

    @Test
    void createShouldFailWhenShipmentCodeExists() {
        shipmentRepository.save(Shipment.builder()
                .shipmentCode("SHIP-001")
                .carrier(carrier)
                .driverId("driver-1")
                .build());

        assertThatThrownBy(() -> shipmentService.create(
                        "SHIP-001",
                        carrier.getId(),
                        "driver-2",
                        List.of(order1.getId())
                ))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("SHIP-001");
    }

    @Test
    void createShouldFailWhenDriverIdIsNull() {
        assertThatThrownBy(() -> shipmentService.create(
                        "SHIP-001",
                        carrier.getId(),
                        null,
                        List.of(order1.getId())
                ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Driver ID is required");
    }

    @Test
    void createShouldFailWhenDriverIdIsBlank() {
        assertThatThrownBy(() -> shipmentService.create(
                        "SHIP-001",
                        carrier.getId(),
                        "   ",
                        List.of(order1.getId())
                ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Driver ID is required");
    }

    @Test
    void createShouldFailWhenOrderNotFound() {
        assertThatThrownBy(() -> shipmentService.create(
                        "SHIP-001",
                        carrier.getId(),
                        "driver-uuid",
                        List.of(order1.getId(), 999L)
                ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void createShouldFailWhenOrderAlreadyAssignedToShipment() {
        order1.setShipmentId(123L);
        orderRepository.save(order1);

        assertThatThrownBy(() -> shipmentService.create(
                        "SHIP-001",
                        carrier.getId(),
                        "driver-uuid",
                        List.of(order1.getId())
                ))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("already assigned");
    }

    @Test
    void createShouldFailWhenOrderNotInCreatedStatus() {
        order1.setStatus(OrderStatus.IN_TRANSIT);
        orderRepository.save(order1);

        assertThatThrownBy(() -> shipmentService.create(
                        "SHIP-001",
                        carrier.getId(),
                        "driver-uuid",
                        List.of(order1.getId())
                ))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("not available for shipment");
    }

    @Test
    void updateDriverShouldChangeDriver() {
        Shipment shipment = shipmentRepository.save(Shipment.builder()
                .shipmentCode("SHIP-001")
                .carrier(carrier)
                .driverId("old-driver-uuid")
                .build());

        Shipment updated = shipmentService.updateDriver(shipment.getId(), "new-driver-uuid");

        assertThat(updated.getDriverId()).isEqualTo("new-driver-uuid");
        Shipment reloaded = shipmentRepository.findById(shipment.getId()).orElseThrow();
        assertThat(reloaded.getDriverId()).isEqualTo("new-driver-uuid");
    }

    @Test
    void updateDriverShouldFailWhenShipmentNotFound() {
        assertThatThrownBy(() -> shipmentService.updateDriver(999L, "new-driver"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void updateDriverShouldFailWhenNewDriverIdIsBlank() {
        Shipment shipment = shipmentRepository.save(Shipment.builder()
                .shipmentCode("SHIP-001")
                .carrier(carrier)
                .driverId("old-driver-uuid")
                .build());

        assertThatThrownBy(() -> shipmentService.updateDriver(shipment.getId(), ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Driver ID cannot be empty");
    }

    @Test
    void getByIdShouldReturnShipment() {
        Shipment shipment = shipmentRepository.save(Shipment.builder()
                .shipmentCode("SHIP-001")
                .carrier(carrier)
                .driverId("driver-uuid")
                .build());

        Shipment retrieved = shipmentService.getById(shipment.getId());

        assertThat(retrieved.getShipmentCode()).isEqualTo("SHIP-001");
        assertThat(retrieved.getDriverId()).isEqualTo("driver-uuid");
    }

    @Test
    void getByIdShouldFailWhenShipmentNotFound() {
        assertThatThrownBy(() -> shipmentService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getByCarrierIdShouldReturnShipments() {
        shipmentRepository.save(Shipment.builder()
                .shipmentCode("SHIP-001")
                .carrier(carrier)
                .driverId("driver-1")
                .build());
        shipmentRepository.save(Shipment.builder()
                .shipmentCode("SHIP-002")
                .carrier(carrier)
                .driverId("driver-2")
                .build());

        List<Shipment> shipments = shipmentService.getByCarrierId(carrier.getId());

        assertThat(shipments).hasSize(2);
        assertThat(shipments).extracting(Shipment::getShipmentCode)
                .containsExactlyInAnyOrder("SHIP-001", "SHIP-002");
    }

    @Test
    void getByDriverIdShouldReturnShipments() {
        shipmentRepository.save(Shipment.builder()
                .shipmentCode("SHIP-001")
                .carrier(carrier)
                .driverId("driver-123")
                .build());
        shipmentRepository.save(Shipment.builder()
                .shipmentCode("SHIP-002")
                .carrier(carrier)
                .driverId("driver-123")
                .build());
        shipmentRepository.save(Shipment.builder()
                .shipmentCode("SHIP-003")
                .carrier(carrier)
                .driverId("driver-456")
                .build());

        List<Shipment> shipments = shipmentService.getByDriverId("driver-123");

        assertThat(shipments).hasSize(2);
        assertThat(shipments).extracting(Shipment::getShipmentCode)
                .containsExactlyInAnyOrder("SHIP-001", "SHIP-002");
    }
}

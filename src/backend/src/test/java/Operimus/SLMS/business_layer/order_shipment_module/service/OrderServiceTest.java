package Operimus.SLMS.business_layer.order_shipment_module.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrder;
import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrderStatus;
import Operimus.SLMS.business_layer.order_shipment_module.entities.Warehouse;
import Operimus.SLMS.business_layer.order_shipment_module.dto.OrderRequest;
import Operimus.SLMS.business_layer.order_shipment_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_shipment_module.repository.ShipmentOrderRepository;
import Operimus.SLMS.business_layer.order_shipment_module.repository.WarehouseRepository;
import Operimus.SLMS.business_layer.shared.exception.ResourceConflictException;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({OrderService.class, WarehouseService.class})
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShipmentOrderRepository shipmentOrderRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        warehouse = warehouseRepository.save(Warehouse.builder()
                .code("W-001")
                .name("Main Hub")
                .addressLine1("123 Industrial Ave")
                .city("Porto")
                .stateOrProvince("Porto")
                .country("Portugal")
                .postalCode("4000-123")
                .active(true)
                .build());
    }

    @Test
    void createShouldPersistShipmentOrder() {
        OrderRequest request = OrderRequest.builder()
                .reference("ORD-001")
                .description("Electronics shipment")
                .shippingAddress("456 Customer St, Porto")
                .estimatedDeliveryDate(LocalDate.now().plusDays(5))
                .warehouseId(warehouse.getId())
                .shippoShipmentId("shippo-shipment")
                .shippoTransactionId("shippo-transaction")
                .shipstationOrderId("shipstation-order")
                .shipstationCarrierCode("DHL")
                .shipstationServiceCode("EXPRESS")
                .trackingNumber("TRACK123")
                .build();

        OrderResponse response = orderService.create(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getReference()).isEqualTo("ORD-001");
        assertThat(response.getStatus()).isEqualTo(ShipmentOrderStatus.CREATED);
        assertThat(shipmentOrderRepository.count()).isEqualTo(1);
        ShipmentOrder saved = shipmentOrderRepository.findById(response.getId()).orElseThrow();
        assertThat(saved.getWarehouse().getId()).isEqualTo(warehouse.getId());
        assertThat(saved.getShipstationCarrierCode()).isEqualTo("DHL");
    }

    @Test
    void createShouldFailWhenReferenceExists() {
        shipmentOrderRepository.save(ShipmentOrder.builder()
                .reference("ORD-001")
                .description("Existing order")
                .shippingAddress("123 Street")
                .warehouse(warehouse)
                .build());

        OrderRequest request = OrderRequest.builder()
                .reference("ORD-001")
                .description("Duplicate")
                .shippingAddress("456 Street")
                .warehouseId(warehouse.getId())
                .build();

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("ORD-001");
    }

    @Test
    void updateShouldModifyShipmentOrder() {
        ShipmentOrder order = shipmentOrderRepository.save(ShipmentOrder.builder()
                .reference("ORD-001")
                .description("Initial")
                .shippingAddress("123 Street")
                .warehouse(warehouse)
                .build());

        Warehouse newWarehouse = warehouseRepository.save(Warehouse.builder()
                .code("W-002")
                .name("Secondary Hub")
                .addressLine1("789 Logistics Rd")
                .city("Lisbon")
                .stateOrProvince("Lisbon")
                .country("Portugal")
                .postalCode("1000-000")
                .active(true)
                .build());

        OrderRequest request = OrderRequest.builder()
                .reference("ORD-002")
                .description("Updated description")
                .status(ShipmentOrderStatus.IN_TRANSIT)
                .shippingAddress("987 Customer Ave")
                .estimatedDeliveryDate(LocalDate.now().plusDays(3))
                .warehouseId(newWarehouse.getId())
                .shippoShipmentId("new-shippo-shipment")
                .shippoTransactionId("new-shippo-transaction")
                .shipstationOrderId("new-shipstation-order")
                .shipstationCarrierCode("FedEx")
                .shipstationServiceCode("STANDARD")
                .trackingNumber("TRACK456")
                .build();

        OrderResponse response = orderService.update(order.getId(), request);

        assertThat(response.getReference()).isEqualTo("ORD-002");
        assertThat(response.getStatus()).isEqualTo(ShipmentOrderStatus.IN_TRANSIT);
        ShipmentOrder updated = shipmentOrderRepository.findById(order.getId()).orElseThrow();
        assertThat(updated.getWarehouse().getId()).isEqualTo(newWarehouse.getId());
        assertThat(updated.getTrackingNumber()).isEqualTo("TRACK456");
    }

    @Test
    void updateShouldFailWhenChangingToExistingReference() {
        shipmentOrderRepository.save(ShipmentOrder.builder()
                .reference("ORD-001")
                .description("Existing")
                .shippingAddress("123 Street")
                .warehouse(warehouse)
                .build());
        ShipmentOrder order = shipmentOrderRepository.save(ShipmentOrder.builder()
                .reference("ORD-002")
                .description("Target")
                .shippingAddress("456 Street")
                .warehouse(warehouse)
                .build());

        OrderRequest request = OrderRequest.builder()
                .reference("ORD-001")
                .description("Updated")
                .shippingAddress("789 Street")
                .warehouseId(warehouse.getId())
                .build();

        assertThatThrownBy(() -> orderService.update(order.getId(), request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("ORD-001");
    }

    @Test
    void deleteShouldRemoveShipmentOrder() {
        ShipmentOrder order = shipmentOrderRepository.save(ShipmentOrder.builder()
                .reference("ORD-001")
                .description("To delete")
                .shippingAddress("123 Street")
                .warehouse(warehouse)
                .build());

        orderService.delete(order.getId());

        assertThat(shipmentOrderRepository.findById(order.getId())).isEmpty();
    }

    @Test
    void getByIdShouldReturnOrder() {
        ShipmentOrder order = shipmentOrderRepository.save(ShipmentOrder.builder()
                .reference("ORD-001")
                .description("Lookup")
                .shippingAddress("123 Street")
                .warehouse(warehouse)
                .build());

        OrderResponse response = orderService.getById(order.getId());

        assertThat(response.getReference()).isEqualTo("ORD-001");
        assertThat(response.getWarehouseId()).isEqualTo(warehouse.getId());
    }

    @Test
    void getByIdShouldFailWhenOrderDoesNotExist() {
        assertThatThrownBy(() -> orderService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getAllShouldReturnAllOrders() {
        shipmentOrderRepository.save(ShipmentOrder.builder()
                .reference("ORD-001")
                .description("First")
                .shippingAddress("123 Street")
                .warehouse(warehouse)
                .build());
        shipmentOrderRepository.save(ShipmentOrder.builder()
                .reference("ORD-002")
                .description("Second")
                .shippingAddress("456 Street")
                .warehouse(warehouse)
                .build());

        assertThat(orderService.getAll())
                .extracting(OrderResponse::getReference)
                .containsExactlyInAnyOrder("ORD-001", "ORD-002");
    }
}

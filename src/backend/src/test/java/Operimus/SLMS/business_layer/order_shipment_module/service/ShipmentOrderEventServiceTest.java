package Operimus.SLMS.business_layer.order_shipment_module.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import Operimus.SLMS.business_layer.order_shipment_module.dto.OrderEventResponse;
import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrder;
import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrderEvent;
import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrderStatus;
import Operimus.SLMS.business_layer.order_shipment_module.entities.Warehouse;
import Operimus.SLMS.business_layer.order_shipment_module.repository.ShipmentOrderEventRepository;
import Operimus.SLMS.business_layer.order_shipment_module.repository.ShipmentOrderRepository;
import Operimus.SLMS.business_layer.order_shipment_module.repository.WarehouseRepository;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(ShipmentOrderEventService.class)
class ShipmentOrderEventServiceTest {

    @Autowired
    private ShipmentOrderEventService shipmentOrderEventService;

    @Autowired
    private ShipmentOrderEventRepository shipmentOrderEventRepository;

    @Autowired
    private ShipmentOrderRepository shipmentOrderRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    private ShipmentOrder order;

    @BeforeEach
    void setUp() {
        Warehouse warehouse = warehouseRepository.save(Warehouse.builder()
                .code("WH-001")
                .name("Main Warehouse")
                .addressLine1("123 Logistics Ave")
                .city("Porto")
                .stateOrProvince("Porto")
                .country("Portugal")
                .postalCode("4000-000")
                .active(true)
                .build());

        order = shipmentOrderRepository.save(ShipmentOrder.builder()
                .reference("ORD-EVENT")
                .description("Event testing order")
                .shippingAddress("456 Customer Rd")
                .warehouse(warehouse)
                .build());
    }

    @Test
    void getEventsForOrderShouldReturnOrderedTimeline() {
        shipmentOrderEventRepository.saveAll(List.of(
                ShipmentOrderEvent.builder()
                        .shipmentOrder(order)
                        .eventType(ShipmentOrderStatus.CREATED)
                        .notes("Created")
                        .build(),
                ShipmentOrderEvent.builder()
                        .shipmentOrder(order)
                        .eventType(ShipmentOrderStatus.READY_FOR_PICKUP)
                        .notes("Ready")
                        .build()));

        List<OrderEventResponse> responses = shipmentOrderEventService.getEventsForOrder(order.getId());

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getEventType()).isEqualTo(ShipmentOrderStatus.CREATED);
        assertThat(responses.get(1).getEventType()).isEqualTo(ShipmentOrderStatus.READY_FOR_PICKUP);
    }

    @Test
    void getEventsForOrderShouldFailWhenOrderDoesNotExist() {
        assertThatThrownBy(() -> shipmentOrderEventService.getEventsForOrder(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getLatestEventForOrderShouldReturnMostRecentEvent() {
        shipmentOrderEventRepository.saveAll(List.of(
                ShipmentOrderEvent.builder()
                        .shipmentOrder(order)
                        .eventType(ShipmentOrderStatus.CREATED)
                        .notes("Created")
                        .build(),
                ShipmentOrderEvent.builder()
                        .shipmentOrder(order)
                        .eventType(ShipmentOrderStatus.IN_TRANSIT)
                        .notes("In transit")
                        .build()));

        OrderEventResponse latest = shipmentOrderEventService.getLatestEventForOrder(order.getId());

        assertThat(latest.getEventType()).isEqualTo(ShipmentOrderStatus.IN_TRANSIT);
        assertThat(latest.getNotes()).isEqualTo("In transit");
    }

    @Test
    void getLatestEventForOrderShouldFailWhenNoEventsExist() {
        assertThatThrownBy(() -> shipmentOrderEventService.getLatestEventForOrder(order.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No events recorded");
    }
}

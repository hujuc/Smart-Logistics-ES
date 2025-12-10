package Operimus.SLMS.business_layer.order_module.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import Operimus.SLMS.business_layer.order_module.dto.OrderEventResponse;
import Operimus.SLMS.business_layer.order_module.entities.Order;
import Operimus.SLMS.business_layer.order_module.entities.OrderEvent;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.entities.Warehouse;
import Operimus.SLMS.business_layer.order_module.repository.OrderEventRepository;
import Operimus.SLMS.business_layer.order_module.repository.OrderRepository;
import Operimus.SLMS.business_layer.order_module.repository.WarehouseRepository;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(OrderEventService.class)
class OrderEventServiceTest {

    @Autowired
    private OrderEventService orderEventService;

    @Autowired
    private OrderEventRepository orderEventRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private Order order;

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

        order = orderRepository.save(Order.builder()
                .reference("ORD-EVENT")
                .description("Event testing order")
                .shippingAddress("456 Customer Rd")
                .warehouse(warehouse)
                .build());
    }

    @Test
    void getEventsForOrderShouldReturnOrderedTimeline() {
        orderEventRepository.saveAll(List.of(
                OrderEvent.builder()
                        .order(order)
                        .eventType(OrderStatus.CREATED)
                        .notes("Created")
                        .build(),
                OrderEvent.builder()
                        .order(order)
                        .eventType(OrderStatus.ASSIGNED_TO_SHIPMENT)
                        .notes("Assigned to shipment")
                        .build()));

        List<OrderEventResponse> responses = orderEventService.getEventsForOrder(order.getId());

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getEventType()).isEqualTo(OrderStatus.CREATED);
        assertThat(responses.get(1).getEventType()).isEqualTo(OrderStatus.ASSIGNED_TO_SHIPMENT);
    }

    @Test
    void getEventsForOrderShouldFailWhenOrderDoesNotExist() {
        assertThatThrownBy(() -> orderEventService.getEventsForOrder(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getLatestEventForOrderShouldReturnMostRecentEvent() {
        orderEventRepository.saveAll(List.of(
                OrderEvent.builder()
                        .order(order)
                        .eventType(OrderStatus.CREATED)
                        .notes("Created")
                        .build(),
                OrderEvent.builder()
                        .order(order)
                        .eventType(OrderStatus.IN_TRANSIT)
                        .notes("In transit")
                        .build()));

        OrderEventResponse latest = orderEventService.getLatestEventForOrder(order.getId());

        assertThat(latest.getEventType()).isEqualTo(OrderStatus.IN_TRANSIT);
        assertThat(latest.getNotes()).isEqualTo("In transit");
    }

    @Test
    void getLatestEventForOrderShouldFailWhenNoEventsExist() {
        assertThatThrownBy(() -> orderEventService.getLatestEventForOrder(order.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No events recorded");
    }
}

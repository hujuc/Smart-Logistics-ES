package Operimus.SLMS.business_layer.order_module.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Operimus.SLMS.business_layer.order_module.dto.DocumentUploadResponse;
import Operimus.SLMS.business_layer.order_module.entities.Order;
import Operimus.SLMS.business_layer.order_module.entities.OrderEvent;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.entities.Warehouse;
import Operimus.SLMS.business_layer.order_module.dto.OrderRequest;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.repository.OrderEventRepository;
import Operimus.SLMS.business_layer.order_module.repository.OrderRepository;
import Operimus.SLMS.business_layer.order_module.repository.WarehouseRepository;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Carrier;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Shipment;
import Operimus.SLMS.business_layer.rate_carrier_module.repository.CarrierRepository;
import Operimus.SLMS.business_layer.rate_carrier_module.repository.ShipmentRepository;
import Operimus.SLMS.business_layer.shared.exception.ResourceConflictException;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.access.AccessDeniedException;

@DataJpaTest
@Import({OrderService.class, WarehouseService.class, OrderEventService.class})
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private OrderEventRepository orderEventRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private CarrierRepository carrierRepository;

    @MockBean
    private OrderDocumentService orderDocumentService;

    @MockBean
    private KeycloakUserService keycloakUserService;

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

        when(orderDocumentService.uploadDocument(any(), any()))
                .thenReturn(DocumentUploadResponse.builder()
                        .url("https://cdn.local/mock-qr.png")
                        .build());

        when(keycloakUserService.searchUsers(any()))
                .thenReturn(Collections.emptyList());
    }

    @Test
    void createShouldPersistOrder() {
        OrderRequest request = OrderRequest.builder()
                .reference("ORD-001")
                .description("Electronics order")
                .shippingAddress("456 Customer St, Porto")
                .estimatedDeliveryDate(LocalDate.now().plusDays(5))
                .warehouseId(warehouse.getId())
                .customerId("customer-uuid-123")
                .trackingNumber("TRACK123")
                .weight(2.5)
                .weightUnit("kg")
                .length(30.0)
                .width(20.0)
                .height(15.0)
                .distanceUnit("cm")
                .build();

        OrderResponse response = orderService.create(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getReference()).isEqualTo("ORD-001");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(orderRepository.count()).isEqualTo(1);
        Order saved = orderRepository.findById(response.getId()).orElseThrow();
        assertThat(saved.getWarehouse().getId()).isEqualTo(warehouse.getId());
        assertThat(saved.getCustomerId()).isEqualTo("customer-uuid-123");
        assertThat(saved.getWeight()).isEqualTo(2.5);
        assertThat(saved.getShipmentId()).isNull();
        assertThat(saved.getQrCodeUrl()).isEqualTo("https://cdn.local/mock-qr.png");
        assertThat(saved.getQrConfirmationUrl()).contains("/orders/" + saved.getId() + "/confirm");
        assertThat(saved.getQrConfirmedAt()).isNull();
        verify(orderDocumentService).uploadDocument(eq(saved.getId()), any());
    }

    @Test
    void createShouldRecordInitialEvent() {
        OrderRequest request = OrderRequest.builder()
                .reference("ORD-010")
                .description("Initial event")
                .shippingAddress("456 Customer St, Porto")
                .warehouseId(warehouse.getId())
                .build();

        OrderResponse response = orderService.create(request);

        List<OrderEvent> events =
                orderEventRepository.findByOrderIdOrderByOccurredAtAsc(response.getId());
        assertThat(events).hasSize(1);
        OrderEvent event = events.get(0);
        assertThat(event.getEventType()).isEqualTo(OrderStatus.CREATED);
        assertThat(event.getNotes()).contains("initialized");
    }

    @Test
    void createShouldFailWhenReferenceExists() {
        orderRepository.save(Order.builder()
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
    void updateShouldModifyOrder() {
        Order order = orderRepository.save(Order.builder()
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
                .status(OrderStatus.IN_TRANSIT)
                .shippingAddress("987 Customer Ave")
                .estimatedDeliveryDate(LocalDate.now().plusDays(3))
                .warehouseId(newWarehouse.getId())
                .trackingNumber("TRACK456")
                .weight(1.5)
                .weightUnit("kg")
                .build();

        OrderResponse response = orderService.update(order.getId(), request);

        assertThat(response.getReference()).isEqualTo("ORD-002");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.IN_TRANSIT);
        Order updated = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updated.getWarehouse().getId()).isEqualTo(newWarehouse.getId());
        assertThat(updated.getTrackingNumber()).isEqualTo("TRACK456");
        assertThat(updated.getWeight()).isEqualTo(1.5);
    }

    @Test
    void updateShouldRecordEventWhenStatusChanges() {
        OrderResponse created = orderService.create(OrderRequest.builder()
                .reference("ORD-CHANGE")
                .description("Status change")
                .shippingAddress("123 Street")
                .warehouseId(warehouse.getId())
                .build());

        OrderRequest updateRequest = OrderRequest.builder()
                .reference("ORD-CHANGE")
                .description("Updated status")
                .status(OrderStatus.IN_TRANSIT)
                .shippingAddress("123 Street")
                .warehouseId(warehouse.getId())
                .build();

        orderService.update(created.getId(), updateRequest);

        List<OrderEvent> events =
                orderEventRepository.findByOrderIdOrderByOccurredAtAsc(created.getId());
        assertThat(events).hasSize(2);
        OrderEvent latest = events.get(1);
        assertThat(latest.getEventType()).isEqualTo(OrderStatus.IN_TRANSIT);
        assertThat(latest.getNotes()).contains("changed from CREATED to IN_TRANSIT");
    }

    @Test
    void updateShouldFailWhenChangingToExistingReference() {
        orderRepository.save(Order.builder()
                .reference("ORD-001")
                .description("Existing")
                .shippingAddress("123 Street")
                .warehouse(warehouse)
                .build());
        Order order = orderRepository.save(Order.builder()
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
    void deleteShouldRemoveOrder() {
        Order order = orderRepository.save(Order.builder()
                .reference("ORD-001")
                .description("To delete")
                .shippingAddress("123 Street")
                .warehouse(warehouse)
                .build());

        orderService.delete(order.getId());

        assertThat(orderRepository.findById(order.getId())).isEmpty();
    }

    @Test
    void getByIdShouldReturnOrder() {
        Order order = orderRepository.save(Order.builder()
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
    void confirmOrderScanShouldUpdateStatusAndProgress() {
        Carrier carrier = carrierRepository.save(Carrier.builder()
                .code("C-DRV-1")
                .name("Driver Carrier")
                .build());

        Shipment shipment = shipmentRepository.save(Shipment.builder()
                .shipmentCode("SHIP-DRV-1")
                .carrier(carrier)
                .driverId("driver-123")
                .build());

        Order primary = orderRepository.save(Order.builder()
                .reference("ORD-SCAN-1")
                .description("Primary")
                .shippingAddress("123 Street")
                .warehouse(warehouse)
                .shipmentId(shipment.getId())
                .status(OrderStatus.ASSIGNED_TO_SHIPMENT)
                .build());

        orderRepository.save(Order.builder()
                .reference("ORD-SCAN-2")
                .description("Pending")
                .shippingAddress("456 Street")
                .warehouse(warehouse)
                .shipmentId(shipment.getId())
                .status(OrderStatus.ASSIGNED_TO_SHIPMENT)
                .build());

        var result = orderService.confirmOrderScan(primary.getId(), "driver-123");

        assertThat(result.isAlreadyConfirmed()).isFalse();
        assertThat(result.isAllOrdersConfirmed()).isFalse();
        assertThat(result.getRemainingOrdersToScan()).isEqualTo(1L);
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.IN_TRANSIT);

        Order updated = orderRepository.findById(primary.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.IN_TRANSIT);
        assertThat(updated.getQrConfirmedBy()).isEqualTo("driver-123");
        assertThat(updated.getQrConfirmedAt()).isNotNull();
    }

    @Test
    void confirmOrderScanShouldRejectDriverMismatch() {
        Carrier carrier = carrierRepository.save(Carrier.builder()
                .code("C-DRV-2")
                .name("Driver Carrier 2")
                .build());

        Shipment shipment = shipmentRepository.save(Shipment.builder()
                .shipmentCode("SHIP-DRV-2")
                .carrier(carrier)
                .driverId("driver-abc")
                .build());

        Order order = orderRepository.save(Order.builder()
                .reference("ORD-SCAN-3")
                .description("Mismatch")
                .shippingAddress("789 Street")
                .warehouse(warehouse)
                .shipmentId(shipment.getId())
                .status(OrderStatus.ASSIGNED_TO_SHIPMENT)
                .build());

        assertThatThrownBy(() -> orderService.confirmOrderScan(order.getId(), "driver-other"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getAllShouldReturnAllOrders() {
        orderRepository.save(Order.builder()
                .reference("ORD-001")
                .description("First")
                .shippingAddress("123 Street")
                .warehouse(warehouse)
                .build());
        orderRepository.save(Order.builder()
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

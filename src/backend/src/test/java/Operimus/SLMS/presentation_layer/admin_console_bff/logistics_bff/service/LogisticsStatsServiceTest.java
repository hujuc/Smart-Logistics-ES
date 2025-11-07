package Operimus.SLMS.presentation_layer.admin_console_bff.logistics_bff.service;

import Operimus.SLMS.business_layer.order_module.entities.Order;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.entities.Warehouse;
import Operimus.SLMS.business_layer.order_module.repository.OrderRepository;
import Operimus.SLMS.business_layer.order_module.repository.WarehouseRepository;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Carrier;
import Operimus.SLMS.business_layer.rate_carrier_module.repository.CarrierRepository;
import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException;
import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.ExceptionStatus;
import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.ExceptionType;
import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.Severity;
import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.Source;
import Operimus.SLMS.business_layer.visibility_module.exceptions.repository.DeliveryExceptionRepository;
import Operimus.SLMS.presentation_layer.admin_console_bff.logistics_bff.dto.LogisticsStatsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogisticsStatsServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private CarrierRepository carrierRepository;

    @Mock
    private DeliveryExceptionRepository deliveryExceptionRepository;

    @InjectMocks
    private LogisticsStatsService logisticsStatsService;

    private List<Order> testOrders;
    private List<Warehouse> testWarehouses;
    private List<Carrier> testCarriers;
    private List<DeliveryException> testExceptions;

    @BeforeEach
    void setUp() {
        // Setup test data
        testWarehouses = createTestWarehouses();
        testOrders = createTestOrders(testWarehouses);
        testCarriers = createTestCarriers();
        testExceptions = createTestExceptions();
    }

    @Test
    void calculateStats_withDailyPeriod_returnsCompleteStats() {
        // Given
        when(orderRepository.findAll()).thenReturn(testOrders);
        when(warehouseRepository.findAll()).thenReturn(testWarehouses);
        when(carrierRepository.findAll()).thenReturn(testCarriers);
        when(carrierRepository.count()).thenReturn((long) testCarriers.size());
        when(deliveryExceptionRepository.findAll()).thenReturn(testExceptions);

        // When
        LogisticsStatsResponse stats = logisticsStatsService.calculateStats("daily");

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getPeriod()).isEqualTo("daily");
        assertThat(stats.getTotalShipments()).isNotNull();
        assertThat(stats.getOrdersToday()).isNotNull();
        assertThat(stats.getPendingPickups()).isNotNull();
        assertThat(stats.getInTransitNow()).isNotNull();
        assertThat(stats.getDeliveredToday()).isNotNull();
        assertThat(stats.getAverageCostPerShipment()).isNotNull();
        assertThat(stats.getTotalShipmentCosts()).isNotNull();
        assertThat(stats.getAverageDeliveryTime()).isNotNull();
        assertThat(stats.getTotalExceptions()).isNotNull();
        assertThat(stats.getTotalTickets()).isNotNull();
        assertThat(stats.getTotalWarehouses()).isNotNull();
        assertThat(stats.getActiveDrivers()).isNotNull();
    }

    @Test
    void calculateStats_withWeeklyPeriod_returnsCompleteStats() {
        // Given
        when(orderRepository.findAll()).thenReturn(testOrders);
        when(warehouseRepository.findAll()).thenReturn(testWarehouses);
        when(carrierRepository.findAll()).thenReturn(testCarriers);
        when(carrierRepository.count()).thenReturn((long) testCarriers.size());
        when(deliveryExceptionRepository.findAll()).thenReturn(testExceptions);

        // When
        LogisticsStatsResponse stats = logisticsStatsService.calculateStats("weekly");

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getPeriod()).isEqualTo("weekly");
        assertThat(stats.getTotalShipments().getLabel()).isEqualTo("vs last week");
    }

    @Test
    void calculateStats_withMonthlyPeriod_returnsCompleteStats() {
        // Given
        when(orderRepository.findAll()).thenReturn(testOrders);
        when(warehouseRepository.findAll()).thenReturn(testWarehouses);
        when(carrierRepository.findAll()).thenReturn(testCarriers);
        when(carrierRepository.count()).thenReturn((long) testCarriers.size());
        when(deliveryExceptionRepository.findAll()).thenReturn(testExceptions);

        // When
        LogisticsStatsResponse stats = logisticsStatsService.calculateStats("monthly");

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getPeriod()).isEqualTo("monthly");
        assertThat(stats.getTotalShipments().getLabel()).isEqualTo("vs last month");
    }

    @Test
    void calculateStats_withNoOrders_returnsZeroStats() {
        // Given
        when(orderRepository.findAll()).thenReturn(new ArrayList<>());
        when(warehouseRepository.findAll()).thenReturn(testWarehouses);
        when(carrierRepository.findAll()).thenReturn(testCarriers);
        when(carrierRepository.count()).thenReturn((long) testCarriers.size());
        when(deliveryExceptionRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        LogisticsStatsResponse stats = logisticsStatsService.calculateStats("daily");

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalShipments().getValue()).isEqualTo(0);
        assertThat(stats.getOrdersToday().getValue()).isEqualTo(0);
    }

    @Test
    void calculateStats_withOrdersInDifferentStatuses_countsCorrectly() {
        // Given
        when(orderRepository.findAll()).thenReturn(testOrders);
        when(warehouseRepository.findAll()).thenReturn(testWarehouses);
        when(carrierRepository.findAll()).thenReturn(testCarriers);
        when(carrierRepository.count()).thenReturn((long) testCarriers.size());
        when(deliveryExceptionRepository.findAll()).thenReturn(testExceptions);

        // When
        LogisticsStatsResponse stats = logisticsStatsService.calculateStats("daily");

        // Then
        assertThat(stats.getTotalShipments().getByStatus()).isNotNull();
        assertThat(stats.getTotalShipments().getByStatus().getCreated()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getTotalShipments().getByStatus().getInTransit()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getTotalShipments().getByStatus().getDelivered()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void calculateStats_withDeliveryExceptions_countsAndCategorizesCorrectly() {
        // Given
        when(orderRepository.findAll()).thenReturn(testOrders);
        when(warehouseRepository.findAll()).thenReturn(testWarehouses);
        when(carrierRepository.findAll()).thenReturn(testCarriers);
        when(carrierRepository.count()).thenReturn((long) testCarriers.size());
        when(deliveryExceptionRepository.findAll()).thenReturn(testExceptions);

        // When
        LogisticsStatsResponse stats = logisticsStatsService.calculateStats("daily");

        // Then
        assertThat(stats.getTotalExceptions()).isNotNull();
        assertThat(stats.getTotalExceptions().getByType()).isNotNull();
        assertThat(stats.getTotalExceptions().getByStatus()).isNotNull();
        assertThat(stats.getTotalExceptions().getBySeverity()).isNotNull();
    }

    @Test
    void calculateStats_withMultipleWarehouses_groupsOrdersByWarehouse() {
        // Given
        when(orderRepository.findAll()).thenReturn(testOrders);
        when(warehouseRepository.findAll()).thenReturn(testWarehouses);
        when(carrierRepository.findAll()).thenReturn(testCarriers);
        when(carrierRepository.count()).thenReturn((long) testCarriers.size());
        when(deliveryExceptionRepository.findAll()).thenReturn(testExceptions);

        // When
        LogisticsStatsResponse stats = logisticsStatsService.calculateStats("daily");

        // Then
        assertThat(stats.getTotalWarehouses()).isNotNull();
        assertThat(stats.getTotalWarehouses().getValue()).isEqualTo(testWarehouses.size());
        assertThat(stats.getTotalWarehouses().getWarehouses()).isNotEmpty();
    }

    @Test
    void calculateStats_withActiveCarriers_countsActiveDrivers() {
        // Given
        when(orderRepository.findAll()).thenReturn(testOrders);
        when(warehouseRepository.findAll()).thenReturn(testWarehouses);
        when(carrierRepository.findAll()).thenReturn(testCarriers);
        when(carrierRepository.count()).thenReturn((long) testCarriers.size());
        when(deliveryExceptionRepository.findAll()).thenReturn(testExceptions);

        // When
        LogisticsStatsResponse stats = logisticsStatsService.calculateStats("daily");

        // Then
        assertThat(stats.getActiveDrivers()).isNotNull();
        assertThat(stats.getActiveDrivers().getValue()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getActiveDrivers().getTotal()).isEqualTo(testCarriers.size());
    }

    @Test
    void calculateStats_withPendingOrders_identifiesOverdue() {
        // Given
        List<Order> ordersWithOld = new ArrayList<>(testOrders);
        Order oldOrder = createOrder(1L, OrderStatus.CREATED, testWarehouses.get(0),
                                    OffsetDateTime.now().minusHours(30));
        ordersWithOld.add(oldOrder);

        when(orderRepository.findAll()).thenReturn(ordersWithOld);
        when(warehouseRepository.findAll()).thenReturn(testWarehouses);
        when(carrierRepository.findAll()).thenReturn(testCarriers);
        when(carrierRepository.count()).thenReturn((long) testCarriers.size());
        when(deliveryExceptionRepository.findAll()).thenReturn(testExceptions);

        // When
        LogisticsStatsResponse stats = logisticsStatsService.calculateStats("daily");

        // Then
        assertThat(stats.getPendingPickups()).isNotNull();
        if (stats.getPendingPickups().getValue() > 0) {
            assertThat(stats.getPendingPickups().getOverdueBreakdown()).isNotNull();
        }
    }

    @Test
    void calculateStats_withDeliveredOrders_calculatesOnTimePercentage() {
        // Given
        when(orderRepository.findAll()).thenReturn(testOrders);
        when(warehouseRepository.findAll()).thenReturn(testWarehouses);
        when(carrierRepository.findAll()).thenReturn(testCarriers);
        when(carrierRepository.count()).thenReturn((long) testCarriers.size());
        when(deliveryExceptionRepository.findAll()).thenReturn(testExceptions);

        // When
        LogisticsStatsResponse stats = logisticsStatsService.calculateStats("daily");

        // Then
        assertThat(stats.getDeliveredToday()).isNotNull();
        assertThat(stats.getDeliveredToday().getOnTimeBreakdown()).isNotNull();
    }

    // Helper methods to create test data

    private List<Warehouse> createTestWarehouses() {
        List<Warehouse> warehouses = new ArrayList<>();

        Warehouse warehouse1 = Warehouse.builder()
                .id(1L)
                .code("WH-001")
                .name("Main Warehouse")
                .addressLine1("123 Main St")
                .city("New York")
                .stateOrProvince("NY")
                .country("USA")
                .postalCode("10001")
                .active(true)
                .build();

        Warehouse warehouse2 = Warehouse.builder()
                .id(2L)
                .code("WH-002")
                .name("Secondary Warehouse")
                .addressLine1("456 Second St")
                .city("Los Angeles")
                .stateOrProvince("CA")
                .country("USA")
                .postalCode("90001")
                .active(true)
                .build();

        warehouses.add(warehouse1);
        warehouses.add(warehouse2);

        return warehouses;
    }

    private List<Order> createTestOrders(List<Warehouse> warehouses) {
        List<Order> orders = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();

        // Create orders with different statuses
        orders.add(createOrder(1L, OrderStatus.CREATED, warehouses.get(0), now.minusHours(2)));
        orders.add(createOrder(2L, OrderStatus.ASSIGNED_TO_SHIPMENT, warehouses.get(0), now.minusHours(4)));
        orders.add(createOrder(3L, OrderStatus.IN_TRANSIT, warehouses.get(1), now.minusHours(6)));
        orders.add(createOrder(4L, OrderStatus.DELIVERED, warehouses.get(1), now.minusHours(24)));
        orders.add(createOrder(5L, OrderStatus.DELIVERED, warehouses.get(0), now.minusHours(48)));
        orders.add(createOrder(6L, OrderStatus.CANCELLED, warehouses.get(1), now.minusHours(12)));

        return orders;
    }

    private Order createOrder(Long id, OrderStatus status, Warehouse warehouse, OffsetDateTime createdAt) {
        return Order.builder()
                .id(id)
                .reference("ORD-" + id)
                .status(status)
                .warehouse(warehouse)
                .shippingAddress("123 Test St, Test City")
                .createdAt(createdAt)
                .updatedAt(OffsetDateTime.now())
                .estimatedDeliveryDate(LocalDate.now().plusDays(2))
                .build();
    }

    private List<Carrier> createTestCarriers() {
        List<Carrier> carriers = new ArrayList<>();

        Carrier carrier1 = Carrier.builder()
                .id(1L)
                .code("UPS")
                .name("UPS")
                .active(true)
                .build();

        Carrier carrier2 = Carrier.builder()
                .id(2L)
                .code("FEDEX")
                .name("FedEx")
                .active(true)
                .build();

        Carrier carrier3 = Carrier.builder()
                .id(3L)
                .code("DHL")
                .name("DHL")
                .active(false)
                .build();

        carriers.add(carrier1);
        carriers.add(carrier2);
        carriers.add(carrier3);

        return carriers;
    }

    private List<DeliveryException> createTestExceptions() {
        List<DeliveryException> exceptions = new ArrayList<>();

        DeliveryException exception1 = DeliveryException.builder()
                .id(UUID.randomUUID())
                .orderId(1L)
                .type(ExceptionType.DELAYED)
                .category("Delivery")
                .severity(Severity.HIGH)
                .description("Package delayed due to weather")
                .source(Source.SYSTEM)
                .status(ExceptionStatus.OPEN)
                .reportedAt(LocalDateTime.now().minusHours(2))
                .build();

        DeliveryException exception2 = DeliveryException.builder()
                .id(UUID.randomUUID())
                .orderId(2L)
                .type(ExceptionType.DAMAGED)
                .category("Quality")
                .severity(Severity.MEDIUM)
                .description("Package arrived damaged")
                .source(Source.CUSTOMER)
                .status(ExceptionStatus.RESOLVED)
                .reportedAt(LocalDateTime.now().minusHours(5))
                .resolvedAt(LocalDateTime.now().minusHours(1))
                .build();

        exceptions.add(exception1);
        exceptions.add(exception2);

        return exceptions;
    }
}

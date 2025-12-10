package Operimus.SLMS.business_layer.chatbot_module.tools.impl;

import Operimus.SLMS.business_layer.chatbot_module.dto.ToolResult;
import Operimus.SLMS.business_layer.chatbot_module.dto.UserContext;
import Operimus.SLMS.business_layer.order_module.dto.OrderEventResponse;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.service.OrderEventService;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for OrderStatusTool.
 * Verifies order status checking with event history and authorization.
 */
@ExtendWith(MockitoExtension.class)
class OrderStatusToolTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderEventService orderEventService;

    private ObjectMapper objectMapper;
    private OrderStatusTool tool;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        tool = new OrderStatusTool(orderService, orderEventService, objectMapper);
    }

    @Test
    void shouldReturnToolMetadata() {
        assertThat(tool.getName()).isEqualTo("order_status");
        assertThat(tool.getDescription()).contains("Check the current status");
        assertThat(tool.getParameters()).hasSize(1);
        assertThat(tool.getParameters().get(0).name()).isEqualTo("orderId");
        assertThat(tool.getParameters().get(0).required()).isTrue();
        assertThat(tool.getAllowedRoles()).contains("CUSTOMER", "DRIVER", "WAREHOUSE_STAFF", "CUSTOMER_SERVICE", "LOGISTICS_MANAGER");
    }

    @Test
    void shouldGetOrderStatus_asCustomer() throws Exception {
        // Given
        Long orderId = 123L;
        String customerId = "customer-1";

        OrderResponse order = createOrderResponse(orderId, customerId, "IN_TRANSIT");
        List<OrderEventResponse> events = createOrderEvents(orderId, 5);

        when(orderService.getById(orderId)).thenReturn(order);
        when(orderEventService.getEventsForOrder(orderId)).thenReturn(events);

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext(customerId, "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.result()).contains("IN_TRANSIT");
        assertThat(result.result()).contains("REF-123");

        verify(orderService).getById(orderId);
        verify(orderEventService).getEventsForOrder(orderId);
    }

    @Test
    void shouldDenyAccess_whenCustomerAccessesOtherCustomerOrder() {
        // Given
        Long orderId = 123L;
        String ownerCustomerId = "customer-1";
        String requestingCustomerId = "customer-2";

        OrderResponse order = createOrderResponse(orderId, ownerCustomerId, "IN_TRANSIT");
        when(orderService.getById(orderId)).thenReturn(order);

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext(requestingCustomerId, "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("can only view your own orders");

        verify(orderService).getById(orderId);
        verify(orderEventService, never()).getEventsForOrder(anyLong());
    }

    @Test
    void shouldAllowStaffToAccessAnyOrder() throws Exception {
        // Given
        Long orderId = 123L;
        String orderOwnerId = "customer-1";
        String staffId = "staff-1";

        OrderResponse order = createOrderResponse(orderId, orderOwnerId, "DELIVERED");
        List<OrderEventResponse> events = createOrderEvents(orderId, 3);

        when(orderService.getById(orderId)).thenReturn(order);
        when(orderEventService.getEventsForOrder(orderId)).thenReturn(events);

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext(staffId, "WAREHOUSE_STAFF", Set.of("WAREHOUSE_STAFF"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.result()).contains("DELIVERED");

        verify(orderService).getById(orderId);
        verify(orderEventService).getEventsForOrder(orderId);
    }

    @Test
    void shouldAllowManagerToAccessAnyOrder() throws Exception {
        // Given
        Long orderId = 123L;
        String orderOwnerId = "customer-1";
        String managerId = "manager-1";

        OrderResponse order = createOrderResponse(orderId, orderOwnerId, "CREATED");
        List<OrderEventResponse> events = createOrderEvents(orderId, 2);

        when(orderService.getById(orderId)).thenReturn(order);
        when(orderEventService.getEventsForOrder(orderId)).thenReturn(events);

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext(managerId, "LOGISTICS_MANAGER", Set.of("LOGISTICS_MANAGER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();

        verify(orderService).getById(orderId);
        verify(orderEventService).getEventsForOrder(orderId);
    }

    @Test
    void shouldAllowDriverToAccessOrder() throws Exception {
        // Given
        Long orderId = 123L;
        String customerId = "customer-1";
        String driverId = "driver-1";

        OrderResponse order = createOrderResponse(orderId, customerId, "IN_TRANSIT");
        List<OrderEventResponse> events = createOrderEvents(orderId, 4);

        when(orderService.getById(orderId)).thenReturn(order);
        when(orderEventService.getEventsForOrder(orderId)).thenReturn(events);

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext(driverId, "DRIVER", Set.of("DRIVER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.result()).contains("IN_TRANSIT");

        verify(orderService).getById(orderId);
        verify(orderEventService).getEventsForOrder(orderId);
    }

    @Test
    void shouldReturnError_whenOrderNotFound() {
        // Given
        Long orderId = 999L;

        when(orderService.getById(orderId))
            .thenThrow(new ResourceNotFoundException("Order not found: " + orderId));

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext("user-1", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Order not found");

        verify(orderService).getById(orderId);
        verify(orderEventService, never()).getEventsForOrder(anyLong());
    }

    @Test
    void shouldReturnError_whenOrderIdNotProvided() {
        // Given
        Map<String, Object> params = Map.of(); // Missing orderId
        UserContext context = new UserContext("user-1", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("orderId is required");

        verify(orderService, never()).getById(anyLong());
        verify(orderEventService, never()).getEventsForOrder(anyLong());
    }

    @Test
    void shouldReturnError_whenOrderIdIsNull() {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", null);
        UserContext context = new UserContext("user-1", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("orderId is required");

        verify(orderService, never()).getById(anyLong());
    }

    @Test
    void shouldHandleOrderServiceException() {
        // Given
        Long orderId = 123L;

        when(orderService.getById(orderId))
            .thenThrow(new RuntimeException("Database connection failed"));

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext("user-1", "LOGISTICS_MANAGER", Set.of("LOGISTICS_MANAGER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Failed to get order status");

        verify(orderService).getById(orderId);
    }

    @Test
    void shouldIncludeRecentEventsInResult() throws Exception {
        // Given
        Long orderId = 123L;
        String customerId = "customer-1";

        OrderResponse order = createOrderResponse(orderId, customerId, "IN_TRANSIT");
        List<OrderEventResponse> events = createOrderEvents(orderId, 10); // More than 5 events

        when(orderService.getById(orderId)).thenReturn(order);
        when(orderEventService.getEventsForOrder(orderId)).thenReturn(events);

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext(customerId, "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();

        // Parse result to verify structure
        @SuppressWarnings("unchecked")
        Map<String, Object> resultData = objectMapper.readValue(result.result(), Map.class);

        assertThat(resultData).containsKeys("orderId", "orderReference", "currentStatus", "recentEvents", "totalEvents");
        assertThat(resultData.get("orderId")).isEqualTo(123);
        assertThat(resultData.get("currentStatus")).isEqualTo("IN_TRANSIT");
        assertThat(resultData.get("totalEvents")).isEqualTo(10);

        // Should only include last 5 events
        @SuppressWarnings("unchecked")
        List<Object> recentEvents = (List<Object>) resultData.get("recentEvents");
        assertThat(recentEvents).hasSize(5);
    }

    @Test
    void shouldHandleOrderWithNoEvents() throws Exception {
        // Given
        Long orderId = 123L;
        String customerId = "customer-1";

        OrderResponse order = createOrderResponse(orderId, customerId, "CREATED");
        List<OrderEventResponse> events = List.of(); // No events

        when(orderService.getById(orderId)).thenReturn(order);
        when(orderEventService.getEventsForOrder(orderId)).thenReturn(events);

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext(customerId, "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();

        @SuppressWarnings("unchecked")
        Map<String, Object> resultData = objectMapper.readValue(result.result(), Map.class);

        assertThat(resultData.get("totalEvents")).isEqualTo(0);
        @SuppressWarnings("unchecked")
        List<Object> recentEvents = (List<Object>) resultData.get("recentEvents");
        assertThat(recentEvents).isEmpty();
    }

    @Test
    void shouldExtractLongFromInteger() throws Exception {
        // Given - LLM might send Integer instead of Long
        Long orderId = 123L;
        String customerId = "customer-1";

        OrderResponse order = createOrderResponse(orderId, customerId, "IN_TRANSIT");
        List<OrderEventResponse> events = createOrderEvents(orderId, 2);

        when(orderService.getById(orderId)).thenReturn(order);
        when(orderEventService.getEventsForOrder(orderId)).thenReturn(events);

        Map<String, Object> params = Map.of("orderId", 123); // Integer
        UserContext context = new UserContext(customerId, "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();
        verify(orderService).getById(123L);
    }

    @Test
    void shouldExtractLongFromString() throws Exception {
        // Given
        Long orderId = 123L;
        String customerId = "customer-1";

        OrderResponse order = createOrderResponse(orderId, customerId, "DELIVERED");
        List<OrderEventResponse> events = createOrderEvents(orderId, 1);

        when(orderService.getById(orderId)).thenReturn(order);
        when(orderEventService.getEventsForOrder(orderId)).thenReturn(events);

        Map<String, Object> params = Map.of("orderId", "123"); // String
        UserContext context = new UserContext(customerId, "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();
        verify(orderService).getById(123L);
    }

    // Helper methods
    private OrderResponse createOrderResponse(Long orderId, String customerId, String status) {
        return OrderResponse.builder()
                .id(orderId)
                .customerId(customerId)
                .reference("REF-" + orderId)
                .status(OrderStatus.valueOf(status))
                .trackingNumber("TRK-" + orderId)
                .build();
    }

    private List<OrderEventResponse> createOrderEvents(Long orderId, int count) {
        List<OrderEventResponse> events = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            OrderEventResponse event = OrderEventResponse.builder()
                    .id((long) (i + 1))
                    .orderId(orderId)
                    .eventType(OrderStatus.IN_TRANSIT)
                    .notes("Event " + (i + 1))
                    .occurredAt(java.time.OffsetDateTime.now().minusDays(i))
                    .build();
            events.add(event);
        }
        return events;
    }
}

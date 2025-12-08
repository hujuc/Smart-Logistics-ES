package Operimus.SLMS.business_layer.chatbot_module.tools.impl;

import Operimus.SLMS.business_layer.chatbot_module.dto.ToolResult;
import Operimus.SLMS.business_layer.chatbot_module.dto.UserContext;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for OrderLookupTool.
 * Verifies order lookup by ID/tracking number with authorization checks.
 */
@ExtendWith(MockitoExtension.class)
class OrderLookupToolTest {

    @Mock
    private OrderService orderService;

    private ObjectMapper objectMapper;
    private OrderLookupTool tool;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        tool = new OrderLookupTool(orderService, objectMapper);
    }

    @Test
    void shouldReturnToolMetadata() {
        assertThat(tool.getName()).isEqualTo("order_lookup");
        assertThat(tool.getDescription()).contains("Look up detailed information");
        assertThat(tool.getParameters()).hasSize(2);
        assertThat(tool.getAllowedRoles()).contains("CUSTOMER", "DRIVER", "WAREHOUSE_STAFF", "CUSTOMER_SERVICE", "LOGISTICS_MANAGER");
    }

    @Test
    void shouldLookupOrderById_asCustomer() throws Exception {
        // Given
        Long orderId = 123L;
        String customerId = "customer-1";

        OrderResponse order = createOrderResponse(orderId, "TRK-123", customerId);
        when(orderService.getById(orderId)).thenReturn(order);

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext(customerId, "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.result()).contains("TRK-123");
        assertThat(result.errorMessage()).isNull();

        verify(orderService).getById(orderId);
    }

    @Test
    void shouldLookupOrderByTrackingNumber_asCustomer() throws Exception {
        // Given
        String trackingNumber = "TRK-ABC123";
        String customerId = "customer-1";

        OrderResponse order = createOrderResponse(123L, trackingNumber, customerId);
        when(orderService.getByTrackingNumber(trackingNumber)).thenReturn(order);

        Map<String, Object> params = Map.of("trackingNumber", trackingNumber);
        UserContext context = new UserContext(customerId, "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.result()).contains(trackingNumber);
        assertThat(result.errorMessage()).isNull();

        verify(orderService).getByTrackingNumber(trackingNumber);
        verify(orderService, never()).getById(anyLong());
    }

    @Test
    void shouldDenyAccess_whenCustomerAccessesOtherCustomerOrder() {
        // Given
        Long orderId = 123L;
        String ownerCustomerId = "customer-1";
        String requestingCustomerId = "customer-2";

        OrderResponse order = createOrderResponse(orderId, "TRK-123", ownerCustomerId);
        when(orderService.getById(orderId)).thenReturn(order);

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext(requestingCustomerId, "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("can only view your own orders");

        verify(orderService).getById(orderId);
    }

    @Test
    void shouldAllowStaffToAccessAnyOrder() throws Exception {
        // Given
        Long orderId = 123L;
        String orderOwnerId = "customer-1";
        String staffId = "staff-1";

        OrderResponse order = createOrderResponse(orderId, "TRK-123", orderOwnerId);
        when(orderService.getById(orderId)).thenReturn(order);

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext(staffId, "WAREHOUSE_STAFF", Set.of("WAREHOUSE_STAFF"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then - Staff can access any order
        assertThat(result.success()).isTrue();
        assertThat(result.result()).contains("TRK-123");

        verify(orderService).getById(orderId);
    }

    @Test
    void shouldAllowManagerToAccessAnyOrder() throws Exception {
        // Given
        Long orderId = 123L;
        String orderOwnerId = "customer-1";
        String managerId = "manager-1";

        OrderResponse order = createOrderResponse(orderId, "TRK-123", orderOwnerId);
        when(orderService.getById(orderId)).thenReturn(order);

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext(managerId, "LOGISTICS_MANAGER", Set.of("LOGISTICS_MANAGER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then - Manager can access any order
        assertThat(result.success()).isTrue();
        assertThat(result.result()).contains("TRK-123");

        verify(orderService).getById(orderId);
    }

    @Test
    void shouldAllowDriverToAccessOrder() throws Exception {
        // Given
        Long orderId = 123L;
        String customerId = "customer-1";
        String driverId = "driver-1";

        OrderResponse order = createOrderResponse(orderId, "TRK-123", customerId);
        when(orderService.getById(orderId)).thenReturn(order);

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext(driverId, "DRIVER", Set.of("DRIVER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then - Driver can access orders
        assertThat(result.success()).isTrue();
        assertThat(result.result()).contains("TRK-123");

        verify(orderService).getById(orderId);
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
    }

    @Test
    void shouldReturnError_whenNeitherOrderIdNorTrackingNumberProvided() {
        // Given
        Map<String, Object> params = Map.of(); // Empty parameters
        UserContext context = new UserContext("user-1", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Must provide either orderId or trackingNumber");

        verify(orderService, never()).getById(anyLong());
        verify(orderService, never()).getByTrackingNumber(anyString());
    }

    @Test
    void shouldReturnError_whenBlankTrackingNumberProvided() {
        // Given
        Map<String, Object> params = Map.of("trackingNumber", "   ");
        UserContext context = new UserContext("user-1", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Must provide either orderId or trackingNumber");

        verify(orderService, never()).getByTrackingNumber(anyString());
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
        assertThat(result.errorMessage()).contains("Failed to lookup order");

        verify(orderService).getById(orderId);
    }

    @Test
    void shouldExtractLongFromNumber() throws Exception {
        // Given - LLM might send numbers in different formats
        Long orderId = 123L;
        String customerId = "customer-1";

        OrderResponse order = createOrderResponse(orderId, "TRK-123", customerId);
        when(orderService.getById(orderId)).thenReturn(order);

        // Using Integer instead of Long
        Map<String, Object> params = Map.of("orderId", 123);
        UserContext context = new UserContext(customerId, "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then - Should handle Integer and convert to Long
        assertThat(result.success()).isTrue();
        verify(orderService).getById(123L);
    }

    @Test
    void shouldExtractLongFromString() throws Exception {
        // Given
        Long orderId = 123L;
        String customerId = "customer-1";

        OrderResponse order = createOrderResponse(orderId, "TRK-123", customerId);
        when(orderService.getById(orderId)).thenReturn(order);

        // Using String representation of number
        Map<String, Object> params = Map.of("orderId", "123");
        UserContext context = new UserContext(customerId, "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then - Should parse string to Long
        assertThat(result.success()).isTrue();
        verify(orderService).getById(123L);
    }

    @Test
    void shouldPreferOrderId_whenBothProvided() throws Exception {
        // Given
        Long orderId = 123L;
        String trackingNumber = "TRK-ABC";
        String customerId = "customer-1";

        OrderResponse order = createOrderResponse(orderId, trackingNumber, customerId);
        when(orderService.getById(orderId)).thenReturn(order);

        Map<String, Object> params = Map.of(
            "orderId", orderId,
            "trackingNumber", trackingNumber
        );
        UserContext context = new UserContext(customerId, "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then - Should use orderId and not trackingNumber
        assertThat(result.success()).isTrue();
        verify(orderService).getById(orderId);
        verify(orderService, never()).getByTrackingNumber(anyString());
    }

    @Test
    void shouldIncludeOrderDataInResult() throws Exception {
        // Given
        Long orderId = 123L;
        String customerId = "customer-1";
        String trackingNumber = "TRK-123";

        OrderResponse order = createOrderResponse(orderId, trackingNumber, customerId);
        when(orderService.getById(orderId)).thenReturn(order);

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext(customerId, "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then - Result should contain order JSON
        assertThat(result.success()).isTrue();
        assertThat(result.result()).isNotNull();
        assertThat(result.result()).contains(trackingNumber);
        assertThat(result.result()).contains(customerId);

        // Verify it's valid JSON by parsing it
        OrderResponse parsedOrder = objectMapper.readValue(result.result(), OrderResponse.class);
        assertThat(parsedOrder.getId()).isEqualTo(orderId);
        assertThat(parsedOrder.getTrackingNumber()).isEqualTo(trackingNumber);
    }

    // Helper method to create test order responses
    private OrderResponse createOrderResponse(Long orderId, String trackingNumber, String customerId) {
        return OrderResponse.builder()
                .id(orderId)
                .trackingNumber(trackingNumber)
                .customerId(customerId)
                .reference("REF-" + orderId)
                .status(OrderStatus.IN_TRANSIT)
                .build();
    }
}

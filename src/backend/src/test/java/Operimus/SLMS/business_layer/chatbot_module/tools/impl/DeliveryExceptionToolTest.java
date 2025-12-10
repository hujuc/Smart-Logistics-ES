package Operimus.SLMS.business_layer.chatbot_module.tools.impl;

import Operimus.SLMS.business_layer.chatbot_module.dto.ToolResult;
import Operimus.SLMS.business_layer.chatbot_module.dto.UserContext;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import Operimus.SLMS.business_layer.visibility_module.exceptions.dto.DeliveryExceptionResponse;
import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException;
import Operimus.SLMS.business_layer.visibility_module.exceptions.service.DeliveryExceptionService;
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
 * Tests for DeliveryExceptionTool.
 * Verifies delivery exception listing with authorization.
 */
@ExtendWith(MockitoExtension.class)
class DeliveryExceptionToolTest {

    @Mock
    private DeliveryExceptionService exceptionService;

    @Mock
    private OrderService orderService;

    private ObjectMapper objectMapper;
    private DeliveryExceptionTool tool;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        tool = new DeliveryExceptionTool(exceptionService, orderService, objectMapper);
    }

    @Test
    void shouldReturnToolMetadata() {
        assertThat(tool.getName()).isEqualTo("delivery_exception");
        assertThat(tool.getDescription()).contains("List active delivery exceptions");
        assertThat(tool.getParameters()).hasSize(1);
        assertThat(tool.getParameters().get(0).name()).isEqualTo("orderId");
        assertThat(tool.getParameters().get(0).required()).isTrue();
        assertThat(tool.getAllowedRoles()).containsExactlyInAnyOrder("CUSTOMER", "CUSTOMER_SERVICE", "LOGISTICS_MANAGER");
    }

    @Test
    void shouldGetExceptions_asCustomer() throws Exception {
        // Given
        Long orderId = 123L;
        String customerId = "customer-1";

        OrderResponse order = createOrderResponse(orderId, customerId);
        List<DeliveryExceptionResponse> exceptions = createExceptions(orderId, 2);

        when(orderService.getById(orderId)).thenReturn(order);
        when(exceptionService.getByOrderId(orderId)).thenReturn(exceptions);

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext(customerId, "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.result()).contains("REF-123");
        assertThat(result.result()).contains("exceptionsCount");

        verify(orderService).getById(orderId);
        verify(exceptionService).getByOrderId(orderId);
    }

    @Test
    void shouldDenyAccess_whenCustomerAccessesOtherCustomerOrder() {
        // Given
        Long orderId = 123L;
        String ownerCustomerId = "customer-1";
        String requestingCustomerId = "customer-2";

        OrderResponse order = createOrderResponse(orderId, ownerCustomerId);
        when(orderService.getById(orderId)).thenReturn(order);

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext(requestingCustomerId, "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("can only view exceptions for your own orders");

        verify(orderService).getById(orderId);
        verify(exceptionService, never()).getByOrderId(anyLong());
    }

    @Test
    void shouldAllowCustomerServiceToAccessAnyOrder() throws Exception {
        // Given
        Long orderId = 123L;
        String orderOwnerId = "customer-1";
        String csStaffId = "cs-staff-1";

        OrderResponse order = createOrderResponse(orderId, orderOwnerId);
        List<DeliveryExceptionResponse> exceptions = createExceptions(orderId, 3);

        when(orderService.getById(orderId)).thenReturn(order);
        when(exceptionService.getByOrderId(orderId)).thenReturn(exceptions);

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext(csStaffId, "CUSTOMER_SERVICE", Set.of("CUSTOMER_SERVICE"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();

        verify(orderService).getById(orderId);
        verify(exceptionService).getByOrderId(orderId);
    }

    @Test
    void shouldAllowManagerToAccessAnyOrder() throws Exception {
        // Given
        Long orderId = 123L;
        String orderOwnerId = "customer-1";
        String managerId = "manager-1";

        OrderResponse order = createOrderResponse(orderId, orderOwnerId);
        List<DeliveryExceptionResponse> exceptions = createExceptions(orderId, 1);

        when(orderService.getById(orderId)).thenReturn(order);
        when(exceptionService.getByOrderId(orderId)).thenReturn(exceptions);

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext(managerId, "LOGISTICS_MANAGER", Set.of("LOGISTICS_MANAGER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();

        verify(orderService).getById(orderId);
        verify(exceptionService).getByOrderId(orderId);
    }

    @Test
    void shouldReturnError_whenOrderNotFound() {
        // Given
        Long orderId = 999L;

        when(orderService.getById(orderId))
            .thenThrow(new ResourceNotFoundException("Order not found: " + orderId));

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext("customer-1", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Order not found");

        verify(orderService).getById(orderId);
        verify(exceptionService, never()).getByOrderId(anyLong());
    }

    @Test
    void shouldReturnError_whenOrderIdNotProvided() {
        // Given
        Map<String, Object> params = Map.of(); // Missing orderId
        UserContext context = new UserContext("customer-1", "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("orderId is required");

        verify(orderService, never()).getById(anyLong());
        verify(exceptionService, never()).getByOrderId(anyLong());
    }

    @Test
    void shouldReturnError_whenOrderIdIsNull() {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", null);
        UserContext context = new UserContext("customer-1", "CUSTOMER", Set.of("CUSTOMER"));

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
        UserContext context = new UserContext("manager-1", "LOGISTICS_MANAGER", Set.of("LOGISTICS_MANAGER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Failed to get exceptions");

        verify(orderService).getById(orderId);
    }

    @Test
    void shouldHandleOrderWithNoExceptions() throws Exception {
        // Given
        Long orderId = 123L;
        String customerId = "customer-1";

        OrderResponse order = createOrderResponse(orderId, customerId);
        List<DeliveryExceptionResponse> exceptions = List.of(); // No exceptions

        when(orderService.getById(orderId)).thenReturn(order);
        when(exceptionService.getByOrderId(orderId)).thenReturn(exceptions);

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext(customerId, "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();

        @SuppressWarnings("unchecked")
        Map<String, Object> resultData = objectMapper.readValue(result.result(), Map.class);

        assertThat(resultData.get("exceptionsCount")).isEqualTo(0);
        @SuppressWarnings("unchecked")
        List<Object> exceptionsList = (List<Object>) resultData.get("exceptions");
        assertThat(exceptionsList).isEmpty();
    }

    @Test
    void shouldIncludeExceptionDetailsInResult() throws Exception {
        // Given
        Long orderId = 123L;
        String customerId = "customer-1";

        OrderResponse order = createOrderResponse(orderId, customerId);
        List<DeliveryExceptionResponse> exceptions = createExceptions(orderId, 3);

        when(orderService.getById(orderId)).thenReturn(order);
        when(exceptionService.getByOrderId(orderId)).thenReturn(exceptions);

        Map<String, Object> params = Map.of("orderId", orderId);
        UserContext context = new UserContext(customerId, "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();

        @SuppressWarnings("unchecked")
        Map<String, Object> resultData = objectMapper.readValue(result.result(), Map.class);

        assertThat(resultData).containsKeys("orderId", "orderReference", "exceptionsCount", "exceptions");
        assertThat(resultData.get("orderId")).isEqualTo(123);
        assertThat(resultData.get("orderReference")).isEqualTo("REF-123");
        assertThat(resultData.get("exceptionsCount")).isEqualTo(3);

        @SuppressWarnings("unchecked")
        List<Object> exceptionsList = (List<Object>) resultData.get("exceptions");
        assertThat(exceptionsList).hasSize(3);
    }

    @Test
    void shouldExtractLongFromInteger() throws Exception {
        // Given - LLM might send Integer instead of Long
        Long orderId = 123L;
        String customerId = "customer-1";

        OrderResponse order = createOrderResponse(orderId, customerId);
        List<DeliveryExceptionResponse> exceptions = createExceptions(orderId, 1);

        when(orderService.getById(orderId)).thenReturn(order);
        when(exceptionService.getByOrderId(orderId)).thenReturn(exceptions);

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

        OrderResponse order = createOrderResponse(orderId, customerId);
        List<DeliveryExceptionResponse> exceptions = createExceptions(orderId, 1);

        when(orderService.getById(orderId)).thenReturn(order);
        when(exceptionService.getByOrderId(orderId)).thenReturn(exceptions);

        Map<String, Object> params = Map.of("orderId", "123"); // String
        UserContext context = new UserContext(customerId, "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();
        verify(orderService).getById(123L);
    }

    // Helper methods
    private OrderResponse createOrderResponse(Long orderId, String customerId) {
        return OrderResponse.builder()
                .id(orderId)
                .customerId(customerId)
                .reference("REF-" + orderId)
                .status(OrderStatus.IN_TRANSIT)
                .build();
    }

    private List<DeliveryExceptionResponse> createExceptions(Long orderId, int count) {
        List<DeliveryExceptionResponse> exceptions = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            DeliveryExceptionResponse exception = DeliveryExceptionResponse.builder()
                    .id(java.util.UUID.randomUUID())
                    .orderId(orderId)
                    .type(DeliveryException.ExceptionType.DELAYED)
                    .severity(DeliveryException.Severity.HIGH)
                    .status(DeliveryException.ExceptionStatus.OPEN)
                    .description("Exception " + (i + 1))
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .build();
            exceptions.add(exception);
        }
        return exceptions;
    }
}

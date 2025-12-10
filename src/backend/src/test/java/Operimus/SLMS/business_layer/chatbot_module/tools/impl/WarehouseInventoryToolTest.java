package Operimus.SLMS.business_layer.chatbot_module.tools.impl;

import Operimus.SLMS.business_layer.chatbot_module.dto.ToolResult;
import Operimus.SLMS.business_layer.chatbot_module.dto.UserContext;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.dto.WarehouseResponse;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import Operimus.SLMS.business_layer.order_module.service.WarehouseService;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for WarehouseInventoryTool.
 * Verifies warehouse information retrieval with role-based access.
 */
@ExtendWith(MockitoExtension.class)
class WarehouseInventoryToolTest {

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private OrderService orderService;

    private ObjectMapper objectMapper;
    private WarehouseInventoryTool tool;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        tool = new WarehouseInventoryTool(warehouseService, orderService, objectMapper);
    }

    @Test
    void shouldReturnToolMetadata() {
        assertThat(tool.getName()).isEqualTo("warehouse_inventory");
        assertThat(tool.getDescription()).contains("Query warehouse information");
        assertThat(tool.getParameters()).hasSize(1);
        assertThat(tool.getParameters().get(0).name()).isEqualTo("warehouseId");
        assertThat(tool.getParameters().get(0).required()).isFalse(); // Optional parameter
        assertThat(tool.getAllowedRoles()).containsExactlyInAnyOrder("WAREHOUSE_STAFF", "LOGISTICS_MANAGER");
    }

    @Test
    void shouldGetWarehouseById_asWarehouseStaff() throws Exception {
        // Given
        Long warehouseId = 1L;
        WarehouseResponse warehouse = createWarehouseResponse(warehouseId, "Main Warehouse", 1000);
        List<OrderResponse> orders = createOrders(5);

        when(warehouseService.getById(warehouseId)).thenReturn(warehouse);
        when(orderService.getByWarehouse(warehouseId)).thenReturn(orders);

        Map<String, Object> params = Map.of("warehouseId", warehouseId);
        UserContext context = new UserContext("staff-1", "WAREHOUSE_STAFF", Set.of("WAREHOUSE_STAFF"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.result()).contains("Main Warehouse");
        assertThat(result.result()).contains("activeOrderCount");

        verify(warehouseService).getById(warehouseId);
        verify(orderService).getByWarehouse(warehouseId);
    }

    @Test
    void shouldGetWarehouseById_asManager() throws Exception {
        // Given
        Long warehouseId = 2L;
        WarehouseResponse warehouse = createWarehouseResponse(warehouseId, "North Warehouse", 500);
        List<OrderResponse> orders = createOrders(3);

        when(warehouseService.getById(warehouseId)).thenReturn(warehouse);
        when(orderService.getByWarehouse(warehouseId)).thenReturn(orders);

        Map<String, Object> params = Map.of("warehouseId", warehouseId);
        UserContext context = new UserContext("manager-1", "LOGISTICS_MANAGER", Set.of("LOGISTICS_MANAGER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.result()).contains("North Warehouse");

        verify(warehouseService).getById(warehouseId);
        verify(orderService).getByWarehouse(warehouseId);
    }

    @Test
    void shouldGetAllWarehouses_whenNoWarehouseIdProvided() throws Exception {
        // Given
        List<WarehouseResponse> warehouses = List.of(
            createWarehouseResponse(1L, "Main Warehouse", 1000),
            createWarehouseResponse(2L, "North Warehouse", 500),
            createWarehouseResponse(3L, "South Warehouse", 750)
        );

        when(warehouseService.getAll()).thenReturn(warehouses);

        Map<String, Object> params = Map.of(); // No warehouseId
        UserContext context = new UserContext("staff-1", "WAREHOUSE_STAFF", Set.of("WAREHOUSE_STAFF"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.result()).contains("Main Warehouse");
        assertThat(result.result()).contains("North Warehouse");
        assertThat(result.result()).contains("South Warehouse");

        verify(warehouseService).getAll();
        verify(orderService, never()).getByWarehouse(anyLong());
    }

    @Test
    void shouldIncludeActiveOrderCount_inSingleWarehouseQuery() throws Exception {
        // Given
        Long warehouseId = 1L;
        WarehouseResponse warehouse = createWarehouseResponse(warehouseId, "Test Warehouse", 1000);
        List<OrderResponse> orders = createOrders(7);

        when(warehouseService.getById(warehouseId)).thenReturn(warehouse);
        when(orderService.getByWarehouse(warehouseId)).thenReturn(orders);

        Map<String, Object> params = Map.of("warehouseId", warehouseId);
        UserContext context = new UserContext("staff-1", "WAREHOUSE_STAFF", Set.of("WAREHOUSE_STAFF"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();

        // Parse result to verify structure
        @SuppressWarnings("unchecked")
        Map<String, Object> resultData = objectMapper.readValue(result.result(), Map.class);

        assertThat(resultData).containsKeys("warehouse", "activeOrderCount");
        assertThat(resultData.get("activeOrderCount")).isEqualTo(7);
    }

    @Test
    void shouldReturnError_whenWarehouseNotFound() {
        // Given
        Long warehouseId = 999L;

        when(warehouseService.getById(warehouseId))
            .thenThrow(new ResourceNotFoundException("Warehouse not found: " + warehouseId));

        Map<String, Object> params = Map.of("warehouseId", warehouseId);
        UserContext context = new UserContext("staff-1", "WAREHOUSE_STAFF", Set.of("WAREHOUSE_STAFF"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Warehouse not found");

        verify(warehouseService).getById(warehouseId);
        verify(orderService, never()).getByWarehouse(anyLong());
    }

    @Test
    void shouldHandleWarehouseServiceException() {
        // Given
        Long warehouseId = 1L;

        when(warehouseService.getById(warehouseId))
            .thenThrow(new RuntimeException("Database connection failed"));

        Map<String, Object> params = Map.of("warehouseId", warehouseId);
        UserContext context = new UserContext("staff-1", "WAREHOUSE_STAFF", Set.of("WAREHOUSE_STAFF"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Failed to get warehouse info");

        verify(warehouseService).getById(warehouseId);
    }

    @Test
    void shouldHandleWarehouseWithNoActiveOrders() throws Exception {
        // Given
        Long warehouseId = 1L;
        WarehouseResponse warehouse = createWarehouseResponse(warehouseId, "Empty Warehouse", 1000);
        List<OrderResponse> orders = List.of(); // No orders

        when(warehouseService.getById(warehouseId)).thenReturn(warehouse);
        when(orderService.getByWarehouse(warehouseId)).thenReturn(orders);

        Map<String, Object> params = Map.of("warehouseId", warehouseId);
        UserContext context = new UserContext("staff-1", "WAREHOUSE_STAFF", Set.of("WAREHOUSE_STAFF"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();

        @SuppressWarnings("unchecked")
        Map<String, Object> resultData = objectMapper.readValue(result.result(), Map.class);

        assertThat(resultData.get("activeOrderCount")).isEqualTo(0);
    }

    @Test
    void shouldExtractLongFromInteger() throws Exception {
        // Given - LLM might send Integer instead of Long
        Long warehouseId = 1L;
        WarehouseResponse warehouse = createWarehouseResponse(warehouseId, "Test Warehouse", 1000);
        List<OrderResponse> orders = createOrders(2);

        when(warehouseService.getById(warehouseId)).thenReturn(warehouse);
        when(orderService.getByWarehouse(warehouseId)).thenReturn(orders);

        Map<String, Object> params = Map.of("warehouseId", 1); // Integer
        UserContext context = new UserContext("staff-1", "WAREHOUSE_STAFF", Set.of("WAREHOUSE_STAFF"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();
        verify(warehouseService).getById(1L);
    }

    @Test
    void shouldExtractLongFromString() throws Exception {
        // Given
        Long warehouseId = 1L;
        WarehouseResponse warehouse = createWarehouseResponse(warehouseId, "Test Warehouse", 1000);
        List<OrderResponse> orders = createOrders(1);

        when(warehouseService.getById(warehouseId)).thenReturn(warehouse);
        when(orderService.getByWarehouse(warehouseId)).thenReturn(orders);

        Map<String, Object> params = Map.of("warehouseId", "1"); // String
        UserContext context = new UserContext("staff-1", "WAREHOUSE_STAFF", Set.of("WAREHOUSE_STAFF"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();
        verify(warehouseService).getById(1L);
    }

    @Test
    void shouldHandleNullWarehouseId_asListAllWarehouses() throws Exception {
        // Given - Explicitly null warehouseId
        List<WarehouseResponse> warehouses = List.of(
            createWarehouseResponse(1L, "Warehouse 1", 1000)
        );

        when(warehouseService.getAll()).thenReturn(warehouses);

        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", null);
        UserContext context = new UserContext("staff-1", "WAREHOUSE_STAFF", Set.of("WAREHOUSE_STAFF"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then - Should list all warehouses
        assertThat(result.success()).isTrue();
        verify(warehouseService).getAll();
        verify(warehouseService, never()).getById(anyLong());
    }

    @Test
    void shouldHandleEmptyWarehouseList() throws Exception {
        // Given
        List<WarehouseResponse> warehouses = List.of(); // No warehouses

        when(warehouseService.getAll()).thenReturn(warehouses);

        Map<String, Object> params = Map.of();
        UserContext context = new UserContext("manager-1", "LOGISTICS_MANAGER", Set.of("LOGISTICS_MANAGER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then - Should succeed but return empty list
        assertThat(result.success()).isTrue();
        assertThat(result.result()).isEqualTo("[]");

        verify(warehouseService).getAll();
    }

    // Helper methods
    private WarehouseResponse createWarehouseResponse(Long id, String name, int capacity) {
        return WarehouseResponse.builder()
                .id(id)
                .name(name)
                .addressLine1("Address " + id)
                .city("City " + id)
                .country("Country")
                .active(true)
                .build();
    }

    private List<OrderResponse> createOrders(int count) {
        List<OrderResponse> orders = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            OrderResponse order = OrderResponse.builder()
                    .id((long) (i + 1))
                    .reference("REF-" + (i + 1))
                    .build();
            orders.add(order);
        }
        return orders;
    }
}

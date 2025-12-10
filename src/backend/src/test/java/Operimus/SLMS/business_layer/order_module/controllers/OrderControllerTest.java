package Operimus.SLMS.business_layer.order_module.controllers;

import Operimus.SLMS.business_layer.order_module.dto.OrderRequest;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private OrderResponse testOrder;
    private OrderRequest testOrderRequest;

    @BeforeEach
    void setUp() {
        testOrder = OrderResponse.builder()
                .id(1L)
                .reference("ORD-001")
                .description("Test order")
                .status(OrderStatus.CREATED)
                .shippingAddress("123 Main St, Aveiro")
                .warehouseId(1L)
                .trackingNumber("TRK-001")
                .customerId("customer-123")
                .weight(5.0)
                .weightUnit("kg")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        testOrderRequest = OrderRequest.builder()
                .reference("ORD-001")
                .description("Test order")
                .shippingAddress("123 Main St, Aveiro")
                .warehouseId(1L)
                .customerId("customer-123")
                .weight(5.0)
                .weightUnit("kg")
                .build();
    }

    // ========================================
    // Get All Orders Tests
    // ========================================

    @Test
    void whenGetOrders_thenReturnOrdersList() throws Exception {
        List<OrderResponse> orders = Arrays.asList(testOrder);
        when(orderService.getAll()).thenReturn(orders);

        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].reference", is("ORD-001")))
                .andExpect(jsonPath("$[0].status", is("CREATED")));

        verify(orderService).getAll();
    }

    @Test
    void whenGetOrdersByStatus_thenReturnFilteredList() throws Exception {
        List<OrderResponse> orders = Arrays.asList(testOrder);
        when(orderService.getByStatus(anyList())).thenReturn(orders);

        mockMvc.perform(get("/api/admin/orders")
                        .param("status", "CREATED,IN_TRANSIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("CREATED")));

        verify(orderService).getByStatus(anyList());
    }

    @Test
    void whenGetOrdersByCustomerId_thenReturnFilteredList() throws Exception {
        List<OrderResponse> orders = Arrays.asList(testOrder);
        when(orderService.getByCustomerId("customer-123")).thenReturn(orders);

        mockMvc.perform(get("/api/admin/orders")
                        .param("customerId", "customer-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerId", is("customer-123")));

        verify(orderService).getByCustomerId("customer-123");
    }

    @Test
    void whenGetOrdersEmpty_thenReturnEmptyList() throws Exception {
        when(orderService.getAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(orderService).getAll();
    }

    // ========================================
    // Get Order by ID Tests
    // ========================================

    @Test
    void whenGetOrderById_thenReturnOrder() throws Exception {
        when(orderService.getById(1L)).thenReturn(testOrder);

        mockMvc.perform(get("/api/admin/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.reference", is("ORD-001")))
                .andExpect(jsonPath("$.trackingNumber", is("TRK-001")));

        verify(orderService).getById(1L);
    }

    @Test
    void whenGetOrderByIdNotFound_thenReturn404() throws Exception {
        when(orderService.getById(999L))
                .thenThrow(new ResourceNotFoundException("Order 999 not found"));

        mockMvc.perform(get("/api/admin/orders/999"))
                .andExpect(status().isNotFound());

        verify(orderService).getById(999L);
    }

    // ========================================
    // Get Order by Tracking Number Tests
    // ========================================

    @Test
    void whenGetOrderByTracking_thenReturnOrder() throws Exception {
        when(orderService.getByTrackingNumber("TRK-001")).thenReturn(testOrder);

        mockMvc.perform(get("/api/admin/orders/tracking/TRK-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.trackingNumber", is("TRK-001")));

        verify(orderService).getByTrackingNumber("TRK-001");
    }

    @Test
    void whenGetOrderByTrackingNotFound_thenReturn404() throws Exception {
        when(orderService.getByTrackingNumber("INVALID"))
                .thenThrow(new ResourceNotFoundException("Order with tracking INVALID not found"));

        mockMvc.perform(get("/api/admin/orders/tracking/INVALID"))
                .andExpect(status().isNotFound());

        verify(orderService).getByTrackingNumber("INVALID");
    }

    // ========================================
    // Create Order Tests
    // ========================================

    @Test
    void whenCreateOrder_thenReturn201() throws Exception {
        when(orderService.create(any(OrderRequest.class))).thenReturn(testOrder);

        mockMvc.perform(post("/api/admin/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/admin/orders/1")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.reference", is("ORD-001")));

        verify(orderService).create(any(OrderRequest.class));
    }

    @Test
    void whenCreateOrderWithInvalidData_thenReturn400() throws Exception {
        OrderRequest invalidRequest = OrderRequest.builder()
                .reference("")
                .build();

        mockMvc.perform(post("/api/admin/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).create(any());
    }

    // ========================================
    // Update Order Tests
    // ========================================

    @Test
    void whenUpdateOrder_thenReturnUpdatedOrder() throws Exception {
        OrderResponse updatedOrder = OrderResponse.builder()
                .id(1L)
                .reference("ORD-001")
                .description("Updated order")
                .status(OrderStatus.IN_TRANSIT)
                .shippingAddress("456 Updated St, Porto")
                .warehouseId(1L)
                .customerId("customer-123")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        when(orderService.update(eq(1L), any(OrderRequest.class))).thenReturn(updatedOrder);

        mockMvc.perform(put("/api/admin/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Updated order")))
                .andExpect(jsonPath("$.status", is("IN_TRANSIT")));

        verify(orderService).update(eq(1L), any(OrderRequest.class));
    }

    @Test
    void whenUpdateOrderNotFound_thenReturn404() throws Exception {
        when(orderService.update(eq(999L), any(OrderRequest.class)))
                .thenThrow(new ResourceNotFoundException("Order 999 not found"));

        mockMvc.perform(put("/api/admin/orders/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andExpect(status().isNotFound());

        verify(orderService).update(eq(999L), any(OrderRequest.class));
    }

    // ========================================
    // Delete Order Tests
    // ========================================

    @Test
    void whenDeleteOrder_thenReturn204() throws Exception {
        doNothing().when(orderService).delete(1L);

        mockMvc.perform(delete("/api/admin/orders/1"))
                .andExpect(status().isNoContent());

        verify(orderService).delete(1L);
    }

    @Test
    void whenDeleteOrderNotFound_thenReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Order 999 not found"))
                .when(orderService).delete(999L);

        mockMvc.perform(delete("/api/admin/orders/999"))
                .andExpect(status().isNotFound());

        verify(orderService).delete(999L);
    }

    // ========================================
    // Edge Cases Tests
    // ========================================

    @Test
    void whenGetOrdersWithEmptyStatus_thenReturnAllOrders() throws Exception {
        List<OrderResponse> orders = Arrays.asList(testOrder);
        when(orderService.getAll()).thenReturn(orders);

        mockMvc.perform(get("/api/admin/orders")
                        .param("status", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(orderService).getAll();
    }

    @Test
    void whenGetOrdersWithEmptyCustomerId_thenReturnAllOrders() throws Exception {
        List<OrderResponse> orders = Arrays.asList(testOrder);
        when(orderService.getAll()).thenReturn(orders);

        mockMvc.perform(get("/api/admin/orders")
                        .param("customerId", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(orderService).getAll();
    }

    @Test
    void whenGetOrdersByMultipleStatuses_thenReturnFilteredList() throws Exception {
        List<OrderResponse> orders = Arrays.asList(testOrder);
        when(orderService.getByStatus(anyList())).thenReturn(orders);

        mockMvc.perform(get("/api/admin/orders")
                        .param("status", "CREATED,IN_TRANSIT,DELIVERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(orderService).getByStatus(anyList());
    }
}

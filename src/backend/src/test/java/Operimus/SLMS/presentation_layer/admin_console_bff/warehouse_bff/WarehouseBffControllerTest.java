package Operimus.SLMS.presentation_layer.admin_console_bff.warehouse_bff;

import Operimus.SLMS.business_layer.order_shipment_module.dto.DocumentDownloadResponse;
import Operimus.SLMS.business_layer.order_shipment_module.dto.DocumentUploadResponse;
import Operimus.SLMS.business_layer.order_shipment_module.dto.OrderRequest;
import Operimus.SLMS.business_layer.order_shipment_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrderStatus;
import Operimus.SLMS.business_layer.order_shipment_module.service.OrderDocumentService;
import Operimus.SLMS.business_layer.order_shipment_module.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WarehouseBffController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class WarehouseBffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderDocumentService orderDocumentService;

    // ========================================
    // Health Check Tests
    // ========================================

    @Test
    void whenHealthCheck_thenReturnStatus() throws Exception {
        mockMvc.perform(get("/api/warehouse/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.service", is("Warehouse BFF")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ========================================
    // Get All Orders Tests
    // ========================================

    @Test
    void whenGetOrders_thenReturnOrdersList() throws Exception {
        List<OrderResponse> orders = Arrays.asList(
                createOrderResponse(1L, "ORD-001", ShipmentOrderStatus.CREATED),
                createOrderResponse(2L, "ORD-002", ShipmentOrderStatus.COLLECTED)
        );

        when(orderService.getAll()).thenReturn(orders);

        mockMvc.perform(get("/api/warehouse/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].reference", is("ORD-001")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].reference", is("ORD-002")));

        verify(orderService).getAll();
    }

    @Test
    void whenGetOrdersWithFilters_thenReturnFilteredOrders() throws Exception {
        List<OrderResponse> orders = Arrays.asList(
                createOrderResponse(1L, "ORD-001", ShipmentOrderStatus.CREATED)
        );

        when(orderService.getAll()).thenReturn(orders);

        mockMvc.perform(get("/api/warehouse/orders")
                        .param("status", "PENDING")
                        .param("search", "ORD-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(orderService).getAll();
    }

    @Test
    void whenGetOrdersEmpty_thenReturnEmptyList() throws Exception {
        when(orderService.getAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/warehouse/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ========================================
    // Get Order by ID Tests
    // ========================================

    @Test
    void whenGetOrderById_thenReturnOrder() throws Exception {
        OrderResponse order = createOrderResponse(1L, "ORD-001", ShipmentOrderStatus.CREATED);

        when(orderService.getById(1L)).thenReturn(order);

        mockMvc.perform(get("/api/warehouse/orders/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.reference", is("ORD-001")))
                .andExpect(jsonPath("$.status", is("CREATED")));

        verify(orderService).getById(1L);
    }

    // ========================================
    // Create Order Tests
    // ========================================

    @Test
    void whenCreateOrder_thenReturnCreatedOrder() throws Exception {
        OrderRequest request = createOrderRequest("ORD-001");
        OrderResponse response = createOrderResponse(1L, "ORD-001", ShipmentOrderStatus.CREATED);

        when(orderService.create(any(OrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/warehouse/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/warehouse/orders/1")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.reference", is("ORD-001")));

        verify(orderService).create(any(OrderRequest.class));
    }

    @Test
    void whenCreateOrderWithInvalidData_thenBadRequest() throws Exception {
        OrderRequest invalidRequest = new OrderRequest();
        // Invalid request with no data

        mockMvc.perform(post("/api/warehouse/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ========================================
    // Update Order Tests
    // ========================================

    @Test
    void whenUpdateOrder_thenReturnUpdatedOrder() throws Exception {
        OrderRequest request = createOrderRequest("ORD-001-UPDATED");
        OrderResponse response = createOrderResponse(1L, "ORD-001-UPDATED", ShipmentOrderStatus.READY_FOR_PICKUP);

        when(orderService.update(eq(1L), any(OrderRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/warehouse/orders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.reference", is("ORD-001-UPDATED")));

        verify(orderService).update(eq(1L), any(OrderRequest.class));
    }

    // ========================================
    // Confirm Pickup Tests
    // ========================================

    @Test
    void whenConfirmPickup_thenReturnUpdatedOrder() throws Exception {
        OrderResponse response = createOrderResponse(1L, "ORD-001", ShipmentOrderStatus.COLLECTED);

        when(orderService.confirmPickup(1L)).thenReturn(response);

        mockMvc.perform(post("/api/warehouse/orders/{id}/collect", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("COLLECTED")));

        verify(orderService).confirmPickup(1L);
    }

    // ========================================
    // Delete Order Tests
    // ========================================

    @Test
    void whenDeleteOrder_thenReturnNoContent() throws Exception {
        doNothing().when(orderService).delete(1L);

        mockMvc.perform(delete("/api/warehouse/orders/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(orderService).delete(1L);
    }

    // ========================================
    // List Documents Tests
    // ========================================

    @Test
    void whenListDocuments_thenReturnDocumentsList() throws Exception {
        List<DocumentUploadResponse> documents = Arrays.asList(
                createDocumentUploadResponse(1L, "doc1.pdf"),
                createDocumentUploadResponse(1L, "doc2.pdf")
        );

        when(orderDocumentService.listDocuments(1L)).thenReturn(documents);

        mockMvc.perform(get("/api/warehouse/orders/{orderId}/documents", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].documentId", is("doc1.pdf")))
                .andExpect(jsonPath("$[1].documentId", is("doc2.pdf")));

        verify(orderDocumentService).listDocuments(1L);
    }

    @Test
    void whenListDocumentsEmpty_thenReturnEmptyList() throws Exception {
        when(orderDocumentService.listDocuments(1L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/warehouse/orders/{orderId}/documents", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ========================================
    // Download Document Tests
    // ========================================

    @Test
    void whenDownloadDocument_thenReturnDocument() throws Exception {
        DocumentDownloadResponse downloadResponse = DocumentDownloadResponse.builder()
                .fileName("invoice.pdf")
                .contentType("application/pdf")
                .size(1024L)
                .content("PDF content".getBytes())
                .build();

        when(orderDocumentService.downloadDocument(1L, "doc1.pdf"))
                .thenReturn(downloadResponse);

        mockMvc.perform(get("/api/warehouse/orders/{orderId}/documents/{documentId}/download",
                        1L, "doc1.pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        containsString("attachment; filename=\"invoice.pdf\"")))
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes("PDF content".getBytes()));

        verify(orderDocumentService).downloadDocument(1L, "doc1.pdf");
    }

    // ========================================
    // Helper Methods
    // ========================================

    private OrderResponse createOrderResponse(Long id, String reference, ShipmentOrderStatus status) {
        return OrderResponse.builder()
                .id(id)
                .reference(reference)
                .status(status)
                .shippingAddress("123 Shipping St, City, Country")
                .description("Test order description")
                .warehouseId(1L)
                .build();
    }

    private OrderRequest createOrderRequest(String reference) {
        return OrderRequest.builder()
                .reference(reference)
                .shippingAddress("123 Shipping St, City, Country")
                .description("Test order description")
                .warehouseId(1L)
                .build();
    }

    private DocumentUploadResponse createDocumentUploadResponse(Long orderId, String documentId) {
        return DocumentUploadResponse.builder()
                .orderId(orderId)
                .documentId(documentId)
                .fileName(documentId)
                .contentType("application/pdf")
                .size(1024L)
                .url("http://localhost/bucket/orders/" + orderId + "/" + documentId)
                .objectKey("orders/" + orderId + "/" + documentId)
                .build();
    }
}

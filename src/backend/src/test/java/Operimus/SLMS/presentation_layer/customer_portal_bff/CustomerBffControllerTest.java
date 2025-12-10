package Operimus.SLMS.presentation_layer.customer_portal_bff;

import Operimus.SLMS.business_layer.order_module.dto.OrderEventResponse;
import Operimus.SLMS.business_layer.order_module.dto.OrderRequest;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.dto.WarehouseResponse;
import Operimus.SLMS.business_layer.order_module.dto.DocumentUploadResponse;
import Operimus.SLMS.business_layer.order_module.dto.DocumentDownloadResponse;
import Operimus.SLMS.business_layer.visibility_module.exceptions.dto.DeliveryExceptionRequest;
import Operimus.SLMS.business_layer.visibility_module.exceptions.dto.DeliveryExceptionResponse;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import Operimus.SLMS.business_layer.order_module.service.OrderEventService;
import Operimus.SLMS.business_layer.order_module.service.OrderDocumentService;
import Operimus.SLMS.business_layer.order_module.service.WarehouseService;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;
import Operimus.SLMS.business_layer.visibility_module.exceptions.service.DeliveryExceptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CustomerBffController.class,
    excludeAutoConfiguration = {OAuth2ResourceServerAutoConfiguration.class, KafkaAutoConfiguration.class})
@ActiveProfiles("test")
class CustomerBffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderEventService orderEventService;

    @MockitoBean
    private WarehouseService warehouseService;

    @MockitoBean
    private DeliveryExceptionService exceptionService;

    @MockitoBean
    private OrderDocumentService orderDocumentService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private KeycloakUserService keycloakUserService;

    private String customerId;
    private OrderResponse mockOrder;
    private WarehouseResponse mockWarehouse;

    @BeforeEach
    void setUp() {
        customerId = java.util.UUID.randomUUID().toString();
        
        mockWarehouse = WarehouseResponse.builder()
                .id(1L)
                .code("WH001")
                .name("Main Warehouse")
                .city("Aveiro")
                .country("Portugal")
                .active(true)
                .build();

        mockOrder = OrderResponse.builder()
                .id(1L)
                .reference("ORD-001")
                .description("Test order")
                .status(OrderStatus.CREATED)
                .shippingAddress("Test Address")
                .warehouseId(1L)
                .trackingNumber("TRK-001")
                .customerId(customerId)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor createJwtWithCustomerRole(String customerId) {
        return jwt()
                .jwt(jwt -> jwt.subject(customerId))
                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    @Test
    void whenHealthCheck_thenReturnStatus() throws Exception {
        mockMvc.perform(get("/api/customer/health")
                        .with(createJwtWithCustomerRole(customerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.service", is("Customer BFF")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void whenGetWarehouses_thenReturnWarehouseList() throws Exception {
        when(warehouseService.getAll()).thenReturn(List.of(mockWarehouse));

        mockMvc.perform(get("/api/customer/warehouses")
                        .with(createJwtWithCustomerRole(customerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Main Warehouse")))
                .andExpect(jsonPath("$[0].city", is("Aveiro")));
    }

    @Test
    void whenGetCustomerOrders_thenReturnOrderList() throws Exception {
        when(orderService.getByCustomerId(anyString())).thenReturn(List.of(mockOrder));

        mockMvc.perform(get("/api/customer/orders")
                        .with(createJwtWithCustomerRole(customerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].reference", is("ORD-001")));
    }

    @Test
    void whenGetOrderById_withValidCustomer_thenReturnOrder() throws Exception {
        when(orderService.getById(1L)).thenReturn(mockOrder);

        mockMvc.perform(get("/api/customer/orders/1")
                        .with(createJwtWithCustomerRole(customerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.reference", is("ORD-001")));
    }

    @Test
    void whenGetOrderByTracking_withValidCustomer_thenReturnOrder() throws Exception {
        when(orderService.getByTrackingNumber("TRK-001")).thenReturn(mockOrder);

        mockMvc.perform(get("/api/customer/orders/tracking/TRK-001")
                        .with(createJwtWithCustomerRole(customerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingNumber", is("TRK-001")));
    }

    @Test
    void whenGetOrderEvents_withValidCustomer_thenReturnEvents() throws Exception {
        OrderEventResponse event = OrderEventResponse.builder()
                .id(1L)
                .orderId(1L)
                .eventType(OrderStatus.CREATED)
                .notes("Order created")
                .build();

        when(orderService.getById(1L)).thenReturn(mockOrder);
        when(orderEventService.getEventsForOrder(1L)).thenReturn(List.of(event));

        mockMvc.perform(get("/api/customer/orders/1/events")
                        .with(createJwtWithCustomerRole(customerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderId", is(1)))
                .andExpect(jsonPath("$[0].eventType", is("CREATED")));
    }

    @Test
    void whenCreateOrder_thenReturnCreatedOrder() throws Exception {
        OrderRequest request = OrderRequest.builder()
                .reference("ORD-002")
                .description("New order")
                .shippingAddress("New Address")
                .warehouseId(1L)
                .build();

        OrderResponse createdOrder = OrderResponse.builder()
                .id(2L)
                .reference("ORD-002")
                .description("New order")
                .status(OrderStatus.CREATED)
                .shippingAddress("New Address")
                .warehouseId(1L)
                .customerId(customerId)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        when(orderService.create(any(OrderRequest.class))).thenReturn(createdOrder);

        mockMvc.perform(post("/api/customer/orders")
                        .with(createJwtWithCustomerRole(customerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.reference", is("ORD-002")));
    }

    @Test
    void whenCreateException_withValidOrder_thenReturnCreatedException() throws Exception {
        DeliveryExceptionRequest request = DeliveryExceptionRequest.builder()
                .orderId(1L)
                .type(Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.ExceptionType.LOST)
                .category("Delivery Issue")
                .description("Package lost")
                .source(Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.Source.CUSTOMER)
                .build();

        DeliveryExceptionResponse response = DeliveryExceptionResponse.builder()
                .id(java.util.UUID.randomUUID())
                .orderId(1L)
                .type(Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.ExceptionType.LOST)
                .description("Package lost")
                .status(Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.ExceptionStatus.OPEN)
                .build();

        // Mock order check
        when(orderService.getById(1L)).thenReturn(mockOrder);
        // Mock exception creation
        when(exceptionService.create(any(DeliveryExceptionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/customer/delivery-exceptions")
                        .with(createJwtWithCustomerRole(customerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId", is(1)))
                .andExpect(jsonPath("$.description", is("Package lost")));
    }

    @Test
    void whenGetCustomerExceptions_thenReturnList() throws Exception {
        DeliveryExceptionResponse exception = DeliveryExceptionResponse.builder()
                .id(java.util.UUID.randomUUID())
                .orderId(1L)
                .description("Test exception")
                .build();

        // Since customerId is now a valid UUID, the controller will use the UUID-based service method
        when(exceptionService.getByCustomerId(java.util.UUID.fromString(customerId))).thenReturn(List.of(exception));

        mockMvc.perform(get("/api/customer/delivery-exceptions")
                        .with(createJwtWithCustomerRole(customerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description", is("Test exception")));
    }

    // ========================================
    // Document Tests
    // ========================================

    @Test
    void whenGetOrderDocuments_withValidOrder_thenReturnList() throws Exception {
        DocumentUploadResponse doc = DocumentUploadResponse.builder()
                .fileName("invoice.pdf")
                .contentType("application/pdf")
                .build();

        when(orderService.getById(1L)).thenReturn(mockOrder);
        when(orderDocumentService.listDocuments(1L)).thenReturn(List.of(doc));

        mockMvc.perform(get("/api/customer/orders/1/documents")
                        .with(createJwtWithCustomerRole(customerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fileName", is("invoice.pdf")));
    }

    @Test
    void whenDownloadDocument_withValidOrder_thenReturnContent() throws Exception {
        DocumentDownloadResponse doc = DocumentDownloadResponse.builder()
                .fileName("invoice.pdf")
                .contentType("application/pdf")
                .content("fake-pdf-content".getBytes())
                .build();

        when(orderService.getById(1L)).thenReturn(mockOrder);
        when(orderDocumentService.downloadDocument(1L, "invoice.pdf")).thenReturn(doc);

        mockMvc.perform(get("/api/customer/orders/1/documents/invoice.pdf/download")
                        .with(createJwtWithCustomerRole(customerId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", containsString("invoice.pdf")));
    }
}

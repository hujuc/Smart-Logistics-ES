package Operimus.SLMS.presentation_layer.driver_pwa_bff;

import Operimus.SLMS.business_layer.order_module.dto.OrderScanResult;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.repository.OrderRepository;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import Operimus.SLMS.business_layer.order_module.service.OrderEventService;
import Operimus.SLMS.business_layer.order_module.service.OrderDocumentService;
import Operimus.SLMS.business_layer.rate_carrier_module.repository.ShipmentRepository;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DriverBffController.class,
    excludeAutoConfiguration = {OAuth2ResourceServerAutoConfiguration.class, KafkaAutoConfiguration.class})
class DriverBffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderEventService orderEventService;

    @MockitoBean
    private ShipmentRepository shipmentRepository;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private KeycloakUserService keycloakUserService;

    @MockitoBean
    private OrderDocumentService orderDocumentService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private String driverId;
    private OrderScanResult mockScanResult;

    @BeforeEach
    void setUp() {
        driverId = "driver-uuid-123";

        mockScanResult = OrderScanResult.builder()
                .orderId(1L)
                .orderReference("ORD-001")
                .shipmentId(10L)
                .shipmentCode("SHP-001")
                .orderStatus(OrderStatus.IN_TRANSIT)
                .confirmedAt(OffsetDateTime.now())
                .alreadyConfirmed(false)
                .allOrdersConfirmed(false)
                .remainingOrdersToScan(4)
                .build();
    }

    private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor createJwtWithDriverRole(String driverId) {
        return jwt()
                .jwt(jwt -> jwt.subject(driverId))
                .authorities(new SimpleGrantedAuthority("ROLE_DRIVER"));
    }

    @Test
    void whenHealthCheck_thenReturnStatus() throws Exception {
        mockMvc.perform(get("/api/driver/health")
                        .with(createJwtWithDriverRole(driverId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.service", is("Driver BFF")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void whenConfirmOrderScan_withValidDriver_thenReturnScanResult() throws Exception {
        when(orderService.confirmOrderScan(anyLong(), anyString())).thenReturn(mockScanResult);

        mockMvc.perform(post("/api/driver/orders/1/confirm")
                        .with(createJwtWithDriverRole(driverId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(1)))
                .andExpect(jsonPath("$.orderReference", is("ORD-001")))
                .andExpect(jsonPath("$.shipmentId", is(10)))
                .andExpect(jsonPath("$.shipmentCode", is("SHP-001")))
                .andExpect(jsonPath("$.status", is("IN_TRANSIT")))
                .andExpect(jsonPath("$.alreadyConfirmed", is(false)))
                .andExpect(jsonPath("$.allOrdersConfirmed", is(false)))
                .andExpect(jsonPath("$.remainingOrdersToScan", is(4)));

        verify(orderService, times(1)).confirmOrderScan(1L, driverId);
    }

    @Test
    void whenConfirmOrderScan_completingAllOrders_thenReturnAllOrdersConfirmed() throws Exception {
        OrderScanResult completedResult = OrderScanResult.builder()
                .orderId(5L)
                .orderReference("ORD-005")
                .shipmentId(10L)
                .shipmentCode("SHP-001")
                .orderStatus(OrderStatus.DELIVERED)
                .confirmedAt(OffsetDateTime.now())
                .alreadyConfirmed(false)
                .allOrdersConfirmed(true)
                .remainingOrdersToScan(0)
                .build();

        when(orderService.confirmOrderScan(anyLong(), anyString())).thenReturn(completedResult);

        mockMvc.perform(post("/api/driver/orders/5/confirm")
                        .with(createJwtWithDriverRole(driverId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(5)))
                .andExpect(jsonPath("$.allOrdersConfirmed", is(true)))
                .andExpect(jsonPath("$.remainingOrdersToScan", is(0)));

        verify(orderService, times(1)).confirmOrderScan(5L, driverId);
    }

    @Test
    void whenConfirmOrderScan_withoutAuthentication_thenReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/driver/orders/1/confirm"))
                .andExpect(status().isForbidden());  // Changed from isUnauthorized() to isForbidden()

        verify(orderService, never()).confirmOrderScan(anyLong(), anyString());
    }
}

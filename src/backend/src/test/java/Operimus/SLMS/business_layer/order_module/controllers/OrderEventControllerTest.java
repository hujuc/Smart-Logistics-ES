package Operimus.SLMS.business_layer.order_module.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Operimus.SLMS.business_layer.order_module.dto.OrderEventResponse;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.service.OrderEventService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(controllers = OrderEventController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class OrderEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderEventService orderEventService;

    @Test
    void listEventsShouldReturnTimeline() throws Exception {
        List<OrderEventResponse> responses = List.of(
                OrderEventResponse.builder()
                        .id(1L)
                        .orderId(10L)
                        .eventType(OrderStatus.CREATED)
                        .occurredAt(OffsetDateTime.now().minusHours(2))
                        .notes("Created")
                        .build(),
                OrderEventResponse.builder()
                        .id(2L)
                        .orderId(10L)
                        .eventType(OrderStatus.IN_TRANSIT)
                        .occurredAt(OffsetDateTime.now().minusHours(1))
                        .notes("In transit")
                        .build());
        when(orderEventService.getEventsForOrder(10L)).thenReturn(responses);

        mockMvc.perform(get("/api/admin/orders/{orderId}/events", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].eventType", is("CREATED")))
                .andExpect(jsonPath("$[1].eventType", is("IN_TRANSIT")));

        verify(orderEventService).getEventsForOrder(10L);
    }

    @Test
    void getLatestEventShouldReturnLatestEntry() throws Exception {
        OrderEventResponse response = OrderEventResponse.builder()
                .id(3L)
                .orderId(10L)
                .eventType(OrderStatus.DELIVERED)
                .occurredAt(OffsetDateTime.now())
                .notes("Delivered to receiver")
                .build();
        when(orderEventService.getLatestEventForOrder(10L)).thenReturn(response);

        mockMvc.perform(get("/api/admin/orders/{orderId}/events/latest", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.eventType", is("DELIVERED")))
                .andExpect(jsonPath("$.notes", is("Delivered to receiver")));

        verify(orderEventService).getLatestEventForOrder(eq(10L));
    }
}

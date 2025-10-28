package Operimus.SLMS.business_layer.order_shipment_module.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Operimus.SLMS.business_layer.order_shipment_module.dto.OrderEventResponse;
import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrderStatus;
import Operimus.SLMS.business_layer.order_shipment_module.service.ShipmentOrderEventService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrderEventController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShipmentOrderEventService shipmentOrderEventService;

    @Test
    void listEventsShouldReturnTimeline() throws Exception {
        List<OrderEventResponse> responses = List.of(
                OrderEventResponse.builder()
                        .id(1L)
                        .orderId(10L)
                        .eventType(ShipmentOrderStatus.CREATED)
                        .occurredAt(OffsetDateTime.now().minusHours(2))
                        .notes("Created")
                        .build(),
                OrderEventResponse.builder()
                        .id(2L)
                        .orderId(10L)
                        .eventType(ShipmentOrderStatus.IN_TRANSIT)
                        .occurredAt(OffsetDateTime.now().minusHours(1))
                        .notes("In transit")
                        .build());
        when(shipmentOrderEventService.getEventsForOrder(10L)).thenReturn(responses);

        mockMvc.perform(get("/api/admin/orders/{orderId}/events", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].eventType", is("CREATED")))
                .andExpect(jsonPath("$[1].eventType", is("IN_TRANSIT")));

        verify(shipmentOrderEventService).getEventsForOrder(10L);
    }

    @Test
    void getLatestEventShouldReturnLatestEntry() throws Exception {
        OrderEventResponse response = OrderEventResponse.builder()
                .id(3L)
                .orderId(10L)
                .eventType(ShipmentOrderStatus.DELIVERED)
                .occurredAt(OffsetDateTime.now())
                .notes("Delivered to receiver")
                .build();
        when(shipmentOrderEventService.getLatestEventForOrder(10L)).thenReturn(response);

        mockMvc.perform(get("/api/admin/orders/{orderId}/events/latest", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.eventType", is("DELIVERED")))
                .andExpect(jsonPath("$.notes", is("Delivered to receiver")));

        verify(shipmentOrderEventService).getLatestEventForOrder(eq(10L));
    }
}

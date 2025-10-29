package Operimus.SLMS.business_layer.order_shipment_module.controllers;

import Operimus.SLMS.business_layer.order_shipment_module.dto.OrderEventResponse;
import Operimus.SLMS.business_layer.order_shipment_module.service.ShipmentOrderEventService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders/{orderId}/events")
@RequiredArgsConstructor
public class OrderEventController {

    private final ShipmentOrderEventService shipmentOrderEventService;

    @GetMapping
    public List<OrderEventResponse> listEvents(@PathVariable Long orderId) {
        return shipmentOrderEventService.getEventsForOrder(orderId);
    }

    @GetMapping("/latest")
    public OrderEventResponse getLatestEvent(@PathVariable Long orderId) {
        return shipmentOrderEventService.getLatestEventForOrder(orderId);
    }
}

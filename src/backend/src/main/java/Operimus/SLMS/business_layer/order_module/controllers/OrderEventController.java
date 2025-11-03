package Operimus.SLMS.business_layer.order_module.controllers;

import Operimus.SLMS.business_layer.order_module.dto.OrderEventResponse;
import Operimus.SLMS.business_layer.order_module.service.OrderEventService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/orders/{orderId}/events")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost", "http://localhost:3000"}, allowCredentials = "true")
public class OrderEventController {

    private final OrderEventService orderEventService;

    @GetMapping
    public ResponseEntity<List<OrderEventResponse>> listEvents(@PathVariable Long orderId) {
        log.info("Fetching events for order: {}", orderId);
        List<OrderEventResponse> events = orderEventService.getEventsForOrder(orderId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/latest")
    public ResponseEntity<OrderEventResponse> getLatestEvent(@PathVariable Long orderId) {
        log.info("Fetching latest event for order: {}", orderId);
        OrderEventResponse event = orderEventService.getLatestEventForOrder(orderId);
        return ResponseEntity.ok(event);
    }
}

package Operimus.SLMS.business_layer.order_module.controllers;

import Operimus.SLMS.business_layer.order_module.dto.OrderRequest;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerId) {
        log.info("Fetching orders - status={}, customerId={}", status, customerId);

        List<OrderResponse> orders;

        if (customerId != null && !customerId.isBlank()) {
            orders = orderService.getByCustomerId(customerId);
        } else if (status != null && !status.isBlank()) {
            // Parse comma-separated statuses
            List<OrderStatus> statuses = Arrays.stream(status.split(","))
                    .map(String::trim)
                    .map(OrderStatus::valueOf)
                    .toList();
            orders = orderService.getByStatus(statuses);
        } else {
            orders = orderService.getAll();
        }

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        log.info("Fetching order: {}", id);
        OrderResponse order = orderService.getById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<OrderResponse> getOrderByTracking(@PathVariable String trackingNumber) {
        log.info("Fetching order by tracking number: {}", trackingNumber);
        OrderResponse order = orderService.getByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(order);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        log.info("Creating order: {}", request.getReference());
        OrderResponse response = orderService.create(request);
        return ResponseEntity.created(URI.create("/api/admin/orders/" + response.getId())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id, @Valid @RequestBody OrderRequest request) {
        log.info("Updating order: {}", id);
        OrderResponse response = orderService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        log.info("Deleting order: {}", id);
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package Operimus.SLMS.business_layer.notifications_module.controllers;

import Operimus.SLMS.business_layer.notifications_module.entities.Notification;
import Operimus.SLMS.business_layer.notifications_module.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{customerId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable String customerId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(customerId));
    }

    @GetMapping("/{customerId}/unread")
    public ResponseEntity<List<Notification>> getUnreadUserNotifications(@PathVariable String customerId) {
        return ResponseEntity.ok(notificationService.getUnreadUserNotifications(customerId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test")
    public ResponseEntity<Void> triggerTestNotification(@RequestParam String customerId) {
        Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent event = Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent.builder()
                .aggregateId("TEST-" + java.util.UUID.randomUUID())
                .aggregateType("TEST")
                .eventType("TEST_EVENT")
                .message("This is a test notification for customer " + customerId)
                .customerId(customerId)
                .occurredAt(java.time.OffsetDateTime.now())
                .build();
        notificationService.processEvent(event);
        return ResponseEntity.ok().build();
    }
}

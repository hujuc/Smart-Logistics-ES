package Operimus.SLMS.business_layer.notifications_module.service;

import Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent;
import Operimus.SLMS.business_layer.notifications_module.entities.Notification;
import Operimus.SLMS.business_layer.notifications_module.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void processEvent(NotificationEvent event) {
        log.info("Processing notification event: {}", event);

        Notification notification = Notification.builder()
                .aggregateId(event.getAggregateId())
                .aggregateType(event.getAggregateType())
                .eventType(event.getEventType())
                .message(event.getMessage())
                .customerId(event.getCustomerId())
                .read(false)
                .build();

        notificationRepository.save(notification);

        sendRealTimeNotification(notification);
    }

    public List<Notification> getUserNotifications(String customerId) {
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public List<Notification> getUnreadUserNotifications(String customerId) {
        return notificationRepository.findByCustomerIdAndReadFalseOrderByCreatedAtDesc(customerId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    private void sendRealTimeNotification(Notification notification) {
        log.info("🔔 Sending real-time notification for customer: {}", notification.getCustomerId());
        
        if (notification.getCustomerId() == null) {
            log.warn("⚠️ Notification has no customerId, skipping WebSocket send");
            return;
        }
        
        try {
            // Send to specific user's queue
            messagingTemplate.convertAndSendToUser(
                notification.getCustomerId(),
                "/queue/notifications",
                notification
            );
            log.info("✅ Notification sent to user: {}", notification.getCustomerId());
        } catch (Exception e) {
            log.error("❌ Failed to send WebSocket notification: {}", e.getMessage(), e);
        }
    }
}

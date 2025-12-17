package Operimus.SLMS.business_layer.notifications_module.service;

import Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent;
import Operimus.SLMS.business_layer.notifications_module.entities.Notification;
import Operimus.SLMS.business_layer.notifications_module.repository.NotificationRepository;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;
    private final KeycloakUserService keycloakUserService;

    @Transactional
    public void processEvent(NotificationEvent event) {
        // Check if this is a broadcast to all logistics managers
        if ("LOGISTICS_MANAGER".equals(event.getCustomerId())) {
            log.info("🎯 Detected LOGISTICS_MANAGER broadcast for event: {}", event.getEventType());
            List<UserRepresentation> logisticsManagers = keycloakUserService.getUsersByRole("LOGISTICS_MANAGER");
            log.info("📋 Found {} logistics managers to notify", logisticsManagers.size());
            for (UserRepresentation manager : logisticsManagers) {
                log.info("👤 Creating notification for logistics manager: {} ({})", manager.getUsername(), manager.getId());
                createAndSendNotification(event, manager.getUsername());
            }
        } 
        // Check if this is a broadcast to all customer service representatives
        else if ("CUSTOMER_SERVICE_REP".equals(event.getCustomerId())) {
            log.info("🎯 Detected CUSTOMER_SERVICE_REP broadcast for event: {}", event.getEventType());
            List<UserRepresentation> csrUsers = keycloakUserService.getUsersByRole("CUSTOMER_SERVICE_REP");
            log.info("📋 Found {} customer service reps to notify", csrUsers.size());
            for (UserRepresentation csr : csrUsers) {
                log.info("👤 Creating notification for CSR: {} ({})", csr.getUsername(), csr.getId());
                createAndSendNotification(event, csr.getUsername());
            }
        }
        // Check if this is a broadcast to all warehouse staff
        else if ("WAREHOUSE_STAFF".equals(event.getCustomerId())) {
            log.info("🎯 Detected WAREHOUSE_STAFF broadcast for event: {}", event.getEventType());
            List<UserRepresentation> warehouseUsers = keycloakUserService.getUsersByRole("WAREHOUSE_STAFF");
            log.info("📋 Found {} warehouse staff to notify", warehouseUsers.size());
            for (UserRepresentation staff : warehouseUsers) {
                log.info("👤 Creating notification for warehouse staff: {} ({})", staff.getUsername(), staff.getId());
                createAndSendNotification(event, staff.getUsername());
            }
        }
        else {
            createAndSendNotification(event, event.getCustomerId());
        }
    }

    private void createAndSendNotification(NotificationEvent event, String customerId) {
        Notification notification = Notification.builder()
                .aggregateId(event.getAggregateId())
                .aggregateType(event.getAggregateType())
                .eventType(event.getEventType())
                .message(event.getMessage())
                .customerId(customerId)
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

    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void deleteAllUserNotifications(String customerId) {
        notificationRepository.deleteByCustomerId(customerId);
    }

    @Transactional
    public void markAllAsRead(String customerId) {
        notificationRepository.markAllAsRead(customerId);
    }

    private void sendRealTimeNotification(Notification notification) {
        if (notification.getCustomerId() == null) {
            return;
        }
        
        try {
            messagingTemplate.convertAndSendToUser(
                notification.getCustomerId(),
                "/queue/notifications",
                notification
            );
        } catch (Exception e) {
            log.error("Failed to send notification via WebSocket: {}", e.getMessage());
        }
    }
}

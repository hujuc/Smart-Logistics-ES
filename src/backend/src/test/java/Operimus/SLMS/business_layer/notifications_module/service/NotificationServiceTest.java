package Operimus.SLMS.business_layer.notifications_module.service;

import Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent;
import Operimus.SLMS.business_layer.notifications_module.entities.Notification;
import Operimus.SLMS.business_layer.notifications_module.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void processEventShouldSaveAndSendNotification() {
        NotificationEvent event = NotificationEvent.builder()
                .aggregateId("123")
                .aggregateType("ORDER")
                .eventType("CREATED")
                .message("Order created")
                .customerId("cust-1")
                .occurredAt(OffsetDateTime.now())
                .build();

        notificationService.processEvent(event);

        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(eq("cust-1"), eq("/queue/notifications"), any(Notification.class));
    }

    @Test
    void markAsReadShouldUpdateStatus() {
        Notification notification = Notification.builder().id(1L).read(false).build();
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(1L);

        verify(notificationRepository).save(any(Notification.class));
    }
}

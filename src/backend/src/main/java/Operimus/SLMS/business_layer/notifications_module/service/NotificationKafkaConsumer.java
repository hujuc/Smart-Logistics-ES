package Operimus.SLMS.business_layer.notifications_module.service;

import Operimus.SLMS.business_layer.notifications_module.dto.NotificationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topic.order-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeOrderEvent(String message) {
        log.info("Received order event: {}", message);
        try {
            NotificationEvent event = objectMapper.readValue(message, NotificationEvent.class);
            notificationService.processEvent(event);
        } catch (Exception e) {
            log.error("Error processing order event", e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.shipment-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeShipmentEvent(String message) {
        log.info("Received shipment event: {}", message);
        try {
            NotificationEvent event = objectMapper.readValue(message, NotificationEvent.class);
            notificationService.processEvent(event);
        } catch (Exception e) {
            log.error("Error processing shipment event", e);
        }
    }
}

package Operimus.SLMS.business_layer.notifications_module.repository;

import Operimus.SLMS.business_layer.notifications_module.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    List<Notification> findByCustomerIdAndReadFalseOrderByCreatedAtDesc(String customerId);
}

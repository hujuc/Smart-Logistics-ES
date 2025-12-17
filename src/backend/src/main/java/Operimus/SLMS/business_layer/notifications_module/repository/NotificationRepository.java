package Operimus.SLMS.business_layer.notifications_module.repository;

import Operimus.SLMS.business_layer.notifications_module.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    List<Notification> findByCustomerIdAndReadFalseOrderByCreatedAtDesc(String customerId);
    void deleteByCustomerId(String customerId);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query("UPDATE Notification n SET n.read = true WHERE n.customerId = :customerId")
    void markAllAsRead(@org.springframework.web.bind.annotation.PathVariable("customerId") String customerId);
}

package Operimus.SLMS.business_layer.order_shipment_module.repository;

import Operimus.SLMS.business_layer.order_shipment_module.entities.OrderEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {
    List<OrderEvent> findByOrderIdOrderByOccurredAtAsc(Long orderId);

    Optional<OrderEvent> findFirstByOrderIdOrderByOccurredAtDesc(Long orderId);
}

package Operimus.SLMS.business_layer.order_shipment_module.repository;

import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrderEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentOrderEventRepository extends JpaRepository<ShipmentOrderEvent, Long> {
    List<ShipmentOrderEvent> findByShipmentOrderIdOrderByOccurredAtAsc(Long shipmentOrderId);

    Optional<ShipmentOrderEvent> findFirstByShipmentOrderIdOrderByOccurredAtDesc(Long shipmentOrderId);
}

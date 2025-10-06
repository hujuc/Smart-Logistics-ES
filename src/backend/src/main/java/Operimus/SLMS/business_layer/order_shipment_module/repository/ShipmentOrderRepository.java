package Operimus.SLMS.business_layer.order_shipment_module.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrder;

public interface ShipmentOrderRepository extends JpaRepository<ShipmentOrder, Long> {
    boolean existsByReferenceIgnoreCase(String reference);

    Optional<ShipmentOrder> findByReferenceIgnoreCase(String reference);
}


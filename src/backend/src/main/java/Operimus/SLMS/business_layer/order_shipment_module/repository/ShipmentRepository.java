package Operimus.SLMS.business_layer.order_shipment_module.repository;

import Operimus.SLMS.business_layer.order_shipment_module.entities.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    boolean existsByShipmentCodeIgnoreCase(String shipmentCode);
}

package Operimus.SLMS.business_layer.rate_carrier_module.repository;

import Operimus.SLMS.business_layer.rate_carrier_module.entities.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    boolean existsByShipmentCodeIgnoreCase(String shipmentCode);
}

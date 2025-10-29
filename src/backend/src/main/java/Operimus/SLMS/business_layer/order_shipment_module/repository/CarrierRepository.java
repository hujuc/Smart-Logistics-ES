package Operimus.SLMS.business_layer.order_shipment_module.repository;

import Operimus.SLMS.business_layer.order_shipment_module.entities.Carrier;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarrierRepository extends JpaRepository<Carrier, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<Carrier> findByCodeIgnoreCase(String code);
}

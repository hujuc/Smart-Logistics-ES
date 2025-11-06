package Operimus.SLMS.business_layer.rate_carrier_module.repository;

import Operimus.SLMS.business_layer.rate_carrier_module.entities.Carrier;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarrierRepository extends JpaRepository<Carrier, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<Carrier> findByCodeIgnoreCase(String code);
}

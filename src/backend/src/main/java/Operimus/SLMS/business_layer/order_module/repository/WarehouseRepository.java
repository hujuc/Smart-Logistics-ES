package Operimus.SLMS.business_layer.order_module.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import Operimus.SLMS.business_layer.order_module.entities.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    boolean existsByCodeIgnoreCase(String code);

    Optional<Warehouse> findByCodeIgnoreCase(String code);
}


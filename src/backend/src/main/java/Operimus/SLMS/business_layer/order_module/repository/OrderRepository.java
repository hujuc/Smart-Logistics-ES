package Operimus.SLMS.business_layer.order_module.repository;

import Operimus.SLMS.business_layer.order_module.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByReferenceIgnoreCase(String reference);
}

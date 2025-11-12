package Operimus.SLMS.business_layer.order_module.repository;

import Operimus.SLMS.business_layer.order_module.entities.Order;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.entities.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByReferenceIgnoreCase(String reference);
    List<Order> findByWarehouse(Warehouse warehouse);
    List<Order> findByWarehouseAndStatus(Warehouse warehouse, OrderStatus status);
    List<Order> findByShipmentId(Long shipmentId);
}

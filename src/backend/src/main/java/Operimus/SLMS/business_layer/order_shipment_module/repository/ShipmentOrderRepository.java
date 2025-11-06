package Operimus.SLMS.business_layer.order_shipment_module.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrder;
import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrderStatus;
import Operimus.SLMS.business_layer.order_shipment_module.entities.Warehouse;

public interface ShipmentOrderRepository extends JpaRepository<ShipmentOrder, Long> {
    boolean existsByReferenceIgnoreCase(String reference);

    Optional<ShipmentOrder> findByReferenceIgnoreCase(String reference);

    List<ShipmentOrder> findByWarehouse(Warehouse warehouse);

    List<ShipmentOrder> findByWarehouseAndStatus(Warehouse warehouse, ShipmentOrderStatus status);
}


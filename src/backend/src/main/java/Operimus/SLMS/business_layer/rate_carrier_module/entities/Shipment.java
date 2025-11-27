package Operimus.SLMS.business_layer.rate_carrier_module.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String shipmentCode;

    // Assignment to carrier and driver (both required at creation)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrier_id", nullable = false)
    private Carrier carrier;

    @Column(name = "driver_id", nullable = false, length = 64)
    private String driverId; // Keycloak driver UUID (required)

    // Shippo/ShipStation integration fields (moved from Order)
    @Column(name = "shippo_shipment_id", length = 128)
    private String shippoShipmentId;

    @Column(name = "shippo_transaction_id", length = 128)
    private String shippoTransactionId;

    @Column(name = "shipstation_order_id", length = 128)
    private String shipstationOrderId;

    @Column(name = "shipstation_carrier_code", length = 64)
    private String shipstationCarrierCode;

    @Column(name = "shipstation_service_code", length = 64)
    private String shipstationServiceCode;

    // Shipment details
    @Column(name = "total_orders")
    private Integer totalOrders;

    @Column(name = "total_weight")
    private Double totalWeight;

    @Column(name = "weight_unit", length = 16)
    private String weightUnit;

    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;

    // Costs
    @Column(name = "shipping_cost")
    private Double shippingCost;

    @Column(name = "transit_time", length = 64)
    private String transitTime;

    // Status
    @Column(name = "active", nullable = false, columnDefinition = "boolean default true")
    private Boolean active = true;

    // Timestamps
    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    // Note: Orders reference this shipment via shipmentId (Long) to avoid cross-module entity dependencies
    // To get orders for a shipment, query OrderRepository.findByShipmentId(shipmentId)
}

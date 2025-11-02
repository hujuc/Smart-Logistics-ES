package Operimus.SLMS.business_layer.order_shipment_module.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "orders")
public class ShipmentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String reference;

    @Column(length = 512)
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ShipmentOrderStatus status = ShipmentOrderStatus.CREATED;

    @Column(nullable = false)
    private String shippingAddress;

    private LocalDate estimatedDeliveryDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrier_id")
    private Carrier carrier;

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

    @Column(name = "tracking_number", length = 64)
    private String trackingNumber;

    @Column(name = "customer_id", length = 64)
    private String customerId;

    // Parcel dimensions for shipping rate calculation
    @Column(name = "weight")
    private Double weight;

    @Column(name = "weight_unit", length = 16)
    private String weightUnit;

    @Column(name = "length")
    private Double length;

    @Column(name = "width")
    private Double width;

    @Column(name = "height")
    private Double height;

    @Column(name = "distance_unit", length = 16)
    private String distanceUnit;

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}

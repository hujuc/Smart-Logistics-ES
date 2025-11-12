package Operimus.SLMS.business_layer.order_module.entities;

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
public class Order {

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
    private OrderStatus status = OrderStatus.CREATED;

    @Column(nullable = false)
    private String shippingAddress;

    @Column(name = "shipping_address_line1")
    private String shippingAddressLine1;

    @Column(name = "shipping_address_line2")
    private String shippingAddressLine2;

    @Column(name = "shipping_city")
    private String shippingCity;

    @Column(name = "shipping_state")
    private String shippingStateOrProvince;

    @Column(name = "shipping_postal_code")
    private String shippingPostalCode;

    @Column(name = "shipping_country")
    private String shippingCountry;

    @Column(name = "shipping_phone")
    private String shippingPhone;

    @Column(name = "shipping_email")
    private String shippingEmail;

    private LocalDate estimatedDeliveryDate;

    // Relationship to warehouse
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    // Shipment ID (foreign key to shipments table - nullable, not all orders are shipped yet)
    // Stored as Long instead of entity reference to avoid cross-module dependency
    @Column(name = "shipment_id")
    private Long shipmentId;

    // Customer ID from Keycloak
    @Column(name = "customer_id", length = 64)
    private String customerId;

    // QR Code for this order
    @Column(name = "qr_code_url", length = 512)
    private String qrCodeUrl;

    // QR confirmation callback URL encoded in the QR image
    @Column(name = "qr_confirmation_url", length = 512)
    private String qrConfirmationUrl;

    // Driver confirmation metadata
    @Column(name = "qr_confirmed_at")
    private OffsetDateTime qrConfirmedAt;

    @Column(name = "qr_confirmed_by", length = 64)
    private String qrConfirmedBy;

    // Tracking number (order-specific tracking if needed)
    @Column(name = "tracking_number", length = 64)
    private String trackingNumber;

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

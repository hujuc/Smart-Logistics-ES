package Operimus.SLMS.business_layer.order_module.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String reference;
    private String description;
    private OrderStatus status;
    private String shippingAddress;
    private LocalDate estimatedDeliveryDate;
    private Long warehouseId;

    // Shipment relationship
    private Long shipmentId;
    private String shipmentCode;

    private String customerId;
    private String qrCodeUrl;
    private String trackingNumber;

    // Parcel dimensions for shipping rate calculation
    private Double weight;
    private String weightUnit;
    private Double length;
    private Double width;
    private Double height;
    private String distanceUnit;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

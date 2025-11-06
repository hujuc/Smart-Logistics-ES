package Operimus.SLMS.business_layer.order_module.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotBlank
    private String reference;

    private String description;

    private OrderStatus status;

    @NotBlank
    private String shippingAddress;

    private LocalDate estimatedDeliveryDate;

    @NotNull
    private Long warehouseId;

    private Long shipmentId; // Link to shipment (nullable)

    private String customerId; // Keycloak customer UUID

    private String trackingNumber;

    private String customerId;

    // Parcel dimensions for shipping rate calculation
    private Double weight;
    private String weightUnit;
    private Double length;
    private Double width;
    private Double height;
    private String distanceUnit;
}

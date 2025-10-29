package Operimus.SLMS.business_layer.order_shipment_module.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrderStatus;
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

    private ShipmentOrderStatus status;

    @NotBlank
    private String shippingAddress;

    private LocalDate estimatedDeliveryDate;

    @NotNull
    private Long warehouseId;

    private Long carrierId;

    private String shippoShipmentId;

    private String shippoTransactionId;

    private String shipstationOrderId;

    private String shipstationCarrierCode;

    private String shipstationServiceCode;

    private String trackingNumber;
}

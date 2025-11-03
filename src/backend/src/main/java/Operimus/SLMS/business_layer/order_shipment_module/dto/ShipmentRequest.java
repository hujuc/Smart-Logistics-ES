package Operimus.SLMS.business_layer.order_shipment_module.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

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
public class ShipmentRequest {

    @NotBlank
    private String shipmentCode;

    @NotNull
    private Long carrierId;

    @NotBlank
    private String driverId; // Keycloak driver UUID (required)

    private List<Long> orderIds; // IDs of orders to include in shipment

    // Shippo/ShipStation integration fields
    private String shippoShipmentId;
    private String shippoTransactionId;
    private String shipstationOrderId;
    private String shipstationCarrierCode;
    private String shipstationServiceCode;

    // Shipment details
    private Integer totalOrders;
    private Double totalWeight;
    private String weightUnit;
    private LocalDate estimatedDeliveryDate;

    // Costs
    private Double shippingCost;
    private String transitTime;
}

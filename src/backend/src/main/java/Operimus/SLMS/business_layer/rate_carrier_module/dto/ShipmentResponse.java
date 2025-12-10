package Operimus.SLMS.business_layer.rate_carrier_module.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {

    private Long id;
    private String shipmentCode;
    private String status; // PENDING_ACCEPTANCE, ACCEPTED, IN_TRANSIT, COMPLETED, EXCEPTION

    // Assignment
    private Long carrierId;
    private String carrierCode;
    private String carrierName;
    private String driverId; // Keycloak driver UUID

    // Shippo/ShipStation integration
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

    // Timestamps
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

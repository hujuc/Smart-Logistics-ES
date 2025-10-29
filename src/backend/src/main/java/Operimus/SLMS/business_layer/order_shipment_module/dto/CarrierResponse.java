package Operimus.SLMS.business_layer.order_shipment_module.dto;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarrierResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String supportEmail;
    private String supportPhone;
    private String website;
    private boolean active;
    private String shippoCarrierId;
    private String shippoAccountId;
    private String shipEngineCarrierId;
    private String shipEngineAccountNumber;
    private String serviceLevelsSummary;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String stateOrProvince;
    private String postalCode;
    private String country;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

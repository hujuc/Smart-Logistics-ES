package Operimus.SLMS.business_layer.order_shipment_module.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class CarrierRequest {

    @NotBlank
    @Size(max = 64)
    private String code;

    @NotBlank
    @Size(max = 128)
    private String name;

    @Size(max = 512)
    private String description;

    @Email
    private String supportEmail;

    private String supportPhone;

    private String website;

    private Boolean active;

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
}

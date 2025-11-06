package Operimus.SLMS.business_layer.rate_carrier_module.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarrierIntegrationSnapshot {

    private String carrierCode;
    private String displayName;
    private String description;
    private Boolean active;
    private String supportEmail;
    private String supportPhone;
    private String website;
    private CarrierAddressView address;
    private List<String> serviceLevels;
    private CarrierIntegrationSourceDetails shippo;
    private CarrierIntegrationSourceDetails shipEngine;
}

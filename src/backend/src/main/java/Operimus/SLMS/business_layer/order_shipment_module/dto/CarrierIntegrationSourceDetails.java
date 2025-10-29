package Operimus.SLMS.business_layer.order_shipment_module.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarrierIntegrationSourceDetails {

    private String source;
    private String externalId;
    private String accountId;
    private Boolean active;
    private String supportEmail;
    private String supportPhone;
    private List<String> serviceLevels;
    private CarrierAddressView address;
}

package Operimus.SLMS.business_layer.order_shipment_module.dto;

import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressValidationResponse {

    private boolean valid;
    private CarrierAddressView normalizedAddress;
    @Builder.Default
    private List<String> messages = Collections.emptyList();
}


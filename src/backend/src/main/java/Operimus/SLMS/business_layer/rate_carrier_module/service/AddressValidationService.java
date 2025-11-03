package Operimus.SLMS.business_layer.rate_carrier_module.service;

import Operimus.SLMS.business_layer.rate_carrier_module.dto.AddressValidationRequest;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.AddressValidationResponse;
import Operimus.SLMS.business_layer.rate_carrier_module.integration.ShippoApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressValidationService {

    private final ShippoApiClient shippoApiClient;

    public AddressValidationResponse validate(AddressValidationRequest request) {
        return shippoApiClient.validateAddress(request);
    }
}


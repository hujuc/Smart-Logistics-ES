package Operimus.SLMS.business_layer.rate_carrier_module.service;

import Operimus.SLMS.business_layer.rate_carrier_module.dto.ShippingRateRequest;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.ShippingRateResponse;
import Operimus.SLMS.business_layer.rate_carrier_module.integration.ShippoApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShippingRateService {

    private final ShippoApiClient shippoApiClient;

    public ShippingRateResponse calculateRates(ShippingRateRequest request) {
        return shippoApiClient.calculateRates(request);
    }
}


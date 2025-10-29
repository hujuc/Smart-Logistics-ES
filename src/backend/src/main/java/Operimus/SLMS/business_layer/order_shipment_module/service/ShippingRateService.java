package Operimus.SLMS.business_layer.order_shipment_module.service;

import Operimus.SLMS.business_layer.order_shipment_module.dto.ShippingRateRequest;
import Operimus.SLMS.business_layer.order_shipment_module.dto.ShippingRateResponse;
import Operimus.SLMS.business_layer.order_shipment_module.integration.ShippoApiClient;
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


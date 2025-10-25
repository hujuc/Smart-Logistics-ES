package Operimus.SLMS.business_layer.order_shipment_module.integration;

import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierAddressView;
import java.util.List;

public record ShipEngineCarrier(
        String carrierCode,
        String displayName,
        String carrierId,
        String accountNumber,
        boolean active,
        String supportEmail,
        String supportPhone,
        List<String> serviceLevels,
        CarrierAddressView address,
        String website) {}

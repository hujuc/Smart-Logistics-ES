package Operimus.SLMS.business_layer.order_shipment_module.integration;

import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierAddressView;
import java.util.List;

public record ShippoCarrierAccount(
        String carrierCode,
        String displayName,
        String objectId,
        String accountId,
        boolean active,
        String supportEmail,
        String supportPhone,
        List<String> serviceLevels,
        CarrierAddressView address) {}

package Operimus.SLMS.business_layer.order_shipment_module.service;

import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierAddressView;
import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierIntegrationSnapshot;
import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierIntegrationSourceDetails;
import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierResponse;
import Operimus.SLMS.business_layer.order_shipment_module.integration.ShipEngineApiClient;
import Operimus.SLMS.business_layer.order_shipment_module.integration.ShipEngineCarrier;
import Operimus.SLMS.business_layer.order_shipment_module.integration.ShippoApiClient;
import Operimus.SLMS.business_layer.order_shipment_module.integration.ShippoCarrierAccount;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class CarrierIntegrationService {

    private final ShippoApiClient shippoApiClient;
    private final ShipEngineApiClient shipEngineApiClient;
    private final CarrierService carrierService;

    @Transactional(readOnly = true)
    public List<CarrierIntegrationSnapshot> fetchSnapshots() {
        List<ShippoCarrierAccount> shippoCarriers = shippoApiClient.fetchCarrierAccounts();
        List<ShipEngineCarrier> shipEngineCarriers = shipEngineApiClient.fetchCarriers();

        Map<String, ShippoCarrierAccount> shippoByCode = shippoCarriers.stream()
                .filter(carrier -> carrier.carrierCode() != null)
                .collect(Collectors.toMap(
                        ShippoCarrierAccount::carrierCode,
                        carrier -> carrier,
                        (left, right) -> left,
                        LinkedHashMap::new));

        Map<String, ShipEngineCarrier> shipEngineByCode = shipEngineCarriers.stream()
                .filter(carrier -> carrier.carrierCode() != null)
                .collect(Collectors.toMap(
                        ShipEngineCarrier::carrierCode,
                        carrier -> carrier,
                        (left, right) -> left,
                        LinkedHashMap::new));

        Set<String> unionCodes = new LinkedHashSet<>();
        unionCodes.addAll(shippoByCode.keySet());
        unionCodes.addAll(shipEngineByCode.keySet());

        List<CarrierIntegrationSnapshot> snapshots = new ArrayList<>();
        for (String code : unionCodes) {
            ShippoCarrierAccount shippo = shippoByCode.get(code);
            ShipEngineCarrier shipEngine = shipEngineByCode.get(code);

            // Only include carriers that have ShipEngine integration
            if (shipEngine != null) {
                snapshots.add(buildSnapshot(code, shippo, shipEngine));
            }
        }

        return snapshots;
    }

    @Transactional
    public List<CarrierResponse> synchronizeCarriers() {
        return carrierService.synchronizeFromSnapshots(fetchSnapshots());
    }

    private CarrierIntegrationSnapshot buildSnapshot(
            String carrierCode, ShippoCarrierAccount shippo, ShipEngineCarrier shipEngine) {

        String displayName = firstNonNull(
                shipEngine != null ? shipEngine.displayName() : null,
                shippo != null ? shippo.displayName() : null,
                carrierCode);

        String description = shippo != null ? shippo.displayName() : null;
        if (!hasText(description)) {
            description = shipEngine != null ? shipEngine.displayName() : null;
        }

        Boolean active = null;
        if (shippo != null || shipEngine != null) {
            boolean shippoActive = shippo != null && shippo.active();
            boolean shipEngineActive = shipEngine != null && shipEngine.active();
            active = shippoActive || shipEngineActive;
        }

        String supportEmail = firstNonNull(
                shipEngine != null ? shipEngine.supportEmail() : null,
                shippo != null ? shippo.supportEmail() : null);

        String supportPhone = firstNonNull(
                shipEngine != null ? shipEngine.supportPhone() : null,
                shippo != null ? shippo.supportPhone() : null);

        String website = shipEngine != null ? shipEngine.website() : null;

        CarrierAddressView address = pickAddress(shipEngine != null ? shipEngine.address() : null,
                shippo != null ? shippo.address() : null);

        List<String> serviceLevels = mergeServiceLevels(
                shippo != null ? shippo.serviceLevels() : null,
                shipEngine != null ? shipEngine.serviceLevels() : null);

        CarrierIntegrationSourceDetails shippoDetails = shippo == null
                ? null
                : CarrierIntegrationSourceDetails.builder()
                        .source("SHIPPO")
                        .externalId(shippo.objectId())
                        .accountId(shippo.accountId())
                        .active(shippo.active())
                        .supportEmail(shippo.supportEmail())
                        .supportPhone(shippo.supportPhone())
                        .serviceLevels(shippo.serviceLevels())
                        .address(shippo.address())
                        .build();

        CarrierIntegrationSourceDetails shipEngineDetails = shipEngine == null
                ? null
                : CarrierIntegrationSourceDetails.builder()
                        .source("SHIPENGINE")
                        .externalId(shipEngine.carrierId())
                        .accountId(shipEngine.accountNumber())
                        .active(shipEngine.active())
                        .supportEmail(shipEngine.supportEmail())
                        .supportPhone(shipEngine.supportPhone())
                        .serviceLevels(shipEngine.serviceLevels())
                        .address(shipEngine.address())
                        .build();

        return CarrierIntegrationSnapshot.builder()
                .carrierCode(carrierCode)
                .displayName(displayName)
                .description(description)
                .active(active)
                .supportEmail(supportEmail)
                .supportPhone(supportPhone)
                .website(website)
                .address(address)
                .serviceLevels(serviceLevels)
                .shippo(shippoDetails)
                .shipEngine(shipEngineDetails)
                .build();
    }

    private CarrierAddressView pickAddress(CarrierAddressView primary, CarrierAddressView fallback) {
        return primary != null ? primary : fallback;
    }

    private List<String> mergeServiceLevels(List<String> first, List<String> second) {
        Set<String> merged = new LinkedHashSet<>();
        if (!CollectionUtils.isEmpty(first)) {
            merged.addAll(first);
        }
        if (!CollectionUtils.isEmpty(second)) {
            merged.addAll(second);
        }
        return new ArrayList<>(merged);
    }

    private String firstNonNull(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

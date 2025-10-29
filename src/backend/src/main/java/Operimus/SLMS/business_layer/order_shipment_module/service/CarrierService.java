package Operimus.SLMS.business_layer.order_shipment_module.service;

import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierAddressView;
import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierIntegrationSnapshot;
import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierIntegrationSourceDetails;
import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierRequest;
import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierResponse;
import Operimus.SLMS.business_layer.order_shipment_module.entities.Carrier;
import Operimus.SLMS.business_layer.order_shipment_module.entities.CarrierAddress;
import Operimus.SLMS.business_layer.order_shipment_module.repository.CarrierRepository;
import Operimus.SLMS.business_layer.shared.exception.ResourceConflictException;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@Transactional
@RequiredArgsConstructor
public class CarrierService {

    private final CarrierRepository carrierRepository;

    @Transactional
    public CarrierResponse create(CarrierRequest request) {
        if (carrierRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new ResourceConflictException(
                    "A carrier with code '%s' already exists.".formatted(request.getCode()));
        }

        Carrier carrier = Carrier.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .supportEmail(request.getSupportEmail())
                .supportPhone(request.getSupportPhone())
                .website(request.getWebsite())
                .active(resolveActive(request.getActive()))
                .shippoCarrierId(request.getShippoCarrierId())
                .shippoAccountId(request.getShippoAccountId())
                .shipEngineCarrierId(request.getShipEngineCarrierId())
                .shipEngineAccountNumber(request.getShipEngineAccountNumber())
                .serviceLevelsSummary(request.getServiceLevelsSummary())
                .address(buildAddress(request))
                .build();

        Carrier saved = carrierRepository.save(carrier);
        return mapToResponse(saved);
    }

    @Transactional
    public CarrierResponse update(Long id, CarrierRequest request) {
        Carrier carrier = carrierRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier %d not found".formatted(id)));

        boolean isCodeChanging = !carrier.getCode().equalsIgnoreCase(request.getCode());
        if (isCodeChanging && carrierRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new ResourceConflictException(
                    "A carrier with code '%s' already exists.".formatted(request.getCode()));
        }

        carrier.setCode(request.getCode());
        carrier.setName(request.getName());
        carrier.setDescription(request.getDescription());
        carrier.setSupportEmail(request.getSupportEmail());
        carrier.setSupportPhone(request.getSupportPhone());
        carrier.setWebsite(request.getWebsite());
        carrier.setActive(resolveActive(request.getActive()));
        carrier.setShippoCarrierId(request.getShippoCarrierId());
        carrier.setShippoAccountId(request.getShippoAccountId());
        carrier.setShipEngineCarrierId(request.getShipEngineCarrierId());
        carrier.setShipEngineAccountNumber(request.getShipEngineAccountNumber());
        carrier.setServiceLevelsSummary(request.getServiceLevelsSummary());
        carrier.setAddress(buildAddress(request));

        return mapToResponse(carrier);
    }

    @Transactional
    public void delete(Long id) {
        Carrier carrier = carrierRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier %d not found".formatted(id)));
        carrierRepository.delete(carrier);
    }

    @Transactional(readOnly = true)
    public CarrierResponse getById(Long id) {
        Carrier carrier = carrierRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier %d not found".formatted(id)));
        return mapToResponse(carrier);
    }

    @Transactional(readOnly = true)
    public List<CarrierResponse> getAll() {
        return carrierRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public Carrier findEntityById(Long id) {
        return carrierRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier %d not found".formatted(id)));
    }

    @Transactional
    public List<CarrierResponse> synchronizeFromSnapshots(List<CarrierIntegrationSnapshot> snapshots) {
        if (CollectionUtils.isEmpty(snapshots)) {
            return List.of();
        }

        return snapshots.stream()
                .filter(Objects::nonNull)
                .map(this::upsertFromSnapshot)
                .toList();
    }

    private CarrierAddress buildAddress(CarrierRequest request) {
        if (request.getAddressLine1() == null
                && request.getAddressLine2() == null
                && request.getCity() == null
                && request.getStateOrProvince() == null
                && request.getPostalCode() == null
                && request.getCountry() == null) {
            return null;
        }

        return CarrierAddress.builder()
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .stateOrProvince(request.getStateOrProvince())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .build();
    }

    private CarrierResponse mapToResponse(Carrier carrier) {
        CarrierAddress address = carrier.getAddress();
        return CarrierResponse.builder()
                .id(carrier.getId())
                .code(carrier.getCode())
                .name(carrier.getName())
                .description(carrier.getDescription())
                .supportEmail(carrier.getSupportEmail())
                .supportPhone(carrier.getSupportPhone())
                .website(carrier.getWebsite())
                .active(carrier.isActive())
                .shippoCarrierId(carrier.getShippoCarrierId())
                .shippoAccountId(carrier.getShippoAccountId())
                .shipEngineCarrierId(carrier.getShipEngineCarrierId())
                .shipEngineAccountNumber(carrier.getShipEngineAccountNumber())
                .serviceLevelsSummary(carrier.getServiceLevelsSummary())
                .addressLine1(address != null ? address.getAddressLine1() : null)
                .addressLine2(address != null ? address.getAddressLine2() : null)
                .city(address != null ? address.getCity() : null)
                .stateOrProvince(address != null ? address.getStateOrProvince() : null)
                .postalCode(address != null ? address.getPostalCode() : null)
                .country(address != null ? address.getCountry() : null)
                .createdAt(carrier.getCreatedAt())
                .updatedAt(carrier.getUpdatedAt())
                .build();
    }

    private boolean resolveActive(Boolean active) {
        return active == null || Boolean.TRUE.equals(active);
    }

    private CarrierResponse upsertFromSnapshot(CarrierIntegrationSnapshot snapshot) {
        if (!StringUtils.hasText(snapshot.getCarrierCode())) {
            throw new ResourceConflictException("Carrier code is required to synchronize carrier data");
        }

        Carrier carrier = carrierRepository
                .findByCodeIgnoreCase(snapshot.getCarrierCode())
                .orElseGet(() -> Carrier.builder().code(snapshot.getCarrierCode()).build());

        carrier.setCode(snapshot.getCarrierCode());
        carrier.setName(StringUtils.hasText(snapshot.getDisplayName())
                ? snapshot.getDisplayName()
                : snapshot.getCarrierCode());
        carrier.setDescription(snapshot.getDescription());

        String supportEmail = firstNonEmpty(
                snapshot.getSupportEmail(),
                snapshot.getShipEngine() != null ? snapshot.getShipEngine().getSupportEmail() : null,
                snapshot.getShippo() != null ? snapshot.getShippo().getSupportEmail() : null);
        carrier.setSupportEmail(supportEmail);

        String supportPhone = firstNonEmpty(
                snapshot.getSupportPhone(),
                snapshot.getShipEngine() != null ? snapshot.getShipEngine().getSupportPhone() : null,
                snapshot.getShippo() != null ? snapshot.getShippo().getSupportPhone() : null);
        carrier.setSupportPhone(supportPhone);

        carrier.setWebsite(snapshot.getWebsite());

        Boolean snapshotActive = snapshot.getActive();
        if (snapshotActive != null) {
            carrier.setActive(snapshotActive);
        } else if (carrier.getId() == null) {
            carrier.setActive(true);
        }

        List<String> serviceLevels = snapshot.getServiceLevels();
        carrier.setServiceLevelsSummary(CollectionUtils.isEmpty(serviceLevels)
                ? null
                : serviceLevels.stream().filter(StringUtils::hasText).collect(Collectors.joining(", ")));

        CarrierAddressView addressView = snapshot.getAddress();
        if (addressView != null) {
            carrier.setAddress(fromAddressView(addressView));
        } else if (carrier.getId() == null) {
            carrier.setAddress(null);
        }

        CarrierIntegrationSourceDetails shippo = snapshot.getShippo();
        carrier.setShippoCarrierId(shippo != null ? shippo.getExternalId() : null);
        carrier.setShippoAccountId(shippo != null ? shippo.getAccountId() : null);

        CarrierIntegrationSourceDetails shipEngine = snapshot.getShipEngine();
        carrier.setShipEngineCarrierId(shipEngine != null ? shipEngine.getExternalId() : null);
        carrier.setShipEngineAccountNumber(shipEngine != null ? shipEngine.getAccountId() : null);

        Carrier saved = carrierRepository.save(carrier);
        return mapToResponse(saved);
    }

    private CarrierAddress fromAddressView(CarrierAddressView view) {
        if (view == null) {
            return null;
        }

        if (!StringUtils.hasText(view.getAddressLine1())
                && !StringUtils.hasText(view.getAddressLine2())
                && !StringUtils.hasText(view.getCity())
                && !StringUtils.hasText(view.getStateOrProvince())
                && !StringUtils.hasText(view.getPostalCode())
                && !StringUtils.hasText(view.getCountry())) {
            return null;
        }

        return CarrierAddress.builder()
                .addressLine1(view.getAddressLine1())
                .addressLine2(view.getAddressLine2())
                .city(view.getCity())
                .stateOrProvince(view.getStateOrProvince())
                .postalCode(view.getPostalCode())
                .country(view.getCountry())
                .build();
    }

    private String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }
}

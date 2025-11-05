package Operimus.SLMS.business_layer.rate_carrier_module.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import Operimus.SLMS.business_layer.rate_carrier_module.dto.CarrierAddressView;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.CarrierIntegrationSnapshot;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.CarrierIntegrationSourceDetails;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.CarrierRequest;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.CarrierResponse;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Carrier;
import Operimus.SLMS.business_layer.rate_carrier_module.repository.CarrierRepository;
import Operimus.SLMS.business_layer.shared.exception.ResourceConflictException;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(CarrierService.class)
class CarrierServiceTest {

    @Autowired
    private CarrierService carrierService;

    @Autowired
    private CarrierRepository carrierRepository;

    @Test
    void createShouldPersistCarrier() {
        CarrierRequest request = CarrierRequest.builder()
                .code("DHL")
                .name("DHL Express")
                .description("International express carrier")
                .supportEmail("support@dhl.com")
                .supportPhone("+49 228 4333112")
                .website("https://www.dhl.com")
                .serviceLevelsSummary("Express Worldwide, Economy Select")
                .addressLine1("Charles-de-Gaulle-Str. 20")
                .city("Bonn")
                .stateOrProvince("NRW")
                .postalCode("53113")
                .country("DE")
                .build();

        CarrierResponse response = carrierService.create(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getCode()).isEqualTo("DHL");
        assertThat(response.getSupportEmail()).isEqualTo("support@dhl.com");
        assertThat(response.isActive()).isTrue();
        Carrier persisted = carrierRepository.findById(response.getId()).orElseThrow();
        assertThat(persisted.getAddress()).isNotNull();
        assertThat(persisted.getAddress().getCity()).isEqualTo("Bonn");
    }

    @Test
    void createShouldFailWhenCodeExists() {
        carrierRepository.save(Carrier.builder()
                .code("DHL")
                .name("Existing")
                .active(true)
                .build());

        CarrierRequest request = CarrierRequest.builder()
                .code("DHL")
                .name("Duplicate")
                .build();

        assertThatThrownBy(() -> carrierService.create(request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("DHL");
    }

    @Test
    void updateShouldModifyCarrier() {
        Carrier carrier = carrierRepository.save(Carrier.builder()
                .code("DHL")
                .name("DHL")
                .active(true)
                .build());

        CarrierRequest request = CarrierRequest.builder()
                .code("DHL-INT")
                .name("DHL International")
                .supportEmail("help@dhl.com")
                .supportPhone("+351 123 456 789")
                .website("https://pt.dhl.com")
                .active(false)
                .shippoCarrierId("shippo-1")
                .shipEngineCarrierId("se-123")
                .serviceLevelsSummary("Express, Freight")
                .addressLine1("Rua Exemplo 1")
                .city("Lisbon")
                .stateOrProvince("Lisbon")
                .postalCode("1000-001")
                .country("PT")
                .build();

        CarrierResponse response = carrierService.update(carrier.getId(), request);

        assertThat(response.getCode()).isEqualTo("DHL-INT");
        assertThat(response.isActive()).isFalse();
        Carrier reloaded = carrierRepository.findById(carrier.getId()).orElseThrow();
        assertThat(reloaded.getShippoCarrierId()).isEqualTo("shippo-1");
        assertThat(reloaded.getAddress().getCountry()).isEqualTo("PT");
    }

    @Test
    void updateShouldFailWhenChangingToExistingCode() {
        carrierRepository.save(Carrier.builder()
                .code("DHL")
                .name("Existing")
                .build());
        Carrier carrier = carrierRepository.save(Carrier.builder()
                .code("FEDEX")
                .name("Target")
                .build());

        CarrierRequest request = CarrierRequest.builder()
                .code("DHL")
                .name("Updated")
                .build();

        assertThatThrownBy(() -> carrierService.update(carrier.getId(), request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("DHL");
    }

    @Test
    void deleteShouldRemoveCarrier() {
        Carrier carrier = carrierRepository.save(Carrier.builder()
                .code("DHL")
                .name("To Delete")
                .build());

        carrierService.delete(carrier.getId());

        assertThat(carrierRepository.findById(carrier.getId())).isEmpty();
    }

    @Test
    void getByIdShouldReturnCarrier() {
        Carrier carrier = carrierRepository.save(Carrier.builder()
                .code("DHL")
                .name("Lookup")
                .supportPhone("+44 1234 567890")
                .build());

        CarrierResponse response = carrierService.getById(carrier.getId());

        assertThat(response.getName()).isEqualTo("Lookup");
        assertThat(response.getSupportPhone()).isEqualTo("+44 1234 567890");
    }

    @Test
    void getByIdShouldFailWhenCarrierMissing() {
        assertThatThrownBy(() -> carrierService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void synchronizeFromSnapshotsShouldCreateCarrier() {
        CarrierIntegrationSnapshot snapshot = CarrierIntegrationSnapshot.builder()
                .carrierCode("DHL")
                .displayName("DHL Express")
                .supportEmail("support@dhl.com")
                .supportPhone("+351 987 654 321")
                .serviceLevels(List.of("Express"))
                .address(CarrierAddressView.builder().city("Bonn").country("DE").build())
                .shippo(CarrierIntegrationSourceDetails.builder()
                        .source("SHIPPO")
                        .externalId("shippo-1")
                        .accountId("acc-shippo")
                        .build())
                .shipEngine(CarrierIntegrationSourceDetails.builder()
                        .source("SHIPENGINE")
                        .externalId("se-1")
                        .accountId("acc-se")
                        .build())
                .build();

        List<CarrierResponse> responses = carrierService.synchronizeFromSnapshots(List.of(snapshot));

        assertThat(responses).hasSize(1);
        Carrier carrier = carrierRepository.findByCodeIgnoreCase("DHL").orElseThrow();
        assertThat(carrier.getShippoCarrierId()).isEqualTo("shippo-1");
        assertThat(carrier.getShipEngineCarrierId()).isEqualTo("se-1");
        assertThat(carrier.getServiceLevelsSummary()).contains("Express");
    }
}

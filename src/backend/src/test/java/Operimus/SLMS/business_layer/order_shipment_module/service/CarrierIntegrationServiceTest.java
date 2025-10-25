package Operimus.SLMS.business_layer.order_shipment_module.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierAddressView;
import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierIntegrationSnapshot;
import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierIntegrationSourceDetails;
import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierResponse;
import Operimus.SLMS.business_layer.order_shipment_module.integration.ShipEngineApiClient;
import Operimus.SLMS.business_layer.order_shipment_module.integration.ShipEngineCarrier;
import Operimus.SLMS.business_layer.order_shipment_module.integration.ShippoApiClient;
import Operimus.SLMS.business_layer.order_shipment_module.integration.ShippoCarrierAccount;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CarrierIntegrationServiceTest {

    @Mock
    private ShippoApiClient shippoApiClient;

    @Mock
    private ShipEngineApiClient shipEngineApiClient;

    @Mock
    private CarrierService carrierService;

    @InjectMocks
    private CarrierIntegrationService carrierIntegrationService;

    @Test
    void fetchSnapshotsShouldMergeCarriersByCode() {
        ShippoCarrierAccount shippo = new ShippoCarrierAccount(
                "dhl",
                "DHL Express",
                "shippo-1",
                "acc-shippo",
                true,
                "support@dhl.com",
                "+49 228 4333112",
                List.of("Express Worldwide"),
                CarrierAddressView.builder().city("Bonn").country("DE").build());

        ShipEngineCarrier shipEngine = new ShipEngineCarrier(
                "dhl",
                "DHL",
                "se-1",
                "acc-se",
                true,
                "help@dhl.com",
                "+351 123 456 789",
                List.of("Express Worldwide", "Economy Select"),
                CarrierAddressView.builder().city("Lisbon").country("PT").build(),
                "https://www.dhl.com");

        when(shippoApiClient.fetchCarrierAccounts()).thenReturn(List.of(shippo));
        when(shipEngineApiClient.fetchCarriers()).thenReturn(List.of(shipEngine));

        List<CarrierIntegrationSnapshot> snapshots = carrierIntegrationService.fetchSnapshots();

        assertThat(snapshots).hasSize(1);
        CarrierIntegrationSnapshot snapshot = snapshots.getFirst();
        assertThat(snapshot.getCarrierCode()).isEqualTo("dhl");
        assertThat(snapshot.getDisplayName()).isEqualTo("DHL");
        assertThat(snapshot.getSupportEmail()).isEqualTo("help@dhl.com");
        assertThat(snapshot.getAddress().getCountry()).isEqualTo("PT");
        assertThat(snapshot.getServiceLevels()).containsExactlyInAnyOrder("Express Worldwide", "Economy Select");

        CarrierIntegrationSourceDetails shippoDetails = snapshot.getShippo();
        assertThat(shippoDetails).isNotNull();
        assertThat(shippoDetails.getExternalId()).isEqualTo("shippo-1");

        CarrierIntegrationSourceDetails shipEngineDetails = snapshot.getShipEngine();
        assertThat(shipEngineDetails).isNotNull();
        assertThat(shipEngineDetails.getAccountId()).isEqualTo("acc-se");
    }

    @Test
    void synchronizeCarriersShouldDelegateToCarrierService() {
        when(shippoApiClient.fetchCarrierAccounts()).thenReturn(List.of());
        when(shipEngineApiClient.fetchCarriers()).thenReturn(List.of());
        when(carrierService.synchronizeFromSnapshots(anyList())).thenReturn(List.of());

        carrierIntegrationService.synchronizeCarriers();

        verify(carrierService).synchronizeFromSnapshots(anyList());
    }
}

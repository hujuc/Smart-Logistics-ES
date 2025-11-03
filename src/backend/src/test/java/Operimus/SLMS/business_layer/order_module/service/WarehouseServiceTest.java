package Operimus.SLMS.business_layer.order_module.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import Operimus.SLMS.business_layer.order_module.entities.Warehouse;
import Operimus.SLMS.business_layer.order_module.dto.WarehouseRequest;
import Operimus.SLMS.business_layer.order_module.dto.WarehouseResponse;
import Operimus.SLMS.business_layer.order_module.repository.WarehouseRepository;
import Operimus.SLMS.business_layer.shared.exception.ResourceConflictException;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(WarehouseService.class)
class WarehouseServiceTest {

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Test
    void createShouldPersistWarehouse() {
        WarehouseRequest request = WarehouseRequest.builder()
                .code("W-001")
                .name("North Hub")
                .addressLine1("123 Industrial Ave")
                .addressLine2("Unit B")
                .city("Porto")
                .stateOrProvince("Porto")
                .country("Portugal")
                .postalCode("4000-123")
                .active(true)
                .shippoLocationToken("shippo-token")
                .shipstationWarehouseId("shipstation-1")
                .build();

        WarehouseResponse response = warehouseService.create(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getCode()).isEqualTo("W-001");
        assertThat(response.getName()).isEqualTo("North Hub");
        assertThat(warehouseRepository.count()).isEqualTo(1);
        Warehouse persisted = warehouseRepository.findById(response.getId()).orElseThrow();
        assertThat(persisted.getCity()).isEqualTo("Porto");
        assertThat(persisted.getShipstationWarehouseId()).isEqualTo("shipstation-1");
    }

    @Test
    void createShouldFailWhenCodeAlreadyExists() {
        warehouseRepository.save(Warehouse.builder()
                .code("W-001")
                .name("Existing")
                .addressLine1("Address 1")
                .city("Lisbon")
                .stateOrProvince("Lisbon")
                .country("Portugal")
                .postalCode("1000-000")
                .active(true)
                .build());

        WarehouseRequest request = WarehouseRequest.builder()
                .code("W-001")
                .name("Duplicate")
                .addressLine1("Another Address")
                .city("Porto")
                .stateOrProvince("Porto")
                .country("Portugal")
                .postalCode("4000-123")
                .active(true)
                .build();

        assertThatThrownBy(() -> warehouseService.create(request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("W-001");
    }

    @Test
    void updateShouldModifyWarehouseData() {
        Warehouse warehouse = warehouseRepository.save(Warehouse.builder()
                .code("W-001")
                .name("Old Name")
                .addressLine1("Old Address")
                .city("Lisbon")
                .stateOrProvince("Lisbon")
                .country("Portugal")
                .postalCode("1000-000")
                .active(true)
                .build());

        WarehouseRequest request = WarehouseRequest.builder()
                .code("W-002")
                .name("Updated Name")
                .addressLine1("123 Updated St")
                .addressLine2("Suite 4")
                .city("Porto")
                .stateOrProvince("Porto")
                .country("Portugal")
                .postalCode("4000-321")
                .active(false)
                .shippoLocationToken("new-token")
                .shipstationWarehouseId("shipstation-2")
                .build();

        WarehouseResponse response = warehouseService.update(warehouse.getId(), request);

        assertThat(response.getCode()).isEqualTo("W-002");
        Warehouse updated = warehouseRepository.findById(warehouse.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.isActive()).isFalse();
        assertThat(updated.getShippoLocationToken()).isEqualTo("new-token");
    }

    @Test
    void updateShouldFailWhenChangingToExistingCode() {
        warehouseRepository.save(Warehouse.builder()
                .code("W-001")
                .name("Existing")
                .addressLine1("Address 1")
                .city("Lisbon")
                .stateOrProvince("Lisbon")
                .country("Portugal")
                .postalCode("1000-000")
                .active(true)
                .build());
        Warehouse warehouse = warehouseRepository.save(Warehouse.builder()
                .code("W-002")
                .name("Target")
                .addressLine1("Address 2")
                .city("Porto")
                .stateOrProvince("Porto")
                .country("Portugal")
                .postalCode("4000-000")
                .active(true)
                .build());

        WarehouseRequest request = WarehouseRequest.builder()
                .code("W-001")
                .name("Updated")
                .addressLine1("Updated Address")
                .city("Porto")
                .stateOrProvince("Porto")
                .country("Portugal")
                .postalCode("4000-000")
                .active(true)
                .build();

        assertThatThrownBy(() -> warehouseService.update(warehouse.getId(), request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("W-001");
    }

    @Test
    void deleteShouldRemoveWarehouse() {
        Warehouse warehouse = warehouseRepository.save(Warehouse.builder()
                .code("W-001")
                .name("To Delete")
                .addressLine1("Address")
                .city("Lisbon")
                .stateOrProvince("Lisbon")
                .country("Portugal")
                .postalCode("1000-000")
                .active(true)
                .build());

        warehouseService.delete(warehouse.getId());

        assertThat(warehouseRepository.findById(warehouse.getId())).isEmpty();
    }

    @Test
    void getByIdShouldReturnWarehouse() {
        Warehouse warehouse = warehouseRepository.save(Warehouse.builder()
                .code("W-001")
                .name("Lookup")
                .addressLine1("Address")
                .city("Lisbon")
                .stateOrProvince("Lisbon")
                .country("Portugal")
                .postalCode("1000-000")
                .active(true)
                .build());

        WarehouseResponse response = warehouseService.getById(warehouse.getId());

        assertThat(response.getName()).isEqualTo("Lookup");
        assertThat(response.getCity()).isEqualTo("Lisbon");
    }

    @Test
    void getByIdShouldFailWhenWarehouseDoesNotExist() {
        assertThatThrownBy(() -> warehouseService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getAllShouldReturnAllWarehouses() {
        warehouseRepository.save(Warehouse.builder()
                .code("W-001")
                .name("First")
                .addressLine1("Address 1")
                .city("Lisbon")
                .stateOrProvince("Lisbon")
                .country("Portugal")
                .postalCode("1000-000")
                .active(true)
                .build());
        warehouseRepository.save(Warehouse.builder()
                .code("W-002")
                .name("Second")
                .addressLine1("Address 2")
                .city("Porto")
                .stateOrProvince("Porto")
                .country("Portugal")
                .postalCode("4000-000")
                .active(false)
                .build());

        assertThat(warehouseService.getAll())
                .extracting(WarehouseResponse::getCode)
                .containsExactlyInAnyOrder("W-001", "W-002");
    }
}

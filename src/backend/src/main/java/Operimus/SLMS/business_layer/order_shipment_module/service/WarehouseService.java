package Operimus.SLMS.business_layer.order_shipment_module.service;

import Operimus.SLMS.business_layer.order_shipment_module.dto.WarehouseRequest;
import Operimus.SLMS.business_layer.order_shipment_module.dto.WarehouseResponse;
import Operimus.SLMS.business_layer.order_shipment_module.entities.Warehouse;
import Operimus.SLMS.business_layer.order_shipment_module.repository.WarehouseRepository;
import Operimus.SLMS.business_layer.shared.exception.ResourceConflictException;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Transactional
    public WarehouseResponse create(WarehouseRequest request) {
        if (warehouseRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new ResourceConflictException(
                    "A warehouse with code '%s' already exists.".formatted(request.getCode()));
        }

        Warehouse warehouse = Warehouse.builder()
                .code(request.getCode())
                .name(request.getName())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .stateOrProvince(request.getStateOrProvince())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .active(Boolean.TRUE.equals(request.getActive()))
                .shippoLocationToken(request.getShippoLocationToken())
                .shipstationWarehouseId(request.getShipstationWarehouseId())
                .build();

        Warehouse saved = warehouseRepository.save(warehouse);
        return mapToResponse(saved);
    }

    @Transactional
    public WarehouseResponse update(Long id, WarehouseRequest request) {
        Warehouse warehouse = warehouseRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse %d not found".formatted(id)));

        boolean isCodeChanging = !warehouse.getCode().equalsIgnoreCase(request.getCode());
        if (isCodeChanging && warehouseRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new ResourceConflictException(
                    "A warehouse with code '%s' already exists.".formatted(request.getCode()));
        }

        warehouse.setCode(request.getCode());
        warehouse.setName(request.getName());
        warehouse.setAddressLine1(request.getAddressLine1());
        warehouse.setAddressLine2(request.getAddressLine2());
        warehouse.setCity(request.getCity());
        warehouse.setStateOrProvince(request.getStateOrProvince());
        warehouse.setCountry(request.getCountry());
        warehouse.setPostalCode(request.getPostalCode());
        warehouse.setActive(Boolean.TRUE.equals(request.getActive()));
        warehouse.setShippoLocationToken(request.getShippoLocationToken());
        warehouse.setShipstationWarehouseId(request.getShipstationWarehouseId());

        return mapToResponse(warehouse);
    }

    @Transactional
    public void delete(Long id) {
        Warehouse warehouse = warehouseRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse %d not found".formatted(id)));
        warehouseRepository.delete(warehouse);
    }

    @Transactional(readOnly = true)
    public WarehouseResponse getById(Long id) {
        Warehouse warehouse = warehouseRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse %d not found".formatted(id)));
        return mapToResponse(warehouse);
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAll() {
        return warehouseRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public Warehouse findEntityById(Long id) {
        return warehouseRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse %d not found".formatted(id)));
    }

    private WarehouseResponse mapToResponse(Warehouse warehouse) {
        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .code(warehouse.getCode())
                .name(warehouse.getName())
                .addressLine1(warehouse.getAddressLine1())
                .addressLine2(warehouse.getAddressLine2())
                .city(warehouse.getCity())
                .stateOrProvince(warehouse.getStateOrProvince())
                .country(warehouse.getCountry())
                .postalCode(warehouse.getPostalCode())
                .active(warehouse.isActive())
                .shippoLocationToken(warehouse.getShippoLocationToken())
                .shipstationWarehouseId(warehouse.getShipstationWarehouseId())
                .build();
    }
}

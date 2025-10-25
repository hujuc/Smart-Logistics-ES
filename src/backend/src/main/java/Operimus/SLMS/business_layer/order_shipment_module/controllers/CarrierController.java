package Operimus.SLMS.business_layer.order_shipment_module.controllers;

import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierIntegrationSnapshot;
import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierRequest;
import Operimus.SLMS.business_layer.order_shipment_module.dto.CarrierResponse;
import Operimus.SLMS.business_layer.order_shipment_module.service.CarrierIntegrationService;
import Operimus.SLMS.business_layer.order_shipment_module.service.CarrierService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/carriers")
@RequiredArgsConstructor
public class CarrierController {

    private final CarrierService carrierService;
    private final CarrierIntegrationService carrierIntegrationService;

    @GetMapping
    public List<CarrierResponse> getCarriers() {
        return carrierService.getAll();
    }

    @GetMapping("/integrations/snapshot")
    public List<CarrierIntegrationSnapshot> getIntegrationSnapshot() {
        return carrierIntegrationService.fetchSnapshots();
    }

    @GetMapping("/{id}")
    public CarrierResponse getCarrier(@PathVariable Long id) {
        return carrierService.getById(id);
    }

    @PostMapping
    public ResponseEntity<CarrierResponse> createCarrier(@Valid @RequestBody CarrierRequest request) {
        CarrierResponse response = carrierService.create(request);
        return ResponseEntity.created(URI.create("/api/admin/carriers/" + response.getId())).body(response);
    }

    @PutMapping("/{id}")
    public CarrierResponse updateCarrier(@PathVariable Long id, @Valid @RequestBody CarrierRequest request) {
        return carrierService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCarrier(@PathVariable Long id) {
        carrierService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/integrations/sync")
    public List<CarrierResponse> synchronizeCarriers() {
        return carrierIntegrationService.synchronizeCarriers();
    }
}

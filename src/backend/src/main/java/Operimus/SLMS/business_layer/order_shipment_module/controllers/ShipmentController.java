package Operimus.SLMS.business_layer.order_shipment_module.controllers;

import Operimus.SLMS.business_layer.order_shipment_module.dto.ShipmentRequest;
import Operimus.SLMS.business_layer.order_shipment_module.dto.ShipmentResponse;
import Operimus.SLMS.business_layer.order_shipment_module.entities.Shipment;
import Operimus.SLMS.business_layer.order_shipment_module.service.ShipmentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost", "http://localhost:3000"}, allowCredentials = "true")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(@Valid @RequestBody ShipmentRequest request) {
        log.info("Creating shipment: {} with carrier: {} and driver: {}",
                request.getShipmentCode(), request.getCarrierId(), request.getDriverId());

        Shipment shipment = shipmentService.create(
                request.getShipmentCode(),
                request.getCarrierId(),
                request.getDriverId(),
                request.getOrderIds()
        );

        ShipmentResponse response = mapToResponse(shipment);
        return ResponseEntity
                .created(URI.create("/api/shipments/" + response.getId()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> getShipment(@PathVariable Long id) {
        log.info("Fetching shipment: {}", id);
        Shipment shipment = shipmentService.getById(id);
        return ResponseEntity.ok(mapToResponse(shipment));
    }

    @GetMapping
    public ResponseEntity<List<ShipmentResponse>> getAllShipments() {
        log.info("Fetching all shipments");
        List<Shipment> shipments = shipmentService.getAll();
        return ResponseEntity.ok(shipments.stream().map(this::mapToResponse).toList());
    }

    @PutMapping("/{id}/driver")
    public ResponseEntity<ShipmentResponse> updateDriver(
            @PathVariable Long id,
            @RequestParam String driverId) {
        log.info("Updating driver for shipment {} to {}", id, driverId);

        Shipment shipment = shipmentService.updateDriver(id, driverId);
        return ResponseEntity.ok(mapToResponse(shipment));
    }

    @GetMapping("/carrier/{carrierId}")
    public ResponseEntity<List<ShipmentResponse>> getByCarrier(@PathVariable Long carrierId) {
        log.info("Fetching shipments for carrier: {}", carrierId);

        List<Shipment> shipments = shipmentService.getByCarrierId(carrierId);
        return ResponseEntity.ok(shipments.stream().map(this::mapToResponse).toList());
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<ShipmentResponse>> getByDriver(@PathVariable String driverId) {
        log.info("Fetching shipments for driver: {}", driverId);

        List<Shipment> shipments = shipmentService.getByDriverId(driverId);
        return ResponseEntity.ok(shipments.stream().map(this::mapToResponse).toList());
    }

    private ShipmentResponse mapToResponse(Shipment shipment) {
        return ShipmentResponse.builder()
                .id(shipment.getId())
                .shipmentCode(shipment.getShipmentCode())
                .carrierId(shipment.getCarrier().getId())
                .carrierCode(shipment.getCarrier().getCode())
                .carrierName(shipment.getCarrier().getName())
                .driverId(shipment.getDriverId())
                .shippoShipmentId(shipment.getShippoShipmentId())
                .shippoTransactionId(shipment.getShippoTransactionId())
                .shipstationOrderId(shipment.getShipstationOrderId())
                .shipstationCarrierCode(shipment.getShipstationCarrierCode())
                .shipstationServiceCode(shipment.getShipstationServiceCode())
                .totalOrders(shipment.getTotalOrders())
                .totalWeight(shipment.getTotalWeight())
                .weightUnit(shipment.getWeightUnit())
                .estimatedDeliveryDate(shipment.getEstimatedDeliveryDate())
                .shippingCost(shipment.getShippingCost())
                .transitTime(shipment.getTransitTime())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .build();
    }
}

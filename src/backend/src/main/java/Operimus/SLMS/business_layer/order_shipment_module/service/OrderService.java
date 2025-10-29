package Operimus.SLMS.business_layer.order_shipment_module.service;

import Operimus.SLMS.business_layer.order_shipment_module.dto.InMemoryMultipartFile;
import Operimus.SLMS.business_layer.order_shipment_module.dto.OrderRequest;
import Operimus.SLMS.business_layer.order_shipment_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrder;
import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrderStatus;
import Operimus.SLMS.business_layer.order_shipment_module.entities.Warehouse;
import Operimus.SLMS.business_layer.order_shipment_module.repository.ShipmentOrderRepository;
import Operimus.SLMS.business_layer.shared.exception.ResourceConflictException;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final ShipmentOrderRepository shipmentOrderRepository;
    private final WarehouseService warehouseService;
    private final OrderDocumentService orderDocumentService;  // Adicionando dependência para o upload
    private final ShipmentOrderEventService shipmentOrderEventService;

    @Transactional
    public OrderResponse create(OrderRequest request) {
        if (shipmentOrderRepository.existsByReferenceIgnoreCase(request.getReference())) {
            throw new ResourceConflictException(
                    "An order with reference '%s' already exists.".formatted(request.getReference()));
        }

        Warehouse warehouse = warehouseService.findEntityById(request.getWarehouseId());

        // Criar a encomenda
        ShipmentOrder order = ShipmentOrder.builder()
                .reference(request.getReference())
                .description(request.getDescription())
                .status(resolveStatus(request.getStatus()))
                .shippingAddress(request.getShippingAddress())
                .estimatedDeliveryDate(request.getEstimatedDeliveryDate())
                .warehouse(warehouse)
                .shippoShipmentId(request.getShippoShipmentId())
                .shippoTransactionId(request.getShippoTransactionId())
                .shipstationOrderId(request.getShipstationOrderId())
                .shipstationCarrierCode(request.getShipstationCarrierCode())
                .shipstationServiceCode(request.getShipstationServiceCode())
                .trackingNumber(request.getTrackingNumber())
                .build();

        // Salvar a encomenda no banco (gera orderId)
        ShipmentOrder saved = shipmentOrderRepository.save(order);
        shipmentOrderEventService.recordStatusChange(saved, null, saved.getStatus());

        // Gerar o QR code para a encomenda
        byte[] qrImage = generateQrCode(saved.getId().toString(), 300);  // Gerando o QR code

        // Criar o MultipartFile a partir do byte[] do QR code
        MultipartFile qrFile = new InMemoryMultipartFile("qr-" + saved.getId() + ".png", qrImage);

        // Fazer o upload do QR code para o MinIO
        String qrKey = "orders/" + saved.getId() + "/qr.png";  // Definindo a chave do arquivo no MinIO
        orderDocumentService.uploadDocument(saved.getId(), qrFile);  // Realizando o upload do QR code

        return mapToResponse(saved);
    }

    @Transactional
    public OrderResponse update(Long id, OrderRequest request) {
        ShipmentOrder order = shipmentOrderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order %d not found".formatted(id)));

        boolean isReferenceChanging = !order.getReference().equalsIgnoreCase(request.getReference());
        if (isReferenceChanging && shipmentOrderRepository.existsByReferenceIgnoreCase(request.getReference())) {
            throw new ResourceConflictException(
                    "An order with reference '%s' already exists.".formatted(request.getReference()));
        }

        Warehouse warehouse = warehouseService.findEntityById(request.getWarehouseId());

        ShipmentOrderStatus previousStatus = order.getStatus();
        ShipmentOrderStatus newStatus = resolveStatus(request.getStatus());
        boolean statusChanged = previousStatus != newStatus;

        order.setReference(request.getReference());
        order.setDescription(request.getDescription());
        order.setStatus(newStatus);
        order.setShippingAddress(request.getShippingAddress());
        order.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());
        order.setWarehouse(warehouse);
        order.setShippoShipmentId(request.getShippoShipmentId());
        order.setShippoTransactionId(request.getShippoTransactionId());
        order.setShipstationOrderId(request.getShipstationOrderId());
        order.setShipstationCarrierCode(request.getShipstationCarrierCode());
        order.setShipstationServiceCode(request.getShipstationServiceCode());
        order.setTrackingNumber(request.getTrackingNumber());

        if (statusChanged) {
            shipmentOrderEventService.recordStatusChange(order, previousStatus, newStatus);
        }

        return mapToResponse(order);
    }

    @Transactional
    public void delete(Long id) {
        ShipmentOrder order = shipmentOrderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order %d not found".formatted(id)));
        shipmentOrderRepository.delete(order);
    }

    @Transactional
    public OrderResponse confirmPickup(Long id) {
        ShipmentOrder order = shipmentOrderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order %d not found".formatted(id)));

        ShipmentOrderStatus currentStatus = order.getStatus();
        if (currentStatus == ShipmentOrderStatus.CANCELLED) {
            throw new ResourceConflictException("Cancelled order %d cannot be collected.".formatted(id));
        }
        if (currentStatus == ShipmentOrderStatus.DELIVERED) {
            throw new ResourceConflictException("Delivered order %d cannot be collected again.".formatted(id));
        }
        if (currentStatus == ShipmentOrderStatus.COLLECTED) {
            return mapToResponse(order);
        }
        if (currentStatus != ShipmentOrderStatus.READY_FOR_PICKUP) {
            throw new ResourceConflictException(
                    "Order %d must be ready for pickup before it can be collected.".formatted(id));
        }

        order.setStatus(ShipmentOrderStatus.COLLECTED);
        shipmentOrderEventService.recordStatusChange(order, currentStatus, ShipmentOrderStatus.COLLECTED);
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        ShipmentOrder order = shipmentOrderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order %d not found".formatted(id)));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAll() {
        return shipmentOrderRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    private ShipmentOrderStatus resolveStatus(ShipmentOrderStatus status) {
        return status == null ? ShipmentOrderStatus.CREATED : status;
    }

    private OrderResponse mapToResponse(ShipmentOrder order) {
        return OrderResponse.builder()
                .id(order.getId())
                .reference(order.getReference())
                .description(order.getDescription())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .estimatedDeliveryDate(order.getEstimatedDeliveryDate())
                .warehouseId(order.getWarehouse().getId())
                .shippoShipmentId(order.getShippoShipmentId())
                .shippoTransactionId(order.getShippoTransactionId())
                .shipstationOrderId(order.getShipstationOrderId())
                .shipstationCarrierCode(order.getShipstationCarrierCode())
                .shipstationServiceCode(order.getShipstationServiceCode())
                .trackingNumber(order.getTrackingNumber())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
    private byte[] generateQrCode(String orderId, int size) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = null;
         try {
            bitMatrix = qrCodeWriter.encode(orderId, BarcodeFormat.QR_CODE, size, size);
        } catch (WriterException e) {
            throw new RuntimeException("Erro ao gerar QR code", e);  // Tratar a exceção adequadamente
        }
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        try {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar QR code", e);
        }
        return pngOutputStream.toByteArray();
    }
}

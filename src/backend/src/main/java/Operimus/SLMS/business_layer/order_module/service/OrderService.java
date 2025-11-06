package Operimus.SLMS.business_layer.order_module.service;

import Operimus.SLMS.business_layer.order_module.dto.InMemoryMultipartFile;
import Operimus.SLMS.business_layer.order_module.dto.OrderRequest;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.entities.Order;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.entities.Warehouse;
import Operimus.SLMS.business_layer.order_module.repository.OrderRepository;
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

    private final OrderRepository orderRepository;
    private final WarehouseService warehouseService;
    private final OrderDocumentService orderDocumentService;
    private final OrderEventService orderEventService;

    @Transactional
    public OrderResponse create(OrderRequest request) {
        if (orderRepository.existsByReferenceIgnoreCase(request.getReference())) {
            throw new ResourceConflictException(
                    "An order with reference '%s' already exists.".formatted(request.getReference()));
        }

        Warehouse warehouse = warehouseService.findEntityById(request.getWarehouseId());

        // Create the order
        Order order = Order.builder()
                .reference(request.getReference())
                .description(request.getDescription())
                .status(resolveStatus(request.getStatus()))
                .shippingAddress(request.getShippingAddress())
                .estimatedDeliveryDate(request.getEstimatedDeliveryDate())
                .warehouse(warehouse)
                .customerId(request.getCustomerId())
                .trackingNumber(request.getTrackingNumber())
                .weight(request.getWeight())
                .weightUnit(request.getWeightUnit())
                .length(request.getLength())
                .width(request.getWidth())
                .height(request.getHeight())
                .distanceUnit(request.getDistanceUnit())
                .build();

        // Save the order in the database (generates orderId)
        Order saved = orderRepository.save(order);
        orderEventService.recordStatusChange(saved, null, saved.getStatus());

        // Generate the QR code for the order
        byte[] qrImage = generateQrCode(saved.getId().toString(), 300);

        // Create the MultipartFile from the byte[] of the QR code
        MultipartFile qrFile = new InMemoryMultipartFile("qr-" + saved.getId() + ".png", qrImage);

        // Upload the QR code to MinIO
        orderDocumentService.uploadDocument(saved.getId(), qrFile);

        return mapToResponse(saved);
    }

    @Transactional
    public OrderResponse update(Long id, OrderRequest request) {
        Order order = orderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order %d not found".formatted(id)));

        boolean isReferenceChanging = !order.getReference().equalsIgnoreCase(request.getReference());
        if (isReferenceChanging && orderRepository.existsByReferenceIgnoreCase(request.getReference())) {
            throw new ResourceConflictException(
                    "An order with reference '%s' already exists.".formatted(request.getReference()));
        }

        Warehouse warehouse = warehouseService.findEntityById(request.getWarehouseId());

        OrderStatus previousStatus = order.getStatus();
        OrderStatus newStatus = resolveStatus(request.getStatus());
        boolean statusChanged = previousStatus != newStatus;

        order.setReference(request.getReference());
        order.setDescription(request.getDescription());
        order.setStatus(newStatus);
        order.setShippingAddress(request.getShippingAddress());
        order.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());
        order.setWarehouse(warehouse);
        order.setCustomerId(request.getCustomerId());
        order.setTrackingNumber(request.getTrackingNumber());
        order.setWeight(request.getWeight());
        order.setWeightUnit(request.getWeightUnit());
        order.setLength(request.getLength());
        order.setWidth(request.getWidth());
        order.setHeight(request.getHeight());
        order.setDistanceUnit(request.getDistanceUnit());

        if (statusChanged) {
            orderEventService.recordStatusChange(order, previousStatus, newStatus);
        }

        return mapToResponse(order);
    }

    @Transactional
    public void delete(Long id) {
        Order order = orderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order %d not found".formatted(id)));
        orderRepository.delete(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        Order order = orderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order %d not found".formatted(id)));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAll() {
        return orderRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getByCustomerId(String customerId) {
        return orderRepository.findAll().stream()
                .filter(order -> order.getCustomerId() != null && order.getCustomerId().equals(customerId))
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getByStatus(List<OrderStatus> statuses) {
        return orderRepository.findAll().stream()
                .filter(order -> statuses.contains(order.getStatus()))
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getByTrackingNumber(String trackingNumber) {
        Order order = orderRepository.findAll().stream()
                .filter(o -> trackingNumber.equals(o.getTrackingNumber()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Order with tracking number '%s' not found".formatted(trackingNumber)));
        return mapToResponse(order);
    }

    private OrderStatus resolveStatus(OrderStatus status) {
        return status == null ? OrderStatus.CREATED : status;
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .reference(order.getReference())
                .description(order.getDescription())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .estimatedDeliveryDate(order.getEstimatedDeliveryDate())
                .warehouseId(order.getWarehouse().getId())
                .shipmentId(order.getShipmentId())
                .shipmentCode(null) // Shipment code not available in Order entity (cross-module)
                .trackingNumber(order.getTrackingNumber())
                .customerId(order.getCustomerId())
                .qrCodeUrl(order.getQrCodeUrl())
                .weight(order.getWeight())
                .weightUnit(order.getWeightUnit())
                .length(order.getLength())
                .width(order.getWidth())
                .height(order.getHeight())
                .distanceUnit(order.getDistanceUnit())
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
            throw new RuntimeException("Error generating QR code", e);
        }
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        try {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error generating QR code", e);
        }
        return pngOutputStream.toByteArray();
    }
}

package Operimus.SLMS.business_layer.order_module.service;

import Operimus.SLMS.business_layer.order_module.dto.DocumentUploadResponse;
import Operimus.SLMS.business_layer.order_module.dto.InMemoryMultipartFile;
import Operimus.SLMS.business_layer.order_module.dto.OrderRequest;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.dto.OrderScanResult;
import Operimus.SLMS.business_layer.order_module.entities.Order;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.entities.Warehouse;
import Operimus.SLMS.business_layer.order_module.repository.OrderRepository;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Shipment;
import Operimus.SLMS.business_layer.rate_carrier_module.repository.ShipmentRepository;
import Operimus.SLMS.business_layer.shared.exception.ResourceConflictException;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
    private final ShipmentRepository shipmentRepository;
    private final KeycloakUserService keycloakUserService;

    @Value("${slms.qr.confirmation-base-url:http://localhost:8080/api/driver}")
    private String qrConfirmationBaseUrl;

    @Transactional
    public OrderResponse create(OrderRequest request) {
        if (orderRepository.existsByReferenceIgnoreCase(request.getReference())) {
            throw new ResourceConflictException("An order with reference '%s' already exists.".formatted(request.getReference()));
        }

        Warehouse warehouse = warehouseService.findEntityById(request.getWarehouseId());

        // Create the order
        Order order = Order.builder()
                .reference(request.getReference())
                .description(request.getDescription())
                .status(resolveStatus(request.getStatus()))
                .shippingAddress(request.getShippingAddress())
                .shippingAddressLine1(request.getShippingAddressLine1())
                .shippingAddressLine2(request.getShippingAddressLine2())
                .shippingCity(request.getShippingCity())
                .shippingStateOrProvince(request.getShippingStateOrProvince())
                .shippingPostalCode(request.getShippingPostalCode())
                .shippingCountry(request.getShippingCountry())
                .shippingPhone(request.getShippingPhone())
                .shippingEmail(request.getShippingEmail())
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
        normalizeShippingAddress(order);
        Order saved = orderRepository.save(order);
        orderEventService.recordStatusChange(saved, null, saved.getStatus());

        String confirmationUrl = buildConfirmationUrl(saved.getId());

        // Generate the QR code for the order using the confirmation callback URL
        byte[] qrImage = generateQrCode(confirmationUrl, 300);

        // Create the MultipartFile from the byte[] of the QR code
        MultipartFile qrFile = new InMemoryMultipartFile("qr-" + saved.getId() + ".png", qrImage);

        // Upload the QR code to MinIO
        DocumentUploadResponse uploadResponse = orderDocumentService.uploadDocument(saved.getId(), qrFile);

        saved.setQrCodeUrl(uploadResponse.getUrl());
        saved.setQrConfirmationUrl(confirmationUrl);
        saved.setQrConfirmedAt(null);
        saved.setQrConfirmedBy(null);

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
        order.setShippingAddressLine1(request.getShippingAddressLine1());
        order.setShippingAddressLine2(request.getShippingAddressLine2());
        order.setShippingCity(request.getShippingCity());
        order.setShippingStateOrProvince(request.getShippingStateOrProvince());
        order.setShippingPostalCode(request.getShippingPostalCode());
        order.setShippingCountry(request.getShippingCountry());
        order.setShippingPhone(request.getShippingPhone());
        order.setShippingEmail(request.getShippingEmail());
        order.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());
        order.setWarehouse(warehouse);
        // Only update customerId if provided in request (preserve existing if null)
        if (request.getCustomerId() != null) {
            order.setCustomerId(request.getCustomerId());
        }
        order.setTrackingNumber(request.getTrackingNumber());
        order.setWeight(request.getWeight());
        order.setWeightUnit(request.getWeightUnit());
        order.setLength(request.getLength());
        order.setWidth(request.getWidth());
        order.setHeight(request.getHeight());
        order.setDistanceUnit(request.getDistanceUnit());

        normalizeShippingAddress(order);

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

        // Delete associated order events first to avoid foreign key constraint violation
        orderEventService.deleteByOrderId(id);

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
    public List<OrderResponse> getByWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseService.findEntityById(warehouseId);
        return orderRepository.findByWarehouse(warehouse).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getByWarehouseAndStatus(Long warehouseId, OrderStatus status) {
        Warehouse warehouse = warehouseService.findEntityById(warehouseId);
        return orderRepository.findByWarehouseAndStatus(warehouse, status).stream()
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

    @Transactional
    public OrderScanResult confirmOrderScan(Long orderId, String driverId) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order %d not found".formatted(orderId)));

        if (order.getShipmentId() == null) {
            throw new ResourceConflictException("Order %d is not yet assigned to a shipment".formatted(orderId));
        }

        Shipment shipment = shipmentRepository
                .findById(order.getShipmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shipment %d not found".formatted(order.getShipmentId())));

        String resolvedDriverId = normalizeDriverIdentifier(driverId);
        if (!StringUtils.hasText(resolvedDriverId)) {
            throw new IllegalArgumentException("Driver ID is required to confirm the scan");
        }

        if (!resolvedDriverId.equals(shipment.getDriverId())) {
            throw new AccessDeniedException("Driver is not assigned to this shipment");
        }

        boolean alreadyConfirmed = order.getStatus() == OrderStatus.IN_TRANSIT
                || order.getStatus() == OrderStatus.DELIVERED;

        OffsetDateTime confirmationTime = OffsetDateTime.now(ZoneOffset.UTC);
        OrderStatus previousStatus = order.getStatus();

        if (!alreadyConfirmed) {
            order.setStatus(OrderStatus.IN_TRANSIT);
            orderEventService.recordStatusChange(order, previousStatus, OrderStatus.IN_TRANSIT);
        }

        order.setQrConfirmedAt(confirmationTime);
        order.setQrConfirmedBy(resolvedDriverId);

        List<Order> shipmentOrders = orderRepository.findByShipmentId(order.getShipmentId());
        long ordersStillPending = shipmentOrders.stream()
                .filter(o -> requiresScan(o.getStatus()))
                .count();

        boolean allOrdersConfirmed = ordersStillPending == 0;

        return OrderScanResult.builder()
                .orderId(order.getId())
                .orderReference(order.getReference())
                .shipmentId(shipment.getId())
                .shipmentCode(shipment.getShipmentCode())
                .orderStatus(order.getStatus())
                .confirmedAt(order.getQrConfirmedAt())
                .alreadyConfirmed(alreadyConfirmed)
                .allOrdersConfirmed(allOrdersConfirmed)
                .remainingOrdersToScan(allOrdersConfirmed ? 0 : ordersStillPending)
                .build();
    }

    private OrderStatus resolveStatus(OrderStatus status) {
        return status == null ? OrderStatus.CREATED : status;
    }

    private OrderResponse mapToResponse(Order order) {
        // Fetch shipment data if order is assigned to a shipment
        String shipmentCode = null;
        Long carrierId = null;
        String carrierCode = null;
        String carrierName = null;

        if (order.getShipmentId() != null) {
            Shipment shipment = shipmentRepository.findById(order.getShipmentId()).orElse(null);
            if (shipment != null) {
                shipmentCode = shipment.getShipmentCode();
                if (shipment.getCarrier() != null) {
                    carrierId = shipment.getCarrier().getId();
                    carrierCode = shipment.getCarrier().getCode();
                    carrierName = shipment.getCarrier().getName();
                }
            }
        }

        return OrderResponse.builder()
                .id(order.getId())
                .reference(order.getReference())
                .description(order.getDescription())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .shippingAddressLine1(order.getShippingAddressLine1())
                .shippingAddressLine2(order.getShippingAddressLine2())
                .shippingCity(order.getShippingCity())
                .shippingStateOrProvince(order.getShippingStateOrProvince())
                .shippingPostalCode(order.getShippingPostalCode())
                .shippingCountry(order.getShippingCountry())
                .shippingPhone(order.getShippingPhone())
                .shippingEmail(order.getShippingEmail())
                .estimatedDeliveryDate(order.getEstimatedDeliveryDate())
                .warehouseId(order.getWarehouse().getId())
                .shipmentId(order.getShipmentId())
                .shipmentCode(shipmentCode)
                .carrierId(carrierId)
                .carrierCode(carrierCode)
                .carrierName(carrierName)
                .trackingNumber(order.getTrackingNumber())
                .customerId(order.getCustomerId())
                .qrCodeUrl(order.getQrCodeUrl())
                .qrConfirmationUrl(order.getQrConfirmationUrl())
                .qrConfirmedAt(order.getQrConfirmedAt())
                .qrConfirmedBy(order.getQrConfirmedBy())
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

    private String normalizeDriverIdentifier(String driverIdentifier) {
        if (!StringUtils.hasText(driverIdentifier)) {
            return driverIdentifier;
        }
        if (looksLikeUuid(driverIdentifier)) {
            return driverIdentifier;
        }
        try {
            return keycloakUserService.searchUsers(driverIdentifier).stream()
                    .filter(user -> matchesIdentifier(driverIdentifier, user))
                    .map(UserRepresentation::getId)
                    .findFirst()
                    .orElse(driverIdentifier);
        } catch (Exception ex) {
            return driverIdentifier;
        }
    }

    private boolean matchesIdentifier(String identifier, UserRepresentation user) {
        if (user == null) {
            return false;
        }
        return identifier.equalsIgnoreCase(user.getUsername())
                || identifier.equalsIgnoreCase(user.getEmail());
    }

    private boolean looksLikeUuid(String value) {
        return value.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }

    private String buildConfirmationUrl(Long orderId) {
        String baseUrl = qrConfirmationBaseUrl == null ? "" : qrConfirmationBaseUrl.trim();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return "%s/orders/%d/confirm".formatted(baseUrl, orderId);
    }

    private boolean requiresScan(OrderStatus status) {
        if (status == null) {
            return true;
        }
        return status == OrderStatus.CREATED
                || status == OrderStatus.READY_FOR_PICKUP
                || status == OrderStatus.COLLECTED
                || status == OrderStatus.ASSIGNED_TO_SHIPMENT;
    }

    private byte[] generateQrCode(String payload, int size) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, size, size);
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

    private void normalizeShippingAddress(Order order) {
        if (order == null) {
            return;
        }

        AddressComponents components = parseAddress(order.getShippingAddress());

        if (!hasText(order.getShippingAddressLine1()) && hasText(components.line1())) {
            order.setShippingAddressLine1(components.line1());
        }
        if (!hasText(order.getShippingCity()) && hasText(components.city())) {
            order.setShippingCity(components.city());
        }
        if (!hasText(order.getShippingStateOrProvince()) && hasText(components.state())) {
            order.setShippingStateOrProvince(components.state());
        }
        if (!hasText(order.getShippingPostalCode()) && hasText(components.postalCode())) {
            order.setShippingPostalCode(components.postalCode());
        }
        if (!hasText(order.getShippingCountry()) && hasText(components.country())) {
            order.setShippingCountry(components.country());
        }

        if (!hasText(order.getShippingAddressLine1()) && hasText(order.getShippingAddress())) {
            order.setShippingAddressLine1(order.getShippingAddress());
        }
        if (!hasText(order.getShippingCountry())) {
            order.setShippingCountry("US");
        }
    }

    private AddressComponents parseAddress(String raw) {
        if (!StringUtils.hasText(raw)) {
            return AddressComponents.empty();
        }
        String normalized = raw.replace("\n", ",");
        String[] tokens = Arrays.stream(normalized.split(","))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .toArray(String[]::new);

        String line1 = tokens.length > 0 ? tokens[0] : null;
        String city = tokens.length > 1 ? tokens[1] : null;
        String country = tokens.length > 0 ? tokens[tokens.length - 1] : null;
        String state = null;
        String postal = null;

        if (tokens.length > 2) {
            String statePostal = tokens[tokens.length - 2];
            String[] pieces = statePostal.trim().split("\\s+");
            if (pieces.length >= 2) {
                state = pieces[0];
                postal = pieces[pieces.length - 1];
            } else {
                state = statePostal;
            }

            if (tokens.length > 3) {
                city = tokens[1];
            }
        }

        return new AddressComponents(line1, city, state, postal, country);
    }

    private boolean hasText(String value) {
        return StringUtils.hasText(value);
    }

    private record AddressComponents(
            String line1, String city, String state, String postalCode, String country) {
        static AddressComponents empty() {
            return new AddressComponents(null, null, null, null, null);
        }
    }
}

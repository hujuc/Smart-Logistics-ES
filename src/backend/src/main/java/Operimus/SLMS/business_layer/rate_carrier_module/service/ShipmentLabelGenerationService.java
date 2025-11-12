package Operimus.SLMS.business_layer.rate_carrier_module.service;

import Operimus.SLMS.business_layer.order_module.dto.ShipmentDocumentResponse;
import Operimus.SLMS.business_layer.order_module.entities.Order;
import Operimus.SLMS.business_layer.order_module.entities.Warehouse;
import Operimus.SLMS.business_layer.order_module.service.ShipmentDocumentService;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.ShippoLabelResponse;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.ShippingRateRequest;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.ShippingRateResponse;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Carrier;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Shipment;
import Operimus.SLMS.business_layer.rate_carrier_module.integration.ShippoApiClient;
import Operimus.SLMS.business_layer.shared.exception.ExternalServiceException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentLabelGenerationService {

    private final ShippoApiClient shippoApiClient;
    private final ShipmentDocumentService shipmentDocumentService;

    private static final String DEFAULT_COMPANY = "SLMS Warehouse";
    private static final String DEFAULT_PHONE = "+15555550100";
    private static final String DEFAULT_EMAIL = "logistics@example.com";
    private static final Set<String> PENDING_TRANSACTION_STATUSES = Set.of("QUEUED", "WAITING", "PROCESSING");
    private static final Set<String> FAILURE_TRANSACTION_STATUSES = Set.of("ERROR", "FAILED", "CANCELED");

    public void generateCarrierDocuments(Shipment shipment, List<Order> orders) {
        if (shipment == null || orders == null || orders.isEmpty()) {
            return;
        }

        Carrier carrier = shipment.getCarrier();
        if (carrier == null) {
            log.debug("Shipment {} does not have a carrier assigned", shipment.getId());
            return;
        }

        if (!StringUtils.hasText(carrier.getShippoCarrierId())) {
            log.info("Skipping Shippo document generation for shipment {} - carrier {} has no Shippo account",
                    shipment.getId(), carrier.getCode());
            return;
        }

        for (Order order : orders) {
            try {
                generateForOrder(shipment, carrier, order);
            } catch (ExternalServiceException ex) {
                log.warn("Shippo integration failed for shipment {} order {}: {}",
                        shipment.getId(), order.getId(), ex.getMessage());
            } catch (Exception ex) {
                log.warn("Unable to generate shipping document for shipment {} order {}",
                        shipment.getId(), order.getId(), ex);
            }
        }
    }

    private void generateForOrder(Shipment shipment, Carrier carrier, Order order) {
        ShippingRateRequest rateRequest = buildRateRequest(order);
        ShippingRateResponse rateResponse = shippoApiClient.calculateRates(rateRequest);
        log.debug("Shippo returned {} rates for shipment {} order {}", rateResponse.getRates().size(),
                shipment.getId(), order.getId());

        Optional<ShippingRateResponse.RateQuote> selectedRate = selectRate(rateResponse.getRates(), carrier);
        if (selectedRate.isEmpty()) {
            log.info("No Shippo rates matched carrier {} for order {}", carrier.getCode(), order.getId());
            return;
        }

        String rateId = selectedRate.get().getRateId();
        if (!StringUtils.hasText(rateId)) {
            log.info("Selected Shippo rate does not include a rate identifier for order {}", order.getId());
            return;
        }

        ShippoLabelResponse labelResponse = shippoApiClient.purchaseLabel(rateId, "PDF");
        labelResponse = awaitLabelUrl(labelResponse);
        if (!StringUtils.hasText(labelResponse.getLabelUrl())) {
            String status = defaultString(labelResponse.getStatus(), "UNKNOWN");
            log.warn("Shippo label response did not include a label URL for order {} (status={}, messages={})",
                    order.getId(), status, labelResponse.getMessages());
            return;
        }

        byte[] labelContent = shippoApiClient.downloadLabel(labelResponse.getLabelUrl());
        if (labelContent == null || labelContent.length == 0) {
            log.warn("Downloaded label for order {} is empty", order.getId());
            return;
        }

        Map<String, String> metadata = new HashMap<>();
        metadata.put("order-reference", defaultString(order.getReference()));
        metadata.put("carrier-code", defaultString(carrier.getCode()));
        metadata.put("service-level", defaultString(labelResponse.getServiceLevel()));
        metadata.put("shippo-shipment-id", defaultString(labelResponse.getShipmentId()));
        metadata.put("shippo-transaction-id", defaultString(labelResponse.getTransactionId()));
        metadata.put("shippo-rate-id", defaultString(labelResponse.getRateId()));
        metadata.put("tracking-number", defaultString(labelResponse.getTrackingNumber()));

        String documentId = "carrier-label-%d.pdf".formatted(order.getId());
        String fileName = "Carrier Label - %s.pdf".formatted(order.getReference());

        ShipmentDocumentResponse stored = shipmentDocumentService.saveDocument(
                shipment.getId(),
                order.getId(),
                documentId,
                fileName,
                "application/pdf",
                labelContent,
                metadata);

        log.info("Stored carrier document {} for shipment {} order {}", stored.getDocumentId(), shipment.getId(), order.getId());

        if (StringUtils.hasText(labelResponse.getTrackingNumber())) {
            order.setTrackingNumber(labelResponse.getTrackingNumber());
        }
        if (StringUtils.hasText(labelResponse.getShipmentId())) {
            shipment.setShippoShipmentId(labelResponse.getShipmentId());
        }
        if (StringUtils.hasText(labelResponse.getTransactionId())) {
            shipment.setShippoTransactionId(labelResponse.getTransactionId());
        }
    }

    private Optional<ShippingRateResponse.RateQuote> selectRate(List<ShippingRateResponse.RateQuote> rates, Carrier carrier) {
        if (rates == null || rates.isEmpty()) {
            return Optional.empty();
        }
        return rates.stream()
                .peek(rate -> log.debug(
                        "Evaluating rate {} (carrierAccount={}, carrierCode={}, provider={}) against carrier {} (account={}, code={}, name={})",
                        rate.getRateId(),
                        rate.getCarrierAccount(),
                        rate.getCarrierCode(),
                        rate.getProvider(),
                        carrier.getCode(),
                        carrier.getShippoAccountId(),
                        carrier.getCode(),
                        carrier.getName()))
                .filter(rate -> matchesCarrier(rate, carrier))
                .min(Comparator.comparing(rate -> rate.getAmount() != null ? rate.getAmount() : BigDecimal.valueOf(Double.MAX_VALUE)));
    }

    private boolean matchesCarrier(ShippingRateResponse.RateQuote rate, Carrier carrier) {
        if (rate == null || carrier == null) {
            return false;
        }
        boolean matchesAccount = StringUtils.hasText(carrier.getShippoAccountId())
                && carrier.getShippoAccountId().equalsIgnoreCase(defaultString(rate.getCarrierAccount()));
        boolean matchesCode = StringUtils.hasText(carrier.getCode())
                && carrier.getCode().equalsIgnoreCase(defaultString(rate.getCarrierCode()));
        boolean matchesProvider = StringUtils.hasText(rate.getProvider())
                && carrier.getName() != null
                && carrier.getName().equalsIgnoreCase(rate.getProvider());
        return matchesAccount || matchesCode || matchesProvider;
    }

    private ShippingRateRequest buildRateRequest(Order order) {
        Warehouse warehouse = order.getWarehouse();
        if (warehouse == null) {
            throw new IllegalStateException("Order %d is not linked to a warehouse".formatted(order.getId()));
        }

        ShippingRateRequest.Address fromAddress = ShippingRateRequest.Address.builder()
                .name(hasText(warehouse.getName()) ? warehouse.getName() : DEFAULT_COMPANY)
                .company(hasText(warehouse.getName()) ? warehouse.getName() : DEFAULT_COMPANY)
                .addressLine1(defaultString(warehouse.getAddressLine1(), "Warehouse Street"))
                .addressLine2(warehouse.getAddressLine2())
                .city(defaultString(warehouse.getCity(), "San Francisco"))
                .stateOrProvince(defaultString(warehouse.getStateOrProvince(), "CA"))
                .postalCode(defaultString(warehouse.getPostalCode(), "94117"))
                .country(defaultString(warehouse.getCountry(), "US"))
                .phone(DEFAULT_PHONE)
                .email(DEFAULT_EMAIL)
                .build();

        ParsedAddress destination = parseDestination(order.getShippingAddress());
        ShippingRateRequest.Address toAddress = ShippingRateRequest.Address.builder()
                .name(hasText(order.getCustomerId()) ? order.getCustomerId() : defaultString(order.getReference(), "Customer"))
                .company(hasText(order.getCustomerId()) ? order.getCustomerId() : "Customer")
                .addressLine1(defaultString(order.getShippingAddressLine1(), defaultString(destination.addressLine1, "Customer Address")))
                .addressLine2(defaultString(order.getShippingAddressLine2(), destination.addressLine2))
                .city(defaultString(defaultString(order.getShippingCity(), destination.city), "San Jose"))
                .stateOrProvince(defaultString(defaultString(order.getShippingStateOrProvince(), destination.state), "CA"))
                .postalCode(defaultString(defaultString(order.getShippingPostalCode(), destination.postalCode), "95122"))
                .country(defaultString(defaultString(order.getShippingCountry(), destination.country), "US"))
                .phone(DEFAULT_PHONE)
                .email(DEFAULT_EMAIL)
                .build();

        BigDecimal weight = BigDecimal.valueOf(order.getWeight() != null ? order.getWeight() : 1.0d);
        String weightUnit = StringUtils.hasText(order.getWeightUnit()) ? order.getWeightUnit() : "kg";

        ShippingRateRequest.Parcel parcel = ShippingRateRequest.Parcel.builder()
                .weight(weight)
                .weightUnit(weightUnit)
                .length(order.getLength() != null ? BigDecimal.valueOf(order.getLength()) : BigDecimal.valueOf(30))
                .width(order.getWidth() != null ? BigDecimal.valueOf(order.getWidth()) : BigDecimal.valueOf(20))
                .height(order.getHeight() != null ? BigDecimal.valueOf(order.getHeight()) : BigDecimal.valueOf(10))
                .distanceUnit(StringUtils.hasText(order.getDistanceUnit()) ? order.getDistanceUnit() : "cm")
                .build();

        return ShippingRateRequest.builder()
                .fromAddress(fromAddress)
                .toAddress(toAddress)
                .parcels(List.of(parcel))
                .build();
    }

    private ShippoLabelResponse awaitLabelUrl(ShippoLabelResponse initialResponse) {
        ShippoLabelResponse current = initialResponse;
        if (current == null) {
            return ShippoLabelResponse.builder().build();
        }

        String status = normalizeStatus(current.getStatus());
        int attempts = 0;
        while (!StringUtils.hasText(current.getLabelUrl())
                && PENDING_TRANSACTION_STATUSES.contains(status)
                && attempts < 4
                && StringUtils.hasText(current.getTransactionId())) {
            attempts++;
            try {
                Thread.sleep(750L * attempts);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            current = shippoApiClient.getTransaction(current.getTransactionId());
            status = normalizeStatus(current.getStatus());
        }

        if (!StringUtils.hasText(current.getLabelUrl()) && FAILURE_TRANSACTION_STATUSES.contains(status)) {
            throw new ExternalServiceException("Shippo failed to generate label (%s): %s"
                    .formatted(status, current.getMessages()));
        }

        return current;
    }

    private String normalizeStatus(String status) {
        return StringUtils.hasText(status) ? status.trim().toUpperCase() : "";
    }

    private ParsedAddress parseDestination(String raw) {
        if (!StringUtils.hasText(raw)) {
            return new ParsedAddress("Unknown Street", null, "Lisbon", "Lisboa", "1000-001", "PT");
        }

        String normalized = raw.replace('\n', ',');
        String[] parts = normalized.split(",");

        String line1 = parts.length > 0 ? parts[0].trim() : raw.trim();
        String city = parts.length > 1 ? parts[1].trim() : "Lisbon";
        String state = null;
        String postal = null;
        String country = parts.length > 3 ? parts[3].trim() : "US";

        if (parts.length > 2) {
            String statePostal = parts[2].trim();
            String[] tokens = statePostal.split("\\s+");
            if (tokens.length >= 2) {
                state = tokens[0];
                postal = tokens[tokens.length - 1];
            } else {
                state = statePostal;
            }
        }

        if (!StringUtils.hasText(postal)) {
            postal = "00000";
        }
        if (!StringUtils.hasText(state)) {
            state = city;
        }

        return new ParsedAddress(line1, null, city, state, postal, country);
    }

    private String defaultString(String value) {
        return defaultString(value, "");
    }

    private String defaultString(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private boolean hasText(String value) {
        return StringUtils.hasText(value);
    }

    private record ParsedAddress(
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String postalCode,
            String country) {}
}

package Operimus.SLMS.business_layer.reporting_module.service;

import Operimus.SLMS.business_layer.order_module.entities.Order;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.entities.Warehouse;
import Operimus.SLMS.business_layer.rate_carrier_module.repository.CarrierRepository;
import Operimus.SLMS.business_layer.order_module.repository.OrderRepository;
import Operimus.SLMS.business_layer.order_module.repository.WarehouseRepository;
import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException;
import Operimus.SLMS.business_layer.visibility_module.exceptions.repository.DeliveryExceptionRepository;
import Operimus.SLMS.business_layer.reporting_module.dto.LogisticsStatsResponse;
import Operimus.SLMS.business_layer.reporting_module.dto.LogisticsStatsResponse.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating comprehensive logistics statistics
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportStatsService {

    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;
    private final CarrierRepository carrierRepository;
    private final DeliveryExceptionRepository deliveryExceptionRepository;

    /**
     * Calculate comprehensive logistics statistics for the dashboard
     *
     * @param period The period for trend calculation: "daily", "weekly", or "monthly"
     * @return Complete logistics statistics
     */
    @Transactional(readOnly = true)
    public LogisticsStatsResponse calculateStats(String period) {
        log.info("Calculating logistics statistics for period: {}", period);

        // Fetch all orders (in production, you'd optimize with queries and date ranges)
        List<Order> allOrders = orderRepository.findAll();
        List<DeliveryException> allExceptions = deliveryExceptionRepository.findAll();
        List<Warehouse> allWarehouses = warehouseRepository.findAll();

        // Calculate date ranges based on period
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodStart = getPeriodStart(now, period);
        LocalDateTime previousPeriodStart = getPeriodStart(periodStart.minusNanos(1), period);

        // Filter orders by current and previous periods
        List<Order> currentPeriodOrders = filterOrdersByPeriod(allOrders, periodStart, now);
        List<Order> previousPeriodOrders = filterOrdersByPeriod(allOrders, previousPeriodStart, periodStart);

        // Build response
        return LogisticsStatsResponse.builder()
                .period(period)
                .totalShipments(calculateTotalShipments(allOrders, currentPeriodOrders, previousPeriodOrders, period))
                .ordersToday(calculateOrdersToday(allOrders, currentPeriodOrders, previousPeriodOrders, period))
                .pendingPickups(calculatePendingPickups(allOrders, currentPeriodOrders, previousPeriodOrders, period))
                .inTransitNow(calculateInTransit(allOrders, currentPeriodOrders, previousPeriodOrders, period))
                .deliveredToday(calculateDelivered(allOrders, currentPeriodOrders, previousPeriodOrders, period))
                .averageCostPerShipment(calculateAverageCost(currentPeriodOrders, previousPeriodOrders, period))
                .totalShipmentCosts(calculateTotalCosts(currentPeriodOrders, previousPeriodOrders, period))
                .averageDeliveryTime(calculateAverageDeliveryTime(currentPeriodOrders, previousPeriodOrders, period))
                .totalExceptions(calculateExceptions(allExceptions, currentPeriodOrders, previousPeriodOrders, periodStart, previousPeriodStart, period))
                .totalTickets(calculateTickets(allExceptions, periodStart, previousPeriodStart, period))
                .totalWarehouses(calculateWarehouses(allWarehouses, allOrders))
                .activeDrivers(calculateDrivers(period))
                .build();
    }

    private TotalShipmentsMetric calculateTotalShipments(List<Order> allOrders,
                                                         List<Order> current,
                                                         List<Order> previous,
                                                         String period) {
        int currentCount = current.size();
        int previousCount = previous.size();
        Integer trend = calculateTrendPercentage(currentCount, previousCount);

        // Status breakdown
        StatusBreakdown statusBreakdown = StatusBreakdown.builder()
                .created((int) allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CREATED).count())
                .readyForPickup((int) allOrders.stream().filter(o -> o.getStatus() == OrderStatus.ASSIGNED_TO_SHIPMENT).count())
                .collected((int) allOrders.stream().filter(o -> o.getStatus() == OrderStatus.ASSIGNED_TO_SHIPMENT).count())
                .inTransit((int) allOrders.stream().filter(o -> o.getStatus() == OrderStatus.IN_TRANSIT).count())
                .delivered((int) allOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count())
                .cancelled((int) allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count())
                .build();

        // Carrier breakdown - TODO: Orders don't have direct carrier relationship
        List<CarrierBreakdown> carrierBreakdown = new ArrayList<>();

        return TotalShipmentsMetric.builder()
                .value(currentCount)
                .trend(trend)
                .label(getPeriodLabel(period))
                .byStatus(statusBreakdown)
                .byCarrier(carrierBreakdown)
                .build();
    }

    private OrdersMetric calculateOrdersToday(List<Order> allOrders,
                                              List<Order> current,
                                              List<Order> previous,
                                              String period) {
        int currentCount = current.size();
        int previousCount = previous.size();
        Integer trend = calculateTrendPercentage(currentCount, previousCount);

        // Warehouse breakdown
        List<WarehouseBreakdown> warehouseBreakdown = calculateWarehouseBreakdown(current);

        // Status breakdown
        Map<String, Integer> statusBreakdown = current.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getStatus().name(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        return OrdersMetric.builder()
                .value(currentCount)
                .trend(trend)
                .label(getPeriodLabel(period))
                .byWarehouse(warehouseBreakdown)
                .byStatus(statusBreakdown)
                .build();
    }

    private PendingPickupsMetric calculatePendingPickups(List<Order> allOrders,
                                                         List<Order> current,
                                                         List<Order> previous,
                                                         String period) {
        List<Order> currentPending = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CREATED)
                .collect(Collectors.toList());

        int currentCount = currentPending.size();

        List<Order> previousPending = previous.stream()
                .filter(o -> o.getStatus() == OrderStatus.CREATED)
                .collect(Collectors.toList());

        Integer trend = calculateTrendPercentage(currentCount, previousPending.size());

        // Check for overdue (example: ready for pickup > 24 hours)
        OffsetDateTime overdueThreshold = OffsetDateTime.now().minusHours(24);
        int overdueCount = (int) currentPending.stream()
                .filter(o -> o.getUpdatedAt() != null && o.getUpdatedAt().isBefore(overdueThreshold))
                .count();

        String alert = overdueCount > 0 ? overdueCount + " Overdue" : null;
        boolean hasWarning = overdueCount > 0;

        // Warehouse breakdown
        List<WarehouseBreakdown> warehouseBreakdown = calculateWarehouseBreakdown(currentPending);

        // Overdue breakdown
        OffsetDateTime dueSoonThreshold = OffsetDateTime.now().minusHours(12);
        OverdueBreakdown overdueBreakdown = OverdueBreakdown.builder()
                .overdue(overdueCount)
                .dueSoon((int) currentPending.stream()
                        .filter(o -> o.getUpdatedAt() != null &&
                                o.getUpdatedAt().isAfter(overdueThreshold) &&
                                o.getUpdatedAt().isBefore(dueSoonThreshold))
                        .count())
                .onTime((int) currentPending.stream()
                        .filter(o -> o.getUpdatedAt() != null && o.getUpdatedAt().isAfter(dueSoonThreshold))
                        .count())
                .build();

        return PendingPickupsMetric.builder()
                .value(currentCount)
                .trend(trend)
                .label(getPeriodLabel(period))
                .alert(alert)
                .hasWarning(hasWarning)
                .byWarehouse(warehouseBreakdown)
                .overdueBreakdown(overdueBreakdown)
                .build();
    }

    private InTransitMetric calculateInTransit(List<Order> allOrders,
                                               List<Order> current,
                                               List<Order> previous,
                                               String period) {
        List<Order> currentInTransit = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.IN_TRANSIT)
                .collect(Collectors.toList());

        int currentCount = currentInTransit.size();

        List<Order> previousInTransit = previous.stream()
                .filter(o -> o.getStatus() == OrderStatus.IN_TRANSIT)
                .collect(Collectors.toList());

        Integer trend = calculateTrendPercentage(currentCount, previousInTransit.size());

        // Carrier breakdown - TODO: Orders don't have direct carrier relationship
        List<CarrierBreakdown> carrierBreakdown = new ArrayList<>();

        // Delivery time breakdown
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate threeDaysLater = LocalDate.now().plusDays(3);

        DeliveryTimeBreakdown deliveryTimeBreakdown = DeliveryTimeBreakdown.builder()
                .deliveringSoon((int) currentInTransit.stream()
                        .filter(o -> o.getEstimatedDeliveryDate() != null &&
                                !o.getEstimatedDeliveryDate().isAfter(tomorrow))
                        .count())
                .delivering1to3Days((int) currentInTransit.stream()
                        .filter(o -> o.getEstimatedDeliveryDate() != null &&
                                o.getEstimatedDeliveryDate().isAfter(tomorrow) &&
                                o.getEstimatedDeliveryDate().isBefore(threeDaysLater))
                        .count())
                .deliveringLater((int) currentInTransit.stream()
                        .filter(o -> o.getEstimatedDeliveryDate() != null &&
                                !o.getEstimatedDeliveryDate().isBefore(threeDaysLater))
                        .count())
                .build();

        return InTransitMetric.builder()
                .value(currentCount)
                .trend(trend)
                .label(getPeriodLabel(period))
                .byCarrier(carrierBreakdown)
                .byDeliveryTime(deliveryTimeBreakdown)
                .build();
    }

    private DeliveredMetric calculateDelivered(List<Order> allOrders,
                                               List<Order> current,
                                               List<Order> previous,
                                               String period) {
        List<Order> currentDelivered = current.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.toList());

        int currentCount = currentDelivered.size();

        List<Order> previousDelivered = previous.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.toList());

        Integer trend = calculateTrendPercentage(currentCount, previousDelivered.size());

        // Carrier breakdown - TODO: Orders don't have direct carrier relationship
        List<CarrierBreakdown> carrierBreakdown = new ArrayList<>();

        // On-time calculation (delivered before or on estimated date)
        int onTime = (int) currentDelivered.stream()
                .filter(o -> o.getEstimatedDeliveryDate() != null &&
                        o.getUpdatedAt().toLocalDate().isBefore(o.getEstimatedDeliveryDate().plusDays(1)))
                .count();

        int delayed = currentCount - onTime;
        Integer onTimePercentage = currentCount > 0 ? (onTime * 100 / currentCount) : null;

        OnTimeBreakdown onTimeBreakdown = OnTimeBreakdown.builder()
                .onTime(onTime)
                .delayed(delayed)
                .onTimePercentage(onTimePercentage)
                .build();

        return DeliveredMetric.builder()
                .value(currentCount)
                .trend(trend)
                .label(getPeriodLabel(period))
                .byCarrier(carrierBreakdown)
                .onTimeBreakdown(onTimeBreakdown)
                .build();
    }

    private CostMetric calculateAverageCost(List<Order> current,
                                            List<Order> previous,
                                            String period) {
        // Note: ShipmentOrder doesn't have cost field yet, so using placeholder
        // In production, you'd add a cost field or fetch from a pricing service
        double avgCost = 15.50; // Placeholder
        double previousAvgCost = 16.20; // Placeholder

        Integer trend = calculateTrendPercentage((int)(avgCost * 100), (int)(previousAvgCost * 100));

        return CostMetric.builder()
                .value(avgCost)
                .trend(trend)
                .label(getPeriodLabel(period))
                .currency("USD")
                .byCarrier(new ArrayList<>()) // Placeholder
                .byCostRange(new ArrayList<>()) // Placeholder
                .build();
    }

    private CostMetric calculateTotalCosts(List<Order> current,
                                           List<Order> previous,
                                           String period) {
        // Placeholder calculation
        double totalCost = current.size() * 15.50;
        double previousTotalCost = previous.size() * 16.20;

        Integer trend = calculateTrendPercentage((int)totalCost, (int)previousTotalCost);

        return CostMetric.builder()
                .value(totalCost)
                .trend(trend)
                .label(getPeriodLabel(period))
                .currency("USD")
                .byCarrier(new ArrayList<>())
                .byCostRange(new ArrayList<>())
                .build();
    }

    private TimeMetric calculateAverageDeliveryTime(List<Order> current,
                                                    List<Order> previous,
                                                    String period) {
        // Calculate average delivery time for delivered orders
        List<Order> deliveredOrders = current.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED && o.getCreatedAt() != null)
                .collect(Collectors.toList());

        double avgHours = deliveredOrders.stream()
                .mapToDouble(o -> ChronoUnit.HOURS.between(o.getCreatedAt(), o.getUpdatedAt()))
                .average()
                .orElse(0.0);

        double avgDays = avgHours / 24.0;

        List<Order> previousDeliveredOrders = previous.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED && o.getCreatedAt() != null)
                .collect(Collectors.toList());

        double previousAvgHours = previousDeliveredOrders.stream()
                .mapToDouble(o -> ChronoUnit.HOURS.between(o.getCreatedAt(), o.getUpdatedAt()))
                .average()
                .orElse(0.0);

        Integer trend = calculateTrendPercentage((int)avgHours, (int)previousAvgHours);

        // Time range breakdown
        TimeRangeBreakdown timeRangeBreakdown = TimeRangeBreakdown.builder()
                .lessThan24Hours((int) deliveredOrders.stream()
                        .filter(o -> ChronoUnit.HOURS.between(o.getCreatedAt(), o.getUpdatedAt()) < 24)
                        .count())
                .oneToThreeDays((int) deliveredOrders.stream()
                        .filter(o -> {
                            long hours = ChronoUnit.HOURS.between(o.getCreatedAt(), o.getUpdatedAt());
                            return hours >= 24 && hours < 72;
                        })
                        .count())
                .threeTo7Days((int) deliveredOrders.stream()
                        .filter(o -> {
                            long hours = ChronoUnit.HOURS.between(o.getCreatedAt(), o.getUpdatedAt());
                            return hours >= 72 && hours < 168;
                        })
                        .count())
                .moreThan7Days((int) deliveredOrders.stream()
                        .filter(o -> ChronoUnit.HOURS.between(o.getCreatedAt(), o.getUpdatedAt()) >= 168)
                        .count())
                .build();

        return TimeMetric.builder()
                .value(Math.round(avgDays * 10.0) / 10.0) // Round to 1 decimal
                .unit("days")
                .trend(trend)
                .label(getPeriodLabel(period))
                .byCarrier(new ArrayList<>()) // Could calculate per carrier
                .byTimeRange(timeRangeBreakdown)
                .build();
    }

    private ExceptionsMetric calculateExceptions(List<DeliveryException> allExceptions,
                                                 List<Order> current,
                                                 List<Order> previous,
                                                 LocalDateTime periodStart,
                                                 LocalDateTime previousPeriodStart,
                                                 String period) {
        List<DeliveryException> currentExceptions = allExceptions.stream()
                .filter(e -> e.getReportedAt().isAfter(periodStart))
                .collect(Collectors.toList());

        int currentCount = currentExceptions.size();

        List<DeliveryException> previousExceptions = allExceptions.stream()
                .filter(e -> e.getReportedAt().isAfter(previousPeriodStart) &&
                            e.getReportedAt().isBefore(periodStart))
                .collect(Collectors.toList());

        Integer trend = calculateTrendPercentage(currentCount, previousExceptions.size());

        // By type
        Map<String, Integer> byType = currentExceptions.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getType().name(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        // By status
        Map<String, Integer> byStatus = currentExceptions.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getStatus().name(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        // By severity
        Map<String, Integer> bySeverity = currentExceptions.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getSeverity().name(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        // By carrier - TODO: Orders don't have direct carrier relationship
        List<CarrierBreakdown> byCarrier = new ArrayList<>();

        return ExceptionsMetric.builder()
                .value(currentCount)
                .trend(trend)
                .label(getPeriodLabel(period))
                .byType(byType)
                .byCarrier(byCarrier)
                .byStatus(byStatus)
                .bySeverity(bySeverity)
                .build();
    }

    private TicketsMetric calculateTickets(List<DeliveryException> allExceptions,
                                          LocalDateTime periodStart,
                                          LocalDateTime previousPeriodStart,
                                          String period) {
        // Using delivery exceptions as "tickets" since no separate ticket entity exists
        List<DeliveryException> currentTickets = allExceptions.stream()
                .filter(e -> e.getReportedAt().isAfter(periodStart))
                .collect(Collectors.toList());

        int currentCount = currentTickets.size();

        List<DeliveryException> previousTickets = allExceptions.stream()
                .filter(e -> e.getReportedAt().isAfter(previousPeriodStart) &&
                            e.getReportedAt().isBefore(periodStart))
                .collect(Collectors.toList());

        Integer trend = calculateTrendPercentage(currentCount, previousTickets.size());

        // By status
        Map<String, Integer> byStatus = currentTickets.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getStatus().name(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        // By category (using type as category)
        Map<String, Integer> byCategory = currentTickets.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getType().name(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        // Average resolution time
        double avgResolutionHours = currentTickets.stream()
                .filter(e -> e.getResolvedAt() != null)
                .mapToDouble(e -> ChronoUnit.HOURS.between(e.getReportedAt(), e.getResolvedAt()))
                .average()
                .orElse(0.0);

        return TicketsMetric.builder()
                .value(currentCount)
                .trend(trend)
                .label(getPeriodLabel(period))
                .byStatus(byStatus)
                .byCategory(byCategory)
                .averageResolutionTime(Math.round(avgResolutionHours * 10.0) / 10.0)
                .resolutionTimeUnit("hours")
                .build();
    }

    private WarehousesMetric calculateWarehouses(List<Warehouse> allWarehouses,
                                                 List<Order> allOrders) {
        int count = allWarehouses.size();

        List<WarehouseBreakdown> warehouses = allWarehouses.stream()
                .map(w -> {
                    int orderCount = (int) allOrders.stream()
                            .filter(o -> o.getWarehouse().getId().equals(w.getId()))
                            .count();
                    return WarehouseBreakdown.builder()
                            .warehouseId(w.getId())
                            .warehouseName(w.getName())
                            .count(orderCount)
                            .percentage(allOrders.size() > 0 ? (orderCount * 100 / allOrders.size()) : 0)
                            .build();
                })
                .collect(Collectors.toList());

        return WarehousesMetric.builder()
                .value(count)
                .trend(0) // Warehouses don't change frequently
                .label("total")
                .warehouses(warehouses)
                .build();
    }

    private DriversMetric calculateDrivers(String period) {
        // Placeholder: Using carriers as "drivers" since no driver entity exists
        long activeCount = carrierRepository.findAll().stream()
                .filter(c -> c.isActive())
                .count();

        long totalCount = carrierRepository.count();

        List<CarrierBreakdown> byCarrier = carrierRepository.findAll().stream()
                .map(c -> CarrierBreakdown.builder()
                        .carrierCode(c.getCode())
                        .carrierName(c.getName())
                        .count(c.isActive() ? 1 : 0)
                        .percentage(c.isActive() ? 100 : 0)
                        .build())
                .collect(Collectors.toList());

        return DriversMetric.builder()
                .value((int)activeCount)
                .trend(0) // Placeholder
                .label(getPeriodLabel(period))
                .byCarrier(byCarrier)
                .total((int)totalCount)
                .inactive((int)(totalCount - activeCount))
                .build();
    }

    // ========================================
    // Helper methods
    // ========================================

    private LocalDateTime getPeriodStart(LocalDateTime from, String period) {
        return switch (period.toLowerCase()) {
            case "daily" -> from.toLocalDate().atStartOfDay();
            case "weekly" -> from.toLocalDate().minusDays(7).atStartOfDay();
            case "monthly" -> from.toLocalDate().minusMonths(1).atStartOfDay();
            default -> from.toLocalDate().atStartOfDay();
        };
    }

    private String getPeriodLabel(String period) {
        return switch (period.toLowerCase()) {
            case "daily" -> "vs yesterday";
            case "weekly" -> "vs last week";
            case "monthly" -> "vs last month";
            default -> "vs last period";
        };
    }

    private List<Order> filterOrdersByPeriod(List<Order> orders,
                                                     LocalDateTime start,
                                                     LocalDateTime end) {
        OffsetDateTime startOffset = start.atOffset(ZoneOffset.UTC);
        OffsetDateTime endOffset = end.atOffset(ZoneOffset.UTC);

        return orders.stream()
                .filter(o -> o.getCreatedAt() != null)
                .filter(o -> o.getCreatedAt().isAfter(startOffset) && o.getCreatedAt().isBefore(endOffset))
                .collect(Collectors.toList());
    }

    private Integer calculateTrendPercentage(int current, int previous) {
        if (previous == 0) {
            return current > 0 ? 100 : 0;
        }
        return ((current - previous) * 100) / previous;
    }

    private List<CarrierBreakdown> calculateCarrierBreakdown(List<Order> orders) {
        // TODO: Orders don't have direct carrier relationship
        // This would need to be calculated through Shipment entity
        return new ArrayList<>();
    }

    private List<WarehouseBreakdown> calculateWarehouseBreakdown(List<Order> orders) {
        int total = orders.size();
        return orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getWarehouse().getId(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> {
                    Order sampleOrder = orders.stream()
                            .filter(o -> o.getWarehouse().getId().equals(entry.getKey()))
                            .findFirst()
                            .orElse(null);
                    return WarehouseBreakdown.builder()
                            .warehouseId(entry.getKey())
                            .warehouseName(sampleOrder != null ? sampleOrder.getWarehouse().getName() : "Unknown")
                            .count(entry.getValue().intValue())
                            .percentage(total > 0 ? (int)(entry.getValue() * 100 / total) : 0)
                            .build();
                })
                .collect(Collectors.toList());
    }
}

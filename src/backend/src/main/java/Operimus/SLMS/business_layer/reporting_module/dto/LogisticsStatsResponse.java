package Operimus.SLMS.business_layer.reporting_module.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for comprehensive logistics dashboard statistics
 * Each metric includes current value, trend, and detailed breakdowns
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsStatsResponse {

    /**
     * The time period used for trend calculation: "daily", "weekly", or "monthly"
     */
    private String period;

    /**
     * Total shipments metric with status and carrier breakdowns
     */
    private TotalShipmentsMetric totalShipments;

    /**
     * Total orders created today/this period
     */
    private OrdersMetric ordersToday;

    /**
     * Pending pickups metric
     */
    private PendingPickupsMetric pendingPickups;

    /**
     * Orders currently in transit
     */
    private InTransitMetric inTransitNow;

    /**
     * Delivered orders metric
     */
    private DeliveredMetric deliveredToday;

    /**
     * Average cost per shipment
     */
    private CostMetric averageCostPerShipment;

    /**
     * Total costs of all shipments
     */
    private CostMetric totalShipmentCosts;

    /**
     * Average delivery time metric
     */
    private TimeMetric averageDeliveryTime;

    /**
     * Total delivery exceptions/errors
     */
    private ExceptionsMetric totalExceptions;

    /**
     * Total support tickets
     */
    private TicketsMetric totalTickets;

    /**
     * Total number of warehouses
     */
    private WarehousesMetric totalWarehouses;

    /**
     * Number of active drivers/carriers
     */
    private DriversMetric activeDrivers;

    // ============================================
    // Nested DTOs for each metric
    // ============================================

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TotalShipmentsMetric {
        private int value;
        private Integer trend; // % change vs previous period
        private String label; // e.g., "vs last week"

        // Breakdown by status
        private StatusBreakdown byStatus;

        // Breakdown by carrier
        private List<CarrierBreakdown> byCarrier;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrdersMetric {
        private int value;
        private Integer trend;
        private String label;

        // Breakdown by warehouse
        private List<WarehouseBreakdown> byWarehouse;

        // Breakdown by status
        private Map<String, Integer> byStatus;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingPickupsMetric {
        private int value;
        private Integer trend;
        private String label;
        private String alert; // e.g., "5 Overdue"
        private boolean hasWarning;

        // Breakdown by warehouse
        private List<WarehouseBreakdown> byWarehouse;

        // Breakdown by priority/age
        private OverdueBreakdown overdueBreakdown;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InTransitMetric {
        private int value;
        private Integer trend;
        private String label;

        // Breakdown by carrier
        private List<CarrierBreakdown> byCarrier;

        // Breakdown by estimated delivery
        private DeliveryTimeBreakdown byDeliveryTime;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveredMetric {
        private int value;
        private Integer trend;
        private String label;

        // Breakdown by carrier
        private List<CarrierBreakdown> byCarrier;

        // On-time vs delayed
        private OnTimeBreakdown onTimeBreakdown;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostMetric {
        private double value; // Cost amount
        private Integer trend; // % change
        private String label;
        private String currency; // e.g., "USD"

        // Breakdown by carrier
        private List<CarrierCostBreakdown> byCarrier;

        // Breakdown by cost range
        private List<CostRangeBreakdown> byCostRange;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeMetric {
        private double value; // Time in hours or days
        private String unit; // "hours" or "days"
        private Integer trend; // % change
        private String label;

        // Breakdown by carrier
        private List<CarrierTimeBreakdown> byCarrier;

        // Breakdown by time ranges
        private TimeRangeBreakdown byTimeRange;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExceptionsMetric {
        private int value;
        private Integer trend;
        private String label;

        // Breakdown by exception type
        private Map<String, Integer> byType; // DAMAGED, LOST, DELAYED, etc.

        // Breakdown by carrier
        private List<CarrierBreakdown> byCarrier;

        // Breakdown by status
        private Map<String, Integer> byStatus; // OPEN, RESOLVED, etc.

        // Breakdown by severity
        private Map<String, Integer> bySeverity; // LOW, MEDIUM, HIGH, CRITICAL
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketsMetric {
        private int value;
        private Integer trend;
        private String label;

        // Breakdown by status
        private Map<String, Integer> byStatus;

        // Breakdown by type/category
        private Map<String, Integer> byCategory;

        // Average resolution time
        private Double averageResolutionTime;
        private String resolutionTimeUnit;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehousesMetric {
        private int value;
        private Integer trend;
        private String label;

        // List of warehouses with their order counts
        private List<WarehouseBreakdown> warehouses;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriversMetric {
        private int value;
        private Integer trend;
        private String label;

        // Breakdown by carrier
        private List<CarrierBreakdown> byCarrier;

        // Active vs total
        private int total;
        private int inactive;
    }

    // ============================================
    // Breakdown helper DTOs
    // ============================================

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusBreakdown {
        private int created;
        private int readyForPickup;
        private int collected;
        private int inTransit;
        private int delivered;
        private int cancelled;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CarrierBreakdown {
        private String carrierCode;
        private String carrierName;
        private int count;
        private Integer percentage; // % of total
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CarrierCostBreakdown {
        private String carrierCode;
        private String carrierName;
        private double totalCost;
        private double averageCost;
        private int shipmentCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CarrierTimeBreakdown {
        private String carrierCode;
        private String carrierName;
        private double averageTime;
        private String unit;
        private int shipmentCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseBreakdown {
        private Long warehouseId;
        private String warehouseName;
        private int count;
        private Integer percentage;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverdueBreakdown {
        private int overdue;
        private int dueSoon; // within 24 hours
        private int onTime;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryTimeBreakdown {
        private int deliveringSoon; // within 24 hours
        private int delivering1to3Days;
        private int deliveringLater; // 3+ days
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OnTimeBreakdown {
        private int onTime;
        private int delayed;
        private Integer onTimePercentage;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostRangeBreakdown {
        private String range; // e.g., "$0-$10", "$10-$25"
        private int count;
        private double totalCost;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeRangeBreakdown {
        private int lessThan24Hours;
        private int oneToThreeDays;
        private int threeTo7Days;
        private int moreThan7Days;
    }
}

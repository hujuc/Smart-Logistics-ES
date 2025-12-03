package Operimus.SLMS.business_layer.reporting_module.service;

import Operimus.SLMS.business_layer.reporting_module.dto.LogisticsStatsResponse;
import Operimus.SLMS.business_layer.reporting_module.dto.LogisticsStatsResponse.*;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportGenerationService {

    private final ReportStatsService reportStatsService;

    private static final DeviceRgb HEADER_BG_COLOR = new DeviceRgb(41, 98, 255); // Blue
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(229, 231, 235);

    /**
     * Generate a comprehensive PDF report with all logistics KPIs
     */
    public byte[] generateKPIReport(String period) {
        log.info("Generating logistics KPI report for period: {}", period);

        try {
            // Fetch statistics from backend
            LogisticsStatsResponse stats = reportStatsService.calculateStats(period);

            // Create PDF in memory
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Add document header
            addDocumentHeader(document, period);

            // Add KPI sections
            addTotalShipmentsSection(document, stats.getTotalShipments());
            addOrdersTodaySection(document, stats.getOrdersToday());
            addPendingPickupsSection(document, stats.getPendingPickups());
            addInTransitSection(document, stats.getInTransitNow());
            addDeliveredTodaySection(document, stats.getDeliveredToday());
            addCostMetricsSection(document, stats.getAverageCostPerShipment(), stats.getTotalShipmentCosts());
            addDeliveryTimeSection(document, stats.getAverageDeliveryTime());
            addExceptionsSection(document, stats.getTotalExceptions());
            addTicketsSection(document, stats.getTotalTickets());
            addWarehousesSection(document, stats.getTotalWarehouses());
            addActiveDriversSection(document, stats.getActiveDrivers());

            // Close document
            document.close();

            log.info("Successfully generated logistics KPI report");
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating KPI report", e);
            throw new RuntimeException("Failed to generate KPI report: " + e.getMessage(), e);
        }
    }

    private void addDocumentHeader(Document document, String period) {
        // Title
        Paragraph title = new Paragraph("Logistics Operations Dashboard Report")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(title);

        // Subtitle with period and date
        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Paragraph subtitle = new Paragraph(String.format("Period: %s | Generated: %s",
                period.substring(0, 1).toUpperCase() + period.substring(1), formattedDate))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(subtitle);
    }

    private void addSectionTitle(Document document, String title) {
        Paragraph sectionTitle = new Paragraph(title)
                .setFontSize(16)
                .setBold()
                .setMarginTop(15)
                .setMarginBottom(10);
        document.add(sectionTitle);
    }

    private void addTotalShipmentsSection(Document document, TotalShipmentsMetric metric) {
        addSectionTitle(document, "Total Shipments");

        // Main metric
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));

        addHeaderCell(table, "Value");
        addHeaderCell(table, "Trend");
        addHeaderCell(table, "Period");

        table.addCell(createCell(metric.getValue()));
        table.addCell(createCell(formatTrendValue(metric.getTrend())));
        table.addCell(createCell(metric.getLabel() != null ? metric.getLabel() : ""));

        document.add(table);

        // Status breakdown
        if (metric.getByStatus() != null) {
            addSubsectionTitle(document, "By Status");
            Table statusTable = createTwoColumnTable();

            StatusBreakdown status = metric.getByStatus();
            statusTable.addCell(createCell("Created")).addCell(createCell(status.getCreated()));
            statusTable.addCell(createCell("Ready for Pickup")).addCell(createCell(status.getReadyForPickup()));
            statusTable.addCell(createCell("Collected")).addCell(createCell(status.getCollected()));
            statusTable.addCell(createCell("In Transit")).addCell(createCell(status.getInTransit()));
            statusTable.addCell(createCell("Delivered")).addCell(createCell(status.getDelivered()));
            statusTable.addCell(createCell("Cancelled")).addCell(createCell(status.getCancelled()));

            document.add(statusTable);
        }

        // Carrier breakdown
        if (metric.getByCarrier() != null && !metric.getByCarrier().isEmpty()) {
            addCarrierBreakdown(document, metric.getByCarrier());
        }
    }

    private void addOrdersTodaySection(Document document, OrdersMetric metric) {
        addSectionTitle(document, "Orders Today");

        // Main metric
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));

        addHeaderCell(table, "Value");
        addHeaderCell(table, "Trend");
        addHeaderCell(table, "Period");

        table.addCell(createCell(metric.getValue()));
        table.addCell(createCell(formatTrendValue(metric.getTrend())));
        table.addCell(createCell(metric.getLabel() != null ? metric.getLabel() : ""));

        document.add(table);

        // Warehouse breakdown
        if (metric.getByWarehouse() != null && !metric.getByWarehouse().isEmpty()) {
            addWarehouseBreakdown(document, metric.getByWarehouse());
        }

        // Status breakdown
        if (metric.getByStatus() != null) {
            addSubsectionTitle(document, "By Status");
            addStatusBreakdownMap(document, metric.getByStatus());
        }
    }

    private void addPendingPickupsSection(Document document, PendingPickupsMetric metric) {
        addSectionTitle(document, "Pending Pickups");

        // Main metric with alert
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));

        addHeaderCell(table, "Value");
        addHeaderCell(table, "Trend");
        addHeaderCell(table, "Period");
        addHeaderCell(table, "Alert");

        table.addCell(createCell(metric.getValue()));
        table.addCell(createCell(formatTrendValue(metric.getTrend())));
        table.addCell(createCell(metric.getLabel() != null ? metric.getLabel() : ""));
        table.addCell(createCell(metric.getAlert() != null ? metric.getAlert() : "None"));

        document.add(table);

        // Overdue breakdown
        if (metric.getOverdueBreakdown() != null) {
            addSubsectionTitle(document, "Urgency Breakdown");
            Table overdueTable = createTwoColumnTable();

            OverdueBreakdown breakdown = metric.getOverdueBreakdown();
            overdueTable.addCell(createCell("Overdue")).addCell(createCell(breakdown.getOverdue()));
            overdueTable.addCell(createCell("Due Soon")).addCell(createCell(breakdown.getDueSoon()));
            overdueTable.addCell(createCell("On Time")).addCell(createCell(breakdown.getOnTime()));

            document.add(overdueTable);
        }

        // Warehouse breakdown
        if (metric.getByWarehouse() != null && !metric.getByWarehouse().isEmpty()) {
            addWarehouseBreakdown(document, metric.getByWarehouse());
        }
    }

    private void addInTransitSection(Document document, InTransitMetric metric) {
        addSectionTitle(document, "In Transit Now");

        // Main metric
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));

        addHeaderCell(table, "Value");
        addHeaderCell(table, "Trend");
        addHeaderCell(table, "Period");

        table.addCell(createCell(metric.getValue()));
        table.addCell(createCell(formatTrendValue(metric.getTrend())));
        table.addCell(createCell(metric.getLabel() != null ? metric.getLabel() : ""));

        document.add(table);

        // Carrier breakdown
        if (metric.getByCarrier() != null && !metric.getByCarrier().isEmpty()) {
            addCarrierBreakdown(document, metric.getByCarrier());
        }

        // Delivery time breakdown
        if (metric.getByDeliveryTime() != null) {
            addSubsectionTitle(document, "Delivery Schedule");
            Table timeTable = createTwoColumnTable();

            DeliveryTimeBreakdown breakdown = metric.getByDeliveryTime();
            timeTable.addCell(createCell("Within 24h")).addCell(createCell(breakdown.getDeliveringSoon()));
            timeTable.addCell(createCell("1-3 Days")).addCell(createCell(breakdown.getDelivering1to3Days()));
            timeTable.addCell(createCell("3+ Days")).addCell(createCell(breakdown.getDeliveringLater()));

            document.add(timeTable);
        }
    }

    private void addDeliveredTodaySection(Document document, DeliveredMetric metric) {
        addSectionTitle(document, "Delivered Today");

        // Main metric
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));

        addHeaderCell(table, "Value");
        addHeaderCell(table, "Trend");
        addHeaderCell(table, "Period");

        table.addCell(createCell(metric.getValue()));
        table.addCell(createCell(formatTrendValue(metric.getTrend())));
        table.addCell(createCell(metric.getLabel() != null ? metric.getLabel() : ""));

        document.add(table);

        // On-time breakdown
        if (metric.getOnTimeBreakdown() != null) {
            addSubsectionTitle(document, "Performance");
            Table onTimeTable = createTwoColumnTable();

            OnTimeBreakdown breakdown = metric.getOnTimeBreakdown();
            onTimeTable.addCell(createCell(String.format("On Time (%d%%)", breakdown.getOnTimePercentage())))
                    .addCell(createCell(breakdown.getOnTime()));
            onTimeTable.addCell(createCell("Delayed")).addCell(createCell(breakdown.getDelayed()));

            document.add(onTimeTable);
        }

        // Carrier breakdown
        if (metric.getByCarrier() != null && !metric.getByCarrier().isEmpty()) {
            addCarrierBreakdown(document, metric.getByCarrier());
        }
    }

    private void addCostMetricsSection(Document document, CostMetric avgCost, CostMetric totalCost) {
        addSectionTitle(document, "Cost Metrics");

        // Cost overview
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));

        addHeaderCell(table, "Metric");
        addHeaderCell(table, "Value");
        addHeaderCell(table, "Trend");
        addHeaderCell(table, "Period");

        table.addCell(createCell("Average Cost/Shipment"));
        table.addCell(createCell(String.format("$%.2f", avgCost.getValue())));
        table.addCell(createCell(formatTrendValue(avgCost.getTrend())));
        table.addCell(createCell(avgCost.getLabel() != null ? avgCost.getLabel() : ""));

        table.addCell(createCell("Total Costs"));
        table.addCell(createCell(String.format("$%.2f", totalCost.getValue())));
        table.addCell(createCell(formatTrendValue(totalCost.getTrend())));
        table.addCell(createCell(totalCost.getLabel() != null ? totalCost.getLabel() : ""));

        document.add(table);
    }

    private void addDeliveryTimeSection(Document document, TimeMetric metric) {
        addSectionTitle(document, "Average Delivery Time");

        // Main metric
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));

        addHeaderCell(table, "Value");
        addHeaderCell(table, "Trend");
        addHeaderCell(table, "Period");

        table.addCell(createCell(String.format("%.1f %s", metric.getValue(), metric.getUnit())));
        table.addCell(createCell(formatTrendValue(metric.getTrend())));
        table.addCell(createCell(metric.getLabel() != null ? metric.getLabel() : ""));

        document.add(table);

        // Time distribution
        if (metric.getByTimeRange() != null) {
            addSubsectionTitle(document, "Time Distribution");
            Table timeTable = createTwoColumnTable();

            TimeRangeBreakdown breakdown = metric.getByTimeRange();
            timeTable.addCell(createCell("< 24 hours")).addCell(createCell(breakdown.getLessThan24Hours()));
            timeTable.addCell(createCell("1-3 Days")).addCell(createCell(breakdown.getOneToThreeDays()));
            timeTable.addCell(createCell("3-7 Days")).addCell(createCell(breakdown.getThreeTo7Days()));
            timeTable.addCell(createCell("> 7 Days")).addCell(createCell(breakdown.getMoreThan7Days()));

            document.add(timeTable);
        }
    }

    private void addExceptionsSection(Document document, ExceptionsMetric metric) {
        addSectionTitle(document, "Total Exceptions");

        // Main metric
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));

        addHeaderCell(table, "Value");
        addHeaderCell(table, "Trend");
        addHeaderCell(table, "Period");

        table.addCell(createCell(metric.getValue()));
        table.addCell(createCell(formatTrendValue(metric.getTrend())));
        table.addCell(createCell(metric.getLabel() != null ? metric.getLabel() : ""));

        document.add(table);

        // By type
        if (metric.getByType() != null && !metric.getByType().isEmpty()) {
            addSubsectionTitle(document, "By Type");
            addMapBreakdown(document, metric.getByType());
        }

        // By severity
        if (metric.getBySeverity() != null && !metric.getBySeverity().isEmpty()) {
            addSubsectionTitle(document, "By Severity");
            addMapBreakdown(document, metric.getBySeverity());
        }
    }

    private void addTicketsSection(Document document, TicketsMetric metric) {
        addSectionTitle(document, "Total Tickets");

        // Main metric
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));

        addHeaderCell(table, "Value");
        addHeaderCell(table, "Trend");
        addHeaderCell(table, "Period");

        table.addCell(createCell(metric.getValue()));
        table.addCell(createCell(formatTrendValue(metric.getTrend())));
        table.addCell(createCell(metric.getLabel() != null ? metric.getLabel() : ""));

        document.add(table);

        // By status
        if (metric.getByStatus() != null && !metric.getByStatus().isEmpty()) {
            addSubsectionTitle(document, "By Status");
            addMapBreakdown(document, metric.getByStatus());
        }

        // Average resolution time
        if (metric.getAverageResolutionTime() > 0) {
            addSubsectionTitle(document, "Resolution Time");
            Table resTable = createTwoColumnTable();
            resTable.addCell(createCell("Average Resolution"))
                    .addCell(createCell(String.format("%.1f %s",
                            metric.getAverageResolutionTime(),
                            metric.getResolutionTimeUnit())));
            document.add(resTable);
        }
    }

    private void addWarehousesSection(Document document, WarehousesMetric metric) {
        addSectionTitle(document, "Warehouses");

        // Warehouse list
        if (metric.getWarehouses() != null && !metric.getWarehouses().isEmpty()) {
            Table table = createTwoColumnTable();
            addHeaderCell(table, "Warehouse");
            addHeaderCell(table, "Order Count");

            for (WarehouseBreakdown wh : metric.getWarehouses()) {
                table.addCell(createCell(wh.getWarehouseName()));
                table.addCell(createCell(wh.getCount()));
            }

            document.add(table);
        }
    }

    private void addActiveDriversSection(Document document, DriversMetric metric) {
        addSectionTitle(document, "Active Carriers");

        // Overview
        Table table = createTwoColumnTable();
        addHeaderCell(table, "Metric");
        addHeaderCell(table, "Value");

        table.addCell(createCell("Total Carriers")).addCell(createCell(metric.getTotal()));
        table.addCell(createCell("Active")).addCell(createCell(metric.getValue()));
        table.addCell(createCell("Inactive")).addCell(createCell(metric.getInactive()));

        document.add(table);

        // Carrier breakdown
        if (metric.getByCarrier() != null && !metric.getByCarrier().isEmpty()) {
            addSubsectionTitle(document, "Carriers");
            Table carrierTable = createTwoColumnTable();
            addHeaderCell(carrierTable, "Carrier");
            addHeaderCell(carrierTable, "Status");

            for (CarrierBreakdown carrier : metric.getByCarrier()) {
                carrierTable.addCell(createCell(carrier.getCarrierName()));
                carrierTable.addCell(createCell(carrier.getCount() > 0 ? "Active" : "Inactive"));
            }

            document.add(carrierTable);
        }
    }

    // Helper methods
    private void addSubsectionTitle(Document document, String title) {
        Paragraph subsection = new Paragraph(title)
                .setFontSize(12)
                .setBold()
                .setMarginTop(10)
                .setMarginBottom(5);
        document.add(subsection);
    }

    private Table createTwoColumnTable() {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));
        return table;
    }

    private void addHeaderCell(Table table, String text) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(HEADER_BG_COLOR)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8);
        table.addHeaderCell(cell);
    }

    private Cell createCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setPadding(6)
                .setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER_COLOR, 1));
    }

    private Cell createCell(int value) {
        return createCell(String.valueOf(value));
    }

    private void addCarrierBreakdown(Document document, List<CarrierBreakdown> carriers) {
        addSubsectionTitle(document, "By Carrier");
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));

        addHeaderCell(table, "Carrier");
        addHeaderCell(table, "Count");
        addHeaderCell(table, "Percentage");

        for (CarrierBreakdown carrier : carriers) {
            table.addCell(createCell(carrier.getCarrierName()));
            table.addCell(createCell(carrier.getCount()));
            table.addCell(createCell(String.format("%.1f%%", (double) carrier.getPercentage())));
        }

        document.add(table);
    }

    private void addWarehouseBreakdown(Document document, List<WarehouseBreakdown> warehouses) {
        addSubsectionTitle(document, "By Warehouse");
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));

        addHeaderCell(table, "Warehouse");
        addHeaderCell(table, "Count");
        addHeaderCell(table, "Percentage");

        for (WarehouseBreakdown wh : warehouses) {
            table.addCell(createCell(wh.getWarehouseName()));
            table.addCell(createCell(wh.getCount()));
            table.addCell(createCell(String.format("%.1f%%", (double) wh.getPercentage())));
        }

        document.add(table);
    }

    private void addStatusBreakdownMap(Document document, Map<String, Integer> statusMap) {
        Table table = createTwoColumnTable();
        addHeaderCell(table, "Status");
        addHeaderCell(table, "Count");

        for (Map.Entry<String, Integer> entry : statusMap.entrySet()) {
            table.addCell(createCell(capitalize(entry.getKey())));
            table.addCell(createCell(entry.getValue()));
        }

        document.add(table);
    }

    private void addMapBreakdown(Document document, Map<String, Integer> map) {
        Table table = createTwoColumnTable();

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            table.addCell(createCell(capitalize(entry.getKey())));
            table.addCell(createCell(entry.getValue()));
        }

        document.add(table);
    }

    private String formatTrendValue(Integer trendValue) {
        if (trendValue == null) {
            return "N/A";
        }
        String sign = trendValue >= 0 ? "+" : "";
        return String.format("%s%d%%", sign, trendValue);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase().replace("_", " ");
    }
}

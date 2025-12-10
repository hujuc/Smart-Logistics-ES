package Operimus.SLMS.business_layer.reporting_module.service;

import Operimus.SLMS.business_layer.reporting_module.dto.LogisticsStatsResponse;
import Operimus.SLMS.business_layer.reporting_module.dto.LogisticsStatsResponse.*;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import java.awt.*;
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

            // Add performance charts section
            addPerformanceChartsSection(document, stats);

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

    // ===========================
    // Performance Charts Section
    // ===========================

    private void addPerformanceChartsSection(Document document, LogisticsStatsResponse stats) {
        try {
            String period = stats.getPeriod();
            String timeFrame = getTimeFrameLabel(period);
            String timeFrameLower = getTimeFrameLabel(period).toLowerCase();

            // Check if there's any data to display
            boolean hasAnyChartData = hasStatusData(stats.getTotalShipments().getByStatus()) ||
                    (stats.getTotalShipments().getByCarrier() != null && !stats.getTotalShipments().getByCarrier().isEmpty()) ||
                    hasTimeRangeData(stats.getAverageDeliveryTime().getByTimeRange()) ||
                    (stats.getDeliveredToday().getOnTimeBreakdown() != null &&
                     (stats.getDeliveredToday().getOnTimeBreakdown().getOnTime() > 0 ||
                      stats.getDeliveredToday().getOnTimeBreakdown().getDelayed() > 0)) ||
                    (stats.getOrdersToday().getByWarehouse() != null && !stats.getOrdersToday().getByWarehouse().isEmpty()) ||
                    hasExceptionTypeData(stats.getTotalExceptions().getByType()) ||
                    hasDeliveryTimeData(stats.getInTransitNow().getByDeliveryTime()) ||
                    hasOverdueData(stats.getPendingPickups().getOverdueBreakdown());

            // Only add the charts section if there's data to display
            if (!hasAnyChartData) {
                log.info("No chart data available, skipping Performance Charts section");
                return;
            }

            // Add page break and section title
            document.add(new com.itextpdf.layout.element.AreaBreak(com.itextpdf.layout.properties.AreaBreakType.NEXT_PAGE));

            Paragraph chartsTitle = new Paragraph("Performance Charts")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(chartsTitle);

            // 1. Shipment Status Distribution (Pie Chart)
            if (hasStatusData(stats.getTotalShipments().getByStatus())) {
                byte[] chartImage = createShipmentStatusPieChart(stats.getTotalShipments().getByStatus());
                addChartToDocument(document, chartImage, "Shipment Status Distribution",
                    "Source: Total shipments aggregated by status (" + timeFrameLower + ")");
            }

            // 2. Carrier Performance Comparison (Bar Chart)
            if (stats.getTotalShipments().getByCarrier() != null && !stats.getTotalShipments().getByCarrier().isEmpty()) {
                byte[] chartImage = createCarrierPerformanceBarChart(stats.getTotalShipments().getByCarrier());
                addChartToDocument(document, chartImage, "Carrier Performance Comparison",
                    "Source: Shipments distributed across carriers (" + timeFrameLower + ")");
            }

            // 3. Delivery Time Distribution (Bar Chart)
            if (hasTimeRangeData(stats.getAverageDeliveryTime().getByTimeRange())) {
                byte[] chartImage = createDeliveryTimeBarChart(stats.getAverageDeliveryTime().getByTimeRange());
                addChartToDocument(document, chartImage, "Delivery Time Distribution",
                    "Source: Delivered orders categorized by delivery time ranges (" + timeFrameLower + ")");
            }

            // 4. On-Time Delivery Performance (Pie Chart)
            if (stats.getDeliveredToday().getOnTimeBreakdown() != null &&
                (stats.getDeliveredToday().getOnTimeBreakdown().getOnTime() > 0 ||
                 stats.getDeliveredToday().getOnTimeBreakdown().getDelayed() > 0)) {
                byte[] chartImage = createOnTimeDeliveryPieChart(stats.getDeliveredToday().getOnTimeBreakdown());
                addChartToDocument(document, chartImage, "On-Time Delivery Performance",
                    "Source: Deliveries completed, classified by on-time status (" + timeFrameLower + ")");
            }

            // 5. Warehouse Orders Distribution (Bar Chart)
            if (stats.getOrdersToday().getByWarehouse() != null && !stats.getOrdersToday().getByWarehouse().isEmpty()) {
                byte[] chartImage = createWarehouseOrdersBarChart(stats.getOrdersToday().getByWarehouse());
                addChartToDocument(document, chartImage, "Warehouse Orders Distribution",
                    "Source: Orders grouped by warehouse location (" + timeFrameLower + ")");
            }

            // 6. Exception Analysis (Bar Chart)
            if (hasExceptionTypeData(stats.getTotalExceptions().getByType())) {
                byte[] chartImage = createExceptionAnalysisBarChart(stats.getTotalExceptions().getByType());
                addChartToDocument(document, chartImage, "Exception Analysis by Type",
                    "Source: Shipping exceptions categorized by exception type (" + timeFrameLower + ")");
            }

            // 7. In Transit Delivery Schedule (Bar Chart)
            if (hasDeliveryTimeData(stats.getInTransitNow().getByDeliveryTime())) {
                byte[] chartImage = createInTransitScheduleBarChart(stats.getInTransitNow().getByDeliveryTime());
                addChartToDocument(document, chartImage, "In Transit Delivery Schedule",
                    "Source: Shipments currently in transit, grouped by expected delivery time");
            }

            // 8. Pending Pickups Urgency (Pie Chart)
            if (hasOverdueData(stats.getPendingPickups().getOverdueBreakdown())) {
                byte[] chartImage = createPendingPickupsUrgencyPieChart(stats.getPendingPickups().getOverdueBreakdown());
                addChartToDocument(document, chartImage, "Pending Pickups Urgency",
                    "Source: Shipments awaiting pickup, classified by urgency level");
            }

        } catch (Exception e) {
            log.error("Error adding performance charts to report", e);
            document.add(new Paragraph("Error generating performance charts: " + e.getMessage())
                    .setFontColor(ColorConstants.RED));
        }
    }

    /**
     * Get human-readable time frame label from period
     */
    private String getTimeFrameLabel(String period) {
        return switch (period.toLowerCase()) {
            case "daily" -> "Today";
            case "weekly" -> "This Week";
            case "monthly" -> "This Month";
            default -> "Current Period";
        };
    }

    private void addChartToDocument(Document document, byte[] chartImage, String title, String source) throws Exception {
        // Add chart title
        Paragraph chartTitle = new Paragraph(title)
                .setFontSize(14)
                .setBold()
                .setMarginTop(15)
                .setMarginBottom(5);
        document.add(chartTitle);

        // Add source description
        Paragraph chartSource = new Paragraph(source)
                .setFontSize(9)
                .setItalic()
                .setFontColor(new DeviceRgb(107, 114, 128))
                .setMarginBottom(10);
        document.add(chartSource);

        // Add chart image
        Image img = new Image(ImageDataFactory.create(chartImage));
        img.setWidth(500);
        img.setAutoScale(true);
        img.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
        document.add(img);
    }

    // Chart generation methods
    private byte[] createShipmentStatusPieChart(StatusBreakdown status) throws Exception {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        if (status.getCreated() > 0) dataset.setValue("Created", status.getCreated());
        if (status.getReadyForPickup() > 0) dataset.setValue("Ready for Pickup", status.getReadyForPickup());
        if (status.getCollected() > 0) dataset.setValue("Collected", status.getCollected());
        if (status.getInTransit() > 0) dataset.setValue("In Transit", status.getInTransit());
        if (status.getDelivered() > 0) dataset.setValue("Delivered", status.getDelivered());
        if (status.getCancelled() > 0) dataset.setValue("Cancelled", status.getCancelled());

        JFreeChart chart = ChartFactory.createPieChart(
                null,
                dataset,
                true,
                true,
                false
        );

        customizePieChart(chart, new Color(41, 98, 255), new Color(0, 191, 165), new Color(0, 200, 83),
                new Color(255, 179, 0), new Color(2, 136, 209), new Color(211, 47, 47));

        return chartToByteArray(chart, 600, 400);
    }

    private byte[] createCarrierPerformanceBarChart(List<CarrierBreakdown> carriers) throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (CarrierBreakdown carrier : carriers) {
            dataset.addValue(carrier.getCount(), "Deliveries", carrier.getCarrierName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                null,
                "Carrier",
                "Shipments",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeBarChart(chart, new Color(41, 98, 255));

        return chartToByteArray(chart, 600, 400);
    }

    private byte[] createDeliveryTimeBarChart(TimeRangeBreakdown timeRange) throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(timeRange.getLessThan24Hours(), "Orders", "< 24h");
        dataset.addValue(timeRange.getOneToThreeDays(), "Orders", "1-3 Days");
        dataset.addValue(timeRange.getThreeTo7Days(), "Orders", "3-7 Days");
        dataset.addValue(timeRange.getMoreThan7Days(), "Orders", "> 7 Days");

        JFreeChart chart = ChartFactory.createBarChart(
                null,
                "Delivery Time Range",
                "Order Count",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeBarChart(chart, new Color(0, 191, 165));

        return chartToByteArray(chart, 600, 400);
    }

    private byte[] createOnTimeDeliveryPieChart(OnTimeBreakdown breakdown) throws Exception {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        if (breakdown.getOnTime() > 0) dataset.setValue("On Time", breakdown.getOnTime());
        if (breakdown.getDelayed() > 0) dataset.setValue("Delayed", breakdown.getDelayed());

        JFreeChart chart = ChartFactory.createPieChart(
                null,
                dataset,
                true,
                true,
                false
        );

        customizePieChart(chart, new Color(0, 200, 83), new Color(211, 47, 47));

        return chartToByteArray(chart, 600, 400);
    }

    private byte[] createWarehouseOrdersBarChart(List<WarehouseBreakdown> warehouses) throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (WarehouseBreakdown wh : warehouses) {
            dataset.addValue(wh.getCount(), "Orders", wh.getWarehouseName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                null,
                "Warehouse",
                "Orders",
                dataset,
                PlotOrientation.HORIZONTAL,
                true,
                true,
                false
        );

        customizeBarChart(chart, new Color(0, 137, 123));

        return chartToByteArray(chart, 600, 400);
    }

    private byte[] createExceptionAnalysisBarChart(Map<String, Integer> exceptionTypes) throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, Integer> entry : exceptionTypes.entrySet()) {
            if (entry.getValue() > 0) {
                dataset.addValue(entry.getValue(), "Exceptions", capitalize(entry.getKey()));
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                null,
                "Exception Type",
                "Count",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeBarChart(chart, new Color(211, 47, 47));

        return chartToByteArray(chart, 600, 400);
    }

    private byte[] createInTransitScheduleBarChart(DeliveryTimeBreakdown breakdown) throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(breakdown.getDeliveringSoon(), "Shipments", "Within 24h");
        dataset.addValue(breakdown.getDelivering1to3Days(), "Shipments", "1-3 Days");
        dataset.addValue(breakdown.getDeliveringLater(), "Shipments", "3+ Days");

        JFreeChart chart = ChartFactory.createBarChart(
                null,
                "Delivery Schedule",
                "Shipment Count",
                dataset,
                PlotOrientation.HORIZONTAL,
                true,
                true,
                false
        );

        customizeBarChart(chart, new Color(245, 124, 0));

        return chartToByteArray(chart, 600, 400);
    }

    private byte[] createPendingPickupsUrgencyPieChart(OverdueBreakdown breakdown) throws Exception {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        if (breakdown.getOverdue() > 0) dataset.setValue("Overdue", breakdown.getOverdue());
        if (breakdown.getDueSoon() > 0) dataset.setValue("Due Soon", breakdown.getDueSoon());
        if (breakdown.getOnTime() > 0) dataset.setValue("On Time", breakdown.getOnTime());

        JFreeChart chart = ChartFactory.createPieChart(
                null,
                dataset,
                true,
                true,
                false
        );

        customizePieChart(chart, new Color(211, 47, 47), new Color(255, 179, 0), new Color(0, 200, 83));

        return chartToByteArray(chart, 600, 400);
    }

    // Helper methods for chart customization
    private void customizePieChart(JFreeChart chart, Color... colors) {
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);

        int colorIndex = 0;
        for (Object key : plot.getDataset().getKeys()) {
            if (colorIndex < colors.length) {
                plot.setSectionPaint((Comparable) key, colors[colorIndex]);
                colorIndex++;
            }
        }
    }

    private void customizeBarChart(JFreeChart chart, Color barColor) {
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.getRenderer().setSeriesPaint(0, barColor);
        chart.setBackgroundPaint(Color.WHITE);
    }

    private byte[] chartToByteArray(JFreeChart chart, int width, int height) throws Exception {
        ByteArrayOutputStream chartOutputStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartOutputStream, chart, width, height);
        return chartOutputStream.toByteArray();
    }

    private boolean hasStatusData(StatusBreakdown status) {
        if (status == null) return false;
        return status.getCreated() > 0 || status.getReadyForPickup() > 0 ||
               status.getCollected() > 0 || status.getInTransit() > 0 ||
               status.getDelivered() > 0 || status.getCancelled() > 0;
    }

    private boolean hasTimeRangeData(TimeRangeBreakdown timeRange) {
        if (timeRange == null) return false;
        return timeRange.getLessThan24Hours() > 0 || timeRange.getOneToThreeDays() > 0 ||
               timeRange.getThreeTo7Days() > 0 || timeRange.getMoreThan7Days() > 0;
    }

    private boolean hasDeliveryTimeData(DeliveryTimeBreakdown breakdown) {
        if (breakdown == null) return false;
        return breakdown.getDeliveringSoon() > 0 ||
               breakdown.getDelivering1to3Days() > 0 ||
               breakdown.getDeliveringLater() > 0;
    }

    private boolean hasOverdueData(OverdueBreakdown breakdown) {
        if (breakdown == null) return false;
        return breakdown.getOverdue() > 0 ||
               breakdown.getDueSoon() > 0 ||
               breakdown.getOnTime() > 0;
    }

    private boolean hasExceptionTypeData(Map<String, Integer> exceptionTypes) {
        if (exceptionTypes == null || exceptionTypes.isEmpty()) return false;
        return exceptionTypes.values().stream().anyMatch(count -> count > 0);
    }
}

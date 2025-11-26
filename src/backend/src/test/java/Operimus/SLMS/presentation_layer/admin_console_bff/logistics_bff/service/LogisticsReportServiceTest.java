package Operimus.SLMS.presentation_layer.admin_console_bff.logistics_bff.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import Operimus.SLMS.presentation_layer.admin_console_bff.logistics_bff.dto.LogisticsStatsResponse;
import Operimus.SLMS.presentation_layer.admin_console_bff.logistics_bff.dto.LogisticsStatsResponse.*;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogisticsReportServiceTest {

    @Mock
    private LogisticsStatsService logisticsStatsService;

    @InjectMocks
    private LogisticsReportService logisticsReportService;

    @Test
    void generateKPIReport_shouldRenderPdfWithKeySections() throws Exception {
        LogisticsStatsResponse stats = buildStatsResponse();
        when(logisticsStatsService.calculateStats("weekly")).thenReturn(stats);

        byte[] pdfBytes = logisticsReportService.generateKPIReport("weekly");

        assertThat(pdfBytes).isNotEmpty();
        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfBytes)))) {
            StringBuilder fullText = new StringBuilder();
            for (int i = 1; i <= pdfDocument.getNumberOfPages(); i++) {
                fullText.append(PdfTextExtractor.getTextFromPage(pdfDocument.getPage(i)));
            }
            assertThat(fullText)
                    .asString()
                    .contains("Logistics Operations Dashboard Report")
                    .contains("Period: Weekly")
                    .contains("Total Shipments")
                    .contains("Pending Pickups")
                    .contains("Average Delivery Time");
        }
    }

    @Test
    void generateKPIReport_shouldWrapExceptionsFromStatsService() {
        when(logisticsStatsService.calculateStats("daily"))
                .thenThrow(new IllegalStateException("stats unavailable"));

        assertThatThrownBy(() -> logisticsReportService.generateKPIReport("daily"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to generate KPI report")
                .hasCauseInstanceOf(IllegalStateException.class);
    }

    private LogisticsStatsResponse buildStatsResponse() {
        TotalShipmentsMetric totalShipments = TotalShipmentsMetric.builder()
                .value(12)
                .trend(5)
                .label("vs last week")
                .byStatus(StatusBreakdown.builder()
                        .created(3)
                        .readyForPickup(2)
                        .collected(1)
                        .inTransit(3)
                        .delivered(3)
                        .cancelled(0)
                        .build())
                .byCarrier(List.of(CarrierBreakdown.builder()
                        .carrierCode("ups")
                        .carrierName("UPS")
                        .count(6)
                        .percentage(50)
                        .build()))
                .build();

        OrdersMetric ordersToday = OrdersMetric.builder()
                .value(5)
                .trend(2)
                .label("vs last week")
                .byWarehouse(List.of(WarehouseBreakdown.builder()
                        .warehouseId(1L)
                        .warehouseName("Main")
                        .count(3)
                        .percentage(60)
                        .build()))
                .byStatus(Map.of("CREATED", 2, "IN_TRANSIT", 3))
                .build();

        PendingPickupsMetric pendingPickups = PendingPickupsMetric.builder()
                .value(2)
                .trend(-10)
                .label("vs last week")
                .alert("1 Overdue")
                .hasWarning(true)
                .byWarehouse(List.of(WarehouseBreakdown.builder()
                        .warehouseId(2L)
                        .warehouseName("Secondary")
                        .count(2)
                        .percentage(40)
                        .build()))
                .overdueBreakdown(OverdueBreakdown.builder()
                        .overdue(1)
                        .dueSoon(1)
                        .onTime(0)
                        .build())
                .build();

        InTransitMetric inTransit = InTransitMetric.builder()
                .value(3)
                .trend(1)
                .label("vs last week")
                .byCarrier(List.of(CarrierBreakdown.builder()
                        .carrierCode("fdx")
                        .carrierName("FedEx")
                        .count(3)
                        .percentage(25)
                        .build()))
                .byDeliveryTime(DeliveryTimeBreakdown.builder()
                        .deliveringSoon(1)
                        .delivering1to3Days(1)
                        .deliveringLater(1)
                        .build())
                .build();

        DeliveredMetric delivered = DeliveredMetric.builder()
                .value(4)
                .trend(3)
                .label("vs last week")
                .byCarrier(List.of(CarrierBreakdown.builder()
                        .carrierCode("dhl")
                        .carrierName("DHL")
                        .count(2)
                        .percentage(17)
                        .build()))
                .onTimeBreakdown(OnTimeBreakdown.builder()
                        .onTime(3)
                        .delayed(1)
                        .onTimePercentage(75)
                        .build())
                .build();

        CostMetric averageCost = CostMetric.builder()
                .value(12.5)
                .trend(4)
                .label("vs last week")
                .byCarrier(List.of(CarrierCostBreakdown.builder()
                        .carrierCode("ups")
                        .carrierName("UPS")
                        .totalCost(50)
                        .averageCost(10)
                        .shipmentCount(5)
                        .build()))
                .byCostRange(List.of(CostRangeBreakdown.builder()
                        .range("$0-$10")
                        .count(3)
                        .totalCost(25)
                        .build()))
                .build();

        CostMetric totalCost = CostMetric.builder()
                .value(120.0)
                .trend(6)
                .label("vs last week")
                .build();

        TimeMetric avgDeliveryTime = TimeMetric.builder()
                .value(5.0)
                .unit("hours")
                .trend(-2)
                .label("vs last week")
                .byTimeRange(TimeRangeBreakdown.builder()
                        .lessThan24Hours(2)
                        .oneToThreeDays(1)
                        .threeTo7Days(0)
                        .moreThan7Days(0)
                        .build())
                .build();

        ExceptionsMetric exceptions = ExceptionsMetric.builder()
                .value(1)
                .trend(0)
                .label("vs last week")
                .byType(Map.of("delayed", 1))
                .byStatus(Map.of("open", 1))
                .bySeverity(Map.of("high", 1))
                .build();

        TicketsMetric tickets = TicketsMetric.builder()
                .value(2)
                .trend(1)
                .label("vs last week")
                .byStatus(Map.of("open", 1, "resolved", 1))
                .averageResolutionTime(4.5)
                .resolutionTimeUnit("hours")
                .build();

        WarehousesMetric warehouses = WarehousesMetric.builder()
                .value(2)
                .warehouses(List.of(
                        WarehouseBreakdown.builder().warehouseId(1L).warehouseName("Main").count(6).percentage(60).build(),
                        WarehouseBreakdown.builder().warehouseId(2L).warehouseName("Secondary").count(4).percentage(40).build()
                ))
                .build();

        DriversMetric drivers = DriversMetric.builder()
                .value(2)
                .total(3)
                .inactive(1)
                .byCarrier(List.of(
                        CarrierBreakdown.builder().carrierCode("ups").carrierName("UPS").count(1).percentage(34).build(),
                        CarrierBreakdown.builder().carrierCode("fdx").carrierName("FedEx").count(1).percentage(33).build()
                ))
                .build();

        return LogisticsStatsResponse.builder()
                .period("weekly")
                .totalShipments(totalShipments)
                .ordersToday(ordersToday)
                .pendingPickups(pendingPickups)
                .inTransitNow(inTransit)
                .deliveredToday(delivered)
                .averageCostPerShipment(averageCost)
                .totalShipmentCosts(totalCost)
                .averageDeliveryTime(avgDeliveryTime)
                .totalExceptions(exceptions)
                .totalTickets(tickets)
                .totalWarehouses(warehouses)
                .activeDrivers(drivers)
                .build();
    }
}

package Operimus.SLMS.business_layer.notifications_module.service;

import Operimus.SLMS.business_layer.reporting_module.dto.LogisticsStatsResponse;
import Operimus.SLMS.business_layer.reporting_module.service.ReportStatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogisticsKpiNotificationSchedulerTest {

    @Mock
    private LogisticsManagerNotificationService notificationService;

    @Mock
    private ReportStatsService reportStatsService;

    @InjectMocks
    private LogisticsKpiNotificationScheduler scheduler;

    private LogisticsStatsResponse.LogisticsStatsResponseBuilder statsBuilder;

    @BeforeEach
    void setUp() {
        // Initialize a baseline builder with "safe" values that trigger no alerts
        statsBuilder = LogisticsStatsResponse.builder()
                .totalShipments(LogisticsStatsResponse.TotalShipmentsMetric.builder().value(100).build())
                .totalExceptions(LogisticsStatsResponse.ExceptionsMetric.builder().value(2).build()) // 2% exception rate (safe)
                .pendingPickups(LogisticsStatsResponse.PendingPickupsMetric.builder().value(10).build()) // < 20 (safe)
                .deliveredToday(LogisticsStatsResponse.DeliveredMetric.builder()
                        .trend(0) // Stable trend
                        .onTimeBreakdown(LogisticsStatsResponse.OnTimeBreakdown.builder().onTimePercentage(90).build()) // < 95 (safe)
                        .build());
    }

    @Test
    void whenHighExceptionRate_thenSendAlert() {
        // Arrange: 10% exception rate (10/100)
        LogisticsStatsResponse stats = statsBuilder
                .totalShipments(LogisticsStatsResponse.TotalShipmentsMetric.builder().value(100).build())
                .totalExceptions(LogisticsStatsResponse.ExceptionsMetric.builder().value(10).build())
                .build();

        when(reportStatsService.calculateStats("daily")).thenReturn(stats);

        // Act
        scheduler.generateAndSendReport();

        // Assert
        verify(notificationService).notifyDailyKpiReport(contains("High Exception Rate"));
    }

    @Test
    void whenPickupBacklog_thenSendAlert() {
        // Arrange: 25 pending pickups (> 20)
        LogisticsStatsResponse stats = statsBuilder
                .pendingPickups(LogisticsStatsResponse.PendingPickupsMetric.builder().value(25).build())
                .build();

        when(reportStatsService.calculateStats("daily")).thenReturn(stats);

        // Act
        scheduler.generateAndSendReport();

        // Assert
        verify(notificationService).notifyDailyKpiReport(contains("Pickup Backlog"));
    }

    @Test
    void whenPerformanceDrop_thenSendAlert() {
        // Arrange: -15% trend (< -10%)
        LogisticsStatsResponse stats = statsBuilder
                .deliveredToday(LogisticsStatsResponse.DeliveredMetric.builder()
                        .trend(-15)
                        .onTimeBreakdown(LogisticsStatsResponse.OnTimeBreakdown.builder().onTimePercentage(90).build())
                        .build())
                .build();

        when(reportStatsService.calculateStats("daily")).thenReturn(stats);

        // Act
        scheduler.generateAndSendReport();

        // Assert
        verify(notificationService).notifyDailyKpiReport(contains("Performance Drop"));
    }

    @Test
    void whenHighReliability_thenSendAlert() {
        // Arrange: 98% on-time (> 95%)
        LogisticsStatsResponse stats = statsBuilder
                .deliveredToday(LogisticsStatsResponse.DeliveredMetric.builder()
                        .trend(0)
                        .onTimeBreakdown(LogisticsStatsResponse.OnTimeBreakdown.builder().onTimePercentage(98).build())
                        .build())
                .build();

        when(reportStatsService.calculateStats("daily")).thenReturn(stats);

        // Act
        scheduler.generateAndSendReport();

        // Assert
        verify(notificationService).notifyDailyKpiReport(contains("High Reliability"));
    }

    @Test
    void whenSmoothOperation_thenSendAlert() {
        // Arrange: 0 exceptions
        LogisticsStatsResponse stats = statsBuilder
                .totalShipments(LogisticsStatsResponse.TotalShipmentsMetric.builder().value(100).build())
                .totalExceptions(LogisticsStatsResponse.ExceptionsMetric.builder().value(0).build())
                .build();

        when(reportStatsService.calculateStats("daily")).thenReturn(stats);

        // Act
        scheduler.generateAndSendReport();

        // Assert
        verify(notificationService).notifyDailyKpiReport(contains("Smooth Operation"));
    }

    @Test
    void whenNoThresholdsMet_thenDoNotSendAlert() {
        // Arrange: Use baseline safe values
        LogisticsStatsResponse stats = statsBuilder.build();

        when(reportStatsService.calculateStats("daily")).thenReturn(stats);

        // Act
        scheduler.generateAndSendReport();

        // Assert
        verify(notificationService, never()).notifyDailyKpiReport(anyString());
    }

    @Test
    void whenExceptionOccurs_thenLogAndDoNotCrash() {
        // Arrange
        when(reportStatsService.calculateStats("daily")).thenThrow(new RuntimeException("Service failure"));

        // Act
        scheduler.generateAndSendReport();

        // Assert
        verify(notificationService, never()).notifyDailyKpiReport(anyString());
    }
}

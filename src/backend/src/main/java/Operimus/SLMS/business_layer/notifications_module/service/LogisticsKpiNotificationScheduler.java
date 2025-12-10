package Operimus.SLMS.business_layer.notifications_module.service;

import Operimus.SLMS.business_layer.reporting_module.dto.LogisticsStatsResponse;
import Operimus.SLMS.business_layer.reporting_module.service.ReportStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Scheduler to trigger daily KPI notifications for logistics managers
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LogisticsKpiNotificationScheduler {

    private final LogisticsManagerNotificationService notificationService;
    private final ReportStatsService reportStatsService;

    /**
     * Run hourly
     */
    @Scheduled(cron = "0 0 * * * *")
    public void scheduleDailyKpiReport() {
        log.info("⏰ Running scheduled hourly KPI monitor...");
        generateAndSendReport();
    }

    /**
     * Generate stats and trigger notification if thresholds are met
     */
    public void generateAndSendReport() {
        try {
            // Fetch daily stats
            LogisticsStatsResponse stats = reportStatsService.calculateStats("daily");
            
            // Check for alerts
            String alertMessage = evaluateThresholds(stats);
            
            if (alertMessage != null) {
                // Send notification only if there are alerts
                notificationService.notifyDailyKpiReport(alertMessage);
                log.info("✅ Daily KPI alert sent successfully");
            } else {
                log.info("ℹ️ No significant KPI events to report today");
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to generate daily KPI report", e);
        }
    }

    private String evaluateThresholds(LogisticsStatsResponse stats) {
        StringBuilder sb = new StringBuilder();
        boolean hasAlert = false;
        
        sb.append("📢 **Daily Logistics Alert** (").append(LocalDate.now()).append(")\n\n");

        // --- BAD CONDITIONS (WARNINGS) ---

        // 1. High Exception Rate (> 5%)
        double totalShipments = stats.getTotalShipments().getValue();
        double totalExceptions = stats.getTotalExceptions().getValue();
        if (totalShipments > 0) {
            double exceptionRate = (totalExceptions / totalShipments) * 100;
            if (exceptionRate > 5.0) {
                sb.append("⚠️ **High Exception Rate:** ").append(String.format("%.1f", exceptionRate))
                  .append("% of shipments have issues (").append((int)totalExceptions).append(" exceptions).\n");
                hasAlert = true;
            }
        }

        // 2. Backlog (Pending Pickups > 20)
        int pendingPickups = stats.getPendingPickups().getValue();
        if (pendingPickups > 20) {
            sb.append("⚠️ **Pickup Backlog:** ").append(pendingPickups).append(" orders are waiting for pickup.\n");
            hasAlert = true;
        }

        // 3. Performance Drop (Delivered trend < -10%)
        Integer deliveryTrend = stats.getDeliveredToday().getTrend();
        if (deliveryTrend != null && deliveryTrend < -10) {
            sb.append("📉 **Performance Drop:** Deliveries are down by ").append(Math.abs(deliveryTrend))
              .append("% compared to yesterday.\n");
            hasAlert = true;
        }

        // --- GOOD CONDITIONS (SUCCESS) ---

        // 4. High Reliability (On-Time > 95%)
        Integer onTimePct = stats.getDeliveredToday().getOnTimeBreakdown().getOnTimePercentage();
        if (onTimePct != null && onTimePct > 95) {
            sb.append("✅ **High Reliability:** ").append(onTimePct).append("% of shipments delivered on time!\n");
            hasAlert = true;
        }

        // 5. Smooth Operation (Zero Exceptions)
        if (totalShipments > 0 && totalExceptions == 0) {
            sb.append("🎉 **Smooth Operation:** Zero exceptions reported today.\n");
            hasAlert = true;
        }

        return hasAlert ? sb.toString() : null;
    }
}

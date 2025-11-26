package Operimus.SLMS.presentation_layer.driver_pwa_bff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverDashboardResponse {
    private TodayStats todayStats;
    private List<NextDeliveryPreview> nextDeliveries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodayStats {
        private Integer totalDeliveries;
        private Integer completed;
        private Integer remaining;
        private String nextStopETA;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextDeliveryPreview {
        private Long orderId;
        private Integer stopNumber;
        private String customerName;
        private String address;
        private String priority;
    }
}

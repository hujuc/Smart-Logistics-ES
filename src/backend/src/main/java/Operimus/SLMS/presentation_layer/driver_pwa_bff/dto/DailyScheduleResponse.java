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
public class DailyScheduleResponse {
    private String shipmentCode;
    private Long shipmentId;
    private Integer totalStops;
    private Integer totalPackages;
    private Double totalWeight;
    private String weightUnit;
    private List<DailyScheduleDeliveryResponse> deliveries;
}

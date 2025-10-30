package Operimus.SLMS.business_layer.visibility_module.exceptions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for dashboard summary statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionSummaryResponse {

    private Long totalExceptions;
    private Long openExceptions;
    private Long inProgressExceptions;
    private Long resolvedToday;
    private Long highPriorityExceptions;
    private Long slaBreachedExceptions;
    private Double averageResolutionTimeHours;
    private Double customerSatisfactionRate;
}

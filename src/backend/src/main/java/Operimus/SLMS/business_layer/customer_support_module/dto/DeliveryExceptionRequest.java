package Operimus.SLMS.business_layer.customer_support_module.dto;

import Operimus.SLMS.business_layer.customer_support_module.entities.DeliveryException.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating a new delivery exception
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryExceptionRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Exception type is required")
    private ExceptionType type;

    @NotBlank(message = "Category is required")
    private String category;

    private Severity severity;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Source is required")
    private Source source;

    private UUID reportedBy;

    private Boolean customerReported;

    private UUID customerId;

    private UUID assignedTo;

    private Priority priority;
}

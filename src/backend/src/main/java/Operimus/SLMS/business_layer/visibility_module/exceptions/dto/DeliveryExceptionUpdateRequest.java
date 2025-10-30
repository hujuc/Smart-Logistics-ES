package Operimus.SLMS.business_layer.visibility_module.exceptions.dto;

import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for updating an existing delivery exception
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryExceptionUpdateRequest {

    private ExceptionStatus status;
    private Priority priority;
    private UUID assignedTo;
    private String resolutionNotes;
    private String resolutionType;
    private Severity severity;
}

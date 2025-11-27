package Operimus.SLMS.business_layer.rate_carrier_module.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request DTO for driver to complete delivery.
 * If hasException is true, exception details must be provided.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryCompletionRequest {

    /**
     * Whether the delivery encountered an exception
     */
    private Boolean hasException;

    /**
     * Type of exception (required if hasException is true)
     * Options: CUSTOMER_NOT_AVAILABLE, WRONG_ADDRESS, PACKAGE_DAMAGED, ACCESS_DENIED, WEATHER
     */
    private String exceptionType;

    /**
     * Detailed description of the exception (required if hasException is true)
     */
    private String exceptionDescription;

    /**
     * Optional photo URL for proof of exception
     */
    private String photoUrl;
}

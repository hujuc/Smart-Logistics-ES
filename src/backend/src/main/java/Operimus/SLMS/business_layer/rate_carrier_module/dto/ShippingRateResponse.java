package Operimus.SLMS.business_layer.rate_carrier_module.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingRateResponse {

    private String shipmentId;
    @Builder.Default
    private List<RateQuote> rates = Collections.emptyList();
    @Builder.Default
    private List<String> messages = Collections.emptyList();

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateQuote {

        private String rateId;
        private String provider;
        private String carrierAccount;
        private String carrierCode;
        private String serviceLevel;
        private BigDecimal amount;
        private String currency;
        private Integer estimatedDays;
        private OffsetDateTime deliveryDate;
    }
}

package Operimus.SLMS.business_layer.rate_carrier_module.dto;

import java.math.BigDecimal;
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
public class ShippoLabelResponse {

    private String transactionId;
    private String status;
    private String labelUrl;
    private String labelFileType;
    private String trackingNumber;
    private String trackingUrl;
    private String carrier;
    private String carrierAccount;
    private String serviceLevel;
    private String shipmentId;
    private String rateId;
    private BigDecimal amount;
    private String currency;
    private String commercialInvoiceUrl;  // URL for customs documents (international shipments)
    @Builder.Default
    private List<String> messages = Collections.emptyList();
}


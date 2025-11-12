package Operimus.SLMS.business_layer.order_module.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentDocumentResponse {

    private Long shipmentId;
    private Long orderId;
    private String documentId;
    private String objectKey;
    private String fileName;
    private String contentType;
    private long size;
    private String url;
    private String carrierCode;
    private String serviceLevel;
}

package Operimus.SLMS.business_layer.order_module.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentDownloadResponse {

    private final String fileName;
    private final String contentType;
    private final long size;
    private final byte[] content;
}

package Operimus.SLMS.business_layer.order_shipment_module.controllers;

import Operimus.SLMS.business_layer.order_shipment_module.dto.DocumentUploadResponse;
import Operimus.SLMS.business_layer.order_shipment_module.service.OrderDocumentService;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderDocumentController {

    private final OrderDocumentService orderDocumentService;

    @PostMapping(path = "/{orderId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @PathVariable Long orderId, @RequestPart("file") MultipartFile file) {
        DocumentUploadResponse response = orderDocumentService.uploadDocument(orderId, file);
        return ResponseEntity.created(buildLocation(orderId, response.getObjectKey())).body(response);
    }

    private URI buildLocation(Long orderId, String objectKey) {
        return URI.create("/api/admin/orders/%d/documents/%s".formatted(orderId, objectKey));
    }
}

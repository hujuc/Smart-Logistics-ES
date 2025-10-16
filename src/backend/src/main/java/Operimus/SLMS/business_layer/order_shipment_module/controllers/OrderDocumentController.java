package Operimus.SLMS.business_layer.order_shipment_module.controllers;

import Operimus.SLMS.business_layer.order_shipment_module.dto.DocumentDownloadResponse;
import Operimus.SLMS.business_layer.order_shipment_module.dto.DocumentUploadResponse;
import Operimus.SLMS.business_layer.order_shipment_module.service.OrderDocumentService;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
        return ResponseEntity.created(buildLocation(orderId, response.getDocumentId())).body(response);
    }

    @GetMapping("/{orderId}/documents")
    public List<DocumentUploadResponse> listDocuments(@PathVariable Long orderId) {
        return orderDocumentService.listDocuments(orderId);
    }

    @GetMapping("/{orderId}/documents/{documentId}")
    public DocumentUploadResponse getDocument(@PathVariable Long orderId, @PathVariable String documentId) {
        return orderDocumentService.getDocumentMetadata(orderId, documentId);
    }

    @GetMapping("/{orderId}/documents/{documentId}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long orderId, @PathVariable String documentId) {
        DocumentDownloadResponse download = orderDocumentService.downloadDocument(orderId, documentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(download.getFileName()))
                .contentLength(download.getSize())
                .body(download.getContent());
    }

    @PutMapping(path = "/{orderId}/documents/{documentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentUploadResponse updateDocument(
            @PathVariable Long orderId,
            @PathVariable String documentId,
            @RequestPart("file") MultipartFile file) {
        return orderDocumentService.updateDocument(orderId, documentId, file);
    }

    @DeleteMapping("/{orderId}/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long orderId, @PathVariable String documentId) {
        orderDocumentService.deleteDocument(orderId, documentId);
        return ResponseEntity.noContent().build();
    }

    private URI buildLocation(Long orderId, String documentId) {
        return URI.create("/api/admin/orders/%d/documents/%s".formatted(orderId, documentId));
    }
}

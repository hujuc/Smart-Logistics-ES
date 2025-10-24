package Operimus.SLMS.presentation_layer.admin_console_bff;

import Operimus.SLMS.business_layer.order_shipment_module.dto.DocumentDownloadResponse;
import Operimus.SLMS.business_layer.order_shipment_module.dto.DocumentUploadResponse;
import Operimus.SLMS.business_layer.order_shipment_module.service.OrderDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Warehouse Document BFF Controller - Admin Console
 *
 * This controller handles document operations (labels, packing slips, QR codes) for the warehouse staff interface.
 * It delegates to business layer document services.
 *
 * Endpoints are mapped to /api/warehouse/orders/{orderId}/documents
 */
@Slf4j
@RestController
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost", "http://localhost:3000"}, allowCredentials = "true")
public class WarehouseDocumentBffController {

    private final OrderDocumentService orderDocumentService;

    /**
     * List all documents for an order
     *
     * @param orderId Order ID
     * @return List of documents
     */
    @GetMapping("/orders/{orderId}/documents")
    public ResponseEntity<List<DocumentUploadResponse>> listDocuments(@PathVariable Long orderId) {
        try {
            log.info("Warehouse BFF: Listing documents for order {}", orderId);

            // Delegate to business layer
            List<DocumentUploadResponse> documents = orderDocumentService.listDocuments(orderId);

            return ResponseEntity.ok(documents);

        } catch (Exception e) {
            log.error("Failed to list documents for order {} via warehouse BFF: {}", orderId, e.getMessage());
            throw e;
        }
    }

    /**
     * Download a specific document
     *
     * @param orderId Order ID
     * @param documentId Document ID
     * @return Document file
     */
    @GetMapping("/orders/{orderId}/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long orderId,
            @PathVariable String documentId) {
        try {
            log.info("Warehouse BFF: Downloading document {} for order {}", documentId, orderId);

            // Delegate to business layer
            DocumentDownloadResponse response = orderDocumentService.downloadDocument(orderId, documentId);

            ByteArrayResource resource = new ByteArrayResource(response.getContent());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(response.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Failed to download document {} for order {} via warehouse BFF: {}",
                    documentId, orderId, e.getMessage());
            throw e;
        }
    }
}

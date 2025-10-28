package Operimus.SLMS.business_layer.order_shipment_module.service;

import Operimus.SLMS.business_layer.order_shipment_module.dto.DocumentDownloadResponse;
import Operimus.SLMS.business_layer.order_shipment_module.dto.DocumentUploadResponse;
import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrder;
import Operimus.SLMS.business_layer.order_shipment_module.entities.Warehouse;
import Operimus.SLMS.business_layer.order_shipment_module.repository.ShipmentOrderRepository;
import Operimus.SLMS.business_layer.order_shipment_module.repository.WarehouseRepository;
import Operimus.SLMS.business_layer.shared.exception.DocumentStorageException;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import Operimus.SLMS.config.TestSecurityConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test for OrderDocumentService that tests real MinIO S3 interactions.
 *
 * This test requires MinIO to be running on the configured endpoint.
 * For local testing, start MinIO with: docker-compose -f infra/docker-compose.dev.yml up minio
 *
 * Note: This test uses @SpringBootTest and requires the full application context.
 * Security is mocked via TestSecurityConfig to avoid connecting to Keycloak.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class OrderDocumentServiceIntegrationTest {

    @Autowired
    private OrderDocumentService orderDocumentService;

    @Autowired
    private ShipmentOrderRepository shipmentOrderRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private ShipmentOrder testOrder;
    private Warehouse testWarehouse;

    @BeforeEach
    void setUp() {
        // Create test warehouse
        testWarehouse = Warehouse.builder()
                .code("TEST-WH-" + System.currentTimeMillis())
                .name("Test Warehouse")
                .addressLine1("Test Street 123")
                .city("Porto")
                .stateOrProvince("Porto")
                .country("PT")
                .postalCode("4000-001")
                .active(true)
                .build();
        testWarehouse = warehouseRepository.save(testWarehouse);

        // Create test order
        testOrder = ShipmentOrder.builder()
                .reference("TEST-ORD-" + System.currentTimeMillis())
                .shippingAddress("Test Address 456")
                .warehouse(testWarehouse)
                .estimatedDeliveryDate(LocalDate.now().plusDays(7))
                .build();
        testOrder = shipmentOrderRepository.save(testOrder);
    }

    @AfterEach
    void tearDown() {
        // Clean up test documents from MinIO
        try {
            String prefix = "orders/" + testOrder.getId() + "/";
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            for (S3Object s3Object : listResponse.contents()) {
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Object.key())
                        .build());
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }

        // Clean up database
        if (testOrder != null) {
            shipmentOrderRepository.delete(testOrder);
        }
        if (testWarehouse != null) {
            warehouseRepository.delete(testWarehouse);
        }
    }

    @Test
    void testMinIOConnection() {
        // Test that we can connect to MinIO
        assertThatCode(() -> {
            HeadBucketRequest request = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.headBucket(request);
        }).as("Should be able to connect to MinIO and check bucket")
                .doesNotThrowAnyException();
    }

    @Test
    void testUploadDocument() {
        // Given
        byte[] content = "Test PDF content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-invoice.pdf",
                "application/pdf",
                content
        );

        // When
        DocumentUploadResponse response = orderDocumentService.uploadDocument(testOrder.getId(), file);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(testOrder.getId());
        assertThat(response.getFileName()).isEqualTo("test-invoice.pdf");
        assertThat(response.getDocumentId()).isEqualTo("test-invoice.pdf");
        assertThat(response.getContentType()).isEqualTo("application/pdf");
        assertThat(response.getSize()).isEqualTo(content.length);
        assertThat(response.getObjectKey()).contains("orders/" + testOrder.getId());

        // Verify document actually exists in MinIO
        String objectKey = response.getObjectKey();
        assertThatCode(() -> {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build());
        }).as("Document should exist in MinIO")
                .doesNotThrowAnyException();
    }

    @Test
    void testUploadAndDownloadDocument() {
        // Given
        byte[] originalContent = "Test document content with special chars: áéíóú".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-document.txt",
                "text/plain",
                originalContent
        );

        // When - Upload
        DocumentUploadResponse uploadResponse = orderDocumentService.uploadDocument(testOrder.getId(), file);

        // When - Download
        DocumentDownloadResponse downloadResponse = orderDocumentService.downloadDocument(
                testOrder.getId(),
                uploadResponse.getDocumentId()
        );

        // Then
        assertThat(downloadResponse).isNotNull();
        assertThat(downloadResponse.getFileName()).isEqualTo("test-document.txt");
        assertThat(downloadResponse.getContentType()).isEqualTo("text/plain");
        assertThat(downloadResponse.getSize()).isEqualTo(originalContent.length);
        assertThat(downloadResponse.getContent()).isEqualTo(originalContent);
    }

    @Test
    void testListDocuments() {
        // Given - Upload multiple documents
        MockMultipartFile file1 = new MockMultipartFile(
                "file1", "document1.pdf", "application/pdf", "Content 1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "file2", "document2.png", "image/png", "Content 2".getBytes()
        );
        MockMultipartFile file3 = new MockMultipartFile(
                "file3", "document3.txt", "text/plain", "Content 3".getBytes()
        );

        orderDocumentService.uploadDocument(testOrder.getId(), file1);
        orderDocumentService.uploadDocument(testOrder.getId(), file2);
        orderDocumentService.uploadDocument(testOrder.getId(), file3);

        // When
        List<DocumentUploadResponse> documents = orderDocumentService.listDocuments(testOrder.getId());

        // Then
        assertThat(documents).hasSize(3);
        assertThat(documents)
                .extracting(DocumentUploadResponse::getFileName)
                .containsExactlyInAnyOrder("document1.pdf", "document2.png", "document3.txt");
        assertThat(documents)
                .extracting(DocumentUploadResponse::getContentType)
                .containsExactlyInAnyOrder("application/pdf", "image/png", "text/plain");
    }

    @Test
    void testGetDocumentMetadata() {
        // Given
        byte[] content = "Test content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "metadata-test.pdf", "application/pdf", content
        );
        DocumentUploadResponse uploadResponse = orderDocumentService.uploadDocument(testOrder.getId(), file);

        // When
        DocumentUploadResponse metadata = orderDocumentService.getDocumentMetadata(
                testOrder.getId(),
                uploadResponse.getDocumentId()
        );

        // Then
        assertThat(metadata).isNotNull();
        assertThat(metadata.getFileName()).isEqualTo("metadata-test.pdf");
        assertThat(metadata.getContentType()).isEqualTo("application/pdf");
        assertThat(metadata.getSize()).isEqualTo(content.length);
    }

    @Test
    void testUpdateDocument() {
        // Given - Upload initial document
        byte[] originalContent = "Original content".getBytes();
        MockMultipartFile originalFile = new MockMultipartFile(
                "file", "update-test.txt", "text/plain", originalContent
        );
        DocumentUploadResponse uploadResponse = orderDocumentService.uploadDocument(testOrder.getId(), originalFile);

        // When - Update the document
        byte[] updatedContent = "Updated content with more data".getBytes();
        MockMultipartFile updatedFile = new MockMultipartFile(
                "file", "update-test.txt", "text/plain", updatedContent
        );
        DocumentUploadResponse updateResponse = orderDocumentService.updateDocument(
                testOrder.getId(),
                uploadResponse.getDocumentId(),
                updatedFile
        );

        // Then
        assertThat(updateResponse.getSize()).isEqualTo(updatedContent.length);

        // Verify the content was actually updated in MinIO
        DocumentDownloadResponse downloadResponse = orderDocumentService.downloadDocument(
                testOrder.getId(),
                uploadResponse.getDocumentId()
        );
        assertThat(downloadResponse.getContent()).isEqualTo(updatedContent);
    }

    @Test
    void testDeleteDocument() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "delete-test.pdf", "application/pdf", "Delete me".getBytes()
        );
        DocumentUploadResponse uploadResponse = orderDocumentService.uploadDocument(testOrder.getId(), file);

        // Verify document exists
        List<DocumentUploadResponse> documentsBefore = orderDocumentService.listDocuments(testOrder.getId());
        assertThat(documentsBefore).hasSize(1);

        // When
        orderDocumentService.deleteDocument(testOrder.getId(), uploadResponse.getDocumentId());

        // Then
        List<DocumentUploadResponse> documentsAfter = orderDocumentService.listDocuments(testOrder.getId());
        assertThat(documentsAfter).isEmpty();

        // Verify document no longer exists in MinIO
        String objectKey = uploadResponse.getObjectKey();
        assertThatThrownBy(() -> {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build());
        }).as("Document should not exist in MinIO after deletion")
                .isInstanceOf(NoSuchKeyException.class);
    }

    @Test
    void testDeleteNonExistentDocument() {
        // Given - Document that doesn't exist
        String nonExistentDocId = "non-existent.pdf";

        // When - Delete is idempotent in S3, so it won't throw an exception
        // The service will succeed even if the document doesn't exist
        assertThatCode(() ->
                orderDocumentService.deleteDocument(testOrder.getId(), nonExistentDocId)
        ).doesNotThrowAnyException();

        // Then - Verify the document still doesn't exist
        assertThatThrownBy(() ->
                orderDocumentService.getDocumentMetadata(testOrder.getId(), nonExistentDocId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(nonExistentDocId);
    }

    @Test
    void testDownloadNonExistentDocument() {
        // When/Then
        assertThatThrownBy(() ->
                orderDocumentService.downloadDocument(testOrder.getId(), "missing-document.pdf")
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing-document.pdf");
    }

    @Test
    void testUploadEmptyFile() {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0]
        );

        // When/Then
        assertThatThrownBy(() ->
                orderDocumentService.uploadDocument(testOrder.getId(), emptyFile)
        )
                .isInstanceOf(DocumentStorageException.class)
                .hasMessageContaining("vazio");
    }

    @Test
    void testUploadWithInvalidOrderId() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "content".getBytes()
        );

        // When/Then
        assertThatThrownBy(() ->
                orderDocumentService.uploadDocument(99999L, file)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99999");
    }

    @Test
    void testUploadMultipleDocumentsWithSameName() {
        // Given - Upload first document
        MockMultipartFile file1 = new MockMultipartFile(
                "file", "duplicate.pdf", "application/pdf", "First content".getBytes()
        );
        DocumentUploadResponse response1 = orderDocumentService.uploadDocument(testOrder.getId(), file1);

        // When - Upload second document with same name (should overwrite)
        MockMultipartFile file2 = new MockMultipartFile(
                "file", "duplicate.pdf", "application/pdf", "Second content".getBytes()
        );
        DocumentUploadResponse response2 = orderDocumentService.uploadDocument(testOrder.getId(), file2);

        // Then - Should have same document ID but updated content
        assertThat(response2.getDocumentId()).isEqualTo(response1.getDocumentId());

        // Verify the content is from the second upload
        DocumentDownloadResponse downloadResponse = orderDocumentService.downloadDocument(
                testOrder.getId(),
                response2.getDocumentId()
        );
        assertThat(new String(downloadResponse.getContent())).isEqualTo("Second content");
    }

    @Test
    void testFilenameSanitization() {
        // Given - File with special characters and path traversal attempt
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../../../etc/passwd with spaces!@#.pdf",
                "application/pdf",
                "Malicious content".getBytes()
        );

        // When
        DocumentUploadResponse response = orderDocumentService.uploadDocument(testOrder.getId(), file);

        // Then - Filename should be sanitized
        assertThat(response.getFileName()).doesNotContain("..");
        assertThat(response.getFileName()).doesNotContain("/");
        assertThat(response.getFileName()).matches("[a-zA-Z0-9._-]+");
    }

    @Test
    void testLargeFileUpload() {
        // Given - 1MB file
        byte[] largeContent = new byte[1024 * 1024]; // 1MB
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large-file.bin", "application/octet-stream", largeContent
        );

        // When
        DocumentUploadResponse response = orderDocumentService.uploadDocument(testOrder.getId(), largeFile);

        // Then
        assertThat(response.getSize()).isEqualTo(largeContent.length);

        // Verify content integrity
        DocumentDownloadResponse downloadResponse = orderDocumentService.downloadDocument(
                testOrder.getId(),
                response.getDocumentId()
        );
        assertThat(downloadResponse.getContent()).isEqualTo(largeContent);
    }
}

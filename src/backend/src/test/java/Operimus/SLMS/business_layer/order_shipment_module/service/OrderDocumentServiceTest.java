package Operimus.SLMS.business_layer.order_shipment_module.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Operimus.SLMS.business_layer.order_shipment_module.dto.DocumentDownloadResponse;
import Operimus.SLMS.business_layer.order_shipment_module.dto.DocumentUploadResponse;
import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrder;
import Operimus.SLMS.business_layer.order_shipment_module.entities.Warehouse;
import Operimus.SLMS.business_layer.order_shipment_module.repository.ShipmentOrderRepository;
import Operimus.SLMS.business_layer.shared.exception.DocumentStorageException;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@ExtendWith(MockitoExtension.class)
class OrderDocumentServiceTest {

    private static final String BUCKET = "bucket";
    private static final String ENDPOINT = "http://localhost:9000";

    @Mock
    private S3Client s3Client;

    @Mock
    private ShipmentOrderRepository shipmentOrderRepository;

    private OrderDocumentService service;

    private ShipmentOrder order;

    @BeforeEach
    void setUp() {
        service = new OrderDocumentService(s3Client, shipmentOrderRepository, BUCKET, ENDPOINT);
        order = ShipmentOrder.builder()
                .id(1L)
                .reference("ORD-001")
                .shippingAddress("Some address")
                .warehouse(buildWarehouse())
                .estimatedDeliveryDate(LocalDate.now())
                .build();
    }

    private Warehouse buildWarehouse() {
        return Warehouse.builder()
                .id(10L)
                .code("MAIN")
                .name("Main Warehouse")
                .addressLine1("Industrial Ave 100")
                .city("Porto")
                .stateOrProvince("Porto")
                .country("PT")
                .postalCode("4000-001")
                .build();
    }

    @Test
    void uploadDocumentShouldStoreFileAndReturnMetadata() {
        when(shipmentOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(s3Client.headBucket(any(HeadBucketRequest.class))).thenThrow(NoSuchBucketException.builder().build());
        when(s3Client.createBucket(any(CreateBucketRequest.class)))
                .thenReturn(CreateBucketResponse.builder().build());
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        MockMultipartFile file = new MockMultipartFile(
                "file", "folder/test File.pdf", "application/pdf", "document-bytes".getBytes());

        DocumentUploadResponse response = service.uploadDocument(1L, file);

        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getDocumentId()).isEqualTo("test_File.pdf");
        assertThat(response.getObjectKey()).isEqualTo("orders/1/test_File.pdf");
        assertThat(response.getUrl()).isEqualTo("http://localhost:9000/bucket/orders/1/test_File.pdf");
        assertThat(response.getSize()).isEqualTo(file.getSize());

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        PutObjectRequest putRequest = requestCaptor.getValue();
        assertThat(putRequest.metadata()).containsEntry("original-filename", "test_File.pdf");

        // Bucket should be created only once even if we upload multiple documents afterwards
        verify(s3Client).createBucket(any(CreateBucketRequest.class));
        service.uploadDocument(1L, file);
        verify(s3Client, times(1)).headBucket(any(HeadBucketRequest.class));
    }

    @Test
    void uploadDocumentShouldFailWhenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);

        assertThatThrownBy(() -> service.uploadDocument(1L, emptyFile))
                .isInstanceOf(DocumentStorageException.class)
                .hasMessage("O ficheiro enviado está vazio.");
        verify(shipmentOrderRepository, never()).findById(anyLong());
    }

    @Test
    void listDocumentsShouldReturnDocumentResponses() {
        when(shipmentOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(s3Client.headBucket(any(HeadBucketRequest.class))).thenReturn(HeadBucketResponse.builder().build());
        S3Object doc1 = S3Object.builder().key("orders/1/doc1.pdf").size(42L).build();
        S3Object doc2 = S3Object.builder().key("orders/1/doc2.png").size(84L).build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(ListObjectsV2Response.builder().contents(List.of(doc1, doc2)).build());
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(
                        HeadObjectResponse.builder()
                                .contentType("application/pdf")
                                .contentLength(42L)
                                .metadata(Map.of("original-filename", "Invoice.pdf"))
                                .build(),
                        HeadObjectResponse.builder()
                                .contentType("image/png")
                                .contentLength(84L)
                                .metadata(Map.of("original-filename", "Label.png"))
                                .build());

        List<DocumentUploadResponse> documents = service.listDocuments(1L);

        assertThat(documents).hasSize(2);
        assertThat(documents.get(0).getFileName()).isEqualTo("doc1.pdf");
        assertThat(documents.get(0).getContentType()).isEqualTo("application/pdf");
        assertThat(documents.get(1).getFileName()).isEqualTo("doc2.png");
        assertThat(documents.get(1).getContentType()).isEqualTo("image/png");
    }

    @Test
    void getDocumentMetadataShouldReturnInformationFromHeadObject() {
        when(shipmentOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(s3Client.headBucket(any(HeadBucketRequest.class))).thenReturn(HeadBucketResponse.builder().build());
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder()
                        .contentType("application/pdf")
                        .contentLength(128L)
                        .metadata(Map.of("original-filename", "invoice.pdf"))
                        .build());

        DocumentUploadResponse response = service.getDocumentMetadata(1L, "invoice.pdf");

        assertThat(response.getFileName()).isEqualTo("invoice.pdf");
        assertThat(response.getSize()).isEqualTo(128L);
        assertThat(response.getContentType()).isEqualTo("application/pdf");
    }

    @Test
    void downloadDocumentShouldReturnBinaryContent() {
        when(shipmentOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(s3Client.headBucket(any(HeadBucketRequest.class))).thenReturn(HeadBucketResponse.builder().build());
        byte[] payload = "payload".getBytes();
        GetObjectResponse getObjectResponse = GetObjectResponse.builder()
                .contentType("application/pdf")
                .contentLength((long) payload.length)
                .metadata(Map.of("original-filename", "invoice.pdf"))
                .build();
        ResponseBytes<GetObjectResponse> responseBytes = ResponseBytes.fromByteArray(getObjectResponse, payload);
        when(s3Client.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class)))
                .thenReturn(responseBytes);

        DocumentDownloadResponse download = service.downloadDocument(1L, "invoice.pdf");

        assertThat(download.getFileName()).isEqualTo("invoice.pdf");
        assertThat(download.getContentType()).isEqualTo("application/pdf");
        assertThat(download.getSize()).isEqualTo(payload.length);
        assertThat(download.getContent()).isEqualTo(payload);
    }

    @Test
    void downloadDocumentShouldThrowWhenObjectMissing() {
        when(shipmentOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(s3Client.headBucket(any(HeadBucketRequest.class))).thenReturn(HeadBucketResponse.builder().build());
        when(s3Client.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class)))
                .thenThrow(NoSuchKeyException.builder().build());

        assertThatThrownBy(() -> service.downloadDocument(1L, "missing.pdf"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing.pdf");
    }

    @Test
    void updateDocumentShouldReuseExistingFilenameWhenSanitizedIsBlank() {
        when(shipmentOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(s3Client.headBucket(any(HeadBucketRequest.class))).thenReturn(HeadBucketResponse.builder().build());
        HeadObjectResponse existing = HeadObjectResponse.builder()
                .contentType("application/pdf")
                .contentLength(256L)
                .metadata(Map.of("original-filename", "stored.pdf"))
                .build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(existing);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());
        MockMultipartFile file = new MockMultipartFile("file", "/", "text/plain", "new-data".getBytes());

        DocumentUploadResponse response = service.updateDocument(1L, "document-id.pdf", file);

        assertThat(response.getFileName()).isEqualTo("stored.pdf");
        assertThat(response.getContentType()).isEqualTo("text/plain");
        assertThat(response.getDocumentId()).isEqualTo("document-id.pdf");

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        PutObjectRequest captured = requestCaptor.getValue();
        assertThat(captured.metadata()).containsEntry("document-id", "document-id.pdf");
    }

    @Test
    void deleteDocumentShouldRemoveObject() {
        when(shipmentOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(s3Client.headBucket(any(HeadBucketRequest.class))).thenReturn(HeadBucketResponse.builder().build());

        service.deleteDocument(1L, "doc.pdf");

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void deleteDocumentShouldThrowWhenObjectDoesNotExist() {
        when(shipmentOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(s3Client.headBucket(any(HeadBucketRequest.class))).thenReturn(HeadBucketResponse.builder().build());
        doThrow(NoSuchKeyException.builder().build())
                .when(s3Client)
                .deleteObject(any(DeleteObjectRequest.class));

        assertThatThrownBy(() -> service.deleteDocument(1L, "missing.pdf"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing.pdf");
    }
}

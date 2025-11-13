package Operimus.SLMS.business_layer.order_module.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import Operimus.SLMS.business_layer.order_module.dto.DocumentDownloadResponse;
import Operimus.SLMS.business_layer.order_module.dto.ShipmentDocumentResponse;
import Operimus.SLMS.business_layer.order_module.entities.Order;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.entities.Warehouse;
import Operimus.SLMS.business_layer.order_module.repository.OrderRepository;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Carrier;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Shipment;
import Operimus.SLMS.business_layer.rate_carrier_module.repository.ShipmentRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

@ExtendWith(MockitoExtension.class)
class ShipmentDocumentServiceTest {

    private static final String BUCKET = "documents";
    private static final String ENDPOINT = "http://localhost:9000";

    @Mock
    private S3Client s3Client;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private OrderRepository orderRepository;

    private ShipmentDocumentService service;

    private Shipment shipment;
    private Order order;

    @BeforeEach
    void setUp() {
        service = new ShipmentDocumentService(s3Client, shipmentRepository, orderRepository, BUCKET, ENDPOINT);

        Carrier carrier = Carrier.builder().id(10L).code("UPS").name("UPS").build();
        shipment = Shipment.builder()
                .id(5L)
                .shipmentCode("SHIP-001")
                .carrier(carrier)
                .driverId("driver")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        Warehouse warehouse = Warehouse.builder()
                .id(99L)
                .code("MAIN")
                .name("Main WH")
                .addressLine1("Street")
                .city("Lisbon")
                .stateOrProvince("Lisboa")
                .postalCode("1000-001")
                .country("PT")
                .active(true)
                .build();

        order = Order.builder()
                .id(8L)
                .reference("ORD-001")
                .warehouse(warehouse)
                .status(OrderStatus.ASSIGNED_TO_SHIPMENT)
                .shipmentId(shipment.getId())
                .shippingAddress("Rua A, Porto, Porto 4000-001, PT")
                .build();
    }

    @Test
    void saveDocumentShouldStoreContentInS3() {
        when(shipmentRepository.findById(5L)).thenReturn(Optional.of(shipment));
        when(orderRepository.findById(8L)).thenReturn(Optional.of(order));
        when(s3Client.headBucket(any(HeadBucketRequest.class))).thenThrow(NoSuchBucketException.builder().build());
        when(s3Client.createBucket(any(CreateBucketRequest.class)))
                .thenReturn(CreateBucketResponse.builder().build());
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        ShipmentDocumentResponse response = service.saveDocument(
                5L,
                8L,
                "label.pdf",
                "Label.pdf",
                "application/pdf",
                "content".getBytes(),
                Map.of("carrier-code", "UPS"));

        assertThat(response.getShipmentId()).isEqualTo(5L);
        assertThat(response.getOrderId()).isEqualTo(8L);
        assertThat(response.getDocumentId()).isEqualTo("label.pdf");
        assertThat(response.getObjectKey()).isEqualTo("shipments/5/orders/8/label.pdf");
        assertThat(response.getCarrierCode()).isEqualTo("UPS");
        assertThat(response.getUrl()).isEqualTo("http://localhost:9000/documents/shipments/5/orders/8/label.pdf");
    }

    @Test
    void listByOrderShouldReturnStoredDocuments() {
        when(orderRepository.findById(8L)).thenReturn(Optional.of(order));
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(ListObjectsV2Response.builder()
                .contents(List.of(S3Object.builder()
                        .key("shipments/5/orders/8/label.pdf")
                        .size(120L)
                        .build()))
                .build());
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder()
                .contentType("application/pdf")
                .metadata(Map.of(
                        "original-filename", "Carrier Label.pdf",
                        "carrier-code", "UPS"))
                .build());

        List<ShipmentDocumentResponse> documents = service.listByOrder(8L);

        assertThat(documents).hasSize(1);
        ShipmentDocumentResponse document = documents.get(0);
        assertThat(document.getFileName()).isEqualTo("Carrier Label.pdf");
        assertThat(document.getSize()).isEqualTo(120L);
        assertThat(document.getCarrierCode()).isEqualTo("UPS");
    }

    @Test
    void downloadForOrderShouldReturnFileBytes() {
        when(orderRepository.findById(8L)).thenReturn(Optional.of(order));
        GetObjectResponse response = GetObjectResponse.builder()
                .contentType("application/pdf")
                .metadata(Map.of("original-filename", "label.pdf"))
                .contentLength(7L)
                .build();
        when(s3Client.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class)))
                .thenAnswer(invocation -> ResponseBytes.fromByteArray(response, "content".getBytes()));

        DocumentDownloadResponse download = service.downloadForOrder(8L, "label.pdf");

        assertThat(download.getFileName()).isEqualTo("label.pdf");
        assertThat(download.getContent()).isEqualTo("content".getBytes());
    }
}

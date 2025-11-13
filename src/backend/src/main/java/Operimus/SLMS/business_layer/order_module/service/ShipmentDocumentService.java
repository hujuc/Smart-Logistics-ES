package Operimus.SLMS.business_layer.order_module.service;

import Operimus.SLMS.business_layer.order_module.dto.DocumentDownloadResponse;
import Operimus.SLMS.business_layer.order_module.dto.ShipmentDocumentResponse;
import Operimus.SLMS.business_layer.order_module.entities.Order;
import Operimus.SLMS.business_layer.order_module.repository.OrderRepository;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Shipment;
import Operimus.SLMS.business_layer.rate_carrier_module.repository.ShipmentRepository;
import Operimus.SLMS.business_layer.shared.exception.DocumentStorageException;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class ShipmentDocumentService {

    private final S3Client s3Client;
    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final String bucketName;
    private final URI endpointUri;
    private final AtomicBoolean bucketReady = new AtomicBoolean(false);
    private final Object bucketLock = new Object();

    public ShipmentDocumentService(
            S3Client s3Client,
            ShipmentRepository shipmentRepository,
            OrderRepository orderRepository,
            @Value("${aws.s3.bucket}") String bucketName,
            @Value("${aws.s3.endpoint}") String endpoint) {
        this.s3Client = s3Client;
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.bucketName = bucketName;
        this.endpointUri = URI.create(endpoint);
    }

    public ShipmentDocumentResponse saveDocument(
            Long shipmentId,
            Long orderId,
            String documentId,
            String fileName,
            String contentType,
            byte[] content,
            Map<String, String> extraMetadata) {

        Shipment shipment = findShipmentOrThrow(shipmentId);
        if (orderId != null) {
            findOrderOrThrow(orderId);
        }

        ensureBucketExists();

        String safeFileName = sanitizeFilename(fileName);
        String safeDocumentId = sanitizeFilename(documentId);
        String objectKey = buildObjectKey(shipmentId, orderId, safeDocumentId);
        String resolvedContentType = StringUtils.hasText(contentType) ? contentType : "application/octet-stream";

        Map<String, String> metadata = buildObjectMetadata(safeFileName, shipmentId, orderId, extraMetadata);

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .contentType(resolvedContentType)
                            .metadata(metadata)
                            .build(),
                    RequestBody.fromBytes(content));
        } catch (S3Exception ex) {
            throw new DocumentStorageException("Erro ao guardar o documento de envio no bucket '%s'.".formatted(bucketName), ex);
        }

        return ShipmentDocumentResponse.builder()
                .shipmentId(shipment.getId())
                .orderId(orderId)
                .documentId(safeDocumentId)
                .objectKey(objectKey)
                .fileName(safeFileName)
                .contentType(resolvedContentType)
                .size(content.length)
                .url(buildObjectUrl(objectKey))
                .carrierCode(Optional.ofNullable(metadata.get("carrier-code")).orElse(null))
                .serviceLevel(Optional.ofNullable(metadata.get("service-level")).orElse(null))
                .build();
    }

    public List<ShipmentDocumentResponse> listByShipment(Long shipmentId) {
        findShipmentOrThrow(shipmentId);
        ensureBucketExists();

        String prefix = buildShipmentPrefix(shipmentId);
        return listObjects(prefix, shipmentId, null);
    }

    public List<ShipmentDocumentResponse> listByOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        if (order.getShipmentId() == null) {
            return Collections.emptyList();
        }
        ensureBucketExists();
        String prefix = buildOrderPrefix(order.getShipmentId(), orderId);
        return listObjects(prefix, order.getShipmentId(), orderId);
    }

    public DocumentDownloadResponse downloadForOrder(Long orderId, String documentId) {
        Order order = findOrderOrThrow(orderId);
        if (order.getShipmentId() == null) {
            throw new ResourceNotFoundException(
                    "A encomenda %d não tem documentos de envio associados.".formatted(orderId));
        }
        return download(order.getShipmentId(), orderId, documentId);
    }

    public DocumentDownloadResponse downloadForShipment(Long shipmentId, String documentId) {
        findShipmentOrThrow(shipmentId);
        return download(shipmentId, null, documentId);
    }

    public void deleteDocument(Long shipmentId, Long orderId, String documentId) {
        findShipmentOrThrow(shipmentId);
        ensureBucketExists();
        String objectKey = buildObjectKey(shipmentId, orderId, documentId);
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build());
        } catch (NoSuchKeyException ex) {
            throw documentNotFound(shipmentId, orderId, documentId);
        } catch (S3Exception ex) {
            throw new DocumentStorageException("Erro ao eliminar o documento '%s'.".formatted(documentId), ex);
        }
    }

    private DocumentDownloadResponse download(Long shipmentId, Long orderId, String documentId) {
        ensureBucketExists();
        String objectKey = buildObjectKey(shipmentId, orderId, documentId);
        try {
            var responseBytes = s3Client.getObject(
                    GetObjectRequest.builder().bucket(bucketName).key(objectKey).build(),
                    ResponseTransformer.toBytes());

            String contentType = Optional.ofNullable(responseBytes.response().contentType())
                    .orElse("application/octet-stream");
            String fileName = Optional.ofNullable(responseBytes.response().metadata().get("original-filename"))
                    .orElse(documentId);

            return DocumentDownloadResponse.builder()
                    .fileName(fileName)
                    .contentType(contentType)
                    .size(responseBytes.asByteArray().length)
                    .content(responseBytes.asByteArray())
                    .build();
        } catch (NoSuchKeyException ex) {
            throw documentNotFound(shipmentId, orderId, documentId);
        } catch (S3Exception ex) {
            throw new DocumentStorageException("Erro ao descarregar documento de envio '%s'.".formatted(documentId), ex);
        }
    }

    private List<ShipmentDocumentResponse> listObjects(String prefix, Long shipmentId, Long orderIdFilter) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        List<ShipmentDocumentResponse> documents = new ArrayList<>();
        try {
            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            for (S3Object objectSummary : response.contents()) {
                String objectKey = objectSummary.key();
                HeadObjectResponse head = fetchHeadObject(objectKey);
                documents.add(buildResponseFromObject(shipmentId, orderIdFilter, objectKey, head, objectSummary.size()));
            }
        } catch (S3Exception ex) {
            throw new DocumentStorageException("Erro ao listar documentos de envio para o shipment '%s'.".formatted(prefix), ex);
        }
        return documents;
    }

    private ShipmentDocumentResponse buildResponseFromObject(
            Long shipmentId, Long orderIdFilter, String objectKey, HeadObjectResponse head, long size) {
        Long orderId = orderIdFilter;
        if (orderId == null) {
            orderId = extractOrderId(objectKey);
        }
        String documentId = extractDocumentId(objectKey);

        return ShipmentDocumentResponse.builder()
                .shipmentId(shipmentId)
                .orderId(orderId)
                .documentId(documentId)
                .objectKey(objectKey)
                .fileName(Optional.ofNullable(head.metadata().get("original-filename")).orElse(documentId))
                .contentType(Optional.ofNullable(head.contentType()).orElse("application/octet-stream"))
                .size(size)
                .url(buildObjectUrl(objectKey))
                .carrierCode(head.metadata().get("carrier-code"))
                .serviceLevel(head.metadata().get("service-level"))
                .build();
    }

    private void ensureBucketExists() {
        if (bucketReady.get()) {
            return;
        }
        synchronized (bucketLock) {
            if (bucketReady.get()) {
                return;
            }
            try {
                s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            } catch (NoSuchBucketException ex) {
                createBucket();
            } catch (S3Exception ex) {
                throw new DocumentStorageException(
                        "Erro ao verificar a existência do bucket '%s'.".formatted(bucketName), ex);
            }
            bucketReady.set(true);
        }
    }

    private void createBucket() {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        } catch (S3Exception ex) {
            if (ex.statusCode() != 409) {
                throw new DocumentStorageException(
                        "Erro ao criar o bucket '%s'.".formatted(bucketName), ex);
            }
        }
    }

    private Shipment findShipmentOrThrow(Long shipmentId) {
        return shipmentRepository
                .findById(shipmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shipment %d não encontrado.".formatted(shipmentId)));
    }

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Encomenda %d não encontrada.".formatted(orderId)));
    }

    private HeadObjectResponse fetchHeadObject(String objectKey) {
        try {
            return s3Client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(objectKey).build());
        } catch (NoSuchKeyException ex) {
            throw new ResourceNotFoundException("Documento '%s' não encontrado.".formatted(objectKey));
        } catch (S3Exception ex) {
            throw new DocumentStorageException("Erro ao obter metadados do documento.", ex);
        }
    }

    private Map<String, String> buildObjectMetadata(
            String fileName, Long shipmentId, Long orderId, Map<String, String> extraMetadata) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("original-filename", fileName);
        metadata.put("shipment-id", shipmentId != null ? shipmentId.toString() : "");
        if (orderId != null) {
            metadata.put("order-id", orderId.toString());
        }
        metadata.put("uploaded-at", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC)));
        if (extraMetadata != null) {
            extraMetadata.forEach((key, value) -> {
                if (StringUtils.hasText(key) && value != null) {
                    metadata.put(key.toLowerCase(), value);
                }
            });
        }
        return metadata;
    }

    private String buildShipmentPrefix(Long shipmentId) {
        return "shipments/%d/".formatted(shipmentId);
    }

    private String buildOrderPrefix(Long shipmentId, Long orderId) {
        if (orderId == null) {
            return buildShipmentPrefix(shipmentId);
        }
        return buildShipmentPrefix(shipmentId) + "orders/%d/".formatted(orderId);
    }

    private String buildObjectKey(Long shipmentId, Long orderId, String documentId) {
        validateDocumentId(documentId);
        return buildOrderPrefix(shipmentId, orderId) + documentId;
    }

    private void validateDocumentId(String documentId) {
        if (!StringUtils.hasText(documentId)) {
            throw new DocumentStorageException("O identificador do documento é inválido.");
        }
        if (documentId.contains("..") || documentId.contains("/") || documentId.contains("\\")) {
            throw new DocumentStorageException("O identificador do documento contém caracteres inválidos.");
        }
    }

    private String sanitizeFilename(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "document";
        }
        String filename = originalFilename.replace("\\", "/");
        int lastSeparator = filename.lastIndexOf('/') + 1;
        filename = filename.substring(lastSeparator);
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String buildObjectUrl(String key) {
        String base = endpointUri.toString();
        if (!base.endsWith("/")) {
            base = base + "/";
        }
        return base + bucketName + "/" + key;
    }

    private String extractDocumentId(String objectKey) {
        return objectKey.substring(objectKey.lastIndexOf('/') + 1);
    }

    private Long extractOrderId(String objectKey) {
        String[] parts = objectKey.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("orders".equals(parts[i]) && i + 1 < parts.length) {
                try {
                    return Long.parseLong(parts[i + 1]);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private ResourceNotFoundException documentNotFound(Long shipmentId, Long orderId, String documentId) {
        if (orderId != null) {
            return new ResourceNotFoundException(
                    "Documento '%s' não encontrado para a encomenda %d do shipment %d."
                            .formatted(documentId, orderId, shipmentId));
        }
        return new ResourceNotFoundException(
                "Documento '%s' não encontrado para o shipment %d.".formatted(documentId, shipmentId));
    }
}

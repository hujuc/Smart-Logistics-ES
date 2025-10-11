package Operimus.SLMS.business_layer.order_shipment_module.service;

import Operimus.SLMS.business_layer.order_shipment_module.dto.DocumentUploadResponse;
import Operimus.SLMS.business_layer.order_shipment_module.entities.ShipmentOrder;
import Operimus.SLMS.business_layer.order_shipment_module.repository.ShipmentOrderRepository;
import Operimus.SLMS.business_layer.shared.exception.DocumentStorageException;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class OrderDocumentService {

    private final S3Client s3Client;
    private final ShipmentOrderRepository shipmentOrderRepository;
    private final String bucketName;
    private final URI endpointUri;
    private final AtomicBoolean bucketReady = new AtomicBoolean(false);
    private final Object bucketLock = new Object();

    public OrderDocumentService(
            S3Client s3Client,
            ShipmentOrderRepository shipmentOrderRepository,
            @Value("${aws.s3.bucket}") String bucketName,
            @Value("${aws.s3.endpoint}") String endpoint) {
        this.s3Client = s3Client;
        this.shipmentOrderRepository = shipmentOrderRepository;
        this.bucketName = bucketName;
        this.endpointUri = URI.create(endpoint);
    }

    public DocumentUploadResponse uploadDocument(Long orderId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DocumentStorageException("O ficheiro enviado está vazio.");
        }

        ShipmentOrder order = shipmentOrderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order %d not found".formatted(orderId)));

        ensureBucketExists();

        String cleanedFileName = sanitizeFilename(file.getOriginalFilename());
        String objectKey = "orders/%d/%s_%s".formatted(orderId, UUID.randomUUID(), cleanedFileName);
        String contentType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (IOException e) {
            throw new DocumentStorageException("Não foi possível ler o ficheiro enviado.", e);
        } catch (S3Exception e) {
            throw new DocumentStorageException("Erro ao guardar o ficheiro no bucket '%s'.".formatted(bucketName), e);
        }

        return DocumentUploadResponse.builder()
                .orderId(order.getId())
                .objectKey(objectKey)
                .fileName(cleanedFileName)
                .contentType(contentType)
                .size(file.getSize())
                .url(buildObjectUrl(objectKey))
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
            } catch (NoSuchBucketException e) {
                createBucket();
            } catch (S3Exception e) {
                throw new DocumentStorageException(
                        "Erro ao verificar a existência do bucket '%s'.".formatted(bucketName), e);
            }

            bucketReady.set(true);
        }
    }

    private void createBucket() {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        } catch (S3Exception e) {
            if (e.statusCode() != 409) {
                throw new DocumentStorageException(
                        "Erro ao criar o bucket '%s'.".formatted(bucketName), e);
            }
        }
    }

    private String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "document";
        }

        String filename = originalFilename.replace("\\", "/");
        int lastSeparator = filename.lastIndexOf('/');
        if (lastSeparator >= 0) {
            filename = filename.substring(lastSeparator + 1);
        }

        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String buildObjectUrl(String key) {
        String base = endpointUri.toString();
        if (!base.endsWith("/")) {
            base = base + "/";
        }
        return base + bucketName + "/" + key;
    }
}

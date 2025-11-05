package Operimus.SLMS.business_layer.order_module.service;

import Operimus.SLMS.business_layer.order_module.dto.DocumentDownloadResponse;
import Operimus.SLMS.business_layer.order_module.dto.DocumentUploadResponse;
import Operimus.SLMS.business_layer.order_module.entities.Order;
import Operimus.SLMS.business_layer.order_module.repository.OrderRepository;
import Operimus.SLMS.business_layer.shared.exception.DocumentStorageException;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class OrderDocumentService {

    private final S3Client s3Client;
    private final OrderRepository orderRepository;

    private final String bucketName;
    private final URI endpointUri;
    private final AtomicBoolean bucketReady = new AtomicBoolean(false);
    private final Object bucketLock = new Object();

    public OrderDocumentService(
            S3Client s3Client,
            OrderRepository orderRepository,
            @Value("${aws.s3.bucket}") String bucketName,
            @Value("${aws.s3.endpoint}") String endpoint) {
        this.s3Client = s3Client;
        this.orderRepository = orderRepository;
        this.bucketName = bucketName;
        this.endpointUri = URI.create(endpoint);
    }

    public DocumentUploadResponse uploadDocument(Long orderId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DocumentStorageException("O ficheiro enviado está vazio.");
        }

        Order order = findOrderOrThrow(orderId);

        ensureBucketExists();

        String cleanedFileName = sanitizeFilename(file.getOriginalFilename());
        String documentId = cleanedFileName;
        String objectKey = buildObjectKey(orderId, documentId);
        String contentType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    .metadata(buildObjectMetadata(cleanedFileName))
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (IOException e) {
            throw new DocumentStorageException("Não foi possível ler o ficheiro enviado.", e);
        } catch (S3Exception e) {
            throw new DocumentStorageException("Erro ao guardar o ficheiro no bucket '%s'.".formatted(bucketName), e);
        }

        return DocumentUploadResponse.builder()
                .orderId(order.getId())
                .documentId(documentId)
                .objectKey(objectKey)
                .fileName(cleanedFileName)
                .contentType(contentType)
                .size(file.getSize())
                .url(buildObjectUrl(objectKey))
                .build();
    }

    public List<DocumentUploadResponse> listDocuments(Long orderId) {
        findOrderOrThrow(orderId);
        ensureBucketExists();

        String prefix = buildOrderPrefix(orderId);
        ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix).build();

        ListObjectsV2Response response;
        try {
            response = s3Client.listObjectsV2(request);
        } catch (S3Exception e) {
            throw new DocumentStorageException(
                    "Erro ao listar documentos no bucket '%s'.".formatted(bucketName), e);
        }

        List<DocumentUploadResponse> documents = new ArrayList<>();
        for (S3Object objectSummary : response.contents()) {
            String objectKey = objectSummary.key();
            String fileName = extractFileName(objectKey);
            String documentId = extractDocumentId(objectKey);
            HeadObjectResponse headResponse = fetchHeadObject(objectKey);
            String contentType = Optional.ofNullable(headResponse.contentType()).orElse("application/octet-stream");
            long size = objectSummary.size();

            documents.add(DocumentUploadResponse.builder()
                    .orderId(orderId)
                    .documentId(documentId)
                    .objectKey(objectKey)
                    .fileName(fileName)
                    .contentType(contentType)
                    .size(size)
                    .url(buildObjectUrl(objectKey))
                    .build());
        }

        return documents;
    }

    public DocumentUploadResponse getDocumentMetadata(Long orderId, String documentId) {
        findOrderOrThrow(orderId);
        ensureBucketExists();

        String objectKey = buildObjectKey(orderId, documentId);
        HeadObjectResponse headObjectResponse = fetchHeadObject(objectKey);

        return DocumentUploadResponse.builder()
                .orderId(orderId)
                .documentId(documentId)
                .objectKey(objectKey)
                .fileName(extractFileName(documentId, headObjectResponse))
                .contentType(Optional.ofNullable(headObjectResponse.contentType()).orElse("application/octet-stream"))
                .size(headObjectResponse.contentLength())
                .url(buildObjectUrl(objectKey))
                .build();
    }

    public DocumentDownloadResponse downloadDocument(Long orderId, String documentId) {
        findOrderOrThrow(orderId);
        ensureBucketExists();

        String objectKey = buildObjectKey(orderId, documentId);
        try {
            var responseBytes = s3Client.getObject(
                    GetObjectRequest.builder().bucket(bucketName).key(objectKey).build(),
                    ResponseTransformer.toBytes());

            String contentType = Optional.ofNullable(responseBytes.response().contentType())
                    .orElse("application/octet-stream");
            String fileName = extractFileName(documentId, responseBytes.response());

            return DocumentDownloadResponse.builder()
                    .fileName(fileName)
                    .contentType(contentType)
                    .size(responseBytes.response().contentLength())
                    .content(responseBytes.asByteArray())
                    .build();
        } catch (NoSuchKeyException e) {
            throw documentNotFound(orderId, documentId);
        } catch (S3Exception e) {
            throw new DocumentStorageException(
                    "Erro ao descarregar documento '%s'.".formatted(documentId), e);
        }
    }

    public DocumentUploadResponse updateDocument(Long orderId, String documentId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DocumentStorageException("O ficheiro enviado está vazio.");
        }

        findOrderOrThrow(orderId);
        ensureBucketExists();

        String objectKey = buildObjectKey(orderId, documentId);
        HeadObjectResponse existing = fetchHeadObject(objectKey);
        String fileName = sanitizeFilename(file.getOriginalFilename());
        if (fileName.isBlank()) {
            fileName = extractFileName(documentId, existing);
        }

        String contentType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    .metadata(buildObjectMetadata(fileName, documentId))
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (NoSuchKeyException e) {
            throw documentNotFound(orderId, documentId);
        } catch (IOException e) {
            throw new DocumentStorageException("Não foi possível ler o ficheiro enviado.", e);
        } catch (S3Exception e) {
            throw new DocumentStorageException(
                    "Erro ao atualizar o ficheiro no bucket '%s'.".formatted(bucketName), e);
        }

        return DocumentUploadResponse.builder()
                .orderId(orderId)
                .documentId(documentId)
                .objectKey(objectKey)
                .fileName(fileName)
                .contentType(contentType)
                .size(file.getSize())
                .url(buildObjectUrl(objectKey))
                .build();
    }

    public void deleteDocument(Long orderId, String documentId) {
        findOrderOrThrow(orderId);
        ensureBucketExists();

        String objectKey = buildObjectKey(orderId, documentId);
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build());
        } catch (NoSuchKeyException e) {
            throw documentNotFound(orderId, documentId);
        } catch (S3Exception e) {
            throw new DocumentStorageException(
                    "Erro ao eliminar o documento '%s' no bucket '%s'.".formatted(documentId, bucketName), e);
        }
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

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order %d not found".formatted(orderId)));
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

    private String buildOrderPrefix(Long orderId) {
        return "orders/%d/".formatted(orderId);
    }

    private String buildObjectKey(Long orderId, String documentId) {
        validateDocumentId(documentId);
        return buildOrderPrefix(orderId) + documentId;
    }

    private void validateDocumentId(String documentId) {
        if (documentId == null || documentId.isBlank()) {
            throw new DocumentStorageException("O identificador do documento é inválido.");
        }
        if (documentId.contains("..") || documentId.contains("/") || documentId.contains("\\")) {
            throw new DocumentStorageException("O identificador do documento contém caracteres inválidos.");
        }
    }

    private String extractFileName(String objectKey) {
        return objectKey.substring(objectKey.lastIndexOf('/') + 1);
    }

    private String extractFileName(String documentId, HeadObjectResponse headObjectResponse) {
        return Optional.ofNullable(headObjectResponse.metadata().get("original-filename"))
                .orElse(documentId);
    }

    private String extractFileName(String documentId, software.amazon.awssdk.services.s3.model.GetObjectResponse response) {
        return Optional.ofNullable(response.metadata().get("original-filename"))
                .orElse(documentId);
    }

    private String extractDocumentId(String objectKey) {
        return objectKey.substring(objectKey.lastIndexOf('/') + 1);
    }

    private HeadObjectResponse fetchHeadObject(String objectKey) {
        try {
            return s3Client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(objectKey).build());
        } catch (NoSuchKeyException e) {
            throw new ResourceNotFoundException("Documento '%s' não encontrado.".formatted(objectKey));
        } catch (S3Exception e) {
            throw new DocumentStorageException("Erro ao obter metadados do documento.", e);
        }
    }

    private HashMap<String, String> buildObjectMetadata(String fileName) {
        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("original-filename", fileName);
        metadata.put("uploaded-at", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC)));
        return metadata;
    }

    private HashMap<String, String> buildObjectMetadata(String fileName, String documentId) {
        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("original-filename", fileName);
        metadata.put("display-name", fileName);
        metadata.put("document-id", documentId);
        metadata.put("uploaded-at", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC)));
        return metadata;
    }

    private ResourceNotFoundException documentNotFound(Long orderId, String documentId) {
        return new ResourceNotFoundException(
                "Documento '%s' não encontrado para a encomenda %d.".formatted(documentId, orderId));
    }
}

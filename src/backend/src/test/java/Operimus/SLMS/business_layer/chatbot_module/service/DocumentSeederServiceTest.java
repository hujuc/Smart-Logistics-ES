package Operimus.SLMS.business_layer.chatbot_module.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Operimus.SLMS.business_layer.chatbot_module.dto.DocumentDto;
import Operimus.SLMS.business_layer.chatbot_module.dto.UpsertDocumentRequest;
import Operimus.SLMS.business_layer.chatbot_module.repository.AssistantDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

/**
 * Tests for DocumentSeederService.
 * Verifies knowledge base document seeding functionality.
 */
@ExtendWith(MockitoExtension.class)
class DocumentSeederServiceTest {

    @Mock
    private DocumentManagementService documentManagementService;

    @Mock
    private AssistantDocumentRepository documentRepository;

    private DocumentSeederService documentSeederService;

    @BeforeEach
    void setUp() {
        // Mock repository to return 0 documents (empty database) by default
        when(documentRepository.count()).thenReturn(0L);
        documentSeederService = new DocumentSeederService(documentManagementService, documentRepository);
    }

    @Test
    void seedInitialDocuments_shouldSeedAllDocuments() {
        // When
        documentSeederService.seedInitialDocuments();

        // Then - Verify that upsertDocument was called 39 times (one for each document)
        ArgumentCaptor<UpsertDocumentRequest> captor = ArgumentCaptor.forClass(UpsertDocumentRequest.class);
        verify(documentManagementService, times(39)).upsertDocument(captor.capture());

        // Verify we captured 39 documents
        List<UpsertDocumentRequest> seededDocuments = captor.getAllValues();
        assertThat(seededDocuments).hasSize(39);

        // Verify all documents have required fields
        for (UpsertDocumentRequest doc : seededDocuments) {
            assertThat(doc.title()).isNotBlank();
            assertThat(doc.content()).isNotBlank();
            assertThat(doc.locale()).isEqualTo("en-US");
            assertThat(doc.roles()).isNotNull();
            assertThat(doc.frontends()).isNotNull();
            assertThat(doc.routes()).isNotNull();
        }
    }

    @Test
    void seedInitialDocuments_shouldIncludeSystemOverviewDocuments() {
        // When
        documentSeederService.seedInitialDocuments();

        // Then
        ArgumentCaptor<UpsertDocumentRequest> captor = ArgumentCaptor.forClass(UpsertDocumentRequest.class);
        verify(documentManagementService, times(39)).upsertDocument(captor.capture());

        List<String> titles = captor.getAllValues().stream()
                .map(UpsertDocumentRequest::title)
                .toList();

        // Verify System Overview documents exist
        assertThat(titles).contains(
                "What is SLMS - Smart Logistics Management System",
                "User Roles and Permissions in SLMS",
                "System Capabilities Overview - What SLMS Can Do"
        );
    }

    @Test
    void seedInitialDocuments_shouldIncludeGettingStartedDocuments() {
        // When
        documentSeederService.seedInitialDocuments();

        // Then
        ArgumentCaptor<UpsertDocumentRequest> captor = ArgumentCaptor.forClass(UpsertDocumentRequest.class);
        verify(documentManagementService, times(39)).upsertDocument(captor.capture());

        List<String> titles = captor.getAllValues().stream()
                .map(UpsertDocumentRequest::title)
                .toList();

        // Verify Getting Started documents exist
        assertThat(titles).contains(
                "Getting Started Guide for Customers",
                "Getting Started Guide for Drivers",
                "Getting Started Guide for Warehouse Staff",
                "Getting Started Guide for Customer Service Representatives",
                "Getting Started Guide for Logistics Managers and Administrators"
        );
    }

    @Test
    void seedInitialDocuments_shouldIncludeOrderTrackingDocuments() {
        // When
        documentSeederService.seedInitialDocuments();

        // Then
        ArgumentCaptor<UpsertDocumentRequest> captor = ArgumentCaptor.forClass(UpsertDocumentRequest.class);
        verify(documentManagementService, times(39)).upsertDocument(captor.capture());

        List<String> titles = captor.getAllValues().stream()
                .map(UpsertDocumentRequest::title)
                .toList();

        // Verify Order Tracking documents exist
        assertThat(titles).contains(
                "How to Track Your Orders in SLMS",
                "Understanding Order Statuses in SLMS",
                "Viewing and Using Order History"
        );
    }

    @Test
    void seedInitialDocuments_shouldIncludeDeliveryExceptionDocuments() {
        // When
        documentSeederService.seedInitialDocuments();

        // Then
        ArgumentCaptor<UpsertDocumentRequest> captor = ArgumentCaptor.forClass(UpsertDocumentRequest.class);
        verify(documentManagementService, times(39)).upsertDocument(captor.capture());

        List<String> titles = captor.getAllValues().stream()
                .map(UpsertDocumentRequest::title)
                .toList();

        // Verify Delivery Exception documents exist
        assertThat(titles).contains(
                "How to Report a Delivery Issue or Problem",
                "Types of Delivery Exceptions in SLMS",
                "How Driver Delivery Confirmation Works"
        );
    }

    @Test
    void seedInitialDocuments_shouldIncludeWarehouseOperationsDocuments() {
        // When
        documentSeederService.seedInitialDocuments();

        // Then
        ArgumentCaptor<UpsertDocumentRequest> captor = ArgumentCaptor.forClass(UpsertDocumentRequest.class);
        verify(documentManagementService, times(39)).upsertDocument(captor.capture());

        List<String> titles = captor.getAllValues().stream()
                .map(UpsertDocumentRequest::title)
                .toList();

        // Verify Warehouse Operations documents exist
        assertThat(titles).contains(
                "Warehouse Order Management - Processing and Fulfillment",
                "Document Management for Warehouse Operations",
                "Warehouse Status Updates - Keeping Orders Current"
        );
    }

    @Test
    void seedInitialDocuments_shouldIncludeAdministrationDocuments() {
        // When
        documentSeederService.seedInitialDocuments();

        // Then
        ArgumentCaptor<UpsertDocumentRequest> captor = ArgumentCaptor.forClass(UpsertDocumentRequest.class);
        verify(documentManagementService, times(39)).upsertDocument(captor.capture());

        List<String> titles = captor.getAllValues().stream()
                .map(UpsertDocumentRequest::title)
                .toList();

        // Verify Administration documents exist
        assertThat(titles).contains(
                "User Management for Administrators",
                "Warehouse Administration and Configuration",
                "Dashboard Statistics and Operational Reporting"
        );
    }

    @Test
    void seedInitialDocuments_shouldIncludeCustomerServiceDocuments() {
        // When
        documentSeederService.seedInitialDocuments();

        // Then
        ArgumentCaptor<UpsertDocumentRequest> captor = ArgumentCaptor.forClass(UpsertDocumentRequest.class);
        verify(documentManagementService, times(39)).upsertDocument(captor.capture());

        List<String> titles = captor.getAllValues().stream()
                .map(UpsertDocumentRequest::title)
                .toList();

        // Verify Customer Service documents exist
        assertThat(titles).contains(
                "Using the Customer Service Dashboard",
                "Understanding Exception Priority and Severity",
                "Filtering and Searching Exceptions Effectively"
        );
    }

    @Test
    void seedInitialDocuments_shouldIncludeTroubleshootingDocuments() {
        // When
        documentSeederService.seedInitialDocuments();

        // Then
        ArgumentCaptor<UpsertDocumentRequest> captor = ArgumentCaptor.forClass(UpsertDocumentRequest.class);
        verify(documentManagementService, times(39)).upsertDocument(captor.capture());

        List<String> titles = captor.getAllValues().stream()
                .map(UpsertDocumentRequest::title)
                .toList();

        // Verify Troubleshooting & FAQ documents exist
        assertThat(titles).contains(
                "Frequently Asked Questions for Customers",
                "Frequently Asked Questions for Drivers",
                "Frequently Asked Questions for Warehouse Staff",
                "Frequently Asked Questions for Customer Service Representatives"
        );
    }

    @Test
    void seedInitialDocuments_shouldSetCorrectRoleFiltersForCustomerDocuments() {
        // When
        documentSeederService.seedInitialDocuments();

        // Then
        ArgumentCaptor<UpsertDocumentRequest> captor = ArgumentCaptor.forClass(UpsertDocumentRequest.class);
        verify(documentManagementService, times(39)).upsertDocument(captor.capture());

        // Find customer-specific document
        UpsertDocumentRequest customerDoc = captor.getAllValues().stream()
                .filter(doc -> doc.title().equals("Getting Started Guide for Customers"))
                .findFirst()
                .orElseThrow();

        // Verify it includes CUSTOMER and ANONYMOUS roles
        assertThat(customerDoc.roles()).contains("CUSTOMER", "ANONYMOUS");
    }

    @Test
    void seedInitialDocuments_shouldSetCorrectRoleFiltersForDriverDocuments() {
        // When
        documentSeederService.seedInitialDocuments();

        // Then
        ArgumentCaptor<UpsertDocumentRequest> captor = ArgumentCaptor.forClass(UpsertDocumentRequest.class);
        verify(documentManagementService, times(39)).upsertDocument(captor.capture());

        // Find driver-specific document
        UpsertDocumentRequest driverDoc = captor.getAllValues().stream()
                .filter(doc -> doc.title().equals("Getting Started Guide for Drivers"))
                .findFirst()
                .orElseThrow();

        // Verify it includes DRIVER role
        assertThat(driverDoc.roles()).contains("DRIVER");
    }

    @Test
    void seedInitialDocuments_shouldSetCorrectRoleFiltersForWarehouseDocuments() {
        // When
        documentSeederService.seedInitialDocuments();

        // Then
        ArgumentCaptor<UpsertDocumentRequest> captor = ArgumentCaptor.forClass(UpsertDocumentRequest.class);
        verify(documentManagementService, times(39)).upsertDocument(captor.capture());

        // Find warehouse-specific document
        UpsertDocumentRequest warehouseDoc = captor.getAllValues().stream()
                .filter(doc -> doc.title().equals("Getting Started Guide for Warehouse Staff"))
                .findFirst()
                .orElseThrow();

        // Verify it includes WAREHOUSE_STAFF role
        assertThat(warehouseDoc.roles()).contains("WAREHOUSE_STAFF");
    }

    @Test
    void seedInitialDocuments_shouldSetCorrectRoleFiltersForCustomerServiceDocuments() {
        // When
        documentSeederService.seedInitialDocuments();

        // Then
        ArgumentCaptor<UpsertDocumentRequest> captor = ArgumentCaptor.forClass(UpsertDocumentRequest.class);
        verify(documentManagementService, times(39)).upsertDocument(captor.capture());

        // Find customer service-specific document
        UpsertDocumentRequest csDoc = captor.getAllValues().stream()
                .filter(doc -> doc.title().equals("Getting Started Guide for Customer Service Representatives"))
                .findFirst()
                .orElseThrow();

        // Verify it includes CUSTOMER_SERVICE_REP role
        assertThat(csDoc.roles()).contains("CUSTOMER_SERVICE_REP");
    }

    @Test
    void seedInitialDocuments_shouldSetCorrectRoleFiltersForAdminDocuments() {
        // When
        documentSeederService.seedInitialDocuments();

        // Then
        ArgumentCaptor<UpsertDocumentRequest> captor = ArgumentCaptor.forClass(UpsertDocumentRequest.class);
        verify(documentManagementService, times(39)).upsertDocument(captor.capture());

        // Find admin-specific document
        UpsertDocumentRequest adminDoc = captor.getAllValues().stream()
                .filter(doc -> doc.title().equals("Getting Started Guide for Logistics Managers and Administrators"))
                .findFirst()
                .orElseThrow();

        // Verify it includes ADMIN role
        assertThat(adminDoc.roles()).contains("ADMIN");
    }

    @Test
    void seedInitialDocuments_shouldSetCorrectFrontendFilters() {
        // When
        documentSeederService.seedInitialDocuments();

        // Then
        ArgumentCaptor<UpsertDocumentRequest> captor = ArgumentCaptor.forClass(UpsertDocumentRequest.class);
        verify(documentManagementService, times(39)).upsertDocument(captor.capture());

        // Find customer portal document
        UpsertDocumentRequest customerDoc = captor.getAllValues().stream()
                .filter(doc -> doc.title().equals("Getting Started Guide for Customers"))
                .findFirst()
                .orElseThrow();

        // Verify it includes appropriate frontends
        assertThat(customerDoc.frontends()).contains("landing-page", "customer-portal");

        // Find driver PWA document
        UpsertDocumentRequest driverDoc = captor.getAllValues().stream()
                .filter(doc -> doc.title().equals("Getting Started Guide for Drivers"))
                .findFirst()
                .orElseThrow();

        // Verify it includes driver-pwa frontend
        assertThat(driverDoc.frontends()).contains("driver-pwa");
    }

    @Test
    void seedInitialDocuments_shouldHandleExceptionsGracefully() {
        // Given - One document fails but seeding continues
        DocumentDto mockDto = new DocumentDto(
                "id-1",
                "chunk-1",
                "Test Doc",
                Set.of("CUSTOMER"),
                Set.of("customer-portal"),
                Set.of(),
                "en-US",
                "Test content",
                null,
                "2024-01-01T00:00:00Z"
        );

        when(documentManagementService.upsertDocument(any(UpsertDocumentRequest.class)))
                .thenThrow(new RuntimeException("Pinecone error"))
                .thenReturn(mockDto);  // Subsequent calls succeed

        // When - Should not throw exception
        documentSeederService.seedInitialDocuments();

        // Then - Should attempt to seed all documents despite the failure
        verify(documentManagementService, times(39)).upsertDocument(any(UpsertDocumentRequest.class));
    }

    @Test
    void seedInitialDocuments_shouldSetEnglishLocaleForAllDocuments() {
        // When
        documentSeederService.seedInitialDocuments();

        // Then
        ArgumentCaptor<UpsertDocumentRequest> captor = ArgumentCaptor.forClass(UpsertDocumentRequest.class);
        verify(documentManagementService, times(39)).upsertDocument(captor.capture());

        // Verify all documents have en-US locale
        List<String> locales = captor.getAllValues().stream()
                .map(UpsertDocumentRequest::locale)
                .distinct()
                .toList();

        assertThat(locales).containsExactly("en-US");
    }

    @Test
    void seedInitialDocuments_shouldSetEmptyRoutesForAllDocuments() {
        // When
        documentSeederService.seedInitialDocuments();

        // Then
        ArgumentCaptor<UpsertDocumentRequest> captor = ArgumentCaptor.forClass(UpsertDocumentRequest.class);
        verify(documentManagementService, times(39)).upsertDocument(captor.capture());

        // Verify all documents have empty routes (general knowledge, not route-specific)
        for (UpsertDocumentRequest doc : captor.getAllValues()) {
            assertThat(doc.routes()).isEmpty();
        }
    }

    @Test
    void seedInitialDocuments_shouldSetNullMetadataForAllDocuments() {
        // When
        documentSeederService.seedInitialDocuments();

        // Then
        ArgumentCaptor<UpsertDocumentRequest> captor = ArgumentCaptor.forClass(UpsertDocumentRequest.class);
        verify(documentManagementService, times(39)).upsertDocument(captor.capture());

        // Verify all documents have null metadata (using default metadata structure)
        for (UpsertDocumentRequest doc : captor.getAllValues()) {
            assertThat(doc.metadataJson()).isNull();
        }
    }

    @Test
    void seedInitialDocuments_shouldHaveComprehensiveContentForEachDocument() {
        // When
        documentSeederService.seedInitialDocuments();

        // Then
        ArgumentCaptor<UpsertDocumentRequest> captor = ArgumentCaptor.forClass(UpsertDocumentRequest.class);
        verify(documentManagementService, times(39)).upsertDocument(captor.capture());

        // Verify all documents have substantial content (600-1200 words ~ 4000-8000 characters)
        for (UpsertDocumentRequest doc : captor.getAllValues()) {
            int contentLength = doc.content().length();
            assertThat(contentLength)
                    .withFailMessage("Document '%s' has insufficient content: %d characters", doc.title(), contentLength)
                    .isGreaterThanOrEqualTo(3000); // Minimum 3000 characters for comprehensive content
        }
    }

    @Test
    void seedInitialDocuments_shouldSkipSeedingWhenDocumentsAlreadyExist() {
        // Given: Database already has documents
        when(documentRepository.count()).thenReturn(39L);

        // When
        documentSeederService.seedInitialDocuments();

        // Then: Should not attempt to seed any documents
        verify(documentManagementService, times(0)).upsertDocument(any());
        verify(documentRepository, times(1)).count();
    }
}

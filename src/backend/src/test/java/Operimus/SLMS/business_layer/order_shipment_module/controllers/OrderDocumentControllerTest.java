package Operimus.SLMS.business_layer.order_shipment_module.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Operimus.SLMS.business_layer.order_shipment_module.dto.DocumentDownloadResponse;
import Operimus.SLMS.business_layer.order_shipment_module.dto.DocumentUploadResponse;
import Operimus.SLMS.business_layer.order_shipment_module.service.OrderDocumentService;
import Operimus.SLMS.business_layer.shared.config.S3Config;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.services.s3.S3Client;

@WebMvcTest(
    controllers = OrderDocumentController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = S3Config.class
    )
)
@AutoConfigureMockMvc(addFilters = false)
class OrderDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderDocumentService orderDocumentService;

    @MockBean
    private S3Client s3Client;

    @Test
    void uploadDocumentShouldReturnCreatedResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "invoice.pdf", "application/pdf", "content".getBytes());
        DocumentUploadResponse response = DocumentUploadResponse.builder()
                .orderId(5L)
                .documentId("invoice.pdf")
                .objectKey("orders/5/invoice.pdf")
                .fileName("invoice.pdf")
                .contentType("application/pdf")
                .size(file.getSize())
                .url("http://localhost/bucket/orders/5/invoice.pdf")
                .build();
        when(orderDocumentService.uploadDocument(eq(5L), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/admin/orders/{orderId}/documents", 5L)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/admin/orders/5/documents/invoice.pdf")))
                .andExpect(jsonPath("$.documentId", is("invoice.pdf")))
                .andExpect(jsonPath("$.fileName", is("invoice.pdf")))
                .andExpect(jsonPath("$.contentType", is("application/pdf")));

        verify(orderDocumentService).uploadDocument(eq(5L), any());
    }

    @Test
    void listDocumentsShouldReturnDocuments() throws Exception {
        List<DocumentUploadResponse> documents = List.of(
                DocumentUploadResponse.builder()
                        .orderId(5L)
                        .documentId("doc1.pdf")
                        .fileName("doc1.pdf")
                        .contentType("application/pdf")
                        .size(10L)
                        .url("http://localhost/orders/doc1.pdf")
                        .objectKey("orders/5/doc1.pdf")
                        .build(),
                DocumentUploadResponse.builder()
                        .orderId(5L)
                        .documentId("doc2.png")
                        .fileName("doc2.png")
                        .contentType("image/png")
                        .size(20L)
                        .url("http://localhost/orders/doc2.png")
                        .objectKey("orders/5/doc2.png")
                        .build());
        when(orderDocumentService.listDocuments(5L)).thenReturn(documents);

        mockMvc.perform(get("/api/admin/orders/{orderId}/documents", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].documentId", is("doc1.pdf")))
                .andExpect(jsonPath("$[1].documentId", is("doc2.png")));
    }

    @Test
    void getDocumentShouldReturnMetadata() throws Exception {
        DocumentUploadResponse response = DocumentUploadResponse.builder()
                .orderId(5L)
                .documentId("doc1.pdf")
                .fileName("doc1.pdf")
                .contentType("application/pdf")
                .size(10L)
                .url("http://localhost/orders/doc1.pdf")
                .objectKey("orders/5/doc1.pdf")
                .build();
        when(orderDocumentService.getDocumentMetadata(5L, "doc1.pdf")).thenReturn(response);

        mockMvc.perform(get("/api/admin/orders/{orderId}/documents/{documentId}", 5L, "doc1.pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId", is("doc1.pdf")))
                .andExpect(jsonPath("$.fileName", is("doc1.pdf")));
    }

    @Test
    void downloadDocumentShouldStreamContent() throws Exception {
        DocumentDownloadResponse download = DocumentDownloadResponse.builder()
                .fileName("doc1.pdf")
                .contentType("application/pdf")
                .size(4L)
                .content("data".getBytes())
                .build();
        when(orderDocumentService.downloadDocument(5L, "doc1.pdf")).thenReturn(download);

        mockMvc.perform(get("/api/admin/orders/{orderId}/documents/{documentId}/download", 5L, "doc1.pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("doc1.pdf")))
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes("data".getBytes()));
    }

    @Test
    void updateDocumentShouldReturnUpdatedMetadata() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "doc1.pdf", "application/pdf", "data".getBytes());
        DocumentUploadResponse response = DocumentUploadResponse.builder()
                .orderId(5L)
                .documentId("doc1.pdf")
                .fileName("doc1.pdf")
                .contentType("application/pdf")
                .size(file.getSize())
                .url("http://localhost/orders/doc1.pdf")
                .objectKey("orders/5/doc1.pdf")
                .build();
        when(orderDocumentService.updateDocument(eq(5L), eq("doc1.pdf"), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/admin/orders/{orderId}/documents/{documentId}", 5L, "doc1.pdf")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId", is("doc1.pdf")))
                .andExpect(jsonPath("$.fileName", is("doc1.pdf")));
    }

    @Test
    void deleteDocumentShouldReturnNoContent() throws Exception {
        doNothing().when(orderDocumentService).deleteDocument(5L, "doc1.pdf");

        mockMvc.perform(delete("/api/admin/orders/{orderId}/documents/{documentId}", 5L, "doc1.pdf"))
                .andExpect(status().isNoContent());

        verify(orderDocumentService).deleteDocument(5L, "doc1.pdf");
    }
}

package Operimus.SLMS.business_layer.visibility_module.exceptions.controller;

import Operimus.SLMS.business_layer.visibility_module.exceptions.dto.*;
import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException;
import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.*;
import Operimus.SLMS.business_layer.visibility_module.exceptions.service.DeliveryExceptionService;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DeliveryExceptionController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class DeliveryExceptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeliveryExceptionService deliveryExceptionService;

    private UUID testExceptionId;
    private UUID testCustomerId;
    private UUID testAssignedTo;
    private DeliveryExceptionResponse testExceptionResponse;

    @BeforeEach
    void setUp() {
        testExceptionId = UUID.randomUUID();
        testCustomerId = UUID.randomUUID();
        testAssignedTo = UUID.randomUUID();

        testExceptionResponse = DeliveryExceptionResponse.builder()
                .id(testExceptionId)
                .orderId(1L)
                .type(ExceptionType.DAMAGED)
                .category("PACKAGE_DAMAGE")
                .severity(Severity.HIGH)
                .description("Package arrived damaged")
                .source(Source.CUSTOMER)
                .reportedBy(testCustomerId)
                .reportedAt(LocalDateTime.now())
                .customerReported(true)
                .customerId(testCustomerId)
                .status(ExceptionStatus.OPEN)
                .priority(Priority.HIGH)
                .slaDeadline(LocalDateTime.now().plusHours(24))
                .slaBreached(false)
                .build();
    }

    // ========================================
    // Create Exception Tests
    // ========================================

    @Test
    void whenCreateException_thenReturn201() throws Exception {
        DeliveryExceptionRequest request = DeliveryExceptionRequest.builder()
                .orderId(1L)
                .type(ExceptionType.DAMAGED)
                .category("PACKAGE_DAMAGE")
                .source(Source.CUSTOMER)
                .severity(Severity.HIGH)
                .description("Package arrived damaged")
                .reportedBy(testCustomerId)
                .customerReported(true)
                .customerId(testCustomerId)
                .priority(Priority.HIGH)
                .build();

        when(deliveryExceptionService.create(any(DeliveryExceptionRequest.class)))
                .thenReturn(testExceptionResponse);

        mockMvc.perform(post("/api/admin/delivery-exceptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testExceptionId.toString()))
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.type").value("DAMAGED"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        verify(deliveryExceptionService).create(any(DeliveryExceptionRequest.class));
    }

    @Test
    void whenCreateExceptionWithInvalidData_thenReturn400() throws Exception {
        DeliveryExceptionRequest invalidRequest = DeliveryExceptionRequest.builder()
                // Missing required fields
                .build();

        mockMvc.perform(post("/api/admin/delivery-exceptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(deliveryExceptionService, never()).create(any());
    }

    // ========================================
    // Get Exception by ID Tests
    // ========================================

    @Test
    void whenGetExceptionById_thenReturn200() throws Exception {
        when(deliveryExceptionService.getById(testExceptionId))
                .thenReturn(testExceptionResponse);

        mockMvc.perform(get("/api/admin/delivery-exceptions/{id}", testExceptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testExceptionId.toString()))
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.type").value("DAMAGED"));

        verify(deliveryExceptionService).getById(testExceptionId);
    }

    @Test
    void whenGetExceptionByIdNotFound_thenReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(deliveryExceptionService.getById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Delivery exception not found"));

        mockMvc.perform(get("/api/admin/delivery-exceptions/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(deliveryExceptionService).getById(nonExistentId);
    }

    // ========================================
    // Get All Exceptions Tests
    // ========================================

    @Test
    void whenGetAllExceptionsWithoutFilters_thenReturn200() throws Exception {
        List<DeliveryExceptionResponse> exceptions = Arrays.asList(testExceptionResponse);
        when(deliveryExceptionService.getAll(null, null, null, null))
                .thenReturn(exceptions);

        mockMvc.perform(get("/api/admin/delivery-exceptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testExceptionId.toString()));

        verify(deliveryExceptionService).getAll(null, null, null, null);
    }

    @Test
    void whenGetAllExceptionsWithFilters_thenReturn200() throws Exception {
        List<DeliveryExceptionResponse> exceptions = Arrays.asList(testExceptionResponse);
        when(deliveryExceptionService.getAll(
                ExceptionStatus.OPEN, Priority.HIGH, testAssignedTo, Severity.HIGH))
                .thenReturn(exceptions);

        mockMvc.perform(get("/api/admin/delivery-exceptions")
                        .param("status", "OPEN")
                        .param("priority", "HIGH")
                        .param("assignedTo", testAssignedTo.toString())
                        .param("severity", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(deliveryExceptionService).getAll(
                ExceptionStatus.OPEN, Priority.HIGH, testAssignedTo, Severity.HIGH);
    }

    @Test
    void whenGetAllExceptionsEmpty_thenReturnEmptyList() throws Exception {
        when(deliveryExceptionService.getAll(null, null, null, null))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/admin/delivery-exceptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(deliveryExceptionService).getAll(null, null, null, null);
    }

    // ========================================
    // Update Exception Tests
    // ========================================

    @Test
    void whenUpdateException_thenReturn200() throws Exception {
        DeliveryExceptionUpdateRequest updateRequest = DeliveryExceptionUpdateRequest.builder()
                .status(ExceptionStatus.IN_PROGRESS)
                .assignedTo(testAssignedTo)
                .priority(Priority.CRITICAL)
                .resolutionNotes("Investigating with carrier")
                .build();

        DeliveryExceptionResponse updatedResponse = testExceptionResponse;
        when(deliveryExceptionService.update(eq(testExceptionId), any(DeliveryExceptionUpdateRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/admin/delivery-exceptions/{id}", testExceptionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testExceptionId.toString()));

        verify(deliveryExceptionService).update(eq(testExceptionId), any(DeliveryExceptionUpdateRequest.class));
    }

    @Test
    void whenUpdateExceptionNotFound_thenReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        DeliveryExceptionUpdateRequest updateRequest = DeliveryExceptionUpdateRequest.builder()
                .status(ExceptionStatus.IN_PROGRESS)
                .build();

        when(deliveryExceptionService.update(eq(nonExistentId), any(DeliveryExceptionUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Delivery exception not found"));

        mockMvc.perform(put("/api/admin/delivery-exceptions/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(deliveryExceptionService).update(eq(nonExistentId), any(DeliveryExceptionUpdateRequest.class));
    }

    // ========================================
    // Close Exception Tests
    // ========================================

    @Test
    void whenCloseException_thenReturn200() throws Exception {
        String resolutionNotes = "Issue resolved successfully";

        when(deliveryExceptionService.close(testExceptionId, resolutionNotes))
                .thenReturn(testExceptionResponse);

        mockMvc.perform(post("/api/admin/delivery-exceptions/{id}/close", testExceptionId)
                        .param("resolutionNotes", resolutionNotes))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testExceptionId.toString()));

        verify(deliveryExceptionService).close(testExceptionId, resolutionNotes);
    }

    @Test
    void whenCloseExceptionWithoutNotes_thenReturn200() throws Exception {
        when(deliveryExceptionService.close(testExceptionId, null))
                .thenReturn(testExceptionResponse);

        mockMvc.perform(post("/api/admin/delivery-exceptions/{id}/close", testExceptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testExceptionId.toString()));

        verify(deliveryExceptionService).close(testExceptionId, null);
    }

    @Test
    void whenCloseExceptionNotFound_thenReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        String resolutionNotes = "Resolved";

        when(deliveryExceptionService.close(nonExistentId, resolutionNotes))
                .thenThrow(new ResourceNotFoundException("Delivery exception not found"));

        mockMvc.perform(post("/api/admin/delivery-exceptions/{id}/close", nonExistentId)
                        .param("resolutionNotes", resolutionNotes))
                .andExpect(status().isNotFound());

        verify(deliveryExceptionService).close(nonExistentId, resolutionNotes);
    }

    // ========================================
    // Dashboard Tests
    // ========================================

    @Test
    void whenGetDashboardSummary_thenReturn200() throws Exception {
        ExceptionSummaryResponse summary = ExceptionSummaryResponse.builder()
                .totalExceptions(10L)
                .openExceptions(3L)
                .inProgressExceptions(4L)
                .resolvedToday(2L)
                .highPriorityExceptions(1L)
                .slaBreachedExceptions(0L)
                .averageResolutionTimeHours(24.5)
                .customerSatisfactionRate(4.5)
                .build();

        when(deliveryExceptionService.getDashboardSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/admin/delivery-exceptions/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExceptions").value(10))
                .andExpect(jsonPath("$.openExceptions").value(3))
                .andExpect(jsonPath("$.inProgressExceptions").value(4))
                .andExpect(jsonPath("$.resolvedToday").value(2))
                .andExpect(jsonPath("$.highPriorityExceptions").value(1))
                .andExpect(jsonPath("$.slaBreachedExceptions").value(0))
                .andExpect(jsonPath("$.averageResolutionTimeHours").value(24.5))
                .andExpect(jsonPath("$.customerSatisfactionRate").value(4.5));

        verify(deliveryExceptionService).getDashboardSummary();
    }

    // ========================================
    // Alerts Tests
    // ========================================

    @Test
    void whenGetCriticalAlerts_thenReturn200() throws Exception {
        List<DeliveryExceptionResponse> alerts = Arrays.asList(testExceptionResponse);
        when(deliveryExceptionService.getAlerts()).thenReturn(alerts);

        mockMvc.perform(get("/api/admin/delivery-exceptions/dashboard/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testExceptionId.toString()));

        verify(deliveryExceptionService).getAlerts();
    }

    @Test
    void whenGetCriticalAlertsEmpty_thenReturnEmptyList() throws Exception {
        when(deliveryExceptionService.getAlerts()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/admin/delivery-exceptions/dashboard/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(deliveryExceptionService).getAlerts();
    }

    // ========================================
    // Get By Order ID Tests
    // ========================================

    @Test
    void whenGetExceptionsByOrderId_thenReturn200() throws Exception {
        List<DeliveryExceptionResponse> exceptions = Arrays.asList(testExceptionResponse);
        when(deliveryExceptionService.getByOrderId(1L)).thenReturn(exceptions);

        mockMvc.perform(get("/api/admin/delivery-exceptions/order/{orderId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderId").value(1L));

        verify(deliveryExceptionService).getByOrderId(1L);
    }

    @Test
    void whenGetExceptionsByOrderIdNotFound_thenReturnEmptyList() throws Exception {
        when(deliveryExceptionService.getByOrderId(999L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/admin/delivery-exceptions/order/{orderId}", 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(deliveryExceptionService).getByOrderId(999L);
    }

    // ========================================
    // Get By Assigned CSR Tests
    // ========================================

    @Test
    void whenGetExceptionsByCsrId_thenReturn200() throws Exception {
        List<DeliveryExceptionResponse> exceptions = Arrays.asList(testExceptionResponse);
        when(deliveryExceptionService.getMyCases(testAssignedTo)).thenReturn(exceptions);

        mockMvc.perform(get("/api/admin/delivery-exceptions/assigned/{csrId}", testAssignedTo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testExceptionId.toString()));

        verify(deliveryExceptionService).getMyCases(testAssignedTo);
    }

    @Test
    void whenGetExceptionsByCsrIdNotFound_thenReturnEmptyList() throws Exception {
        UUID csrId = UUID.randomUUID();
        when(deliveryExceptionService.getMyCases(csrId)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/admin/delivery-exceptions/assigned/{csrId}", csrId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(deliveryExceptionService).getMyCases(csrId);
    }

    // ========================================
    // Submit Feedback Tests
    // ========================================

    @Test
    void whenSubmitFeedback_thenReturn200() throws Exception {
        Integer rating = 5;
        String comment = "Excellent service";

        when(deliveryExceptionService.submitFeedback(testExceptionId, rating, comment))
                .thenReturn(testExceptionResponse);

        mockMvc.perform(post("/api/admin/delivery-exceptions/{id}/feedback", testExceptionId)
                        .param("rating", rating.toString())
                        .param("comment", comment))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testExceptionId.toString()));

        verify(deliveryExceptionService).submitFeedback(testExceptionId, rating, comment);
    }

    @Test
    void whenSubmitFeedbackWithoutComment_thenReturn200() throws Exception {
        Integer rating = 4;

        when(deliveryExceptionService.submitFeedback(testExceptionId, rating, null))
                .thenReturn(testExceptionResponse);

        mockMvc.perform(post("/api/admin/delivery-exceptions/{id}/feedback", testExceptionId)
                        .param("rating", rating.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testExceptionId.toString()));

        verify(deliveryExceptionService).submitFeedback(testExceptionId, rating, null);
    }

    @Test
    void whenSubmitFeedbackNotFound_thenReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        Integer rating = 5;
        String comment = "Good";

        when(deliveryExceptionService.submitFeedback(nonExistentId, rating, comment))
                .thenThrow(new ResourceNotFoundException("Delivery exception not found"));

        mockMvc.perform(post("/api/admin/delivery-exceptions/{id}/feedback", nonExistentId)
                        .param("rating", rating.toString())
                        .param("comment", comment))
                .andExpect(status().isNotFound());

        verify(deliveryExceptionService).submitFeedback(nonExistentId, rating, comment);
    }
}

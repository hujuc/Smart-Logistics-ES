package Operimus.SLMS.presentation_layer.admin_console_bff.customer_service_bff;

import Operimus.SLMS.business_layer.visibility_module.exceptions.dto.*;
import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.*;
import Operimus.SLMS.business_layer.visibility_module.exceptions.service.DeliveryExceptionService;
import Operimus.SLMS.business_layer.order_module.dto.OrderResponse;
import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import Operimus.SLMS.business_layer.order_module.service.OrderService;
import Operimus.SLMS.business_layer.order_module.service.OrderEventService;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

@WebMvcTest(controllers = CustomerServiceBffController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class CustomerServiceBffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeliveryExceptionService exceptionService;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderEventService shipmentOrderEventService;

    @MockitoBean
    private KeycloakUserService userService;

    // ========================================
    // Health Check Tests
    // ========================================

    @Test
    void whenHealthCheck_thenReturnStatus() throws Exception {
        mockMvc.perform(get("/api/customer-service/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.service", is("Customer Service BFF")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ========================================
    // Create Exception Tests
    // ========================================

    @Test
    void whenCreateException_thenReturnCreatedException() throws Exception {
        DeliveryExceptionRequest request = createExceptionRequest();
        DeliveryExceptionResponse response = createExceptionResponse(
                UUID.randomUUID(), ExceptionStatus.OPEN);

        when(exceptionService.create(any(DeliveryExceptionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/customer-service/delivery-exceptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/customer-service/delivery-exceptions/")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.orderId", is(1)))
                .andExpect(jsonPath("$.type", is("DAMAGED")))
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andExpect(jsonPath("$.severity", is("HIGH")));

        verify(exceptionService).create(any(DeliveryExceptionRequest.class));
    }

    @Test
    void whenCreateExceptionWithInvalidData_thenBadRequest() throws Exception {
        DeliveryExceptionRequest request = DeliveryExceptionRequest.builder()
                .build(); // Missing required fields

        mockMvc.perform(post("/api/customer-service/delivery-exceptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(exceptionService, never()).create(any());
    }

    // ========================================
    // Get Exception by ID Tests
    // ========================================

    @Test
    void whenGetExceptionById_thenReturnException() throws Exception {
        UUID exceptionId = UUID.randomUUID();
        DeliveryExceptionResponse response = createExceptionResponse(exceptionId, ExceptionStatus.OPEN);

        when(exceptionService.getById(exceptionId)).thenReturn(response);

        mockMvc.perform(get("/api/customer-service/delivery-exceptions/{id}", exceptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(exceptionId.toString())))
                .andExpect(jsonPath("$.orderId", is(1)))
                .andExpect(jsonPath("$.type", is("DAMAGED")))
                .andExpect(jsonPath("$.status", is("OPEN")));

        verify(exceptionService).getById(exceptionId);
    }

    @Test
    void whenGetExceptionByIdNotFound_thenNotFound() throws Exception {
        UUID exceptionId = UUID.randomUUID();

        when(exceptionService.getById(exceptionId))
                .thenThrow(new ResourceNotFoundException("Exception not found with id: " + exceptionId));

        mockMvc.perform(get("/api/customer-service/delivery-exceptions/{id}", exceptionId))
                .andExpect(status().isNotFound());
    }

    // ========================================
    // Get All Exceptions Tests
    // ========================================

    @Test
    void whenGetAllExceptions_thenReturnList() throws Exception {
        List<DeliveryExceptionResponse> exceptions = Arrays.asList(
                createExceptionResponse(UUID.randomUUID(), ExceptionStatus.OPEN),
                createExceptionResponse(UUID.randomUUID(), ExceptionStatus.IN_PROGRESS)
        );

        when(exceptionService.getAll(null, null, null, null)).thenReturn(exceptions);

        mockMvc.perform(get("/api/customer-service/delivery-exceptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status", is("OPEN")))
                .andExpect(jsonPath("$[1].status", is("IN_PROGRESS")));

        verify(exceptionService).getAll(null, null, null, null);
    }

    @Test
    void whenGetAllExceptionsWithFilters_thenReturnFilteredList() throws Exception {
        UUID assignedTo = UUID.randomUUID();
        List<DeliveryExceptionResponse> exceptions = Arrays.asList(
                createExceptionResponse(UUID.randomUUID(), ExceptionStatus.IN_PROGRESS)
        );

        when(exceptionService.getAll(ExceptionStatus.IN_PROGRESS, Priority.HIGH, assignedTo, Severity.HIGH))
                .thenReturn(exceptions);

        mockMvc.perform(get("/api/customer-service/delivery-exceptions")
                        .param("status", "IN_PROGRESS")
                        .param("priority", "HIGH")
                        .param("assignedTo", assignedTo.toString())
                        .param("severity", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("IN_PROGRESS")));

        verify(exceptionService).getAll(ExceptionStatus.IN_PROGRESS, Priority.HIGH, assignedTo, Severity.HIGH);
    }

    @Test
    void whenGetAllExceptionsEmpty_thenReturnEmptyList() throws Exception {
        when(exceptionService.getAll(null, null, null, null)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/customer-service/delivery-exceptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ========================================
    // Update Exception Tests
    // ========================================

    @Test
    void whenUpdateException_thenReturnUpdatedException() throws Exception {
        UUID exceptionId = UUID.randomUUID();
        DeliveryExceptionUpdateRequest updateRequest = DeliveryExceptionUpdateRequest.builder()
                .status(ExceptionStatus.IN_PROGRESS)
                .assignedTo(UUID.randomUUID())
                .resolutionNotes("Investigating with carrier")
                .build();

        DeliveryExceptionResponse response = createExceptionResponse(exceptionId, ExceptionStatus.IN_PROGRESS);

        when(exceptionService.update(eq(exceptionId), any(DeliveryExceptionUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/customer-service/delivery-exceptions/{id}", exceptionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(exceptionId.toString())))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        verify(exceptionService).update(eq(exceptionId), any(DeliveryExceptionUpdateRequest.class));
    }

    @Test
    void whenUpdateExceptionNotFound_thenNotFound() throws Exception {
        UUID exceptionId = UUID.randomUUID();
        DeliveryExceptionUpdateRequest updateRequest = DeliveryExceptionUpdateRequest.builder()
                .status(ExceptionStatus.IN_PROGRESS)
                .build();

        when(exceptionService.update(eq(exceptionId), any()))
                .thenThrow(new ResourceNotFoundException("Exception not found"));

        mockMvc.perform(patch("/api/customer-service/delivery-exceptions/{id}", exceptionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    // ========================================
    // Close Exception Tests
    // ========================================

    @Test
    void whenCloseException_thenReturnClosedException() throws Exception {
        UUID exceptionId = UUID.randomUUID();
        String requestBody = "{\"resolutionNotes\": \"Issue resolved successfully\"}";

        DeliveryExceptionResponse response = createExceptionResponse(exceptionId, ExceptionStatus.CLOSED);

        when(exceptionService.close(eq(exceptionId), eq("Issue resolved successfully")))
                .thenReturn(response);

        mockMvc.perform(post("/api/customer-service/delivery-exceptions/{id}/close", exceptionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(exceptionId.toString())))
                .andExpect(jsonPath("$.status", is("CLOSED")));

        verify(exceptionService).close(eq(exceptionId), eq("Issue resolved successfully"));
    }

    @Test
    void whenCloseExceptionNotFound_thenNotFound() throws Exception {
        UUID exceptionId = UUID.randomUUID();
        String requestBody = "{\"resolutionNotes\": \"Issue resolved\"}";

        when(exceptionService.close(eq(exceptionId), anyString()))
                .thenThrow(new ResourceNotFoundException("Exception not found"));

        mockMvc.perform(post("/api/customer-service/delivery-exceptions/{id}/close", exceptionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    // ========================================
    // Dashboard Tests
    // ========================================

    @Test
    void whenGetDashboardSummary_thenReturnSummary() throws Exception {
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

        when(exceptionService.getDashboardSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/customer-service/dashboard/exceptions/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExceptions", is(10)))
                .andExpect(jsonPath("$.openExceptions", is(3)))
                .andExpect(jsonPath("$.inProgressExceptions", is(4)))
                .andExpect(jsonPath("$.resolvedToday", is(2)))
                .andExpect(jsonPath("$.highPriorityExceptions", is(1)))
                .andExpect(jsonPath("$.slaBreachedExceptions", is(0)))
                .andExpect(jsonPath("$.averageResolutionTimeHours", is(24.5)))
                .andExpect(jsonPath("$.customerSatisfactionRate", is(4.5)));

        verify(exceptionService).getDashboardSummary();
    }

    @Test
    void whenGetAlerts_thenReturnAlertsList() throws Exception {
        List<DeliveryExceptionResponse> alerts = Arrays.asList(
                createExceptionResponse(UUID.randomUUID(), ExceptionStatus.OPEN),
                createExceptionResponse(UUID.randomUUID(), ExceptionStatus.OPEN)
        );

        when(exceptionService.getAlerts()).thenReturn(alerts);

        mockMvc.perform(get("/api/customer-service/dashboard/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(exceptionService).getAlerts();
    }

    @Test
    void whenGetAlertsEmpty_thenReturnEmptyList() throws Exception {
        when(exceptionService.getAlerts()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/customer-service/dashboard/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        
        verify(exceptionService).getAlerts();
    }

    // ========================================
    // Order Search Tests
    // ========================================

    @Test
    void whenSearchOrders_thenReturnOrdersList() throws Exception {
        List<OrderResponse> orders = Arrays.asList(
                createOrderResponse(1L, "ORD-001"),
                createOrderResponse(2L, "ORD-002")
        );

        when(orderService.getAll()).thenReturn(orders);

        mockMvc.perform(get("/api/customer-service/orders/search")
                        .param("query", "ORD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].reference", is("ORD-001")))
                .andExpect(jsonPath("$[1].reference", is("ORD-002")));

        verify(orderService).getAll();
    }

    @Test
    void whenSearchOrdersEmpty_thenReturnEmptyList() throws Exception {
        when(orderService.getAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/customer-service/orders/search")
                        .param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        
        verify(orderService).getAll();
    }

    // ========================================
    // Get Customer Service Reps Tests
    // ========================================

    @Test
    void whenGetCustomerServiceReps_thenReturnCsrUsers() throws Exception {
        UserRepresentation user1 = new UserRepresentation();
        user1.setId("csr-1");
        user1.setUsername("csr1");
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("john.doe@example.com");

        UserRepresentation user2 = new UserRepresentation();
        user2.setId("csr-2");
        user2.setUsername("csr2");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane.smith@example.com");

        List<UserRepresentation> csrUsers = Arrays.asList(user1, user2);

        when(userService.getUsersByRole("CUSTOMER_SERVICE_REP")).thenReturn(csrUsers);

        mockMvc.perform(get("/api/customer-service/users/customer-service-reps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("csr-1")))
                .andExpect(jsonPath("$[0].username", is("csr1")))
                .andExpect(jsonPath("$[0].firstName", is("John")))
                .andExpect(jsonPath("$[0].lastName", is("Doe")))
                .andExpect(jsonPath("$[0].email", is("john.doe@example.com")))
                .andExpect(jsonPath("$[1].id", is("csr-2")))
                .andExpect(jsonPath("$[1].username", is("csr2")))
                .andExpect(jsonPath("$[1].firstName", is("Jane")))
                .andExpect(jsonPath("$[1].lastName", is("Smith")))
                .andExpect(jsonPath("$[1].email", is("jane.smith@example.com")));

        verify(userService).getUsersByRole("CUSTOMER_SERVICE_REP");
    }

    @Test
    void whenGetCustomerServiceRepsEmpty_thenTryAlternativeRole() throws Exception {
        UserRepresentation user1 = new UserRepresentation();
        user1.setId("csr-1");
        user1.setUsername("csr1");
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("john.doe@example.com");

        // First call returns empty, second call returns users
        when(userService.getUsersByRole("CUSTOMER_SERVICE_REP")).thenReturn(Arrays.asList());
        when(userService.getUsersByRole("customer_service_representative")).thenReturn(Arrays.asList(user1));

        mockMvc.perform(get("/api/customer-service/users/customer-service-reps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("csr-1")))
                .andExpect(jsonPath("$[0].firstName", is("John")));

        verify(userService).getUsersByRole("CUSTOMER_SERVICE_REP");
        verify(userService).getUsersByRole("customer_service_representative");
    }

    @Test
    void whenGetCustomerServiceRepsBothRolesEmpty_thenReturnEmptyList() throws Exception {
        when(userService.getUsersByRole("CUSTOMER_SERVICE_REP")).thenReturn(Arrays.asList());
        when(userService.getUsersByRole("customer_service_representative")).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/customer-service/users/customer-service-reps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userService).getUsersByRole("CUSTOMER_SERVICE_REP");
        verify(userService).getUsersByRole("customer_service_representative");
    }

    // ========================================
    // Helper Methods
    // ========================================

    private DeliveryExceptionRequest createExceptionRequest() {
        return DeliveryExceptionRequest.builder()
                .orderId(1L)
                .type(ExceptionType.DAMAGED)
                .category("PACKAGE_DAMAGE")
                .source(Source.CUSTOMER)
                .severity(Severity.HIGH)
                .description("Package arrived damaged")
                .reportedBy(UUID.randomUUID())
                .customerReported(true)
                .customerId(UUID.randomUUID())
                .build();
    }

    private DeliveryExceptionResponse createExceptionResponse(UUID id, ExceptionStatus status) {
        return DeliveryExceptionResponse.builder()
                .id(id)
                .orderId(1L)
                .type(ExceptionType.DAMAGED)
                .category("PACKAGE_DAMAGE")
                .severity(Severity.HIGH)
                .description("Package arrived damaged")
                .source(Source.CUSTOMER)
                .reportedBy(UUID.randomUUID())
                .reportedAt(LocalDateTime.now())
                .customerReported(true)
                .customerId(UUID.randomUUID())
                .status(status)
                .priority(Priority.HIGH)
                .slaDeadline(LocalDateTime.now().plusHours(24))
                .slaBreached(false)
                .build();
    }

    private OrderResponse createOrderResponse(Long id, String reference) {
        return OrderResponse.builder()
                .id(id)
                .reference(reference)
                .description("Test order")
                .status(OrderStatus.CREATED)
                .shippingAddress("123 Test St")
                .build();
    }
}

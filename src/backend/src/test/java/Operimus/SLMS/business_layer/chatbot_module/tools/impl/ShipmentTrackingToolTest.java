package Operimus.SLMS.business_layer.chatbot_module.tools.impl;

import Operimus.SLMS.business_layer.chatbot_module.dto.ToolResult;
import Operimus.SLMS.business_layer.chatbot_module.dto.UserContext;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Carrier;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Shipment;
import Operimus.SLMS.business_layer.rate_carrier_module.service.ShipmentService;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for ShipmentTrackingTool.
 * Verifies shipment tracking with driver/customer authorization.
 */
@ExtendWith(MockitoExtension.class)
class ShipmentTrackingToolTest {

    @Mock
    private ShipmentService shipmentService;

    private ObjectMapper objectMapper;
    private ShipmentTrackingTool tool;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        tool = new ShipmentTrackingTool(shipmentService, objectMapper);
    }

    @Test
    void shouldReturnToolMetadata() {
        assertThat(tool.getName()).isEqualTo("shipment_tracking");
        assertThat(tool.getDescription()).contains("Get real-time shipment tracking");
        assertThat(tool.getParameters()).hasSize(1);
        assertThat(tool.getParameters().get(0).name()).isEqualTo("shipmentId");
        assertThat(tool.getParameters().get(0).required()).isTrue();
        assertThat(tool.getAllowedRoles()).containsExactlyInAnyOrder("CUSTOMER", "DRIVER", "CUSTOMER_SERVICE", "LOGISTICS_MANAGER");
    }

    @Test
    void shouldGetShipment_asAssignedDriver() throws Exception {
        // Given
        Long shipmentId = 100L;
        String driverId = "driver-1";

        Shipment shipment = createShipment(shipmentId, "SHIP-100", driverId, 1L);
        when(shipmentService.getById(shipmentId)).thenReturn(shipment);

        Map<String, Object> params = Map.of("shipmentId", shipmentId);
        UserContext context = new UserContext(driverId, "DRIVER", Set.of("DRIVER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then - Driver can access their own shipment
        assertThat(result.success()).isTrue();
        assertThat(result.result()).contains("SHIP-100");

        verify(shipmentService).getById(shipmentId);
    }

    @Test
    void shouldDenyAccess_whenDriverAccessesUnassignedShipment() {
        // Given
        Long shipmentId = 100L;
        String assignedDriverId = "driver-1";
        String requestingDriverId = "driver-2";

        Shipment shipment = createShipment(shipmentId, "SHIP-100", assignedDriverId, 1L);
        when(shipmentService.getById(shipmentId)).thenReturn(shipment);

        Map<String, Object> params = Map.of("shipmentId", shipmentId);
        UserContext context = new UserContext(requestingDriverId, "DRIVER", Set.of("DRIVER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then - Different driver cannot access
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("can only view shipments assigned to you");

        verify(shipmentService).getById(shipmentId);
    }

    @Test
    void shouldAllowCustomerToAccessShipment() throws Exception {
        // Given
        Long shipmentId = 100L;
        String customerId = "customer-1";

        Shipment shipment = createShipment(shipmentId, "SHIP-100", "driver-1", 1L);
        when(shipmentService.getById(shipmentId)).thenReturn(shipment);

        Map<String, Object> params = Map.of("shipmentId", shipmentId);
        UserContext context = new UserContext(customerId, "CUSTOMER", Set.of("CUSTOMER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then - Customer can access shipments
        assertThat(result.success()).isTrue();
        assertThat(result.result()).contains("SHIP-100");

        verify(shipmentService).getById(shipmentId);
    }

    @Test
    void shouldAllowCustomerServiceToAccessShipment() throws Exception {
        // Given
        Long shipmentId = 100L;
        String csStaffId = "cs-staff-1";

        Shipment shipment = createShipment(shipmentId, "SHIP-100", "driver-1", 1L);
        when(shipmentService.getById(shipmentId)).thenReturn(shipment);

        Map<String, Object> params = Map.of("shipmentId", shipmentId);
        UserContext context = new UserContext(csStaffId, "CUSTOMER_SERVICE", Set.of("CUSTOMER_SERVICE"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then - Customer service can access any shipment
        assertThat(result.success()).isTrue();

        verify(shipmentService).getById(shipmentId);
    }

    @Test
    void shouldAllowManagerToAccessShipment() throws Exception {
        // Given
        Long shipmentId = 100L;
        String managerId = "manager-1";

        Shipment shipment = createShipment(shipmentId, "SHIP-100", "driver-1", 1L);
        when(shipmentService.getById(shipmentId)).thenReturn(shipment);

        Map<String, Object> params = Map.of("shipmentId", shipmentId);
        UserContext context = new UserContext(managerId, "LOGISTICS_MANAGER", Set.of("LOGISTICS_MANAGER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then - Manager can access any shipment
        assertThat(result.success()).isTrue();

        verify(shipmentService).getById(shipmentId);
    }

    @Test
    void shouldReturnError_whenShipmentNotFound() {
        // Given
        Long shipmentId = 999L;

        when(shipmentService.getById(shipmentId))
            .thenThrow(new ResourceNotFoundException("Shipment not found: " + shipmentId));

        Map<String, Object> params = Map.of("shipmentId", shipmentId);
        UserContext context = new UserContext("driver-1", "DRIVER", Set.of("DRIVER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Shipment not found");

        verify(shipmentService).getById(shipmentId);
    }

    @Test
    void shouldReturnError_whenShipmentIdNotProvided() {
        // Given
        Map<String, Object> params = Map.of(); // Missing shipmentId
        UserContext context = new UserContext("driver-1", "DRIVER", Set.of("DRIVER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("shipmentId is required");

        verify(shipmentService, never()).getById(anyLong());
    }

    @Test
    void shouldReturnError_whenShipmentIdIsNull() {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("shipmentId", null);
        UserContext context = new UserContext("driver-1", "DRIVER", Set.of("DRIVER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("shipmentId is required");

        verify(shipmentService, never()).getById(anyLong());
    }

    @Test
    void shouldHandleShipmentServiceException() {
        // Given
        Long shipmentId = 100L;

        when(shipmentService.getById(shipmentId))
            .thenThrow(new RuntimeException("Database connection failed"));

        Map<String, Object> params = Map.of("shipmentId", shipmentId);
        UserContext context = new UserContext("manager-1", "LOGISTICS_MANAGER", Set.of("LOGISTICS_MANAGER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Failed to get shipment info");

        verify(shipmentService).getById(shipmentId);
    }

    @Test
    void shouldIncludeShipmentDetailsInResult() throws Exception {
        // Given
        Long shipmentId = 100L;
        String shipmentCode = "SHIP-100";
        String driverId = "driver-1";
        Long carrierId = 1L;

        Shipment shipment = createShipment(shipmentId, shipmentCode, driverId, carrierId);
        when(shipmentService.getById(shipmentId)).thenReturn(shipment);

        Map<String, Object> params = Map.of("shipmentId", shipmentId);
        UserContext context = new UserContext(driverId, "DRIVER", Set.of("DRIVER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then - Result should contain shipment details
        assertThat(result.success()).isTrue();
        assertThat(result.result()).isNotNull();
        assertThat(result.result()).contains(shipmentCode);
        assertThat(result.result()).contains(driverId);

        // Verify it's valid JSON
        @SuppressWarnings("unchecked")
        Map<String, Object> parsedResult = objectMapper.readValue(result.result(), Map.class);
        assertThat(parsedResult).containsKeys("shipmentId", "shipmentCode", "driverId", "carrierId");
        assertThat(parsedResult.get("shipmentId")).isEqualTo(100);
        assertThat(parsedResult.get("shipmentCode")).isEqualTo(shipmentCode);
    }

    @Test
    void shouldExtractLongFromInteger() throws Exception {
        // Given - LLM might send Integer instead of Long
        Long shipmentId = 100L;
        Shipment shipment = createShipment(shipmentId, "SHIP-100", "driver-1", 1L);

        when(shipmentService.getById(shipmentId)).thenReturn(shipment);

        Map<String, Object> params = Map.of("shipmentId", 100); // Integer
        UserContext context = new UserContext("driver-1", "DRIVER", Set.of("DRIVER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();
        verify(shipmentService).getById(100L);
    }

    @Test
    void shouldExtractLongFromString() throws Exception {
        // Given
        Long shipmentId = 100L;
        Shipment shipment = createShipment(shipmentId, "SHIP-100", "driver-1", 1L);

        when(shipmentService.getById(shipmentId)).thenReturn(shipment);

        Map<String, Object> params = Map.of("shipmentId", "100"); // String
        UserContext context = new UserContext("driver-1", "DRIVER", Set.of("DRIVER"));

        // When
        ToolResult result = tool.execute(params, context);

        // Then
        assertThat(result.success()).isTrue();
        verify(shipmentService).getById(100L);
    }

    // Helper method to create test shipments
    private Shipment createShipment(Long id, String shipmentCode, String driverId, Long carrierId) {
        // Create a mock carrier for the shipment
        Carrier carrier = Carrier.builder()
                .id(carrierId)
                .code("CARRIER-" + carrierId)
                .name("Test Carrier")
                .build();

        return Shipment.builder()
                .id(id)
                .shipmentCode(shipmentCode)
                .driverId(driverId)
                .carrier(carrier)
                .build();
    }
}

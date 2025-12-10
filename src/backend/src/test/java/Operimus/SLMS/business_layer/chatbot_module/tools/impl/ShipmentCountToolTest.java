package Operimus.SLMS.business_layer.chatbot_module.tools.impl;

import Operimus.SLMS.business_layer.chatbot_module.dto.ToolResult;
import Operimus.SLMS.business_layer.chatbot_module.dto.UserContext;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Shipment;
import Operimus.SLMS.business_layer.rate_carrier_module.service.ShipmentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentCountToolTest {

    @Mock
    private ShipmentService shipmentService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ShipmentCountTool shipmentCountTool;

    private UserContext managerContext;
    private UserContext driverContext;
    private UserContext customerServiceContext;

    @BeforeEach
    void setUp() {
        managerContext = new UserContext("manager1", "LOGISTICS_MANAGER", Set.of("LOGISTICS_MANAGER"));
        driverContext = new UserContext("driver123", "DRIVER", Set.of("DRIVER"));
        customerServiceContext = new UserContext("cs1", "CUSTOMER_SERVICE", Set.of("CUSTOMER_SERVICE"));
    }

    @Test
    void shouldReturnTotalShipmentCount_whenNoDriverFilter() throws JsonProcessingException {
        // Given
        List<Shipment> shipments = createShipmentList(8);
        when(shipmentService.getAll()).thenReturn(shipments);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"totalShipments\":8,\"driverFilter\":\"all\"}");

        // When
        ToolResult result = shipmentCountTool.execute(Map.of(), managerContext);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.result()).contains("totalShipments");
        verify(shipmentService).getAll();
        verify(shipmentService, never()).getByDriverId(anyString());
    }

    @Test
    void shouldReturnFilteredShipmentCount_whenDriverIdProvided() throws JsonProcessingException {
        // Given
        List<Shipment> shipments = createShipmentList(3);
        when(shipmentService.getByDriverId("driver456")).thenReturn(shipments);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"totalShipments\":3,\"driverFilter\":\"driver456\"}");

        Map<String, Object> params = new HashMap<>();
        params.put("driverId", "driver456");

        // When
        ToolResult result = shipmentCountTool.execute(params, managerContext);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.result()).contains("driver456");
        verify(shipmentService).getByDriverId("driver456");
        verify(shipmentService, never()).getAll();
    }

    @Test
    void shouldOverrideDriverId_whenDriverRoleAccessingOwnShipments() throws JsonProcessingException {
        // Given
        List<Shipment> shipments = createShipmentList(5);
        when(shipmentService.getByDriverId("driver123")).thenReturn(shipments);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"totalShipments\":5,\"driverFilter\":\"driver123\"}");

        // When - driver tries to see all shipments (no params)
        ToolResult result = shipmentCountTool.execute(Map.of(), driverContext);

        // Then - should override to their own driver ID
        assertThat(result.success()).isTrue();
        verify(shipmentService).getByDriverId("driver123");
        verify(shipmentService, never()).getAll();
    }

    @Test
    void shouldOverrideDriverId_whenDriverTriesToAccessOtherDriverShipments() throws JsonProcessingException {
        // Given
        List<Shipment> shipments = createShipmentList(5);
        when(shipmentService.getByDriverId("driver123")).thenReturn(shipments);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"totalShipments\":5,\"driverFilter\":\"driver123\"}");

        Map<String, Object> params = new HashMap<>();
        params.put("driverId", "driver999");  // Trying to access other driver's shipments

        // When
        ToolResult result = shipmentCountTool.execute(params, driverContext);

        // Then - should be overridden to their own driver ID
        assertThat(result.success()).isTrue();
        verify(shipmentService).getByDriverId("driver123");  // Should use context userId, not driver999
        verify(shipmentService, never()).getByDriverId("driver999");
    }

    @Test
    void shouldHandleEmptyDriverIdParameter() throws JsonProcessingException {
        // Given
        List<Shipment> shipments = createShipmentList(10);
        when(shipmentService.getAll()).thenReturn(shipments);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"totalShipments\":10,\"driverFilter\":\"all\"}");

        Map<String, Object> params = new HashMap<>();
        params.put("driverId", "   ");  // Blank driver ID

        // When
        ToolResult result = shipmentCountTool.execute(params, managerContext);

        // Then
        assertThat(result.success()).isTrue();
        verify(shipmentService).getAll();  // Should fall back to all shipments
        verify(shipmentService, never()).getByDriverId(anyString());
    }

    @Test
    void shouldHandleServiceException() throws JsonProcessingException {
        // Given
        when(shipmentService.getAll()).thenThrow(new RuntimeException("Database error"));

        // When
        ToolResult result = shipmentCountTool.execute(Map.of(), managerContext);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Failed to get shipment count");
        assertThat(result.errorMessage()).contains("Database error");
    }

    @Test
    void shouldHandleJsonProcessingException() throws JsonProcessingException {
        // Given
        List<Shipment> shipments = createShipmentList(3);
        when(shipmentService.getAll()).thenReturn(shipments);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("JSON error") {});

        // When
        ToolResult result = shipmentCountTool.execute(Map.of(), managerContext);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Failed to get shipment count");
    }

    @Test
    void shouldWorkForCustomerService() throws JsonProcessingException {
        // Given
        List<Shipment> shipments = createShipmentList(12);
        when(shipmentService.getAll()).thenReturn(shipments);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"totalShipments\":12,\"driverFilter\":\"all\"}");

        // When
        ToolResult result = shipmentCountTool.execute(Map.of(), customerServiceContext);

        // Then
        assertThat(result.success()).isTrue();
        verify(shipmentService).getAll();
    }

    @Test
    void shouldHaveCorrectToolMetadata() {
        // When/Then
        assertThat(shipmentCountTool.getName()).isEqualTo("shipment_count");
        assertThat(shipmentCountTool.getDescription()).contains("total number of shipments");
        assertThat(shipmentCountTool.getAllowedRoles()).containsExactlyInAnyOrder(
            "LOGISTICS_MANAGER", "DRIVER", "CUSTOMER_SERVICE"
        );
        assertThat(shipmentCountTool.getParameters()).hasSize(1);
        assertThat(shipmentCountTool.getParameters().get(0).name()).isEqualTo("driverId");
        assertThat(shipmentCountTool.getParameters().get(0).required()).isFalse();
    }

    private List<Shipment> createShipmentList(int count) {
        List<Shipment> shipments = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Shipment shipment = Shipment.builder()
                    .id((long) i)
                    .shipmentCode("SHIP-" + i)
                    .driverId("driver-" + i)
                    .build();
            shipments.add(shipment);
        }
        return shipments;
    }
}

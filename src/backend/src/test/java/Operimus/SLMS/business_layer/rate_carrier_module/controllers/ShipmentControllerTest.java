package Operimus.SLMS.business_layer.rate_carrier_module.controllers;

import Operimus.SLMS.business_layer.rate_carrier_module.dto.ShipmentRequest;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Carrier;
import Operimus.SLMS.business_layer.rate_carrier_module.entities.Shipment;
import Operimus.SLMS.business_layer.rate_carrier_module.service.ShipmentService;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ShipmentController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShipmentService shipmentService;

    private Shipment testShipment;
    private ShipmentRequest testShipmentRequest;

    @BeforeEach
    void setUp() {
        Carrier carrier = Carrier.builder()
                .id(1L)
                .code("DHL")
                .name("DHL Express")
                .active(true)
                .build();

        testShipment = Shipment.builder()
                .id(1L)
                .shipmentCode("SHP-001")
                .carrier(carrier)
                .driverId("driver-123")
                .totalOrders(5)
                .totalWeight(25.5)
                .weightUnit("kg")
                .estimatedDeliveryDate(LocalDate.now().plusDays(3))
                .shippingCost(150.00)
                .transitTime("3 days")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        testShipmentRequest = ShipmentRequest.builder()
                .shipmentCode("SHP-001")
                .carrierId(1L)
                .driverId("driver-123")
                .orderIds(Arrays.asList(1L, 2L, 3L))
                .build();
    }

    // ========================================
    // Create Shipment Tests
    // ========================================

    @Test
    void whenCreateShipment_thenReturn201() throws Exception {
        when(shipmentService.create(anyString(), any(), anyString(), anyList()))
                .thenReturn(testShipment);

        mockMvc.perform(post("/api/admin/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testShipmentRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/shipments/1")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.shipmentCode", is("SHP-001")))
                .andExpect(jsonPath("$.carrierCode", is("DHL")))
                .andExpect(jsonPath("$.driverId", is("driver-123")))
                .andExpect(jsonPath("$.totalOrders", is(5)));

        verify(shipmentService).create(anyString(), any(), anyString(), anyList());
    }

    @Test
    void whenCreateShipmentWithInvalidData_thenReturn400() throws Exception {
        ShipmentRequest invalidRequest = ShipmentRequest.builder()
                .shipmentCode("")
                .build();

        mockMvc.perform(post("/api/admin/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(shipmentService, never()).create(anyString(), any(), anyString(), anyList());
    }

    // ========================================
    // Get Shipment by ID Tests
    // ========================================

    @Test
    void whenGetShipmentById_thenReturnShipment() throws Exception {
        when(shipmentService.getById(1L)).thenReturn(testShipment);

        mockMvc.perform(get("/api/admin/shipments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.shipmentCode", is("SHP-001")))
                .andExpect(jsonPath("$.carrierCode", is("DHL")))
                .andExpect(jsonPath("$.totalOrders", is(5)));

        verify(shipmentService).getById(1L);
    }

    @Test
    void whenGetShipmentByIdNotFound_thenReturn404() throws Exception {
        when(shipmentService.getById(999L))
                .thenThrow(new ResourceNotFoundException("Shipment 999 not found"));

        mockMvc.perform(get("/api/admin/shipments/999"))
                .andExpect(status().isNotFound());

        verify(shipmentService).getById(999L);
    }

    // ========================================
    // Get All Shipments Tests
    // ========================================

    @Test
    void whenGetAllShipments_thenReturnShipmentsList() throws Exception {
        List<Shipment> shipments = Arrays.asList(testShipment);
        when(shipmentService.getAll()).thenReturn(shipments);

        mockMvc.perform(get("/api/admin/shipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].shipmentCode", is("SHP-001")));

        verify(shipmentService).getAll();
    }

    @Test
    void whenGetAllShipmentsEmpty_thenReturnEmptyList() throws Exception {
        when(shipmentService.getAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/admin/shipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(shipmentService).getAll();
    }

    // ========================================
    // Update Driver Tests
    // ========================================

    @Test
    void whenUpdateDriver_thenReturnUpdatedShipment() throws Exception {
        Shipment updatedShipment = Shipment.builder()
                .id(1L)
                .shipmentCode("SHP-001")
                .carrier(testShipment.getCarrier())
                .driverId("new-driver-456")
                .totalOrders(5)
                .totalWeight(25.5)
                .weightUnit("kg")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        when(shipmentService.updateDriver(1L, "new-driver-456")).thenReturn(updatedShipment);

        mockMvc.perform(put("/api/admin/shipments/1/driver")
                        .param("driverId", "new-driver-456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.driverId", is("new-driver-456")));

        verify(shipmentService).updateDriver(1L, "new-driver-456");
    }

    @Test
    void whenUpdateDriverNotFound_thenReturn404() throws Exception {
        when(shipmentService.updateDriver(999L, "new-driver-456"))
                .thenThrow(new ResourceNotFoundException("Shipment 999 not found"));

        mockMvc.perform(put("/api/admin/shipments/999/driver")
                        .param("driverId", "new-driver-456"))
                .andExpect(status().isNotFound());

        verify(shipmentService).updateDriver(999L, "new-driver-456");
    }

    // ========================================
    // Get Shipments by Carrier Tests
    // ========================================

    @Test
    void whenGetShipmentsByCarrier_thenReturnShipmentsList() throws Exception {
        List<Shipment> shipments = Arrays.asList(testShipment);
        when(shipmentService.getByCarrierId(1L)).thenReturn(shipments);

        mockMvc.perform(get("/api/admin/shipments/carrier/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].carrierCode", is("DHL")));

        verify(shipmentService).getByCarrierId(1L);
    }

    @Test
    void whenGetShipmentsByCarrierEmpty_thenReturnEmptyList() throws Exception {
        when(shipmentService.getByCarrierId(999L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/admin/shipments/carrier/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(shipmentService).getByCarrierId(999L);
    }

    // ========================================
    // Get Shipments by Driver Tests
    // ========================================

    @Test
    void whenGetShipmentsByDriver_thenReturnShipmentsList() throws Exception {
        List<Shipment> shipments = Arrays.asList(testShipment);
        when(shipmentService.getByDriverId("driver-123")).thenReturn(shipments);

        mockMvc.perform(get("/api/admin/shipments/driver/driver-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].driverId", is("driver-123")));

        verify(shipmentService).getByDriverId("driver-123");
    }

    @Test
    void whenGetShipmentsByDriverEmpty_thenReturnEmptyList() throws Exception {
        when(shipmentService.getByDriverId("unknown-driver")).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/admin/shipments/driver/unknown-driver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(shipmentService).getByDriverId("unknown-driver");
    }
}

package Operimus.SLMS.business_layer.order_module.controllers;

import Operimus.SLMS.business_layer.order_module.dto.WarehouseRequest;
import Operimus.SLMS.business_layer.order_module.dto.WarehouseResponse;
import Operimus.SLMS.business_layer.order_module.service.WarehouseService;
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

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WarehouseController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WarehouseService warehouseService;

    private WarehouseResponse testWarehouse;
    private WarehouseRequest testWarehouseRequest;

    @BeforeEach
    void setUp() {
        testWarehouse = WarehouseResponse.builder()
                .id(1L)
                .code("WH-001")
                .name("Main Warehouse")
                .addressLine1("123 Logistics Ave")
                .city("Aveiro")
                .stateOrProvince("Aveiro")
                .postalCode("3810-123")
                .country("Portugal")
                .active(true)
                .build();

        testWarehouseRequest = WarehouseRequest.builder()
                .code("WH-001")
                .name("Main Warehouse")
                .addressLine1("123 Logistics Ave")
                .city("Aveiro")
                .stateOrProvince("Aveiro")
                .postalCode("3810-123")
                .country("Portugal")
                .active(true)
                .build();
    }

    // ========================================
    // Get All Warehouses Tests
    // ========================================

    @Test
    void whenGetWarehouses_thenReturnWarehousesList() throws Exception {
        List<WarehouseResponse> warehouses = Arrays.asList(testWarehouse);
        when(warehouseService.getAll()).thenReturn(warehouses);

        mockMvc.perform(get("/api/admin/warehouses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].code", is("WH-001")))
                .andExpect(jsonPath("$[0].name", is("Main Warehouse")))
                .andExpect(jsonPath("$[0].city", is("Aveiro")));

        verify(warehouseService).getAll();
    }

    @Test
    void whenGetWarehousesEmpty_thenReturnEmptyList() throws Exception {
        when(warehouseService.getAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/admin/warehouses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(warehouseService).getAll();
    }

    // ========================================
    // Get Warehouse by ID Tests
    // ========================================

    @Test
    void whenGetWarehouseById_thenReturnWarehouse() throws Exception {
        when(warehouseService.getById(1L)).thenReturn(testWarehouse);

        mockMvc.perform(get("/api/admin/warehouses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.code", is("WH-001")))
                .andExpect(jsonPath("$.name", is("Main Warehouse")))
                .andExpect(jsonPath("$.active", is(true)));

        verify(warehouseService).getById(1L);
    }

    @Test
    void whenGetWarehouseByIdNotFound_thenReturn404() throws Exception {
        when(warehouseService.getById(999L))
                .thenThrow(new ResourceNotFoundException("Warehouse 999 not found"));

        mockMvc.perform(get("/api/admin/warehouses/999"))
                .andExpect(status().isNotFound());

        verify(warehouseService).getById(999L);
    }

    // ========================================
    // Create Warehouse Tests
    // ========================================

    @Test
    void whenCreateWarehouse_thenReturn201() throws Exception {
        when(warehouseService.create(any(WarehouseRequest.class))).thenReturn(testWarehouse);

        mockMvc.perform(post("/api/admin/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testWarehouseRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/admin/warehouses/1")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.code", is("WH-001")))
                .andExpect(jsonPath("$.name", is("Main Warehouse")));

        verify(warehouseService).create(any(WarehouseRequest.class));
    }

    @Test
    void whenCreateWarehouseWithInvalidData_thenReturn400() throws Exception {
        WarehouseRequest invalidRequest = WarehouseRequest.builder()
                .code("")
                .name("")
                .build();

        mockMvc.perform(post("/api/admin/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(warehouseService, never()).create(any());
    }

    // ========================================
    // Update Warehouse Tests
    // ========================================

    @Test
    void whenUpdateWarehouse_thenReturnUpdatedWarehouse() throws Exception {
        WarehouseResponse updatedWarehouse = WarehouseResponse.builder()
                .id(1L)
                .code("WH-001")
                .name("Updated Warehouse")
                .addressLine1("456 New Ave")
                .city("Porto")
                .stateOrProvince("Porto")
                .postalCode("4000-001")
                .country("Portugal")
                .active(true)
                .build();

        when(warehouseService.update(eq(1L), any(WarehouseRequest.class))).thenReturn(updatedWarehouse);

        mockMvc.perform(put("/api/admin/warehouses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testWarehouseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated Warehouse")))
                .andExpect(jsonPath("$.city", is("Porto")));

        verify(warehouseService).update(eq(1L), any(WarehouseRequest.class));
    }

    @Test
    void whenUpdateWarehouseNotFound_thenReturn404() throws Exception {
        when(warehouseService.update(eq(999L), any(WarehouseRequest.class)))
                .thenThrow(new ResourceNotFoundException("Warehouse 999 not found"));

        mockMvc.perform(put("/api/admin/warehouses/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testWarehouseRequest)))
                .andExpect(status().isNotFound());

        verify(warehouseService).update(eq(999L), any(WarehouseRequest.class));
    }

    // ========================================
    // Delete Warehouse Tests
    // ========================================

    @Test
    void whenDeleteWarehouse_thenReturn204() throws Exception {
        doNothing().when(warehouseService).delete(1L);

        mockMvc.perform(delete("/api/admin/warehouses/1"))
                .andExpect(status().isNoContent());

        verify(warehouseService).delete(1L);
    }

    @Test
    void whenDeleteWarehouseNotFound_thenReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Warehouse 999 not found"))
                .when(warehouseService).delete(999L);

        mockMvc.perform(delete("/api/admin/warehouses/999"))
                .andExpect(status().isNotFound());

        verify(warehouseService).delete(999L);
    }

    // ========================================
    // Multiple Warehouses Tests
    // ========================================

    @Test
    void whenGetMultipleWarehouses_thenReturnList() throws Exception {
        WarehouseResponse warehouse2 = WarehouseResponse.builder()
                .id(2L)
                .code("WH-002")
                .name("Secondary Warehouse")
                .city("Porto")
                .country("Portugal")
                .active(true)
                .build();

        List<WarehouseResponse> warehouses = Arrays.asList(testWarehouse, warehouse2);
        when(warehouseService.getAll()).thenReturn(warehouses);

        mockMvc.perform(get("/api/admin/warehouses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code", is("WH-001")))
                .andExpect(jsonPath("$[1].code", is("WH-002")));

        verify(warehouseService).getAll();
    }

    @Test
    void whenCreateWarehouseWithFullDetails_thenReturn201() throws Exception {
        WarehouseRequest detailedRequest = WarehouseRequest.builder()
                .code("WH-DETAIL")
                .name("Detailed Warehouse")
                .addressLine1("123 Main St")
                .addressLine2("Building A")
                .city("Lisboa")
                .stateOrProvince("Lisboa")
                .postalCode("1000-001")
                .country("Portugal")
                .active(true)
                .build();

        WarehouseResponse detailedResponse = WarehouseResponse.builder()
                .id(3L)
                .code("WH-DETAIL")
                .name("Detailed Warehouse")
                .addressLine1("123 Main St")
                .addressLine2("Building A")
                .city("Lisboa")
                .stateOrProvince("Lisboa")
                .postalCode("1000-001")
                .country("Portugal")
                .active(true)
                .build();

        when(warehouseService.create(any(WarehouseRequest.class))).thenReturn(detailedResponse);

        mockMvc.perform(post("/api/admin/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(detailedRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is("WH-DETAIL")))
                .andExpect(jsonPath("$.addressLine2", is("Building A")))
                .andExpect(jsonPath("$.city", is("Lisboa")));

        verify(warehouseService).create(any(WarehouseRequest.class));
    }
}

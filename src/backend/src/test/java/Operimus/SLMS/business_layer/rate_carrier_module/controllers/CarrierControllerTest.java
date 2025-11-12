package Operimus.SLMS.business_layer.rate_carrier_module.controllers;

import Operimus.SLMS.business_layer.rate_carrier_module.dto.*;
import Operimus.SLMS.business_layer.rate_carrier_module.service.AddressValidationService;
import Operimus.SLMS.business_layer.rate_carrier_module.service.CarrierIntegrationService;
import Operimus.SLMS.business_layer.rate_carrier_module.service.CarrierService;
import Operimus.SLMS.business_layer.rate_carrier_module.service.ShippingRateService;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import Operimus.SLMS.business_layer.rate_carrier_module.service.*;
import Operimus.SLMS.business_layer.user_management_module.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CarrierController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class CarrierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarrierService carrierService;

    @MockBean
    private CarrierIntegrationService carrierIntegrationService;

    @MockBean
    private AddressValidationService addressValidationService;

    @MockBean
    private ShippingRateService shippingRateService;

    private CarrierResponse testCarrier;
    private CarrierRequest testCarrierRequest;

    @BeforeEach
    void setUp() {
        testCarrier = CarrierResponse.builder()
                .id(1L)
                .code("DHL")
                .name("DHL Express")
                .description("International express carrier")
                .supportEmail("support@dhl.com")
                .supportPhone("+49 228 4333112")
                .website("https://www.dhl.com")
                .active(true)
                .build();

        testCarrierRequest = CarrierRequest.builder()
                .code("DHL")
                .name("DHL Express")
                .description("International express carrier")
                .supportEmail("support@dhl.com")
                .supportPhone("+49 228 4333112")
                .website("https://www.dhl.com")
                .addressLine1("Charles-de-Gaulle-Str. 20")
                .city("Bonn")
                .stateOrProvince("NRW")
                .postalCode("53113")
                .country("DE")
                .build();
    }

    // ========================================
    // Get All Carriers Tests
    // ========================================

    @Test
    void whenGetCarriers_thenReturnCarriersList() throws Exception {
        List<CarrierResponse> carriers = Arrays.asList(testCarrier);
        when(carrierService.getAll()).thenReturn(carriers);

        mockMvc.perform(get("/api/admin/carriers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].code", is("DHL")))
                .andExpect(jsonPath("$[0].name", is("DHL Express")));

        verify(carrierService).getAll();
    }

    @Test
    void whenGetCarriersEmpty_thenReturnEmptyList() throws Exception {
        when(carrierService.getAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/admin/carriers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(carrierService).getAll();
    }

    // ========================================
    // Get Carrier by ID Tests
    // ========================================

    @Test
    void whenGetCarrierById_thenReturnCarrier() throws Exception {
        when(carrierService.getById(1L)).thenReturn(testCarrier);

        mockMvc.perform(get("/api/admin/carriers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.code", is("DHL")))
                .andExpect(jsonPath("$.name", is("DHL Express")));

        verify(carrierService).getById(1L);
    }

    @Test
    void whenGetCarrierByIdNotFound_thenReturn404() throws Exception {
        when(carrierService.getById(999L))
                .thenThrow(new ResourceNotFoundException("Carrier 999 not found"));

        mockMvc.perform(get("/api/admin/carriers/999"))
                .andExpect(status().isNotFound());

        verify(carrierService).getById(999L);
    }

    // ========================================
    // Create Carrier Tests
    // ========================================

    @Test
    void whenCreateCarrier_thenReturn201() throws Exception {
        when(carrierService.create(any(CarrierRequest.class))).thenReturn(testCarrier);

        mockMvc.perform(post("/api/admin/carriers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCarrierRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/admin/carriers/1")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.code", is("DHL")));

        verify(carrierService).create(any(CarrierRequest.class));
    }

    @Test
    void whenCreateCarrierWithInvalidData_thenReturn400() throws Exception {
        CarrierRequest invalidRequest = CarrierRequest.builder()
                .code("")
                .name("")
                .build();

        mockMvc.perform(post("/api/admin/carriers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(carrierService, never()).create(any());
    }

    // ========================================
    // Update Carrier Tests
    // ========================================

    @Test
    void whenUpdateCarrier_thenReturnUpdatedCarrier() throws Exception {
        CarrierResponse updatedCarrier = CarrierResponse.builder()
                .id(1L)
                .code("DHL")
                .name("DHL Express Updated")
                .active(true)
                .build();

        when(carrierService.update(eq(1L), any(CarrierRequest.class))).thenReturn(updatedCarrier);

        mockMvc.perform(put("/api/admin/carriers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCarrierRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("DHL Express Updated")));

        verify(carrierService).update(eq(1L), any(CarrierRequest.class));
    }

    @Test
    void whenUpdateCarrierNotFound_thenReturn404() throws Exception {
        when(carrierService.update(eq(999L), any(CarrierRequest.class)))
                .thenThrow(new ResourceNotFoundException("Carrier 999 not found"));

        mockMvc.perform(put("/api/admin/carriers/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCarrierRequest)))
                .andExpect(status().isNotFound());

        verify(carrierService).update(eq(999L), any(CarrierRequest.class));
    }

    // ========================================
    // Delete Carrier Tests
    // ========================================

    @Test
    void whenDeleteCarrier_thenReturn204() throws Exception {
        doNothing().when(carrierService).delete(1L);

        mockMvc.perform(delete("/api/admin/carriers/1"))
                .andExpect(status().isNoContent());

        verify(carrierService).delete(1L);
    }

    @Test
    void whenDeleteCarrierNotFound_thenReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Carrier 999 not found"))
                .when(carrierService).delete(999L);

        mockMvc.perform(delete("/api/admin/carriers/999"))
                .andExpect(status().isNotFound());

        verify(carrierService).delete(999L);
    }

    // ========================================
    // Integration Snapshot Tests
    // ========================================

    @Test
    void whenGetIntegrationSnapshot_thenReturnSnapshots() throws Exception {
        CarrierIntegrationSnapshot snapshot = CarrierIntegrationSnapshot.builder()
                .carrierCode("DHL")
                .displayName("DHL Express")
                .active(true)
                .serviceLevels(Arrays.asList("Express", "Standard", "Economy"))
                .build();

        when(carrierIntegrationService.fetchSnapshots()).thenReturn(Arrays.asList(snapshot));

        mockMvc.perform(get("/api/admin/carriers/integrations/snapshot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].carrierCode", is("DHL")))
                .andExpect(jsonPath("$[0].displayName", is("DHL Express")))
                .andExpect(jsonPath("$[0].serviceLevels", hasSize(3)));

        verify(carrierIntegrationService).fetchSnapshots();
    }

    // ========================================
    // Address Validation Tests
    // ========================================

    @Test
    void whenValidateAddress_thenReturnValidationResult() throws Exception {
        AddressValidationRequest request = AddressValidationRequest.builder()
                .addressLine1("123 Main St")
                .city("Aveiro")
                .postalCode("3810-123")
                .country("PT")
                .build();

        AddressValidationResponse response = AddressValidationResponse.builder()
                .valid(true)
                .build();

        when(addressValidationService.validate(any(AddressValidationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/admin/carriers/addresses/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(true)));

        verify(addressValidationService).validate(any(AddressValidationRequest.class));
    }

    // ========================================
    // Shipping Rate Tests
    // ========================================

    @Test
    void whenGetShippingRates_thenReturnRates() throws Exception {
        ShippingRateRequest.Address fromAddress = ShippingRateRequest.Address.builder()
                .addressLine1("123 Origin St")
                .city("Aveiro")
                .postalCode("3810-123")
                .country("PT")
                .build();

        ShippingRateRequest.Address toAddress = ShippingRateRequest.Address.builder()
                .addressLine1("456 Destination Ave")
                .city("Lisboa")
                .postalCode("1000-001")
                .country("PT")
                .build();

        ShippingRateRequest.Parcel parcel = ShippingRateRequest.Parcel.builder()
                .weight(new java.math.BigDecimal("2.5"))
                .weightUnit("kg")
                .length(new java.math.BigDecimal("30.0"))
                .width(new java.math.BigDecimal("20.0"))
                .height(new java.math.BigDecimal("10.0"))
                .distanceUnit("cm")
                .build();

        ShippingRateRequest request = ShippingRateRequest.builder()
                .fromAddress(fromAddress)
                .toAddress(toAddress)
                .parcels(Arrays.asList(parcel))
                .build();

        ShippingRateResponse.RateQuote rateQuote = ShippingRateResponse.RateQuote.builder()
                .rateId("rate_123")
                .provider("Shippo")
                .carrierCode("DHL")
                .serviceLevel("Express")
                .amount(new java.math.BigDecimal("15.50"))
                .currency("EUR")
                .estimatedDays(2)
                .build();

        ShippingRateResponse response = ShippingRateResponse.builder()
                .shipmentId("ship_123")
                .rates(Arrays.asList(rateQuote))
                .build();

        when(shippingRateService.calculateRates(any(ShippingRateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/admin/carriers/rates/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shipmentId", is("ship_123")))
                .andExpect(jsonPath("$.rates", hasSize(1)))
                .andExpect(jsonPath("$.rates[0].carrierCode", is("DHL")))
                .andExpect(jsonPath("$.rates[0].serviceLevel", is("Express")));

        verify(shippingRateService).calculateRates(any(ShippingRateRequest.class));
    }
}

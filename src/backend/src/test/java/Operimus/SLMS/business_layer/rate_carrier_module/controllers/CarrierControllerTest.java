package Operimus.SLMS.business_layer.rate_carrier_module.controllers;

import Operimus.SLMS.business_layer.rate_carrier_module.dto.*;
import Operimus.SLMS.business_layer.rate_carrier_module.service.*;
import Operimus.SLMS.business_layer.user_management_module.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CarrierController.class,
    excludeAutoConfiguration = {OAuth2ResourceServerAutoConfiguration.class})
@Import(SecurityConfig.class)
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

    @MockBean
    private JwtDecoder jwtDecoder;

    private CarrierResponse mockCarrier;
    private CarrierRequest mockCarrierRequest;

    @BeforeEach
    void setUp() {
        mockCarrier = CarrierResponse.builder()
                .id(1L)
                .code("DHL")
                .name("DHL Express")
                .description("International shipping")
                .active(true)
                .createdAt(OffsetDateTime.now())
                .build();

        mockCarrierRequest = CarrierRequest.builder()
                .code("DHL")
                .name("DHL Express")
                .description("International shipping")
                .active(true)
                .build();
    }

    private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor createJwtWithAdminRole() {
        return jwt()
                .jwt(jwt -> jwt.subject("admin-uuid"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void whenGetCarriers_thenReturnCarrierList() throws Exception {
        when(carrierService.getAll()).thenReturn(List.of(mockCarrier));

        mockMvc.perform(get("/api/admin/carriers")
                        .with(createJwtWithAdminRole()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].code", is("DHL")))
                .andExpect(jsonPath("$[0].name", is("DHL Express")))
                .andExpect(jsonPath("$[0].active", is(true)));

        verify(carrierService, times(1)).getAll();
    }

    @Test
    void whenGetCarrierById_thenReturnCarrier() throws Exception {
        when(carrierService.getById(1L)).thenReturn(mockCarrier);

        mockMvc.perform(get("/api/admin/carriers/1")
                        .with(createJwtWithAdminRole()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.code", is("DHL")))
                .andExpect(jsonPath("$.name", is("DHL Express")));

        verify(carrierService, times(1)).getById(1L);
    }

    @Test
    void whenCreateCarrier_thenReturnCreatedCarrier() throws Exception {
        when(carrierService.create(any(CarrierRequest.class))).thenReturn(mockCarrier);

        mockMvc.perform(post("/api/admin/carriers")
                        .with(createJwtWithAdminRole())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockCarrierRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/admin/carriers/1")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.code", is("DHL")))
                .andExpect(jsonPath("$.name", is("DHL Express")));

        verify(carrierService, times(1)).create(any(CarrierRequest.class));
    }

    @Test
    void whenUpdateCarrier_thenReturnUpdatedCarrier() throws Exception {
        CarrierResponse updatedCarrier = CarrierResponse.builder()
                .id(1L)
                .code("DHL")
                .name("DHL Express Updated")
                .description("Updated description")
                .active(true)
                .createdAt(OffsetDateTime.now())
                .build();

        when(carrierService.update(anyLong(), any(CarrierRequest.class))).thenReturn(updatedCarrier);

        mockMvc.perform(put("/api/admin/carriers/1")
                        .with(createJwtWithAdminRole())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockCarrierRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("DHL Express Updated")));

        verify(carrierService, times(1)).update(eq(1L), any(CarrierRequest.class));
    }

    @Test
    void whenDeleteCarrier_thenReturnNoContent() throws Exception {
        doNothing().when(carrierService).delete(1L);

        mockMvc.perform(delete("/api/admin/carriers/1")
                        .with(createJwtWithAdminRole()))
                .andExpect(status().isNoContent());

        verify(carrierService, times(1)).delete(1L);
    }

    @Test
    void whenSynchronizeCarriers_thenReturnSynchronizedCarriers() throws Exception {
        when(carrierIntegrationService.synchronizeCarriers()).thenReturn(List.of(mockCarrier));

        mockMvc.perform(post("/api/admin/carriers/integrations/sync")
                        .with(createJwtWithAdminRole()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].code", is("DHL")));

        verify(carrierIntegrationService, times(1)).synchronizeCarriers();
    }

    @Test
    void whenCreateCarrierWithInvalidData_thenReturnBadRequest() throws Exception {
        CarrierRequest invalidRequest = CarrierRequest.builder()
                .code("") // Invalid: empty code
                .name("DHL Express")
                .build();

        mockMvc.perform(post("/api/admin/carriers")
                        .with(createJwtWithAdminRole())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(carrierService, never()).create(any(CarrierRequest.class));
    }
}

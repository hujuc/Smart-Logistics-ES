package Operimus.SLMS.business_layer.user_management_module.config;

import Operimus.SLMS.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenAccessPublicEndpoint_thenNoAuthenticationRequired() throws Exception {
        mockMvc.perform(get("/api/public/test"))
                .andExpect(status().isNotFound()); // 404 means no auth required, endpoint doesn't exist
    }

    @Test
    void whenAccessActuatorEndpoint_thenNoAuthenticationRequired() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void whenAccessSwaggerEndpoint_thenNoAuthenticationRequired() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void whenAccessProtectedEndpointWithoutAuth_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "LOGISTICS_MANAGER")
    void whenAccessAdminEndpointWithLogisticsManagerRole_thenAuthorized() throws Exception {
        mockMvc.perform(get("/api/admin/test"))
                .andExpect(status().isNotFound()); // 404 means authorized, endpoint doesn't exist
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void whenAccessAdminEndpointWithCustomerRole_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/test"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE_STAFF")
    void whenAccessWarehouseEndpointWithWarehouseStaffRole_thenAuthorized() throws Exception {
        mockMvc.perform(get("/api/warehouse/test"))
                .andExpect(status().isNotFound()); // 404 means authorized, endpoint doesn't exist
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void whenAccessWarehouseEndpointWithDriverRole_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/warehouse/test"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void whenAccessDriverEndpointWithDriverRole_thenAuthorized() throws Exception {
        mockMvc.perform(get("/api/driver/test"))
                .andExpect(status().isNotFound()); // 404 means authorized, endpoint doesn't exist
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void whenAccessCustomerEndpointWithCustomerRole_thenAuthorized() throws Exception {
        mockMvc.perform(get("/api/customer/test"))
                .andExpect(status().isNotFound()); // 404 means authorized, endpoint doesn't exist
    }

    @Test
    @WithMockUser(roles = "CUSTOMER_SERVICE_REP")
    void whenAccessCustomerEndpointWithCustomerServiceRepRole_thenAuthorized() throws Exception {
        mockMvc.perform(get("/api/customer/test"))
                .andExpect(status().isNotFound()); // 404 means authorized, endpoint doesn't exist
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE_STAFF")
    void whenAccessCustomerEndpointWithWarehouseStaffRole_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/customer/test"))
                .andExpect(status().isForbidden());
    }
}

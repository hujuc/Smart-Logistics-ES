package Operimus.SLMS.presentation_layer.admin_console_bff.logistics_bff;

import Operimus.SLMS.business_layer.user_management_module.dto.UpdateUserDTO;
import Operimus.SLMS.business_layer.user_management_module.dto.UserDTO;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LogisticsBffController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class LogisticsBffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KeycloakUserService userService;

    @MockBean
    private Operimus.SLMS.presentation_layer.admin_console_bff.logistics_bff.service.LogisticsStatsService statsService;

    @MockBean
    private Operimus.SLMS.business_layer.order_module.service.WarehouseService warehouseService;

    @MockBean
    private Operimus.SLMS.business_layer.order_module.service.OrderService orderService;

    @MockBean
    private Operimus.SLMS.business_layer.order_module.repository.OrderRepository orderRepository;

    @MockBean
    private Operimus.SLMS.business_layer.order_module.repository.WarehouseRepository warehouseRepository;

    @MockBean
    private Operimus.SLMS.business_layer.rate_carrier_module.repository.CarrierRepository carrierRepository;

    @MockBean
    private Operimus.SLMS.business_layer.visibility_module.exceptions.repository.DeliveryExceptionRepository deliveryExceptionRepository;

    @MockBean
    private Operimus.SLMS.business_layer.order_module.service.OrderDocumentService orderDocumentService;

    @MockBean
    private Operimus.SLMS.business_layer.order_module.service.OrderEventService orderEventService;

    @MockBean
    private Operimus.SLMS.business_layer.order_module.repository.OrderEventRepository orderEventRepository;

    @MockBean
    private software.amazon.awssdk.services.s3.S3Client s3Client;

    @Test
    void whenHealthCheck_thenReturnStatus() throws Exception {
        mockMvc.perform(get("/api/logistics/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.service", is("Logistics BFF")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void whenGetAllUsers_thenUsersAreReturned() throws Exception {
        List<UserRepresentation> users = Arrays.asList(
                createUserRepresentation("user1", "user1@example.com"),
                createUserRepresentation("user2", "user2@example.com")
        );

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/logistics/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("user1")))
                .andExpect(jsonPath("$[1].username", is("user2")));
    }

    @Test
    void whenGetAllUsersFails_thenInternalServerError() throws Exception {
        when(userService.getAllUsers())
                .thenThrow(new RuntimeException("Failed to fetch users"));

        mockMvc.perform(get("/api/logistics/users"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", is("Failed to fetch users")));
    }

    @Test
    void whenGetUserById_thenUserIsReturned() throws Exception {
        String userId = "test-user-id";
        UserRepresentation user = createUserRepresentation("testuser", "test@example.com");

        when(userService.getUserById(userId)).thenReturn(user);

        mockMvc.perform(get("/api/logistics/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    void whenGetUserByIdNotFound_thenNotFound() throws Exception {
        String userId = "non-existent-user-id";

        when(userService.getUserById(userId))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/logistics/users/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("User not found")));
    }

    @Test
    void whenCreateUser_thenUserIsCreated() throws Exception {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User",
                Arrays.asList("DRIVER")
        );

        UserRepresentation createdUser = createUserRepresentation("testuser", "test@example.com");

        when(userService.createUser(any(UserDTO.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/logistics/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    void whenCreateUserFails_thenBadRequest() throws Exception {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User",
                Arrays.asList("DRIVER")
        );

        when(userService.createUser(any(UserDTO.class)))
                .thenThrow(new RuntimeException("User already exists"));

        mockMvc.perform(post("/api/logistics/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("User already exists")));
    }

    @Test
    void whenUpdateUser_thenUserIsUpdated() throws Exception {
        String userId = "test-user-id";
        UpdateUserDTO updateUserDTO = new UpdateUserDTO(
                "updateduser",
                "updated@example.com",
                "Updated",
                "User",
                Arrays.asList("DRIVER")
        );

        UserRepresentation updatedUser = createUserRepresentation("updateduser", "updated@example.com");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");
        updatedUser.setGroups(Arrays.asList("Drivers"));

        when(userService.updateUser(anyString(), any(UpdateUserDTO.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/logistics/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("updateduser")))
                .andExpect(jsonPath("$.email", is("updated@example.com")))
                .andExpect(jsonPath("$.firstName", is("Updated")))
                .andExpect(jsonPath("$.lastName", is("User")))
                .andExpect(jsonPath("$.groups[0]", is("Drivers")));
    }

    @Test
    void whenUpdateUserPartialFields_thenOnlyProvidedFieldsAreUpdated() throws Exception {
        String userId = "test-user-id";
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setEmail("newemail@example.com");

        UserRepresentation updatedUser = createUserRepresentation("testuser", "newemail@example.com");

        when(userService.updateUser(anyString(), any(UpdateUserDTO.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/logistics/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("newemail@example.com")));
    }

    @Test
    void whenUpdateUserFails_thenBadRequest() throws Exception {
        String userId = "test-user-id";
        UpdateUserDTO updateUserDTO = new UpdateUserDTO(
                "updateduser",
                "updated@example.com",
                "Updated",
                "User",
                Arrays.asList("DRIVER")
        );

        when(userService.updateUser(anyString(), any(UpdateUserDTO.class)))
                .thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(put("/api/logistics/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Update failed")));
    }

    @Test
    void whenDeleteUser_thenUserIsDeleted() throws Exception {
        String userId = "test-user-id";

        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/logistics/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User deleted successfully")));
    }

    @Test
    void whenDeleteUserFails_thenInternalServerError() throws Exception {
        String userId = "test-user-id";

        doThrow(new RuntimeException("Delete failed"))
                .when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/logistics/users/{userId}", userId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", is("Failed to delete user")));
    }

    @Test
    void whenSearchUsers_thenUsersAreReturned() throws Exception {
        String query = "test";
        List<UserRepresentation> users = Arrays.asList(
                createUserRepresentation("testuser1", "test1@example.com"),
                createUserRepresentation("testuser2", "test2@example.com")
        );

        when(userService.searchUsers(query)).thenReturn(users);

        mockMvc.perform(get("/api/logistics/users/search")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("testuser1")))
                .andExpect(jsonPath("$[1].username", is("testuser2")));
    }

    @Test
    void whenSearchUsersFails_thenInternalServerError() throws Exception {
        String query = "test";

        when(userService.searchUsers(query))
                .thenThrow(new RuntimeException("Search failed"));

        mockMvc.perform(get("/api/logistics/users/search")
                        .param("query", query))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", is("Failed to search users")));
    }

    private UserRepresentation createUserRepresentation(String username, String email) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(true);
        return user;
    }
}

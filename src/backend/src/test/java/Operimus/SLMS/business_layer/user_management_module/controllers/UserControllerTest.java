package Operimus.SLMS.business_layer.user_management_module.controllers;

import Operimus.SLMS.business_layer.user_management_module.config.SecurityConfig;
import Operimus.SLMS.business_layer.user_management_module.dto.UpdateUserDTO;
import Operimus.SLMS.business_layer.user_management_module.dto.UserDTO;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class,
    excludeAutoConfiguration = {OAuth2ResourceServerAutoConfiguration.class})
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KeycloakUserService userService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void whenRegisterUser_thenUserIsCreated() throws Exception {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User",
                Arrays.asList("CUSTOMER")
        );

        UserRepresentation createdUser = createUserRepresentation("testuser", "test@example.com");

        when(userService.createUser(any(UserDTO.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/users/register")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    void whenRegisterUserWithInvalidData_thenBadRequest() throws Exception {
        UserDTO invalidUserDTO = new UserDTO(
                "",
                "invalid-email",
                "",
                "",
                "",
                Arrays.asList()
        );

        mockMvc.perform(post("/api/users/register")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenRegisterUserFails_thenReturnError() throws Exception {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User",
                Arrays.asList("CUSTOMER")
        );

        when(userService.createUser(any(UserDTO.class)))
                .thenThrow(new RuntimeException("User already exists"));

        mockMvc.perform(post("/api/users/register")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("User already exists")));
    }

    @Test
    void whenGetCurrentUser_thenUserInfoIsReturned() throws Exception {
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", Arrays.asList("CUSTOMER"));

        mockMvc.perform(get("/api/users/me")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("test-user-id")
                                        .claim("preferred_username", "testuser")
                                        .claim("email", "test@example.com")
                                        .claim("given_name", "Test")
                                        .claim("family_name", "User")
                                        .claim("realm_access", realmAccess)
                                        .issuedAt(Instant.now())
                                        .expiresAt(Instant.now().plusSeconds(3600))
                                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sub", is("test-user-id")))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.firstName", is("Test")))
                .andExpect(jsonPath("$.lastName", is("User")));
    }

    @Test
    void whenGetCurrentUserWithoutAuth_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void whenGetUserById_thenUserIsReturned() throws Exception {
        String userId = "test-user-id";
        UserRepresentation user = createUserRepresentation("testuser", "test@example.com");

        when(userService.getUserById(userId)).thenReturn(user);

        mockMvc.perform(get("/api/users/{userId}", userId)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    @WithMockUser
    void whenGetUserByIdNotFound_thenNotFound() throws Exception {
        String userId = "non-existent-user-id";

        when(userService.getUserById(userId))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/users/{userId}", userId)
                        .with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("User not found")));
    }

    @Test
    @WithMockUser
    void whenSearchUsers_thenUsersAreReturned() throws Exception {
        String query = "test";
        List<UserRepresentation> users = Arrays.asList(
                createUserRepresentation("testuser1", "test1@example.com"),
                createUserRepresentation("testuser2", "test2@example.com")
        );

        when(userService.searchUsers(query)).thenReturn(users);

        mockMvc.perform(get("/api/users/search")
                        .param("query", query)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("testuser1")))
                .andExpect(jsonPath("$[1].username", is("testuser2")));
    }

    @Test
    @WithMockUser
    void whenSearchUsersFails_thenInternalServerError() throws Exception {
        String query = "test";

        when(userService.searchUsers(query))
                .thenThrow(new RuntimeException("Search failed"));

        mockMvc.perform(get("/api/users/search")
                        .param("query", query)
                        .with(jwt()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", is("Failed to search users")));
    }

    @Test
    @WithMockUser
    void whenDeleteUser_thenUserIsDeleted() throws Exception {
        String userId = "test-user-id";

        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/users/{userId}", userId)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User deleted successfully")));
    }

    @Test
    @WithMockUser
    void whenDeleteUserFails_thenInternalServerError() throws Exception {
        String userId = "test-user-id";

        doThrow(new RuntimeException("Delete failed"))
                .when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/users/{userId}", userId)
                        .with(jwt()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", is("Failed to delete user")));
    }

    @Test
    void whenAccessProtectedEndpointWithoutAuth_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/test-user-id"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
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

        mockMvc.perform(put("/api/users/{userId}", userId)
                        .with(jwt())
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
    @WithMockUser
    void whenUpdateUserPartialFields_thenOnlyProvidedFieldsAreUpdated() throws Exception {
        String userId = "test-user-id";
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setEmail("newemail@example.com");

        UserRepresentation updatedUser = createUserRepresentation("testuser", "newemail@example.com");

        when(userService.updateUser(anyString(), any(UpdateUserDTO.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{userId}", userId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("newemail@example.com")));
    }

    @Test
    @WithMockUser
    void whenUpdateUserWithInvalidData_thenBadRequest() throws Exception {
        String userId = "test-user-id";
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setEmail("invalid-email");

        mockMvc.perform(put("/api/users/{userId}", userId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
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

        mockMvc.perform(put("/api/users/{userId}", userId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Update failed")));
    }

    @Test
    @WithMockUser
    void whenGetAllUsers_thenUsersAreReturned() throws Exception {
        List<UserRepresentation> users = Arrays.asList(
                createUserRepresentation("user1", "user1@example.com"),
                createUserRepresentation("user2", "user2@example.com")
        );

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("user1")))
                .andExpect(jsonPath("$[1].username", is("user2")));
    }

    @Test
    @WithMockUser
    void whenGetAllUsersFails_thenInternalServerError() throws Exception {
        when(userService.getAllUsers())
                .thenThrow(new RuntimeException("Failed to fetch users"));

        mockMvc.perform(get("/api/users")
                        .with(jwt()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", is("Failed to fetch users")));
    }

    private UserRepresentation createUserRepresentation(String username, String email) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(true);
        return user;
    }
}

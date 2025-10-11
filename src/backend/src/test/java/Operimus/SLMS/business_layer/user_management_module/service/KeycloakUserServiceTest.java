package Operimus.SLMS.business_layer.user_management_module.service;

import Operimus.SLMS.business_layer.user_management_module.dto.UserDTO;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakUserServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Mock
    private RolesResource rolesResource;

    @Mock
    private RoleResource roleResource;

    @Mock
    private RoleMappingResource roleMappingResource;

    @Mock
    private RoleScopeResource roleScopeResource;

    @Mock
    private Response response;

    @InjectMocks
    private KeycloakUserService keycloakUserService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(keycloakUserService, "keycloakServerUrl", "http://localhost:8180");
        ReflectionTestUtils.setField(keycloakUserService, "realm", "slms");
        ReflectionTestUtils.setField(keycloakUserService, "adminClientId", "slms-backend");
        ReflectionTestUtils.setField(keycloakUserService, "adminClientSecret", "test-secret");
        ReflectionTestUtils.setField(keycloakUserService, "keycloak", keycloak);
    }

    @Test
    void whenCreateUser_thenUserIsCreatedSuccessfully() {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User",
                Arrays.asList("CUSTOMER")
        );

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.getHeaderString("Location")).thenReturn("http://localhost:8180/admin/realms/slms/users/test-user-id");
        when(usersResource.get(anyString())).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(createUserRepresentation(userDTO));
        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get(anyString())).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(createRoleRepresentation("CUSTOMER"));
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        UserRepresentation result = keycloakUserService.createUser(userDTO);

        assertNotNull(result);
        assertEquals(userDTO.getUsername(), result.getUsername());
        assertEquals(userDTO.getEmail(), result.getEmail());
        verify(usersResource).create(any(UserRepresentation.class));
        verify(userResource).resetPassword(any(CredentialRepresentation.class));
        verify(roleScopeResource).add(anyList());
    }

    @Test
    void whenCreateUserFails_thenThrowsException() {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User",
                Arrays.asList("CUSTOMER")
        );

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(409);
        when(response.readEntity(String.class)).thenReturn("User already exists");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            keycloakUserService.createUser(userDTO);
        });

        assertTrue(exception.getMessage().contains("Failed to create user"));
    }

    @Test
    void whenGetUserById_thenUserIsReturned() {
        String userId = "test-user-id";
        UserRepresentation expectedUser = new UserRepresentation();
        expectedUser.setId(userId);
        expectedUser.setUsername("testuser");

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(expectedUser);

        UserRepresentation result = keycloakUserService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(usersResource).get(userId);
    }

    @Test
    void whenGetUserByIdFails_thenThrowsException() {
        String userId = "non-existent-user-id";

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenThrow(new RuntimeException("User not found"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            keycloakUserService.getUserById(userId);
        });

        assertTrue(exception.getMessage().contains("Failed to fetch user"));
    }

    @Test
    void whenSearchUsers_thenUsersAreReturned() {
        String searchQuery = "test";
        List<UserRepresentation> expectedUsers = Arrays.asList(
                createUserRepresentation("testuser1", "test1@example.com"),
                createUserRepresentation("testuser2", "test2@example.com")
        );

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search(searchQuery)).thenReturn(expectedUsers);

        List<UserRepresentation> result = keycloakUserService.searchUsers(searchQuery);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(usersResource).search(searchQuery);
    }

    @Test
    void whenSearchUsersFails_thenThrowsException() {
        String searchQuery = "test";

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search(searchQuery)).thenThrow(new RuntimeException("Search failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            keycloakUserService.searchUsers(searchQuery);
        });

        assertTrue(exception.getMessage().contains("Failed to search users"));
    }

    @Test
    void whenDeleteUser_thenUserIsDeleted() {
        String userId = "test-user-id";

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.delete(userId)).thenReturn(response);

        assertDoesNotThrow(() -> keycloakUserService.deleteUser(userId));

        verify(usersResource).delete(userId);
    }

    @Test
    void whenDeleteUserFails_thenThrowsException() {
        String userId = "test-user-id";

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.delete(userId)).thenThrow(new RuntimeException("Delete failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            keycloakUserService.deleteUser(userId);
        });

        assertTrue(exception.getMessage().contains("Failed to delete user"));
    }

    @Test
    void whenAssignRoleNotFound_thenSkipsRole() {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User",
                Arrays.asList("CUSTOMER", "NON_EXISTENT_ROLE")
        );

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.getHeaderString("Location")).thenReturn("http://localhost:8180/admin/realms/slms/users/test-user-id");
        when(usersResource.get(anyString())).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(createUserRepresentation(userDTO));
        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("CUSTOMER")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(createRoleRepresentation("CUSTOMER"));
        when(rolesResource.get("NON_EXISTENT_ROLE")).thenThrow(new RuntimeException("Role not found"));
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        UserRepresentation result = keycloakUserService.createUser(userDTO);

        assertNotNull(result);
        verify(roleScopeResource).add(argThat(roles -> roles.size() == 1));
    }

    private UserRepresentation createUserRepresentation(UserDTO userDTO) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(true);
        return user;
    }

    private UserRepresentation createUserRepresentation(String username, String email) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(true);
        return user;
    }

    private RoleRepresentation createRoleRepresentation(String roleName) {
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        return role;
    }
}

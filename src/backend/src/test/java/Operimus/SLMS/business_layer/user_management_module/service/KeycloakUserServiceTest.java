package Operimus.SLMS.business_layer.user_management_module.service;

import Operimus.SLMS.business_layer.user_management_module.dto.UserDTO;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
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
    private GroupsResource groupsResource;

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

        String userId = "test-user-id";
        String groupId = "customers-group-id";
        Response mockResponse = createMockResponse(201, "http://localhost:8180/admin/realms/slms/users/" + userId);

        GroupRepresentation customersGroup = new GroupRepresentation();
        customersGroup.setId(groupId);
        customersGroup.setName("Customers");

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(createUserRepresentation(userDTO));
        when(realmResource.groups()).thenReturn(groupsResource);
        when(groupsResource.groups("Customers", 0, 1)).thenReturn(Arrays.asList(customersGroup));

        UserRepresentation result = keycloakUserService.createUser(userDTO);

        assertNotNull(result);
        assertEquals(userDTO.getUsername(), result.getUsername());
        assertEquals(userDTO.getEmail(), result.getEmail());
        verify(usersResource).create(any(UserRepresentation.class));
        verify(userResource).joinGroup(groupId);
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

        Response mockResponse = createMockResponse(409, null);
        when(mockResponse.readEntity(String.class)).thenReturn("User already exists");

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);

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

        Response mockResponse = createMockResponse(204, null);

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.delete(userId)).thenReturn(mockResponse);

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
    void whenAssignGroupNotFound_thenLogsWarningAndContinues() {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User",
                Arrays.asList("CUSTOMER", "NON_EXISTENT_ROLE")
        );

        String userId = "test-user-id";
        String groupId = "customers-group-id";
        Response mockResponse = createMockResponse(201, "http://localhost:8180/admin/realms/slms/users/" + userId);

        GroupRepresentation customersGroup = new GroupRepresentation();
        customersGroup.setId(groupId);
        customersGroup.setName("Customers");

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(createUserRepresentation(userDTO));
        when(realmResource.groups()).thenReturn(groupsResource);
        // Setup mock to return Customers group for "Customers" and empty list for other group names
        when(groupsResource.groups(anyString(), eq(0), eq(1)))
                .thenAnswer(invocation -> {
                    String groupName = invocation.getArgument(0);
                    if ("Customers".equals(groupName)) {
                        return Arrays.asList(customersGroup);
                    }
                    return Arrays.asList(); // Non-existent group returns empty list
                });

        UserRepresentation result = keycloakUserService.createUser(userDTO);

        assertNotNull(result);
        // Verify user was added to the Customers group
        verify(userResource).joinGroup(groupId);
        // Verify joinGroup was only called once (for CUSTOMER, not for NON_EXISTENT_ROLE)
        verify(userResource, times(1)).joinGroup(anyString());
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

    private Response createMockResponse(int statusCode, String location) {
        Response mockResponse = mock(Response.class, withSettings().lenient());
        Response.Status status = Response.Status.fromStatusCode(statusCode);

        when(mockResponse.getStatusInfo()).thenReturn(status);
        when(mockResponse.getStatus()).thenReturn(statusCode);
        if (location != null) {
            try {
                URI locationUri = URI.create(location);
                when(mockResponse.getLocation()).thenReturn(locationUri);
                when(mockResponse.getHeaderString("Location")).thenReturn(location);
            } catch (Exception e) {
                throw new RuntimeException("Invalid location URI: " + location, e);
            }
        }
        return mockResponse;
    }
}

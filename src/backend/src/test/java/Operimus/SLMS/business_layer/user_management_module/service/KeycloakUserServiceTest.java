package Operimus.SLMS.business_layer.user_management_module.service;

import Operimus.SLMS.business_layer.user_management_module.dto.UpdateUserDTO;
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
        when(userResource.groups()).thenReturn(Arrays.asList());

        UserRepresentation result = keycloakUserService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(usersResource, times(2)).get(userId);
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
        UserRepresentation user1 = createUserRepresentation("testuser1", "test1@example.com");
        user1.setId("user1-id");
        UserRepresentation user2 = createUserRepresentation("testuser2", "test2@example.com");
        user2.setId("user2-id");
        List<UserRepresentation> expectedUsers = Arrays.asList(user1, user2);

        UserResource userResource1 = mock(UserResource.class);
        UserResource userResource2 = mock(UserResource.class);

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search(searchQuery)).thenReturn(expectedUsers);
        when(usersResource.get("user1-id")).thenReturn(userResource1);
        when(usersResource.get("user2-id")).thenReturn(userResource2);
        when(userResource1.groups()).thenReturn(Arrays.asList());
        when(userResource2.groups()).thenReturn(Arrays.asList());

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

    @Test
    void whenUpdateUserWithUpdateUserDTO_thenUserIsUpdated() {
        String userId = "test-user-id";
        UpdateUserDTO updateUserDTO = new UpdateUserDTO(
                "updateduser",
                "updated@example.com",
                "Updated",
                "User",
                Arrays.asList("DRIVER")
        );

        String driverGroupId = "drivers-group-id";
        GroupRepresentation driversGroup = new GroupRepresentation();
        driversGroup.setId(driverGroupId);
        driversGroup.setName("Drivers");

        UserRepresentation existingUser = new UserRepresentation();
        existingUser.setId(userId);
        existingUser.setUsername("testuser");
        existingUser.setEmail("test@example.com");

        UserRepresentation updatedUser = new UserRepresentation();
        updatedUser.setId(userId);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");
        updatedUser.setGroups(Arrays.asList("Drivers"));

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation())
                .thenReturn(existingUser)
                .thenReturn(updatedUser);
        when(userResource.groups())
                .thenReturn(Arrays.asList())
                .thenReturn(Arrays.asList(driversGroup));
        when(realmResource.groups()).thenReturn(groupsResource);
        when(groupsResource.groups("Drivers", 0, 1)).thenReturn(Arrays.asList(driversGroup));

        UserRepresentation result = keycloakUserService.updateUser(userId, updateUserDTO);

        assertNotNull(result);
        assertEquals("updateduser", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("Updated", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertNotNull(result.getGroups());
        assertEquals(1, result.getGroups().size());
        assertEquals("Drivers", result.getGroups().get(0));
        verify(userResource).update(any(UserRepresentation.class));
        verify(userResource).joinGroup(driverGroupId);
    }

    @Test
    void whenUpdateUserPartialFields_thenOnlyProvidedFieldsUpdated() {
        String userId = "test-user-id";
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setEmail("newemail@example.com");

        UserRepresentation existingUser = new UserRepresentation();
        existingUser.setId(userId);
        existingUser.setUsername("testuser");
        existingUser.setEmail("old@example.com");
        existingUser.setFirstName("Test");
        existingUser.setLastName("User");

        UserRepresentation updatedUser = new UserRepresentation();
        updatedUser.setId(userId);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("newemail@example.com");
        updatedUser.setFirstName("Test");
        updatedUser.setLastName("User");

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation())
                .thenReturn(existingUser)
                .thenReturn(updatedUser);
        when(userResource.groups()).thenReturn(Arrays.asList());

        UserRepresentation result = keycloakUserService.updateUser(userId, updateUserDTO);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("newemail@example.com", result.getEmail());
        verify(userResource).update(any(UserRepresentation.class));
    }

    @Test
    void whenGetUserByIdWithGroups_thenGroupsAreReturned() {
        String userId = "test-user-id";
        UserRepresentation user = new UserRepresentation();
        user.setId(userId);
        user.setUsername("testuser");

        GroupRepresentation customersGroup = new GroupRepresentation();
        customersGroup.setId("customers-group-id");
        customersGroup.setName("Customers");

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(user);
        when(userResource.groups()).thenReturn(Arrays.asList(customersGroup));

        UserRepresentation result = keycloakUserService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertNotNull(result.getGroups());
        assertEquals(1, result.getGroups().size());
        assertEquals("Customers", result.getGroups().get(0));
    }

    @Test
    void whenGetAllUsersWithGroups_thenGroupsAreReturnedForEachUser() {
        UserRepresentation user1 = createUserRepresentation("user1", "user1@example.com");
        user1.setId("user1-id");
        UserRepresentation user2 = createUserRepresentation("user2", "user2@example.com");
        user2.setId("user2-id");

        GroupRepresentation customersGroup = new GroupRepresentation();
        customersGroup.setId("customers-group-id");
        customersGroup.setName("Customers");

        GroupRepresentation driversGroup = new GroupRepresentation();
        driversGroup.setId("drivers-group-id");
        driversGroup.setName("Drivers");

        UserResource userResource1 = mock(UserResource.class);
        UserResource userResource2 = mock(UserResource.class);

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.list()).thenReturn(Arrays.asList(user1, user2));
        when(usersResource.get("user1-id")).thenReturn(userResource1);
        when(usersResource.get("user2-id")).thenReturn(userResource2);
        when(userResource1.groups()).thenReturn(Arrays.asList(customersGroup));
        when(userResource2.groups()).thenReturn(Arrays.asList(driversGroup));

        List<UserRepresentation> result = keycloakUserService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals(1, result.get(0).getGroups().size());
        assertEquals("Customers", result.get(0).getGroups().get(0));
        assertEquals("user2", result.get(1).getUsername());
        assertEquals(1, result.get(1).getGroups().size());
        assertEquals("Drivers", result.get(1).getGroups().get(0));
    }

    @Test
    void whenSearchUsersWithGroups_thenGroupsAreReturnedForEachUser() {
        String searchQuery = "test";
        UserRepresentation user1 = createUserRepresentation("testuser1", "test1@example.com");
        user1.setId("user1-id");

        GroupRepresentation customersGroup = new GroupRepresentation();
        customersGroup.setId("customers-group-id");
        customersGroup.setName("Customers");

        UserResource userResource1 = mock(UserResource.class);

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search(searchQuery)).thenReturn(Arrays.asList(user1));
        when(usersResource.get("user1-id")).thenReturn(userResource1);
        when(userResource1.groups()).thenReturn(Arrays.asList(customersGroup));

        List<UserRepresentation> result = keycloakUserService.searchUsers(searchQuery);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser1", result.get(0).getUsername());
        assertEquals(1, result.get(0).getGroups().size());
        assertEquals("Customers", result.get(0).getGroups().get(0));
    }

    @Test
    void whenCreateUserWithGroups_thenGroupsAreReturnedInCreatedUser() {
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

        UserRepresentation createdUser = createUserRepresentation(userDTO);
        createdUser.setGroups(Arrays.asList("Customers"));

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(createdUser);
        when(userResource.groups()).thenReturn(Arrays.asList(customersGroup));
        when(realmResource.groups()).thenReturn(groupsResource);
        when(groupsResource.groups("Customers", 0, 1)).thenReturn(Arrays.asList(customersGroup));

        UserRepresentation result = keycloakUserService.createUser(userDTO);

        assertNotNull(result);
        assertEquals(userDTO.getUsername(), result.getUsername());
        assertNotNull(result.getGroups());
        assertEquals(1, result.getGroups().size());
        assertEquals("Customers", result.getGroups().get(0));
        verify(userResource).joinGroup(groupId);
    }

    @Test
    void whenUpdateUserRoles_thenOldGroupsRemovedAndNewGroupsAdded() {
        String userId = "test-user-id";
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setRoles(Arrays.asList("DRIVER"));

        String customersGroupId = "customers-group-id";
        String driversGroupId = "drivers-group-id";

        GroupRepresentation customersGroup = new GroupRepresentation();
        customersGroup.setId(customersGroupId);
        customersGroup.setName("Customers");

        GroupRepresentation driversGroup = new GroupRepresentation();
        driversGroup.setId(driversGroupId);
        driversGroup.setName("Drivers");

        UserRepresentation existingUser = new UserRepresentation();
        existingUser.setId(userId);
        existingUser.setUsername("testuser");

        UserRepresentation updatedUser = new UserRepresentation();
        updatedUser.setId(userId);
        updatedUser.setUsername("testuser");
        updatedUser.setGroups(Arrays.asList("Drivers"));

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation())
                .thenReturn(existingUser)
                .thenReturn(updatedUser);
        when(userResource.groups())
                .thenReturn(Arrays.asList(customersGroup))
                .thenReturn(Arrays.asList(driversGroup));
        when(realmResource.groups()).thenReturn(groupsResource);
        when(groupsResource.groups("Drivers", 0, 1)).thenReturn(Arrays.asList(driversGroup));

        UserRepresentation result = keycloakUserService.updateUser(userId, updateUserDTO);

        assertNotNull(result);
        verify(userResource).leaveGroup(customersGroupId);
        verify(userResource).joinGroup(driversGroupId);
        assertNotNull(result.getGroups());
        assertEquals(1, result.getGroups().size());
        assertEquals("Drivers", result.getGroups().get(0));
    }

    // ========================================
    // Get Users By Role Tests
    // ========================================

    @Test
    void whenGetUsersByRole_thenReturnUsersWithThatRole() {
        String roleName = "CUSTOMER_SERVICE_REP";
        String userId1 = "user-1";
        String userId2 = "user-2";
        String userId3 = "user-3";

        // Create users
        UserRepresentation user1 = createUserRepresentation("user1", "user1@test.com");
        user1.setId(userId1);
        user1.setFirstName("John");
        user1.setLastName("Doe");

        UserRepresentation user2 = createUserRepresentation("user2", "user2@test.com");
        user2.setId(userId2);
        user2.setFirstName("Jane");
        user2.setLastName("Smith");

        UserRepresentation user3 = createUserRepresentation("user3", "user3@test.com");
        user3.setId(userId3);
        user3.setFirstName("Bob");
        user3.setLastName("Johnson");

        // Create roles
        RoleRepresentation csrRole = createRoleRepresentation("CUSTOMER_SERVICE_REP");
        RoleRepresentation customerRole = createRoleRepresentation("CUSTOMER");

        // Mock Keycloak responses
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.list()).thenReturn(Arrays.asList(user1, user2, user3));

        // Mock user1 and user2 have CSR role, user3 has CUSTOMER role
        UserResource userResource1 = mock(UserResource.class);
        UserResource userResource2 = mock(UserResource.class);
        UserResource userResource3 = mock(UserResource.class);

        when(usersResource.get(userId1)).thenReturn(userResource1);
        when(usersResource.get(userId2)).thenReturn(userResource2);
        when(usersResource.get(userId3)).thenReturn(userResource3);

        RoleMappingResource roleMappingResource1 = mock(RoleMappingResource.class);
        RoleMappingResource roleMappingResource2 = mock(RoleMappingResource.class);
        RoleMappingResource roleMappingResource3 = mock(RoleMappingResource.class);

        when(userResource1.roles()).thenReturn(roleMappingResource1);
        when(userResource2.roles()).thenReturn(roleMappingResource2);
        when(userResource3.roles()).thenReturn(roleMappingResource3);

        RoleScopeResource roleScopeResource1 = mock(RoleScopeResource.class);
        RoleScopeResource roleScopeResource2 = mock(RoleScopeResource.class);
        RoleScopeResource roleScopeResource3 = mock(RoleScopeResource.class);

        when(roleMappingResource1.realmLevel()).thenReturn(roleScopeResource1);
        when(roleMappingResource2.realmLevel()).thenReturn(roleScopeResource2);
        when(roleMappingResource3.realmLevel()).thenReturn(roleScopeResource3);

        when(roleScopeResource1.listEffective()).thenReturn(Arrays.asList(csrRole));
        when(roleScopeResource2.listEffective()).thenReturn(Arrays.asList(csrRole));
        when(roleScopeResource3.listEffective()).thenReturn(Arrays.asList(customerRole));

        // Execute
        List<UserRepresentation> result = keycloakUserService.getUsersByRole(roleName);

        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(userId1)));
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(userId2)));
        assertFalse(result.stream().anyMatch(u -> u.getId().equals(userId3)));

        verify(usersResource).list();
        verify(usersResource).get(userId1);
        verify(usersResource).get(userId2);
        verify(usersResource).get(userId3);
    }

    @Test
    void whenGetUsersByRoleCaseInsensitive_thenReturnUsersWithThatRole() {
        String roleName = "customer_service_rep"; // lowercase
        String userId = "user-1";

        UserRepresentation user = createUserRepresentation("user1", "user1@test.com");
        user.setId(userId);
        user.setFirstName("John");
        user.setLastName("Doe");

        RoleRepresentation csrRole = createRoleRepresentation("CUSTOMER_SERVICE_REP"); // uppercase in Keycloak

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.list()).thenReturn(Arrays.asList(user));

        UserResource mockUserResource = mock(UserResource.class);
        when(usersResource.get(userId)).thenReturn(mockUserResource);

        RoleMappingResource mockRoleMappingResource = mock(RoleMappingResource.class);
        when(mockUserResource.roles()).thenReturn(mockRoleMappingResource);

        RoleScopeResource mockRoleScopeResource = mock(RoleScopeResource.class);
        when(mockRoleMappingResource.realmLevel()).thenReturn(mockRoleScopeResource);
        when(mockRoleScopeResource.listEffective()).thenReturn(Arrays.asList(csrRole));

        List<UserRepresentation> result = keycloakUserService.getUsersByRole(roleName);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getId());
    }

    @Test
    void whenGetUsersByRoleNoMatches_thenReturnEmptyList() {
        String roleName = "NONEXISTENT_ROLE";
        String userId = "user-1";

        UserRepresentation user = createUserRepresentation("user1", "user1@test.com");
        user.setId(userId);

        RoleRepresentation customerRole = createRoleRepresentation("CUSTOMER");

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.list()).thenReturn(Arrays.asList(user));

        UserResource mockUserResource = mock(UserResource.class);
        when(usersResource.get(userId)).thenReturn(mockUserResource);

        RoleMappingResource mockRoleMappingResource = mock(RoleMappingResource.class);
        when(mockUserResource.roles()).thenReturn(mockRoleMappingResource);

        RoleScopeResource mockRoleScopeResource = mock(RoleScopeResource.class);
        when(mockRoleMappingResource.realmLevel()).thenReturn(mockRoleScopeResource);
        when(mockRoleScopeResource.listEffective()).thenReturn(Arrays.asList(customerRole));

        List<UserRepresentation> result = keycloakUserService.getUsersByRole(roleName);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void whenGetUsersByRoleEmptyUserList_thenReturnEmptyList() {
        String roleName = "CUSTOMER_SERVICE_REP";

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.list()).thenReturn(Arrays.asList());

        List<UserRepresentation> result = keycloakUserService.getUsersByRole(roleName);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(usersResource).list();
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

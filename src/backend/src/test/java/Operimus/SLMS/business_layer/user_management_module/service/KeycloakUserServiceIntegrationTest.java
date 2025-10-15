package Operimus.SLMS.business_layer.user_management_module.service;

import Operimus.SLMS.business_layer.user_management_module.dto.UserDTO;
import Operimus.SLMS.config.TestSecurityConfig;
import org.junit.jupiter.api.*;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test for KeycloakUserService that tests real Keycloak interactions.
 *
 * This test requires Keycloak to be running on the configured endpoint.
 * For local testing, start Keycloak with: docker-compose -f infra/docker-compose.dev.yml up keycloak -d
 *
 * These tests will:
 * 1. Connect to real Keycloak instance
 * 2. Obtain admin token using client credentials
 * 3. Create, read, search, and delete users
 * 4. Assign and verify roles
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KeycloakUserServiceIntegrationTest {

    @Autowired
    private KeycloakUserService keycloakUserService;

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String adminClientId;

    @Value("${keycloak.admin.client-secret}")
    private String adminClientSecret;

    private Keycloak keycloakAdminClient;
    private static String testUserId;
    private static String testUsername;

    @BeforeEach
    void setUp() {
        // Initialize Keycloak admin client for verification
        keycloakAdminClient = KeycloakBuilder.builder()
                .serverUrl(keycloakServerUrl)
                .realm(realm)
                .clientId(adminClientId)
                .clientSecret(adminClientSecret)
                .grantType("client_credentials")
                .build();

        // Generate unique username for each test run
        testUsername = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @AfterEach
    void tearDown() {
        // Clean up test user if it was created
        if (testUserId != null) {
            try {
                keycloakAdminClient.realm(realm).users().delete(testUserId);
            } catch (Exception e) {
                // Ignore cleanup errors
            }
            testUserId = null;
        }

        if (keycloakAdminClient != null) {
            keycloakAdminClient.close();
        }
    }

    @Test
    @Order(1)
    void testKeycloakConnection() {
        // Test that we can connect to Keycloak and get realm info
        assertThatCode(() -> {
            RealmResource realmResource = keycloakAdminClient.realm(realm);
            String realmName = realmResource.toRepresentation().getRealm();
            assertThat(realmName).isEqualTo(realm);
        }).as("Should be able to connect to Keycloak and retrieve realm information")
                .doesNotThrowAnyException();
    }

    @Test
    @Order(2)
    void testGetAdminToken() {
        // Test that we can obtain an access token using client credentials
        assertThatCode(() -> {
            String token = keycloakAdminClient.tokenManager().getAccessTokenString();
            assertThat(token).isNotNull().isNotEmpty();
        }).as("Should be able to obtain admin access token")
                .doesNotThrowAnyException();
    }

    @Test
    @Order(3)
    void testCreateUser() {
        // Given
        UserDTO userDTO = new UserDTO(
                testUsername,
                testUsername + "@test.com",
                "Test123!@#",
                "Integration",
                "Test",
                Arrays.asList("CUSTOMER")
        );

        // When
        UserRepresentation createdUser = keycloakUserService.createUser(userDTO);
        testUserId = createdUser.getId();

        // Then
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getUsername()).isEqualTo(testUsername);
        assertThat(createdUser.getEmail()).isEqualTo(userDTO.getEmail());
        assertThat(createdUser.getFirstName()).isEqualTo(userDTO.getFirstName());
        assertThat(createdUser.getLastName()).isEqualTo(userDTO.getLastName());
        assertThat(createdUser.isEnabled()).isTrue();
        assertThat(createdUser.isEmailVerified()).isTrue();

        // Verify user actually exists in Keycloak
        UserRepresentation verifyUser = keycloakAdminClient.realm(realm)
                .users()
                .get(testUserId)
                .toRepresentation();
        assertThat(verifyUser).isNotNull();
        assertThat(verifyUser.getUsername()).isEqualTo(testUsername);
    }

    @Test
    @Order(4)
    void testCreateUserWithMultipleRoles() {
        // Given
        UserDTO userDTO = new UserDTO(
                testUsername,
                testUsername + "@test.com",
                "Test123!@#",
                "Integration",
                "Test",
                Arrays.asList("CUSTOMER", "DRIVER")
        );

        // When
        UserRepresentation createdUser = keycloakUserService.createUser(userDTO);
        testUserId = createdUser.getId();

        // Then
        assertThat(createdUser).isNotNull();

        // Verify roles were assigned
        var roles = keycloakAdminClient.realm(realm)
                .users()
                .get(testUserId)
                .roles()
                .realmLevel()
                .listEffective();

        List<String> assignedRoleNames = roles.stream()
                .map(org.keycloak.representations.idm.RoleRepresentation::getName)
                .toList();

        assertThat(assignedRoleNames)
                .contains("CUSTOMER", "DRIVER");
    }

    @Test
    @Order(5)
    void testGetUserById() {
        // Given - Create a test user first
        UserDTO userDTO = new UserDTO(
                testUsername,
                testUsername + "@test.com",
                "Test123!@#",
                "Get",
                "Test",
                Arrays.asList("CUSTOMER")
        );

        UserRepresentation createdUser = keycloakUserService.createUser(userDTO);
        testUserId = createdUser.getId();

        // When
        UserRepresentation fetchedUser = keycloakUserService.getUserById(testUserId);

        // Then
        assertThat(fetchedUser).isNotNull();
        assertThat(fetchedUser.getId()).isEqualTo(testUserId);
        assertThat(fetchedUser.getUsername()).isEqualTo(testUsername);
        assertThat(fetchedUser.getEmail()).isEqualTo(userDTO.getEmail());
    }

    @Test
    @Order(6)
    void testSearchUsers() {
        // Given - Create a test user first
        UserDTO userDTO = new UserDTO(
                testUsername,
                testUsername + "@test.com",
                "Test123!@#",
                "Search",
                "Test",
                Arrays.asList("CUSTOMER")
        );

        UserRepresentation createdUser = keycloakUserService.createUser(userDTO);
        testUserId = createdUser.getId();

        // When - Search by username prefix
        List<UserRepresentation> results = keycloakUserService.searchUsers(testUsername.substring(0, 10));

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results)
                .extracting(UserRepresentation::getUsername)
                .contains(testUsername);
    }

    @Test
    @Order(7)
    void testDeleteUser() {
        // Given - Create a test user first
        UserDTO userDTO = new UserDTO(
                testUsername,
                testUsername + "@test.com",
                "Test123!@#",
                "Delete",
                "Test",
                Arrays.asList("CUSTOMER")
        );

        UserRepresentation createdUser = keycloakUserService.createUser(userDTO);
        testUserId = createdUser.getId();

        // Verify user exists
        UserRepresentation existingUser = keycloakUserService.getUserById(testUserId);
        assertThat(existingUser).isNotNull();

        // When
        keycloakUserService.deleteUser(testUserId);
        testUserId = null; // Mark as deleted so tearDown doesn't try to delete again

        // Then - Verify user no longer exists
        assertThatThrownBy(() -> keycloakUserService.getUserById(existingUser.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch user");
    }

    @Test
    @Order(8)
    void testCreateUserWithInvalidRole() {
        // Given - User with non-existent role
        UserDTO userDTO = new UserDTO(
                testUsername,
                testUsername + "@test.com",
                "Test123!@#",
                "Invalid",
                "Role",
                Arrays.asList("CUSTOMER", "NON_EXISTENT_ROLE")
        );

        // When - Service should skip invalid roles and continue
        UserRepresentation createdUser = keycloakUserService.createUser(userDTO);
        testUserId = createdUser.getId();

        // Then - User should be created with only valid roles
        assertThat(createdUser).isNotNull();

        var roles = keycloakAdminClient.realm(realm)
                .users()
                .get(testUserId)
                .roles()
                .realmLevel()
                .listEffective();

        List<String> assignedRoleNames = roles.stream()
                .map(org.keycloak.representations.idm.RoleRepresentation::getName)
                .toList();

        assertThat(assignedRoleNames).contains("CUSTOMER");
        assertThat(assignedRoleNames).doesNotContain("NON_EXISTENT_ROLE");
    }

    @Test
    @Order(9)
    void testCreateDuplicateUser() {
        // Given - Create first user
        UserDTO userDTO1 = new UserDTO(
                testUsername,
                testUsername + "@test.com",
                "Test123!@#",
                "Duplicate",
                "Test",
                Arrays.asList("CUSTOMER")
        );

        UserRepresentation firstUser = keycloakUserService.createUser(userDTO1);
        testUserId = firstUser.getId();

        // When - Try to create second user with same username
        UserDTO userDTO2 = new UserDTO(
                testUsername, // Same username
                "different@test.com",
                "Test123!@#",
                "Another",
                "User",
                Arrays.asList("CUSTOMER")
        );

        // Then - Should throw exception
        assertThatThrownBy(() -> keycloakUserService.createUser(userDTO2))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create user");
    }

    @Test
    @Order(10)
    void testGetNonExistentUser() {
        // Given
        String nonExistentUserId = UUID.randomUUID().toString();

        // When/Then
        assertThatThrownBy(() -> keycloakUserService.getUserById(nonExistentUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch user");
    }

    @Test
    @Order(11)
    void testSearchNonExistentUsers() {
        // Given
        String searchQuery = "nonexistentuserpattern" + UUID.randomUUID();

        // When
        List<UserRepresentation> results = keycloakUserService.searchUsers(searchQuery);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    @Order(12)
    void testCreateMultipleUsersSequentially() {
        // Given
        int numberOfUsers = 3;
        String[] userIds = new String[numberOfUsers];

        try {
            // When - Create multiple users
            for (int i = 0; i < numberOfUsers; i++) {
                String uniqueUsername = "multitest_" + UUID.randomUUID().toString().substring(0, 8);
                UserDTO userDTO = new UserDTO(
                        uniqueUsername,
                        uniqueUsername + "@test.com",
                        "Test123!@#",
                        "Multi" + i,
                        "Test",
                        Arrays.asList("CUSTOMER")
                );

                UserRepresentation createdUser = keycloakUserService.createUser(userDTO);
                userIds[i] = createdUser.getId();

                assertThat(createdUser).isNotNull();
                assertThat(createdUser.getUsername()).isEqualTo(uniqueUsername);
            }

            // Then - Verify all users exist
            for (String userId : userIds) {
                UserRepresentation user = keycloakUserService.getUserById(userId);
                assertThat(user).isNotNull();
            }

        } finally {
            // Cleanup all created users
            for (String userId : userIds) {
                if (userId != null) {
                    try {
                        keycloakAdminClient.realm(realm).users().delete(userId);
                    } catch (Exception e) {
                        // Ignore cleanup errors
                    }
                }
            }
        }
    }
}

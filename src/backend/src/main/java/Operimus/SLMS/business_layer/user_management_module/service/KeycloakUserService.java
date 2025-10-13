package Operimus.SLMS.business_layer.user_management_module.service;

import Operimus.SLMS.business_layer.user_management_module.dto.UserDTO;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KeycloakUserService {

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String adminClientId;

    @Value("${keycloak.admin.client-secret}")
    private String adminClientSecret;

    private Keycloak keycloak;

    @PostConstruct
    public void init() {
        log.info("Initializing Keycloak Admin Client for realm: {}", realm);
        keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakServerUrl)
                .realm(realm)
                .clientId(adminClientId)
                .clientSecret(adminClientSecret)
                .grantType("client_credentials")
                .build();
    }

    public UserRepresentation createUser(UserDTO userDTO) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Create user representation
            UserRepresentation user = new UserRepresentation();
            user.setUsername(userDTO.getUsername());
            user.setEmail(userDTO.getEmail());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setEnabled(true);
            user.setEmailVerified(true);

            // Create user
            Response response = usersResource.create(user);

            if (response.getStatus() != 201) {
                String errorMessage = response.readEntity(String.class);
                log.error("Failed to create user: {} - {}", response.getStatus(), errorMessage);
                throw new RuntimeException("Failed to create user: " + errorMessage);
            }

            String userId = CreatedResponseUtil.getCreatedId(response);
            log.info("User created successfully with ID: {}", userId);

            // Set password
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setTemporary(false);
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(userDTO.getPassword());

            usersResource.get(userId).resetPassword(credential);
            log.info("Password set for user: {}", userId);

            // Assign roles
            assignRoles(userId, userDTO.getRoles());
            log.info("Roles assigned to user: {}", userId);

            // Fetch and return the created user
            return usersResource.get(userId).toRepresentation();

        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }

    private void assignRoles(String userId, List<String> roleNames) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            List<RoleRepresentation> roles = roleNames.stream()
                    .map(roleName -> {
                        try {
                            return realmResource.roles().get(roleName).toRepresentation();
                        } catch (Exception e) {
                            log.warn("Role not found: {}. Skipping...", roleName);
                            return null;
                        }
                    })
                    .filter(role -> role != null)
                    .collect(Collectors.toList());

            if (!roles.isEmpty()) {
                realmResource.users().get(userId).roles().realmLevel().add(roles);
            }
        } catch (Exception e) {
            log.error("Error assigning roles: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to assign roles: " + e.getMessage(), e);
        }
    }

    public UserRepresentation getUserById(String userId) {
        try {
            return keycloak.realm(realm).users().get(userId).toRepresentation();
        } catch (Exception e) {
            log.error("Error fetching user by ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch user: " + e.getMessage(), e);
        }
    }

    public List<UserRepresentation> searchUsers(String search) {
        try {
            return keycloak.realm(realm).users().search(search);
        } catch (Exception e) {
            log.error("Error searching users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search users: " + e.getMessage(), e);
        }
    }

    public void deleteUser(String userId) {
        try {
            keycloak.realm(realm).users().delete(userId);
            log.info("User deleted: {}", userId);
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage(), e);
        }
    }
}

package Operimus.SLMS.business_layer.user_management_module.service;

import Operimus.SLMS.business_layer.user_management_module.dto.UpdateUserDTO;
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

            // Set password credentials during user creation
            // This avoids a separate API call and potential 400 errors
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setTemporary(false);
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(userDTO.getPassword());
            user.setCredentials(List.of(credential));
            log.info("Credentials configured for user creation");

            // Create user with password in a single operation
            Response response = usersResource.create(user);

            if (response.getStatus() != 201) {
                String errorMessage = response.readEntity(String.class);
                log.error("Failed to create user: {} - {}", response.getStatus(), errorMessage);
                throw new RuntimeException("Failed to create user: " + errorMessage);
            }

            String userId = CreatedResponseUtil.getCreatedId(response);
            log.info("User created successfully with ID: {} (including password)", userId);

            // Assign user to groups based on roles
            // Users inherit roles from their group membership
            log.info("Assigning user to groups based on roles for user: {}", userId);
            for (String roleName : userDTO.getRoles()) {
                assignUserToGroup(userId, roleName);
            }
            log.info("Group assignment completed for user: {}", userId);

            // Fetch and return the created user with groups
            UserRepresentation createdUser = usersResource.get(userId).toRepresentation();
            var groups = usersResource.get(userId).groups();
            if (groups != null && !groups.isEmpty()) {
                createdUser.setGroups(groups.stream().map(g -> g.getName()).collect(java.util.stream.Collectors.toList()));
            }
            return createdUser;

        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }

    /**
     * Assigns a user to a Keycloak group based on their role.
     * Maps role names to group names and adds the user to the appropriate group.
     * Users automatically inherit all roles associated with the group.
     */
    private void assignUserToGroup(String userId, String roleName) {
        try {
            RealmResource realmResource = keycloak.realm(realm);

            // Map role names to group names
            String groupName = mapRoleToGroupName(roleName);

            if (groupName == null) {
                log.warn("No group mapping found for role: {}. Skipping group assignment.", roleName);
                return;
            }

            // Search for the group by name
            var groups = realmResource.groups().groups(groupName, 0, 1);

            if (groups.isEmpty()) {
                log.error("Group not found: {}. User will not have proper permissions.", groupName);
                return;
            }

            String groupId = groups.get(0).getId();

            // Add user to group
            realmResource.users().get(userId).joinGroup(groupId);
            log.info("User {} assigned to group: {} (mapped from role: {})", userId, groupName, roleName);

        } catch (Exception e) {
            log.error("Error assigning user to group for role {}: {}", roleName, e.getMessage(), e);
            // Don't throw exception - allow user creation to succeed even if group assignment fails
        }
    }

    /**
     * Maps role names to Keycloak group names.
     * This mapping connects the application's role concept with Keycloak's group structure.
     */
    private String mapRoleToGroupName(String roleName) {
        return switch (roleName) {
            case "LOGISTICS_MANAGER" -> "Logistics Managers";
            case "WAREHOUSE_STAFF" -> "Warehouse";
            case "CUSTOMER_SERVICE_REP" -> "Customer Service";
            case "DRIVER" -> "Drivers";
            case "CUSTOMER" -> "Customers";
            default -> {
                log.warn("Unknown role name: {}. No group mapping available.", roleName);
                yield null;
            }
        };
    }

    public UserRepresentation getUserById(String userId) {
        try {
            UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();
            // Fetch and set groups for the user
            var groups = keycloak.realm(realm).users().get(userId).groups();
            if (groups != null && !groups.isEmpty()) {
                user.setGroups(groups.stream().map(g -> g.getName()).collect(java.util.stream.Collectors.toList()));
            }
            return user;
        } catch (Exception e) {
            log.error("Error fetching user by ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch user: " + e.getMessage(), e);
        }
    }

    public List<UserRepresentation> getAllUsers() {
        try {
            List<UserRepresentation> users = keycloak.realm(realm).users().list();
            // Fetch groups for each user
            for (UserRepresentation user : users) {
                try {
                    var groups = keycloak.realm(realm).users().get(user.getId()).groups();
                    if (groups != null && !groups.isEmpty()) {
                        user.setGroups(groups.stream().map(g -> g.getName()).collect(java.util.stream.Collectors.toList()));
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch groups for user {}: {}", user.getId(), e.getMessage());
                }
            }
            return users;
        } catch (Exception e) {
            log.error("Error fetching all users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch all users: " + e.getMessage(), e);
        }
    }

    /**
     * Get users by role name.
     * Fetches all users and filters by checking their realm-level role mappings.
     */
    public List<UserRepresentation> getUsersByRole(String roleName) {
        try {
            log.info("Fetching users with role: {}", roleName);
            RealmResource realmResource = keycloak.realm(realm);
            List<UserRepresentation> users = realmResource.users().list();
            
            List<UserRepresentation> usersWithRole = users.stream()
                    .filter(user -> {
                        try {
                            // Get realm-level roles for this user
                            List<RoleRepresentation> userRoles = realmResource.users()
                                    .get(user.getId())
                                    .roles()
                                    .realmLevel()
                                    .listEffective(); // listEffective includes inherited roles from groups
                            
                            boolean hasRole = userRoles.stream()
                                    .anyMatch(role -> role.getName().equalsIgnoreCase(roleName));
                            
                            if (hasRole) {
                                log.debug("User {} has role {}", user.getUsername(), roleName);
                            }
                            
                            return hasRole;
                        } catch (Exception e) {
                            log.warn("Failed to check roles for user {}: {}", user.getId(), e.getMessage());
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            
            log.info("Found {} users with role {}", usersWithRole.size(), roleName);
            return usersWithRole;
        } catch (Exception e) {
            log.error("Error fetching users by role {}: {}", roleName, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch users by role: " + e.getMessage(), e);
        }
    }

    public List<UserRepresentation> searchUsers(String search) {
        try {
            List<UserRepresentation> users = keycloak.realm(realm).users().search(search);
            // Fetch groups for each user
            for (UserRepresentation user : users) {
                try {
                    var groups = keycloak.realm(realm).users().get(user.getId()).groups();
                    if (groups != null && !groups.isEmpty()) {
                        user.setGroups(groups.stream().map(g -> g.getName()).collect(java.util.stream.Collectors.toList()));
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch groups for user {}: {}", user.getId(), e.getMessage());
                }
            }
            return users;
        } catch (Exception e) {
            log.error("Error searching users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search users: " + e.getMessage(), e);
        }
    }

    public UserRepresentation updateUser(String userId, UpdateUserDTO updateUserDTO) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Get existing user
            UserRepresentation user = usersResource.get(userId).toRepresentation();

            // Update user fields (only if provided)
            if (updateUserDTO.getUsername() != null && !updateUserDTO.getUsername().isEmpty()) {
                user.setUsername(updateUserDTO.getUsername());
            }
            if (updateUserDTO.getEmail() != null && !updateUserDTO.getEmail().isEmpty()) {
                user.setEmail(updateUserDTO.getEmail());
            }
            if (updateUserDTO.getFirstName() != null && !updateUserDTO.getFirstName().isEmpty()) {
                user.setFirstName(updateUserDTO.getFirstName());
            }
            if (updateUserDTO.getLastName() != null && !updateUserDTO.getLastName().isEmpty()) {
                user.setLastName(updateUserDTO.getLastName());
            }

            // Update the user
            usersResource.get(userId).update(user);
            log.info("User updated successfully: {}", userId);

            // Update roles/groups if provided
            if (updateUserDTO.getRoles() != null && !updateUserDTO.getRoles().isEmpty()) {
                // First, remove user from all current groups
                var currentGroups = usersResource.get(userId).groups();
                for (var group : currentGroups) {
                    usersResource.get(userId).leaveGroup(group.getId());
                }

                // Assign new groups based on roles
                for (String roleName : updateUserDTO.getRoles()) {
                    assignUserToGroup(userId, roleName);
                }
                log.info("User roles updated successfully: {}", userId);
            }

            // Return updated user with groups
            UserRepresentation updatedUser = usersResource.get(userId).toRepresentation();
            var groups = usersResource.get(userId).groups();
            if (groups != null && !groups.isEmpty()) {
                updatedUser.setGroups(groups.stream().map(g -> g.getName()).collect(java.util.stream.Collectors.toList()));
            }
            return updatedUser;

        } catch (Exception e) {
            log.error("Error updating user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
        }
    }

    public void updatePassword(String userId, String newPassword) {
        try {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setTemporary(false);
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);

            keycloak.realm(realm).users().get(userId).resetPassword(credential);
            log.info("Password updated successfully for user: {}", userId);
        } catch (Exception e) {
            log.error("Error updating password: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update password: " + e.getMessage(), e);
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

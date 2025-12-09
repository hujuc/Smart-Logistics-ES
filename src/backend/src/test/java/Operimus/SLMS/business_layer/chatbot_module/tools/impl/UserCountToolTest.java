package Operimus.SLMS.business_layer.chatbot_module.tools.impl;

import Operimus.SLMS.business_layer.chatbot_module.dto.ToolResult;
import Operimus.SLMS.business_layer.chatbot_module.dto.UserContext;
import Operimus.SLMS.business_layer.user_management_module.service.KeycloakUserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCountToolTest {

    @Mock
    private KeycloakUserService keycloakUserService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserCountTool userCountTool;

    private UserContext managerContext;
    private UserContext customerServiceContext;

    @BeforeEach
    void setUp() {
        managerContext = new UserContext("manager1", "LOGISTICS_MANAGER", Set.of("LOGISTICS_MANAGER"));
        customerServiceContext = new UserContext("cs1", "CUSTOMER_SERVICE", Set.of("CUSTOMER_SERVICE"));
    }

    @Test
    void shouldReturnTotalUserCount_whenNoRoleFilter() throws JsonProcessingException {
        // Given
        List<UserRepresentation> users = createUserList(15);
        when(keycloakUserService.getAllUsers()).thenReturn(users);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"totalUsers\":15,\"roleFilter\":\"all\"}");

        // When
        ToolResult result = userCountTool.execute(Map.of(), managerContext);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.result()).contains("totalUsers");
        verify(keycloakUserService).getAllUsers();
        verify(keycloakUserService, never()).getUsersByRole(anyString());
    }

    @Test
    void shouldReturnFilteredUserCount_whenRoleProvided() throws JsonProcessingException {
        // Given
        List<UserRepresentation> drivers = createUserList(5);
        when(keycloakUserService.getUsersByRole("DRIVER")).thenReturn(drivers);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"totalUsers\":5,\"roleFilter\":\"DRIVER\"}");

        Map<String, Object> params = new HashMap<>();
        params.put("role", "DRIVER");

        // When
        ToolResult result = userCountTool.execute(params, managerContext);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.result()).contains("DRIVER");
        verify(keycloakUserService).getUsersByRole("DRIVER");
        verify(keycloakUserService, never()).getAllUsers();
    }

    @Test
    void shouldReturnFilteredUserCount_whenCustomerRoleProvided() throws JsonProcessingException {
        // Given
        List<UserRepresentation> customers = createUserList(10);
        when(keycloakUserService.getUsersByRole("CUSTOMER")).thenReturn(customers);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"totalUsers\":10,\"roleFilter\":\"CUSTOMER\"}");

        Map<String, Object> params = new HashMap<>();
        params.put("role", "CUSTOMER");

        // When
        ToolResult result = userCountTool.execute(params, customerServiceContext);

        // Then
        assertThat(result.success()).isTrue();
        verify(keycloakUserService).getUsersByRole("CUSTOMER");
    }

    @Test
    void shouldHandleEmptyRoleParameter() throws JsonProcessingException {
        // Given
        List<UserRepresentation> users = createUserList(20);
        when(keycloakUserService.getAllUsers()).thenReturn(users);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"totalUsers\":20,\"roleFilter\":\"all\"}");

        Map<String, Object> params = new HashMap<>();
        params.put("role", "   ");  // Blank role

        // When
        ToolResult result = userCountTool.execute(params, managerContext);

        // Then
        assertThat(result.success()).isTrue();
        verify(keycloakUserService).getAllUsers();  // Should fall back to all users
        verify(keycloakUserService, never()).getUsersByRole(anyString());
    }

    @Test
    void shouldHandleServiceException() throws JsonProcessingException {
        // Given
        when(keycloakUserService.getAllUsers()).thenThrow(new RuntimeException("Keycloak error"));

        // When
        ToolResult result = userCountTool.execute(Map.of(), managerContext);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Failed to get user count");
        assertThat(result.errorMessage()).contains("Keycloak error");
    }

    @Test
    void shouldHandleJsonProcessingException() throws JsonProcessingException {
        // Given
        List<UserRepresentation> users = createUserList(5);
        when(keycloakUserService.getAllUsers()).thenReturn(users);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("JSON error") {});

        // When
        ToolResult result = userCountTool.execute(Map.of(), managerContext);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Failed to get user count");
    }

    @Test
    void shouldHaveCorrectToolMetadata() {
        // When/Then
        assertThat(userCountTool.getName()).isEqualTo("user_count");
        assertThat(userCountTool.getDescription()).contains("total number of users");
        assertThat(userCountTool.getAllowedRoles()).containsExactlyInAnyOrder("LOGISTICS_MANAGER", "CUSTOMER_SERVICE");
        assertThat(userCountTool.getParameters()).hasSize(1);
        assertThat(userCountTool.getParameters().get(0).name()).isEqualTo("role");
        assertThat(userCountTool.getParameters().get(0).required()).isFalse();
    }

    private List<UserRepresentation> createUserList(int count) {
        List<UserRepresentation> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            UserRepresentation user = new UserRepresentation();
            user.setId("user" + i);
            user.setUsername("user" + i);
            users.add(user);
        }
        return users;
    }
}

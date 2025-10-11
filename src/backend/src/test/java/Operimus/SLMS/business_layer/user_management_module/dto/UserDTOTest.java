package Operimus.SLMS.business_layer.user_management_module.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User",
                Arrays.asList("CUSTOMER")
        );

        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        assertTrue(violations.isEmpty());
    }

    @Test
    void whenUsernameIsBlank_thenViolation() {
        UserDTO userDTO = new UserDTO(
                "",
                "test@example.com",
                "password123",
                "Test",
                "User",
                Arrays.asList("CUSTOMER")
        );

        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Username is required"));
    }

    @Test
    void whenUsernameIsNull_thenViolation() {
        UserDTO userDTO = new UserDTO(
                null,
                "test@example.com",
                "password123",
                "Test",
                "User",
                Arrays.asList("CUSTOMER")
        );

        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Username is required")));
    }

    @Test
    void whenEmailIsBlank_thenViolation() {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "",
                "password123",
                "Test",
                "User",
                Arrays.asList("CUSTOMER")
        );

        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email is required")));
    }

    @Test
    void whenEmailIsInvalid_thenViolation() {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "invalid-email",
                "password123",
                "Test",
                "User",
                Arrays.asList("CUSTOMER")
        );

        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email must be valid")));
    }

    @Test
    void whenEmailIsNull_thenViolation() {
        UserDTO userDTO = new UserDTO(
                "testuser",
                null,
                "password123",
                "Test",
                "User",
                Arrays.asList("CUSTOMER")
        );

        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email is required")));
    }

    @Test
    void whenPasswordIsBlank_thenViolation() {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "test@example.com",
                "",
                "Test",
                "User",
                Arrays.asList("CUSTOMER")
        );

        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password is required")));
    }

    @Test
    void whenPasswordIsNull_thenViolation() {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "test@example.com",
                null,
                "Test",
                "User",
                Arrays.asList("CUSTOMER")
        );

        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password is required")));
    }

    @Test
    void whenFirstNameIsBlank_thenViolation() {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "test@example.com",
                "password123",
                "",
                "User",
                Arrays.asList("CUSTOMER")
        );

        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("First name is required")));
    }

    @Test
    void whenFirstNameIsNull_thenViolation() {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "test@example.com",
                "password123",
                null,
                "User",
                Arrays.asList("CUSTOMER")
        );

        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("First name is required")));
    }

    @Test
    void whenLastNameIsBlank_thenViolation() {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "",
                Arrays.asList("CUSTOMER")
        );

        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Last name is required")));
    }

    @Test
    void whenLastNameIsNull_thenViolation() {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                null,
                Arrays.asList("CUSTOMER")
        );

        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Last name is required")));
    }

    @Test
    void whenRolesIsEmpty_thenViolation() {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User",
                Collections.emptyList()
        );

        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("At least one role is required")));
    }

    @Test
    void whenRolesIsNull_thenViolation() {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User",
                null
        );

        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("At least one role is required")));
    }

    @Test
    void whenMultipleFieldsInvalid_thenMultipleViolations() {
        UserDTO userDTO = new UserDTO(
                "",
                "invalid-email",
                "",
                "",
                "",
                Collections.emptyList()
        );

        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        assertEquals(7, violations.size());
    }

    @Test
    void whenValidEmailFormats_thenNoViolations() {
        String[] validEmails = {
                "test@example.com",
                "user.name@example.com",
                "user+tag@example.co.uk",
                "test123@test-domain.com"
        };

        for (String email : validEmails) {
            UserDTO userDTO = new UserDTO(
                    "testuser",
                    email,
                    "password123",
                    "Test",
                    "User",
                    Arrays.asList("CUSTOMER")
            );

            Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
            assertTrue(violations.isEmpty(), "Email should be valid: " + email);
        }
    }

    @Test
    void whenMultipleRoles_thenNoViolations() {
        UserDTO userDTO = new UserDTO(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User",
                Arrays.asList("CUSTOMER", "CUSTOMER_SERVICE_REP", "DRIVER")
        );

        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        assertTrue(violations.isEmpty());
    }
}

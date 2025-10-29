package Operimus.SLMS.business_layer.user_management_module.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for updating user information
 * All fields are optional - only provided fields will be updated
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDTO {

    private String username;

    @Email(message = "Email must be valid")
    private String email;

    private String firstName;

    private String lastName;

    private List<String> roles;
}

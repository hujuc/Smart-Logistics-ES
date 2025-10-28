package Operimus.SLMS.business_layer.user_management_module.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordUpdateDTO {

    @NotBlank(message = "Password is required")
    private String password;
}

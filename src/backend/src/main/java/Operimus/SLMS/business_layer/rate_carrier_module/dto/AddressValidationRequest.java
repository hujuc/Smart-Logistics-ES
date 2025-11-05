package Operimus.SLMS.business_layer.rate_carrier_module.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressValidationRequest {

    @Size(max = 64)
    private String name;

    @Size(max = 128)
    private String company;

    @NotBlank
    @Size(max = 128)
    private String addressLine1;

    @Size(max = 128)
    private String addressLine2;

    @NotBlank
    @Size(max = 64)
    private String city;

    @Size(max = 64)
    private String stateOrProvince;

    @NotBlank
    @Size(max = 32)
    private String postalCode;

    @NotBlank
    @Size(max = 2)
    private String country;

    private Boolean residential;

    @Size(max = 32)
    private String phone;

    @Size(max = 128)
    private String email;
}


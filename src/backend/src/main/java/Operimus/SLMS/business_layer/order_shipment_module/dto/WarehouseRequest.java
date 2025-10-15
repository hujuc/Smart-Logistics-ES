package Operimus.SLMS.business_layer.order_shipment_module.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class WarehouseRequest {

    @NotBlank
    @Size(max = 64)
    private String code;

    @NotBlank
    private String name;

    @NotBlank
    private String addressLine1;

    private String addressLine2;

    @NotBlank
    private String city;

    @NotBlank
    private String stateOrProvince;

    @NotBlank
    private String country;

    @NotBlank
    private String postalCode;

    @NotNull
    private Boolean active;

    private String shippoLocationToken;

    private String shipstationWarehouseId;
}

package Operimus.SLMS.business_layer.order_module.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponse {

    private Long id;
    private String code;
    private String name;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String stateOrProvince;
    private String country;
    private String postalCode;
    private boolean active;
    private String shippoLocationToken;
    private String shipstationWarehouseId;
}

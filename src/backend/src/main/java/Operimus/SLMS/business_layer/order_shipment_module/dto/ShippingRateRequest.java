package Operimus.SLMS.business_layer.order_shipment_module.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
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
public class ShippingRateRequest {

    @NotNull
    @Valid
    private Address fromAddress;

    @NotNull
    @Valid
    private Address toAddress;

    @NotNull
    @Size(min = 1)
    @Valid
    private List<Parcel> parcels;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {

        @Size(max = 64)
        private String name;

        @Size(max = 128)
        private String company;

        @NotNull
        @Size(max = 128)
        private String addressLine1;

        @Size(max = 128)
        private String addressLine2;

        @NotNull
        @Size(max = 64)
        private String city;

        @Size(max = 64)
        private String stateOrProvince;

        @NotNull
        @Size(max = 32)
        private String postalCode;

        @NotNull
        @Size(max = 2)
        private String country;

        private Boolean residential;

        @Size(max = 32)
        private String phone;

        @Size(max = 128)
        private String email;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parcel {

        @NotNull
        private BigDecimal weight;

        @NotNull
        private String weightUnit;

        private BigDecimal length;
        private BigDecimal width;
        private BigDecimal height;
        private String distanceUnit;
    }
}


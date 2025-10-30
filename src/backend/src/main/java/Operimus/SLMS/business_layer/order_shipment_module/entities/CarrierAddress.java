package Operimus.SLMS.business_layer.order_shipment_module.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CarrierAddress {

    @Column(name = "address_line1", length = 128)
    private String addressLine1;

    @Column(name = "address_line2", length = 128)
    private String addressLine2;

    @Column(name = "address_city", length = 128)
    private String city;

    @Column(name = "address_state", length = 128)
    private String stateOrProvince;

    @Column(name = "address_postal_code", length = 32)
    private String postalCode;

    @Column(name = "address_country", length = 64)
    private String country;
}

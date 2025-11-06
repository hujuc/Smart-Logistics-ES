package Operimus.SLMS.business_layer.rate_carrier_module.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "carriers")
public class Carrier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 512)
    private String description;

    @Column(name = "support_email", length = 128)
    private String supportEmail;

    @Column(name = "support_phone", length = 64)
    private String supportPhone;

    @Column(length = 256)
    private String website;

    @Column(name = "shippo_carrier_id", length = 128)
    private String shippoCarrierId;

    @Column(name = "shippo_account_id", length = 128)
    private String shippoAccountId;

    @Column(name = "shipengine_carrier_id", length = 128)
    private String shipEngineCarrierId;

    @Column(name = "shipengine_account_number", length = 128)
    private String shipEngineAccountNumber;

    @Column(name = "service_levels_summary", length = 1024)
    private String serviceLevelsSummary;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Embedded
    private CarrierAddress address;

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}

package Operimus.SLMS.presentation_layer.driver_pwa_bff.dto;

import Operimus.SLMS.business_layer.order_module.entities.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyScheduleDeliveryResponse {
    private Long orderId;
    private String orderReference;
    private Integer stopNumber;
    private String customerName;
    private String address;
    private String phone;
    private OrderStatus status;
    private Double weight;
    private String weightUnit;
    private LocalDate estimatedDeliveryDate;
    private String shippingCity;
    private String shippingPostalCode;
}

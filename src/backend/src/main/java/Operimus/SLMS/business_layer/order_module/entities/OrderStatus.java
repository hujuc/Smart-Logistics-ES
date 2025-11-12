package Operimus.SLMS.business_layer.order_module.entities;

public enum OrderStatus {
    CREATED,              // Customer created order
    READY_FOR_PICKUP,     // Order ready for carrier pickup (legacy)
    COLLECTED,            // Order collected by carrier (legacy)
    ASSIGNED_TO_SHIPMENT, // Order added to a shipment
    IN_TRANSIT,           // Shipment is in transit
    DELIVERED,            // Order delivered to customer
    CANCELLED             // Order cancelled
}

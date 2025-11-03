package Operimus.SLMS.business_layer.order_shipment_module.entities;

public enum OrderStatus {
    CREATED,              // Customer created order
    ASSIGNED_TO_SHIPMENT, // Order added to a shipment
    IN_TRANSIT,           // Shipment is in transit
    DELIVERED,            // Order delivered to customer
    CANCELLED             // Order cancelled
}

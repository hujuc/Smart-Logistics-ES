package Operimus.SLMS.business_layer.rate_carrier_module.entities;

/**
 * Enum representing the various states of a shipment in the system.
 */
public enum ShipmentStatus {
    /**
     * Shipment created and assigned to driver, awaiting driver acceptance
     */
    PENDING_ACCEPTANCE,

    /**
     * Driver has accepted the shipment and is ready to start delivery
     */
    ACCEPTED,

    /**
     * Shipment is currently in transit (driver is delivering orders)
     */
    IN_TRANSIT,

    /**
     * All orders in shipment have been successfully delivered
     */
    COMPLETED,

    /**
     * Shipment encountered exceptions during delivery
     */
    EXCEPTION
}

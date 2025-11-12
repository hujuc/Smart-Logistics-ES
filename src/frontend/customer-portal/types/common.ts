/**
 * Common types shared across the application
 */

export type Priority = "Low" | "Medium" | "High" | "Critical";

export type IssueStatus = "Open" | "In Progress" | "Resolved" | "Closed";

// Backend enum values - must match Java OrderStatus enum
export type ShipmentOrderStatus =
    | "CREATED"
    | "READY_FOR_PICKUP"
    | "COLLECTED"
    | "ASSIGNED_TO_SHIPMENT"
    | "IN_TRANSIT"
    | "DELIVERED"
    | "CANCELLED";
    
export type IssueType = 
  | "Delivery Delay"
  | "Failed Delivery"
  | "Address Issue"
  | "Damaged Package"
  | "Missing Package"
  | "Wrong Item"
  | "Other"

export type Carrier = {
    id: string;
    name: string;
    service: string;
    logo: string;
    features: string[];
    cost?: number;
    transitTime?: string;
    reliability?: number;
    totalShipments?: number;
    onTimeDelivery?: number;
};


export interface DeliveryItem {
  id: string
  trackingNumber: string
  currentLocation: string
  destination: string
  estimatedArrival: string
  status: ShipmentOrderStatus
  driver: string
  vehicle: string
  progress: number
}
export interface ActiveDeliveriesProps {
  deliveries: DeliveryItem[]
  onViewDetails?: (id: string) => void
  onContactDriver?: (id: string) => void
}

export interface Order {
    id: number;
    reference: string;
    description: string;
    status: ShipmentOrderStatus;
    shippingAddress: string;
    shippingAddressLine1?: string;
    shippingAddressLine2?: string;
    shippingCity?: string;
    shippingStateOrProvince?: string;
    shippingPostalCode?: string;
    shippingCountry?: string;
    shippingPhone?: string;
    shippingEmail?: string;
    estimatedDeliveryDate: Date;
    warehouseId: number;
    shippoShipmentId: string;
    shippoTransactionId: string;
    shipstationOrderId: string;
    shipstationCarrierCode: string;
    shipstationServiceCode: string;
    trackingNumber: string;
    customerId?: string;
    createdAt: Date;
    updatedAt: Date;
    weight: number;
    priority: Priority;
    origin: string;
    actualDelivery?: Date;
    scannedAt?: Date;
}

export interface OrderDocument {
    orderId: number;
    documentId: string;
    objectKey: string;
    fileName: string;
    contentType: string;
    size: number;
    url: string;
}

export interface OrderEvent {
    id: number;
    orderId: number;
    eventType: ShipmentOrderStatus;
    occurredAt: string;
    notes: string;
}

export interface Warehouse {
    id: number;
    code: string;
    name: string;
    addressLine1: string;
    addressLine2?: string;
    city: string;
    stateOrProvince: string;
    country: string;
    postalCode: string;
    active: boolean;
}

export interface ContactInfo {
    phone: string;
    email: string;
}

export interface Address {
    street?: string;
    city: string;
    postalCode?: string;
    country?: string;
    fullAddress: string; // For simplified storage
}

export interface StatCard {
    value: number;
    trend?: number;
    label?: string;
    alert?: string;
    hasWarning?: boolean;
    color?: string;
}

export interface DateRange {
    from: Date;
    to: Date;
}

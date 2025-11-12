/**
 * Common types shared across the application
 */

export type Priority = "Low" | "Medium" | "High" | "Critical";

export type IssueStatus = "Open" | "In Progress" | "Escalated" | "Resolved" | "Closed";

// Backend enum values - must match Java OrderStatus enum
export type OrderStatus =
    | "CREATED"
    | "READY_FOR_PICKUP"
    | "COLLECTED"
    | "ASSIGNED_TO_SHIPMENT"
    | "IN_TRANSIT"
    | "DELIVERED"
    | "CANCELLED";

// Legacy alias for backward compatibility during migration
export type ShipmentOrderStatus = OrderStatus;
    
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
    shipmentId?: number;
    shipmentCode?: string;
    carrierId?: number;
    carrierCode?: string;
    carrierName?: string;
    shippoShipmentId: string;
    shippoTransactionId: string;
    shipstationOrderId: string;
    shipstationCarrierCode: string;
    shipstationServiceCode: string;
    trackingNumber: string;

    // Parcel dimensions for shipping rate calculation
    weight?: number;
    weightUnit?: string;
    length?: number;
    width?: number;
    height?: number;
    distanceUnit?: string;

    createdAt: Date;
    updatedAt: Date;
    priority: Priority;
    origin: string;
    actualDelivery?: Date;
    scannedAt?: Date;
}

export interface OrderEvent {
    id: number;
    orderId: number;
    eventType: ShipmentOrderStatus;
    occurredAt: string;
    notes: string;
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

export interface ShipmentDocument {
    shipmentId: number;
    orderId?: number;
    documentId: string;
    objectKey?: string;
    fileName: string;
    contentType: string;
    size: number;
    url: string;
    carrierCode?: string;
    serviceLevel?: string;
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
    value: number | string;
    trend?: number | null;
    label?: string;
    alert?: string;
    hasWarning?: boolean;
    color?: string;
}

export interface DateRange {
    from: Date;
    to: Date;
}

// User Management Types
export interface User {
    id: string;
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    enabled: boolean;
    emailVerified: boolean;
    createdTimestamp?: number;
    groups?: string[] | null;
    attributes?: Record<string, string[]>;
}

export interface CreateUserRequest {
    username: string;
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    roles: string[];
}

export interface UpdateUserRequest {
    username?: string;
    email?: string;
    firstName?: string;
    lastName?: string;
    roles?: string[];
}

export interface PasswordUpdateRequest {
    password: string;
}

export type UserRole =
    | "LOGISTICS_MANAGER"
    | "WAREHOUSE_STAFF"
    | "CUSTOMER_SERVICE_REP"
    | "DRIVER"
    | "CUSTOMER";

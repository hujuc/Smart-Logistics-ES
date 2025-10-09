/**
 * Common types shared across the application
 */

export type Priority = "Low" | "Medium" | "High" | "Critical";

export type IssueStatus = "Open" | "In Progress" | "Resolved" | "Closed";

export type ShipmentOrderStatus =
    | "Pending Labels"
    | "Ready for Dispatch"
    | "In Transit"
    | "Delivered"
    | "Cancelled"
    | "Returned";
    
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

export interface Order {
    id: number;
    reference: string;
    description: string;
    status: ShipmentOrderStatus;
    shippingAddress: string;
    estimatedDeliveryDate: Date;
    warehouseId: number;
    shippoShipmentId: string;
    shippoTransactionId: string;
    shipstationOrderId: string;
    shipstationCarrierCode: string;
    shipstationServiceCode: string;
    trackingNumber: string;
    createdAt: Date;
    updatedAt: Date;
    weight: number;
    priority: Priority;
    origin: string;
    actualDelivery?: Date;
    scannedAt?: Date;
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


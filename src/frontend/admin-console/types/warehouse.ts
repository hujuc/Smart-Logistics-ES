/**
 * Warehouse-related types
 */

import type { ShipmentOrderStatus, Priority } from "./common"

// ===========================
// Warehouse Entity Types
// ===========================

export interface Warehouse {
  id: number
  code: string
  name: string
  addressLine1: string
  addressLine2?: string
  city: string
  stateOrProvince: string
  country: string
  postalCode: string
  active: boolean
  shippoLocationToken?: string
  shipstationWarehouseId?: string
}

export interface WarehouseFormData {
  code: string
  name: string
  addressLine1: string
  addressLine2?: string
  city: string
  stateOrProvince: string
  country: string
  postalCode: string
  active: boolean
  shippoLocationToken?: string
  shipstationWarehouseId?: string
}

export type CreateWarehouseRequest = WarehouseFormData

export type UpdateWarehouseRequest = WarehouseFormData

// ===========================
// Warehouse Package Types
// ===========================

export interface WarehousePackage {
  id: string
  reference: string
  status: ShipmentOrderStatus
  priority: Priority
  origin: string
  destination: string
  scannedAt?: string
  weight?: number
}

export interface WarehouseStats {
  totalPackages: number
  readyForDispatch: number
  pendingLabels: number
  inTransit: number
}

export const STATUS_COLORS = {
  "CREATED": "bg-yellow-100 text-yellow-700",
  "ASSIGNED_TO_SHIPMENT": "bg-green-100 text-green-700",
  "IN_TRANSIT": "bg-blue-100 text-blue-700",
  "DELIVERED": "bg-gray-100 text-gray-700",
  "CANCELLED": "bg-red-100 text-red-700",
  "READY_FOR_PICKUP": "bg-purple-100 text-purple-700",
  "COLLECTED": "bg-indigo-100 text-indigo-700",
} as const

// Display labels for status values
export const STATUS_LABELS: Record<ShipmentOrderStatus, string> = {
  "CREATED": "Created",
  "ASSIGNED_TO_SHIPMENT": "Assigned to Shipment",
  "IN_TRANSIT": "In Transit",
  "DELIVERED": "Delivered",
  "CANCELLED": "Cancelled",
  "READY_FOR_PICKUP": "Ready for Pickup",
  "COLLECTED": "Collected",
}


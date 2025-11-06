/**
 * Warehouse-related types
 */

import type { ShipmentOrderStatus, Priority } from "./common"

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
  "READY_FOR_PICKUP": "bg-green-100 text-green-700",
  "COLLECTED": "bg-purple-100 text-purple-700",
  "ASSIGNED_TO_SHIPMENT": "bg-orange-100 text-orange-700",
  "IN_TRANSIT": "bg-blue-100 text-blue-700",
  "DELIVERED": "bg-gray-100 text-gray-700",
  "CANCELLED": "bg-red-100 text-red-700",
} as const

// Display labels for status values
export const STATUS_LABELS: Record<ShipmentOrderStatus, string> = {
  "CREATED": "Pending Labels",
  "READY_FOR_PICKUP": "Ready for Dispatch",
  "COLLECTED": "Collected",
  "ASSIGNED_TO_SHIPMENT": "Assigned to Shipment",
  "IN_TRANSIT": "In Transit",
  "DELIVERED": "Delivered",
  "CANCELLED": "Cancelled",
}


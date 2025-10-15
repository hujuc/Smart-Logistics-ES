/**
 * Warehouse-related types
 */

import { ShipmentOrderStatus, Priority } from "./common"

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
  "Pending Labels": "bg-yellow-100 text-yellow-700",
  "Ready for Dispatch": "bg-green-100 text-green-700",
  "In Transit": "bg-blue-100 text-blue-700",
  "Delivered": "bg-gray-100 text-gray-700",
  "Cancelled": "bg-red-100 text-red-700",
  "Returned": "bg-orange-100 text-orange-700",
} as const


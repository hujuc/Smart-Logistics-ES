/**
 * Logistics and operations-related types
 */

import type { Priority, ShipmentOrderStatus, StatCard } from "./common"

export interface LogisticsKPIs {
  ordersToday: StatCard
  pendingPickups: StatCard
  inTransitNow: StatCard
  deliveredToday: StatCard
}

export interface CarrierPerformance {
  carrierId: string
  carrierName: string
  totalDeliveries: number
  onTimeRate: number
  averageDeliveryTime: number // in hours
  costPerDelivery: number
  rating: number // out of 5
}

export interface DeliveryMetrics {
  date: string
  totalOrders: number
  delivered: number
  inTransit: number
  delayed: number
  failed: number
}

export interface ShipmentCard {
  id: string
  weight: string
  destination: string
  priority: Priority
  date: string
}


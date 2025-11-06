"use client"

import { Package, Truck, CheckCircle, XCircle, AlertTriangle, Clock } from "lucide-react"
import { Order, ShipmentOrderStatus } from "@/types/common"

interface WarehouseDetailsStatsProps {
  orders: Order[]
}

export function WarehouseDetailsStats({ orders }: WarehouseDetailsStatsProps) {
  // Calculate statistics
  const totalOrders = orders.length
  const inTransit = orders.filter(o => o.status === "IN_TRANSIT").length
  const delivered = orders.filter(o => o.status === "DELIVERED").length
  const pendingPickup = orders.filter(o =>
    o.status === "CREATED" || o.status === "READY_FOR_PICKUP"
  ).length
  const collected = orders.filter(o => o.status === "COLLECTED").length
  const cancelled = orders.filter(o => o.status === "CANCELLED").length

  const stats = [
    {
      label: "Total Orders",
      value: totalOrders,
      icon: Package,
      color: "bg-blue-100 text-blue-700",
    },
    {
      label: "Pending Pickup",
      value: pendingPickup,
      icon: Clock,
      color: "bg-yellow-100 text-yellow-700",
    },
    {
      label: "In Transit",
      value: inTransit,
      icon: Truck,
      color: "bg-purple-100 text-purple-700",
    },
    {
      label: "Delivered",
      value: delivered,
      icon: CheckCircle,
      color: "bg-green-100 text-green-700",
    },
    {
      label: "Collected",
      value: collected,
      icon: Package,
      color: "bg-indigo-100 text-indigo-700",
    },
    {
      label: "Cancelled",
      value: cancelled,
      icon: XCircle,
      color: "bg-red-100 text-red-700",
    },
  ]

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
      {stats.map((stat) => (
        <div
          key={stat.label}
          className="bg-white border rounded-lg shadow-sm p-4"
        >
          <div className="flex items-center gap-3">
            <div className={`${stat.color} rounded-lg p-2`}>
              <stat.icon className="h-5 w-5" />
            </div>
            <div>
              <p className="text-sm text-gray-600">{stat.label}</p>
              <p className="text-2xl font-bold text-black">{stat.value}</p>
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}

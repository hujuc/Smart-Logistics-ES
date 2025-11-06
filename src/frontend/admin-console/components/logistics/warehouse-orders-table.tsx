"use client"

import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Package, ExternalLink } from "lucide-react"
import { Order, ShipmentOrderStatus } from "@/types/common"
import Link from "next/link"

interface WarehouseOrdersTableProps {
  orders: Order[]
  warehouseId: number
}

const STATUS_COLORS: Record<ShipmentOrderStatus, string> = {
  CREATED: "bg-yellow-100 text-yellow-700",
  READY_FOR_PICKUP: "bg-green-100 text-green-700",
  COLLECTED: "bg-purple-100 text-purple-700",
  IN_TRANSIT: "bg-blue-100 text-blue-700",
  DELIVERED: "bg-gray-100 text-gray-700",
  CANCELLED: "bg-red-100 text-red-700",
}

const STATUS_LABELS: Record<ShipmentOrderStatus, string> = {
  CREATED: "Pending Labels",
  READY_FOR_PICKUP: "Ready for Dispatch",
  COLLECTED: "Collected",
  IN_TRANSIT: "In Transit",
  DELIVERED: "Delivered",
  CANCELLED: "Cancelled",
}

export function WarehouseOrdersTable({ orders, warehouseId }: WarehouseOrdersTableProps) {
  const formatDate = (date: Date | string) => {
    return new Date(date).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    })
  }

  return (
    <div className="bg-white border rounded-lg shadow-sm">
      <div className="p-6">
        <div className="flex items-center gap-2 mb-4">
          <Package className="h-5 w-5 text-gray-600" />
          <h2 className="text-xl font-semibold text-black">
            Orders ({orders.length})
          </h2>
        </div>

        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Reference</TableHead>
              <TableHead>Description</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Carrier</TableHead>
              <TableHead>Tracking Number</TableHead>
              <TableHead>Est. Delivery</TableHead>
              <TableHead>Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {orders.map((order) => (
              <TableRow key={order.id}>
                <TableCell className="font-medium font-mono text-sm">
                  {order.reference}
                </TableCell>
                <TableCell className="max-w-xs truncate">
                  {order.description || "-"}
                </TableCell>
                <TableCell>
                  <span
                    className={`inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ${
                      STATUS_COLORS[order.status]
                    }`}
                  >
                    {STATUS_LABELS[order.status]}
                  </span>
                </TableCell>
                <TableCell>
                  {order.carrierName || "-"}
                </TableCell>
                <TableCell className="font-mono text-sm">
                  {order.trackingNumber || "-"}
                </TableCell>
                <TableCell>
                  {order.estimatedDeliveryDate
                    ? formatDate(order.estimatedDeliveryDate)
                    : "-"}
                </TableCell>
                <TableCell>
                  <Link
                    href={`/interfaces/warehouse/package/${order.id}`}
                    className="text-blue-600 hover:text-blue-800 inline-flex items-center gap-1"
                    title="View order details"
                  >
                    <ExternalLink className="h-4 w-4" />
                  </Link>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>

        {orders.length === 0 && (
          <div className="text-center py-8 text-gray-500">
            No orders found for this warehouse.
          </div>
        )}
      </div>
    </div>
  )
}

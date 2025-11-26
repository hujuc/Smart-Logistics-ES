"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { useRouter } from "next/navigation"
import { isAuthenticated, debugToken } from "@/lib/shared-auth"
import { QuickStats } from "@/components/warehouse/quick-stats"
import { OrderTable } from "@/components/warehouse/order-table"
import type { WarehousePackage } from "@/types"
import { warehouseService } from "@/lib/services/warehouse.service"
import { formatOrderShippingAddress } from "@/lib/address"

export default function WarehousePage() {
  const router = useRouter()
  const [packages, setPackages] = useState<WarehousePackage[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    // Debug token information
    debugToken();

    // Check if user is authenticated
    if (!isAuthenticated()) {
      setError("You must be logged in to view orders. Redirecting to login...")
      setIsLoading(false)
      setTimeout(() => {
        window.location.href = "/"
      }, 2000)
      return
    }

    loadOrders()
  }, [])

  const loadOrders = async () => {
    try {
      setIsLoading(true)
      setError(null)
      const orders = await warehouseService.getAllOrders()

      // Transform backend Order to frontend WarehousePackage
      const transformedPackages: WarehousePackage[] = orders.map(order => ({
        id: order.id.toString(),
        reference: order.reference,
        status: order.status,
        priority: order.priority,
        origin: order.origin || "-",
        destination: formatOrderShippingAddress(order),
        scannedAt: order.scannedAt ? new Date(order.scannedAt).toLocaleString() : undefined,
        weight: order.weight,
      }))

      setPackages(transformedPackages)
    } catch (err) {
      console.error('Failed to load orders:', err)
      setError('Failed to load orders. Please try again.')
    } finally {
      setIsLoading(false)
    }
  }

  const handleDeleteOrder = async (id: string) => {
    if (!confirm(`Are you sure you want to delete order ${id}?`)) {
      return
    }

    try {
      await warehouseService.deleteOrder(parseInt(id))
      // Reload orders after successful delete
      await loadOrders()
      alert('Order deleted successfully')
    } catch (err) {
      console.error('Failed to delete order:', err)
      alert('Failed to delete order. This might be due to authentication issues with the DELETE endpoint.')
    }
  }

  const stats = {
    totalPackages: packages.length,
    readyForDispatch: packages.filter(p => p.status === "ASSIGNED_TO_SHIPMENT").length,
    pendingLabels: packages.filter(p => p.status === "CREATED").length,
    inTransit: packages.filter(p => p.status === "IN_TRANSIT").length,
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading orders...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <p className="text-red-600 mb-4">{error}</p>
          <Button onClick={loadOrders}>Try Again</Button>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-black">Order Processing Dashboard</h1>
          <p className="text-gray-600 mt-1">Manage incoming orders and prepare for dispatch</p>
          <p className="text-sm text-gray-500 mt-1">Note: Order creation is now handled by customers through the Customer Portal</p>
        </div>
      </div>

      {/* Quick Stats */}
      <QuickStats stats={stats} />

      {/* Orders Table */}
      <OrderTable
        packages={packages}
        onViewPackage={(id) => router.push(`/interfaces/warehouse/package/${id}`)}
        onDeletePackage={handleDeleteOrder}
      />
    </div>
  )
}

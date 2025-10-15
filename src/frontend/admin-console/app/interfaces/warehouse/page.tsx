"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { QuickStats } from "@/components/warehouse/quick-stats"
import { OrderTable } from "@/components/warehouse/order-table"
import type { WarehousePackage } from "@/types"
import { warehouseService } from "@/lib/services/warehouse.service"

export default function WarehousePage() {
  const router = useRouter()
  const [packages, setPackages] = useState<WarehousePackage[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
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
        destination: order.shippingAddress,
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

  const handleCollectPackage = async (id: string) => {
    try {
      await warehouseService.confirmPickup(parseInt(id))
      // Reload orders to reflect the status change
      await loadOrders()
    } catch (err) {
      console.error('Failed to collect package:', err)
      alert('Failed to mark package as collected. Please try again.')
    }
  }

  const stats = {
    totalPackages: packages.length,
    readyForDispatch: packages.filter(p => p.status === "READY_FOR_PICKUP").length,
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
        </div>
        <Link href="/interfaces/warehouse/new-package">
          <Button>Add New Package</Button>
        </Link>
      </div>

      {/* Quick Stats */}
      <QuickStats stats={stats} />

      {/* Orders Table */}
      <OrderTable
        packages={packages}
        onViewPackage={(id) => router.push(`/interfaces/warehouse/package/${id}`)}
        onCollectPackage={handleCollectPackage}
      />
    </div>
  )
}


"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { ArrowLeft, MapPin, CheckCircle, XCircle } from "lucide-react"
import Link from "next/link"
import { warehouseManagementService } from "@/lib/services/warehouse-management.service"
import { apiFetch, API_ENDPOINTS } from "@/lib/api"
import { WarehouseDetailsStats } from "@/components/logistics/warehouse-details-stats"
import { WarehouseOrdersTable } from "@/components/logistics/warehouse-orders-table"
import type { Warehouse } from "@/types/warehouse"
import type { Order } from "@/types/common"

interface WarehouseDetailsPageProps {
  params: Promise<{ id: string }>
}

export default function WarehouseDetailsPage({ params }: WarehouseDetailsPageProps) {
  const [warehouseId, setWarehouseId] = useState<string | null>(null)
  const [warehouse, setWarehouse] = useState<Warehouse | null>(null)
  const [orders, setOrders] = useState<Order[]>([])
  const [filteredOrders, setFilteredOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [statusFilter, setStatusFilter] = useState<string>("all")

  // Unwrap params Promise
  useEffect(() => {
    params.then(p => setWarehouseId(p.id))
  }, [params])

  // Fetch warehouse and orders data
  useEffect(() => {
    if (!warehouseId) return

    const loadData = async () => {
      try {
        setLoading(true)
        setError(null)

        // Fetch warehouse details
        const warehouseData = await warehouseManagementService.getById(parseInt(warehouseId))
        setWarehouse(warehouseData)

        // Fetch orders for this warehouse
        const ordersData = await apiFetch<Order[]>(
          API_ENDPOINTS.logistics.warehouseOrders(parseInt(warehouseId))
        )
        setOrders(ordersData)
        setFilteredOrders(ordersData)
      } catch (err) {
        console.error("Failed to load warehouse details:", err)
        setError("Failed to load warehouse details. Please try again.")
      } finally {
        setLoading(false)
      }
    }

    loadData()
  }, [warehouseId])

  // Filter orders when status filter changes
  useEffect(() => {
    if (statusFilter === "all") {
      setFilteredOrders(orders)
    } else {
      setFilteredOrders(orders.filter(order => order.status === statusFilter))
    }
  }, [statusFilter, orders])

  const formatAddress = (warehouse: Warehouse) => {
    const parts = [
      warehouse.addressLine1,
      warehouse.addressLine2,
      warehouse.city,
      warehouse.stateOrProvince,
      warehouse.postalCode,
      warehouse.country,
    ].filter(Boolean)
    return parts.join(", ")
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-gray-500">Loading warehouse details...</p>
      </div>
    )
  }

  if (error || !warehouse) {
    return (
      <div className="space-y-4">
        <Link href="/interfaces/logistics/warehouses">
          <Button variant="outline">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Warehouses
          </Button>
        </Link>
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <p className="text-red-800">{error || "Warehouse not found"}</p>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link href="/interfaces/logistics/warehouses">
            <Button variant="outline" size="sm">
              <ArrowLeft className="h-4 w-4" />
            </Button>
          </Link>
          <div>
            <h1 className="text-3xl font-bold text-black">{warehouse.name}</h1>
            <p className="text-sm text-gray-600 mt-1">
              Code: <span className="font-mono">{warehouse.code}</span>
            </p>
          </div>
        </div>
      </div>

      {/* Warehouse Information Card */}
      <div className="bg-white border rounded-lg shadow-sm p-6">
        <h2 className="text-xl font-semibold text-black mb-4">Warehouse Information</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <div className="flex items-start gap-2 mb-3">
              <MapPin className="h-5 w-5 text-gray-600 mt-0.5" />
              <div>
                <p className="text-sm font-medium text-gray-700">Address</p>
                <p className="text-sm text-gray-600 mt-1">{formatAddress(warehouse)}</p>
              </div>
            </div>
          </div>

          <div>
            <div className="space-y-3">
              <div>
                <p className="text-sm font-medium text-gray-700">Status</p>
                <div className="flex items-center gap-2 mt-1">
                  {warehouse.active ? (
                    <>
                      <CheckCircle className="h-4 w-4 text-green-600" />
                      <span className="text-sm text-green-600 font-medium">Active</span>
                    </>
                  ) : (
                    <>
                      <XCircle className="h-4 w-4 text-gray-600" />
                      <span className="text-sm text-gray-600 font-medium">Inactive</span>
                    </>
                  )}
                </div>
              </div>

              {(warehouse.shippoLocationToken || warehouse.shipstationWarehouseId) && (
                <div>
                  <p className="text-sm font-medium text-gray-700">Integrations</p>
                  <div className="flex gap-2 mt-1">
                    {warehouse.shippoLocationToken && (
                      <span className="bg-blue-100 text-blue-700 px-2 py-1 rounded text-xs">
                        Shippo
                      </span>
                    )}
                    {warehouse.shipstationWarehouseId && (
                      <span className="bg-purple-100 text-purple-700 px-2 py-1 rounded text-xs">
                        ShipStation
                      </span>
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Statistics */}
      <WarehouseDetailsStats orders={orders} />

      {/* Orders Section */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-semibold text-black">Orders</h2>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-2 border rounded-md text-sm"
          >
            <option value="all">All Status</option>
            <option value="CREATED">Created</option>
            <option value="ASSIGNED_TO_SHIPMENT">Assigned to Shipment</option>
            <option value="IN_TRANSIT">In Transit</option>
            <option value="DELIVERED">Delivered</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>

        <WarehouseOrdersTable orders={filteredOrders} />
      </div>
    </div>
  )
}
